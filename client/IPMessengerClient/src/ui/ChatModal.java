// Modal de chat (para amigo/grupo/todos)
package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * ChatModal - Interfaz principal de IP Messenger.
 * Diseñada con componentes modulares para ser integrada con lógica de negocio.
 * Los datos mostrados son ejemplos; utilizar métodos públicos para actualizar.
 */
public class ChatModal extends JFrame {

    // Paneles principales
    private JPanel invitationsPanel;          // Panel horizontal de invitaciones
    private JList<FriendConversation> friendList;
    private DefaultListModel<FriendConversation> friendListModel;
    private JPanel allUsersPanel;              // Panel vertical con lista de usuarios (Botón +)
    private JList<GroupItem> groupList;
    private DefaultListModel<GroupItem> groupListModel;

    // Etiquetas de badges
    private JLabel friendBadgeLabel;
    private JLabel groupBadgeLabel;

    // Campo temporal de mensaje
    private JTextField tempMessageField;

    // Usuario actualmente seleccionado en "Todos los Usuarios"
    private UserItem currentlySelectedUser = null;

    // Listeners para acciones externas
    private OnFriendChatSelectedListener onFriendChatSelectedListener;
    private OnGroupSelectedListener onGroupSelectedListener;
    private OnUserActionListener onUserActionListener;
    private OnInvitationActionListener onInvitationActionListener;
    private OnSendTempMessageListener onSendTempMessageListener;

    // Modelos de datos internos
    public static class FriendConversation {
        private String name;
        private String lastMessage;
        private String time;
        private boolean unread;
        private int userId;

