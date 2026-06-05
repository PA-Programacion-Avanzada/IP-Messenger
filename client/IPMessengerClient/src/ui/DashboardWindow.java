// Interfaz principal (3 columnas + notificaciones)
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 * DashboardWindow - Interfaz principal de IP Messenger después del inicio de sesión.
 * Muestra invitaciones a grupos, lista de conversaciones con amigos,
 * todos los usuarios registrados y grupos del usuario.
 */
public class DashboardWindow extends JFrame {

    // Invitaciones
    private JPanel invitationsPanel;

    // Columna izquierda: Mensajes con Amigos
    private JList<FriendConversation> friendList;
    private DefaultListModel<FriendConversation> friendListModel;
    private JLabel friendBadgeLabel;

    // Columna central: Todos los Usuarios
    private JPanel allUsersPanel;

    // Columna derecha: Mis Grupos
    private JList<GroupItem> groupList;
    private DefaultListModel<GroupItem> groupListModel;
    private JLabel groupBadgeLabel;

    // Listeners
    private OnFriendChatSelectedListener onFriendChatSelectedListener;
    private OnGroupSelectedListener onGroupSelectedListener;
    private OnUserActionListener onUserActionListener;
    private OnInvitationActionListener onInvitationActionListener;
    private OnSendTemporaryMessageListener onSendTemporaryMessageListener;

    // Clases internas para datos
    public static class FriendConversation {
        private String name;
        private String lastMessage;
        private String time;
        private boolean unread;
        private boolean online;
        private int friendId;

