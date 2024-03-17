package com.ultreon.craft.api.commands;

import com.google.common.collect.Lists;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.api.commands.error.CommandError;
import com.ultreon.craft.api.commands.error.InvalidError;
import com.ultreon.craft.api.commands.output.BasicCommandResult;
import com.ultreon.craft.api.commands.selector.*;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.*;
import com.ultreon.craft.gamerule.Rule;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.server.player.CacheablePlayer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.Gamemode;
import com.ultreon.craft.weather.Weather;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.Location;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.World;
import com.ultreon.data.UsoParser;
import com.ultreon.data.types.IType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.datetime.v0.Duration;
import it.unimi.dsi.fastutil.objects.Reference2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class CommandData {
    private final Command executor;
    private final Map<CommandSpec, String> overloads0 = new HashMap<>();
    private final List<String> aliases0 = new ArrayList<>();

    private @Nullable CommandStatus status0 = null;
    private @Nullable String description = null;
    
    private final Reference2BooleanMap<CommandFlag> flags = new Reference2BooleanArrayMap<>();
    private final Map<CommandSpec, Method> methodMap = new HashMap<>();
    private final Map<CommandSpec, @Nullable String> permissionMap = new HashMap<>();
    public CommandData(Command executor) {
        this.executor = executor;
    }

    public CommandData alias(String alias) {
        this.aliases0.add(alias);
        return this;
    }

    public CommandData aliases(String... aliases) {
        this.aliases0.addAll(List.of(aliases));
        return this;
    }

    public Map<CommandSpec, String> getOverloads() {
        return Collections.unmodifiableMap(this.overloads0);
    }

    public Collection<CommandSpec> getCommandSpecs() {
        return Collections.unmodifiableCollection(this.overloads0.keySet());
    }

    public Collection<String> getOverloadDetails() {
        return this.overloads0.values();
    }

    public void sendUsage(String name, CommandSender sender) {
        this.sendUsage(name, sender, null);
    }

    public void sendUsage(String name, CommandSender sender, @Nullable String usage) {
        this.sendUsage0(name, sender, usage);
        if (this.getStatus() != null) {
            switch (this.getStatus()) {
                case WIP -> this.executor.wip().send(sender);
                case OUTDATED -> this.executor.outdated().send(sender);
                case DEPRECATED -> this.executor.deprecated().send(sender);
                default -> {}
            }
        }
    }

    public CommandStatus getStatus() {
        return this.status0;
    }

    private void sendUsage0(String name, CommandSender s) {
        this.sendUsage0(name, s, null);
    }

    private void sendUsage0(String name, CommandSender sender, @Nullable String usage) {
        Chat.sendError(sender, "</ptc/><b><_>Invalid usage:", name);
        if (usage != null) {
            Chat.sendError(sender, "  <gray-12>Usage: <gray-8>$usage", name);
        }
        this.sendInfo(name, sender);
    }

    public void sendHelp(String name, CommandSender s, String alias) {
        Chat.sendError(s, "</ptc/><b><_>Command information for alias: $alias", name);
        this.sendInfo(name, s);
        for (var entry : this.flags.reference2BooleanEntrySet()) {
            if (entry.getBooleanValue()) {
                final var key = entry.getKey();
                new BasicCommandResult("</pc/>[</ptc/>" + key.getMessageCode() + "</pc/>] </>" + key.getDescription(), key.getMessageType()).send(s);
            }
        }
    }

    private void sendInfo(String name, CommandSender s) {
        if (this.description != null) {
            Chat.sendError(s, "  <gray-12>Description: <gray-8>${this.description}", name);
        }
        Chat.sendError(s, "<!>Overloads:", name);
        for (var entry : this.overloads0.entrySet()) {
            Chat.sendError(s, "  <gray-12>" + entry.getKey().toString().replace("<", "&<"), name);
            Chat.sendError(s, "    <gray-8>$value", name);
        }
        Chat.sendError(s, "<!>Aliases:", name);
        Chat.sendError(s, "<gray-12>" + String.join(", ", this.aliases0), name);
    }

    public void description(@Nullable String description) {
        this.description = description;
    }

    public void setFlag(CommandFlag flag, boolean enable) {
        this.flags.put(flag, enable);
    }

    public void onRegister(CommandContext commandCtx) {
        for (var method : this.executor.getClass().getMethods()) {
            final var annotation = method.getAnnotation(DefineCommand.class);
            if (annotation == null) continue;
            @Nullable String permission = null;
            final var perm = method.getAnnotation(Perm.class);
            if (perm != null) permission = perm.value();
            final var stringSpec = annotation.value();
            final var info = annotation.comment();
            final var compiled = Objects.equals(info, "")
                    ? new CommandSpecParser().parse("/" + commandCtx.name())
                    : new CommandSpecParser().parse("/" + commandCtx.name() + " " + stringSpec);
            this.overloads0.put(compiled, info);
            this.methodMap.put(compiled, method);
            this.permissionMap.put(compiled, permission);
        }
    }

    public @Nullable Method mapToMethod(@Nullable CommandSpec spec) {
        return this.methodMap.get(spec);
    }

    final List<Method> getMethods() {
        return this.methodMap.values().stream().toList();
    }

    public @Nullable String mapToPerm(@Nullable CommandSpec spec) {
        return this.permissionMap.get(spec);
    }

    public void setCommandStatus(CommandStatus value) {
        if (this.status0 == null) {
            this.status0 = value;
        } else {
            UltracraftServer.LOGGER.error("Command status already set for command-executor: ${this.executor.getClass()}");
        }
    }
    
    public String[] getAliases() {
        return this.aliases0.toArray(new String[0]);
    }

    private static final Map<String, CommandParser<?>> parsers = new HashMap<>();
    private static final Map<String, Class<?>> types = new HashMap<>();
    private static final Map<String, CommandTabCompleter> completers = new HashMap<>();

    static {
        CommandData.registerArgument("any-player", CommandData::readAnyPlayer, CommandData::completeAnyPlayer);
        CommandData.registerArgument("attribute", CommandData::readAttribute, CommandData::completeAttribute);
        CommandData.registerArgument("biome", CommandData::readBiome, CommandData::completeBiome);
        CommandData.registerArgument("block", CommandData::readBlock, CommandData::completeBlocks);
        CommandData.registerArgument("boolean", CommandReader::readBoolean, CommandData::completeBooleans);
        CommandData.registerArgument("byte", CommandReader::readByteHex, CommandData::completeHex);
        CommandData.registerArgument("command", CommandReader::readMessage, CommandData::completeCommand);
        CommandData.registerArgument("command-sender", CommandData::readCommandSender, CommandData::completeCommandSender);
        CommandData.registerArgument("date", CommandData::readDate, CommandData::completeDate);
        CommandData.registerArgument("date-time", CommandData::readDateTime, CommandData::completeDateTime);
        CommandData.registerArgument("dimension", CommandData::readDimension, CommandData::completeDimensions);
        CommandData.registerArgument("double", CommandReader::readDouble, CommandData::completeFloats);
        CommandData.registerArgument("duration", CommandData::readDuration, CommandData::completeFloats);
        CommandData.registerArgument("entity", CommandData::readEntity, CommandData::completeEntities);
        CommandData.registerArgument("entity-type", CommandData::readEntityTypeExceptPlayer, CommandData::completeEntityTypesExceptPlayer);
        CommandData.registerArgument("entity-type-all", CommandData::readEntityType, CommandData::completeEntityTypes);
        CommandData.registerArgument("gamemode", CommandData::readGamemode, CommandData::completeGamemode);
        CommandData.registerArgument("give-item", CommandData::readItem, CommandData::completeItems);
        CommandData.registerArgument("int", CommandReader::readInt, CommandData::completeInts);
        CommandData.registerArgument("int8", CommandReader::readByte, CommandData::completeInts);
        CommandData.registerArgument("int16", CommandReader::readShort, CommandData::completeInts);
        CommandData.registerArgument("int32", CommandReader::readInt, CommandData::completeInts);
        CommandData.registerArgument("int64", CommandReader::readLong, CommandData::completeInts);
        CommandData.registerArgument("item", CommandData::readItem, CommandData::completeItems);
        CommandData.registerArgument("item-stack", CommandData::readItemStackRef, CommandData::completeItemStackRef);
        CommandData.registerArgument("item-stack-ref", CommandData::readItemStackRef, CommandData::completeItemStackRef);
        CommandData.registerArgument("living-entity", CommandData::readLivingEntity, CommandData::completeLivingEntities);
        CommandData.registerArgument("long", CommandReader::readLong, CommandData::completeInts);
        CommandData.registerArgument("long-int", CommandReader::readLong, CommandData::completeInts);
        CommandData.registerArgument("location", CommandData::readLocation, CommandData::completePosition);
        CommandData.registerArgument("position", CommandData::readPosition, CommandData::completePosition);
        CommandData.registerArgument("message", CommandReader::readMessage, CommandData::completeVoid);
        CommandData.registerArgument("offline-player", CommandData::readOfflinePlayer, CommandData::completeOfflinePlayers);
        CommandData.registerArgument("player", CommandData::readPlayer, CommandData::completeOnlinePlayers);
        CommandData.registerArgument("short", CommandReader::readShort, CommandData::completeInts);
        CommandData.registerArgument("short-int", CommandReader::readShort, CommandData::completeInts);
        CommandData.registerArgument("sound", CommandData::readSound, CommandData::completeSounds);
        CommandData.registerArgument("string", CommandData::readString, CommandData::completeVoidArg);
        CommandData.registerArgument("time", CommandData::readTime, CommandData::completeTime);
        CommandData.registerArgument("ubo", CommandData::readUbo, CommandData::completeVoid);
        CommandData.registerArgument("uuid", CommandData::readUuid, CommandData::completeVoidArg);
        CommandData.registerArgument("weather", CommandData::readWeather, CommandData::completeWeather);
        CommandData.registerArgument("world", CommandData::readDimension, CommandData::completeDimensions);
    }

    private static List<String> completeVoid(CommandSender commandSender, CommandContext commandCtx, CommandReader ctx, String[] strings) throws CommandParseException {
        ctx.readMessage();
        return new ArrayList<>();
    }

    private static List<String> completeVoidArg(CommandSender commandSender, CommandContext commandCtx, CommandReader ctx, String[] strings) throws CommandParseException {
        ctx.readString();
        return new ArrayList<>();
    }

    private static @Nullable ServerPlayer readPlayer(CommandReader ctx) throws CommandParseException {
        var parsed = new PlayerBaseSelector(ctx.getSender(), ctx.getArgument());
        ctx.readString();
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        return (ServerPlayer) parsed.getValue();
    }

    private static Entity readEntity(CommandReader ctx) throws CommandParseException {
        EntityBaseSelector<@NotNull Entity> parsed = new EntityBaseSelector<>(ctx.getSender(), Entity.class, ctx.readString());
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        Entity value = parsed.getValue();
        if (value == null) throw new CommandParseException.NotFound("entity", ctx.getOffset());
        return value;
    }

    private static LivingEntity readLivingEntity(CommandReader ctx) throws CommandParseException {
        EntityBaseSelector<@NotNull LivingEntity> parsed = new EntityBaseSelector<>(ctx.getSender(), LivingEntity.class, ctx.readString());
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        LivingEntity value = parsed.getValue();
        if (value == null) throw new CommandParseException.NotFound("living entity", ctx.getOffset());
        return value;
    }

    private static @Nullable CommandSender readCommandSender(CommandReader ctx) throws CommandParseException {
        var parsed = new CommandSenderBaseSelector(ctx.getSender(), ctx.readString());
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        return parsed.getValue();
    }

    private static Weather readWeather(CommandReader ctx) throws CommandParseException {
        return switch (ctx.readString()) {
            case "clear", "sunny" -> Weather.SUNNY;
            case "rain", "downfall" -> Weather.RAIN;
            case "storm", "thunder" -> Weather.THUNDER;
            default -> throw new CommandParseException.NotFound("weather", ctx.getOffset());
        };
    }

    private static World readDimension(CommandReader ctx) throws CommandParseException {
        World dimension = UltracraftServer.get().getWorld(ctx.readId());
        if (dimension == null) throw new CommandParseException.NotFound("dimension", ctx.getOffset());
        return dimension;
    }

    private static UUID readUuid(CommandReader ctx) throws CommandParseException {
        try {
            return UUID.fromString(ctx.readString());
        } catch (IllegalArgumentException e) {
            throw new CommandParseException.NotFound("dimension", ctx.getOffset());
        }
    }

    private static IType<?> readUbo(CommandReader ctx) throws CommandParseException {
        String s = ctx.readMessage();
        if (s == null) throw new CommandParseException.Invalid("ubo data", ctx.getOffset());

        try {
            return new UsoParser(s).parse();
        } catch (IOException e) {
            throw new CommandParseException.Invalid("ubo data", ctx.getOffset());
        }
    }

    private Rule<?> readRule(CommandReader ctx) throws CommandParseException {
        Rule<?> rule = UltracraftServer.get().getGameRules().getRule(ctx.readString());
        if (rule == null) throw new CommandParseException.NotFound("number rule", ctx.getOffset());
        return rule;
    }

    private static CacheablePlayer readAnyPlayer(CommandReader ctx) throws CommandParseException {
        var parsed = new AnyPlayerBaseSelector(ctx.getSender(), ctx.readString());
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        return parsed.getValue();
    }

    private static CacheablePlayer readOfflinePlayer(CommandReader ctx) throws CommandParseException {
        var parsed = new OfflinePlayerBaseSelector(ctx.getSender(), ctx.readString());
        var error = parsed.getError();
        if (error != null) {
            throw new CommandParseException(error, ctx.getOffset());
        }
        return parsed.getValue();
    }

    /**
     * @param <T> the type of argument.
     */
    @SafeVarargs
    public static <T> void registerArgument(String tag, CommandParser<T> parser, CommandTabCompleter completer, T... typeGetter) {
        var componentType = typeGetter.getClass().getComponentType();
        CommandData.types.put(tag, componentType);
        CommandData.parsers.put(tag, parser);
        CommandData.completers.put(tag, completer);
    }

    private static Duration readDuration(CommandReader ctx) throws CommandParseException {
        try {
            return Utils.parseDuration(ctx.readString());
        } catch (Exception e) {
            throw new CommandParseException(new InvalidError("duration"), ctx.getOffset());
        }
    }

    private static LocalTime readTime(CommandReader ctx) throws CommandParseException {
        try {
            return LocalTime.parse(ctx.readString());
        } catch (Exception e) {
            throw new CommandParseException(new InvalidError("time"), ctx.getOffset());
        }
    }

    private static LocalDate readDate(CommandReader ctx) throws CommandParseException {
        try {
            return LocalDate.parse(ctx.readString());
        } catch (Exception e) {
            throw new CommandParseException(new InvalidError("date"), ctx.getOffset());
        }
    }

    private static LocalDateTime readDateTime(CommandReader ctx) throws CommandParseException {
        LocalDate date;
        try {
            date = LocalDate.parse(ctx.readString());
        } catch (Exception e) {
            throw new CommandParseException(new InvalidError("date"), ctx.getOffset());
        }
        LocalTime time;
        try {
            time = LocalTime.parse(ctx.readString());
        } catch (Exception e) {
            throw new CommandParseException(new InvalidError("time"), ctx.getOffset());
        }
        return LocalDateTime.of(date, time);
    }

    public static boolean validateArgId(String argId) {
        return CommandData.parsers.containsKey(argId);
    }

    public static @Nullable CommandParser<?> getParser(String argId) {
        return CommandData.parsers.get(argId);
    }

    public static @Nullable Class<?> getType(String argId) {
        return CommandData.types.get(argId);
    }

    public static @Nullable CommandTabCompleter getTabCompleter(String argId) {
        return CommandData.completers.get(argId);
    }

    private static Item readItem(CommandReader ctx) throws CommandParseException {
        var id = ctx.readId();
        var value = CommandData.getItem(id);
        if (value != null) return value;
        throw new CommandParseException.NotFound("item", ctx.getOffset());
    }

    private static Item getItem(@Nullable Identifier id) {
        for (var value : Registries.ITEM.entries()) {
            if (Objects.equals(value.getKey(), id)) {
                return value.getValue();
            }
        }
        return null;
    }

    private static @Nullable ItemStack readItemStackRef(CommandReader ctx) throws CommandParseException {
        var sender = ctx.getSender();
        var string = ctx.readString();
        if (sender instanceof LivingEntity) {
            var selector = new ItemBaseSelector(ctx.getSender(), string);
            CommandError error = selector.getError();
            if (error != null) throw new CommandParseException(error, ctx.getOffset());
            return selector.getValue();
        }
        throw new CommandParseException("Not ran from a living entity.");
    }

    private static Gamemode readGamemode(CommandReader ctx) throws CommandParseException {
        return switch (ctx.readString()) {
            case "survival" -> Gamemode.SURVIVAL;
            case "mini_game" -> Gamemode.MINI_GAME;
            case "builder" -> Gamemode.BUILDER;
            case "builder_plus" -> Gamemode.BUILDER_PLUS;
            case "spectator" -> Gamemode.SPECTATOR;
            default -> throw new CommandParseException.NotFound("gamemode", ctx.getOffset());
        };
    }

    private static Block readBlock(CommandReader ctx) throws CommandParseException {
        var id = ctx.readId();
        for (var entry : Registries.BLOCK.entries()) {
            try {
                if (Objects.equals(entry.getKey(), id)) {
                    return entry.getValue();
                }
            } catch (Exception ignored) {

            }
        }
        throw new CommandParseException.NotFound("block", ctx.getOffset());
    }

    private static EntityType<?> readEntityTypeExceptPlayer(CommandReader ctx) throws CommandParseException {
        var id = ctx.readId();
        for (var value : Registries.ENTITY_TYPE.values()) {
            try {
                if (value != EntityTypes.PLAYER && Objects.equals(value.getId(), id)) {
                    return value;
                }
            } catch (Exception ignored) {
            }
        }
        throw new CommandParseException.NotFound("entity type", ctx.getOffset());
    }

    private static EntityType<?> readEntityType(CommandReader ctx) throws CommandParseException {
        return CommandData.readFromRegistry(ctx, "entity type", Registries.ENTITY_TYPE);
    }

    private static Biome readBiome(CommandReader ctx) throws CommandParseException {
        return CommandData.readFromRegistry(ctx, "biome", Registries.BIOME);
    }

    private static Attribute readAttribute(CommandReader ctx) throws CommandParseException {
        return CommandData.readFromRegistry(ctx, "attribute", Registries.ATTRIBUTE);
    }

    private static String readString(CommandReader ctx) throws CommandParseException {
        try {
            return ctx.readString();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to read string in command " + ctx.getCommand(), e);
            throw new CommandParseException(e.getMessage(), ctx.getOffset());
        }
    }

    private static SoundEvent readSound(CommandReader ctx) throws CommandParseException {
        return CommandData.readFromRegistry(ctx, "sound", Registries.SOUND_EVENT);
    }

    public static <T> T readFromRegistry(CommandReader ctx, String type, Registry<T> registry) throws CommandParseException {
        var id = ctx.readId();
        for (var value : registry.entries()) {
            if (Objects.equals(value.getKey(), id)) {
                return value.getValue();
            }
        }
        throw new CommandParseException.NotFound(type, ctx.getOffset());
    }

    public static <T extends Enum<T>> T readFromEnum(CommandReader ctx, String type, Class<T> enumClass) throws CommandParseException {
        var id = ctx.readString();
        for (var value : enumClass.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(id)) {
                return value;
            }
        }
        throw new CommandParseException.NotFound(type, ctx.getOffset());
    }

    public static <T> T readFromFunc(CommandReader ctx, String type, Function<Identifier, T> enum_) throws CommandParseException {
        var id = ctx.readId();
        T apply = enum_.apply(id);
        if (apply == null) throw new CommandParseException.NotFound(type, ctx.getOffset());
        return apply;
    }

    private static Location readLocation(CommandReader ctx) throws CommandParseException.NotANumber, CommandParseException.NotADigit, CommandParseException.NotAtStartOfArg, CommandParseException.EndOfArgument, CommandParseException.NotAtEndOfArg {
        var x = ctx.readDouble();
        var y = ctx.readDouble();
        var z = ctx.readDouble();
        return new Location(x, y, z);
    }

    private static Vec3d readPosition(CommandReader ctx) throws CommandParseException.NotANumber, CommandParseException.NotADigit, CommandParseException.NotAtStartOfArg, CommandParseException.EndOfArgument, CommandParseException.NotAtEndOfArg {
        var x = ctx.readDouble();
        var y = ctx.readDouble();
        var z = ctx.readDouble();
        return new Vec3d(x, y, z);
    }

    private Location readLocationRot(CommandReader ctx) throws CommandParseException.NotANumber, CommandParseException.NotADigit, CommandParseException.NotAtStartOfArg, CommandParseException.EndOfArgument, CommandParseException.NotAtEndOfArg {
        var x = ctx.readDouble();
        var y = ctx.readDouble();
        var z = ctx.readDouble();
        var xRot = ctx.readFloat();
        var yRot = ctx.readFloat();
        return new Location(x, y, z, xRot, yRot);
    }

    private Location readDimLocation(CommandReader ctx) throws CommandParseException {
        var x = ctx.readDouble();
        var y = ctx.readDouble();
        var z = ctx.readDouble();
        var dimName = ctx.readId();
        var dimension = UltracraftServer.get().getWorld(dimName);
        if (dimension == null) throw new CommandParseException.NotFound("dimension", ctx.getOffset());
        return new Location(dimension, x, y, z);
    }

    private Location readDimLocationRot(CommandReader ctx) throws CommandParseException {
        var x = ctx.readDouble();
        var y = ctx.readDouble();
        var z = ctx.readDouble();
        var xRot = ctx.readFloat();
        var yRot = ctx.readFloat();
        var dimName = ctx.readId();
        var dimension = UltracraftServer.get().getWorld(dimName);
        if (dimension == null) throw new CommandParseException.NotFound("dimension", ctx.getOffset());
        return new Location(dimension, x, y, z, xRot, yRot);
    }

    private static List<String> completeGamemode(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.strings(ctx.readString(), "survival", "mini_game", "builder", "builder_plus", "spectator");
    }

    private static List<String> completeBiome(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return CommandData.complete(ctx, Registries.BIOME);
    }

    private static List<String> completeBlocks(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.blocks(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeItems(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.items(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completePotions(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) {
        return Lists.newArrayList();
    }

    private static List<String> completeAttribute(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return CommandData.complete(ctx, Registries.ATTRIBUTE);
    }

    private static List<String> completeSounds(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return CommandData.complete(ctx, Registries.SOUND_EVENT);
    }

    static <T> List<String> complete(CommandReader ctx, Registry<T> registry) throws CommandParseException {
        var currentArgument = ctx.readString();
        List<String> list = new ArrayList<>();
        for (var id : registry.ids()) {
            try {
                TabCompleting.addIfStartsWith(list, id, currentArgument);
            } catch (Exception ignored) {

            }
        }
        return list;
    }

    private static List<String> completeItemStackRef(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] strings) throws CommandParseException {
        return ItemBaseSelector.tabComplete(sender, commandCtx, ctx.readString());
    }

    private static List<String> completeHex(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.hex(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeInts(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.ints(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeFloats(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.doubles(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeBooleans(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.booleans(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeAnyPlayer(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return AnyPlayerBaseSelector.tabComplete(sender, commandCtx, ctx.readString());
    }

    private List<@Nullable String> completeDuration(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        var currentArgument = ctx.readString();
        List<String> list = new ArrayList<>();
        list.add(currentArgument);
        var parts = CommandData.dropLastWhile(List.of(currentArgument.split(":")), String::isEmpty);
        if (parts.size() > 4) {
            return List.of();
        }
        if (parts.size() > 1) {
            if (parts.getLast().length() > 2) {
                return List.of("$currentArgument:");
            } else if (!parts.getLast().isEmpty()) {
                list.add(":");
            }
        } else if (!parts.getLast().isEmpty()) {
            list.add(":");
        }
        for (var i : new Range(0, 9)) {
            list.add(currentArgument + i);
        }
        return list;
    }

    private static List<String> completeTime(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        var currentArgument = ctx.readString();
        List<String> list = new ArrayList<>();
        list.add(currentArgument);
        var parts = CommandData.dropLastWhile(List.of(currentArgument.split(":")), String::isEmpty);
        if (parts.size() > 3) {
            return List.of();
        }
        if (parts.getLast().length() > 2) {
            return List.of("$currentArgument:");
        } else if (!parts.getLast().isEmpty()) {
            list.add(":");
        }
        for (var i : new Range(0, 9)) {
            list.add(currentArgument + i);
        }
        return list;
    }

    private static List<String> completeDate(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        var currentArgument = ctx.readString();
        List<String> list = new ArrayList<>();
        list.add(currentArgument);
        var parts = CommandData.dropLastWhile(List.of(currentArgument.split("-")), String::isEmpty);
        if (parts.size() > 3) {
            return List.of();
        }
        if (parts.size() > 1) {
            if (parts.getLast().length()     > 2) {
                return List.of("$currentArgument:");
            } else if (!parts.getLast().isEmpty()) {
                list.add("-");
            }
        } else if (!parts.getLast().isEmpty()) {
            list.add("-");
        }
        for (var i : new Range(0, 9)) {
            list.add(currentArgument + i);
        }
        return list;
    }

    private static List<String> completeDateTime(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        var date = ctx.readString();
        List<String> list = new ArrayList<>();
        list.add(date);
        {
            var parts = CommandData.dropLastWhile(List.of(date.split("-")), String::isEmpty);
            if (parts.size() > 3) {
                return List.of(" ");
            }
            if (parts.size() > 1) {
                if (parts.getLast().length() > 2) {
                    return List.of("$date:");
                } else if (!parts.getLast().isEmpty()) {
                    list.add("-");
                }
            } else if (!parts.getLast().isEmpty()) {
                list.add("-");
            }
            for (var i : new Range(0, 9)) {
                list.add(date + i);
            }
        }
        if (!ctx.isAtEndOfCmd()) {
            var time = ctx.readString();
            list.add(time);
            var parts = CommandData.dropLastWhile(List.of(time.split(":")), String::isEmpty);
            if (parts.size() > 3) {
                return List.of();
            }
            if (parts.getLast().length()     > 2) {
                return List.of("$time:");
            } else if (!parts.getLast().isEmpty()) {
                list.add(":");
            }
            for (var i : new Range(0, 9)) {
                list.add(time + i);
            }
            return list;
        }
        return list;
    }

    public static <T> List<T> dropLastWhile(List<T> list, Predicate<T> o) {
        if (list == null || list.isEmpty()) return null;
        ListIterator<T> iterator = list.listIterator(list.size());
        while (iterator.hasNext()) {
            if (!o.test(iterator.next())) break;
            iterator.remove();
        }
        return list;
    }

    private static List<String> completePosition(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        var arg = ctx.readString();
        if (ctx.isAtEndOfCmd()) {
            return TabCompleting.doubles(new ArrayList<>(), arg);
        }
        arg = ctx.readString();
        if (ctx.isAtEndOfCmd()) {
            return TabCompleting.doubles(new ArrayList<>(), arg);
        }
        arg = ctx.readString();
        return TabCompleting.doubles(new ArrayList<>(), arg);
    }

    private static List<String> completeOfflinePlayers(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return OfflinePlayerBaseSelector.tabComplete(sender, commandCtx, ctx.readString());
    }

    private static List<String> completeOnlinePlayers(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return PlayerBaseSelector.tabComplete(sender, commandCtx, ctx.readString());
    }

    private static List<String> completeEntities(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return EntityBaseSelector.tabComplete(Entity.class, sender, commandCtx, ctx.readString());
    }

    private static List<String> completeLivingEntities(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return EntityBaseSelector.tabComplete(LivingEntity.class, sender, commandCtx, ctx.readString());
    }

    private static List<String> completeCommand(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) {
        return List.of();
    }

    private static List<String> completeCommandSender(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return CommandSenderBaseSelector.tabComplete(sender, commandCtx, ctx.readString());
    }

    private static List<String> completeWeather(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.strings(ctx.readString(), "clear", "sunny", "rain", "downfall", "storm", "thunder");
    }

    private static List<String> completeDimensions(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.worlds(new ArrayList<>(), ctx.readString());
    }

    private static List<String> completeNumberRules(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.strings(ctx.readString(), (String[]) UltracraftServer.get().getGameRules().getRules().stream().map(Rule::getKey).toArray());
    }

    private static List<String> completeEntityTypes(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.entityTypes(new ArrayList<>(), ctx.readString(), true);
    }

    private static List<String> completeEntityTypesExceptPlayer(CommandSender sender, CommandContext commandCtx, CommandReader ctx, String[] args) throws CommandParseException {
        return TabCompleting.entityTypes(new ArrayList<>(), ctx.readString(), false);
    }

    public Set<CommandSpec> getOverloadSpecs() {
        return this.overloads0.keySet();
    }
}