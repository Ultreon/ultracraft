package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.error.*;
import com.ultreon.craft.api.commands.output.BasicCommandOutput;
import com.ultreon.craft.api.commands.output.CommandOutput;
import com.ultreon.craft.api.commands.output.ObjectCommandOutput;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.LivingEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.ultreon.craft.api.commands.CommandData.dropLastWhile;
import static com.ultreon.craft.util.ExceptionMap.getErrorCode;

@SuppressWarnings("LeakingThis")
public abstract class Command {
    private static final List<Runnable> loaders = new ArrayList<>();
    private static final List<Command> incompleteCommands = new ArrayList<>();
    private static final List<Command> commands = new ArrayList<>();

    public static void runCommandLoaders() {
        for (var runnable : Command.loaders){
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

    public Command() {
        Command.loaders.add(() -> {
            this.parser = new CommandParserImpl(this.getClass(), this.data, this.data.getOverloadSpecs());
            this.data.getOverloadSpecs();
            Command.commands.add(this);
            if (!this.isCreatedYet) {
                Command.incompleteCommands.add(this);
                UltracraftServer.LOGGER.warn("Incomplete command:", this.getClass().getSimpleName());
            }

            if (this.category == null)
                UltracraftServer.LOGGER.warn("Missing category in command:", this.getClass().getSimpleName());
            if (this.getRequiredPermission() == null)
                UltracraftServer.LOGGER.warn("Broken permissions in command:", this.getClass().getSimpleName());

            this.detectBrokenCommand();
        });
    }

    public static int getIncompleteCount() {
        return Command.incompleteCommands.size();
    }

    public static int getTotalCount() {
        return Command.commands.size();
    }

    private void detectBrokenCommand() {
        for (var method : this.data.getMethods()){
            // FIXME this stuff is broken :skull:
//            val parameterTypes = method.parameterTypes
//            val overloads = this.parser.overloads
//            val any = overloads
//                .asSequence()
//                .filter { overload -> overload
//                    .args()
//                    .filter { param -> param is ArgumentType<?> }.size != parameterTypes.size - 3 }
//                .any()
//
//            if (any)
//                UltracraftServer.LOGGER.warn("Broken command overload:", this::class.simpleName)
        }
    }

    public boolean onCommand(CommandSender sender, CommandContext commandCtx, String alias, String[] args) {
        try {
            if (this.getRequiredPermission() == null) {
                new PermissionsBrokenError().send(sender);
                return true;
            }

            // Check if the command needs an entity to execute.
            if (this.requiresEntity && !(sender instanceof Entity entity)) {
                this.needEntity().send(sender, this.data);
                return true;
            }

            // Check if the command needs a living entity to execute.
            if (this.requiresLivingEntity && !(sender instanceof LivingEntity livingEntity)) {
                this.needLivingEntity().send(sender, this.data);
                return true;
            }

            // Check if the command needs a player to execute.
            if (this.requiresPlayer && !(sender instanceof Player player)) {
                this.needPlayer().send(sender, this.data);
                return true;
            }

            // Check if the command is created yet.
            if (this.data.getStatus() == CommandStatus.DEBUG
                && sender.hasExplicitPermission("ultracraft.debug_command")) {
                UltracraftServer.LOGGER.warn("Access to <!>DEBUG</> command: ${sender.name}");
                this.noPermission().send(sender);
                return true;
            }

            // Check for WIP status.
            if (this.data.getStatus() == CommandStatus.WIP && sender.hasPermission("ultracraft.status.unfinished_command")) {
                UltracraftServer.LOGGER.warn("Access to <!>WIP</> command: ${sender.name}");
                this.noPermission().send(sender);
                return true;
            }

            // Check for required permission
            if (!sender.hasExplicitPermission(this.getRequiredPermission())){
                this.noPermission().send(sender);
                return true;
            }

            // Check for required permission
            if (!this.isCreatedYet && !sender.hasPermission("ultracraft.status.empty_command")
            ) {
                this.noPermission().send(sender);
                return true;
            }

            // Check if the command is created yet.
            if (!this.isCreatedYet) {
                this.errorMessage("Command not created properly yet.").send(sender);
                return true;
            }
            if (args.length == 1 && Objects.equals(args[0], "--help")) {
                this.data.sendHelp("Help", sender, alias);
                return true;
            }

            // Execute command on the command parser.
            final CommandOutput commandOutput;
            try {
                commandOutput = this.parser.execute(this, sender, commandCtx, alias, args);
            } catch (CommandParseException e){
                e.send(sender);
                return true;
            } catch(CommandArgumentMismatch e){
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
            if (commandOutput != null) {
                if (commandOutput instanceof CommandError error){
                    // Command has an error.
                    error.send(sender, this.data);
                } else{
                    // Command has an output.
                    commandOutput.send(sender);
                }
            }
        } catch (IllegalCommandException e){
            // Command crash report.
            final var crashReport = new CommandCrashReport(e, alias, args);
            final var consoleSender = UltracraftServer.get().getConsoleSender();

            // Get crash details.
            final var details = crashReport.save(sender);

            // Log each line of the report.
            for (var line : dropLastWhile(List.of(details.report().split("\n")), String::isEmpty)){
                Chat.sendFatal(consoleSender, line);
            }
            // Send error details.
            Chat.sendFatal(sender, e.getCode(), "An internal error occurred when executing the command.");
            Chat.sendFatal(sender, e.getCode(), "Error code:<i> " + e.getCode());
            Chat.sendFatal(sender, e.getCode(), "Report ID:<i> " + details.id());
            Chat.sendFatal(sender, e.toString());
        } catch(Throwable t){
            // Command crash report.
            final var crashReport = new CommandCrashReport(t, alias, args);
            final var consoleSender = UltracraftServer.get().getConsoleSender();

            // Get crash details.
            final var details = crashReport.save(sender);

            // Log each line of the report.
            for (var line : dropLastWhile(List.of(details.report().split("\n")), String::isEmpty))
                Chat.sendFatal(sender, line);

            // Send error details.
            Chat.sendFatal(sender, "An internal error occurred when executing the command.");
            Chat.sendFatal(sender, "Error code:<i> " + getErrorCode(t));
            Chat.sendFatal(sender, "Report ID:<i> " + details.id());
            Chat.sendFatal(sender, t.getMessage());
        }
        return true;
    }

    public @Nullable List<String> onTabComplete(CommandSender sender, CommandContext commandCtx, String alias, String[] args) {
        if (!this.isCreatedYet) {
            return new ArrayList<>();
        }
        if (this.requiresOperator0 && !sender.isAdmin()) {
            return new ArrayList<>();
        }

        String permission = this.getRequiredPermission();
        if (permission != null && !sender.hasPermission(permission)){
            return new ArrayList<>();
        }

        if (this.requiresPlayer && !(sender instanceof Player)) return new ArrayList<>();
        return this.deduplicate(this.parser.tabComplete(sender, commandCtx, alias, args));
    }

    private List<String> deduplicate(List<String> strings) {
        Set<String> set = new LinkedHashSet<>(strings);
        strings.clear();
        strings.addAll(set);
        return strings;
    }

    protected CommandOutput voidOutput() {
        return new ObjectCommandOutput(null, ObjectCommandOutput.TYPE.VOID);
    }

    protected CommandOutput objectOutput(@Nullable Object object) {
        CommandOutput result;
        CommandError commandError = (CommandError) object;
        if (commandError == null) {
            result = new ObjectCommandOutput(object, ObjectCommandOutput.TYPE.OBJECT);
        } else {
            result = commandError;
        }
        return result;
    }

    protected CommandOutput genericMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.SERVER);
    }

    protected CommandOutput editModeMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.EDIT_MODE);
    }

    protected CommandOutput successMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.SUCCESS);
    }

    protected CommandOutput infoMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.INFO);
    }

    protected CommandOutput warningMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.WARNING);
    }

    protected CommandOutput errorMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.ERROR);
    }

    protected CommandOutput deniedMessage(@NotNull String msg) {
        return new BasicCommandOutput(msg, BasicCommandOutput.MessageType.DENIED);
    }

    protected CommandError overloadError() {
        return new OverloadError();
    }

    protected CommandError noPermission() {
        return new NoPermissionError();
    }

    protected CommandError needPlayer() {
        return new NeedPlayerError();
    }

    protected CommandError needLivingEntity() {
        return new NeedLivingEntityError();
    }

    protected CommandError needEntity() {
        return new NeedEntityError();
    }

    protected void requirePlayer() {
        this.requiresPlayer = true;
    }

    protected void requireEntity() {
        this.requiresEntity = true;
    }

    public void requireLivingEntity() {
        this.requiresLivingEntity = true;
    }

    public void setNeedPlayer(boolean needsPlayer) {
        this.requiresPlayer = needsPlayer;
    }

    public void setNeedEntity(boolean needsEntity) {
        this.requiresEntity = needsEntity;
    }

    public void setNeedLivingEntity(boolean needsLivingEntity) {
        this.requiresLivingEntity = needsLivingEntity;
    }

    public void incomplete() {
        this.isCreatedYet = false;
    }

    protected void requirePermission(@Nullable String requiredPermission) {
        this.requiredPermission_ = requiredPermission;
    }

    protected void requireOperator() {
        this.requiresOperator0 = true;
    }

    protected void requireRole(Role role) {
        this.requiredRoles.add(role);
    }

    public boolean getRequiresOperator() {
        return this.requiresOperator0;
    }

    public CommandOutput warningMessage(MessageCode messageCode, String message) {
        return this.warningMessage("[" + messageCode.getCode() + "] " + message);
    }

    public CommandOutput wip() {
        return this.warningMessage(MessageCode.WIP, "This command is an work in progress.");
    }

    public CommandOutput outdated() {
        return this.warningMessage(MessageCode.OUTDATED, "This command is an work in progress.");
    }

    public CommandOutput deprecated() {
        return this.warningMessage(MessageCode.DEPRECATED, "This command is deprecated and will be removed in the future.");
    }

    public CommandData data() {
        return this.data;
    }

    protected void setCategory(CommandCategory category) {
        this.category = category;
    }
}