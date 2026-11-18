package za.ac.ctu.campusrent.app;
import za.ac.ctu.campusrent.gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import za.ac.ctu.campusrent.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

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

            try (Connection connection = DBConnection.getConnection()) {

                System.out.println("================================");
                System.out.println("CampusRent Database Connected!");
                System.out.println("Database: " + connection.getCatalog());
                System.out.println("================================");

            } catch (SQLException | IllegalStateException e) {

                System.err.println("Database connection failed!");
                System.err.println(e.getMessage());

            }
        });
    }
}
