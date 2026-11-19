package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.service.PreferencesService;
import javax.swing.AbstractButton;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ThemeManager {

    private static final Color DARK_BACKGROUND =
            new Color(21, 26, 35);

    private static final Color DARK_SURFACE =
            new Color(32, 41, 56);

    private static final Color DARK_INPUT =
            new Color(17, 24, 39);

    private static final Color DARK_TEXT =
            new Color(229, 231, 235);

    private static final Color DARK_MUTED =
            new Color(170, 180, 195);

    private static final Color DARK_BORDER =
            new Color(65, 75, 90);

    private static final Color DARK_ACCENT =
            new Color(55, 65, 81);

    private static final Map<Component, OriginalStyle> ORIGINAL_STYLES =
            new IdentityHashMap<>();

    private static String currentTheme = "LIGHT";

    private ThemeManager() {
    }

    private static class OriginalStyle {

        private final Color background;
        private final Color foreground;

        private OriginalStyle(Component component) {
            background = component.getBackground();
            foreground = component.getForeground();
        }
    }

    public static void loadSavedTheme() {

        try {
            String savedTheme =
                    new PreferencesService().load().getTheme();

            setTheme(savedTheme);

        } catch (Exception ex) {
            setTheme("LIGHT");
        }
    }

    public static void setTheme(String theme) {

        if (theme == null) {
            currentTheme = "LIGHT";
            return;
        }

        currentTheme = theme.trim().equalsIgnoreCase("DARK")
                ? "DARK"
                : "LIGHT";
    }

    public static String getTheme() {
        return currentTheme;
    }

    public static boolean isDarkMode() {
        return "DARK".equals(currentTheme);
    }

    public static void applyToAllWindows() {

        for (Window window : Window.getWindows()) {

            if (window.isDisplayable()) {
                apply(window);
            }
        }
    }

    public static void apply(Component component) {

        if (component == null) {
            return;
        }

        applyComponent(component, true);
        component.revalidate();
        component.repaint();
    }

    private static void applyComponent(
            Component component,
            boolean root) {

        ORIGINAL_STYLES.computeIfAbsent(
                component,
                OriginalStyle::new
        );

        OriginalStyle original = ORIGINAL_STYLES.get(component);

        if (!isDarkMode()) {

            if (original.background != null) {
                component.setBackground(original.background);
            }

            if (original.foreground != null) {
                component.setForeground(original.foreground);
            }

        } else {

            applyDarkStyle(component, original, root);
        }

        if (component instanceof JTable table) {

            if (isDarkMode()) {

                table.setBackground(DARK_INPUT);
                table.setForeground(DARK_TEXT);
                table.setGridColor(DARK_BORDER);
                table.setSelectionBackground(
                        new Color(55, 85, 125)
                );
                table.setSelectionForeground(Color.WHITE);

                JTableHeaderStyle.apply(table);

            }
        }

        if (component instanceof JTextComponent text) {

            if (isDarkMode()) {

                text.setBackground(DARK_INPUT);
                text.setForeground(DARK_TEXT);
                text.setCaretColor(Color.WHITE);
                text.setSelectionColor(
                        new Color(55, 85, 125)
                );
                text.setSelectedTextColor(Color.WHITE);
            }
        }

        if (component instanceof JList<?> list) {

            if (isDarkMode()) {
                list.setBackground(DARK_INPUT);
                list.setForeground(DARK_TEXT);
                list.setSelectionBackground(
                        new Color(55, 85, 125)
                );
                list.setSelectionForeground(Color.WHITE);
            }
        }

        if (component instanceof JTabbedPane tabs) {

            if (isDarkMode()) {
                tabs.setBackground(DARK_SURFACE);
                tabs.setForeground(DARK_TEXT);
            }
        }

        if (component instanceof JScrollPane scrollPane) {

            if (isDarkMode()) {
                scrollPane.setBackground(DARK_SURFACE);

                if (scrollPane.getViewport() != null) {
                    scrollPane.getViewport().setBackground(
                            DARK_INPUT
                    );
                }
            }
        }

        if (component instanceof Container container) {

            for (Component child : container.getComponents()) {
                applyComponent(child, false);
            }
        }
    }

    private static void applyDarkStyle(
            Component component,
            OriginalStyle original,
            boolean root) {

        Color originalBackground = original.background;
        Color originalForeground = original.foreground;

        if (component instanceof JPanel
                || component instanceof JRootPane
                || component instanceof Window) {

            if (root) {
                component.setBackground(DARK_BACKGROUND);

            } else if (isLightNeutral(originalBackground)) {
                component.setBackground(DARK_SURFACE);
            }

        } else if (component instanceof AbstractButton button) {
            button.setBackground(new Color(55, 65, 81));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setContentAreaFilled(true);
            button.setBorderPainted(true);

        } else if (component instanceof JComboBox<?>) {
            component.setBackground(DARK_ACCENT);
            component.setForeground(DARK_TEXT);

        } else if (component instanceof JTableHeader) {
            component.setBackground(DARK_SURFACE);
            component.setForeground(DARK_TEXT);

        } else if (component instanceof JTable) {

            component.setBackground(DARK_INPUT);

        } else if (component instanceof JLabel
                || component instanceof AbstractButton) {

            // Foreground is handled below.
        }

        if (originalForeground != null
                && isDarkNeutral(originalForeground)) {

            component.setForeground(DARK_TEXT);
        }

        if (component instanceof JLabel label
                && originalForeground != null
                && originalForeground.equals(Color.GRAY)) {

            label.setForeground(DARK_MUTED);
        }

        if (component instanceof JTableHeader) {
            component.setBackground(DARK_SURFACE);
            component.setForeground(DARK_TEXT);
        }

        if (component instanceof JSeparator) {
            component.setForeground(DARK_BORDER);
            component.setBackground(DARK_BORDER);
        }
    }

    private static boolean isLightNeutral(Color color) {

        if (color == null) {
            return false;
        }

        int max = Math.max(
                color.getRed(),
                Math.max(color.getGreen(), color.getBlue())
        );

        int min = Math.min(
                color.getRed(),
                Math.min(color.getGreen(), color.getBlue())
        );

        int brightness = (
                color.getRed()
                        + color.getGreen()
                        + color.getBlue()
        ) / 3;

        return max - min < 40 && brightness > 170;
    }

    private static boolean isDarkNeutral(Color color) {

        if (color == null) {
            return false;
        }

        int max = Math.max(
                color.getRed(),
                Math.max(color.getGreen(), color.getBlue())
        );

        int min = Math.min(
                color.getRed(),
                Math.min(color.getGreen(), color.getBlue())
        );

        int brightness = (
                color.getRed()
                        + color.getGreen()
                        + color.getBlue()
        ) / 3;

        return max - min < 45 && brightness < 180;
    }

    private static class JTableHeaderStyle {

        private static void apply(JTable table) {

            if (table.getTableHeader() != null) {

                table.getTableHeader().setBackground(
                        DARK_SURFACE
                );

                table.getTableHeader().setForeground(
                        DARK_TEXT
                );

                table.getTableHeader().setOpaque(true);
            }
        }
    }
}
