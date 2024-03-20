package com.ultreon.craft.server.dedicated.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ServerLogGui extends JScrollPane {
    public ServerLogGui() {
        super();

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));

        JTextArea logArea = new JTextArea();
        this.setViewportView(logArea);
        logArea.setEditable(false);
        logArea.setOpaque(false);
        logArea.setBackground(new Color(0, 0, 0, 0));
        logArea.setCaretPosition(0);
        logArea.setMargin(new Insets(0, 0, 0, 0));
        logArea.setLineWrap(false);
        logArea.setWrapStyleWord(false);

        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        Thread thread = createThread(logArea);
        thread.start();
    }

    @NotNull
    private static Thread createThread(JTextArea logArea) {
        Thread thread = new Thread(() -> {
            StringBuilder log = new StringBuilder();
            while (true) {
                try (Scanner scanner = new Scanner(new File("logs/latest.log"))) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        log.append(line).append("\n");
                    }
                } catch (FileNotFoundException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                logArea.setText(log.toString());
                log = new StringBuilder();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        thread.setDaemon(true);
        return thread;
    }

    private void close() {

    }

    public void clear() {
        JTextArea logArea = (JTextArea) this.getViewport().getView();
        logArea.setText("");
    }

    public void scrollToBottom() {
        this.getVerticalScrollBar().setValue(this.getVerticalScrollBar().getMaximum());
    }

    public void scrollToTop() {
        this.getVerticalScrollBar().setValue(0);
    }
}
