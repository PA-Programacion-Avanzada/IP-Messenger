//Modal de mensajes pendientes
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * PendingMessagesModal - Modal para mostrar mensajes pendientes recibidos
 * mientras el usuario estaba desconectado.
 * Estructura similar a TemporalMessageModal, FriendRequestModal y GroupInviteModal.
 */
public class PendingMessagesModal extends JDialog {

    private JList<PendingMessage> messageList;
    private DefaultListModel<PendingMessage> listModel;
    private JButton markAsReadButton;
    private JButton closeButton;
    private JLabel infoLabel;

    private OnMarkAsReadListener onMarkAsReadListener;
    private OnCloseListener onCloseListener;

    public interface OnMarkAsReadListener {
        void onMarkAsRead(List<PendingMessage> selectedMessages);
    }

    public interface OnCloseListener {
        void onClose();
    }

    // Clase interna para representar un mensaje pendiente
    public static class PendingMessage {
        private String senderName;
        private String content;
        private String timestamp;
        private int messageId;

        public PendingMessage(String senderName, String content, String timestamp, int messageId) {
            this.senderName = senderName;
            this.content = content;
            this.timestamp = timestamp;
            this.messageId = messageId;
        }

        public String getSenderName() { return senderName; }
        public String getContent() { return content; }
        public String getTimestamp() { return timestamp; }
        public int getMessageId() { return messageId; }

        @Override
        public String toString() {
            return senderName + " - " + timestamp + "\n" + content;
        }
    }

    public PendingMessagesModal(Frame owner) {
        super(owner, "Mensajes Pendientes", true);
        initUI();
        setSize(500, 450);
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

        // Cabecera
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Centro (lista de mensajes)
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);

        // Botones inferiores
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Mensajes Pendientes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeButton = new JButton("X");
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

        infoLabel = new JLabel("<html>Estos mensajes fueron recibidos mientras estabas desconectado.<br/>Selecciona los que deseas marcar como leídos.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        center.add(infoLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setCellRenderer(new PendingMessageRenderer());
        messageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        messageList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setBorder(null);
        center.add(scrollPane, BorderLayout.CENTER);

        return center;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        closeButton = new JButton("Cerrar");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setBackground(new Color(240, 240, 240));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            if (onCloseListener != null) onCloseListener.onClose();
            dispose();
        });

        markAsReadButton = new JButton("Marcar seleccionados como leídos");
        markAsReadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        markAsReadButton.setBackground(new Color(0, 123, 255));
        markAsReadButton.setForeground(Color.WHITE);
        markAsReadButton.setFocusPainted(false);
        markAsReadButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        markAsReadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        markAsReadButton.addActionListener(e -> {
            List<PendingMessage> selected = messageList.getSelectedValuesList();
            if (selected.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Selecciona al menos un mensaje pendiente.", 
                    "Sin selección", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (onMarkAsReadListener != null) {
                onMarkAsReadListener.onMarkAsRead(selected);
            }
            // Eliminar los seleccionados de la lista visual
            for (PendingMessage msg : selected) {
                listModel.removeElement(msg);
            }
            if (listModel.isEmpty()) {
                dispose();
            }
        });

        buttonPanel.add(closeButton);
        buttonPanel.add(markAsReadButton);
        return buttonPanel;
    }

    // Renderer personalizado para mensajes pendientes
    private class PendingMessageRenderer extends JPanel implements ListCellRenderer<PendingMessage> {
        private JLabel senderLabel = new JLabel();
        private JLabel contentLabel = new JLabel();
        private JLabel timeLabel = new JLabel();

        PendingMessageRenderer() {
            setLayout(new BorderLayout(8, 4));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setBackground(Color.WHITE);

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);
            topPanel.add(senderLabel, BorderLayout.WEST);
            topPanel.add(timeLabel, BorderLayout.EAST);

            contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            contentLabel.setForeground(new Color(60, 60, 60));

            add(topPanel, BorderLayout.NORTH);
            add(contentLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PendingMessage> list,
                                                      PendingMessage value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            senderLabel.setText(value.getSenderName());
            timeLabel.setText(value.getTimestamp());
            contentLabel.setText(value.getContent());

            if (isSelected) {
                setBackground(new Color(230, 242, 255));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    // Borde con sombra (idéntico a los otros modales)
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

    // Métodos públicos para cargar datos y listeners
    public void setPendingMessages(List<PendingMessage> messages) {
        listModel.clear();
        for (PendingMessage msg : messages) {
            listModel.addElement(msg);
        }
    }

    public void setOnMarkAsReadListener(OnMarkAsReadListener listener) {
        this.onMarkAsReadListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    // Prueba visual
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PendingMessagesModal modal = new PendingMessagesModal(null);
            // Datos de ejemplo
            List<PendingMessage> sample = new ArrayList<>();
            sample.add(new PendingMessage("Juan Pérez", "¿Nos vemos mañana?", "10:45", 1));
            sample.add(new PendingMessage("María García", "Perfecto, gracias", "10:30", 2));
            sample.add(new PendingMessage("Carlos López", "Te enviaré el archivo", "09:15", 3));
            modal.setPendingMessages(sample);
            modal.setVisible(true);
        });
    }
}