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
import java.util.List;
import java.util.Map;
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().start());
    }

    private void start() {
        showStartWindow();
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

        // ----- DEBUG -----
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

                String mensaje;
                if (cause instanceof java.net.ConnectException) {
                    mensaje = "No se encontró ningún servidor en la IP " +
                            host + ". Verifica que la aplicación esté ejecutándose.";
                } else if (cause instanceof java.net.UnknownHostException) {
                    mensaje = "La IP introducida (" + host + ") no es válida.";
                } else if (cause instanceof java.net.SocketTimeoutException) {
                    mensaje = "Conexión a " + host +
                            " tardó más de " + (Client.SOCKET_CONNECT_TIMEOUT_MS / 1000) + " s.";
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
            try {
                Map<String, Object> response = client.sendGeneralMessage(message);
                if (!Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                    JOptionPane.showMessageDialog(dashboardWindow,
                            response.getOrDefault("message", "No se pudo enviar el mensaje general"));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dashboardWindow, "Error de conexión: " + ex.getMessage());
            }
        });

        dashboardWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Registrar listener global para mensajes entrantes (actualiza UI y entrega a paneles abiertos)
        client.setMessageListener(message -> handleServerMessage(client, message));
        dashboardWindow.setOnGroupSelectedListener(group -> {
            // Abrir PanelGrupos en un diálogo flotante
            try {
                PanelGrupos miPanelDeGrupos = new PanelGrupos();
                miPanelDeGrupos.setGroupInfo(group.getName(), group.getMemberCount());
                if (group.getMemberNames() != null && !group.getMemberNames().isEmpty()) {
                    miPanelDeGrupos.setGroupMembers(group.getMemberNames());
                }
                miPanelDeGrupos.setPreferredSize(new Dimension(900, 640));

                // Registrar envío de mensajes desde el panel hacia el servidor
                miPanelDeGrupos.setOnSendGroupMessageListener(msg -> {
                    try {
                        client.sendGroupMessage(group.getGroupId(), msg);
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dashboardWindow, "Error enviando mensaje de grupo: " + ex.getMessage()));
                    }
                });

                // Guardar panel abierto para recibir mensajes en tiempo real
                openGroupPanels.put(group.getGroupId(), miPanelDeGrupos);

                JDialog ventanaFlotante = new JDialog(dashboardWindow, group.getName(), false);
                ventanaFlotante.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                ventanaFlotante.getContentPane().add(miPanelDeGrupos);
                ventanaFlotante.pack();
                ventanaFlotante.setSize(920, 660);
                ventanaFlotante.setMinimumSize(new Dimension(760, 560));
                ventanaFlotante.setLocationRelativeTo(dashboardWindow);
                ventanaFlotante.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        openGroupPanels.remove(group.getGroupId());
                    }
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        openGroupPanels.remove(group.getGroupId());
                    }
                });
                ventanaFlotante.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dashboardWindow, "No se pudo abrir la ventana de grupo: " + ex.getMessage());
            }
        });

        dashboardWindow.setOnCreateGroupListener((groupName, invitedUserIds) -> {
            try {
                Map<String, Object> resp = client.createGroup(groupName, invitedUserIds);
                if (Protocol.RES_OK.equals(String.valueOf(resp.get("status")))) {
                    int gid = -1;
                    Object g = resp.get("groupId");
                    if (g instanceof Number) gid = ((Number) g).intValue();
                    List<String> memberNames = new ArrayList<>();
                    memberNames.add(session.getUsername());
                    for (Map<String, Object> friend : session.getFriends()) {
                        int friendId = ((Number) friend.get("id")).intValue();
                        if (invitedUserIds.contains(friendId)) {
                            memberNames.add(String.valueOf(friend.get("username")));
                        }
                    }
                    DashboardWindow.GroupItem created = new DashboardWindow.GroupItem(groupName, invitedUserIds.size() + 1, false, gid, memberNames);
                    dashboardWindow.addGroup(created);
                } else {
                    JOptionPane.showMessageDialog(dashboardWindow, String.valueOf(resp.getOrDefault("message", "No se pudo crear el grupo")));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dashboardWindow, "Error al crear grupo: " + ex.getMessage());
            }
        });
        dashboardWindow.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeClientQuietly(client);
            }
        });

        dashboardWindow.setVisible(true);
    }

    private void openChatWithUser(int userId, String username) {
        FriendRequestModal chatModal = new FriendRequestModal(dashboardWindow, username);
        chatModal.setOnSendFriendMessageListener((recipient, message) -> {
            try {
                Map<String, Object> response = client.sendFriendMessage(userId, message);
                if (Protocol.RES_OK.equals(String.valueOf(response.get("status")))) {
                    updateConversation(userId, username, message, false);
                    chatModal.dispose();
                } else {
                    JOptionPane.showMessageDialog(chatModal,
                            String.valueOf(response.getOrDefault("message", "No se pudo enviar el mensaje")));
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(chatModal, "Error de conexión: " + ex.getMessage());
            }
        });
        chatModal.setOnCancelListener(chatModal::dispose);
        chatModal.setVisible(true);
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
                || Protocol.RES_FRIEND_INVITE_LIST.equals(status)) {
            if (session != null) {
                session.absorb(message);
                refreshDashboardData(session);
            }
        }
    }

    private void handleIncomingMessage(Map<String, Object> message) {
        String type = String.valueOf(message.getOrDefault("type", "friend"));
        String content = String.valueOf(message.get("content"));
        int senderId = ((Number) message.get("senderId")).intValue();
        String senderName = String.valueOf(message.getOrDefault("senderUsername", userNamesById.getOrDefault(senderId, "Usuario " + senderId)));

        if ("general".equals(type)) {
            JOptionPane.showMessageDialog(dashboardWindow,
                    senderName + " (chat general): " + content,
                    "Mensaje general",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if ("group".equals(type)) {
            int groupId = ((Number) message.getOrDefault("groupId", -1)).intValue();
            ui.PanelGrupos panel = openGroupPanels.get(groupId);
            if (panel != null) {
                panel.addMessage(content, senderName, senderId == session.getUserId());
                return;
            } else {
                // Notificar con dialog por defecto y actualizar badge
                JOptionPane.showMessageDialog(dashboardWindow,
                        senderName + " (grupo): " + content,
                        "Mensaje de grupo",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        updateConversation(senderId, senderName, content, true);
        JOptionPane.showMessageDialog(dashboardWindow,
                senderName + ": " + content,
                "Nuevo mensaje",
                JOptionPane.INFORMATION_MESSAGE);
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
        for (Map<String, Object> friend : data.getFriends()) {
            int friendId = ((Number) friend.get("id")).intValue();
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
        dashboardWindow.setInvitations(new ArrayList<>());
        List<DashboardWindow.FriendInvitation> friendInvites = new ArrayList<>();
        for (Map<String, Object> invite : data.getFriendInvites()) {
            int requesterId = invite.get("requesterId") instanceof Number ? ((Number) invite.get("requesterId")).intValue() : -1;
            String requesterName = String.valueOf(invite.getOrDefault("requesterName", "Usuario"));
            boolean incoming = Boolean.TRUE.equals(invite.get("incoming"));
            friendInvites.add(new DashboardWindow.FriendInvitation(requesterName, requesterId, incoming));
        }
        dashboardWindow.setFriendInvitations(friendInvites);
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
