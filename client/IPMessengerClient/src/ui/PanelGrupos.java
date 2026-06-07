/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * PanelGrupos - Interfaz de chat grupal tipo WhatsApp.
 * Muestra mensajes de grupo, lista de participantes y campo para enviar mensajes.
 */
public class PanelGrupos extends JPanel {
    private static final int MEMBERS_PANEL_WIDTH = 240;
    private static final int ANIMATION_STEP = 16;
    private static final int ANIMATION_DELAY = 10;

    private JPanel messagesContainer;
    private JScrollPane messagesScrollPane;
    private JPanel membersContainer;
    private JTextField messageInput;
    private JButton sendButton;
    private JButton toggleMembersButton;
    private JLabel groupNameLabel;
    private JLabel groupSubtitleLabel;
    private boolean membersVisible = true;

    private boolean estado = true;

    public interface OnSendGroupMessageListener {
        void onSendGroupMessage(String message);
    }

    private OnSendGroupMessageListener onSendGroupMessageListener;

    public void setOnSendGroupMessageListener(OnSendGroupMessageListener listener) {
        this.onSendGroupMessageListener = listener;
    }

    public PanelGrupos() {
        buildUI();
        setGroupInfo("Grupo de chat", 0);
        setGroupMembers(new ArrayList<>());
    }

