package com.ultreon.craft.api.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.ultreon.craft.api.commands.selector.SelectorKey;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.gamerule.Rule;
import com.ultreon.craft.registry.CommandRegistry;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.CacheablePlayer;
import com.ultreon.craft.util.Difficulty;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.World;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class TabCompleting {
    public static List<String> onlinePlayers(String currentArgument) {
        return TabCompleting.onlinePlayers(new ArrayList<>(), currentArgument);
    }

    public static List<String> onlinePlayers(List<String> list, String currentArgument) {
        for (var player : UltracraftServer.get().getPlayers()) {
            TabCompleting.addIfStartsWith(list, player.getName(), currentArgument);
        }
        return list;
    }

    public static List<String> players(String currentArgument) {
        return TabCompleting.players(new ArrayList<>(), currentArgument);
    }

    public static List<String> players(List<String> list, String currentArgument) {
        List<CacheablePlayer> players = new ArrayList<>();
        players.addAll(UltracraftServer.get().getCachedPlayers());
        players.addAll(UltracraftServer.get().getPlayers());
        for (var player : players) {
            var name = player.getName();
            if (name != null) {
                TabCompleting.addIfStartsWith(list, name, currentArgument);
            }
        }
        return list;
    }

    public static List<String> offlinePlayers(List<String> list, String currentArgument) {
        var players = Lists.newArrayList(UltracraftServer.get().getCachedPlayers());
        for (var player : players) {
            var name = player.getName();
            if (name != null) {
                TabCompleting.addIfStartsWith(list, name, currentArgument);
            }
        }
        return list;
    }

    public static List<String> offlinePlayerUuids(List<String> list, String currentArgument) {
        for (var player : UltracraftServer.get().getCachedPlayers()) {
            UUID uuid = player.getUuid();

            if (uuid != null) {
                TabCompleting.addIfStartsWith(list, uuid.toString(), currentArgument);
            }
        }
        return list;
    }

    public static <T extends Rule<?>> List<String> ruleNames(List<String> list, List<T> rules, String currentArgument) {
        for (Rule<?> rule : rules) {
            TabCompleting.addIfStartsWith(list, rule.getKey(), currentArgument);
        }
        return list;
    }

    public static List<String> entityTypes(List<String> list, String currentArgument) {
        return entityTypes(list, currentArgument, true);
    }

    public static List<String> entityTypes(List<String> list, String currentArgument, boolean includePlayer) {
        for (var entityType : Registries.ENTITY_TYPE.entries()) {
            var key = entityType.getKey();
            if (!includePlayer && entityType.getKey().equals(Registries.ENTITY_TYPE.getKey(EntityTypes.PLAYER)))
                continue;

            TabCompleting.addIfStartsWith(list, key, currentArgument);
        }
        return list;
    }

    public static List<String> biomes(List<String> list, String currentArgument) {
        for (var biome : Registries.BIOME.ids()) {
            var key = biome.toString();
            TabCompleting.addIfStartsWith(list, key, currentArgument);
        }
        return list;
    }

    public static List<String> difficulties(List<String> list, String currentArgument) {
        for (Difficulty difficulty : Difficulty.values()) {
            String name = difficulty.name().toLowerCase();
            TabCompleting.addIfStartsWith(list, name, currentArgument);
        }
        return list;
    }

    public static List<String> entityUuids(List<String> list, String currentArgument) {
        for (World world : UltracraftServer.get().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String uuid = entity.getUuid().toString();
                TabCompleting.addIfStartsWith(list, uuid, currentArgument);
            }
        }
        return list;
    }

    public static List<String> entityUuids(List<String> list, String currentArgument, Class<? extends Entity> instance) {
        for (World world : UltracraftServer.get().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!instance.isInstance(entity)) {
                    continue;
                }
                String uuid = entity.getUuid().toString();
                TabCompleting.addIfStartsWith(list, uuid, currentArgument);
            }
        }
        return list;
    }

    public static List<String> blocks(List<String> list, String currentArgument) {
        for (Identifier id : Registries.BLOCK.ids()) {
            TabCompleting.addIfStartsWith(list, id, currentArgument);
        }
        return list;
    }

    public static List<String> items(List<String> list, String currentArgument) {
        for (Identifier id : Registries.ITEM.ids()) {
            TabCompleting.addIfStartsWith(list, id, currentArgument);
        }
        return list;
    }

    public static List<String> keys(String currentArgument, Collection<Identifier> keys) {
        List<String> list = new ArrayList<>();
        for (var key : keys) {
            TabCompleting.addIfStartsWith(list, key.toString(), currentArgument);
        }
        return list;
    }

    public static List<String> worlds(List<String> list, String currentArgument) {
        for (World world : UltracraftServer.get().getWorlds()) {
            Identifier id = world.getDimension().getId();
            TabCompleting.addIfStartsWith(list, id, currentArgument);
        }
        return list;
    }

    public static List<String> worldIds(List<String> list, String currentArgument) {
        for (World world : UltracraftServer.get().getWorlds()) {
            String uuid = world.getUID().toString();
            TabCompleting.addIfStartsWith(list, uuid, currentArgument);
        }
        return list;
    }

    public static List<String> strings(String currentArgument, String... strings) {
        return TabCompleting.strings(new ArrayList<>(), currentArgument, strings);
    }

    public static List<String> strings(String currentArgument, char... chars) {
        return TabCompleting.strings(new ArrayList<>(), currentArgument, TabCompleting.forceToString(chars));
    }

    public static List<String> strings(List<String> list, String currentArgument, String... strings) {
        for (var string : strings) {
            TabCompleting.addIfStartsWith(list, string, currentArgument);
        }
        return list;
    }

    public static List<String> strings(List<String> list, String currentArgument, char... chars) {
        for (var c : chars) {
            TabCompleting.addIfStartsWith(list, String.valueOf(c), currentArgument);
        }
        return list;
    }

    public static List<String> doubles(List<String> list, String currentArgument) {
        list.add(currentArgument);
        if (!currentArgument.isEmpty() && !currentArgument.contains(".")) {
            list.add(currentArgument + ".");
        }
        if (currentArgument.startsWith("0") && !currentArgument.startsWith("0.")) return list;
        for (var i = 0; i <= 9; i++) {
            list.add(currentArgument + i);
        }
        return list;
    }

    public static List<String> ints(List<String> list, String currentArgument) {
        if (!currentArgument.isEmpty()) {
            list.add(currentArgument);
        }
        if (currentArgument.startsWith("0")) return list;
        for (var i = 0; i <= 9; i++) {
            list.add(currentArgument + i);
        }
        return list;
    }

    public static List<String> hex(List<String> list, String currentArgument) {
        list.add(currentArgument);
        for (var c : "0123456789abcdef".toCharArray()) {
            list.add(currentArgument + c);
        }
        return list;
    }

    public static List<String> booleans(List<String> list, String currentArgument) {
        TabCompleting.addIfStartsWith(list, "true", currentArgument);
        TabCompleting.addIfStartsWith(list, "on", currentArgument);
        TabCompleting.addIfStartsWith(list, "yes", currentArgument);
        TabCompleting.addIfStartsWith(list, "enable", currentArgument);
        TabCompleting.addIfStartsWith(list, "false", currentArgument);
        TabCompleting.addIfStartsWith(list, "off", currentArgument);
        TabCompleting.addIfStartsWith(list, "no", currentArgument);
        TabCompleting.addIfStartsWith(list, "disable", currentArgument);
        return list;
    }

    public static List<String> mods(List<String> list, String currentArgument) {
        FabricLoader manager = FabricLoader.getInstance();
        for (ModContainer plugin : manager.getAllMods()) {
            TabCompleting.addIfStartsWith(list, plugin.getMetadata().getId(), currentArgument);
        }
        return list;
    }

    public static List<String> commands(List<String> list, String currentArgument) {
        CommandRegistry.getCommandNames().forEach(s -> TabCompleting.addIfStartsWith(list, s, currentArgument));
        return list;
    }

    public static List<String> subCommand(List<String> list, CommandSender sender, String commandName, String... commandArgs) {
        Command command = CommandRegistry.get(commandName);
        if (command == null) return list;

        List<String> options = command.onTabComplete(sender, new CommandContext(commandName), commandName, commandArgs);
        if (options == null) return list;

        list.addAll(options);
        return list;
    }

    public static List<String> selectors(List<String> list, SelectorKey filterKey, String currentArgument, Collection<String> values) {
        return TabCompleting.selectors(list, filterKey, currentArgument, values.toArray(new String[]{}));
    }

    public static List<String> selectors(SelectorKey filterKey, String currentArgument, String... values) {
        return TabCompleting.selectors(new ArrayList<>(), filterKey, currentArgument, values);
    }

    public static List<String> selectors(SelectorKey filterKey, String currentArgument, Collection<String> values) {
        return TabCompleting.selectors(new ArrayList<>(), filterKey, currentArgument, values);
    }

    public static List<String> selectors(List<String> list, SelectorKey filterKey, String currentArgument, String... values) {
        if (currentArgument.isEmpty()) {
            TabCompleting.addIfStartsWith(list, filterKey.symbol(), currentArgument);
            return list;
        }
        for (var value : values) {
            TabCompleting.addIfStartsWith(list, filterKey.symbol() + value, currentArgument);
        }
        return list;
    }

    public static List<String> prefixed(List<String> list, String prefix, String currentArgument, Collection<String> values) {
        return TabCompleting.prefixed(list, prefix, currentArgument, values.toArray(new String[]{}));
    }

    public static List<String> prefixed(String prefix, String currentArgument, String... values) {
        return TabCompleting.prefixed(new ArrayList<>(), prefix, currentArgument, values);
    }

    public static List<String> prefixed(String prefix, String currentArgument, Collection<String> values) {
        return TabCompleting.prefixed(new ArrayList<>(), prefix, currentArgument, values);
    }

    public static List<String> prefixed(List<String> list, String prefix, String currentArgument, String... values) {
        if (currentArgument.isEmpty()) {
            TabCompleting.addIfStartsWith(list, prefix, currentArgument);
            return list;
        }
        for (var value : values) {
            TabCompleting.addIfStartsWith(list, prefix + value, currentArgument);
        }
        return list;
    }

    public static List<String> numbers(String currentArgument, Number... numbers) {
        return TabCompleting.numbers(new ArrayList<>(), currentArgument, numbers);
    }

    public static List<String> numbers(List<String> list, String currentArgument, Number... numbers) {
        return TabCompleting.numbers(list, currentArgument, 10, numbers);
    }

    public static List<String> numbers(List<String> list, String currentArgument, int radix, Number... numbers) {
        for (var number : numbers) {
            if (number.toString().contains(currentArgument)) {
                list.add(Long.toUnsignedString(number.longValue(), radix));
            }
        }
        return list;
    }

    public static String[] forceToString(char... chars) {
        return (String[]) Stream.of(chars).map(String::valueOf).toArray();
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull String text, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(text, "text");
        Preconditions.checkNotNull(startsWith, "startsWith");
        if (text.startsWith(startsWith)) {
            list.add(text);
        }

    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull UUID uuid, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(startsWith, "startsWith");

        var text = uuid.toString();
        if (text.toLowerCase(Locale.getDefault()).startsWith(startsWith)) {
            list.add(text);
        }
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull Identifier id, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(startsWith, "startsWith");

        if (id.path().startsWith(startsWith)) {
            list.add(id.toString());
        } else {
            if (id.toString().startsWith(startsWith)) {
                list.add(id.toString());
            }
        }
    }

    public static void addIfStartsWith(@NotNull Collection<String> list, @NotNull Object obj, @NotNull String startsWith) {
        Preconditions.checkNotNull(list, "list");
        Preconditions.checkNotNull(obj, "obj");
        Preconditions.checkNotNull(startsWith, "startsWith");
        var text = obj.toString();
        if (!text.contains(" ") && text.startsWith(startsWith)) {
            list.add(text);
        }
    }
}