        public FriendConversation(String name, String lastMessage, String time, boolean unread, int userId) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.time = time;
            this.unread = unread;
            this.userId = userId;
        }

        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public String getTime() { return time; }
        public boolean isUnread() { return unread; }
        public int getUserId() { return userId; }
    }

    public static class GroupItem {
        private String name;
        private int memberCount;
        private int groupId;

        public GroupItem(String name, int memberCount, int groupId) {
            this.name = name;
            this.memberCount = memberCount;
            this.groupId = groupId;
        }

        public String getName() { return name; }
        public int getMemberCount() { return memberCount; }
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

    // Interfaz para callbacks
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

    public interface OnSendTempMessageListener {
        void onSendTempMessage(String message, UserItem targetUser);
    }

    // Constructor
    public ChatModal() {
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setTitle("IP Messenger");
    }

    private void initUI() {
        // Panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Barra superior personalizada (título + botones ventana)
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // Panel de invitaciones (horizontal, scroll)
        mainPanel.add(createInvitationsRow(), BorderLayout.CENTER);

        // Panel de tres columnas
        mainPanel.add(createThreeColumnsPanel(), BorderLayout.SOUTH);

        // Ajuste para que la sección de invitaciones no ocupe todo el centro
        // En realidad usaremos BorderLayout: NORTH (title), CENTER (invitations), SOUTH (3 columns)
        // Pero SOUTH necesita mucho espacio, definimos constraints.
        // Mejor rediseño: Title NORTH, debajo un panel con GridBagLayout para que invitations ocupe poco y columnas el resto.
        // Para mayor claridad, reemplazamos: mainPanel con BorderLayout, NORTH title, CENTER un panel vertical con invitations + columnas.
        // Corregir:
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

        // Flecha atrás
        JButton backButton = new JButton("←");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> System.out.println("[UI] Navegar atrás"));
        titleBar.add(backButton, BorderLayout.WEST);

        // Título
        JLabel titleLabel = new JLabel("IP Messenger");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBar.add(titleLabel, BorderLayout.CENTER);

        // Botones de ventana (minimizar, maximizar, cerrar)
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

        // Flecha derecha (simular más invitaciones)
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

        // Columna izquierda: Mensajes con Amigos
        columnsPanel.add(createFriendsColumn());

        // Columna central: Todos los Usuarios
        columnsPanel.add(createAllUsersColumn());

        // Columna derecha: Mis Grupos
        columnsPanel.add(createGroupsColumn());

        return columnsPanel;
    }

    private JPanel createFriendsColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        // Header con badge
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

        // Lista de amigos
        friendListModel = new DefaultListModel<>();
        friendList = new JList<>(friendListModel);
        friendList.setCellRenderer(new FriendListRenderer());
        friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendList.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        JScrollPane scroll = new JScrollPane(friendList);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        // Botón abrir chat
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

        // Panel inferior: mensaje temporal
        JPanel tempPanel = new JPanel(new BorderLayout(8, 0));
        tempPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        tempPanel.setBackground(Color.WHITE);
        tempMessageField = new JTextField();
        tempMessageField.putClientProperty("JTextField.placeholder", "Mandar mensaje temporal");
        tempMessageField.setForeground(Color.GRAY);
        tempMessageField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tempMessageField.getText().equals("Mandar mensaje temporal")) {
                    tempMessageField.setText("");
                    tempMessageField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (tempMessageField.getText().isEmpty()) {
                    tempMessageField.setForeground(Color.GRAY);
                    tempMessageField.setText("Mandar mensaje temporal");
                }
            }
        });
        tempMessageField.setText("Mandar mensaje temporal");

        JButton sendBtn = new JButton("📨");
        sendBtn.setFocusPainted(false);
        sendBtn.addActionListener(e -> {
            String msg = tempMessageField.getText().trim();
            if (!msg.isEmpty() && !msg.equals("Mandar mensaje temporal") && currentlySelectedUser != null) {
                if (onSendTempMessageListener != null) {
                    onSendTempMessageListener.onSendTempMessage(msg, currentlySelectedUser);
                }
                tempMessageField.setText("Mandar mensaje temporal");
                tempMessageField.setForeground(Color.GRAY);
            } else if (currentlySelectedUser == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista");
            }
        });
        tempPanel.add(tempMessageField, BorderLayout.CENTER);
        tempPanel.add(sendBtn, BorderLayout.EAST);
        panel.add(tempPanel, BorderLayout.SOUTH);

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

    // Renderers personalizados
    private class FriendListRenderer extends JPanel implements ListCellRenderer<FriendConversation> {
        private JLabel avatarLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel msgLabel = new JLabel();
        private JLabel timeLabel = new JLabel();

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
            add(timeLabel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FriendConversation> list, FriendConversation value, int index, boolean isSelected, boolean cellHasFocus) {
            String initials = value.getName().substring(0, 1).toUpperCase();
            avatarLabel.setText(initials);
            avatarLabel.setBackground(new Color(100, 149, 237));
            avatarLabel.setForeground(Color.WHITE);
            nameLabel.setText(value.getName());
            msgLabel.setText(value.getLastMessage());
            timeLabel.setText(value.getTime());
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

    private class GroupListRenderer extends JPanel implements ListCellRenderer<GroupItem> {
        private JLabel avatarLabel = new JLabel();
        private JLabel nameLabel = new JLabel();
        private JLabel memberLabel = new JLabel();

        GroupListRenderer() {
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            avatarLabel.setOpaque(true);
            avatarLabel.setPreferredSize(new Dimension(40, 40));
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            memberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            JPanel textPanel = new JPanel(new BorderLayout());
            textPanel.setOpaque(false);
            textPanel.add(nameLabel, BorderLayout.NORTH);
            textPanel.add(memberLabel, BorderLayout.SOUTH);
            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GroupItem> list, GroupItem value, int index, boolean isSelected, boolean cellHasFocus) {
            avatarLabel.setText(value.getName().substring(0, 1).toUpperCase());
            avatarLabel.setBackground(new Color(102, 205, 170));
            avatarLabel.setForeground(Color.WHITE);
            nameLabel.setText(value.getName());
            memberLabel.setText("👥 " + value.getMemberCount() + " miembros");
            setBackground(isSelected ? new Color(230, 242, 255) : Color.WHITE);
            return this;
        }
    }

    // Métodos públicos para actualizar datos desde el controlador
    public void setFriendList(List<FriendConversation> conversations) {
        friendListModel.clear();
        for (FriendConversation fc : conversations) {
            friendListModel.addElement(fc);
        }
        updateFriendBadge((int) conversations.stream().filter(FriendConversation::isUnread).count());
    }

    public void setGroupList(List<GroupItem> groups) {
        groupListModel.clear();
        for (GroupItem g : groups) {
            groupListModel.addElement(g);
        }
        updateGroupBadge(groups.size()); // según diseño se muestra número total o nuevos, ajustar
    }

    public void setAllUsers(List<UserItem> users) {
        allUsersPanel.removeAll();
        for (UserItem user : users) {
            UserRowPanel row = new UserRowPanel(user, this);
            allUsersPanel.add(row);
            allUsersPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        allUsersPanel.revalidate();
        allUsersPanel.repaint();
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

    public void updateFriendBadge(int count) {
        friendBadgeLabel.setText(String.valueOf(count));
        friendBadgeLabel.setVisible(count > 0);
    }

    public void updateGroupBadge(int count) {
        groupBadgeLabel.setText(String.valueOf(count));
        groupBadgeLabel.setVisible(count > 0);
    }

    public void setSelectedUser(UserItem user) {
        this.currentlySelectedUser = user;
        // Resaltar visualmente en allUsersPanel
        for (Component comp : allUsersPanel.getComponents()) {
            if (comp instanceof UserRowPanel) {
                ((UserRowPanel) comp).setSelected(((UserRowPanel) comp).getUser().getUserId() == user.getUserId());
            }
        }
    }

    // Setters de listeners
    public void setOnFriendChatSelectedListener(OnFriendChatSelectedListener listener) { this.onFriendChatSelectedListener = listener; }
    public void setOnGroupSelectedListener(OnGroupSelectedListener listener) { this.onGroupSelectedListener = listener; }
    public void setOnUserActionListener(OnUserActionListener listener) { this.onUserActionListener = listener; }
    public void setOnInvitationActionListener(OnInvitationActionListener listener) { this.onInvitationActionListener = listener; }
    public void setOnSendTempMessageListener(OnSendTempMessageListener listener) { this.onSendTempMessageListener = listener; }

    // Clase interna para fila de usuario con botón
    private class UserRowPanel extends JPanel {
        private UserItem user;
        private boolean selected = false;

        UserRowPanel(UserItem user, ChatModal parent) {
            this.user = user;
            setLayout(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            setBackground(Color.WHITE);

            JLabel avatar = new JLabel(user.getName().substring(0, 1));
            avatar.setOpaque(true);
            avatar.setBackground(new Color(70, 130, 180));
            avatar.setForeground(Color.WHITE);
            avatar.setHorizontalAlignment(SwingConstants.CENTER);
            avatar.setPreferredSize(new Dimension(36, 36));
            add(avatar, BorderLayout.WEST);

            JLabel nameLabel = new JLabel(user.getName());
            nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JLabel statusLabel = new JLabel(user.isOnline() ? "● En línea" : "○ Desconectado");
            statusLabel.setForeground(user.isOnline() ? new Color(40, 167, 69) : Color.GRAY);
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(statusLabel);
            add(textPanel, BorderLayout.CENTER);

            JButton actionBtn = new JButton("+");
            actionBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            actionBtn.setFocusPainted(false);
            actionBtn.setBackground(new Color(0, 123, 255));
            actionBtn.setForeground(Color.WHITE);
            actionBtn.addActionListener(e -> {
                if (onUserActionListener != null) onUserActionListener.onUserAction(user);
            });
            add(actionBtn, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    parent.setSelectedUser(user);
                }
            });
        }

        public UserItem getUser() { return user; }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setBackground(selected ? new Color(230, 242, 255) : Color.WHITE);
        }
    }

    // Main para prueba visual (datos dummy)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatModal frame = new ChatModal();
            // Datos de ejemplo para mostrar la interfaz
            List<FriendConversation> friends = Arrays.asList(
                new FriendConversation("Juan Pérez", "¿Nos vemos mañana?", "10:45", true, 1),
                new FriendConversation("María García", "Perfecto, gracias", "10:30", true, 2),
                new FriendConversation("Carlos López", "Te enviaré el archivo", "09:15", true, 3)
            );
            frame.setFriendList(friends);
            List<GroupItem> groups = Arrays.asList(
                new GroupItem("Estudio Java", 5, 101),
                new GroupItem("Desarrollo Web", 8, 102),
                new GroupItem("Proyecto Final", 6, 103)
            );
            frame.setGroupList(groups);
            List<UserItem> users = Arrays.asList(
                new UserItem("Pedro Díaz", true, 201),
                new UserItem("Valentina Ruiz", true, 202),
                new UserItem("Mateo Salazar", false, 203),
                new UserItem("Camila Ortega", true, 204)
            );
            frame.setAllUsers(users);
            List<GroupInvitation> invs = Arrays.asList(
                new GroupInvitation("Luis Contreras", "Proyecto Final", 103, 301),
                new GroupInvitation("María Rodríguez", "Diseño UI/UX", 104, 302)
            );
            frame.setInvitations(invs);
            frame.setVisible(true);
        });
    }
}