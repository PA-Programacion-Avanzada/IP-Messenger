package Interfaces;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SalirGrupoDialog extends JDialog {

    public SalirGrupoDialog(JFrame parent, String grupo) {
        super(parent, "Salir del grupo", true);
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setUndecorated(false); // Mostrar barra de título con X
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        // Icono de advertencia (triángulo naranja)
        JLabel iconLabel = new JLabel("⚠"); // Emoji de advertencia
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(255, 165, 0)); // Naranja
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Salir del grupo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("<html><div style='text-align:center;'>¿Estás seguro de que quieres salir del grupo '" + grupo + "'?</div></html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        message.setForeground(Color.DARK_GRAY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botones
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttons.setBackground(Color.WHITE);
        JButton cancelar = new JButton("Cancelar");
        cancelar.setBackground(Color.LIGHT_GRAY);
        cancelar.setForeground(Color.BLACK);
        cancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelar.setFocusPainted(false);
        cancelar.addActionListener(e -> dispose());

        JButton salir = new JButton("Salir");
        salir.setBackground(new Color(244, 67, 54)); // Rojo intenso
        salir.setForeground(Color.WHITE);
        salir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        salir.setFocusPainted(false);
        salir.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(this, "Has salido del grupo " + grupo);
            dispose();
        });

        buttons.add(cancelar);
        buttons.add(salir);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(message);
        panel.add(Box.createVerticalStrut(25));
        panel.add(buttons);

        add(panel, BorderLayout.CENTER);
    }
}   