package core;

import database.UserDAO;
import logic.*;
import models.User;
import network.JSONParser;
import network.LZ77Compressor;
import network.Protocol;
import utils.Logger;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private User currentUser;
    private int loginAttempts = 0;

    // Mapa estático para mantener usuarios conectados (username -> ClientHandler)
    private static final Map<String, ClientHandler> connectedClients = new HashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            Logger.log("Cliente conectado desde " + socket.getInetAddress());

            while (true) {
                // Leer longitud del mensaje (4 bytes)
                byte[] lenBytes = new byte[4];
                int read = input.read(lenBytes);
                if (read == -1) break;
                int messageLength = ((lenBytes[0] & 0xFF) << 24) |
                                    ((lenBytes[1] & 0xFF) << 16) |
                                    ((lenBytes[2] & 0xFF) << 8)  |
                                    (lenBytes[3] & 0xFF);
                if (messageLength <= 0) continue;

                byte[] compressedData = new byte[messageLength];
                int totalRead = 0;
                while (totalRead < messageLength) {
                    int r = input.read(compressedData, totalRead, messageLength - totalRead);
                    if (r == -1) break;
                    totalRead += r;
                }
                byte[] decompressed = LZ77Compressor.decompress(compressedData);
                String json = new String(decompressed, "UTF-8");
                Logger.log("Recibido: " + json);

                Map<String, Object> request = JSONParser.fromJson(json, Map.class);
                String command = (String) request.get("command");
                Map<String, Object> data = (Map<String, Object>) request.get("data");

                if (command == null) {
                    sendError("Comando no especificado");
                    continue;
                }

                switch (command) {
                    case Protocol.CMD_LOGIN:
                        handleLogin(data);
                        break;
                    case Protocol.CMD_REGISTER:
                        handleRegister(data);
                        break;
                    case Protocol.CMD_RECOVER_PASSWORD:
                        handleRecover(data);
                        break;
                    case Protocol.CMD_GET_ALL_USERS:
                        handleGetAllUsers();
                        break;
                    case Protocol.CMD_GET_FRIENDS:
                        handleGetFriends();
                        break;
                    case Protocol.CMD_GET_GROUPS:
                        handleGetGroups();
                        break;
                    case Protocol.CMD_GET_FRIEND_INVITES:
                        handleGetFriendInvites();
                        break;
                    case Protocol.CMD_SEND_FRIEND_MSG:
                        handleSendFriendMsg(data);
                        break;
                    case Protocol.CMD_SEND_FRIEND_REQUEST:
                        handleSendFriendRequest(data);
                        break;
                    case Protocol.CMD_ACCEPT_FRIEND_REQUEST:
                        handleAcceptFriendRequest(data);
                        break;
                    case Protocol.CMD_REJECT_FRIEND_REQUEST:
                        handleRejectFriendRequest(data);
                        break;
                    case Protocol.CMD_SEND_GROUP_MSG:
                        handleSendGroupMsg(data);
                        break;
                    case Protocol.CMD_GET_PENDING_MSGS:
                        handleGetPendingMsgs();
                        break;
                    case Protocol.CMD_SEND_TEMP_MSG:
                        handleSendTempMsg(data);
                        break;
                    case Protocol.CMD_CREATE_GROUP:
                        handleCreateGroup(data);
                        break;
                    case Protocol.CMD_ACCEPT_GROUP_INVITE:
                        handleAcceptGroupInvite(data);
                        break;
                    case "GET_GROUP_HISTORY":
                        handleGetGroupHistory(data);
                        break;
            
                    // ... otros comandos
                    default:
                        sendError("Comando desconocido");
                }
            }
        } catch (Exception e) {
            Logger.log("Error en ClientHandler: " + e.getMessage());
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private void handleLogin(Map<String, Object> data) throws SQLException, IOException {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        UserManager userManager = new UserManager();
        User user = userManager.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            loginAttempts = 0;
            // Registrar en conectados
            synchronized (connectedClients) {
                connectedClients.put(username, this);
            }
            // Enviar éxito
            Map<String, Object> response = new HashMap<>();
            response.put("status", Protocol.RES_LOGIN_SUCCESS);
            response.put("userId", user.getId());
            sendMessage(response);
            // Enviar listas iniciales (amigos, grupos, usuarios, pendientes)
            sendFriendList();
            sendGroupList();
            sendAllUsers();
            sendPendingMessages();
            sendFriendInviteList();
            scheduleUserListBroadcast();
        } else {
            loginAttempts++;
            Map<String, Object> response = new HashMap<>();
            if (loginAttempts >= 3) {
                response.put("status", Protocol.RES_NEED_REGISTER);
                response.put("message", "Demasiados intentos fallidos. Regístrate o recupera contraseña.");
            } else {
                response.put("status", Protocol.RES_LOGIN_FAIL);
                response.put("message", "Credenciales incorrectas. Intentos restantes: " + (3 - loginAttempts));
            }
            sendMessage(response);
        }
    }

    private void handleRegister(Map<String, Object> data) throws SQLException, IOException {
        String username = (String) data.get("username");
        String password = (String) data.get("password");
        UserManager um = new UserManager();
        boolean ok = um.register(username, password);
        Map<String, Object> response = new HashMap<>();
        if (ok) {
            response.put("status", Protocol.RES_OK);
            response.put("message", "Registro exitoso. Ahora puedes iniciar sesión.");
        } else {
            response.put("status", Protocol.RES_ERROR);
            response.put("message", "El usuario ya existe.");
        }
        sendMessage(response);
    }

    private void handleRecover(Map<String, Object> data) throws SQLException, IOException {
        String username = (String) data.get("username");
        String newPassword = (String) data.get("newPassword");
        UserManager um = new UserManager();
        boolean ok = um.recoverPassword(username, newPassword);
        Map<String, Object> response = new HashMap<>();
        if (ok) {
            response.put("status", Protocol.RES_OK);
            response.put("message", "Contraseña restablecida.");
        } else {
            response.put("status", Protocol.RES_ERROR);
            response.put("message", "Usuario no encontrado.");
        }
        sendMessage(response);
    }

    private void handleGetFriends() throws SQLException, IOException {
        sendFriendList();
    }

    private void handleGetPendingMsgs() throws SQLException, IOException {
        sendPendingMessages();
    }

    private void handleGetFriendInvites() throws SQLException, IOException {
        sendFriendInviteList();
    }

    private void handleGetAllUsers() throws SQLException, IOException {
        // Obtener todos los usuarios menos el actual
        UserManager um = new UserManager(); // mejor crear un UserDAO
        // Simplificado: llamamos a un método que liste todos
        List<Map<String, Object>> users = getAllUsersExcept(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_USER_LIST);
        response.put("users", users);
        sendMessage(response);
    }

    private void handleGetGroups() throws SQLException, IOException {
        sendGroupList();
    }

    private List<Map<String, Object>> getAllUsersExcept(int userId) throws SQLException {
        List<User> users = new UserDAO().findAllExcept(userId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        synchronized (connectedClients) {
            for (User user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("username", user.getUsername());
                map.put("online", connectedClients.containsKey(user.getUsername()));
                result.add(map);
            }
        }
        return result;
    }

    private void scheduleUserListBroadcast() {
        ThreadPoolManager.execute(this::broadcastUserList);
    }

    private void broadcastUserList() {
        synchronized (connectedClients) {
            for (ClientHandler handler : connectedClients.values()) {
                if (handler != this && handler.currentUser != null) {
                    try {
                        handler.sendAllUsers();
                    } catch (Exception e) {
                        Logger.log("No se pudo actualizar la lista de usuarios: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void sendFriendList() throws SQLException, IOException {
        FriendshipManager fm = new FriendshipManager();
        List<User> friends = fm.getFriends(currentUser.getId());
        List<Map<String, Object>> friendList = new java.util.ArrayList<>();
        for (User f : friends) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("username", f.getUsername());
            map.put("online", "online".equals(f.getStatus()));
            friendList.add(map);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_FRIEND_LIST);
        response.put("friends", friendList);
        sendMessage(response);
    }

    private void sendFriendInviteList() throws SQLException, IOException {
        FriendshipManager fm = new FriendshipManager();
        List<Map<String, Object>> invites = fm.getPendingRequests(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_FRIEND_INVITE_LIST);
        response.put("invites", invites);
        sendMessage(response);
    }

    private void sendFriendInviteUpdateToUser(int userId) {
        synchronized (connectedClients) {
            try {
                User target = new UserDAO().findById(userId);
                if (target == null) return;
                ClientHandler handler = connectedClients.get(target.getUsername());
                if (handler != null) {
                    handler.sendFriendInviteList();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void sendFriendListToUser(int userId) {
        synchronized (connectedClients) {
            try {
                User target = new UserDAO().findById(userId);
                if (target == null) return;
                ClientHandler handler = connectedClients.get(target.getUsername());
                if (handler != null) {
                    handler.sendFriendList();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void sendGroupListToUser(int userId) {
        synchronized (connectedClients) {
            try {
                User target = new UserDAO().findById(userId);
                if (target == null) return;
                ClientHandler handler = connectedClients.get(target.getUsername());
                if (handler != null) {
                    handler.sendGroupList();
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void sendGroupList() throws SQLException, IOException {
        GroupManager gm = new GroupManager();
        List<models.Group> groups = gm.getGroupsForUser(currentUser.getId());
        List<Map<String, Object>> groupList = new java.util.ArrayList<>();
        for (models.Group g : groups) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", g.getId());
            map.put("name", g.getName());
            List<User> members = gm.getGroupMembers(g.getId());
            map.put("memberCount", members.size());
            List<String> memberNames = new java.util.ArrayList<>();
            for (User member : members) {
                memberNames.add(member.getUsername());
            }
            map.put("members", memberNames);
            groupList.add(map);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_GROUP_LIST);
        response.put("groups", groupList);
        sendMessage(response);
    }

    private void sendAllUsers() throws SQLException, IOException {
        handleGetAllUsers();
    }

    private void sendPendingMessages() throws SQLException, IOException {
        MessageManager mm = new MessageManager();
        List<models.Message> pending = mm.getPendingMessages(currentUser.getId());
        List<Map<String, Object>> pendingList = new java.util.ArrayList<>();
        for (models.Message m : pending) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSenderId());
            map.put("content", m.getContent());
            map.put("timestamp", m.getTimestamp().toString());
            pendingList.add(map);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_PENDING_MESSAGES);
        response.put("messages", pendingList);
        sendMessage(response);
    }

    private void handleSendFriendMsg(Map<String, Object> data) throws SQLException, IOException {
        int friendId = ((Number) data.get("friendId")).intValue();
        String content = (String) data.get("content");
        MessageManager mm = new MessageManager();
        mm.sendFriendMessage(currentUser.getId(), friendId, content);
        // Si el amigo está conectado, enviarle el mensaje en tiempo real
        ClientHandler friendHandler;
        synchronized (connectedClients) {
            // Necesitamos obtener el username del amigo por su ID, luego buscarlo en connectedClients
            String friendUsername = getUsernameById(friendId);
            friendHandler = connectedClients.get(friendUsername);
        }
        if (friendHandler != null) {
            Map<String, Object> newMsg = new HashMap<>();
            newMsg.put("status", Protocol.RES_NEW_MESSAGE);
            newMsg.put("senderId", currentUser.getId());
            newMsg.put("senderUsername", currentUser.getUsername());
            newMsg.put("content", content);
            newMsg.put("type", "friend");
            friendHandler.sendMessage(newMsg);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_OK);
        response.put("message", "Mensaje enviado");
        sendMessage(response);
    }

    private void handleSendFriendRequest(Map<String, Object> data) throws SQLException, IOException {
        int friendId = ((Number) data.get("friendId")).intValue();
        FriendshipManager fm = new FriendshipManager();
        boolean success = fm.sendFriendRequest(currentUser.getId(), friendId);
        if (!success) {
            sendError("No se pudo enviar la solicitud de amistad. Ya existe o el usuario ya es tu amigo.");
            return;
        }
        sendOk();
        sendFriendInviteUpdateToUser(friendId);
        sendFriendInviteList();
    }

    private void handleAcceptFriendRequest(Map<String, Object> data) throws SQLException, IOException {
        int requesterId = ((Number) data.get("requesterId")).intValue();
        FriendshipManager fm = new FriendshipManager();
        fm.acceptRequest(requesterId, currentUser.getId());
        sendOk();
        sendFriendList();
        sendFriendInviteList();
        sendFriendListToUser(requesterId);
        sendFriendInviteUpdateToUser(requesterId);
    }

    private void handleRejectFriendRequest(Map<String, Object> data) throws SQLException, IOException {
        int requesterId = ((Number) data.get("requesterId")).intValue();
        FriendshipManager fm = new FriendshipManager();
        fm.rejectRequest(requesterId, currentUser.getId());
        sendOk();
        sendFriendInviteList();
        sendFriendInviteUpdateToUser(requesterId);
    }

    private void handleSendTempMsg(Map<String, Object> data) throws IOException {
        String content = (String) data.get("content");
        if (content == null || content.trim().isEmpty()) {
            sendError("El mensaje general no puede estar vacío");
            return;
        }

        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("status", Protocol.RES_NEW_MESSAGE);
        newMsg.put("senderId", currentUser.getId());
        newMsg.put("senderUsername", currentUser.getUsername());
        newMsg.put("content", content);
        newMsg.put("type", "general");

        synchronized (connectedClients) {
            for (ClientHandler handler : connectedClients.values()) {
                if (handler != this) {
                    handler.sendMessage(newMsg);
                }
            }
        }
        sendOk();
    }
    

    private void handleSendGroupMsg(Map<String, Object> data) throws SQLException, IOException {
        int groupId = ((Number) data.get("groupId")).intValue();
        String content = (String) data.get("content");
        MessageManager mm = new MessageManager();
        mm.sendGroupMessage(currentUser.getId(), groupId, content);
        // Reenviar a todos los miembros del grupo que estén conectados
        GroupManager gm = new GroupManager();
        List<User> members = gm.getGroupMembers(groupId);
        synchronized (connectedClients) {
            for (User m : members) {
                if (m.getId() == currentUser.getId()) continue;
                ClientHandler ch = connectedClients.get(m.getUsername());
                if (ch != null) {
                    Map<String, Object> newMsg = new HashMap<>();
                    newMsg.put("status", Protocol.RES_NEW_MESSAGE);
                    newMsg.put("groupId", groupId);
                    newMsg.put("senderId", currentUser.getId());
                    newMsg.put("senderUsername", currentUser.getUsername());
                    newMsg.put("content", content);
                    newMsg.put("type", "group");
                    ch.sendMessage(newMsg);
                }
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_OK);
        sendMessage(response);
    }

    private void handleCreateGroup(Map<String, Object> data) throws SQLException, IOException {
        String groupName = (String) data.get("groupName");
        Object rawInvited = data.get("invitedUserIds");
        List<Integer> invitedUserIds = convertToIntegerList(rawInvited);
        GroupManager gm = new GroupManager();
        int groupId = gm.createGroup(groupName, currentUser.getId(), invitedUserIds);
        if (groupId != -1) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", Protocol.RES_OK);
            response.put("groupId", groupId);
            sendMessage(response);

            sendGroupListToUser(currentUser.getId());

            // Notificar inmediatamente a los usuarios agregados si están conectados
            for (int userId : invitedUserIds) {
                sendGroupListToUser(userId);
            }
        } else {
            sendError("No se pudo crear el grupo. Verifica que hayas seleccionado al menos un amigo y que sean tus amigos aceptados.");
        }
    }

    private List<Integer> convertToIntegerList(Object rawList) {
        List<Integer> result = new ArrayList<>();
        if (!(rawList instanceof List<?> list)) {
            return result;
        }
        for (Object item : list) {
            if (item instanceof Number number) {
                result.add(number.intValue());
            } else if (item instanceof String text) {
                try {
                    result.add(Integer.parseInt(text));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    private void handleAcceptGroupInvite(Map<String, Object> data) throws SQLException, IOException {
        int groupId = ((Number) data.get("groupId")).intValue();
        GroupManager gm = new GroupManager();
        gm.acceptInvitation(groupId, currentUser.getId());
        sendOk();
    }

    private synchronized void sendMessage(Map<String, Object> message) throws IOException {
        String json = JSONParser.toJson(message);
        byte[] data = json.getBytes("UTF-8");
        byte[] compressed = LZ77Compressor.compress(data);
        byte[] len = new byte[4];
        len[0] = (byte) ((compressed.length >> 24) & 0xFF);
        len[1] = (byte) ((compressed.length >> 16) & 0xFF);
        len[2] = (byte) ((compressed.length >> 8) & 0xFF);
        len[3] = (byte) (compressed.length & 0xFF);
        output.write(len);
        output.write(compressed);
        output.flush();
    }

    private void sendOk() throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", Protocol.RES_OK);
        sendMessage(resp);
    }

    private void sendError(String errMsg) throws IOException {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", Protocol.RES_ERROR);
        resp.put("message", errMsg);
        sendMessage(resp);
    }

    private String getUsernameById(int userId) throws SQLException {
        UserManager um = new UserManager();
        // Necesitamos un método findById en UserManager o usar UserDAO directamente
        // Por simplicidad, añade un método getUserById en UserManager.
        User u = new UserDAO().findById(userId);
        return u != null ? u.getUsername() : null;
    }

    private void disconnect() {
        try {
            if (currentUser != null) {
                new UserManager().setUserOffline(currentUser.getId());
                synchronized (connectedClients) {
                    connectedClients.remove(currentUser.getUsername());
                }
                scheduleUserListBroadcast();
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleGetGroupHistory(Map<String, Object> data) throws SQLException, IOException {
    int groupId = ((Number) data.get("groupId")).intValue();
    
    // Llamamos al MessageManager para recuperar los mensajes de la BD
    MessageManager mm = new MessageManager();
    List<models.Message> mensajes = mm.getGroupMessages(groupId); // Asegúrate de tener este método en MessageManager
    
    List<Map<String, Object>> listaMensajes = new java.util.ArrayList<>();
    for (models.Message m : mensajes) {
        Map<String, Object> map = new HashMap<>();
        map.put("content", m.getContent());
        map.put("senderUsername", getUsernameById(m.getSenderId()));
        map.put("timestamp", m.getTimestamp().toString());
        listaMensajes.add(map);
    }
    
    Map<String, Object> response = new HashMap<>();
    response.put("status", "RES_GROUP_HISTORY"); // Este status es el que espera tu Main.java
    response.put("groupId", groupId);
    response.put("messages", listaMensajes);
    
    sendMessage(response);
    Logger.log("[SERVER] Historial enviado para grupo " + groupId + " con " + listaMensajes.size() + " mensajes.");
}
}