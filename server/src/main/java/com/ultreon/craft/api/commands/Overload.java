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

    public boolean validFor(CommandSender sender, CommandContext commandCtx, String[] args) throws CommandParseException {
        AtomicBoolean flag = new AtomicBoolean(true);

        if (!this.spec.commandName().equals(commandCtx.name())) {
            return false;
        } else if (args.length == 0 && this.params.isEmpty()) {
            return true;
        } else if (this.params.isEmpty()) {
            return false;
        } else if (args.length == 0) {
            return false;
        }

        CommandReader context = new CommandReader(sender, commandCtx.name(), args);
        ArrayList<Object> local = new ArrayList<>();
        AtomicReference<CommandParseException> exception = new AtomicReference<>(null);

        // Loop all arguments
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
                    if (e instanceof CommandParseException.EndOfCommand || e instanceof CommandParseException.NotAtStartOfArg) {
                        return false;
                    } else {
                        exception.set(e);
                    }
                }
            }
        }

        boolean flagResult = flag.get();
        boolean result = flagResult && context.isAtEndOfCmd();

        CommandParseException commandParseException = exception.get();
        if (commandParseException != null) {
            throw commandParseException;
        }
        if (result) {
            this.objectCache = local;
        }

        return result;
    }

    public List<String> tabComplete(CommandSender sender, CommandContext commandCtx, String[] args, List<String> output) {
        if (!this.hasPermission(sender)) {
            return null;
        }
        CommandReader context = new CommandReader(sender, commandCtx.name(), args);

        // Loop all arguments.
        for (CommandParameter param : this.params) {
            AtomicBoolean cancel = new AtomicBoolean(false);
            AtomicBoolean no = new AtomicBoolean(false);
            param.ifText(text -> {
                try {
                    String readied = context.readString();
                    List<String> strings = TabCompleting.strings(new ArrayList<>(), readied, text);
                    if (!context.isAtEndOfCmd()) {
                        if (!Arrays.asList(text).contains(readied)) {
                            no.set(true);
                        }
                    } else {
                        output.addAll(strings);
                        cancel.set(true);
                    }
                } catch (CommandParseException e) {
                    UltracraftServer.LOGGER.error("Failed to parse command: ", e);
                    cancel.set(true);
                }
            }).ifArgType(t -> {
                List<String> res;
                try {
                    String[] completionArgs = Arrays.copyOfRange(args, context.getCurrent(), args.length);
                    res = t.getCompleter().tabComplete(sender, commandCtx, context, completionArgs);
                } catch (CommandParseException e) {
                    output.clear();
                    cancel.set(true);
                    UltracraftServer.LOGGER.error("Failed to parse command: ", e);
                    return;
                }
                if (context.isAtEndOfCmd()) {
                    output.clear();
                    output.addAll(res);
                    cancel.set(true);
                }
            });

            if (no.get()) {
                return null;
            }
            if (cancel.get()) {
                return output;
            }
        }
        return output;
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