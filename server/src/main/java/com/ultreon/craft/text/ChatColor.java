package com.ultreon.craft.text;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.Char2ReferenceArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

import java.util.regex.Pattern;

public enum ChatColor {

    BLACK('0', 0, false, 0x000000),
    DARK_BLUE('1', 1, false, 0x0000aa),
    DARK_GREEN('2', 2, false, 0x00aa00),
    DARK_AQUA('3', 3, false, 0x00aaaa),
    DARK_RED('4', 4, false, 0xaa0000),
    DARK_PURPLE('5', 5, false, 0xaa00aa),
    GOLD('6', 6, false, 0xffaa00),
    GRAY('7', 7, false, 0xaaaaaa),
    DARK_GRAY('8', 8, false, 0x555555),
    BLUE('9', 9, false, 0x5555ff),
    GREEN('a', 10, false, 0x55ff55),
    AQUA('b', 11, false, 0x55ffff),
    RED('c', 12, false, 0xff5555),
    LIGHT_PURPLE('d', 13, false, 0xff55ff),
    YELLOW('e', 14, false, 0xffff55),
    WHITE('f', 15, false, 0xffffff),
    MAGIC('k', 16, true),
    BOLD('l', 17, true),
    STRIKETHROUGH('m', 18, true),
    UNDERLINE('n', 19, true),
    ITALIC('o', 20, true),
    RESET('r', 21);

    private final char code;
    private final int intCode;
    private final boolean isFormat;
    private final String toString;
    private final Integer color;

    ChatColor(char code, int intCode, boolean isFormat, Integer color) {
        this.code = code;
        this.intCode = intCode;
        this.isFormat = isFormat;
        this.color = color;
        this.toString = new String(new char[]{'§', code});
    }

    ChatColor(char code, int intCode) {
        this(code, intCode, false);
    }

    ChatColor(char code, int intCode, boolean isFormat) {
        this(code, intCode, isFormat, null);
    }

    public char getCode() {
        return this.code;
    }

    public int getIntCode() {
        return this.intCode;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public Integer getColor() {
        return this.color;
    }

    public boolean isColor() {
        return !this.isFormat && this != ChatColor.RESET;
    }

    @Override
    public String toString() {
        return this.toString;
    }

    public String concat(String str) {
        return this.toString + str;
    }

    public String concat(ChatColor color) {
        return this.toString + color.toString;
    }

    public ChatColor asBungee() {
        return ChatColor.getByChar(this.code);
    }

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    private static final Int2ReferenceMap<ChatColor> BY_ID = new Int2ReferenceArrayMap<>();
    private static final Char2ReferenceMap<ChatColor> BY_CHAR = new Char2ReferenceArrayMap<>();

    static {
        for (ChatColor color : ChatColor.values()) {
            ChatColor.BY_ID.put(color.intCode, color);
            ChatColor.BY_CHAR.put(color.code, color);
        }
    }

    public static ChatColor getByChar(char code) {
        return ChatColor.BY_CHAR.get(code);
    }

    public static ChatColor getById(int id) {
        return ChatColor.BY_ID.get(id);
    }

    public static ChatColor getByChar(String code) {
        Preconditions.checkNotNull(code, "Code cannot be null");
        Preconditions.checkArgument(!code.isEmpty(), "Code must have at least one char");
        return ChatColor.BY_CHAR.get(code.charAt(0));
    }

    public static String stripColor(final String input) {
        return input == null ? null : ChatColor.STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static ChatColor getLastColors(String input) {
        ChatColor result = ChatColor.WHITE;
        int length = input.length();

        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == '§' && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {
                    result = color;

                    if (color.isColor() || color == ChatColor.RESET) {
                        break;
                    }
                }
            }
        }

        return result;
    }
}