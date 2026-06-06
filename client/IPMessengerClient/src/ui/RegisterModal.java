// Modal de registro (después de 3 fallos)
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * RegisterModal - Interfaz de registro de cuenta.
 * Diseñada como un diálogo moderno con campos para IP del servidor,
 * usuario y contraseña. Solo frontend; sin lógica de base de datos.
 */
public class RegisterModal extends JDialog {

    private JTextField serverIpField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton enterButton;
    private JButton cancelButton;
    private JLabel loginLinkLabel;

    // Listeners para acciones externas (a implementar por el controlador)
    private OnRegisterListener onRegisterListener;
    private OnCancelListener onCancelListener;
    private OnSwitchToLoginListener onSwitchToLoginListener;

    public interface OnRegisterListener {
        void onRegister(String serverIp, String username, String password);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnSwitchToLoginListener {
        void onSwitchToLogin();
    }

    public RegisterModal(Frame owner) {
        super(owner, "Registro de Cuenta", true);

        initUI();

        setMinimumSize(new Dimension(500, 540));
        setPreferredSize(new Dimension(500, 540));
        setMaximumSize(new Dimension(500, 540));

        pack();
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
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Dimensiones fijas: 500 × 540
        Dimension dim = new Dimension(500, 540);
        mainPanel.setPreferredSize(dim);
        mainPanel.setMaximumSize(dim);      // impide expandir verticalmente

        // Construir header + form + footer
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(),   BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        // Añadimos al diálogo
        add(mainPanel);

        // Borde externo (para el recuadro blanco)
        getRootPane().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Registro de Cuenta");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("✕");
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

        // Título "Crea tu cuenta"
        JLabel mainTitle = new JLabel("Crea tu cuenta");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(mainTitle);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Campo Servidor IP
        formPanel.add(createFieldRow("Servidor IP:", serverIpField = createTextField("ej. 192.168.1.100")));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Campo Usuario
        formPanel.add(createFieldRow("Usuario:", usernameField = createTextField("Nombre de usuario")));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Campo Contraseña
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        passwordField.setEchoChar('•');
        setPasswordFieldSize(passwordField);

        formPanel.add(createFieldRow("Contraseña:", passwordField));
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        formPanel.add(Box.createVerticalGlue());

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        enterButton = new JButton("Entrar");
        enterButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        enterButton.setBackground(new Color(0, 123, 255));
        enterButton.setForeground(Color.WHITE);
        enterButton.setFocusPainted(false);
        enterButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        enterButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enterButton.addActionListener(e -> {
            if (onRegisterListener != null) {
                String ip = serverIpField.getText().trim();
                String user = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword());
                if (ip.isEmpty() || ip.equals("ej. 192.168.1.100")) ip = "";
                if (user.isEmpty() || user.equals("Nombre de usuario")) user = "";
                onRegisterListener.onRegister(ip, user, pass);
            }
        });

        cancelButton = new JButton("Cancelar");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(Color.WHITE);
        cancelButton.setForeground(new Color(80, 80, 80));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(9, 24, 9, 24)
        ));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> closeDialog());

        buttonPanel.add(enterButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel);

        return formPanel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));

        loginLinkLabel = new JLabel("¿Ya tienes cuenta? Inicia sesión aquí");
        loginLinkLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        loginLinkLabel.setForeground(new Color(100, 100, 100));
        loginLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onSwitchToLoginListener != null) onSwitchToLoginListener.onSwitchToLogin();
            }
        });
        footer.add(loginLinkLabel);
        return footer;
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Border inner = BorderFactory.createLineBorder(new Color(200, 200, 200), 1);
        Border outer = BorderFactory.createCompoundBorder(
                inner,
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        );

        field.setBorder(outer);
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

        // Fijar altura
        int height = 30;
        field.setPreferredSize(new Dimension(0, height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return field;
    }

    private void setPasswordFieldSize(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        int h = 35;

        // Borde interno
        Border inner = BorderFactory.createLineBorder(new Color(200, 200, 200), 1);
        pf.setBorder(BorderFactory.createCompoundBorder(
                inner,
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        pf.setPreferredSize(new Dimension(0, h));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    private JPanel createFieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(100, 30));

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        label.setMaximumSize(new Dimension(100, 30));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void closeDialog() {
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        } else {
            dispose();
        }
    }

    // Métodos públicos para establecer listeners
    public void setOnRegisterListener(OnRegisterListener listener) {
        this.onRegisterListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    public void setOnSwitchToLoginListener(OnSwitchToLoginListener listener) {
        this.onSwitchToLoginListener = listener;
    }

    // Método para limpiar campos (útil después de registro)
    public void clearFields() {
        serverIpField.setText("ej. 192.168.1.100");
        serverIpField.setForeground(Color.GRAY);
        usernameField.setText("Nombre de usuario");
        usernameField.setForeground(Color.GRAY);
        passwordField.setText("");
    }

    // Método para mostrar mensajes de error
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Método main para prueba visual independiente
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegisterModal dialog = new RegisterModal(null);
            dialog.setVisible(true);
        });
    }
}