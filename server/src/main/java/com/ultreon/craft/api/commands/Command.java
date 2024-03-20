package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.api.commands.output.BasicCommandResult;
import com.ultreon.craft.api.commands.output.CommandResult;
import com.ultreon.craft.api.commands.output.ObjectCommandResult;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ultreon.craft.api.commands.CommandData.dropLastWhile;

@SuppressWarnings("LeakingThis")
public abstract class Command {
    private static final List<Runnable> loaders = new ArrayList<>();
    private static final List<Command> incompleteCommands = new ArrayList<>();
    private static final List<Command> commands = new ArrayList<>();

    public static void runCommandLoaders() {
        for (var runnable : Command.loaders) {
            runnable.run();
        }
        Command.loaders.clear();
    }

    private final CommandData data = new CommandData(this);
    private @MonotonicNonNull CommandParserImpl parser;
    private String requiredPermission_ = null;

    public String getRequiredPermission() {
        Perm annotation = Arrays.stream(this.getClass().getAnnotations())
                .filter(it -> it instanceof Perm)
                .map(Perm.class::cast)
                .findFirst().orElse(null);

        if (annotation != null) {
            return annotation.value();
        }

        return this.requiredPermission_;
    }

    private boolean isCreatedYet = true;
    private boolean requiresPlayer = false;
    private boolean requiresLivingEntity = false;
    private boolean requiresEntity = false;
    private boolean requiresOperator0 = false;
    private final List<Role> requiredRoles = new ArrayList<>();

    private CommandCategory category;

    /**
     * Constructor for Command class. It initializes the command by adding it to various lists and performing checks.
     */
    public Command() {
        Command.loaders.add(() -> {
            this.parser = new CommandParserImpl(this.getClass(), this.data, this.data.getOverloadSpecs());
            this.data.getOverloadSpecs();
            Command.commands.add(this);
            if (!this.isCreatedYet) {
                Command.incompleteCommands.add(this);
                UltracraftServer.LOGGER.warn("Incomplete command: {}", this.getClass().getSimpleName());
            }

            if (this.category == null)
                UltracraftServer.LOGGER.warn("Missing category in command: {}", this.getClass().getSimpleName());
            if (this.getRequiredPermission() == null)
                UltracraftServer.LOGGER.warn("Broken permissions in command: {}", this.getClass().getSimpleName());

            this.detectBrokenCommand();
        });
    }

    public static int getIncompleteCount() {
        return Command.incompleteCommands.size();
    }

    public static int getTotalCount() {
        return Command.commands.size();
    }

    /**
     * Detects and handles broken command methods.
     */
    private void detectBrokenCommand() {
        // Iterate through all methods in this data
        for (Method method : this.data.getMethods()) {
            // Skip if method is not annotated with SubCommand
            if (method.getAnnotation(DefineCommand.class) == null) continue;

            // Check if return type is CommandResult or subtype
            if (!CommandResult.class.isAssignableFrom(method.getReturnType())) {
                // Throw error if return type is invalid
                throw new InvalidCommandMethodError("Invalid return type: %s (%s - %s)".formatted(
                        method.getReturnType().getName(),
                        method.getName(),
                        method.getDeclaringClass().getName()
                ));
            }

            // Check for invalid method types
            checkForInvalidMethodTypes(method);
        }

        // Throw error if no overload specifications found
        if (this.data.getOverloadSpecs().isEmpty()) {
            throw new InvalidCommandMethodError("No overloads found! (%s)".formatted(
                    this.getClass().getName()
            ));
        }
    }

