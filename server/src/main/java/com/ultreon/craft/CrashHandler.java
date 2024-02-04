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

public class CrashHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Crash-Handler");
    private static final List<Consumer<CrashLog>> HANDLERS = new ArrayList<>();

    public static void addHandler(Consumer<CrashLog> handler) {
        CrashHandler.HANDLERS.add(handler);
    }

    public static void handleCrash(CrashLog crashLog) {
        File file = new File("crash-reports", crashLog.getDefaultFileName());
        crashLog.writeToFile(file);

        try {
            CrashHandler.runCrashHandlers(crashLog);
        } catch (Throwable t) {
            CrashHandler.LOGGER.error("Failed to run crash handlers:", t);
        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                TaskDialog dlg = new TaskDialog(null, "Game crashed!");

                String title = "Game crashed!";
                String description = "See crash report below:";
                boolean noMessage = Strings.isEmpty(title);

                dlg.setInstruction(noMessage ? description : title);
                dlg.setText(noMessage ? "" : description);

                dlg.setIcon(UIManager.getIcon(TaskDialog.StandardIcon.ERROR));
                dlg.setCommands(TaskDialog.StandardCommand.CANCEL.derive(TaskDialog.makeKey("Close")));

                JTextArea text = new JTextArea();
                text.setEditable(false);
                text.setFont(new Font("Monospaced", Font.PLAIN, 11));
                text.setText(crashLog.toString());
                text.setCaretPosition(0);

                JScrollPane scroller = new JScrollPane(text);
                scroller.setPreferredSize(new Dimension(400, 200));
                dlg.getDetails().setExpandableComponent(scroller);
                dlg.getDetails().setExpanded(noMessage);

                dlg.setResizable(true);
                dlg.setVisible(true);

            });
        } catch (Throwable ignored) {

        }
        Runtime.getRuntime().halt(1);
    }

    private static void runCrashHandlers(CrashLog crashLog) {
        for (var handler : CrashHandler.HANDLERS) {
            try {
                handler.accept(crashLog);
            } catch (Throwable t) {
                CrashHandler.LOGGER.error("Crash handler failed:", t);
            }
        }
    }
}
