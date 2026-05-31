// Punto de entrada del cliente

import ui.*;
import javax.swing.*;

/**
 * MainLauncher - Punto de entrada principal de la aplicación.
 * No es una interfaz gráfica, solo orquesta la creación y transición
 * entre las diferentes ventanas modales y principales.
 * 
 * Para integrar con la lógica de negocio, reemplazar los métodos dummy
 * con llamadas reales a los controladores/servicios.
 */
public class MainLauncher {

    private StartWindow startWindow;
    private DashboardWindow dashboardWindow;
    private ChatModal chatModal;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainLauncher().start());
    }

    private void start() {
        // Mostrar ventana de inicio de sesión
        showStartWindow();
    }

    private void showStartWindow() {
        startWindow = new StartWindow();
        startWindow.setOnConnectListener(this::onLoginAttempt);
        startWindow.setOnRegisterLinkListener(this::onRegisterLinkClicked);
        startWindow.setVisible(true);
    }

    private void onLoginAttempt(String serverIp, String username, String password) {
        // TODO: Llamar al controlador de autenticación real
        // Por ahora, simulación: si campos no vacíos, login exitoso
        if (serverIp.isEmpty() || username.isEmpty() || password.isEmpty()) {
            startWindow.showError("Por favor completa todos los campos");
            return;
        }

        // Simular validación (en un caso real, se haría una llamada asíncrona)
        boolean success = true; // Aquí vendría la comprobación real

        if (success) {
            startWindow.dispose();
            showDashboardWindow(username);
        } else {
            startWindow.showError("Credenciales incorrectas o servidor no disponible");
        }
    }

    private void onRegisterLinkClicked() {
        RegisterModal registerModal = new RegisterModal(startWindow);
        registerModal.setOnRegisterListener((ip, user, pass) -> {
            // TODO: Llamar al servicio de registro
            if (ip.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                registerModal.showError("Todos los campos son obligatorios");
                return;
            }
            // Simular registro exitoso
            JOptionPane.showMessageDialog(registerModal,
                    "Registro exitoso. Ahora puedes iniciar sesión.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            registerModal.dispose();
            // Regresar a la ventana de inicio (ya está abierta)
            startWindow.clearFields();
            startWindow.setVisible(true);
        });
        registerModal.setOnCancelListener(() -> {
            registerModal.dispose();
            startWindow.setVisible(true);
        });
        registerModal.setOnSwitchToLoginListener(() -> {
            registerModal.dispose();
            startWindow.setVisible(true);
        });
        // Ocultar la ventana de inicio mientras se muestra el registro
        startWindow.setVisible(false);
        registerModal.setVisible(true);
    }

    private void onRecoverAccountLinkClicked() {
        RecoverAccountModal recoverModal = new RecoverAccountModal(startWindow);
        recoverModal.setOnResetListener((ip, user, newPass) -> {
            // TODO: Llamar al servicio de recuperación de contraseña
            if (ip.isEmpty() || user.isEmpty() || newPass.isEmpty()) {
                recoverModal.showError("Complete todos los campos");
                return;
            }
            recoverModal.showSuccess("Contraseña restablecida. Ahora puede iniciar sesión.");
            recoverModal.dispose();
            startWindow.setVisible(true);
        });
        recoverModal.setOnCancelListener(() -> {
            recoverModal.dispose();
            startWindow.setVisible(true);
        });
        recoverModal.setOnCreateAccountListener(() -> {
            recoverModal.dispose();
            onRegisterLinkClicked(); // Reutilizar registro
        });
        startWindow.setVisible(false);
        recoverModal.setVisible(true);
    }

    private void showDashboardWindow(String username) {
        dashboardWindow = new DashboardWindow();
        dashboardWindow.setTitle("IP Messenger - " + username);

        // Cargar datos simulados (en producción vendrían de la capa de negocio)
        loadDummyDataIntoDashboard();

        // Configurar listeners del Dashboard
        dashboardWindow.setOnFriendChatSelectedListener(conversation -> {
            // Abrir ventana de chat con el amigo seleccionado
            FriendRequestModal chatModal = new FriendRequestModal(dashboardWindow, conversation.getName());
            chatModal.setOnSendFriendMessageListener((recipient, message) -> {
                // TODO: Enviar mensaje a través del controlador
                System.out.println("Mensaje para " + recipient + ": " + message);
                JOptionPane.showMessageDialog(chatModal, "Mensaje enviado a " + recipient);
                chatModal.dispose();
            });
            chatModal.setOnCancelListener(chatModal::dispose);
            chatModal.setVisible(true);
        });

        dashboardWindow.setOnGroupSelectedListener(group -> {
            GroupInviteModal groupModal = new GroupInviteModal(dashboardWindow, group.getName());
            groupModal.setOnSendGroupMessageListener((grpName, message) -> {
                System.out.println("Mensaje al grupo " + grpName + ": " + message);
                JOptionPane.showMessageDialog(groupModal, "Mensaje enviado al grupo");
                groupModal.dispose();
            });
            groupModal.setOnCancelListener(groupModal::dispose);
            groupModal.setVisible(true);
        });

        dashboardWindow.setOnUserActionListener(user -> {
            // Acción al hacer clic en el botón "+" de un usuario: enviar invitación o mensaje temporal
            int option = JOptionPane.showConfirmDialog(dashboardWindow,
                    "¿Enviar mensaje temporal a " + user.getName() + "?",
                    "Mensaje temporal",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                String msg = JOptionPane.showInputDialog(dashboardWindow, "Mensaje:");
                if (msg != null && !msg.trim().isEmpty()) {
                    System.out.println("Mensaje temporal a " + user.getName() + ": " + msg);
                    JOptionPane.showMessageDialog(dashboardWindow, "Mensaje temporal enviado (no se guarda)");
                }
            }
        });

        dashboardWindow.setOnInvitationActionListener(new DashboardWindow.OnInvitationActionListener() {
            @Override
            public void onAccept(DashboardWindow.GroupInvitation invitation) {
                // TODO: Aceptar invitación a grupo
                JOptionPane.showMessageDialog(dashboardWindow,
                        "Te uniste al grupo " + invitation.getGroupName());
                // Refrescar lista de grupos
            }

            @Override
            public void onReject(DashboardWindow.GroupInvitation invitation) {
                // TODO: Rechazar invitación
                JOptionPane.showMessageDialog(dashboardWindow,
                        "Rechazaste la invitación al grupo " + invitation.getGroupName());
            }
        });

        dashboardWindow.setVisible(true);
    }

    private void loadDummyDataIntoDashboard() {
        // Datos de ejemplo para visualización
        java.util.List<DashboardWindow.FriendConversation> friends = new java.util.ArrayList<>();
        friends.add(new DashboardWindow.FriendConversation("Juan Pérez", "¿Nos vemos mañana?", "10:45", true, true, 1));
        friends.add(new DashboardWindow.FriendConversation("María García", "Perfecto, gracias", "10:30", true, true, 2));
        friends.add(new DashboardWindow.FriendConversation("Carlos López", "Te enviaré el archivo", "09:15", false, false, 3));
        dashboardWindow.setFriendConversations(friends);

        java.util.List<DashboardWindow.GroupItem> groups = new java.util.ArrayList<>();
        groups.add(new DashboardWindow.GroupItem("Estudio Java", 5, true, 101));
        groups.add(new DashboardWindow.GroupItem("Desarrollo Web", 8, false, 102));
        groups.add(new DashboardWindow.GroupItem("Proyecto Final", 6, true, 103));
        dashboardWindow.setGroups(groups);

        java.util.List<DashboardWindow.UserItem> users = new java.util.ArrayList<>();
        users.add(new DashboardWindow.UserItem("Pedro Díaz", true, 201));
        users.add(new DashboardWindow.UserItem("Valentina Ruiz", true, 202));
        users.add(new DashboardWindow.UserItem("Mateo Salazar", false, 203));
        users.add(new DashboardWindow.UserItem("Camila Ortega", true, 204));
        dashboardWindow.setAllUsers(users);

        java.util.List<DashboardWindow.GroupInvitation> invitations = new java.util.ArrayList<>();
        invitations.add(new DashboardWindow.GroupInvitation("Luis Contreras", "Proyecto Final", 103, 301));
        invitations.add(new DashboardWindow.GroupInvitation("María Rodríguez", "Diseño UI/UX", 104, 302));
        dashboardWindow.setInvitations(invitations);
    }
}