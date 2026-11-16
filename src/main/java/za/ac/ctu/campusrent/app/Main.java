package za.ac.ctu.campusrent.app;

import za.ac.ctu.campusrent.gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/** Main entry point for CampusRent. **/

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.err.println(
                        "Could not load system appearance: " + e.getMessage()
                );
            }

            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
