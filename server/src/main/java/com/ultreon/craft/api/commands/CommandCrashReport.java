package com.ultreon.craft.api.commands;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.ConsoleCommandSender;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

/**
 * Represents a command crash report.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class CommandCrashReport {
    private final Throwable throwable;
    private final String alias;
    private final String[] args;
    private final UUID uniqueId;

    /**
     * Creates a new command crash report.
     *
     * @param throwable The exception that caused the crash
     * @param alias     The alias of the command
     * @param args      The arguments of the command
     */
    public CommandCrashReport(Throwable throwable, String alias, String[] args) {
        this.throwable = throwable;
        this.alias = alias;
        this.args = args;
        this.uniqueId = UUID.randomUUID();
    }

    /**
     * Saves the details of a command error to a file and returns the details.
     *
     * @param sender The command sender
     * @return The details of the command error
     */
    public Details save(CommandSender sender) {
        // Create the directory for command crashes if it does not exist
        File commandCrashes = new File("servercorex-data/command-crashes/");
        if (!commandCrashes.exists()) {
            commandCrashes.mkdirs();
        }

        // Generate a unique ID for the error file
        BigInteger bigInteger = new BigInteger("1");
        byte[] bytes = bigInteger.toByteArray();
        String id = Base64.getEncoder().encodeToString(bytes).replace("\\+", " ").replace("/", "-");
        id = id.substring(0, id.length() - 2);
        File file = new File(commandCrashes, "crash_" + id + ".txt");
        while (file.exists()) {
            bytes = bigInteger.toByteArray();
            id = Base64.getEncoder().encodeToString(bytes).replace("\\+", " ").replace("/", "-");
            id = id.substring(0, id.length() - 2);
            file = new File(commandCrashes, "crash_" + id + ".txt");
            bigInteger = bigInteger.add(BigInteger.ONE);
        }

        // Write the details of the command error to a string
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        this.throwable.printStackTrace(printWriter);
        StringBuilder sb = new StringBuilder();

        // Appending the details
        sb.append("-========- COMMAND ERROR -========-\n")
                .append(writer)
                .append("Details:\n")
                .append("  Command: ")
                .append("/").append(this.alias).append(" ")
                .append(StringUtils.join(Arrays.asList(this.args), " ")).append("\n");
        if (sender instanceof Player) {
            sb.append("  Player:  ").append(sender.getName()).append("\n");
        }
        sb.append("  Sender:  ");
        if (sender instanceof ConsoleCommandSender) {
            sb.append("Console\n");
        } else if (sender instanceof Player) {
            sb.append("Player\n");
        } else if (sender instanceof Entity) {
            sb.append("Entity\n");
        } else if (sender != null) {
            sb.append("Unknown\n");
        } else {
            sb.append("None\n");
        }
        sb.append("  UUID:    ").append(this.uniqueId).append("\n")
                .append("  ID:      ").append(id);

        // Write the details to the error file
        FileWriter writer1 = null;
        try {
            file.createNewFile();
            writer1 = new FileWriter(file);
            writer1.write(sb.toString());
            writer1.close();
        } catch (IOException e) {
            if (writer1 != null) {
                try {
                    writer1.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            } else {
                e.printStackTrace();
            }
        }
        return new Details(sb.toString(), id);
    }

    /**
     * Represents the details of a command error.
     *
     * @param report The details of the command error
     * @param id     The unique ID of the error
     */
    public record Details(String report, String id) {
    }
}