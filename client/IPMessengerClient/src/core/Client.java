package core;

import network.JSONParser;
import network.LZ77Compressor;
import network.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.net.InetSocketAddress;

public class Client {
    public static final int DEFAULT_PORT = 12346;
    public static final int SOCKET_CONNECT_TIMEOUT_MS = 5_000;
    private static final int RESPONSE_TIMEOUT_SECONDS = 20;

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Thread readerThread;
    private final BlockingQueue<Map<String, Object>> inbox = new LinkedBlockingQueue<>();
    private volatile MessageListener messageListener;
    private volatile boolean bootstrapInProgress;

    public void connect(String host, int port) throws IOException {
        connect(host, port, SOCKET_CONNECT_TIMEOUT_MS);
    }

    public void connect(String host, int port, int timeoutMs) throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;
        }
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        readerThread = new Thread(this::readLoop, "ip-messenger-client-reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public Map<String, Object> register(String host, String username, String password) throws IOException {
        return register(host, DEFAULT_PORT, username, password);
    }

    public Map<String, Object> register(String host, int port, String username, String password) throws IOException {
        connect(host, port);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        Map<String, Object> response = sendCommand(Protocol.CMD_REGISTER, data);
        close();
        return response;
    }

    public Map<String, Object> recoverPassword(String host, String username, String newPassword) throws IOException {
        return recoverPassword(host, DEFAULT_PORT, username, newPassword);
    }

    public Map<String, Object> recoverPassword(String host, int port, String username, String newPassword) throws IOException {
        connect(host, port);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("newPassword", newPassword);
        Map<String, Object> response = sendCommand(Protocol.CMD_RECOVER_PASSWORD, data);
        close();
        return response;
    }

    public SessionData login(String host, String username, String password) throws IOException {
        return login(host, DEFAULT_PORT, username, password);
    }

    public SessionData login(String host, int port, String username, String password) throws IOException {
        connect(host, port);
        bootstrapInProgress = true;
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            data.put("password", password);
            sendCommandPayload(Protocol.CMD_LOGIN, data);

            SessionData session = new SessionData();
            session.setUsername(username);

            Map<String, Object> firstResponse = waitForResponse();
            String status = String.valueOf(firstResponse.get("status"));
            if (Protocol.RES_LOGIN_FAIL.equals(status) || Protocol.RES_NEED_REGISTER.equals(status)) {
                throw new IOException(String.valueOf(firstResponse.getOrDefault("message", "Credenciales incorrectas")));
            }
            if (!Protocol.RES_LOGIN_SUCCESS.equals(status)) {
                throw new IOException("Respuesta de login inesperada: " + status);
            }

            session.absorb(firstResponse);
            Set<String> pendingBootstrap = new HashSet<>();
            pendingBootstrap.add(Protocol.RES_FRIEND_LIST);
            pendingBootstrap.add(Protocol.RES_GROUP_LIST);
            pendingBootstrap.add(Protocol.RES_USER_LIST);
            pendingBootstrap.add(Protocol.RES_PENDING_MESSAGES);
            pendingBootstrap.add(Protocol.RES_FRIEND_INVITE_LIST);
            pendingBootstrap.add(Protocol.RES_GROUP_INVITE_LIST);

            while (!pendingBootstrap.isEmpty()) {
                Map<String, Object> bootstrapMessage = waitForResponse();
                session.absorb(bootstrapMessage);
                pendingBootstrap.remove(String.valueOf(bootstrapMessage.get("status")));
            }
            return session;
        } finally {
            bootstrapInProgress = false;
        }
    }

    public Map<String, Object> sendFriendMessage(int friendId, String content) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("friendId", friendId);
        data.put("content", content);
        return sendCommand(Protocol.CMD_SEND_FRIEND_MSG, data);
    }

    public Map<String, Object> sendGeneralMessage(String content) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        return sendCommand(Protocol.CMD_SEND_TEMP_MSG, data);
    }

    public Map<String, Object> sendGroupMessage(int groupId, String content) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("content", content);
        return sendCommand(Protocol.CMD_SEND_GROUP_MSG, data);
    }

    public Map<String, Object> sendFriendRequest(int friendId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("friendId", friendId);
        return sendCommand(Protocol.CMD_SEND_FRIEND_REQUEST, data);
    }

    public Map<String, Object> sendTemporaryMessage(int targetUserId, String content) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("targetUserId", targetUserId);   // <-- nuevo campo
        data.put("content", content);
        return sendCommand(Protocol.CMD_SEND_TEMP_MSG, data);
    }

    public Map<String, Object> markMessageRead(int messageId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        return sendCommand(Protocol.CMD_MARK_MSG_READ, data);
    }

    public Map<String, Object> getPendingMessages() throws IOException {
        return sendCommand(Protocol.CMD_GET_PENDING_MSGS, new HashMap<>());
    }

    public Map<String, Object> acceptFriendRequest(int requesterId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("requesterId", requesterId);
        return sendCommand(Protocol.CMD_ACCEPT_FRIEND_REQUEST, data);
    }

    public Map<String, Object> rejectFriendRequest(int requesterId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("requesterId", requesterId);
        return sendCommand(Protocol.CMD_REJECT_FRIEND_REQUEST, data);
    }

    public Map<String, Object> createGroup(String groupName, java.util.List<Integer> invitedUserIds) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupName", groupName);
        data.put("invitedUserIds", invitedUserIds);
        return sendCommand(Protocol.CMD_CREATE_GROUP, data);
    }

    public Map<String, Object> getGroups() throws IOException {
        return sendCommand(Protocol.CMD_GET_GROUPS, new HashMap<>());
    }

    public Map<String, Object> getFriendHistory(int friendId, int limit) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("friendId", friendId);
        data.put("limit", limit);
        return sendCommand(Protocol.CMD_GET_FRIEND_HISTORY, data);
    }

    public Map<String, Object> getGroupHistory(int groupId, int limit) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("limit", limit);
        return sendCommand(Protocol.CMD_GET_GROUP_HISTORY, data);
    }

    public Map<String, Object> inviteToGroup(int groupId, java.util.List<Integer> invitedUserIds) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        data.put("invitedUserIds", invitedUserIds);
        return sendCommand(Protocol.CMD_INVITE_TO_GROUP, data);
    }

    public Map<String, Object> acceptGroupInvite(int groupId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return sendCommand(Protocol.CMD_ACCEPT_GROUP_INVITE, data);
    }

    public Map<String, Object> rejectGroupInvite(int groupId) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("groupId", groupId);
        return sendCommand(Protocol.CMD_REJECT_GROUP_INVITE, data);
    }

    public Map<String, Object> sendCommand(String command, Map<String, Object> data) throws IOException {
        sendCommandPayload(command, data);
        return waitForResponse();
    }

    private void sendCommandPayload(String command, Map<String, Object> data) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("command", command);
        request.put("data", data);
        sendMessage(request);
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void readLoop() {
        try {
            while (socket != null && !socket.isClosed()) {
                Map<String, Object> message = receiveMessage();
                String status = String.valueOf(message.get("status"));
                if (!bootstrapInProgress
                        && (Protocol.RES_NEW_MESSAGE.equals(status)
                            || Protocol.RES_USER_LIST.equals(status)
                            || Protocol.RES_FRIEND_LIST.equals(status)
                            || Protocol.RES_GROUP_LIST.equals(status)
                            || Protocol.RES_FRIEND_INVITE_LIST.equals(status)
                            || Protocol.RES_GROUP_INVITE_LIST.equals(status))) {
                    MessageListener listener = messageListener;
                    if (listener != null) {
                        listener.onMessage(message);
                    } else {
                        inbox.offer(message);
                    }
                } else {
                    inbox.offer(message);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private Map<String, Object> waitForResponse() throws IOException {
        try {
            Map<String, Object> response = inbox.poll(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (response == null) {
                throw new IOException("El servidor no respondió a tiempo");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Espera interrumpida");
        }
    }

    public void sendMessage(Map<String, Object> message) throws IOException {
        String json = JSONParser.toJson(message);
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        byte[] compressed = LZ77Compressor.compress(payload);
        byte[] lengthHeader = new byte[4];
        lengthHeader[0] = (byte) ((compressed.length >> 24) & 0xFF);
        lengthHeader[1] = (byte) ((compressed.length >> 16) & 0xFF);
        lengthHeader[2] = (byte) ((compressed.length >> 8) & 0xFF);
        lengthHeader[3] = (byte) (compressed.length & 0xFF);
        output.write(lengthHeader);
        output.write(compressed);
        output.flush();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> receiveMessage() throws IOException {
        byte[] lengthHeader = readFully(input, 4);
        int messageLength = ((lengthHeader[0] & 0xFF) << 24)
                | ((lengthHeader[1] & 0xFF) << 16)
                | ((lengthHeader[2] & 0xFF) << 8)
                | (lengthHeader[3] & 0xFF);
        if (messageLength <= 0) {
            throw new IOException("Respuesta del servidor inválida");
        }

        byte[] compressed = readFully(input, messageLength);
        byte[] decompressed = LZ77Compressor.decompress(compressed);
        String json = new String(decompressed, StandardCharsets.UTF_8);
        return JSONParser.fromJson(json, Map.class);
    }

    private static byte[] readFully(InputStream stream, int size) throws IOException {
        byte[] buffer = new byte[size];
        int totalRead = 0;
        while (totalRead < size) {
            int read = stream.read(buffer, totalRead, size - totalRead);
            if (read == -1) {
                throw new IOException("Conexión cerrada por el servidor");
            }
            totalRead += read;
        }
        return buffer;
    }
}
