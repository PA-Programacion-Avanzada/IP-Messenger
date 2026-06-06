package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PendingFriendChatsModal - Lista visual de amigos que tienen mensajes pendientes.
 * Al abrir un amigo, el controlador muestra el chat completo de ese amigo.
 */
public class PendingFriendChatsModal extends JDialog {
    private final DefaultListModel<PendingFriendChat> listModel = new DefaultListModel<>();
    private JList<PendingFriendChat> pendingChatList;
    private JButton openChatButton;
    private JButton closeButton;
    private JLabel infoLabel;

    private OnOpenPendingChatListener onOpenPendingChatListener;

    public static class PendingFriendChat {
        private final int friendId;
        private final String friendName;
        private final int pendingCount;
        private final String lastMessage;
        private final String lastTime;

        public PendingFriendChat(int friendId, String friendName, int pendingCount, String lastMessage, String lastTime) {
            this.friendId = friendId;
            this.friendName = friendName;
            this.pendingCount = pendingCount;
            this.lastMessage = lastMessage;
            this.lastTime = lastTime;
        }

        public int getFriendId() { return friendId; }
        public String getFriendName() { return friendName; }
        public int getPendingCount() { return pendingCount; }
        public String getLastMessage() { return lastMessage; }
        public String getLastTime() { return lastTime; }
    }

    public interface OnOpenPendingChatListener {
        void onOpenPendingChat(PendingFriendChat pendingChat);
    }

    public PendingFriendChatsModal(Frame owner) {
        super(owner, "Chats con mensajes pendientes", true);
        initUI();
        setSize(520, 460);
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

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Mensajes pendientes de amigos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        infoLabel = new JLabel("Selecciona un amigo para abrir su chat con el historial completo.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(90, 90, 90));

        titlePanel.add(titleLabel);
        titlePanel.add(infoLabel);
        header.add(titlePanel, BorderLayout.CENTER);

        JButton closeTopButton = new JButton("X");
        closeTopButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeTopButton.setFocusPainted(false);
        closeTopButton.setContentAreaFilled(false);
        closeTopButton.setBorderPainted(false);
        closeTopButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeTopButton.addActionListener(e -> dispose());
        header.add(closeTopButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);

        pendingChatList = new JList<>(listModel);
        pendingChatList.setCellRenderer(new PendingFriendChatRenderer());
        pendingChatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingChatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedChat();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(pendingChatList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
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
        closeButton.addActionListener(e -> dispose());

        openChatButton = new JButton("Abrir chat");
        openChatButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openChatButton.setBackground(new Color(0, 123, 255));
        openChatButton.setForeground(Color.WHITE);
        openChatButton.setFocusPainted(false);
        openChatButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        openChatButton.addActionListener(e -> openSelectedChat());

        buttonPanel.add(closeButton);
        buttonPanel.add(openChatButton);
        return buttonPanel;
    }

    private void openSelectedChat() {
        PendingFriendChat selected = pendingChatList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un amigo con mensajes pendientes.", "Sin seleccion", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onOpenPendingChatListener != null) {
            onOpenPendingChatListener.onOpenPendingChat(selected);
        }
        dispose();
    }

    public void setPendingFriendChats(List<PendingFriendChat> pendingChats) {
        listModel.clear();
        for (PendingFriendChat chat : pendingChats) {
            listModel.addElement(chat);
        }
        boolean hasPending = !pendingChats.isEmpty();
        openChatButton.setEnabled(hasPending);
        infoLabel.setText(hasPending
                ? "Selecciona un amigo para abrir su chat con el historial completo."
                : "No tienes chats de amigos con mensajes pendientes.");
    }

    public void setOnOpenPendingChatListener(OnOpenPendingChatListener listener) {
        this.onOpenPendingChatListener = listener;
    }

    private class PendingFriendChatRenderer extends JPanel implements ListCellRenderer<PendingFriendChat> {
        private final JLabel avatarLabel = new JLabel();
        private final JLabel nameLabel = new JLabel();
        private final JLabel messageLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();
        private final JLabel countBadge = new JLabel();

        PendingFriendChatRenderer() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            avatarLabel.setOpaque(true);
            avatarLabel.setBackground(new Color(70, 130, 180));
            avatarLabel.setForeground(Color.WHITE);
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setPreferredSize(new Dimension(40, 40));
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            add(avatarLabel, BorderLayout.WEST);

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            messageLabel.setForeground(new Color(80, 80, 80));
            textPanel.add(nameLabel);
            textPanel.add(messageLabel);
            add(textPanel, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new BorderLayout(0, 6));
            rightPanel.setOpaque(false);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);
            timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            countBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            countBadge.setForeground(Color.WHITE);
            countBadge.setBackground(new Color(220, 53, 69));
            countBadge.setOpaque(true);
            countBadge.setHorizontalAlignment(SwingConstants.CENTER);
            countBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            rightPanel.add(timeLabel, BorderLayout.NORTH);
            rightPanel.add(countBadge, BorderLayout.SOUTH);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PendingFriendChat> list,
                                                      PendingFriendChat value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            avatarLabel.setText(value.getFriendName().substring(0, 1).toUpperCase());
            nameLabel.setText(value.getFriendName());
            messageLabel.setText(value.getLastMessage());
            timeLabel.setText(value.getLastTime());
            countBadge.setText(String.valueOf(value.getPendingCount()));
            setBackground(isSelected ? new Color(230, 242, 255) : Color.WHITE);
            return this;
        }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PendingFriendChatsModal modal = new PendingFriendChatsModal(null);
            List<PendingFriendChat> sample = new ArrayList<>();
            sample.add(new PendingFriendChat(1, "Juan Perez", 2, "Nos vemos manana?", "10:45"));
            sample.add(new PendingFriendChat(2, "Maria Garcia", 1, "Te envie el archivo", "11:10"));
            modal.setPendingFriendChats(sample);
            modal.setVisible(true);
        });
    }
}
