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
        dashboardWindow.setOnUserActionListener(user -> openChatWithUser(user.getUserId(), user.getName()));
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
        } else if (Protocol.RES_USER_LIST.equals(status)) {
            SessionData updated = new SessionData();
            updated.absorb(message);
            refreshDashboardData(updated);
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
            groups.add(new DashboardWindow.GroupItem(
                    String.valueOf(group.get("name")),
                    0,
                    false,
                    ((Number) group.get("id")).intValue()
            ));
        }
        dashboardWindow.setGroups(groups);
        dashboardWindow.setInvitations(new ArrayList<>());

        if (conversationsByUserId.isEmpty()) {
            dashboardWindow.setFriendConversations(new ArrayList<>());
        }
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
