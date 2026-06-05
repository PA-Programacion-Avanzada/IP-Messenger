package com.ipmessenger.client;

// Punto de entrada del cliente



import core.Client;

import core.SessionData;

import network.Protocol;

import ui.*;



import javax.swing.*;

import java.io.IOException;

import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;



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



        Client loginClient = new Client();

        try {

            SessionData loginSession = loginClient.login(serverIp, username, password);

            loginClient.setMessageListener(message -> handleServerMessage(loginClient, message));

            this.client = loginClient;

            this.session = loginSession;

            startWindow.dispose();

            showDashboardWindow(serverIp);

        } catch (IOException ex) {

            startWindow.showError("No se pudo conectar al servidor: " + ex.getMessage());

            closeClientQuietly(loginClient);

        }

    }



    private void onRegisterLinkClicked() {

        RegisterModal registerModal = new RegisterModal(startWindow);

        registerModal.setOnRegisterListener((ip, user, pass) -> {

            if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {

                registerModal.showError("Todos los campos son obligatorios");

                return;

            }



            Client registerClient = new Client();

            try {

                Map<String, Object> response = registerClient.register(ip, user, pass);

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

            } catch (IOException ex) {

                registerModal.showError("No se pudo conectar al servidor: " + ex.getMessage());

            } finally {

                closeClientQuietly(registerClient);

            }

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



            Client recoverClient = new Client();

            try {

                Map<String, Object> response = recoverClient.recoverPassword(ip, user, newPass);

                String status = String.valueOf(response.get("status"));

                if (Protocol.RES_OK.equals(status)) {

                    recoverModal.showSuccess(String.valueOf(response.getOrDefault("message", "Contraseña restablecida.")));

                    recoverModal.dispose();

                    startWindow.clearFields();

                    startWindow.setVisible(true);

                } else {

                    recoverModal.showError(String.valueOf(response.getOrDefault("message", "No se pudo restablecer la contraseña")));

                }

            } catch (IOException ex) {

                recoverModal.showError("No se pudo conectar al servidor: " + ex.getMessage());

            } finally {

                closeClientQuietly(recoverClient);

            }

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


