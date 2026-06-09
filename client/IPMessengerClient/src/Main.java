package com.ipmessenger.client;

import core.Client;
import core.NetworkTask;
import core.SessionData;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import java.awt.Dimension;
import network.Protocol;
import ui.*;

public class Main {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private StartWindow startWindow;
    private DashboardWindow dashboardWindow;
    private Client client;
    private SessionData session;
    private final Map<Integer, String> userNamesById = new HashMap<>();
    private final Map<Integer, DashboardWindow.FriendConversation> conversationsByUserId = new HashMap<>();
    private final Map<Integer, ui.PanelGrupos> openGroupPanels = new HashMap<>();
    private final Map<Integer, FriendRequestModal> openFriendModals = new HashMap<>();
    private final Map<Integer, javax.swing.JDialog> openGroupDialogs = new HashMap<>();
    // IDs de los usuarios que SON AMIGOS del usuario logueado
    private final Set<Integer> friendIds = new HashSet<>();
    // Lista en memoria de los mensajes **temporales** (no‑persistentes)
    private final List<PendingMessagesModal.PendingMessage> temporaryMessages = new ArrayList<>();


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        showStartWindow();
    }

    public interface OnCreateGroupListener {
        void onCreateGroup(String name, List<Integer> memberIds);
    }
    private OnCreateGroupListener onCreateGroupListener;

    public void setOnCreateGroupListener(OnCreateGroupListener listener) {
        this.onCreateGroupListener = listener;
    }

    private void showStartWindow() {
        startWindow = new StartWindow();
        startWindow.setOnConnectListener(this::onLoginAttempt);
        startWindow.setOnRegisterLinkListener(this::onRegisterLinkClicked);
        startWindow.setOnForgotPasswordListener(this::onForgotPasswordClicked);
        startWindow.setVisible(true);
    }

    private void onLoginAttempt(String serverIp, String username, String password) {
        if (serverIp.isEmpty() || username.isEmpty() || password.isEmpty()) {
            startWindow.showError("Por favor completa todos los campos");
            return;
        }

        System.out.println("[LOGIN] intentando en " + serverIp);

        new NetworkTask<SessionData>() {
            private Client loginClient;
            private String host = serverIp;

            @Override
            protected SessionData doTask() throws Exception {
                System.out.printf("[LOGIN] conectando a %s:%d …%n", host, Client.DEFAULT_PORT);
                loginClient = new Client();
                return loginClient.login(host, username, password);
            }

            @Override
            protected void onSuccess(SessionData sessionData) {
                System.out.println("[LOGIN] login exitoso → ventana Dashboard");

                client     = loginClient;
                session    = sessionData;

                startWindow.dispose();
                showDashboardWindow(host);
            }

            @Override
            protected void propagateError(Throwable ex) {
                Throwable cause = (ex instanceof ExecutionException && ex.getCause() != null)
                        ? ex.getCause() : ex;

                if (cause instanceof IOException ioEx && ioEx.getMessage() != null
                        && ioEx.getMessage().startsWith("NEED_RECOVER::")) {
                    String msg = ioEx.getMessage().substring("NEED_RECOVER::".length());
                    startWindow.showError(msg);
                    onForgotPasswordClicked();
                    return;
                }

                String mensaje;
                if (cause instanceof java.net.ConnectException) {
                    mensaje = "No se encontró ningún servidor en la IP " +
                            host + ". Verifica que la aplicación esté ejecutándose.";
                } else if (cause instanceof java.net.UnknownHostException) {
                    mensaje = "La IP introducida (" + host + ") no es válida.";
                } else if (cause instanceof java.net.SocketTimeoutException) {
                    mensaje = "Conexión a " + host +
                            " tardó más de " + (Client.SOCKET_CONNECT_TIMEOUT_MS / 1000) + " s.";
                } else if (cause instanceof IOException) {
                    mensaje = "No se pudo conectar al servidor: " + cause.getMessage();
                } else {
                    mensaje = "Error inesperado: " + cause.getMessage();
                }

                System.out.println("[LOGIN ERROR] " + mensaje);
                startWindow.showError(mensaje);
            }
        }.execute();
    }

    private void onRegisterLinkClicked() {
        RegisterModal registerModal = new RegisterModal(startWindow);
        registerModal.setOnRegisterListener((ip, user, pass) -> {
            if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                registerModal.showError("Todos los campos son obligatorios");
                return;
            }

            new NetworkTask<Map<String,Object>>() {
                @Override
                protected Map<String,Object> doTask() throws Exception {
                    Client regClient = new Client();
                    return regClient.register(ip, user, pass);
                }

                @Override
                protected void onSuccess(Map<String,Object> response) {
                    String status = String.valueOf(response.get("status"));
                    if (Protocol.RES_OK.equals(status)) {
                        String message = String.valueOf(response.getOrDefault("message", "Registro exitoso."));
                        JOptionPane.showMessageDialog(registerModal, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        registerModal.dispose();
                        startWindow.clearFields();
                        startWindow.setVisible(true);
                    } else {
                        registerModal.showError(String.valueOf(response.getOrDefault("message", "No se pudo registrar el usuario")));
                    }
                }

                @Override
                protected void propagateError(Throwable ex) {
                    Throwable cause = (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null)
                            ? ex.getCause() : ex;

                    String mensaje;
                    if (cause instanceof java.net.ConnectException) {
                        mensaje = "No se encontró servidor en la IP " + ip + ". Verifica que la aplicación esté ejecutándose.";
                    } else if (cause instanceof java.net.UnknownHostException) {
                        mensaje = "La IP introducida (" + ip + ") no es válida.";
                    } else {
                        mensaje = "No se pudo conectar al servidor: " + cause.getMessage();
                    }
                    registerModal.showError(mensaje);
                }
            }.execute();
        });

        registerModal.setOnCancelListener(() -> {
            registerModal.dispose();
            startWindow.setVisible(true);
        });

        registerModal.setOnSwitchToLoginListener(() -> {
            registerModal.dispose();
            startWindow.setVisible(true);
        });

        startWindow.setVisible(false);
        registerModal.setVisible(true);
    }

    private void onForgotPasswordClicked() {
        RecoverAccountModal recoverModal = new RecoverAccountModal(startWindow);
        recoverModal.prefill(startWindow.getEnteredServerIp(), startWindow.getEnteredUsername());
        recoverModal.setOnResetListener((ip, user, newPass) -> {
            if (ip.isEmpty() || user.isEmpty() || newPass.isEmpty()) {
                recoverModal.showError("Complete todos los campos");
                return;
            }

            new NetworkTask<Map<String,Object>>() {
                @Override
                protected Map<String,Object> doTask() throws Exception {
                    Client recClient = new Client();
                    return recClient.recoverPassword(ip, user, newPass);
                }

                @Override
                protected void onSuccess(Map<String,Object> response) {
                    String status = String.valueOf(response.get("status"));
                    if (Protocol.RES_OK.equals(status)) {
                        recoverModal.showSuccess(String.valueOf(response.getOrDefault("message", "Contraseña restablecida.")));
                        recoverModal.dispose();
                        startWindow.clearFields();
                        startWindow.setVisible(true);
                    } else {
                        recoverModal.showError(String.valueOf(response.getOrDefault("message", "No se pudo restablecer la contraseña")));
                    }
                }

                @Override
                protected void propagateError(Throwable ex) {
                    Throwable cause = (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null)
                            ? ex.getCause() : ex;

                    String mensaje;
                    if (cause instanceof java.net.ConnectException) {
                        mensaje = "No se encontró servidor en la IP " + ip + ". Verifica que la aplicación esté ejecutándose.";
                    } else if (cause instanceof java.net.UnknownHostException) {
                        mensaje = "La IP introducida (" + ip + ") no es válida.";
                    } else {
                        mensaje = "No se pudo conectar al servidor: " + cause.getMessage();
                    }
                    recoverModal.showError(mensaje);
                }
            }.execute();
        });

        recoverModal.setOnCancelListener(() -> {
            recoverModal.dispose();
            startWindow.setVisible(true);
        });

        recoverModal.setOnCreateAccountListener(() -> {
            recoverModal.dispose();
            onRegisterLinkClicked();
        });

        startWindow.setVisible(false);
        recoverModal.setVisible(true);
    }

    private void refreshPendingBadge() {
        new NetworkTask<List<PendingMessagesModal.PendingMessage>>() {
            @Override
            protected List<PendingMessagesModal.PendingMessage> doTask() throws Exception {
                Map<String, Object> resp = client.getPendingMessages();
                List<?> raw = (List<?>) resp.getOrDefault("messages", List.of());

                List<PendingMessagesModal.PendingMessage> friendPending = new ArrayList<>();

                for (Object o : raw) {
                    if (!(o instanceof Map<?, ?> rawMap)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) rawMap;

                    int senderId = ((Number) m.getOrDefault("senderId", -1)).intValue();

                    PendingMessagesModal.PendingMessage pm = new PendingMessagesModal.PendingMessage(
                            String.valueOf(m.getOrDefault("senderUsername", "Desconocido")),
                            String.valueOf(m.getOrDefault("content", "")),
                            String.valueOf(m.getOrDefault("timestamp", "")),
                            ((Number) m.getOrDefault("id", -1)).intValue());

                    if (friendIds.contains(senderId)) {
                        // mensaje pendiente de amigo
                        friendPending.add(pm);
                    } else {
                        // mensaje temporal (no‑amigo)
                        temporaryMessages.add(pm);
                    }
                }

                // actualizar el badge de temporales
                setTempMessageCount(temporaryMessages.size());

                return friendPending;   // solo los de amigos
            }

            @Override
            protected void onSuccess(List<PendingMessagesModal.PendingMessage> pending) {
                // refresca el badge de la columna “Correo”
                int count = pending.size();
                dashboardWindow.setPendingFriendChatCount(count);
            }

            @Override
            protected void propagateError(Throwable ex) {
                // Si falla simplemente ignoramos el badge; la UI ya mostrará 0
                dashboardWindow.setPendingFriendChatCount(0);
            }
        }.execute();
    }

    private void showDashboardWindow(String serverIp) {
        dashboardWindow = new DashboardWindow();
        dashboardWindow.setTitle("IP Messenger - " + session.getUsername() + " (" + serverIp + ")");
        refreshDashboardData(session);

        dashboardWindow.setOnFriendChatSelectedListener(conversation -> openChatWithUser(conversation.getFriendId(), conversation.getName()));
        dashboardWindow.setOnUserActionListener(user -> sendFriendRequestToUser(user));

        dashboardWindow.setOnFriendInvitationActionListener(new DashboardWindow.OnFriendInvitationActionListener() {
            @Override
            public void onAccept(DashboardWindow.FriendInvitation invitation) {
                try {
                    Map<String, Object> response = client.acceptFriendRequest(invitation.getRequesterId());
                    if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                "Solicitud de amistad aceptada.", "Amigos", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                String.valueOf(response.getOrDefault("message", "No se pudo aceptar la solicitud")));
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
                }
            }

            @Override
            public void onReject(DashboardWindow.FriendInvitation invitation) {
                try {
                    Map<String, Object> response = client.rejectFriendRequest(invitation.getRequesterId());
                    if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                "Solicitud de amistad rechazada.", "Amigos", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                String.valueOf(response.getOrDefault("message", "No se pudo rechazar la solicitud")));
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
                }
            }
        });

        dashboardWindow.setOnSendTemporaryMessageListener((message, targetUser) -> {
            // No se permite enviar temporales a usuarios offline
            if (!targetUser.isOnline()) {
                JOptionPane.showMessageDialog(dashboardWindow,
                        "El usuario está desconectado y no puede recibir mensajes temporales.",
                        "No disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // Envío al servidor
                Map<String, Object> response = client.sendTemporaryMessage(targetUser.getUserId(), message);
                String status = String.valueOf(response.get("status"));

                // Si el servidor responde OK o PENDING, guardamos el mensaje en la lista de temporales
                if (Protocol.RES_OK.equals(status) || "PENDING".equalsIgnoreCase(status)) {
                    PendingMessagesModal.PendingMessage pm = new PendingMessagesModal.PendingMessage(
                            session.getUsername(),          // remitente = yo
                            message,
                            LocalTime.now().format(TIME_FORMAT),
                            -1);                            // aún no tiene ID en la BD

                    temporaryMessages.add(pm);
                    // Opcional: actualizar el badge del remitente
                    // setTempMessageCount(temporaryMessages.size());
                }

                // Manejo de la respuesta del servidor
                if (Protocol.RES_OK.equals(status)) {
                    // Mensaje entregado inmediatamente → nada que actualizar en la UI
                } else if ("PENDING".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(dashboardWindow,
                            "El destinatario está offline; el mensaje se guardó como pendiente temporal.",
                            "Mensaje pendiente", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dashboardWindow,
                            response.getOrDefault("message", "No se pudo enviar el mensaje"),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dashboardWindow,
                        "Error de conexión: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    
        dashboardWindow.setOnViewTempMessagesListener(() -> {
            TemporalMessagesModal modal = new TemporalMessagesModal(dashboardWindow);
            List<PendingMessagesModal.PendingMessage> toShow = temporaryMessages.stream()
                    .filter(pm -> !pm.getSenderName().equals(session.getUsername())) // <-- nombre correcto del getter
                    .collect(java.util.stream.Collectors.toList());
            modal.setPendingMessages(toShow);
            modal.setVisible(true);
        });

        dashboardWindow.setOnInvitationActionListener(new DashboardWindow.OnInvitationActionListener() {
            @Override
            public void onAccept(DashboardWindow.GroupInvitation invitation) {
                new NetworkTask<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> doTask() throws Exception {
                        return client.acceptGroupInvite(invitation.getGroupId());
                    }

                    @Override
                    protected void onSuccess(Map<String, Object> response) {
                        if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                            JOptionPane.showMessageDialog(dashboardWindow,
                                    "Te uniste al grupo \"" + invitation.getGroupName() + "\". "
                                            + "Abre el grupo para ver todo el historial.",
                                    "Grupo", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dashboardWindow,
                                    String.valueOf(response.getOrDefault("message", "No se pudo aceptar la invitación")));
                        }
                    }

                    @Override
                    protected void propagateError(Throwable ex) {
                        JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
                    }
                }.execute();
            }

            @Override
            public void onReject(DashboardWindow.GroupInvitation invitation) {
                new NetworkTask<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> doTask() throws Exception {
                        return client.rejectGroupInvite(invitation.getGroupId());
                    }

                    @Override
                    protected void onSuccess(Map<String, Object> response) {
                        if (!Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                            JOptionPane.showMessageDialog(dashboardWindow,
                                    String.valueOf(response.getOrDefault("message", "No se pudo rechazar la invitación")));
                        }
                    }

                    @Override
                    protected void propagateError(Throwable ex) {
                        JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
                    }
                }.execute();
            }
        });

        dashboardWindow.setOnInviteToGroupListener((group, invitedIds) -> {
            new NetworkTask<Map<String, Object>>() {
                @Override
                protected Map<String, Object> doTask() throws Exception {
                    return client.inviteToGroup(group.getGroupId(), invitedIds);
                }

                @Override
                protected void onSuccess(Map<String, Object> response) {
                    if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                "Invitación enviada. El usuario verá la invitación y al aceptar podrá ver el historial completo.",
                                "Grupo", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dashboardWindow,
                                String.valueOf(response.getOrDefault("message", "No se pudo invitar al grupo")),
                                "Grupo", JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                protected void propagateError(Throwable ex) {
                    JOptionPane.showMessageDialog(dashboardWindow, "Error de comunicación: " + ex.getMessage());
                }
            }.execute();
        });

        dashboardWindow.setOnPendingMessagesSelectedListener(() -> {
            new NetworkTask<List<PendingMessagesModal.PendingMessage>>() {
                @Override
                protected List<PendingMessagesModal.PendingMessage> doTask() throws Exception {
                    Map<String, Object> resp = client.getPendingMessages();
                    List<?> raw = (List<?>) resp.get("messages");
                    List<PendingMessagesModal.PendingMessage> out = new ArrayList<>();
                    for (Object o : raw) {
                        if (o instanceof Map<?, ?> rawMap) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> m = (Map<String, Object>) rawMap;

                            int id = ((Number) m.getOrDefault("id", -1)).intValue();
                            String sender = String.valueOf(m.getOrDefault("senderUsername", "Desconocido"));
                            String content = String.valueOf(m.getOrDefault("content", ""));
                            String ts = String.valueOf(m.getOrDefault("timestamp", ""));

                            out.add(new PendingMessagesModal.PendingMessage(sender, content, ts, id));
                        }
                    }
                    return out;
                }

                @Override
                protected void onSuccess(List<PendingMessagesModal.PendingMessage> pending) {
                    PendingMessagesModal modal = new PendingMessagesModal(dashboardWindow);
                    modal.setPendingMessages(pending);

                    modal.setOnMarkAsReadListener(selected -> {
                        for (PendingMessagesModal.PendingMessage pm : selected) {
                            try {
                                client.markMessageRead(pm.getMessageId());
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(modal,
                                        "Error marcando como leído: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        refreshPendingBadge();
                    });

                    modal.setOnCloseListener(() -> refreshPendingBadge());
                    modal.setVisible(true);
                }

                @Override
                protected void propagateError(Throwable ex) {
                    JOptionPane.showMessageDialog(dashboardWindow,
                            "Error cargando mensajes pendientes: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }.execute();
        });

        dashboardWindow.setOnCreateGroupListener((groupName, invitedIds) -> {
            // Ejecutamos la operación de red en un hilo de fondo (NetworkTask = SwingWorker)
            new NetworkTask<Map<String,Object>>() {

                @Override
                protected Map<String,Object> doTask() throws Exception {
                    // Crear el grupo en el servidor
                    return client.createGroup(groupName, invitedIds);
                }

                @Override
                protected void onSuccess(Map<String,Object> response) {
                    // Si el servidor respondió OK, pedimos la lista actualizada de grupos
                    if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                        // Do nothing: server will send us the updated group list.
                    } else {
                        // Caso error: mostramos el mensaje que vino del servidor
                        JOptionPane.showMessageDialog(dashboardWindow,
                                String.valueOf(response.getOrDefault("message",
                                        "No se pudo crear el grupo")), "Crear grupo",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                protected void propagateError(Throwable ex) {
                    // Cualquier excepción (timeout, desconexión, etc.)
                    JOptionPane.showMessageDialog(dashboardWindow,
                            "Error de comunicación: " + ex.getMessage(),
                            "Crear grupo", JOptionPane.ERROR_MESSAGE);
                }
            }.execute();
        });

        dashboardWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Registrar listener global para mensajes entrantes
        client.setMessageListener(message -> handleServerMessage(client, message));

        dashboardWindow.setOnGroupSelectedListener(group -> {
            try {
                javax.swing.JDialog existingDialog = openGroupDialogs.get(group.getGroupId());
                if (existingDialog != null && existingDialog.isDisplayable()) {
                    existingDialog.toFront();
                    existingDialog.requestFocus();
                    return;
                }
                openGroupPanels.remove(group.getGroupId());
                openGroupDialogs.remove(group.getGroupId());

                ui.PanelGrupos miPanelDeGrupos = new ui.PanelGrupos();
                miPanelDeGrupos.setGroupInfo(group.getName(), group.getMemberCount());
                if (group.getMemberNames() != null && !group.getMemberNames().isEmpty()) {
                    miPanelDeGrupos.setGroupMembers(group.getMemberNames());
                }
                miPanelDeGrupos.setPreferredSize(new java.awt.Dimension(900, 640));

                miPanelDeGrupos.setOnSendGroupMessageListener(new ui.PanelGrupos.OnSendGroupMessageListener() {
                    @Override
                    public void onSendGroupMessage(String msg) {
                        if (msg == null || msg.trim().isEmpty()) {
                            return;
                        }
                        try {
                            client.sendGroupMessage(group.getGroupId(), msg);
                        } catch (java.io.IOException ex) {
                            javax.swing.SwingUtilities.invokeLater(() ->
                                javax.swing.JOptionPane.showMessageDialog(dashboardWindow,
                                    "Error enviando mensaje de grupo: " + ex.getMessage()));
                        }
                    }
                });

                openGroupPanels.put(group.getGroupId(), miPanelDeGrupos);

                new NetworkTask<java.util.Map<String, Object>>() {
                    @Override
                    protected java.util.Map<String, Object> doTask() throws Exception {
                        return client.getGroupHistory(group.getGroupId(), 500);
                    }

                    @Override
                    protected void onSuccess(java.util.Map<String, Object> response) {
                        if (response == null || !response.containsKey("messages")) {
                            return;
                        }
                        java.util.List<?> mensajes = (java.util.List<?>) response.get("messages");
                        for (Object obj : mensajes) {
                            if (obj instanceof java.util.Map<?, ?> msg) {
                                String txt = String.valueOf(msg.get("content"));
                                String rem = String.valueOf(msg.get("senderUsername"));
                                String hora = msg.containsKey("timestamp") ? String.valueOf(msg.get("timestamp")) : "";
                                boolean esMio = isOwnGroupHistoryMessage(msg);
                                miPanelDeGrupos.addHistoryMessage(txt, rem, esMio, hora);
                            }
                        }
                    }

                    @Override
                    protected void propagateError(Throwable ex) {
                        System.out.println("[HISTORIAL ERROR] Error cargando mensajes antiguos: " + ex.getMessage());
                    }
                }.execute();

                javax.swing.JDialog ventanaFlotante = new javax.swing.JDialog(dashboardWindow, group.getName(), false);
                ventanaFlotante.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                ventanaFlotante.getContentPane().add(miPanelDeGrupos);
                ventanaFlotante.pack();
                ventanaFlotante.setSize(920, 660);
                ventanaFlotante.setMinimumSize(new java.awt.Dimension(760, 560));
                ventanaFlotante.setLocationRelativeTo(dashboardWindow);

                openGroupDialogs.put(group.getGroupId(), ventanaFlotante);
                ventanaFlotante.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        openGroupPanels.remove(group.getGroupId());
                        openGroupDialogs.remove(group.getGroupId());
                    }
                });

                ventanaFlotante.setVisible(true);

            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(dashboardWindow, "No se pudo abrir la ventana de grupo: " + ex.getMessage());
            }
        }); // <-- cierra setOnGroupSelectedListener

        dashboardWindow.setVisible(true);
    } // <-- cierra showDashboardWindow

    private void openChatWithUser(int userId, String username) {
        // Retrieve history (run in background)
        new NetworkTask<List<FriendRequestModal.ChatMessage>>() {
            @Override
            protected List<FriendRequestModal.ChatMessage> doTask() throws Exception {
                Map<String, Object> resp = client.getFriendHistory(userId, 200); // 200 msgs max
                List<FriendRequestModal.ChatMessage> msgs = new ArrayList<>();
                List<?> raw = (List<?>) resp.get("messages");
                for (Object o : raw) {
                    if (o instanceof Map<?, ?> m) {
                        String sender  = String.valueOf(m.get("senderUsername"));
                        String content = String.valueOf(m.get("content"));

                        Object tsObj = m.get("timestamp");               // get the raw object
                        String ts = (tsObj != null ? tsObj.toString() // convert safely to String
                                                : "");               // fallback if null

                        boolean mine = sender.equals(session.getUsername());
                        msgs.add(new FriendRequestModal.ChatMessage(
                                sender, content, ts, mine, false));
                    }
                }
                return msgs;
            }

            @Override
            protected void onSuccess(List<FriendRequestModal.ChatMessage> history) {
                // Build the modal and inject the history
                FriendRequestModal existingModal = openFriendModals.get(userId);
                if (existingModal != null && existingModal.isDisplayable()) {
                    existingModal.toFront();
                    existingModal.requestFocus();
                    return;
                }

                FriendRequestModal chatModal = new FriendRequestModal(dashboardWindow, username);
                openFriendModals.put(userId, chatModal);
                chatModal.setMessages(history);
                chatModal.setOnSendFriendMessageListener((recipient, message) -> {
                    try {
                        Map<String, Object> response = client.sendFriendMessage(userId, message);
                        if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                            // Update the “dashboard” list (unread badge, last‑message preview)
                            updateConversation(userId, username, message, false);

                            // Insert the new bubble into the open modal
                            // We reuse the same TIME_FORMAT that the class already defines.
                            String now = java.time.LocalTime.now().format(TIME_FORMAT);
                            FriendRequestModal.ChatMessage myMsg =
                                    new FriendRequestModal.ChatMessage(
                                            session.getUsername(),   // remitente = yo
                                            message,                 // contenido
                                            now,                     // hora
                                            true,                    // isMine = true
                                            false);                  // pending = false
                            chatModal.addMessage(myMsg);

                            // (optional) scroll to the newest message – `addMessage` already does it.
                        } else {
                            JOptionPane.showMessageDialog(chatModal,
                                    String.valueOf(response.getOrDefault("message", "No se pudo enviar el mensaje")));
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(chatModal, "Error de conexión: " + ex.getMessage());
                    }
                });
                chatModal.setOnCancelListener(chatModal::dispose);
                chatModal.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        openFriendModals.remove(userId);
                    }
                });
                chatModal.setVisible(true);
            }

            @Override
            protected void propagateError(Throwable ex) {
                JOptionPane.showMessageDialog(dashboardWindow,
                        "Error obteniendo historial: " + ex.getMessage(),
                        "Historial", JOptionPane.ERROR_MESSAGE);
            }
        }.execute();
    }

    private void sendFriendRequestToUser(DashboardWindow.UserItem user) {
        try {
            Map<String, Object> response = client.sendFriendRequest(user.getUserId());
            if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                JOptionPane.showMessageDialog(dashboardWindow,
                        "Solicitud de amistad enviada a " + user.getName() + ".",
                        "Invitación enviada",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dashboardWindow,
                        String.valueOf(response.getOrDefault("message", "No se pudo enviar la solicitud de amistad")));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
        }
    }

    private void handleServerMessage(Client activeClient, Map<String, Object> message) {
        if (activeClient != client) {
            return;
        }
        SwingUtilities.invokeLater(() -> processServerMessage(message));
    }

    private void processServerMessage(Map<String, Object> message) {
        String status = String.valueOf(message.get("status"));
        if (Protocol.RES_NEW_MESSAGE.equals(status)) {
            handleIncomingMessage(message);
        } else if (Protocol.RES_FRIEND_LIST.equals(status)
                || Protocol.RES_GROUP_LIST.equals(status)
                || Protocol.RES_USER_LIST.equals(status)
                || Protocol.RES_FRIEND_INVITE_LIST.equals(status)
                || Protocol.RES_GROUP_INVITE_LIST.equals(status)) {
            if (session != null) {
                session.absorb(message);
                refreshDashboardData(session);
            }
        }
    }

    private void handleIncomingMessage(Map<String, Object> message) {
        String status = String.valueOf(message.get("status"));
        String type = String.valueOf(message.getOrDefault("type", "friend"));
        String content = String.valueOf(message.get("content"));

        int senderId = -1;
        if (message.containsKey("senderId") && message.get("senderId") != null) {
            try {
                senderId = ((Number) message.get("senderId")).intValue();
            } catch (Exception ignored) {}
        }

        String senderName = String.valueOf(message.getOrDefault("senderUsername",
                userNamesById.getOrDefault(senderId, "Usuario " + senderId)));

        //  si el remitente NO ES AMIGO NI YO → temporal 
        if (senderId != -1
                && !friendIds.contains(senderId)               // no es amigo
                && senderId != session.getUserId()) {          // no es yo mismo
            // Tratamos como mensaje **temporal**
            PendingMessagesModal.PendingMessage pm = new PendingMessagesModal.PendingMessage(
                    senderName,
                    content,
                    String.valueOf(message.getOrDefault("timestamp",
                            LocalTime.now().format(TIME_FORMAT))),
                    ((Number) message.getOrDefault("id", -1)).intValue());

            temporaryMessages.add(pm);
            setTempMessageCount(temporaryMessages.size());

            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + " (temporal): " + content,
                    "Nuevo mensaje temporal",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // -------------------  Mensaje temporal -------------------
        if ("temporary".equals(type) || Protocol.RES_NEW_MESSAGE.equals(status) && "temporary".equals(type)) {
            // Guardamos en la lista de temporales del cliente
            PendingMessagesModal.PendingMessage pm = new PendingMessagesModal.PendingMessage(
                    senderName,
                    content,
                    String.valueOf(message.getOrDefault("timestamp",
                            LocalTime.now().format(TIME_FORMAT))),
                    ((Number) message.getOrDefault("id", -1)).intValue());
            temporaryMessages.add(pm);
            setTempMessageCount(temporaryMessages.size());

            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + " (temporal): " + content,
                    "Nuevo mensaje temporal",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("RES_GROUP_HISTORY".equals(status) || (message.containsKey("messages") && message.containsKey("groupId"))) {
            int groupId = ((Number) message.get("groupId")).intValue();
            java.util.List<?> mensajes = (java.util.List<?>) message.get("messages");

            ui.PanelGrupos panel = openGroupPanels.get(groupId);
            if (panel != null && mensajes != null) {
                System.out.println("Mensajes recibidos: " + mensajes.size());
                SwingUtilities.invokeLater(() -> {
                    for (Object obj : mensajes) {
                        if (obj instanceof Map<?, ?> msg) {
                            String txt = String.valueOf(msg.get("content"));
                            String rem = String.valueOf(msg.get("senderUsername"));
                            String hora = msg.containsKey("timestamp") ? String.valueOf(msg.get("timestamp")) : "";

                            boolean esMio = isOwnGroupHistoryMessage(msg);
                            panel.addHistoryMessage(txt, rem, esMio, hora);
                        }
                    }
                });
            }
            return;
        }

        if ("group".equals(type)) {
            int groupId = ((Number) message.getOrDefault("groupId", -1)).intValue();
            boolean esMio = (senderId == session.getUserId());

            ui.PanelGrupos panel = openGroupPanels.get(groupId);
            if (panel != null) {
                panel.addMessage(content, senderName, esMio);
                return;
            } else {
                JOptionPane.showMessageDialog(dashboardWindow,
                        senderName + " (grupo): " + content,
                        "Nuevo mensaje en el grupo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        if ("general".equals(type)) {
            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + " (chat general): " + content,
                    "Mensaje general",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("temporary".equals(type)) {
            // Guardamos el mensaje en la lista interna de temporales
            temporaryMessages.add(new PendingMessagesModal.PendingMessage(
                    senderName,
                    content,
                    String.valueOf(message.getOrDefault("timestamp", LocalTime.now().format(TIME_FORMAT))),
                    -1));
            setTempMessageCount(temporaryMessages.size());

            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + " (temporal): " + content,
                    "Nuevo mensaje temporal",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (senderId != session.getUserId() && !senderName.equals(session.getUsername())) {
            String time = String.valueOf(message.getOrDefault("timestamp", LocalTime.now().format(TIME_FORMAT)));
            FriendRequestModal openModal = openFriendModals.get(senderId);
            updateConversation(senderId, senderName, content, true);

            if (openModal != null && openModal.isDisplayable() && openModal.isVisible()) {
                FriendRequestModal.ChatMessage incoming = new FriendRequestModal.ChatMessage(
                        senderName,
                        content,
                        time,
                        false,
                        false
                );
                openModal.addMessage(incoming);
                return;
            }

            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + ": " + content,
                    "Nuevo mensaje",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateConversation(int userId, String username, String lastMessage, boolean unread) {
        userNamesById.put(userId, username);
        DashboardWindow.FriendConversation conversation = new DashboardWindow.FriendConversation(
                username,
                lastMessage,
                LocalTime.now().format(TIME_FORMAT),
                unread,
                true,
                userId
        );
        conversationsByUserId.put(userId, conversation);
        dashboardWindow.setFriendConversations(new ArrayList<>(conversationsByUserId.values()));
    }

    private void refreshDashboardData(SessionData data) {
        for (Map<String, Object> user : data.getUsers()) {
            userNamesById.put(((Number) user.get("id")).intValue(), String.valueOf(user.get("username")));
        }

        List<DashboardWindow.FriendConversation> friendConversations = new ArrayList<>();
        this.friendIds.clear();
        for (Map<String, Object> friend : data.getFriends()) {
            int friendId = ((Number) friend.get("id")).intValue();
            this.friendIds.add(friendId);
            String friendName = String.valueOf(friend.get("username"));
            boolean online = Boolean.TRUE.equals(friend.get("online"));
            DashboardWindow.FriendConversation existing = conversationsByUserId.get(friendId);
            if (existing != null) {
                friendConversations.add(existing);
            } else {
                friendConversations.add(new DashboardWindow.FriendConversation(
                        friendName,
                        "Toca para chatear",
                        LocalTime.now().format(TIME_FORMAT),
                        false,
                        online,
                        friendId
                ));
            }
        }

        for (Map.Entry<Integer, DashboardWindow.FriendConversation> entry : conversationsByUserId.entrySet()) {
            if (!friendIds.contains(entry.getKey())) {
                friendConversations.add(entry.getValue());
            }
        }
        dashboardWindow.setFriendConversations(friendConversations);

        List<DashboardWindow.UserItem> users = new ArrayList<>();
        for (Map<String, Object> user : data.getUsers()) {
            users.add(new DashboardWindow.UserItem(
                    String.valueOf(user.get("username")),
                    Boolean.TRUE.equals(user.get("online")),
                    ((Number) user.get("id")).intValue()
            ));
        }
        dashboardWindow.setAllUsers(users);

        List<DashboardWindow.GroupItem> groups = new ArrayList<>();
        for (Map<String, Object> group : data.getGroups()) {
            List<String> memberNames = new ArrayList<>();
            Object rawMembers = group.get("members");
            if (rawMembers instanceof List<?> membersList) {
                for (Object item : membersList) {
                    memberNames.add(String.valueOf(item));
                }
            }
            groups.add(new DashboardWindow.GroupItem(
                    String.valueOf(group.get("name")),
                    group.containsKey("memberCount") ? ((Number) group.get("memberCount")).intValue() : 0,
                    false,
                    ((Number) group.get("id")).intValue(),
                    memberNames
            ));
        }
        dashboardWindow.setGroups(groups);

        // ----------  ACTUALIZAR CONJUNTO DE IDs DE AMIGOS ----------
        friendIds.clear();
        for (Map<String, Object> f : data.getFriends()) {
            int fid = ((Number) f.get("id")).intValue();
            friendIds.add(fid);
        }

        List<DashboardWindow.GroupInvitation> groupInvites = new ArrayList<>();
        for (Map<String, Object> invite : data.getGroupInvites()) {
            int groupId = invite.get("groupId") instanceof Number ? ((Number) invite.get("groupId")).intValue() : -1;
            int inviterId = invite.get("inviterId") instanceof Number ? ((Number) invite.get("inviterId")).intValue() : -1;
            String groupName = String.valueOf(invite.getOrDefault("groupName", "Grupo"));
            String inviterName = String.valueOf(invite.getOrDefault("inviterName", "Usuario"));
            groupInvites.add(new DashboardWindow.GroupInvitation(inviterName, groupName, groupId, inviterId));
        }
        dashboardWindow.setInvitations(groupInvites);

        List<DashboardWindow.FriendInvitation> friendInvites = new ArrayList<>();
        for (Map<String, Object> invite : data.getFriendInvites()) {
            int requesterId = invite.get("requesterId") instanceof Number ? ((Number) invite.get("requesterId")).intValue() : -1;
            String requesterName = String.valueOf(invite.getOrDefault("requesterName", "Usuario"));
            boolean incoming = Boolean.TRUE.equals(invite.get("incoming"));
            friendInvites.add(new DashboardWindow.FriendInvitation(requesterName, requesterId, incoming));
        }
        dashboardWindow.setFriendInvitations(friendInvites);
    }

    private void setTempMessageCount(int count) {
        if (dashboardWindow != null) {
            dashboardWindow.setTempMessageCount(count);
        }
    }

    private boolean isOwnGroupHistoryMessage(Map<?, ?> msg) {
        if (msg.containsKey("senderId") && msg.get("senderId") instanceof Number senderId) {
            return senderId.intValue() == session.getUserId();
        }
        String rem = String.valueOf(msg.get("senderUsername"));
        return rem.equals(session.getUsername());
    }

    private void closeClientQuietly(Client activeClient) {
        if (activeClient == null) {
            return;
        }

        try {
            activeClient.close();
        } catch (IOException ignored) {
        }
    }
}