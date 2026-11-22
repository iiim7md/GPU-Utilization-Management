
package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.*;

public class ClockSetupDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JSpinner dateSpinner;
    private final JSpinner timeSpinner;

    private LocalDateTime selectedDateTime;

    public ClockSetupDialog(JFrame parent) {
        super(parent, "Set Simulation Clock", true);

        setSize(420, 230);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        dateSpinner = new JSpinner(
                new SpinnerDateModel());

        dateSpinner.setEditor(
                new JSpinner.DateEditor(
                        dateSpinner, "dd/MM/yyyy"));

        timeSpinner = new JSpinner(
                new SpinnerDateModel());

        timeSpinner.setEditor(
                new JSpinner.DateEditor(
                        timeSpinner, "HH:mm"));

        JPanel fields = new JPanel(
                new GridLayout(2, 2, 10, 10));

        fields.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20));

        fields.add(new JLabel("Starting date:"));
        fields.add(dateSpinner);

        fields.add(new JLabel("Starting time:"));
        fields.add(timeSpinner);

        JButton startButton = new JButton("Start Clock");

        startButton.addActionListener(e -> {
            Date date = (Date) dateSpinner.getValue();
            Date time = (Date) timeSpinner.getValue();

            LocalDateTime selectedDate =
                    LocalDateTime.ofInstant(
                            date.toInstant(),
                            ZoneId.systemDefault());

            LocalDateTime selectedTime =
                    LocalDateTime.ofInstant(
                            time.toInstant(),
                            ZoneId.systemDefault());

            selectedDateTime = LocalDateTime.of(
                    selectedDate.toLocalDate(),
                    selectedTime.toLocalTime()
                            .withSecond(0)
                            .withNano(0));

            dispose();
        });

        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.add(cancelButton);
        buttons.add(startButton);

        add(fields, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public LocalDateTime getSelectedDateTime() {
        return selectedDateTime;
    }
}
