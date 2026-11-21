package za.ac.ctu.campusrent.gui;

import javax.swing.JButton;
import java.awt.event.ActionListener;

public class InnerClassDemonstration {

    private String screenName = "CampusRent";

    // Member inner class.
    public class ScreenStatus {
        public String describe() {
            return screenName + " screen is ready.";
        }
    }

    // Static nested class.
    public static class WindowConfiguration {
        private final int width;
        private final int height;

        public WindowConfiguration(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    // Local class.
    public Runnable createLocalTask() {
        class LocalRefreshTask implements Runnable {
            @Override
            public void run() {
                System.out.println(screenName + " refreshed.");
            }
        }

        return new LocalRefreshTask();
    }

    // Anonymous class listener.
    public JButton createAnonymousListenerButton() {
        JButton button = new JButton("Refresh");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(
                    java.awt.event.ActionEvent event) {
                System.out.println(
                        "Refresh clicked using anonymous class.");
            }
        });

        return button;
    }

    // Equivalent lambda listener.
    public JButton createLambdaListenerButton() {
        JButton button = new JButton("Refresh");

        button.addActionListener(event ->
                System.out.println("Refresh clicked using lambda."));

        return button;
    }

    public static void main(String[] args) {
        InnerClassDemonstration demo =
                new InnerClassDemonstration();

        System.out.println(
                demo.new ScreenStatus().describe());

        demo.createLocalTask().run();

        WindowConfiguration config =
                new WindowConfiguration(1200, 800);

        System.out.println(
                "Window: " + config.getWidth()
                        + "x" + config.getHeight());
    }
}