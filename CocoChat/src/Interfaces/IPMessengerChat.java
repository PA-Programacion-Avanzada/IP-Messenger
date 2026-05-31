package Interfaces;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IPMessengerChat extends JFrame {

    // Paneles principales
    private JPanel invitationPanel;
    private JPanel leftColumn, centerColumn, rightColumn;

    public IPMessengerChat() {
        initComponents();
        setTitle("IP Messenger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(950, 600));
        setVisible(true);
    }

    private void initComponents() {
        // Contenedor principal con BorderLayout
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(Color.WHITE);
        setContentPane(mainContainer);

        // Panel de invitaciones (parte superior)
        invitationPanel = createInvitationPanel();
        mainContainer.add(invitationPanel, BorderLayout.NORTH);

        // Panel de las tres columnas
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        columnsPanel.setBackground(Color.WHITE);
        columnsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        leftColumn = createLeftColumn();
        centerColumn = createCenterColumn();
        rightColumn = createRightColumn();

        columnsPanel.add(leftColumn);
        columnsPanel.add(centerColumn);
        columnsPanel.add(rightColumn);

        mainContainer.add(columnsPanel, BorderLayout.CENTER);
    }

    /* ===================== PANEL DE INVITACIONES ===================== */
    private JPanel createInvitationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // Flecha izquierda
        JLabel leftArrow = new JLabel("◀");
        leftArrow.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        leftArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        leftArrow.setForeground(new Color(0, 120, 215));

        // Panel con las invitaciones
        JPanel invitationsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        invitationsContainer.setBackground(Color.WHITE);

        invitationsContainer.add(createInvitationCard("Luis Contreras", "Proyecto Final"));
        invitationsContainer.add(createInvitationCard("María Rodríguez", "Diseño UI/UX"));
        invitationsContainer.add(createInvitationCard("Andrés Gómez", "Desarrollo Web"));

        // Flecha derecha
        JLabel rightArrow = new JLabel("▶");
        rightArrow.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        rightArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rightArrow.setForeground(new Color(0, 120, 215));

        panel.add(leftArrow, BorderLayout.WEST);
        panel.add(invitationsContainer, BorderLayout.CENTER);
        panel.add(rightArrow, BorderLayout.EAST);

        return panel;
    }

    private JPanel createInvitationCard(String userName, String groupName) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 248, 248));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        card.setPreferredSize(new Dimension(220, 100));

        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel groupLabel = new JLabel("invita a: " + groupName);
        groupLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        groupLabel.setForeground(Color.DARK_GRAY);
        groupLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonsPanel.setBackground(new Color(248, 248, 248));

        JButton acceptBtn = new JButton("✓ Aceptar");
        acceptBtn.setBackground(new Color(76, 175, 80)); // Verde
        acceptBtn.setForeground(Color.WHITE);
        acceptBtn.setFocusPainted(false);
        acceptBtn.setBorderPainted(false);
        acceptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton rejectBtn = new JButton("✗ Rechazar");
        rejectBtn.setBackground(new Color(244, 67, 54)); // Rojo
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFocusPainted(false);
        rejectBtn.setBorderPainted(false);
        rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        buttonsPanel.add(acceptBtn);
        buttonsPanel.add(rejectBtn);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(groupLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(buttonsPanel);

        return card;
    }

    /* ===================== COLUMNA IZQUIERDA (Amigos) ===================== */
    private JPanel createLeftColumn() {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Título con badge
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 120, 215));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Mensajes con Amigos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Badge rojo con "3"
        JPanel badge = new JPanel(new GridBagLayout());
        badge.setBackground(Color.RED);
        badge.setPreferredSize(new Dimension(24, 24));
        badge.setOpaque(true);
        JLabel badgeText = new JLabel("3");
        badgeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeText.setForeground(Color.WHITE);
        badge.add(badgeText);
        header.add(badge, BorderLayout.EAST);

        column.add(header, BorderLayout.NORTH);

        // Lista de contactos
        JPanel contactsPanel = new JPanel();
        contactsPanel.setLayout(new BoxLayout(contactsPanel, BoxLayout.Y_AXIS));
        contactsPanel.setBackground(Color.WHITE);

        // Datos de ejemplo
        Object[][] friends = {
                {"Carlos López", "Hola, ¿cómo estás?", "10:30 AM", true},
                {"Ana Martínez", "Nos vemos mañana", "Ayer", false},
                {"Pedro Gómez", "Archivo enviado", "12:45 PM", true},
                {"Laura Ríos", "Gracias!", "Lunes", true}
        };

        for (Object[] f : friends) {
            contactsPanel.add(createFriendRow((String) f[0], (String) f[1], (String) f[2], (boolean) f[3]));
        }

        JScrollPane scrollPane = new JScrollPane(contactsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        column.add(scrollPane, BorderLayout.CENTER);

        // Botón inferior
        JButton openChatBtn = new JButton("Abrir Chat Seleccionado");
        openChatBtn.setBackground(new Color(173, 216, 230)); // Azul claro
        openChatBtn.setForeground(Color.BLACK);
        openChatBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        openChatBtn.setFocusPainted(false);
        openChatBtn.setBorderPainted(false);
        openChatBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        column.add(openChatBtn, BorderLayout.SOUTH);

        return column;
    }

    private JPanel createFriendRow(String name, String lastMsg, String time, boolean online) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Panel izquierdo: círculo con iniciales y estado en línea
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarPanel.setBackground(Color.WHITE);
        JPanel circle = createCircle(name.charAt(0) + "", new Color(100, 149, 237), 40);
        if (online) {
            JLabel onlineDot = new JLabel("●");
            onlineDot.setFont(new Font("Segoe UI", Font.BOLD, 14));
            onlineDot.setForeground(new Color(76, 175, 80));
            onlineDot.setHorizontalAlignment(SwingConstants.RIGHT);
            onlineDot.setVerticalAlignment(SwingConstants.BOTTOM);
            circle.add(onlineDot, BorderLayout.SOUTH);
        }
        avatarPanel.add(circle, BorderLayout.WEST);

        // Centro: nombre y último mensaje
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel msgLbl = new JLabel(lastMsg);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        msgLbl.setForeground(Color.GRAY);
        textPanel.add(nameLbl);
        textPanel.add(msgLbl);

        row.add(avatarPanel, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        // Hora
        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLbl.setForeground(Color.GRAY);
        row.add(timeLbl, BorderLayout.EAST);

        return row;
    }

    /* ===================== COLUMNA CENTRAL (Todos los usuarios) ===================== */
    private JPanel createCenterColumn() {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel title = new JLabel("Todos los Usuarios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(10, 15, 10, 15));
        title.setBackground(new Color(0, 120, 215));
        title.setOpaque(true);
        title.setForeground(Color.WHITE);
        column.add(title, BorderLayout.NORTH);

        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setBackground(Color.WHITE);

        Object[][] users = {
                {"Juan Pérez", "En línea", true},
                {"María García", "Desconectado", false},
                {"Carlos Ruiz", "En línea", true},
                {"Elena Torres", "Desconectado", false}
        };

        for (Object[] u : users) {
            usersPanel.add(createUserRow((String) u[0], (String) u[1], (boolean) u[2]));
        }

        JScrollPane scroll = new JScrollPane(usersPanel);
        scroll.setBorder(null);
        column.add(scroll, BorderLayout.CENTER);

        return column;
    }

    private JPanel createUserRow(String name, String status, boolean online) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel circle = createCircle(name.charAt(0) + "", randomPastelColor(name), 35);
        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(online ? new Color(76, 175, 80) : Color.GRAY);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(Color.WHITE);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        text.add(nameLbl);
        text.add(statusLbl);

        // Botón de añadir (icono persona con +)
        JButton addBtn = new JButton("+");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setForeground(new Color(0, 120, 215));
        addBtn.setBorderPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setFocusPainted(false);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        row.add(circle, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(addBtn, BorderLayout.EAST);

        return row;
    }

    /* ===================== COLUMNA DERECHA (Grupos) ===================== */
    private JPanel createRightColumn() {
        JPanel column = new JPanel(new BorderLayout());
        column.setBackground(Color.WHITE);
        column.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 120, 215));
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Mis Grupos");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel badge = new JPanel(new GridBagLayout());
        badge.setBackground(Color.RED);
        badge.setPreferredSize(new Dimension(24, 24));
        JLabel badgeText = new JLabel("2");
        badgeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badgeText.setForeground(Color.WHITE);
        badge.add(badgeText);
        header.add(badge, BorderLayout.EAST);

        column.add(header, BorderLayout.NORTH);

        JPanel groupsPanel = new JPanel();
        groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));
        groupsPanel.setBackground(Color.WHITE);

        Object[][] groups = {
                {"Estudio Java", 5},
                {"Proyecto Final", 8},
                {"Diseño UI/UX", 3}
        };

        for (Object[] g : groups) {
            groupsPanel.add(createGroupRow((String) g[0], (int) g[1]));
        }

        JScrollPane scroll = new JScrollPane(groupsPanel);
        scroll.setBorder(null);
        column.add(scroll, BorderLayout.CENTER);

        JButton viewGroupBtn = new JButton("Ver Grupo Seleccionado");
        viewGroupBtn.setBackground(new Color(0, 120, 215));
        viewGroupBtn.setForeground(Color.WHITE);
        viewGroupBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewGroupBtn.setFocusPainted(false);
        viewGroupBtn.setBorderPainted(false);
        viewGroupBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        column.add(viewGroupBtn, BorderLayout.SOUTH);

        return column;
    }

    private JPanel createGroupRow(String groupName, int members) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JPanel circle = createCircle(groupName.substring(0, 1).toUpperCase(), randomPastelColor(groupName), 35);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        JLabel nameLbl = new JLabel(groupName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel membersLbl = new JLabel("👥 " + members + " miembros");
        membersLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        membersLbl.setForeground(Color.GRAY);
        info.add(nameLbl);
        info.add(membersLbl);

        row.add(circle, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);

        // Agregar listener para abrir modal de confirmación de salida (doble clic o botón)
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    new SalirGrupoDialog(IPMessengerChat.this, groupName).setVisible(true);
                }
            }
        });

        return row;
    }

    /* ===================== UTILIDADES ===================== */
    private JPanel createCircle(String text, Color bg, int size) {
        JPanel circle = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bg);
                g.fillOval(0, 0, size, size);
            }
        };
        circle.setPreferredSize(new Dimension(size, size));
        circle.setMaximumSize(new Dimension(size, size));
        circle.setMinimumSize(new Dimension(size, size));
        circle.setOpaque(false);
        JLabel letter = new JLabel(text);
        letter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        letter.setForeground(Color.WHITE);
        circle.add(letter);
        return circle;
    }

    private Color randomPastelColor(String seed) {
        int hash = seed.hashCode();
        int r = 150 + Math.abs(hash % 80);
        int g = 150 + Math.abs((hash * 2) % 80);
        int b = 150 + Math.abs((hash * 3) % 80);
        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new IPMessengerChat());
    }
}