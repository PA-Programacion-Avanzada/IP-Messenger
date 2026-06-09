package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import ui.PendingMessagesModal;

public class TemporalMessagesModal extends JDialog {

    /* ---------------------------------------------------------------- *
     *  Los componentes de la UI
     * ---------------------------------------------------------------- */
    private JList<PendingMessagesModal.PendingMessage> messageList;
    private DefaultListModel<PendingMessagesModal.PendingMessage> listModel;
    private JButton closeButton;
    private JLabel infoLabel;

    /** Constructor */
    public TemporalMessagesModal(Frame owner) {
        super(owner, "Mensajes temporales", true);
        initUI();
        setSize(500, 450);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /* ---------------------------------------------------------------- *
     *  Construcción de la interfaz
     * ---------------------------------------------------------------- */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                mainPanel.getBorder()));

        // Header (título + botón cerrar)
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        // Center (lista de mensajes)
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        // Footer (botón cerrar)
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel titleLabel = new JLabel("Mensajes temporales");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(titleLabel, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        header.add(closeBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        infoLabel = new JLabel(
                "<html>Estos son los mensajes temporales que has recibido (1‑a‑1, no persisten)."
                + "<br/>Selecciona uno para ver su contenido completo.</html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(100, 100, 100));
        center.add(infoLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setCellRenderer(new PendingMessageRenderer());
        messageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        messageList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JScrollPane scroll = new JScrollPane(messageList);
        scroll.setBorder(null);
        center.add(scroll, BorderLayout.CENTER);

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
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            if (onCloseListener != null) onCloseListener.onClose();
            dispose();
        });

        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    /* ---------------------------------------------------------------- *
     *  API pública del modal
     * ---------------------------------------------------------------- */
    /** Reemplaza la lista completa de mensajes temporales. */
    public void setPendingMessages(List<PendingMessagesModal.PendingMessage> messages) {
        listModel.clear();
        for (PendingMessagesModal.PendingMessage m : messages) {
            listModel.addElement(m);
        }
    }

    /** Listener opcional que se ejecuta al cerrar el diálogo. */
    public interface OnCloseListener {
        void onClose();
    }

    private OnCloseListener onCloseListener;

    public void setOnCloseListener(OnCloseListener listener) {
        this.onCloseListener = listener;
    }

    /* ---------------------------------------------------------------- *
     *  Renderizado de cada fila de la JList
     * ---------------------------------------------------------------- */
    private static class PendingMessageRenderer extends JPanel
            implements ListCellRenderer<PendingMessagesModal.PendingMessage> {

        private final JLabel senderLabel = new JLabel();
        private final JLabel contentLabel = new JLabel();
        private final JLabel timeLabel = new JLabel();

        PendingMessageRenderer() {
            setLayout(new BorderLayout(8, 4));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            setBackground(Color.WHITE);

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(Color.GRAY);
            top.add(senderLabel, BorderLayout.WEST);
            top.add(timeLabel, BorderLayout.EAST);

            contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            contentLabel.setForeground(new Color(60, 60, 60));

            add(top, BorderLayout.NORTH);
            add(contentLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends PendingMessagesModal.PendingMessage> list,
                PendingMessagesModal.PendingMessage value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            senderLabel.setText(value.getSenderName());
            timeLabel.setText(value.getTimestamp());
            contentLabel.setText("<html>" + value.getContent() + "</html>");

            if (isSelected) {
                setBackground(new Color(230, 242, 255));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    /* ---------------------------------------------------------------- *
     *  Borde con sombra (re‑usado en otros modales)
     * ---------------------------------------------------------------- */
    private static class ShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            Color shadow = new Color(0, 0, 0, 30);
            for (int i = 1; i <= 4; i++) {
                g2d.setColor(shadow);
                g2d.drawRoundRect(x + i, y + i,
                        width - 1 - 2 * i, height - 1 - 2 * i,
                        12, 12);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }

    /* ---------------------------------------------------------------- *
     *  Punto de prueba rápida (main)
     * ---------------------------------------------------------------- */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemporalMessagesModal modal = new TemporalMessagesModal(null);
            List<PendingMessagesModal.PendingMessage> sample = new ArrayList<>();
            sample.add(new PendingMessagesModal.PendingMessage(
                    "Juan Pérez", "¿Quieres café?", "10:45", 1));
            sample.add(new PendingMessagesModal.PendingMessage(
                    "María García", "Te paso el archivo.", "11:02", 2));
            sample.add(new PendingMessagesModal.PendingMessage(
                    "Carlos López", "Nos vemos luego.", "09:15", 3));
            modal.setPendingMessages(sample);
            modal.setVisible(true);
        });
    }
}