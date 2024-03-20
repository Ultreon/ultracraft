package com.ultreon.craft.api.commands;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.server.UltracraftServer;

import java.io.*;
import java.util.ArrayList;

/**
 * Represents an argument mismatch exception.
 * Used to indicate that an argument mismatch has occurred in a {@link Command}.
 * This generally means that the number of arguments or the types provided does not match the method signature.
 * <p>
 * Always check your arguments before making a command :wink:
 */
public class CommandArgumentMismatch extends IllegalArgumentException {
    private final Class<?>[] parameterTypes;
    private final ArrayList<Object> callArgs;
    private final IllegalArgumentException e;

    public CommandArgumentMismatch(Class<?>[] parameterTypes, ArrayList<Object> callArgs, IllegalArgumentException e) {
        super(e);
        this.parameterTypes = parameterTypes;
        this.callArgs = callArgs;
        this.e = e;
    }

    public String dumps() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Got illegal argument, possible argument mismatch.");
        pw.println("Dumping method argument types:");
        for (Class<?> type : this.parameterTypes) pw.println("  " + type.getName());

        pw.println("Dumping called argument types:");
        for (Object obj : this.callArgs) pw.println("  " + (obj != null ? obj.getClass().getName() : null));

        pw.println("Dumping stack trace:");
        for (StackTraceElement element : this.e.getStackTrace()) pw.println("  " + element);

        String string = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to dump command mismatch: ", e);
        }
        return string;
    }

    public void dump(Writer writer) {
        try {
            writer.write(this.dumps());
        } catch (IOException ioException) {
            CommonConstants.LOGGER.error("Failed to dump command mismatch: ", ioException);
        }
    }

    public void dump(OutputStream outputStream) {
        try {
            outputStream.write(this.dumps().getBytes());
        } catch (IOException ioException) {
            CommonConstants.LOGGER.error("Failed to dump command mismatch: ", ioException);
        }
    }

    public void dump(File file) {
        try (FileWriter fileWriter = new FileWriter(file, false)) {
            fileWriter.write(this.dumps());
        } catch (IOException ioException) {
            CommonConstants.LOGGER.error("Failed to dump command mismatch: ", ioException);
        }
    }
}