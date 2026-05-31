package Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IPMessengerLogin extends JFrame {

    private JTextField serverField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JLabel registerLink;

    public IPMessengerLogin() {
        initComponents();
    }

    private void initComponents() {
        // Configuración de la ventana
        setTitle("IP Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal con fondo blanco
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tarjeta blanca redondeada (simulada)
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(30, 25, 30, 25)
        ));
        cardPanel.setOpaque(true);

        // Título "Iniciar Sesión"
        JLabel titleLabel = new JLabel("Iniciar Sesión");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.BLACK);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(20));

        // Panel de campos
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Etiqueta y campo "Servidor IP"
        JLabel serverLabel = new JLabel("Servidor IP:");
        serverLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverLabel.setForeground(Color.BLACK);

        serverField = new JTextField("ej. 192.168.1.100", 15);
        serverField.setForeground(Color.GRAY);
        serverField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (serverField.getText().equals("ej. 192.168.1.100")) {
                    serverField.setText("");
                    serverField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (serverField.getText().isEmpty()) {
                    serverField.setText("ej. 192.168.1.100");
                    serverField.setForeground(Color.GRAY);
                }
            }
        });

        // Etiqueta y campo "Usuario"
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.BLACK);

        userField = new JTextField("ej. mi_usuario", 15);
        userField.setForeground(Color.GRAY);
        userField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (userField.getText().equals("ej. mi_usuario")) {
                    userField.setText("");
                    userField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (userField.getText().isEmpty()) {
                    userField.setText("ej. mi_usuario");
                    userField.setForeground(Color.GRAY);
                }
            }
        });

        // Etiqueta y campo "Contraseña"
        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(Color.BLACK);

        passwordField = new JPasswordField(15);
        passwordField.setEchoChar('●');
        passwordField.setForeground(Color.BLACK);

        // Ubicación en la cuadrícula
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        fieldsPanel.add(serverLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        fieldsPanel.add(serverField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        fieldsPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        fieldsPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        fieldsPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        fieldsPanel.add(passwordField, gbc);

        cardPanel.add(fieldsPanel);
        cardPanel.add(Box.createVerticalStrut(20));

        // Botón "Conectar"
        connectButton = new JButton("Conectar");
        connectButton.setBackground(new Color(0, 120, 215));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        connectButton.setFocusPainted(false);
        connectButton.setBorderPainted(false);
        connectButton.setOpaque(true);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        connectButton.addActionListener(e -> {
            // Aquí iría la validación real. Por ahora abre el chat principal.
            dispose(); // cierra el login
            new IPMessengerChat(); // abre la interfaz principal
        });

        cardPanel.add(connectButton);
        cardPanel.add(Box.createVerticalStrut(15));

        // Enlace "¿No tienes cuenta? Registrarse"
        registerLink = new JLabel("¿No tienes cuenta? Registrarse");
        registerLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerLink.setForeground(new Color(0, 120, 215));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // cierra el login
                new Register().setVisible(true); // abre la ventana de registro
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                registerLink.setForeground(new Color(0, 80, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerLink.setForeground(new Color(0, 120, 215));
            }
        });

        cardPanel.add(registerLink);

        // Agregar la tarjeta al panel principal
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1;
        mainGbc.weighty = 1;
        mainGbc.fill = GridBagConstraints.NONE;
        mainPanel.add(cardPanel, mainGbc);

        add(mainPanel);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new IPMessengerLogin().setVisible(true));
    }
}