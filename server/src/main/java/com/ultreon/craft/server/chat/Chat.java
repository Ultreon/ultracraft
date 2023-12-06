package com.ultreon.craft.server.chat;

import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.api.commands.MessageCode;
import com.ultreon.craft.text.ChatColor;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class Chat {
    public static void sendServerMessage(CommandSender sender, String message) {
        sender.sendMessage("<blue>[<white>SERVER<blue>] <gray>" + message);
    }

    public static void sendFatal(CommandSender sender, String message) {
        sender.sendMessage("<red>[ERROR] <white>" + message);
    }

    public static void sendFatal(CommandSender sender, MessageCode code, String message) {
        sender.sendMessage(Chat.formatFatal(sender, message, code.getId()));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage("<red>[ERROR] <white>" + message);
    }

    public static void sendError(CommandSender sender, String message, String name) {
        sender.sendMessage(Chat.formatError(sender, message, name));
    }

    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage("<gold>[WARNING] <white>" + message);
    }

    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage("<blue>[INFO] <white>" + message);
    }

    public static void sendDebug(CommandSender sender, String message) {
        sender.sendMessage("<gray>[DEBUG] <white>" + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage("<green>[SUCCESS] <white>" + message);
    }

    public static void sendDenied(CommandSender sender, String message) {
        sender.sendMessage("<gold>[<red>DENIED<gold>] <gray>" + message);
    }

    public static MutableText formatServerMessage(CommandSender formater, String message) {
        return new Formatter(true, false, "<blue>[<white>SERVER<blue>] <gray>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.YELLOW)).parse().getResult();
    }

    public static MutableText formatFatal(CommandSender formater, String message) {
        return new Formatter(true, false, "<dark-red>[FATAL] <gray>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.GRAY)).parse().getResult();
    }

    public static MutableText formatError(CommandSender formater, String message) {
        return new Formatter(true, false, "<red>[ERROR] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.WHITE)).parse().getResult();
    }

    public static MutableText formatWarning(CommandSender formater, String message) {
        return new Formatter(true, false, "<gold>[WARNING] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.WHITE)).parse().getResult();
    }

    public static MutableText formatInfo(CommandSender formater, String message) {
        return new Formatter(true, false, "<blue>[INFO] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.WHITE)).parse().getResult();
    }

    public static MutableText formatDebug(CommandSender formater, String message) {
        return new Formatter(true, false, "<gray>[DEBUG] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.WHITE)).parse().getResult();
    }

    public static MutableText formatSuccess(CommandSender formater, String message) {
        return new Formatter(true, false, "<green>[SUCCESS] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.WHITE)).parse().getResult();
    }

    public static MutableText formatDenied(CommandSender formater, String message) {
        return new Formatter(true, false, "<gold>[<red>DENIED<gold>] <gray>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.GRAY)).parse().getResult();
    }

    public static void sendVoidObject(CommandSender sender) {

    }

    public static void sendObject(CommandSender sender, Object object) {

    }

    public static MutableText formatError(CommandSender sender, String message, String name) {
        return new Formatter(true, false, "<red>[ERROR] <white>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.RED)).parse().getResult();
    }

    public static MutableText formatFatal(CommandSender sender, String message, String name) {
        return new Formatter(true, false, "<dark-red>[FATAL] <gray>" + message, TextObject.empty(), TextObject.empty(), null, Color.of(ChatColor.DARK_RED)).parse().getResult();
    }
}
