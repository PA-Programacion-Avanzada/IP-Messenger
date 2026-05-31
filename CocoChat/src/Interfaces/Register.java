package Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Register extends JFrame {

    private JTextField serverField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton entrarButton;
    private JButton cancelarButton;
    private JLabel loginLink;

    public Register() {
        initComponents();
    }

    private void initComponents() {
        // Configuración de la ventana
        setTitle("Registro de Cuenta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 480);
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

        // Título "Crea tu cuenta"
        JLabel titleLabel = new JLabel("Crea tu cuenta");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(Color.BLACK);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(25));

        // Panel de campos
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);

        // Etiquetas
        JLabel serverLabel = new JLabel("Servidor IP:");
        serverLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverLabel.setForeground(Color.BLACK);

        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.BLACK);

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(Color.BLACK);

        // Campos de entrada con marcadores de posición
        serverField = new JTextField("ej. 192.168.1.100", 15);
        configurarPlaceholder(serverField, "ej. 192.168.1.100");

        userField = new JTextField("Nombre de usuario", 15);
        configurarPlaceholder(userField, "Nombre de usuario");

        passwordField = new JPasswordField(15);
        passwordField.setEchoChar('●'); // Muestra puntos negros
        passwordField.setForeground(Color.BLACK);

        // Colocación en la cuadrícula
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.15;
        fieldsPanel.add(serverLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.85;
        fieldsPanel.add(serverField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.15;
        fieldsPanel.add(userLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.85;
        fieldsPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.15;
        fieldsPanel.add(passLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.85;
        fieldsPanel.add(passwordField, gbc);

        cardPanel.add(fieldsPanel);
        cardPanel.add(Box.createVerticalStrut(25));

        // Botón "Entrar" (azul brillante)
        entrarButton = new JButton("Entrar");
        entrarButton.setBackground(new Color(0, 120, 215));
        entrarButton.setForeground(Color.WHITE);
        entrarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        entrarButton.setFocusPainted(false);
        entrarButton.setBorderPainted(false);
        entrarButton.setOpaque(true);
        entrarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        entrarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        entrarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        entrarButton.addActionListener(e -> {
            // Lógica de registro (placeholder)
            JOptionPane.showMessageDialog(this, "Registrando usuario...");
        });

        cardPanel.add(entrarButton);
        cardPanel.add(Box.createVerticalStrut(10));

        // Botón "Cancelar" (contorno gris claro, fondo blanco)
        cancelarButton = new JButton("Cancelar");
        cancelarButton.setBackground(Color.WHITE);
        cancelarButton.setForeground(Color.BLACK);
        cancelarButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelarButton.setFocusPainted(false);
        cancelarButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cancelarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelarButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelarButton.addActionListener(e -> dispose()); // Cierra la ventana

        cardPanel.add(cancelarButton);
        cardPanel.add(Box.createVerticalStrut(20));

        // Enlace "¿Ya tienes cuenta? Inicia sesión aquí"
        loginLink = new JLabel("¿Ya tienes cuenta? Inicia sesión aquí");
        loginLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginLink.setForeground(new Color(150, 150, 150)); // Gris claro
        loginLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            dispose();  // cierra la ventana de registro
            new IPMessengerLogin().setVisible(true);  // abre la ventana de login
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            loginLink.setForeground(new Color(0, 120, 215));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            loginLink.setForeground(new Color(150, 150, 150));
        }
    });

        cardPanel.add(loginLink);

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

    /**
     * Agrega comportamiento de placeholder: se borra al ganar el foco y se restaura si queda vacío.
     */
    private void configurarPlaceholder(JTextField campo, String placeholder) {
        campo.setForeground(Color.GRAY);
        campo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (campo.getText().equals(placeholder)) {
                    campo.setText("");
                    campo.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (campo.getText().isEmpty()) {
                    campo.setText(placeholder);
                    campo.setForeground(Color.GRAY);
                }
            }
        });
    }
}