        public FriendConversation(String name, String lastMessage, String time, boolean unread, boolean online, int friendId) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.time = time;
            this.unread = unread;
            this.online = online;
            this.friendId = friendId;
        }

        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }
        public boolean isUnread() { return unread; }
        public boolean isOnline() { return online; }
        public int getFriendId() { return friendId; }
    }

    public static class GroupItem {
        private String name;
        private int memberCount;
        private boolean hasUpdate;
        private int groupId;

        public GroupItem(String name, int memberCount, boolean hasUpdate, int groupId) {
            this.name = name;
            this.memberCount = memberCount;
            this.hasUpdate = hasUpdate;
            this.groupId = groupId;
        }

        public String getName() { return name; }
        public int getMemberCount() { return memberCount; }
        public boolean hasUpdate() { return hasUpdate; }
        public int getGroupId() { return groupId; }
    }

    public static class UserItem {
        private String name;
        private boolean online;
        private int userId;

        public UserItem(String name, boolean online, int userId) {
            this.name = name;
            this.online = online;
            this.userId = userId;
        }

        public String getName() { return name; }
        public boolean isOnline() { return online; }
        public int getUserId() { return userId; }
    }

    public static class GroupInvitation {
        private String inviterName;
        private String groupName;
        private int groupId;
        private int inviterId;

        public GroupInvitation(String inviterName, String groupName, int groupId, int inviterId) {
            this.inviterName = inviterName;
            this.groupName = groupName;
            this.groupId = groupId;
            this.inviterId = inviterId;
        }

        public String getInviterName() { return inviterName; }
        public String getGroupName() { return groupName; }
        public int getGroupId() { return groupId; }
        public int getInviterId() { return inviterId; }
    }

    // Interfaces de eventos
    public interface OnFriendChatSelectedListener {
        void onFriendChatSelected(FriendConversation conversation);
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(GroupItem group);
    }

    public interface OnUserActionListener {
        void onUserAction(UserItem user);
    }

    public interface OnInvitationActionListener {
        void onAccept(GroupInvitation invitation);
        void onReject(GroupInvitation invitation);
    }

    public interface OnSendTemporaryMessageListener {
        void onSendTemporaryMessage(String message, UserItem targetUser);
    }

    public DashboardWindow() {
        initUI();
        setTitle("IP Messenger - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // Barra superior personalizada (opcional, similar a ChatModal)
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // Panel de invitaciones (fila superior)
        mainPanel.add(createInvitationsRow(), BorderLayout.CENTER);

        // Tres columnas
        mainPanel.add(createThreeColumnsPanel(), BorderLayout.SOUTH);

        // Ajuste: usar un contenedor vertical para que invitations y columns se apilen
        // Reorganizamos mejor
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);
        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(createInvitationsRow(), BorderLayout.NORTH);
        centerContainer.add(createThreeColumnsPanel(), BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(240, 248, 255));
        titleBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel("IP Messenger");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleBar.add(titleLabel, BorderLayout.WEST);

        // Botones de ventana estándar (se usarán los del JFrame, pero agregamos uno de cierre personalizado opcional)
        JPanel windowButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        windowButtons.setOpaque(false);
        JButton minButton = createWindowButton("─");
        minButton.addActionListener(e -> setExtendedState(ICONIFIED));
        JButton maxButton = createWindowButton("□");
        maxButton.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH)
                setExtendedState(NORMAL);
            else
                setExtendedState(MAXIMIZED_BOTH);
        });
        JButton closeButton = createWindowButton("✕");
        closeButton.addActionListener(e -> dispose());
        windowButtons.add(minButton);
        windowButtons.add(maxButton);
        windowButtons.add(closeButton);
        titleBar.add(windowButtons, BorderLayout.EAST);

        return titleBar;
    }

    private JButton createWindowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createInvitationsRow() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        container.setBackground(Color.WHITE);

        JLabel invLabel = new JLabel("Invitaciones a grupos");
        invLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        container.add(invLabel, BorderLayout.NORTH);

        JPanel scrollContainer = new JPanel(new BorderLayout());
        invitationsPanel = new JPanel();
        invitationsPanel.setLayout(new BoxLayout(invitationsPanel, BoxLayout.X_AXIS));
        invitationsPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(invitationsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollContainer.add(scrollPane, BorderLayout.CENTER);

        JButton rightArrow = new JButton("→");
        rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        rightArrow.setFocusPainted(false);
        rightArrow.setContentAreaFilled(false);
        rightArrow.addActionListener(e -> {
            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
            hBar.setValue(hBar.getValue() + 200);
        });
        scrollContainer.add(rightArrow, BorderLayout.EAST);

        container.add(scrollContainer, BorderLayout.CENTER);
        return container;
    }

    private JPanel createThreeColumnsPanel() {
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        columnsPanel.setBackground(Color.WHITE);

        columnsPanel.add(createFriendsColumn());
        columnsPanel.add(createAllUsersColumn());
        columnsPanel.add(createGroupsColumn());

        return columnsPanel;
    }

    private JPanel createFriendsColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Mensajes con Amigos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        friendBadgeLabel = new JLabel("0");
        friendBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        friendBadgeLabel.setForeground(Color.WHITE);
        friendBadgeLabel.setBackground(new Color(220, 53, 69));
        friendBadgeLabel.setOpaque(true);
        friendBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        friendBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        header.add(title, BorderLayout.WEST);
        header.add(friendBadgeLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        friendListModel = new DefaultListModel<>();
        friendList = new JList<>(friendListModel);
        friendList.setCellRenderer(new FriendListRenderer());
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(friendList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JButton openChatBtn = new JButton("Abrir Chat Seleccionado");
        openChatBtn.setBackground(new Color(0, 123, 255));
        openChatBtn.setForeground(Color.WHITE);
        openChatBtn.setFocusPainted(false);
        openChatBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        openChatBtn.addActionListener(e -> {
            FriendConversation selected = friendList.getSelectedValue();
            if (selected != null && onFriendChatSelectedListener != null) {
                onFriendChatSelectedListener.onFriendChatSelected(selected);
            } else if (selected == null) {
                JOptionPane.showMessageDialog(this, "Selecciona una conversación");
            }
        });
        panel.add(openChatBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAllUsersColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel title = new JLabel("Todos los Usuarios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        allUsersPanel = new JPanel();
        allUsersPanel.setLayout(new BoxLayout(allUsersPanel, BoxLayout.Y_AXIS));
        allUsersPanel.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(allUsersPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JButton tempMsgBtn = new JButton("Mandar mensaje temporal");
        tempMsgBtn.setBackground(new Color(0, 123, 255));
        tempMsgBtn.setForeground(Color.WHITE);
        tempMsgBtn.setFocusPainted(false);
        tempMsgBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tempMsgBtn.addActionListener(e -> {
            // Se puede abrir un diálogo para escribir mensaje temporal
            String message = JOptionPane.showInputDialog(this, "Mensaje para todos los usuarios conectados:");
            if (message != null && !message.trim().isEmpty() && onSendTemporaryMessageListener != null) {
                onSendTemporaryMessageListener.onSendTemporaryMessage(message.trim(), null);
            }
        });
        panel.add(tempMsgBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGroupsColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Mis Grupos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        groupBadgeLabel = new JLabel("0");
        groupBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        groupBadgeLabel.setForeground(Color.WHITE);
        groupBadgeLabel.setBackground(new Color(220, 53, 69));
        groupBadgeLabel.setOpaque(true);
        groupBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        groupBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        header.add(title, BorderLayout.WEST);
        header.add(groupBadgeLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setCellRenderer(new GroupListRenderer());
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(groupList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JButton viewGroupBtn = new JButton("Ver Grupo Seleccionado");
        viewGroupBtn.setBackground(new Color(0, 123, 255));
        viewGroupBtn.setForeground(Color.WHITE);
        viewGroupBtn.setFocusPainted(false);
        viewGroupBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        viewGroupBtn.addActionListener(e -> {
            GroupItem selected = groupList.getSelectedValue();
            if (selected != null && onGroupSelectedListener != null) {
                onGroupSelectedListener.onGroupSelected(selected);
            } else if (selected == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un grupo");
            }
        });
        panel.add(viewGroupBtn, BorderLayout.SOUTH);

        return panel;
    }

    // Renderer para amigos
    private class FriendListRenderer extends JPanel implements ListCellRenderer<FriendConversation> {
        private JLabel avatarLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel msgLabel = new JLabel();
        private JLabel timeLabel = new JLabel();
        private JLabel statusIndicator = new JLabel();

        FriendListRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            avatarLabel.setOpaque(true);
            avatarLabel.setPreferredSize(new Dimension(40, 40));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            textPanel.add(nameLabel);
            textPanel.add(msgLabel);
            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(timeLabel, BorderLayout.NORTH);
            statusIndicator.setPreferredSize(new Dimension(10, 10));
            statusIndicator.setOpaque(true);
            statusIndicator.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            rightPanel.add(statusIndicator, BorderLayout.SOUTH);
            add(rightPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FriendConversation> list,
                                                      FriendConversation value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            String initials = value.getName().substring(0, 1).toUpperCase();
            avatarLabel.setText(initials);
            avatarLabel.setBackground(new Color(70, 130, 180));
            avatarLabel.setForeground(Color.WHITE);
            nameLabel.setText(value.getName());
            msgLabel.setText(value.getLastMessage());
            timeLabel.setText(value.getTime());
            statusIndicator.setBackground(value.isOnline() ? new Color(40, 167, 69) : Color.GRAY);
            statusIndicator.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
            if (value.isUnread()) {
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
                msgLabel.setFont(msgLabel.getFont().deriveFont(Font.BOLD));
            } else {
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN));
                msgLabel.setFont(msgLabel.getFont().deriveFont(Font.PLAIN));
            }
            setBackground(isSelected ? new Color(230, 242, 255) : Color.WHITE);
            return this;
        }
    }

    // Renderer para grupos
    private class GroupListRenderer extends JPanel implements ListCellRenderer<GroupItem> {
        private JLabel avatarLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel memberLabel = new JLabel();
        private JLabel updateIndicator = new JLabel();

        GroupListRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            avatarLabel.setOpaque(true);
            avatarLabel.setPreferredSize(new Dimension(40, 40));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            avatarLabel.setBackground(new Color(102, 205, 170));
            avatarLabel.setForeground(Color.WHITE);
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            textPanel.add(nameLabel);
            textPanel.add(memberLabel);
            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            updateIndicator.setFont(new Font("Segoe UI", Font.BOLD, 12));
            updateIndicator.setForeground(new Color(220, 53, 69));
            add(updateIndicator, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GroupItem> list,
                                                      GroupItem value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            avatarLabel.setText(value.getName().substring(0, 1).toUpperCase());
            nameLabel.setText(value.getName());
            memberLabel.setText("👥 " + value.getMemberCount() + " miembros");
            updateIndicator.setText(value.hasUpdate() ? "●" : "");
            setBackground(isSelected ? new Color(230, 242, 255) : Color.WHITE);
            return this;
        }
    }

    // Métodos públicos para actualizar datos
    public void setFriendConversations(List<FriendConversation> conversations) {
        friendListModel.clear();
        for (FriendConversation fc : conversations) {
            friendListModel.addElement(fc);
        }
        int unreadCount = (int) conversations.stream().filter(FriendConversation::isUnread).count();
        updateFriendBadge(unreadCount);
    }

    public void setGroups(List<GroupItem> groups) {
        groupListModel.clear();
        for (GroupItem g : groups) {
            groupListModel.addElement(g);
        }
        int updateCount = (int) groups.stream().filter(GroupItem::hasUpdate).count();
        updateGroupBadge(updateCount);
    }

    public void setAllUsers(List<UserItem> users) {
        allUsersPanel.removeAll();
        for (UserItem user : users) {
            allUsersPanel.add(createUserRow(user));
            allUsersPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        allUsersPanel.revalidate();
        allUsersPanel.repaint();
    }

    private JPanel createUserRow(UserItem user) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        row.setBackground(Color.WHITE);

        // Avatar
        JLabel avatar = new JLabel(user.getName().substring(0, 1));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(70, 130, 180));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setMaximumSize(new Dimension(36, 36));
        avatar.setPreferredSize(new Dimension(36, 36));
        row.add(avatar);

        row.add(Box.createRigidArea(new Dimension(10, 0)));

        // Texto (nombre + estado)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel statusLabel = new JLabel(user.isOnline() ? "● En línea" : "○ Desconectado");
        statusLabel.setForeground(user.isOnline() ? new Color(40, 167, 69) : Color.GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        textPanel.add(nameLabel);
        textPanel.add(statusLabel);
        row.add(textPanel);
        row.add(Box.createHorizontalGlue());

        // Botón “+” – ahora con ancho fijo mayor
        JButton actionBtn = new JButton("+");
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        actionBtn.setFocusPainted(false);
        actionBtn.setBackground(new Color(255, 140, 0));
        actionBtn.setForeground(Color.WHITE);

        // Tamaño fijo de 40×40
        Dimension btnSize = new Dimension(40, 40);
        actionBtn.setMinimumSize(btnSize);
        actionBtn.setPreferredSize(btnSize);
        actionBtn.setMaximumSize(btnSize);
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionBtn.addActionListener(e -> {
            if (onUserActionListener != null) onUserActionListener.onUserAction(user);
        });
        row.add(actionBtn);

        // Evita que la fila se estire verticalmente
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        return row;
    }

    public void setInvitations(List<GroupInvitation> invitations) {
        invitationsPanel.removeAll();
        for (GroupInvitation inv : invitations) {
            invitationsPanel.add(createInvitationCard(inv));
            invitationsPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        }
        invitationsPanel.revalidate();
        invitationsPanel.repaint();
    }

    private JPanel createInvitationCard(GroupInvitation inv) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(260, 80));

        JLabel avatar = new JLabel(inv.getInviterName().substring(0, 1));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(255, 160, 122));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(40, 40));
        card.add(avatar, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(new JLabel(inv.getInviterName()));
        textPanel.add(new JLabel("Te ha invitado al grupo " + inv.getGroupName()));
        card.add(textPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        JButton acceptBtn = new JButton("Aceptar");
        acceptBtn.setBackground(new Color(40, 167, 69));
        acceptBtn.setForeground(Color.WHITE);
        acceptBtn.setFocusPainted(false);
        acceptBtn.addActionListener(e -> {
            if (onInvitationActionListener != null) onInvitationActionListener.onAccept(inv);
        });
        JButton rejectBtn = new JButton("Rechazar");
        rejectBtn.setBackground(new Color(220, 53, 69));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFocusPainted(false);
        rejectBtn.addActionListener(e -> {
            if (onInvitationActionListener != null) onInvitationActionListener.onReject(inv);
        });
        btnPanel.add(acceptBtn);
        btnPanel.add(rejectBtn);
        card.add(btnPanel, BorderLayout.EAST);

        return card;
    }

    private void updateFriendBadge(int count) {
        friendBadgeLabel.setText(String.valueOf(count));
        friendBadgeLabel.setVisible(count > 0);
    }

    private void updateGroupBadge(int count) {
        groupBadgeLabel.setText(String.valueOf(count));
        groupBadgeLabel.setVisible(count > 0);
    }

    // Setters de listeners
    public void setOnFriendChatSelectedListener(OnFriendChatSelectedListener listener) { this.onFriendChatSelectedListener = listener; }
    public void setOnGroupSelectedListener(OnGroupSelectedListener listener) { this.onGroupSelectedListener = listener; }
    public void setOnUserActionListener(OnUserActionListener listener) { this.onUserActionListener = listener; }
    public void setOnInvitationActionListener(OnInvitationActionListener listener) { this.onInvitationActionListener = listener; }
    public void setOnSendTemporaryMessageListener(OnSendTemporaryMessageListener listener) { this.onSendTemporaryMessageListener = listener; }

    // Método main para prueba visual
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardWindow window = new DashboardWindow();

            // Datos de ejemplo
            List<FriendConversation> friends = new ArrayList<>();
            friends.add(new FriendConversation("Juan Pérez", "¿Nos vemos mañana?", "10:45", true, true, 1));
            friends.add(new FriendConversation("María García", "Perfecto, gracias", "10:30", true, true, 2));
            friends.add(new FriendConversation("Carlos López", "Te enviaré el archivo", "09:15", false, false, 3));
            window.setFriendConversations(friends);

            List<GroupItem> groups = new ArrayList<>();
            groups.add(new GroupItem("Estudio Java", 5, true, 101));
            groups.add(new GroupItem("Desarrollo Web", 8, false, 102));
            groups.add(new GroupItem("Proyecto Final", 6, true, 103));
            window.setGroups(groups);

            List<UserItem> users = new ArrayList<>();
            users.add(new UserItem("Pedro Díaz", true, 201));
            users.add(new UserItem("Valentina Ruiz", true, 202));
            users.add(new UserItem("Mateo Salazar", false, 203));
            users.add(new UserItem("Camila Ortega", true, 204));
            window.setAllUsers(users);

            List<GroupInvitation> invitations = new ArrayList<>();
            invitations.add(new GroupInvitation("Luis Contreras", "Proyecto Final", 103, 301));
            invitations.add(new GroupInvitation("María Rodríguez", "Diseño UI/UX", 104, 302));
            window.setInvitations(invitations);

            window.setVisible(true);
        });
    }
}