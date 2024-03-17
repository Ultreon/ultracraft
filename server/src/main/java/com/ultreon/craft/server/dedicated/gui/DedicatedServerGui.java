package com.ultreon.craft.server.dedicated.gui;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.ultreon.craft.server.UltracraftServer;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DedicatedServerGui extends JFrame {
    private JTextField commandField;

    public DedicatedServerGui() throws HeadlessException {
        super("Ultracraft Dedicated Server");

        this.setSize(new Dimension(1024, 600));

        if (!FlatMacDarkLaf.setup()) {
            JOptionPane.showMessageDialog(this, "Failed to initialize FlatLaf", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.add(new ServerLogGui(), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        contentPane.add(this.createCommandArea(), new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.setContentPane(contentPane);

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private Component createCommandArea() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(createCommandField());

        JButton button = new JButton("Send");
        button.addActionListener(this::runCommand);
        panel.add(button);
        return panel;
    }

    private JTextField createCommandField() {
        this.commandField = new JTextField();
        this.commandField.addActionListener(this::runCommand);
        return this.commandField;
    }

    private void runCommand(ActionEvent e) {
        String command = this.commandField.getText();
        if (!command.isEmpty()) {
            UltracraftServer server = UltracraftServer.get();
            if (server == null) return;
            server.getConsoleSender().execute(command);
            this.commandField.setText("");
        }
    }
}
