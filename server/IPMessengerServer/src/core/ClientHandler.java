package core;

import database.GroupMemberDAO;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {
    private static final int MAX_MESSAGE_LENGTH = 1_048_576;

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private User currentUser;

    // Mapa estático para mantener usuarios conectados (username -> ClientHandler)
    private static final ConcurrentHashMap<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

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
                if (!readFully(input, lenBytes, 4)) {
                    break;
                }

                int messageLength = ((lenBytes[0] & 0xFF) << 24) |
                                    ((lenBytes[1] & 0xFF) << 16) |
                                    ((lenBytes[2] & 0xFF) << 8)  |
                                    (lenBytes[3] & 0xFF);
                if (messageLength <= 0 || messageLength > MAX_MESSAGE_LENGTH) {
                    sendError("Longitud de mensaje inválida: " + messageLength);
                    continue;
                }

                byte[] compressedData = new byte[messageLength];
                if (!readFully(input, compressedData, messageLength)) {
                    sendError("Payload incompleto");
                    break;
                }

                Map<String, Object> request;
                String command;
                Map<String, Object> data;

                try {
                    byte[] decompressed = LZ77Compressor.decompress(compressedData);
                    String json = new String(decompressed, "UTF-8");
                    Logger.log("Recibido: " + json);

                    request = JSONParser.fromJson(json, Map.class);
                    if (request == null) {
                        sendError("Solicitud inválida");
                        continue;
                    }
                    command = (String) request.get("command");
                    data = (Map<String, Object>) request.get("data");
                } catch (RuntimeException ex) {
                    Logger.log("Payload inválido en ClientHandler: " + ex.getMessage());
                    sendError("Payload inválido: " + ex.getMessage());
                    continue;
                }

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
                    case Protocol.CMD_MARK_MSG_READ:
                        handleMarkMsgRead(data);
                        break;
                    case Protocol.CMD_CREATE_GROUP:
                        handleCreateGroup(data);
                        break;
                    case Protocol.CMD_INVITE_TO_GROUP:
                        handleInviteToGroup(data);
                        break;
                    case Protocol.CMD_ACCEPT_GROUP_INVITE:
                        handleAcceptGroupInvite(data);
                        break;
                    case Protocol.CMD_REJECT_GROUP_INVITE:
                        handleRejectGroupInvite(data);
                        break;
                    case Protocol.CMD_GET_GROUP_INVITES:
                        sendGroupInviteList();
                        break;
                    case Protocol.CMD_GET_GROUP_HISTORY:
                        handleGetGroupHistory(data);
                        break;
                    case Protocol.CMD_LEAVE_GROUP:
                        handleLeaveGroup(data);
                        break;
                    case "GET_FRIEND_HISTORY":
                        handleGetFriendHistory(data);
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
            userManager.resetFailedLoginAttempts(username);
            // Registrar en conectados
            connectedClients.put(username, this);
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
            sendGroupInviteList();
            scheduleUserListBroadcast();
        } else {
            int failedAttempts = userManager.recordFailedLoginAttempt(username);
            Map<String, Object> response = new HashMap<>();
            if (failedAttempts >= 3) {
                response.put("status", Protocol.RES_NEED_RECOVER);
                response.put("message", "Demasiados intentos fallidos. Recupera tu contraseña para continuar.");
            } else {
                response.put("status", Protocol.RES_LOGIN_FAIL);
                int remaining = Math.max(0, 3 - failedAttempts);
                response.put("message", "Credenciales incorrectas. Intentos restantes: " + remaining);
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

    private void handleGetFriendHistory(Map<String, Object> data) throws SQLException, IOException {
        int friendId = ((Number) data.get("friendId")).intValue();
        int limit   = data.containsKey("limit") ? ((Number) data.get("limit")).intValue() : 100;

        MessageManager mm = new MessageManager();
        List<models.Message> msgs = mm.getFriendHistory(currentUser.getId(), friendId, limit);

        List<Map<String, Object>> list = new ArrayList<>();
        for (models.Message m : msgs) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", m.getContent());
            map.put("senderUsername", getUsernameById(m.getSenderId()));
            map.put("timestamp", m.getTimestamp().toString());
            list.add(map);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", Protocol.RES_FRIEND_HISTORY);
        resp.put("friendId", friendId);
        resp.put("messages", list);
        sendMessage(resp);
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
        for (User user : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("online", connectedClients.containsKey(user.getUsername()));
            result.add(map);
        }
        return result;
    }

    private void scheduleUserListBroadcast() {
        ThreadPoolManager.execute(this::broadcastUserList);
    }

    private void broadcastUserList() {
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

    private void sendFriendListToUser(int userId) {
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

    private void sendGroupListToUser(int userId) {
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

    private void sendGroupInviteList() throws SQLException, IOException {
        GroupManager gm = new GroupManager();
        List<Map<String, Object>> invites = gm.getPendingGroupInvites(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_GROUP_INVITE_LIST);
        response.put("invites", invites);
        sendMessage(response);
    }

    private void sendGroupInviteListToUser(int userId) {
        try {
            User target = new UserDAO().findById(userId);
            if (target == null) return;
            ClientHandler handler = connectedClients.get(target.getUsername());
            if (handler != null) {
                handler.sendGroupInviteList();
            }
        } catch (Exception ignored) {
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
            // Añadimos también el nombre de usuario del remitente para que el cliente lo muestre
            map.put("senderUsername", getUsernameById(m.getSenderId()));
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
        // Necesitamos obtener el username del amigo por su ID, luego buscarlo en connectedClients
        String friendUsername = getUsernameById(friendId);
        ClientHandler friendHandler = connectedClients.get(friendUsername);

        if (friendHandler != null) {
            // Destinatario online: guardamos como entregado y enviamos en tiempo real
            mm.sendFriendMessage(currentUser.getId(), friendId, content, "delivered");
            Map<String, Object> newMsg = new HashMap<>();
            newMsg.put("status", Protocol.RES_NEW_MESSAGE);
            newMsg.put("senderId", currentUser.getId());
            newMsg.put("senderUsername", currentUser.getUsername());
            newMsg.put("content", content);
            newMsg.put("type", "friend");
            friendHandler.sendMessage(newMsg);
        } else {
            // Destinatario offline: guardamos como pendiente
            mm.sendFriendMessage(currentUser.getId(), friendId, content, "pending");
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

    private void handleSendTempMsg(Map<String, Object> data) throws IOException, SQLException {
        if (data == null) {
            sendError("Datos inválidos para mensaje temporal");
            return;
        }

        String content = (String) data.get("content");
        Integer targetId = (data.get("targetUserId") instanceof Number n) ? n.intValue() : null;
        if (content == null || content.trim().isEmpty()) {
            sendError("El mensaje temporal no puede estar vacío");
            return;
        }

        if (targetId == null) {
            sendError("targetUserId es obligatorio para mensaje temporal 1 a 1");
            return;
        }

        // Si el remitente envía a sí mismo, ignoramos (no tiene sentido)
        if (targetId == currentUser.getId()) {
            sendError("No puedes enviarte un mensaje a ti mismo");
            return;
        }

        User targetUser = new UserDAO().findById(targetId);
        if (targetUser == null) {
            sendError("El usuario destino no existe");
            return;
        }

        FriendshipManager friendshipManager = new FriendshipManager();
        if (friendshipManager.areFriends(currentUser.getId(), targetId)) {
            sendError("Los mensajes temporales no se permiten entre amigos");
            return;
        }

        // Guardamos el mensaje **siempre** como "pending"
        MessageManager mm = new MessageManager();
        mm.saveTemporaryMessage(currentUser.getId(), targetId, content);

        // Construir el mensaje que se enviará a los clientes conectados (solo a los online)
        Map<String, Object> newMsg = new HashMap<>();
        newMsg.put("status", Protocol.RES_NEW_MESSAGE);
        newMsg.put("senderId", currentUser.getId());
        newMsg.put("senderUsername", currentUser.getUsername());
        newMsg.put("content", content);
        newMsg.put("type", "temporary");   // nuevo tipo “temporary”

        // Si el destinatario está online enviamos en tiempo real, de lo contrario no.
        ClientHandler targetHandler = connectedClients.get(targetUser.getUsername());
        if (targetHandler != null) {                      // está conectado
            newMsg.put("targetUserId", targetId);
            targetHandler.sendMessage(newMsg);
            // También podemos marcar el mensaje como “delivered” en BD, pero no es obligatorio para la UI.
        } else {
            // Destinatario offline → devolvemos al remitente que el mensaje quedó pendiente
            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "PENDING");                 // <‑‑ nuevo status que el cliente debe interpretar
            resp.put("message", "Mensaje almacenado como pendiente");
            sendMessage(resp);
            return;
        }

        // Si llegamos aquí, el mensaje se entregó en tiempo real → respondemos OK
        sendOk();
    }

    private void handleMarkMsgRead(Map<String, Object> data) throws SQLException, IOException {
        Integer msgId = (data != null && data.get("messageId") instanceof Number n) ? n.intValue() : null;
        if (msgId == null) {
            sendError("messageId ausente o no numérico");
            return;
        }

        MessageManager mm = new MessageManager();
        // Cambiamos el estado a "sent"
        mm.markMessageRead(msgId);      // método que crearemos a continuación
        sendOk();                       // responde { "status":"OK" }
    }    

    private void handleSendGroupMsg(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();
        String content = data.get("content") != null ? String.valueOf(data.get("content")).trim() : "";
        if (content.isEmpty()) {
            sendError("El mensaje no puede estar vacío");
            return;
        }
        GroupMemberDAO memberDAO = new GroupMemberDAO();
        if (!memberDAO.isAcceptedMember(groupId, currentUser.getId())) {
            sendError("No eres miembro de este grupo");
            return;
        }
        MessageManager mm = new MessageManager();
        mm.sendGroupMessage(currentUser.getId(), groupId, content);
        // Reenviar a todos los miembros del grupo que estén conectados
        GroupManager gm = new GroupManager();
        List<User> members = gm.getGroupMembers(groupId);
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
        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_OK);
        sendMessage(response);
    }

    private void handleCreateGroup(Map<String, Object> data) throws SQLException, IOException {
        if (data == null) {
            sendError("Datos inválidos para crear grupo");
            return;
        }

        String groupName = (String) data.get("groupName");
        Object rawInvited = data.get("invitedUserIds");
        List<Integer> invitedUserIds = convertToIntegerList(rawInvited);
        GroupManager gm = new GroupManager();
        int groupId;

        try {
            groupId = gm.createGroup(groupName, currentUser.getId(), invitedUserIds);
        } catch (IllegalArgumentException ex) {
            sendError(ex.getMessage());
            return;
        }

        if (groupId != -1) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", Protocol.RES_OK);
            response.put("groupId", groupId);
            sendMessage(response);

            sendGroupListToUser(currentUser.getId());

            // Notificar invitación a los usuarios agregados si están conectados
            for (int userId : invitedUserIds) {
                sendGroupInviteListToUser(userId);
            }
        } else {
            sendError("No se pudo crear el grupo.");
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

    private void handleInviteToGroup(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();
        List<Integer> invitedUserIds = convertToIntegerList(data.get("invitedUserIds"));
        if (invitedUserIds.isEmpty()) {
            sendError("Selecciona al menos un amigo para invitar");
            return;
        }

        GroupManager gm = new GroupManager();
        int added = gm.inviteMembersToGroup(groupId, currentUser.getId(), invitedUserIds);
        if (added <= 0) {
            sendError("No se pudo invitar. Verifica que sean tus amigos y que no estén ya en el grupo.");
            return;
        }

        sendOk();
        sendGroupList();
        for (int userId : invitedUserIds) {
            sendGroupInviteListToUser(userId);
        }
    }

    private void handleLeaveGroup(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();

        GroupManager gm = new GroupManager();
        // Obtener miembros actuales antes de eliminar para notificarles
        List<User> members = gm.getGroupMembers(groupId);
        java.util.List<Integer> memberIds = new java.util.ArrayList<>();
        for (User u : members) memberIds.add(u.getId());

        boolean deleted = gm.leaveGroup(groupId, currentUser.getId());

        // Notificar a todos los miembros anteriores para que refresquen su lista de grupos
        for (Integer uid : memberIds) {
            sendGroupListToUser(uid);
        }
        // También notificar al propio usuario que salió
        sendGroupListToUser(currentUser.getId());

        sendOk();
    }

    private void handleAcceptGroupInvite(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();
        GroupMemberDAO memberDAO = new GroupMemberDAO();
        if (!"invited".equals(memberDAO.getMemberStatus(groupId, currentUser.getId()))) {
            sendError("No tienes una invitación pendiente para este grupo");
            return;
        }

        GroupManager gm = new GroupManager();
        gm.acceptInvitation(groupId, currentUser.getId());
        sendOk();
        sendGroupList();
        sendGroupInviteList();
    }

    private void handleRejectGroupInvite(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();
        GroupMemberDAO memberDAO = new GroupMemberDAO();
        if (!"invited".equals(memberDAO.getMemberStatus(groupId, currentUser.getId()))) {
            sendError("No tienes una invitación pendiente para este grupo");
            return;
        }

        GroupManager gm = new GroupManager();
        gm.rejectInvitation(groupId, currentUser.getId());
        sendOk();
        sendGroupInviteList();
        sendGroupListToUser(currentUser.getId());
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

    private static boolean readFully(InputStream in, byte[] buffer, int length) throws IOException {
        int totalRead = 0;
        while (totalRead < length) {
            int r = in.read(buffer, totalRead, length - totalRead);
            if (r == -1) {
                return false;
            }
            totalRead += r;
        }
        return true;
    }

    private void disconnect() {
        try {
            if (currentUser != null) {
                new UserManager().setUserOffline(currentUser.getId());
                connectedClients.remove(currentUser.getUsername());
                scheduleUserListBroadcast();
            }
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void handleGetGroupHistory(Map<String, Object> data) throws SQLException, IOException {
        if (data == null || data.get("groupId") == null) {
            sendError("groupId requerido");
            return;
        }
        int groupId = ((Number) data.get("groupId")).intValue();
        int limit = 500;
        if (data.get("limit") instanceof Number limitNumber) {
            limit = Math.max(1, Math.min(1000, limitNumber.intValue()));
        }

        GroupMemberDAO memberDAO = new GroupMemberDAO();
        if (!memberDAO.isAcceptedMember(groupId, currentUser.getId())) {
            sendError("No eres miembro de este grupo");
            return;
        }

        MessageManager mm = new MessageManager();
        List<models.Message> mensajes = mm.getGroupMessages(groupId, limit);

        List<Map<String, Object>> listaMensajes = new java.util.ArrayList<>();
        for (models.Message m : mensajes) {
            Map<String, Object> map = new HashMap<>();
            map.put("content", m.getContent());
            map.put("senderId", m.getSenderId());
            String senderUsername = getUsernameById(m.getSenderId());
            map.put("senderUsername", senderUsername != null ? senderUsername : "Usuario");
            map.put("timestamp", m.getTimestamp() != null ? m.getTimestamp().toString() : "");
            listaMensajes.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", Protocol.RES_GROUP_HISTORY);
        response.put("groupId", groupId);
        response.put("messages", listaMensajes);

        sendMessage(response);
        Logger.log("[SERVER] Historial enviado para grupo " + groupId + " con " + listaMensajes.size() + " mensajes.");
    }
}