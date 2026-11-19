
package za.ac.ctu.campusrent.app;

import za.ac.ctu.campusrent.gui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {

        // Set the Look and Feel ONCE before creating any windows.
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );

        } catch (Exception ex) {
            System.err.println(
                    "Could not set application appearance: "
                            + ex.getMessage()
            );
        }

        // Start CampusRent.
        SwingUtilities.invokeLater(() -> {

            LoginFrame loginFrame = new LoginFrame();

            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }
}
