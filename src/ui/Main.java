
package ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("GPU Utilization Management");

            frame.setSize(650, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            JLabel label = new JLabel(
                "GPU Utilization Management - COSC230",
                SwingConstants.CENTER
            );

            frame.add(label);
            frame.setVisible(true);
        });
    }
}