    private static void checkForInvalidMethodTypes(Method method) {
        if (method.getParameters().length < 3) {
            throw new InvalidCommandMethodError("Invalid number of parameters (min 3): " + method.getParameters().length + " (" + method.getName() + " - " + method.getDeclaringClass().getName() + ")");
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        checkParams(method, parameterTypes);
    }

    private static void checkParams(Method method, Class<?>[] parameterTypes) {
        if (parameterTypes[0] != CommandSender.class) {
            throw new InvalidCommandMethodError("Invalid first parameter type: " + parameterTypes[0].getName() + " (" + method.getName() + " - " + method.getDeclaringClass().getName() + ")");
        }

        if (parameterTypes[1] != CommandContext.class) {
            throw new InvalidCommandMethodError("Invalid second parameter type: " + parameterTypes[1].getName() + " (" + method.getName() + " - " + method.getDeclaringClass().getName() + ")");
        }

        if (parameterTypes[2] != String.class) {
            throw new InvalidCommandMethodError("Invalid third parameter type: " + parameterTypes[2].getName() + " (" + method.getName() + " - " + method.getDeclaringClass().getName() + ")");
        }
    }

    /**
     * Execute the command based on given parameters.
     *
     * @param sender     the command sender
     * @param commandCtx the command context
     * @param alias      the command alias
     * @param args       the command arguments
     * @return true if the command is executed successfully
     */
    public boolean onCommand(CommandSender sender, CommandContext commandCtx, String alias, String[] args) {
        try {
            // Check if permission is required and send error if not provided
            if (this.getRequiredPermission() == null) {
                new PermissionsBrokenError().send(sender);
                return true;
            }

            // Check if the command needs an entity to execute
            if (this.requiresEntity && !(sender instanceof Entity entity)) {
                this.needEntity().send(sender, this.data);
                return true;
            }

            // Check if the command needs a living entity to execute
            if (this.requiresLivingEntity && !(sender instanceof LivingEntity livingEntity)) {
                this.needLivingEntity().send(sender, this.data);
                return true;
            }

            // Check if the command needs a player to execute
            if (this.requiresPlayer && !(sender instanceof Player player)) {
                this.needPlayer().send(sender, this.data);
                return true;
            }

            // Check if the command is in DEBUG mode and sender has explicit permission
            if (this.data.getStatus() == CommandStatus.DEBUG
                && sender.hasExplicitPermission("ultracraft.debug_command")) {
                UltracraftServer.LOGGER.warn("Access to <!>DEBUG</> command: ${sender.name}");
                this.noPermission().send(sender);
                return true;
            }

            // Check for WIP status and sender has permission
            if (this.data.getStatus() == CommandStatus.WIP && sender.hasPermission("ultracraft.status.unfinished_command")) {
                UltracraftServer.LOGGER.warn("Access to <!>WIP</> command: ${sender.name}");
                this.noPermission().send(sender);
                return true;
            }

            // Check for required permission and send error if not provided
            if (!sender.hasExplicitPermission(this.getRequiredPermission())) {
                this.noPermission().send(sender);
                return true;
            }

            // Check for required permission and send error if not provided
            if (!this.isCreatedYet && !sender.hasPermission("ultracraft.status.empty_command")) {
                this.noPermission().send(sender);
                return true;
            }

            // Send error message if the command is not created yet
            if (!this.isCreatedYet) {
                this.errorMessage("Command not created properly yet.").send(sender);
                return true;
            }

            // Send help if --help argument is provided
            if (args.length == 1 && Objects.equals(args[0], "--help")) {
                this.data.sendHelp("Help", sender, alias);
                return true;
            }

            // Execute command on the command parser and handle exceptions
            final CommandResult commandResult;
            try {
                commandResult = this.parser.execute(this, sender, commandCtx, alias, args);
            } catch (CommandParseException e) {
                e.send(sender);
                return true;
            } catch (CommandArgumentMismatch e) {
                // Handle command argument mismatch
                final var dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH.mm.ss.SSS"));
                final var dir = new File("crash-reports/commands/arg-mismatch");
                if (!dir.exists()) dir.mkdirs();
                final var file = new File(dir, "crash $dateTime.txt");
                e.dump(file);
                Chat.sendFatal(sender, "An internal error occurred when executing the command.");
                if (sender.hasPermission("ultracraft.error.command.argument_mismatch")) {
                    Chat.sendFatal(sender, "Possibly a command argument mismatch has occurred.");
                }
                return true;
            }
            if (commandResult != null) {
                if (commandResult instanceof CommandError error) {
                    // Command has an error
                    error.send(sender, this.data);
                } else {
                    // Command has an output
                    commandResult.send(sender);
                }
            }
        } catch (IllegalCommandException e) {
            // Handle command crash report
            final var crashReport = new CommandCrashReport(e, alias, args);
            final var consoleSender = UltracraftServer.get().getConsoleSender();

            // Get crash details and log each line of the report
            final var details = crashReport.save(sender);
            for (var line : dropLastWhile(List.of(details.report().split("\n")), String::isEmpty)) {
                Chat.sendFatal(consoleSender, line);
            }

            // Send error details
            Chat.sendFatal(sender, e.getCode(), "An internal error occurred when executing the command.");
            Chat.sendFatal(sender, e.getCode(), "Error code:<i> " + e.getCode());
            Chat.sendFatal(sender, e.getCode(), "Report ID:<i> " + details.id());
            Chat.sendFatal(sender, e.getMessage());
        }
        return true;
    }

    /**
     * This method handles tab completion for a custom command.
     *
     * @param sender     The command sender
     * @param commandCtx The command context
     * @param alias      The alias of the command
     * @param args       The arguments of the command
     * @return A list of tab completion suggestions
     */
    public @Nullable List<String> onTabComplete(CommandSender sender, CommandContext commandCtx, String alias, String[] args) {
        // Check if the command has been created
        if (!this.isCreatedYet) {
            return new ArrayList<>();
        }

        // Check if operator is required and if the sender is an admin
        if (this.requiresOperator0 && !sender.isAdmin()) {
            return new ArrayList<>();
        }

        // Check if the sender has the required permission
        String permission = this.getRequiredPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            return new ArrayList<>();
        }

        // Check if the command requires a player and if the sender is not a player
        if (this.requiresPlayer && !(sender instanceof Player)) {
            return new ArrayList<>();
        }

        // Perform tab completion using the command parser
        return this.deduplicate(this.parser.tabComplete(sender, commandCtx, alias, args));
    }

