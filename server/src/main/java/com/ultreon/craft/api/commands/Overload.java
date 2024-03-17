package com.ultreon.craft.api.commands;

import com.ultreon.craft.server.UltracraftServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Overload {
    private final List<CommandParameter> params;
    private final CommandSpec spec;
    private final String perm;
    private List<Object> objectCache = new ArrayList<>();

    public Overload(List<CommandParameter> params, CommandSpec spec, String perm) {
        this.params = params;
        this.spec = spec;
        this.perm = perm;
    }

    public boolean hasPermission(CommandSender sender) {
        return this.perm == null || sender.hasPermission(this.perm);
    }

    public boolean matches(ArrayList<CommandParameter> parameters) {
        return this.params.equals(parameters);
    }

    /**
     * Checks if the command is valid for the given sender and command context.
     *
     * @param sender the command sender
     * @param commandCtx the command context
     * @param args the command arguments
     * @return true if the command is valid, false otherwise
     * @throws CommandParseException if an error occurs while parsing the command
     */
    public boolean validFor(CommandSender sender, CommandContext commandCtx, String[] args) throws CommandParseException {
        // Flag to track the validity of the command
        AtomicBoolean flag = new AtomicBoolean(true);

        // Check if the command name matches the specified command context
        if (!this.spec.commandName().equals(commandCtx.name())) {
            return false;
        } else if (args.length == 0 && this.params.isEmpty()) {
            return true;
        } else if (this.params.isEmpty()) {
            return false;
        } else if (args.length == 0) {
            return false;
        }

        // Create a command reader context
        CommandReader context = new CommandReader(sender, commandCtx.name(), args);
        // List to store parsed command parameters
        ArrayList<Object> local = new ArrayList<>();
        // Atomic reference to store any command parse exception
        AtomicReference<CommandParseException> exception = new AtomicReference<>(null);

        // Loop through all command parameters
        for(CommandParameter param : this.params) {
            text: if (param instanceof CommandParameter.Text textParam) {
                String[] text = textParam.getText();
                List<String> strings = Arrays.asList(text.clone());
                String arg;

                try {
                    arg = context.readString();
                } catch (CommandParseException e) {
                    if (e instanceof CommandParseException.EndOfCommand || e instanceof CommandParseException.NotAtStartOfArg) {
                        return false;
                    } else {
                        exception.set(e);
                    }
                    break text;
                }

                if (!strings.contains(arg)) {
                    return false;
                }

                flag.set(flag.get() && strings.contains(arg));
            } else if (param instanceof CommandParameter.ArgumentType t) {
                try {
                    local.add(t.getParser().parse(context));
                } catch (CommandParseException e) {
                    UltracraftServer.LOGGER.error("Failed to parse command: ", e);
                    if (e instanceof CommandParseException.EndOfCommand || e instanceof CommandParseException.NotAtStartOfArg) {
                        return false;
                    } else {
                        exception.set(e);
                    }
                }
            }
        }

        // Calculate the final result based on the flag and context state
        boolean flagResult = flag.get();
        boolean result = flagResult && context.isAtEndOfCmd();

        // Get the command parse exception if any
        CommandParseException commandParseException = exception.get();
        if (commandParseException != null) {
            throw commandParseException;
        }
        // Cache the parsed command parameters if the command is valid
        if (result) {
            this.objectCache = local;
        }

        return result;
    }

    /**
     * Tab complete the command based on the provided arguments and parameters.
     *
     * @param sender the command sender
     * @param commandCtx the command context
     * @param args the command arguments
     * @param output the list of tab completions
     * @return the list of tab completions
     */
    public List<String> tabComplete(CommandSender sender, CommandContext commandCtx, String[] args,
                                    List<String> output) {
        if (!this.hasPermission(sender)) {
            return null;
        }
        CommandReader context = new CommandReader(sender, commandCtx.name(), args);

        // Loop through all command parameters for tab completion.
        for (CommandParameter param : this.params) {
            AtomicBoolean cancel = new AtomicBoolean(false);
            AtomicBoolean no = new AtomicBoolean(false);
            param.ifText(text -> handleText(output, text, context, no, cancel))
                    .ifArgType(t -> handleArgType(sender, commandCtx, args, output, t, context, cancel));

            // Check if the command is cancelled
            if (no.get()) {
                return null;
            }

            if (cancel.get()) {
                return output;
            }
        }
        return output;
    }

    private static void handleArgType(CommandSender sender, CommandContext commandCtx, String[] args, List<String> output, CommandParameter.ArgumentType t, CommandReader context, AtomicBoolean cancel) {
        List<String> res;
        try {
            // Read the next argument
            String[] completionArgs = Arrays.copyOfRange(args, context.getCurrent(), args.length);
            res = t.getCompleter().tabComplete(sender, commandCtx, context, completionArgs);
        } catch (CommandParseException e) {
            // If an error occurs, clear the output
            output.clear();
            cancel.set(true);

            // Log the error
            UltracraftServer.LOGGER.error("Failed to parse command: ", e);
            return;
        }

        // Check if the argument is at the end of the command
        if (context.isAtEndOfCmd()) {
            output.clear();
            output.addAll(res);
            cancel.set(true);
        }
    }

    private static void handleText(List<String> output, String[] text, CommandReader context, AtomicBoolean no, AtomicBoolean cancel) {
        try {
            // Read the next argument
            String readied = context.readString();
            List<String> strings = TabCompleting.strings(new ArrayList<>(), readied, text);

            // Check if the argument is at the end of the command
            if (!context.isAtEndOfCmd()) {
                if (!Arrays.asList(text).contains(readied)) {
                    no.set(true);
                }
            } else {
                // Add the tab completions
                output.addAll(strings);
                cancel.set(true);
            }
        } catch (CommandParseException e) {
            UltracraftServer.LOGGER.error("Failed to parse command: ", e);
            cancel.set(true);
        }
    }

    public List<CommandParameter> args() {
        return this.params;
    }

    public CommandSpec spec() {
        return this.spec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Overload overload = (Overload) o;
        return this.params.equals(overload.params) && this.spec.equals(overload.spec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.params, this.spec);
    }

    @Override
    public String toString() {
        return "Overload[" +
               "args=" + this.params + ", " +
               "spec=" + this.spec + ']';
    }

    public List<Object> getObjectCache() {
        return this.objectCache;
    }
}