    private void buildUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createInputPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);

        groupNameLabel = new JLabel("Grupo de prueba");
        groupNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        groupSubtitleLabel = new JLabel("Chat de grupo · 5 miembros");
        groupSubtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        groupSubtitleLabel.setForeground(Color.DARK_GRAY);

        labelPanel.add(groupNameLabel);
        labelPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        labelPanel.add(groupSubtitleLabel);

        header.add(labelPanel, BorderLayout.WEST);

        toggleMembersButton = new JButton("Ocultar miembros");
        stylePrimaryButton(toggleMembersButton);
        toggleMembersButton.addActionListener(e -> toggleMembersPanel());
        header.add(toggleMembersButton, BorderLayout.EAST);

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 14, 12, 14)));

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(new Color(245, 247, 250));

        messagesScrollPane = new JScrollPane(messagesContainer);
        messagesScrollPane.setBorder(BorderFactory.createEmptyBorder());
        messagesScrollPane.getViewport().setBackground(new Color(245, 247, 250));
        messagesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        center.add(messagesScrollPane, BorderLayout.CENTER);
        center.add(createMembersPanel(), BorderLayout.EAST);

        return center;
    }

    private JPanel createMembersPanel() {
        membersContainer = new JPanel();
        membersContainer.setLayout(new BoxLayout(membersContainer, BoxLayout.Y_AXIS));
        membersContainer.setBackground(Color.WHITE);
        membersContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(12, 12, 12, 12)));
        membersContainer.setPreferredSize(new Dimension(MEMBERS_PANEL_WIDTH, 0));

        JLabel title = new JLabel("Miembros del grupo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        membersContainer.add(title);
        membersContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        return membersContainer;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(8, 0, 0, 0));

        messageInput = new JTextField();
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 12, 10, 12)));
        messageInput.setText("");
        messageInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                messageInput.setForeground(Color.BLACK);
            }
        });

        sendButton = new JButton("Enviar");
        stylePrimaryButton(sendButton);
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendButton.addActionListener(e -> sendCurrentMessage());

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    private void toggleMembersPanel() {
        int targetWidth = membersVisible ? 0 : MEMBERS_PANEL_WIDTH;
        int currentWidth = membersContainer.getPreferredSize().width;

        if (membersVisible) {
            Izq(membersContainer, 2, 4, targetWidth);
            toggleMembersButton.setText("Ver miembros");
        } else {
            Der(membersContainer, 2, 4, targetWidth);
            toggleMembersButton.setText("Ocultar miembros");
        }
        membersVisible = !membersVisible;
    }

    public static void Izq(JComponent componente, int milisegundos, int saltos, int parar) {
        new Thread() {
            @Override
            public void run() {
                for (int i = componente.getWidth(); i >= parar; i -= saltos) {
                    try {
                        Thread.sleep(milisegundos);
                        final int anchoActual = i;
                        SwingUtilities.invokeLater(() -> {
                            componente.setPreferredSize(new Dimension(anchoActual, componente.getHeight()));
                            componente.revalidate();
                            componente.repaint();
                        });
                    } catch (InterruptedException e) {
                        System.out.println("Error Thread Interrumpido (Izq): " + e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }.start();
    }

    public static void Der(JComponent componente, int milisegundos, int saltos, int parar) {
        new Thread() {
            @Override
            public void run() {
                for (int i = componente.getWidth(); i <= parar; i += saltos) {
                    try {
                        Thread.sleep(milisegundos);
                        final int anchoActual = i;
                        SwingUtilities.invokeLater(() -> {
                            componente.setPreferredSize(new Dimension(anchoActual, componente.getHeight()));
                            componente.revalidate();
                            componente.repaint();
                        });
                    } catch (InterruptedException e) {
                        System.out.println("Error Thread Interrumpido (Der): " + e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }.start();
    }

    private void stylePrimaryButton(JButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBackground(new Color(20, 110, 230));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void sendCurrentMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        // Enviar localmente
        addMessage(text, "Yo", true);
        messageInput.setText("");
        messageInput.requestFocus();
        // Notificar al listener para que lo envíe al servidor
        if (onSendGroupMessageListener != null) {
            onSendGroupMessageListener.onSendGroupMessage(text);
        }
    }

    public void addMessage(String text, String sender, boolean own) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(own ? new Color(19, 100, 231) : new Color(200, 200, 200)),
                new EmptyBorder(10, 12, 10, 12)));
        bubble.setBackground(own ? new Color(19, 100, 231) : new Color(245, 245, 245));
        bubble.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        JLabel senderLabel = new JLabel(sender);
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        senderLabel.setForeground(own ? Color.WHITE : new Color(55, 55, 55));

        JLabel textLabel = new JLabel(formatText(text));
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(own ? Color.WHITE : Color.BLACK);
        textLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel("Ahora");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(own ? new Color(220, 230, 255) : new Color(120, 120, 120));
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bubble.add(senderLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 6)));
        bubble.add(textLabel);
        bubble.add(Box.createRigidArea(new Dimension(0, 8)));
        bubble.add(timeLabel);

        if (own) {
            wrapper.add(bubble, BorderLayout.EAST);
        } else {
            wrapper.add(bubble, BorderLayout.WEST);
        }
        wrapper.setBorder(new EmptyBorder(4, 4, 4, 4));
        messagesContainer.add(wrapper);
        messagesContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        messagesContainer.revalidate();
        messagesScrollPane.getVerticalScrollBar().setValue(messagesScrollPane.getVerticalScrollBar().getMaximum());
    }

    private String formatText(String text) {
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }

    public void setGroupInfo(String name, int memberCount) {
        groupNameLabel.setText(name);
        groupSubtitleLabel.setText("Chat de grupo · " + memberCount + " miembros");
    }

    public void setGroupMembers(List<String> members) {
        membersContainer.removeAll();
        JLabel title = new JLabel("Miembros del grupo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        membersContainer.add(title);
        membersContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        for (String member : members) {
            membersContainer.add(createMemberCard(member));
            membersContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        membersContainer.add(Box.createVerticalGlue());
        membersContainer.revalidate();
        membersContainer.repaint();
    }

    private JPanel createMemberCard(String name) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setOpaque(true);
        card.setBackground(new Color(245, 247, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 10, 10, 10)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel avatar = new JLabel(name.substring(0, 1).toUpperCase());
        avatar.setOpaque(true);
        avatar.setBackground(new Color(20, 110, 230));
        avatar.setForeground(Color.WHITE);
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel statusLabel = new JLabel("En línea");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(40, 167, 69));
        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        textPanel.add(statusLabel);

        card.add(avatar, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chat grupal");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(920, 640);
            frame.setLocationRelativeTo(null);
            frame.add(new PanelGrupos());
            frame.setVisible(true);
        });
    }
}
