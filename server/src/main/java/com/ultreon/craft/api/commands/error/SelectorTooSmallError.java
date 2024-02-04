package com.ultreon.craft.api.commands.error;

import com.ultreon.craft.api.commands.MessageCode;

public class SelectorTooSmallError extends CommandError {

    public SelectorTooSmallError(String got) {
        super(MessageCode.SELECTOR_TOO_SMALL,
                "Selector is too small, got \"" + got.replaceAll("\"", "\\\\\"") + "\"");
    }

    public SelectorTooSmallError(String got, int index) {
        super(MessageCode.SELECTOR_TOO_SMALL,
                "Selector is too small, got \"" + got.replaceAll("\"", "\\\\\"") + "\"",
                index);
    }

    @Override
    public String getName() {
        return "Invalid";
    }
}