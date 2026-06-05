// Modal de recuperación de cuenta
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * RecoverAccountModal - Diálogo para recuperar cuenta.
 * Permite ingresar servidor IP, nombre de usuario y nueva contraseña.
 */
public class RecoverAccountModal extends JDialog {

    private JTextField serverIpField;
    private JTextField usernameField;
    private JPasswordField newPasswordField;
    private JButton resetButton;
    private JButton cancelButton;
    private JLabel createAccountLinkLabel;

    // Listeners
    private OnResetListener onResetListener;
    private OnCancelListener onCancelListener;
    private OnCreateAccountListener onCreateAccountListener;

    public interface OnResetListener {
        void onReset(String serverIp, String username, String newPassword);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnCreateAccountListener {
        void onCreateAccount();
    }

    public RecoverAccountModal(Frame owner) {
        super(owner, "Restablecer contraseña", true);
        initUI();
        setMinimumSize(new Dimension(500, 540));
        setSize(500, 540);
        setLocationRelativeTo(owner);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 25, 30));

        // Cabecera con título y botón cerrar
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel central con formulario
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);

        // Panel inferior con enlace
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainPanel);
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1)
        ));
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("¿Olvidaste tu contraseña?");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> closeDialog());
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        // Texto descriptivo
        JLabel description = new JLabel("<html>Ingresa el servidor, tu usuario y la nueva contraseña.<br>Se actualizará directamente en el servidor.</html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(new Color(80, 80, 80));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(description);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Campo Servidor IP
        serverIpField = createTextField("ej. 192.168.1.100");
        formPanel.add(createFieldRow("Servidor IP:", serverIpField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Campo Usuario
        usernameField = createTextField("ej. juan_perez");
        formPanel.add(createFieldRow("Usuario:", usernameField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Texto de ayuda debajo de usuario
        JLabel helpLabel = new JLabel("Se actualizará la contraseña asociada a este usuario");
        helpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helpLabel.setForeground(new Color(120, 120, 120));
        helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        helpPanel.setBackground(Color.WHITE);
        helpPanel.add(helpLabel);
        formPanel.add(helpPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Campo Nueva Contraseña
        newPasswordField = new JPasswordField();
        newPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        newPasswordField.setEchoChar('•');
        formPanel.add(createFieldRow("Nueva Contraseña:", newPasswordField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botón Restablecer (azul)
        resetButton = new JButton("Restablecer");
        resetButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetButton.setBackground(new Color(0, 123, 255));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> {
            if (onResetListener != null) {
                String ip = serverIpField.getText().trim();
                String user = usernameField.getText().trim();
                String pass = new String(newPasswordField.getPassword());
                if (ip.isEmpty() || ip.equals("ej. 192.168.1.100")) ip = "";
                if (user.isEmpty() || user.equals("ej. juan_perez")) user = "";
                onResetListener.onReset(ip, user, pass);
            }
        });
        formPanel.add(resetButton);
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Botón Cancelar (gris)
        cancelButton = new JButton("Cancelar");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(230, 230, 230));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> closeDialog());
        formPanel.add(cancelButton);

        return formPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        createAccountLinkLabel = new JLabel("¿No tienes cuenta? Crea una aquí");
        createAccountLinkLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        createAccountLinkLabel.setForeground(new Color(100, 100, 100));
        createAccountLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createAccountLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onCreateAccountListener != null) onCreateAccountListener.onCreateAccount();
            }
        });
        footer.add(createAccountLinkLabel);
        return footer;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });
        return field;
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(110, 30));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // Borde con sombra
    private static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color shadowColor = new Color(0, 0, 0, 40);
            for (int i = 1; i <= 5; i++) {
                g2d.setColor(shadowColor);
                g2d.drawRoundRect(x + i, y + i, width - 1 - 2*i, height - 1 - 2*i, 15, 15);
            }
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 5, 5, 5);
        }
    }

    public void prefill(String serverIp, String username) {
        if (serverIp != null && !serverIp.isEmpty()) {
            serverIpField.setText(serverIp);
            serverIpField.setForeground(Color.BLACK);
        }
        if (username != null && !username.isEmpty()) {
            usernameField.setText(username);
            usernameField.setForeground(Color.BLACK);
        }
    }

    private void closeDialog() {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        } else {
            dispose();
        }
    }

    // Métodos públicos
    public void setOnResetListener(OnResetListener listener) {
        this.onResetListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    public void setOnCreateAccountListener(OnCreateAccountListener listener) {
        this.onCreateAccountListener = listener;
    }

    public void clearFields() {
        serverIpField.setText("ej. 192.168.1.100");
        serverIpField.setForeground(Color.GRAY);
        usernameField.setText("ej. juan_perez");
        usernameField.setForeground(Color.GRAY);
        newPasswordField.setText("");
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    // Prueba visual
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RecoverAccountModal dialog = new RecoverAccountModal(null);
            dialog.setVisible(true);
        });
    }
}