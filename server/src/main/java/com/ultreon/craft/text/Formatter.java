package com.ultreon.craft.text;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.registry.CustomKeyRegistry;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.apache.commons.lang3.CharUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Formatter {
    private final boolean allowFormatting;
    private final boolean doPing;
    private final String message;
    private final TextObject prefix;
    private final TextObject textPrefix;
    private final @Nullable Player sender;
    private MutableText builder = MutableText.literal("");

    // Predicates
    private final CharPredicate emotePredicate = it -> CharUtils.isAsciiAlphanumeric(it) || "_-".contains(Character.toString(it));

    // Redirect
    private @MonotonicNonNull ParseResult redirectValue = null;
    private boolean redirect = false;

    // Reader
    private int offset = 0;

    // Colors
    private final Color messageColor;

    // Formatting
    private StringBuilder currentBuilder = new StringBuilder();
    private Color currentColor;
    private @Nullable ClickEvent currentClickEvent = null;
    private @Nullable HoverEvent<?> currentHoverEvent = null;
    private final @Nullable Identifier currentFont = null;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean strikethrough = false;

    // Flags
    private final @MonotonicNonNull ParseResult parsed = null;

    // Pings
    private final List<Player> pinged = new ArrayList<>();

    // Locks
    private final Object lock = new Object();

    public Formatter(boolean allowFormatting, boolean doPing, String message, TextObject prefix, TextObject textPrefix, @Nullable Player sender, Color defaultMessageColor) {
        this.allowFormatting = allowFormatting;
        this.doPing = doPing;
        this.message = message;
        this.prefix = prefix;
        this.textPrefix = textPrefix;
        this.sender = sender;
        this.messageColor = defaultMessageColor;
        this.currentColor = this.messageColor;
    }

    public ParseResult parse() {
        return this.parse0();
    }

    private ParseResult parse0() {
        if (this.allowFormatting) {
            while (this.offset < this.message.length()) {
                switch (this.c()) {
                    case '&' -> {
                        this.parseId();
                        continue;
                    }

                    case '@' -> {
                        this.parseMention();
                        continue;
                    }

                    case '<' -> {
                        this.parseColor();
                        continue;
                    }

                    case '{' -> {
                        this.parseFunction();
                        continue;
                    }

                    case '%' -> {
                        this.parseKey();
                        continue;
                    }

//                    case ':' -> {
//                        this.parseEmote();
//                        continue;
//                    }

                    case '!' -> {
                        if (this.sender == null) {
                            this.currentBuilder.append(this.c());
                            if (this.redirect) {
                                this.redirect = false;
                                return this.redirectValue;
                            }
                            this.offset++;
                            continue;
                        }
                        if (this.offset + 1 == this.message.length()) {
                            this.currentBuilder.append(this.c());
                            this.offset++;
                            continue;
                        }
                        this.offset++;
                        if (this.c() == '(') {
                            this.parseIcon();
                        }
                        continue;
                    }

                    default -> this.currentBuilder.append(this.c());
                }
                if (this.redirect) {
                    this.redirect = false;
                    return this.redirectValue;
                }
                this.offset++;
            }
        } else {
            var offset = 0;
            while (offset < this.message.length()) {
                var c = this.message.charAt(offset);
                if (c == '@') {
                    offset++;
                    final var name = new StringBuilder();
                    while (true) {
                        c = this.message.charAt(offset);
                        if (!("_-".contains(Character.toString(c)) || CharUtils.isAsciiAlphanumeric(c))) break;
                        offset++;
                        if (offset >= this.message.length()) {
                            break;
                        }
                        name.append(c);
                    }
                    final var player = UltracraftServer.get().getPlayer(name.toString());
                    if (player != null && !name.toString().isEmpty() && Objects.equals(player.getName(), name.toString()) && this.doPing) {
                        this.pushBuilder();
                        this.addPingText(player);
                    } else {
                        this.currentBuilder.append("@").append(name);
                    }
                    continue;
                } else {
                    this.currentBuilder.append(c);
                }
                if (this.redirect) {
                    this.redirect = false;
                    return this.redirectValue;
                }
                offset++;
            }
        }
        final var s = this.currentBuilder.toString();
        final var textObj =
                TextObject.literal(s).style(style -> style.color(this.currentColor).bold(this.bold)
                        .italic(this.italic).underline(this.underlined)
                        .strikethrough(this.strikethrough));
        this.builder.append(textObj);
        return new ParseResult(this.pinged, this.prefix, this.textPrefix, this.builder);
    }

    public static TextObject format(String message) {
        return Formatter.format(message, false);
    }

    public static TextObject format(String message, boolean doPing) {
        var formatter = new Formatter(true, doPing, message, TextObject.empty(), TextObject.empty(), null, Color.WHITE);
        var parse = formatter.parse();
        return parse.getResult();
    }

    private void parseEmote() {
        // TODO: Add emote parsing system. (Currently broken)
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                this.currentBuilder.append(':');
                this.pushBuilder();

                this.redirect(String.join("", new String(characters.toCharArray())));
                return;
            }
            characters.add(this.c());
        } while (this.c() != ':' && this.emotePredicate.test(this.c()));

        if (this.c() != ':') {
            this.currentBuilder.append(':');
            this.pushBuilder();

            var toFormat = new String(characters.toCharArray());
            this.redirect(toFormat);
            return;
        }

        this.offset++;

        var arg = new String(characters.toCharArray()).substring(0, characters.size() - 1);
        this.pushBuilder();

        if (EmoteMap.get(arg) != null) {
            var texture = TextObject.literal(String.valueOf(EmoteMap.get(arg).getChar())).style(style ->
                style.font(EmoteMap.get(arg).getFont())
                        .color(Color.rgb(0xffffff)));
            this.builder.append(texture);
        }
    }

    private void parseIcon() {
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            characters.add(this.c());
        } while (this.c() != ')');

        this.offset++;

        var arg = String.join(new String(characters.toCharArray())).substring(0, characters.size() - 1);
        this.pushBuilder();

        if (IconMap.get(arg) != null) {
            var texture = TextObject.literal(String.valueOf(IconMap.get(arg).getChar()))
                    .style(style -> style.font(IconMap.get(arg).getFont())
                            .color(Color.rgb(0xffffff)));
            this.builder.append(texture);
        }
    }

    public void parseId() {
        this.offset++;
        if (this.offset >= this.message.length()) return;
        switch (this.c()) {
            case '#' -> {
                this.pushBuilder();
                this.currentColor = Color.rgb(Integer.parseInt(this.message.substring(this.offset + 1, this.offset + 7), 16));
                this.offset += 7;
            }
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F' -> {
                this.pushBuilder();
                this.currentColor = Color.of(ChatColor.getByChar(Character.toLowerCase(this.c())));
                this.offset++;
            }
            case 'l' -> {
                this.bold = true;
                this.offset++;
            }
            case 'm' -> {
                this.strikethrough = true;
                this.offset++;
            }
            case 'n' -> {
                this.underlined = true;
                this.offset++;
            }
            case 'o' -> {
                this.italic = true;
                this.offset++;
            }
            case 'r' -> {
                this.pushBuilder();
                this.currentColor = this.messageColor;
                this.bold = false;
                this.italic = false;
                this.underlined = false;
                this.strikethrough = false;
                this.currentClickEvent = null;
                this.currentHoverEvent = null;
                this.offset++;
            }
            default -> {
                this.currentBuilder.append(this.c());
                this.offset++;
            }
        }
    }

    private void parseKey() {
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            characters.add(this.c());
        } while (this.c() != '%');

        this.offset++;

        var key = String.valueOf(characters).replace("%", "");
        this.pushBuilder();

        switch (key) {
            case "message-type", "username" -> {
                if (this.sender != null) {
                    this.currentBuilder.append(this.sender.getName());
                }
            }
            case "player" -> {
                if (this.sender != null) {
                    this.pushBuilder();

                    String hoverText = this.getPlayerHoverText(this.sender);
                    TextObject textObj2 = TextObject.literal(this.sender.getName()).style(style -> style
                            .color(this.currentColor)
                            .bold(this.bold)
                            .italic(this.italic)
                            .underline(this.underlined)
                            .strikethrough(this.strikethrough)
                            .hoverEvent(HoverEvent.text(new Formatter(true, false, hoverText, TextObject.empty(), TextObject.empty(), null, Color.WHITE).parse().getResult()))
                            .clickEvent(this.currentClickEvent));
                    this.builder.append(textObj2);
                    this.currentBuilder.append(this.sender.getName());
                }
            }
            case "console-sender" -> this.currentBuilder.append("Console");
            case "time" -> this.currentBuilder.append(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            case "date" -> this.currentBuilder.append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            default -> {
                var identifier = Identifier.tryParse(key);
                if (identifier == null) break;
                var textKey = CustomKeyRegistry.get(identifier);
                if (textKey == null) break;
                this.currentBuilder.append(textKey.get(this.sender));
            }
        }
    }

    private void parseFunction() {
        CharList characters = new CharArrayList();
        var type = "";

        var mode = Mode.TYPE;
        List<String> arguments = new ArrayList<>();
        var currentArg = new StringBuilder();
        var ignoreSpaces = true;
        while (this.c() != '}') {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            if (ignoreSpaces) {
                if (this.c() == ' ') continue;
                ignoreSpaces = false;
            }
            if (mode == Mode.TYPE) {
                switch (this.c()) {
                    case ':':
                        type = new String(characters.toCharArray()).replaceAll("[:}]", "");
                        characters.clear();
                        mode = Mode.BODY;
                        ignoreSpaces = true;
                        continue;
                    case '}':
                        return;
                }
            }
            if (mode == Mode.BODY) {
                if (this.c() == '\\') {
                    this.offset++;
                    if (this.offset >= this.message.length()) {
                        return;
                    }
                } else if (this.c() == ';') {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            }
            characters.add(this.c());
        }
        arguments.add(currentArg.toString());

        this.pushBuilder();

        switch (type) {
            case "click":
                var actionName = arguments.remove(0);
                ClickEvent event = null;
                switch (actionName) {
                    case "@", "web", "url", "open-url" -> {
                        if (arguments.isEmpty()) return;
                        var url = arguments.remove(0);
                        try {
                            event = ClickEvent.openUrl(new URL(url));
                        } catch (MalformedURLException ignored) {

                        }
                    }
                    case "#", "clip", "clipboard", "copy", "cp", "copy-to-clipboard" -> {
                        if (arguments.isEmpty()) return;
                        var text = arguments.remove(0);
                        event = ClickEvent.copyToClipboard(text);
                    }
                    case "/", "cmd", "command" -> {
                        if (arguments.isEmpty()) return;
                        var cmd = arguments.remove(0);
                        event = ClickEvent.runCommand(cmd);
                    }
                    case ">", "suggest", "suggest-msg", "put-msg", "put", "example", "example-msg" -> {
                        if (arguments.isEmpty()) return;
                        var cmd = arguments.remove(0);
                        event = ClickEvent.suggestMessage(cmd);
                    }
                    default -> {
                        if (actionName.startsWith("@")) {
                            var url = actionName.substring(1);
                            try {
                                event = ClickEvent.openUrl(new URL(url));
                            } catch (MalformedURLException ignored) {

                            }
                        }
                        if (actionName.startsWith("#")) {
                            var text = actionName.substring(1);
                            event = ClickEvent.copyToClipboard(text);
                        }
                        if (actionName.startsWith("/")) {
                            var cmd = actionName.substring(1);
                            event = ClickEvent.runCommand(cmd);
                        }
                        if (actionName.startsWith(">")) {
                            var cmd = actionName.substring(1);
                            event = ClickEvent.suggestMessage(cmd);
                        }
                    }
                }
                this.currentClickEvent = event;
                break;

            case "hover":
                this.currentHoverEvent = HoverEvent.text(
                        new Formatter(
                                true,
                                false,
                                arguments.remove(0),
                                TextObject.empty(),
                                this.textPrefix,
                                null,
                                this.messageColor
                        ).parse().getResult()
                );
                break;
        }
        this.offset++;
    }

    private void parseMention() {

        this.offset++;

        var name = new StringBuilder();
        while (true) {
            if (!("_-".contains(String.valueOf(this.c())) || Character.isLetterOrDigit(this.c())))
                break;

            this.offset++;
            name.append(this.c());
            if (this.offset >= this.message.length()) {
                break;
            }
        }

        var player = UltracraftServer.get().getPlayer(name.toString());

        if (player != null && !name.toString().isEmpty() && player.getName().contentEquals(name) && this.doPing) {
            this.pushBuilder();

            var hoverText = String.format(
                    "<blue>%s\n<gray>Name <i>%s</i>\n<dark-gray>%s".trim(),
                    ChatColor.stripColor(player.getPublicName()),
                    player.getName(),
                    player.getUuid()
            );

            TextObject textComponent1 = TextObject.literal(
                    "@" + player.getName()).style(style -> style.color(Color.of(ChatColor.BLUE))
                    .bold(false)
                    .italic(false)
                    .underline(true)
                    .strikethrough(false)
                    .hoverEvent(HoverEvent.text(new Formatter(true, false, hoverText, TextObject.empty(), TextObject.empty(), null, Color.WHITE).parse().getResult()))
                    .clickEvent(ClickEvent.suggestMessage("@" + player.getName())));

            this.builder.append(textComponent1);
            this.pinged.add(player);

        } else {
            this.currentBuilder.append("@").append(name);
        }
    }

    public void parseColor() {
        var characters = new CharArrayList();

        while (this.c() != '>') {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            characters.push(this.c());
        }

        var arg = new String(characters.toCharArray()).replace(">", "");

        this.pushBuilder();

        switch (arg) {
            case "/mc/", "/message/", "/message-color/" -> this.currentColor = this.messageColor;
            case "b", "bold", "fat", "%" -> this.bold = true;
            case "/b", "/bold", "/fat", "/%" -> this.bold = false;
            case "i", "italic", "+" -> this.italic = true;
            case "/i", "/italic", "/+" -> this.italic = false;
            case "u", "underlined", "underline", "_" -> this.underlined = true;
            case "/u", "/underlined", "/underline", "/_" -> this.underlined = false;
            case "s", "strikethrough", "st", "-" -> this.strikethrough = true;
            case "/s", "/strikethrough", "/st", "/-" -> this.strikethrough = false;
            case "/", "/*", "r", "reset", "clear" -> {
                this.bold = false;
                this.italic = false;
                this.underlined = false;
                this.strikethrough = false;
                this.currentColor = this.messageColor;
                this.currentClickEvent = null;
                this.currentHoverEvent = null;
            }

            case "red" -> this.currentColor = Color.of(ChatColor.RED);
            case "yellow" -> this.currentColor = Color.of(ChatColor.YELLOW);
            case "lime", "green" -> this.currentColor = Color.of(ChatColor.GREEN);
            case "cyan", "aqua" -> this.currentColor = Color.of(ChatColor.AQUA);
            case "blue" -> this.currentColor = Color.of(ChatColor.BLUE);
            case "magenta", "light-purple" -> this.currentColor = Color.of(ChatColor.LIGHT_PURPLE);
            case "dark-red" -> this.currentColor = Color.of(ChatColor.DARK_RED);
            case "gold" -> this.currentColor = Color.of(ChatColor.GOLD);
            case "dark-green" -> this.currentColor = Color.of(ChatColor.DARK_GREEN);
            case "turquoise", "dark-aqua" -> this.currentColor = Color.of(ChatColor.DARK_AQUA);
            case "dark-blue" -> this.currentColor = Color.of(ChatColor.DARK_BLUE);
            case "purple", "dark-purple" -> this.currentColor = Color.of(ChatColor.DARK_PURPLE);
            case "gray-16", "white" -> this.currentColor = Color.of(ChatColor.WHITE);
            case "gray-15" -> this.currentColor = Color.rgb(0xf0f0f0);
            case "gray-14" -> this.currentColor = Color.rgb(0xe0e0e0);
            case "gray-13" -> this.currentColor = Color.rgb(0xd0d0d0);
            case "gray-12", "light-gray" -> this.currentColor = Color.rgb(0xc0c0c0);
            case "gray-11" -> this.currentColor = Color.rgb(0xb0b0b0);
            case "gray-10" -> this.currentColor = Color.rgb(0xa0a0a0);
            case "gray", "silver" -> this.currentColor = Color.of(ChatColor.GRAY);
            case "gray-9" -> this.currentColor = Color.rgb(0x909090);
            case "gray-8" -> this.currentColor = Color.rgb(0x808080);
            case "mid-gray", "gray-7" -> this.currentColor = Color.rgb(0x707070);
            case "gray-6" -> this.currentColor = Color.rgb(0x606060);
            case "dark-gray" -> this.currentColor = Color.of(ChatColor.DARK_GRAY);
            case "gray-5" -> this.currentColor = Color.rgb(0x505050);
            case "gray-4" -> this.currentColor = Color.rgb(0x404040);
            case "darker-gray", "gray-3" -> this.currentColor = Color.rgb(0x303030);
            case "gray-2" -> this.currentColor = Color.rgb(0x202020);
            case "gray-1" -> this.currentColor = Color.rgb(0x101010);
            case "gray-0" -> this.currentColor = Color.rgb(0x000000);
            case "black" -> this.currentColor = Color.of(ChatColor.BLACK);
            case "brown" -> this.currentColor = Color.rgb(0x614E36);
            case "azure" -> this.currentColor = Color.rgb(0x007FFF);
            case "mint" -> this.currentColor = Color.rgb(0x00FF7F);
            case "orange" -> this.currentColor = Color.rgb(0xFF7F00);
            case "pure-yellow" -> this.currentColor = Color.rgb(0xFFFF00);
            case "yellow-gold" -> this.currentColor = Color.rgb(0xFFD500);
            case "pure-gold" -> this.currentColor = Color.rgb(0xFFC500);
            case "dark-yellow" -> this.currentColor = Color.rgb(0x7F7F00);
            case "method" -> this.currentColor = Color.rgb(0x61AFEF);
            case "string-escape" -> this.currentColor = Color.rgb(0x2BBAC5);
            case "string" -> this.currentColor = Color.rgb(0x89CA78);
            case "class" -> this.currentColor = Color.rgb(0xE5C07B);
            case "number" -> this.currentColor = Color.rgb(0xD19A66);
            case "enum-value" -> this.currentColor = Color.rgb(0xEF596F);
            case "keyword" -> this.currentColor = Color.rgb(0xD55FDE);
            default -> {

            }
        }

        this.offset++;
    }

    private void redirect(String message) {
        this.redirect = true;
        var it = new Formatter(
                this.allowFormatting,
                this.doPing, message,
                this.builder,
                this.textPrefix,
                this.sender,
                this.messageColor
        );
        it.currentColor = this.currentColor;
        it.bold = this.bold;
        it.italic = this.italic;
        it.underlined = this.underlined;
        it.strikethrough = this.strikethrough;
        it.currentHoverEvent = this.currentHoverEvent;
        it.currentClickEvent = this.currentClickEvent;
        it.currentBuilder = this.currentBuilder;
        this.redirectValue = it.parse();
    }

    private void pushBuilder() {
        LiteralText obj = TextObject.literal(this.currentBuilder.toString()).style(style -> style
                .color(this.currentColor)
                .bold(this.bold)
                .italic(this.italic)
                .underline(this.underlined)
                .strikethrough(this.strikethrough)
                .hoverEvent(this.currentHoverEvent)
                .clickEvent(this.currentClickEvent));

        if (this.builder == null) this.builder = obj;
        this.builder.append(obj);
        this.currentBuilder = new StringBuilder();
    }

    public char c() {
        return this.message.charAt(this.offset);
    }

    private void addPingText(ServerPlayer player) {
        // Set hover text.
        final var hoverText = this.getPlayerHoverText(player);
        final var textComponent1 = TextObject.literal(
                "@" + player.getName()).style(style -> style
                .color(Color.of(ChatColor.BLUE))
                .bold(false)
                .italic(false)
                .underline(true)
                .strikethrough(false)
                .hoverEvent(HoverEvent.text(Formatter.format(hoverText, false)))
                .clickEvent(null));

        // Add components and pinged person.
        this.builder.append(textComponent1);
        this.pinged.add(player);
    }


    private String getPlayerHoverText(@Nullable Player player) {
        if (player == null) {
            return """
                   <red>NULL
                   <dark-gray>&<unknown-entity>
                   """;
        }
        // Set hover text.
        return """
                <blue>%s
                <gray>Name <i>%s</i>
                <dark-gray>%s
                """.formatted(ChatColor.stripColor(player.getPublicName()), player.getName(), player.getUuid());
    }

    private enum Mode {
        TYPE,
        BODY
    }
}