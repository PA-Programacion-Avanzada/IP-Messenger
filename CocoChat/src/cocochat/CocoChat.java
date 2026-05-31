/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package cocochat;

import Interfaces.IPMessengerLogin;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author danie
 */
public class CocoChat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new IPMessengerLogin().setVisible(true);
        });
    }
    
}
