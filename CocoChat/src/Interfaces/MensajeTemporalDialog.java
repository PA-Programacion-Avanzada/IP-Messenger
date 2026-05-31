package Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MensajeTemporalDialog extends JDialog {

    public MensajeTemporalDialog(JFrame parent, String destinatario) {
        super(parent, "Mensaje Temporal", true);
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Mensaje a: " + destinatario + " (Temporal)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel("<html><div style='text-align:center;'>Este mensaje se borrará cuando el remitente o el destinatario se desconecten<br>y no se guardará en la base de datos.</div></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(Color.DARK_GRAY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea msgArea = new JTextArea(5, 30);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBorder(null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);
        JButton cancelar = new JButton("Cancelar");
        cancelar.setBackground(Color.LIGHT_GRAY);
        cancelar.setForeground(Color.BLACK);
        cancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelar.setFocusPainted(false);
        cancelar.addActionListener(e -> dispose());

        JButton enviar = new JButton("Enviar a " + destinatario.split(" ")[0]);
        enviar.setBackground(new Color(0, 120, 215));
        enviar.setForeground(Color.WHITE);
        enviar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        enviar.setFocusPainted(false);
        enviar.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(this, "Mensaje temporal enviado.");
            dispose();
        });

        buttons.add(cancelar);
        buttons.add(enviar);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(info);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(scroll);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttons);

        add(mainPanel, BorderLayout.CENTER);
    }
}