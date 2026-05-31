//Modal de mensajes temporales
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * TemporalMessageModal - Modal para enviar mensajes temporales.
 * Los mensajes no se guardan en la base de datos y se borran al desconectarse.
 */
public class TemporalMessageModal extends JDialog {

    private String recipientName;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton cancelButton;

    private OnSendTemporalMessageListener onSendListener;
    private OnCancelListener onCancelListener;

    public interface OnSendTemporalMessageListener {
        void onSendTemporalMessage(String recipientName, String message);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public TemporalMessageModal(Frame owner, String recipientName) {
        super(owner, "Mensaje temporal a: " + recipientName, true);
        this.recipientName = recipientName;
        initUI();
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                mainPanel.getBorder()
        ));

        // Cabecera con título y botón cerrar
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel central con texto explicativo y área de mensaje
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        // Panel inferior con botones
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Mensaje a: " + recipientName + " (Temporal)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("✕");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> dispose());
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        JLabel infoLabel = new JLabel("<html>Este mensaje se borrará cuando el remitente o el destinatario se desconecten.<br/>No se guardará en la base de datos.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        center.add(infoLabel, BorderLayout.NORTH);

        messageArea = new JTextArea(6, 30);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setText("Escribe tu mensaje...");
        messageArea.setForeground(Color.GRAY);
        messageArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (messageArea.getText().equals("Escribe tu mensaje...")) {
                    messageArea.setText("");
                    messageArea.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (messageArea.getText().isEmpty()) {
                    messageArea.setText("Escribe tu mensaje...");
                    messageArea.setForeground(Color.GRAY);
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(null);
        center.add(scrollPane, BorderLayout.CENTER);

        return center;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        cancelButton = new JButton("Cancelar");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(240, 240, 240));
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            if (onCancelListener != null) onCancelListener.onCancel();
            dispose();
        });

        sendButton = new JButton("Enviar a " + recipientName);
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> {
            String msg = messageArea.getText().trim();
            if (msg.isEmpty() || msg.equals("Escribe tu mensaje...")) {
                JOptionPane.showMessageDialog(this, "Por favor escribe un mensaje.", "Mensaje vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (onSendListener != null) {
                onSendListener.onSendTemporalMessage(recipientName, msg);
            }
            dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);
        return buttonPanel;
    }

    private static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color shadowColor = new Color(0, 0, 0, 30);
            for (int i = 1; i <= 4; i++) {
                g2d.setColor(shadowColor);
                g2d.drawRoundRect(x + i, y + i, width - 1 - 2*i, height - 1 - 2*i, 12, 12);
            }
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }

    public void setOnSendTemporalMessageListener(OnSendTemporalMessageListener listener) {
        this.onSendListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemporalMessageModal modal = new TemporalMessageModal(null, "Juan Pérez");
            modal.setVisible(true);
        });
    }
}