    /**
     * Removes duplicates from a list of strings.
     *
     * @param strings The list of strings to deduplicate
     * @return A list of strings without duplicates
     */
    private List<String> deduplicate(List<String> strings) {
        Set<String> set = new LinkedHashSet<>(strings);
        strings.clear();
        strings.addAll(set);
        return strings;
    }

    /**
     * Creates a new void result.
     *
     * @return The new void result
     */
    protected CommandResult voidResult() {
        return new ObjectCommandResult(null, ObjectCommandResult.TYPE.VOID);
    }

    /**
     * Creates a CommandResult based on the provided object.
     *
     * @param object The object to create the result from
     * @return The CommandResult based on the object
     */
    protected CommandResult objectResult(@Nullable Object object) {
        CommandResult result;
        CommandError commandError = (CommandError) object;
        if (commandError == null) {
            result = new ObjectCommandResult(object, ObjectCommandResult.TYPE.OBJECT);
        } else {
            result = commandError;
        }
        return result;
    }

    /**
     * Creates a CommandResult for a generic server message.
     *
     * @param msg The message for the server
     * @return The CommandResult for the server message
     */
    protected CommandResult genericMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.SERVER);
    }

    /**
     * Creates a CommandResult for an edit mode message.
     *
     * @param msg The message for the edit mode
     * @return The CommandResult for the edit mode message
     */
    protected CommandResult editModeMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.EDIT_MODE);
    }

    /**
     * Creates a CommandResult for a success message.
     *
     * @param msg The success message
     * @return The CommandResult for the success message
     */
    protected CommandResult successMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.SUCCESS);
    }

    /**
     * Creates a CommandResult for an info message.
     *
     * @param msg The info message
     * @return The CommandResult for the info message
     */
    protected CommandResult infoMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.INFO);
    }

    /**
     * Creates a CommandResult for a warning message.
     *
     * @param msg The warning message
     * @return The CommandResult for the warning message
     */
    protected CommandResult warningMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.WARNING);
    }

    /**
     * Creates a CommandResult for an error message.
     *
     * @param msg The error message
     * @return The CommandResult for the error message
     */
    protected CommandResult errorMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.ERROR);
    }

    /**
     * Creates a CommandResult for a denied message.
     *
     * @param msg The denied message
     * @return The CommandResult for the denied message
     */
    protected CommandResult deniedMessage(@NotNull String msg) {
        return new BasicCommandResult(msg, BasicCommandResult.MessageType.DENIED);
    }

    /**
     * Returns an OverloadError instance.
     *
     * @return OverloadError instance
     */
    protected CommandError overloadError() {
        return new OverloadError();
    }

    /**
     * Returns a NoPermissionError instance.
     *
     * @return NoPermissionError instance
     */
    protected CommandError noPermission() {
        return new NoPermissionError();
    }

    /**
     * Returns a NeedPlayerError instance.
     *
     * @return NeedPlayerError instance
     */
    protected CommandError needPlayer() {
        return new NeedPlayerError();
    }

    /**
     * Returns a NeedLivingEntityError instance.
     *
     * @return NeedLivingEntityError instance
     */
    protected CommandError needLivingEntity() {
        return new NeedLivingEntityError();
    }

    /**
     * Returns a NeedEntityError instance.
     *
     * @return NeedEntityError instance
     */
    protected CommandError needEntity() {
        return new NeedEntityError();
    }

    /**
     * Marks that this action requires a player.
     */
    protected void requirePlayer() {
        this.requiresPlayer = true;
    }

    /**
     * Marks that this action requires an entity.
     */
    protected void requireEntity() {
        this.requiresEntity = true;
    }

    /**
     * Marks that this action requires a living entity.
     */
    public void requireLivingEntity() {
        this.requiresLivingEntity = true;
    }

    /**
     * Sets whether this action needs a player.
     *
     * @param needsPlayer true if a player is needed, false otherwise
     */
    public void setNeedPlayer(boolean needsPlayer) {
        this.requiresPlayer = needsPlayer;
    }

    /**
     * Sets whether this action needs an entity.
     *
     * @param needsEntity true if an entity is needed, false otherwise
     */
    public void setNeedEntity(boolean needsEntity) {
        this.requiresEntity = needsEntity;
    }

    /**
     * Sets whether this action needs a living entity.
     *
     * @param needsLivingEntity true if a living entity is needed, false otherwise
     */
    public void setNeedLivingEntity(boolean needsLivingEntity) {
        this.requiresLivingEntity = needsLivingEntity;
    }

    /**
     * Marks that this action is incomplete and not created yet.
     */
    public void incomplete() {
        this.isCreatedYet = false;
    }

    /**
     * Sets the required permission for this action.
     *
     * @param requiredPermission the required permission
     */
    protected void requirePermission(@Nullable String requiredPermission) {
        this.requiredPermission_ = requiredPermission;
    }

    /**
     * Marks that this action requires an operator.
     */
    protected void requireOperator() {
        this.requiresOperator0 = true;
    }

    /**
     * Adds a required role for this action.
     *
     * @param role the role to require
     */
    protected void requireRole(Role role) {
        this.requiredRoles.add(role);
    }

    /**
     * Checks if this action requires an operator.
     *
     * @return true if an operator is required, false otherwise
     */
    public boolean getRequiresOperator() {
        return this.requiresOperator0;
    }

    /**
     * Generates a warning message with a custom message code and message.
     *
     * @param messageCode The message code.
     * @param message     The custom message.
     * @return The command result.
     */
    public CommandResult warningMessage(MessageCode messageCode, String message) {
        return this.warningMessage("[" + messageCode.getCode() + "] " + message);
    }

    /**
     * Generates a warning message for a work in progress command.
     *
     * @return The command result.
     */
    public CommandResult wip() {
        return this.warningMessage(MessageCode.WIP, "This command is an work in progress.");
    }

    /**
     * Generates a warning message for an outdated command.
     *
     * @return The command result.
     */
    public CommandResult outdated() {
        return this.warningMessage(MessageCode.OUTDATED, "This command is an work in progress.");
    }

    /**
     * Generates a warning message for a deprecated command.
     *
     * @return The command result.
     */
    public CommandResult deprecated() {
        return this.warningMessage(MessageCode.DEPRECATED, "This command is deprecated and will be removed in the future.");
    }

    /**
     * This method returns the CommandData object associated with the current instance of the class.
     *
     * @return CommandData object
     */
    public CommandData data() {
        return this.data;
    }

    /**
     * This method sets the category of the current instance of the class.
     *
     * @param category The category to be set
     */
    protected void setCategory(CommandCategory category) {
        this.category = category;
    }
}