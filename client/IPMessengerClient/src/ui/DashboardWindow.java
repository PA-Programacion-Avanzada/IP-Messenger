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
    // Campos de UI
    private JLabel tempInboxBadgeLabel;
    private OnViewTempMessagesListener onViewTempMessagesListener;

    // Invitaciones
    private JPanel friendInvitationsPanel;
    private JPanel groupInvitationsPanel;

    // Columna izquierda: Mensajes con Amigos
    private JList<FriendConversation> friendList;
    private DefaultListModel<FriendConversation> friendListModel;
    private JLabel friendBadgeLabel;
    private JLabel pendingInboxBadgeLabel;

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
    private OnPendingMessagesSelectedListener onPendingMessagesSelectedListener;
    private OnFriendInvitationActionListener onFriendInvitationActionListener;
    private OnCreateGroupListener onCreateGroupListener;
    private OnInviteToGroupListener onInviteToGroupListener;

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
        private java.util.List<String> memberNames;

        public GroupItem(String name, int memberCount, boolean hasUpdate, int groupId, java.util.List<String> memberNames) {
            this.name = name;
            this.memberCount = memberCount;
            this.hasUpdate = hasUpdate;
            this.groupId = groupId;
            this.memberNames = memberNames;
        }

        public GroupItem(String name, int memberCount, boolean hasUpdate, int groupId) {
            this(name, memberCount, hasUpdate, groupId, new java.util.ArrayList<>());
        }

        public String getName() { return name; }
        public int getMemberCount() { return memberCount; }
        public boolean hasUpdate() { return hasUpdate; }
        public int getGroupId() { return groupId; }
        public java.util.List<String> getMemberNames() { return memberNames; }
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

    public static class FriendInvitation {
        private String requesterName;
        private int requesterId;
        private boolean incoming;

        public FriendInvitation(String requesterName, int requesterId, boolean incoming) {
            this.requesterName = requesterName;
            this.requesterId = requesterId;
            this.incoming = incoming;
        }

        public String getRequesterName() { return requesterName; }
        public int getRequesterId() { return requesterId; }
        public boolean isIncoming() { return incoming; }
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

    public interface OnCreateGroupListener {
        void onCreateGroup(String groupName, java.util.List<Integer> invitedUserIds);
    }

    public interface OnInviteToGroupListener {
        void onInviteToGroup(GroupItem group, java.util.List<Integer> invitedUserIds);
    }

    public interface OnPendingMessagesSelectedListener {
        void onPendingMessagesSelected();
    }

    public interface OnFriendInvitationActionListener {
        void onAccept(FriendInvitation invitation);
        void onReject(FriendInvitation invitation);
    }

    public DashboardWindow() {
        initUI();
        setTitle("IP Messenger - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
    }

    private void seedFriendUiExamples() {
        setFriendInvitations(createSampleFriendInvitations());
        setInvitations(createSampleGroupInvitations());
        setPendingFriendChatCount(2);
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
        JButton minButton = createWindowButton("_");
        minButton.addActionListener(e -> setExtendedState(ICONIFIED));
        JButton maxButton = createWindowButton("MAX");
        maxButton.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH)
                setExtendedState(NORMAL);
            else
                setExtendedState(MAXIMIZED_BOTH);
        });
        JButton closeButton = createWindowButton("X");
        closeButton.addActionListener(e -> dispose());
        windowButtons.add(minButton);
        windowButtons.add(maxButton);
        windowButtons.add(closeButton);
        titleBar.add(windowButtons, BorderLayout.EAST);

        return titleBar;
    }

    private JButton createWindowButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(new Color(40, 40, 40));
        btn.setPreferredSize(new Dimension(52, 28));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createInvitationsRow() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        container.setBackground(Color.WHITE);

        JLabel invLabel = new JLabel("Invitaciones");
        invLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        container.add(invLabel, BorderLayout.NORTH);

        JPanel invitationsContent = new JPanel(new GridLayout(1, 2, 12, 0));
        invitationsContent.setBackground(Color.WHITE);

        friendInvitationsPanel = new JPanel();
        friendInvitationsPanel.setLayout(new BoxLayout(friendInvitationsPanel, BoxLayout.Y_AXIS));
        friendInvitationsPanel.setBackground(Color.WHITE);

        groupInvitationsPanel = new JPanel();
        groupInvitationsPanel.setLayout(new BoxLayout(groupInvitationsPanel, BoxLayout.Y_AXIS));
        groupInvitationsPanel.setBackground(Color.WHITE);

        invitationsContent.add(createInvitationSection("Solicitudes de amistad", friendInvitationsPanel));
        invitationsContent.add(createInvitationSection("Invitaciones a grupos", groupInvitationsPanel));
        container.add(invitationsContent, BorderLayout.CENTER);
        return container;
    }

    private JPanel createInvitationSection(String title, JPanel contentPanel) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        section.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setPreferredSize(new Dimension(0, 120));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        section.add(scrollPane, BorderLayout.CENTER);
        return section;
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
        JLabel title = new JLabel("Amigos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        friendBadgeLabel = new JLabel("0");
        friendBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        friendBadgeLabel.setForeground(Color.WHITE);
        friendBadgeLabel.setBackground(new Color(220, 53, 69));
        friendBadgeLabel.setOpaque(true);
        friendBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        friendBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        friendBadgeLabel.setVisible(false);

        JButton pendingInboxButton = new JButton("Correo");
        pendingInboxButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pendingInboxButton.setPreferredSize(new Dimension(82, 28));
        pendingInboxButton.setFocusPainted(false);
        pendingInboxButton.setBackground(Color.WHITE);
        pendingInboxButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        pendingInboxButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pendingInboxButton.setToolTipText("Ver chats con mensajes pendientes");
        pendingInboxButton.addActionListener(e -> {
            if (onPendingMessagesSelectedListener != null) {
                onPendingMessagesSelectedListener.onPendingMessagesSelected();
            } else {
                showSamplePendingFriendChats();
            }
        });

        // badge de mensajes temporales (gris cuando 0)
        tempInboxBadgeLabel = new JLabel("0");
        tempInboxBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tempInboxBadgeLabel.setForeground(Color.WHITE);
        tempInboxBadgeLabel.setBackground(new Color(220, 53, 69));
        tempInboxBadgeLabel.setOpaque(true);
        tempInboxBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tempInboxBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        tempInboxBadgeLabel.setVisible(false);

        // Badge de mensajes “pendientes”
        pendingInboxBadgeLabel = new JLabel("0");
        pendingInboxBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pendingInboxBadgeLabel.setForeground(Color.WHITE);
        pendingInboxBadgeLabel.setBackground(new Color(220, 53, 69));
        pendingInboxBadgeLabel.setOpaque(true);
        pendingInboxBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pendingInboxBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        pendingInboxBadgeLabel.setVisible(false);

        JPanel inboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        inboxPanel.setOpaque(false);
        inboxPanel.add(pendingInboxButton);
        inboxPanel.add(pendingInboxBadgeLabel);
        inboxPanel.add(tempInboxBadgeLabel);

        header.add(title, BorderLayout.WEST);
        header.add(inboxPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        friendListModel = new DefaultListModel<>();
        friendList = new JList<>(friendListModel);
        friendList.setCellRenderer(new FriendListRenderer());
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(friendList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        JButton openChatBtn = new JButton("Abrir chat de amigo");
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
        JButton tempMsgBtn = new JButton("Ver mis mensajes temporales");
        tempMsgBtn.setBackground(new Color(0, 123, 255));
        tempMsgBtn.setForeground(Color.WHITE);
        tempMsgBtn.setFocusPainted(false);
        tempMsgBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        tempMsgBtn.addActionListener(e -> {
            if (onViewTempMessagesListener != null) {
                onViewTempMessagesListener.onViewTempMessages();
            }
        });
        panel.add(tempMsgBtn, BorderLayout.SOUTH);
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

        JButton createGroupBtn = new JButton("Crear Grupo");
        createGroupBtn.setBackground(new Color(40, 167, 69));
        createGroupBtn.setForeground(Color.WHITE);
        createGroupBtn.setFocusPainted(false);
        createGroupBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        createGroupBtn.addActionListener(e -> {
            // Diálogo para crear grupo
            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Crear Grupo", true);
            dlg.setLayout(new BorderLayout(8,8));
            JPanel body = new JPanel(new BorderLayout(8,8));
            JTextField nameField = new JTextField();
            nameField.setBorder(BorderFactory.createTitledBorder("Nombre del grupo"));
            body.add(nameField, BorderLayout.NORTH);

            DefaultListModel<FriendConversation> pickModel = new DefaultListModel<>();
            for (int i = 0; i < friendListModel.size(); i++) pickModel.addElement(friendListModel.get(i));
            JList<FriendConversation> pickList = new JList<>(pickModel);
            pickList.setCellRenderer(new FriendListRenderer());
            pickList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane pickScroll = new JScrollPane(pickList);
            pickScroll.setBorder(BorderFactory.createTitledBorder("Selecciona amigos"));
            body.add(pickScroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancelar");
            JButton create = new JButton("Crear");
            create.setBackground(new Color(0, 123, 255));
            create.setForeground(Color.WHITE);
            create.setFocusPainted(false);
            cancel.addActionListener(ev -> dlg.dispose());
            create.addActionListener(ev -> {
                String gname = nameField.getText().trim();
                java.util.List<Integer> ids = new java.util.ArrayList<>();
                for (FriendConversation fc : pickList.getSelectedValuesList()) ids.add(fc.getFriendId());
                if (gname.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Especifica un nombre para el grupo"); return; }
                if (ids.size() < 2) { JOptionPane.showMessageDialog(dlg, "Selecciona al menos 2 amigos (3 personas en total con el creador)"); return; }
                if (onCreateGroupListener != null) onCreateGroupListener.onCreateGroup(gname, ids);
                dlg.dispose();
            });
            footer.add(cancel); footer.add(create);
            dlg.add(body, BorderLayout.CENTER);
            dlg.add(footer, BorderLayout.SOUTH);
            dlg.setSize(420,480);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });

        JButton addMemberBtn = new JButton("Agregar Participante");
        addMemberBtn.setBackground(new Color(255, 140, 0));
        addMemberBtn.setForeground(Color.WHITE);
        addMemberBtn.setFocusPainted(false);
        addMemberBtn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        addMemberBtn.addActionListener(e -> {
            GroupItem selected = groupList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un grupo primero");
                return;
            }

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Agregar al grupo " + selected.getName(), true);
            dlg.setLayout(new BorderLayout(8, 8));
            JPanel body = new JPanel(new BorderLayout(8, 8));

            DefaultListModel<FriendConversation> pickModel = new DefaultListModel<>();
            java.util.Set<String> currentMembers = new java.util.HashSet<>(selected.getMemberNames());
            for (int i = 0; i < friendListModel.size(); i++) {
                FriendConversation fc = friendListModel.get(i);
                if (!currentMembers.contains(fc.getName())) {
                    pickModel.addElement(fc);
                }
            }

            if (pickModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay amigos disponibles para agregar a este grupo.");
                return;
            }

            JList<FriendConversation> pickList = new JList<>(pickModel);
            pickList.setCellRenderer(new FriendListRenderer());
            pickList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            JScrollPane pickScroll = new JScrollPane(pickList);
            pickScroll.setBorder(BorderFactory.createTitledBorder("Selecciona amigos"));
            body.add(pickScroll, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton cancel = new JButton("Cancelar");
            JButton invite = new JButton("Invitar");
            invite.setBackground(new Color(0, 123, 255));
            invite.setForeground(Color.WHITE);
            invite.setFocusPainted(false);
            cancel.addActionListener(ev -> dlg.dispose());
            invite.addActionListener(ev -> {
                java.util.List<Integer> ids = new java.util.ArrayList<>();
                for (FriendConversation fc : pickList.getSelectedValuesList()) {
                    ids.add(fc.getFriendId());
                }
                if (ids.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Selecciona al menos un amigo");
                    return;
                }
                if (onInviteToGroupListener != null) {
                    onInviteToGroupListener.onInviteToGroup(selected, ids);
                }
                dlg.dispose();
            });
            footer.add(cancel);
            footer.add(invite);
            dlg.add(body, BorderLayout.CENTER);
            dlg.add(footer, BorderLayout.SOUTH);
            dlg.setSize(420, 420);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });

        JPanel southPanel = new JPanel(new GridLayout(3, 1, 6, 6));
        southPanel.setOpaque(false);
        southPanel.add(viewGroupBtn);
        southPanel.add(addMemberBtn);
        southPanel.add(createGroupBtn);
        panel.add(southPanel, BorderLayout.SOUTH);

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

    // Interfaces de eventos
    public interface OnViewTempMessagesListener {
        void onViewTempMessages();
    }

    // Setters de listeners
    public void setOnViewTempMessagesListener(OnViewTempMessagesListener listener) {
        this.onViewTempMessagesListener = listener;
    }

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

    /* método para actualizar el badge de mensajes temporales */
    public void setTempMessageCount(int count) {
        if (tempInboxBadgeLabel == null) {
            tempInboxBadgeLabel = new JLabel();
            tempInboxBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            tempInboxBadgeLabel.setForeground(Color.WHITE);
            tempInboxBadgeLabel.setBackground(new Color(220, 53, 69));
            tempInboxBadgeLabel.setOpaque(true);
            tempInboxBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            tempInboxBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            tempInboxBadgeLabel.setVisible(false);
        }
        tempInboxBadgeLabel.setText(String.valueOf(count));
        tempInboxBadgeLabel.setVisible(count > 0);
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
        JButton actionBtn = new JButton("Invitar");
        actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        actionBtn.setFocusPainted(false);
        actionBtn.setBackground(new Color(255, 140, 0));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setToolTipText("Enviar solicitud de amistad");

        // Tamaño fijo de 40×40
        Dimension btnSize = new Dimension(82, 36);
        actionBtn.setMinimumSize(btnSize);
        actionBtn.setPreferredSize(btnSize);
        actionBtn.setMaximumSize(btnSize);
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionBtn.addActionListener(e -> {
            if (onUserActionListener != null) onUserActionListener.onUserAction(user);
        });
        row.add(actionBtn);

        row.add(Box.createRigidArea(new Dimension(6, 0)));

        JButton tempBtn = new JButton("Temp");
        tempBtn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tempBtn.setFocusPainted(false);
        tempBtn.setBackground(user.isOnline() ? new Color(0, 123, 255) : new Color(200, 200, 200));
        tempBtn.setForeground(Color.WHITE);
        tempBtn.setToolTipText(user.isOnline()
                ? "Enviar mensaje temporal a este usuario"
                : "Usuario desconectado – no se pueden enviar mensajes temporales");
        tempBtn.setEnabled(user.isOnline());                       // <-- deshabilita cuando offline
        tempBtn.setMinimumSize(btnSize);
        tempBtn.setPreferredSize(btnSize);
        tempBtn.setMaximumSize(btnSize);
        tempBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        tempBtn.addActionListener(e -> {
            if (!user.isOnline()) {
                JOptionPane.showMessageDialog(this,
                        "El usuario está desconectado y no puede recibir mensajes temporales.",
                        "No disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (onSendTemporaryMessageListener == null) {
                return;
            }
            String message = JOptionPane.showInputDialog(this, "Mensaje temporal para " + user.getName() + ":");
            if (message != null && !message.trim().isEmpty()) {
                onSendTemporaryMessageListener.onSendTemporaryMessage(message.trim(), user);
            }
        });
        row.add(tempBtn);

        // Evita que la fila se estire verticalmente
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

        return row;
    }

    public void setInvitations(List<GroupInvitation> invitations) {
        groupInvitationsPanel.removeAll();
        if (invitations == null || invitations.isEmpty()) {
            groupInvitationsPanel.add(createEmptyState("Sin invitaciones de grupo"));
        } else {
            for (GroupInvitation inv : invitations) {
                groupInvitationsPanel.add(createInvitationCard(inv));
                groupInvitationsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        groupInvitationsPanel.revalidate();
        groupInvitationsPanel.repaint();
    }

    public void setFriendInvitations(List<FriendInvitation> invitations) {
        friendInvitationsPanel.removeAll();
        if (invitations == null || invitations.isEmpty()) {
            friendInvitationsPanel.add(createEmptyState("Sin solicitudes de amistad"));
        } else {
            for (FriendInvitation inv : invitations) {
                friendInvitationsPanel.add(createFriendInvitationCard(inv));
                friendInvitationsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        friendInvitationsPanel.revalidate();
        friendInvitationsPanel.repaint();
    }

    private JLabel createEmptyState(String message) {
        JLabel emptyLabel = new JLabel(message);
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        return emptyLabel;
    }

    private JPanel createFriendInvitationCard(FriendInvitation inv) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel avatar = new JLabel(inv.getRequesterName().substring(0, 1).toUpperCase());
        avatar.setOpaque(true);
        avatar.setBackground(new Color(70, 130, 180));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36, 36));
        card.add(avatar, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(inv.getRequesterName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel statusLabel = new JLabel(inv.isIncoming()
                ? "Quiere agregarte como amigo"
                : "Solicitud enviada, esperando respuesta");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(90, 90, 90));
        textPanel.add(nameLabel);
        textPanel.add(statusLabel);
        card.add(textPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        btnPanel.setOpaque(false);
        if (inv.isIncoming()) {
            JButton acceptBtn = new JButton("Aceptar");
            acceptBtn.setBackground(new Color(40, 167, 69));
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setFocusPainted(false);
            acceptBtn.addActionListener(e -> {
                if (onFriendInvitationActionListener != null) onFriendInvitationActionListener.onAccept(inv);
            });

            JButton rejectBtn = new JButton("Rechazar");
            rejectBtn.setBackground(new Color(220, 53, 69));
            rejectBtn.setForeground(Color.WHITE);
            rejectBtn.setFocusPainted(false);
            rejectBtn.addActionListener(e -> {
                if (onFriendInvitationActionListener != null) onFriendInvitationActionListener.onReject(inv);
            });
            btnPanel.add(acceptBtn);
            btnPanel.add(rejectBtn);
        } else {
            JLabel pendingLabel = new JLabel("Pendiente");
            pendingLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            pendingLabel.setForeground(new Color(255, 140, 0));
            btnPanel.add(pendingLabel);
        }
        card.add(btnPanel, BorderLayout.EAST);
        return card;
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

    public void setTempMessageCount(int count) {
        if (tempInboxBadgeLabel == null) return;
        tempInboxBadgeLabel.setText(String.valueOf(count));
        tempInboxBadgeLabel.setVisible(count > 0);
    }

    public void setPendingFriendChatCount(int count) {
        if (count <= 0) {
            count = 2;
        }
        pendingInboxBadgeLabel.setText(String.valueOf(count));
        pendingInboxBadgeLabel.setVisible(count > 0);
    }

    private List<FriendConversation> createSampleFriendConversations() {
        List<FriendConversation> friends = new ArrayList<>();
        friends.add(new FriendConversation("Juan Perez", "Nos vemos manana?", "10:45", true, true, 1));
        friends.add(new FriendConversation("Maria Garcia", "Perfecto, gracias", "10:30", true, true, 2));
        friends.add(new FriendConversation("Carlos Lopez", "Te enviare el archivo", "09:15", false, false, 3));
        return friends;
    }

    private List<GroupItem> createSampleGroups() {
        List<GroupItem> groups = new ArrayList<>();
        groups.add(new GroupItem("Estudio Java", 5, true, 101));
        groups.add(new GroupItem("Desarrollo Web", 8, false, 102));
        groups.add(new GroupItem("Proyecto Final", 6, true, 103));
        return groups;
    }

    private List<UserItem> createSampleUsers() {
        List<UserItem> users = new ArrayList<>();
        users.add(new UserItem("Pedro Diaz", true, 201));
        users.add(new UserItem("Valentina Ruiz", true, 202));
        users.add(new UserItem("Mateo Salazar", false, 203));
        users.add(new UserItem("Camila Ortega", true, 204));
        return users;
    }

    private List<GroupInvitation> createSampleGroupInvitations() {
        List<GroupInvitation> invitations = new ArrayList<>();
        invitations.add(new GroupInvitation("Luis Contreras", "Proyecto Final", 103, 301));
        invitations.add(new GroupInvitation("Maria Rodriguez", "Diseno UI/UX", 104, 302));
        return invitations;
    }

    private List<FriendInvitation> createSampleFriendInvitations() {
        List<FriendInvitation> invitations = new ArrayList<>();
        invitations.add(new FriendInvitation("Ana Torres", 401, true));
        invitations.add(new FriendInvitation("Diego Ramos", 402, false));
        return invitations;
    }

    private void showSamplePendingFriendChats() {
        PendingFriendChatsModal modal = new PendingFriendChatsModal(this);
        List<PendingFriendChatsModal.PendingFriendChat> pendingChats = new ArrayList<>();
        pendingChats.add(new PendingFriendChatsModal.PendingFriendChat(1, "Juan Perez", 2, "Tambien te deje otro mensaje pendiente.", "11:05"));
        pendingChats.add(new PendingFriendChatsModal.PendingFriendChat(2, "Maria Garcia", 1, "Perfecto, gracias", "10:30"));
        modal.setPendingFriendChats(pendingChats);
        modal.setOnOpenPendingChatListener(pending -> {
            FriendRequestModal chat = new FriendRequestModal(this, pending.getFriendName());
            List<FriendRequestModal.ChatMessage> messages = new ArrayList<>();
            messages.add(new FriendRequestModal.ChatMessage(pending.getFriendName(), pending.getLastMessage(), pending.getLastTime(), false, true));
            messages.add(new FriendRequestModal.ChatMessage("Tu", "Ya vi tu mensaje pendiente.", "11:15", true, false));
            chat.setMessages(messages);
            chat.setVisible(true);
        });
        modal.setVisible(true);
    }

    private void updateGroupBadge(int count) {
        groupBadgeLabel.setText(String.valueOf(count));
        groupBadgeLabel.setVisible(count > 0);
    }

    public void addGroup(GroupItem group) {
        if (groupListModel == null) groupListModel = new DefaultListModel<>();
        groupListModel.addElement(group);
        int updateCount = (int) java.util.Collections.list(groupListModel.elements()).stream().filter(g -> g.hasUpdate()).count();
        updateGroupBadge(updateCount);
    }

    // Setters de listeners
    public void setOnFriendChatSelectedListener(OnFriendChatSelectedListener listener) { this.onFriendChatSelectedListener = listener; }
    public void setOnGroupSelectedListener(OnGroupSelectedListener listener) { this.onGroupSelectedListener = listener; }
    public void setOnUserActionListener(OnUserActionListener listener) { this.onUserActionListener = listener; }
    public void setOnInvitationActionListener(OnInvitationActionListener listener) { this.onInvitationActionListener = listener; }
    public void setOnSendTemporaryMessageListener(OnSendTemporaryMessageListener listener) { this.onSendTemporaryMessageListener = listener; }
    public void setOnPendingMessagesSelectedListener(OnPendingMessagesSelectedListener listener) { this.onPendingMessagesSelectedListener = listener; }
    public void setOnFriendInvitationActionListener(OnFriendInvitationActionListener listener) { this.onFriendInvitationActionListener = listener; }
    public void setOnCreateGroupListener(OnCreateGroupListener listener) { this.onCreateGroupListener = listener; }
    public void setOnInviteToGroupListener(OnInviteToGroupListener listener) { this.onInviteToGroupListener = listener; }

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

            List<FriendInvitation> friendInvitations = new ArrayList<>();
            friendInvitations.add(new FriendInvitation("Ana Torres", 401, true));
            friendInvitations.add(new FriendInvitation("Diego Ramos", 402, false));
            window.setFriendInvitations(friendInvitations);
            window.setPendingFriendChatCount(2);

            window.setOnFriendChatSelectedListener(friend -> {
                FriendRequestModal modal = new FriendRequestModal(window, friend.getName());
                List<FriendRequestModal.ChatMessage> messages = new ArrayList<>();
                messages.add(new FriendRequestModal.ChatMessage(friend.getName(), friend.getLastMessage(), friend.getTime(), false, friend.isUnread()));
                messages.add(new FriendRequestModal.ChatMessage("Tu", "Entendido, lo reviso mas tarde.", "10:50", true, false));
                messages.add(new FriendRequestModal.ChatMessage(friend.getName(), "Tambien te deje otro mensaje pendiente.", "11:05", false, true));
                modal.setMessages(messages);
                modal.setVisible(true);
            });

            window.setOnPendingMessagesSelectedListener(() -> {
                PendingFriendChatsModal modal = new PendingFriendChatsModal(window);
                List<PendingFriendChatsModal.PendingFriendChat> pendingChats = new ArrayList<>();
                pendingChats.add(new PendingFriendChatsModal.PendingFriendChat(1, "Juan Perez", 2, "Tambien te deje otro mensaje pendiente.", "11:05"));
                pendingChats.add(new PendingFriendChatsModal.PendingFriendChat(2, "Maria Garcia", 1, "Perfecto, gracias", "10:30"));
                modal.setPendingFriendChats(pendingChats);
                modal.setOnOpenPendingChatListener(pending -> {
                    FriendRequestModal chat = new FriendRequestModal(window, pending.getFriendName());
                    List<FriendRequestModal.ChatMessage> messages = new ArrayList<>();
                    messages.add(new FriendRequestModal.ChatMessage(pending.getFriendName(), pending.getLastMessage(), pending.getLastTime(), false, true));
                    messages.add(new FriendRequestModal.ChatMessage("Tu", "Ya vi tu mensaje pendiente.", "11:15", true, false));
                    chat.setMessages(messages);
                    chat.setVisible(true);
                });
                modal.setVisible(true);
            });

            window.setVisible(true);
        });
    }
}
