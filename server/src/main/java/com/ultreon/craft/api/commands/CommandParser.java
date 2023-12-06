package com.ultreon.craft.api.commands;

public interface CommandParser<T> {
    T parse(CommandReader ctx) throws CommandParseException;
}