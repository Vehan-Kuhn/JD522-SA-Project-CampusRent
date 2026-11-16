
package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import java.awt.*;

/** Temporary screen used while other modules are developed. **/

public class PlaceholderPanel extends JPanel {

    public PlaceholderPanel(String title) {

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        JLabel heading = new JLabel(title + " - Coming Soon");
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        add(heading);
    }
}
