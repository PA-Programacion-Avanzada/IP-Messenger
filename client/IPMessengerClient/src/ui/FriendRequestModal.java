// Modal de chat de amigos
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FriendRequestModal - Ventana de chat para un amigo especifico.
 * Muestra el historial local de la conversacion y permite enviar nuevos mensajes.
 */
public class FriendRequestModal extends JDialog {

    private final String recipientName;
    private final DefaultListModel<ChatMessage> messageListModel = new DefaultListModel<>();
    private JList<ChatMessage> messageList;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton cancelButton;
    private JLabel offlineInfoLabel;

    private OnSendFriendMessageListener onSendListener;
    private OnCancelListener onCancelListener;

    public static class ChatMessage {
        private final String senderName;
        private final String content;
        private final String time;
        private final boolean mine;
        private final boolean pending;

        public ChatMessage(String senderName, String content, String time, boolean mine, boolean pending) {
            this.senderName = senderName;
            this.content = content;
            this.time = time;
            this.mine = mine;
            this.pending = pending;
        }

        public String getSenderName() { return senderName; }
        public String getContent() { return content; }
        public String getTime() { return time; }
        public boolean isMine() { return mine; }
        public boolean isPending() { return pending; }
    }

    public interface OnSendFriendMessageListener {
        void onSendFriendMessage(String recipientName, String message);
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public FriendRequestModal(Frame owner, String recipientName) {
        super(owner, "Chat de amigo: " + recipientName, false);
        this.recipientName = recipientName;
        initUI();
        setSize(520, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Chat de amigo: " + recipientName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel subtitleLabel = new JLabel("Conversacion de amigos: los mensajes offline se muestran como pendientes.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitleLabel.setForeground(new Color(90, 90, 90));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        header.add(titlePanel, BorderLayout.CENTER);

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
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(Color.WHITE);

        messageList = new JList<>(messageListModel);
        messageList.setCellRenderer(new ChatMessageRenderer());
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setFixedCellHeight(-1);
        messageList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane historyScroll = new JScrollPane(messageList);
        historyScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        historyScroll.getViewport().setBackground(Color.WHITE);
        center.add(historyScroll, BorderLayout.CENTER);

        JPanel composerPanel = new JPanel(new BorderLayout(0, 6));
        composerPanel.setBackground(Color.WHITE);

        messageArea = new JTextArea(4, 30);
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
        composerPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        offlineInfoLabel = new JLabel("Amigos permite enviar aunque el usuario este desconectado; se vera como pendiente.");
        offlineInfoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        offlineInfoLabel.setForeground(new Color(120, 120, 120));
        composerPanel.add(offlineInfoLabel, BorderLayout.SOUTH);

        center.add(composerPanel, BorderLayout.SOUTH);
        return center;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        cancelButton = new JButton("Cerrar");
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

        sendButton = new JButton("Enviar mensaje");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 123, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendCurrentMessage());

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);
        return buttonPanel;
    }

    private void sendCurrentMessage() {
        String msg = messageArea.getText().trim();
        if (msg.isEmpty() || msg.equals("Escribe tu mensaje...")) {
            JOptionPane.showMessageDialog(this, "Por favor escribe un mensaje.", "Mensaje vacio", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onSendListener != null) {
            onSendListener.onSendFriendMessage(recipientName, msg);
        }
        messageArea.setText("");
        messageArea.requestFocusInWindow();
    }

    public void setMessages(List<ChatMessage> messages) {
        messageListModel.clear();
        for (ChatMessage message : messages) {
            messageListModel.addElement(message);
        }
        scrollToLastMessage();
    }

    public void addMessage(ChatMessage message) {
        messageListModel.addElement(message);
        scrollToLastMessage();
    }

    private void scrollToLastMessage() {
        if (!messageListModel.isEmpty()) {
            messageList.ensureIndexIsVisible(messageListModel.size() - 1);
        }
    }

    private class ChatMessageRenderer extends JPanel implements ListCellRenderer<ChatMessage> {
        private final JLabel senderLabel = new JLabel();
        private final JLabel contentLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();
        private final JPanel bubble = new JPanel(new BorderLayout(4, 4));

        ChatMessageRenderer() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            setOpaque(true);

            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);

            bubble.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            bubble.add(senderLabel, BorderLayout.NORTH);
            bubble.add(contentLabel, BorderLayout.CENTER);
            bubble.add(timeLabel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ChatMessage> list,
                                                      ChatMessage value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            removeAll();
            setBackground(Color.WHITE);

            senderLabel.setText(value.isMine() ? "Tu" : value.getSenderName());
            contentLabel.setText("<html><body style='width: 280px'>" + escapeHtml(value.getContent()) + "</body></html>");
            timeLabel.setText(value.getTime() + (value.isPending() ? " - pendiente" : ""));

            bubble.setBackground(value.isMine() ? new Color(220, 242, 255) : new Color(245, 245, 245));
            JPanel wrapper = new JPanel(new FlowLayout(value.isMine() ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(bubble);
            add(wrapper, BorderLayout.CENTER);
            return this;
        }
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
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

    public void setOnSendFriendMessageListener(OnSendFriendMessageListener listener) {
        this.onSendListener = listener;
    }

    public void setOnCancelListener(OnCancelListener listener) {
        this.onCancelListener = listener;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FriendRequestModal modal = new FriendRequestModal(null, "Juan Perez");
            List<ChatMessage> sample = new ArrayList<>();
            sample.add(new ChatMessage("Juan Perez", "Nos vemos manana?", "10:45", false, false));
            sample.add(new ChatMessage("Tu", "Si, despues de clase.", "10:46", true, false));
            modal.setMessages(sample);
            modal.setVisible(true);
        });
    }
}
