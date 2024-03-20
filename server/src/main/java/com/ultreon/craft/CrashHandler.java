package com.ultreon.craft;

import com.ultreon.craft.crash.CrashLog;
import org.oxbow.swingbits.dialog.task.TaskDialog;
import org.oxbow.swingbits.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@UnsafeApi
public class CrashHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Crash-Handler");
    private static final List<Consumer<CrashLog>> HANDLERS = new ArrayList<>();

    /**
     * Add a handler to the list of crash log handlers.
     *
     * @param handler the consumer function to handle the CrashLog
     */
    public static void addHandler(Consumer<CrashLog> handler) {
        // Add the handler to the list of crash log handlers
        CrashHandler.HANDLERS.add(handler);
    }

    /**
     * Handles a crash by logging it, displaying a dialog with crash details, and halting the runtime.
     *
     * @param crashLog The CrashLog object containing crash information.
     */
    public static void handleCrash(CrashLog crashLog) {
        // Save crash log to a file
        File crashLogFile = new File("crash-reports", CrashLog.getFileName());
        crashLog.writeToFile(crashLogFile);

        try {
            // Run crash handlers
            CrashHandler.processCrashHandlers(crashLog);
        } catch (Throwable t) {
            // Log error if crash handlers fail
            CrashHandler.LOGGER.error("Failed to run crash handlers:", t);
        }

        // Display crash dialog
        displayCrashDialog(crashLog);

        // Halt the runtime
        Runtime.getRuntime().halt(1);
    }

    /**
     * Displays a dialog with crash information.
     *
     * @param crashLog The CrashLog object containing crash details
     */
    private static void displayCrashDialog(CrashLog crashLog) {
        // Display the crash dialog on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            // Create a new TaskDialog
            TaskDialog dialog = new TaskDialog(null, "Game crashed!");

            // Set up title and description for the dialog
            String title = "Game crashed!";
            String description = "See crash report below:";

            // Check if the title is empty
            boolean noMessage = Strings.isEmpty(title);

            // Set the instruction and text of the dialog based on the title and description
            dialog.setInstruction(noMessage ? description : title);
            dialog.setText(noMessage ? "" : description);

            // Set the error icon for the dialog
            dialog.setIcon(UIManager.getIcon(TaskDialog.StandardIcon.ERROR));

            // Set the commands for the dialog
            dialog.setCommands(TaskDialog.StandardCommand.CANCEL.derive(TaskDialog.makeKey("Close")));

            // Create a JTextArea to display the crash log
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            textArea.setText(crashLog.toString());
            textArea.setCaretPosition(0);

            // Create a JScrollPane to allow scrolling in the text area
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 200));

            // Set the expandable component and expanded state in the details of the dialog
            dialog.getDetails().setExpandableComponent(scrollPane);
            dialog.getDetails().setExpanded(noMessage);

            // Allow the dialog to be resizable and make it visible
            dialog.setResizable(true);
            dialog.setVisible(true);
        });
    }

    /**
     * Processes the crash handlers by iterating over each handler and invoking it with the given crash log.
     * If any handler throws an exception, logs the error.
     *
     * @param crashLog The crash log to be processed by the handlers.
     */
    private static void processCrashHandlers(CrashLog crashLog) {
        for (var handler : CrashHandler.HANDLERS) {
            try {
                handler.accept(crashLog);
            } catch (Throwable e) {
                CrashHandler.LOGGER.error("Error in crash handler:", e);
            }
        }
    }
}
