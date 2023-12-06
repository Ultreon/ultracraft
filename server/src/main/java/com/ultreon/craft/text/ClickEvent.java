package com.ultreon.craft.text;

import java.io.Serializable;
import java.net.URL;

public record ClickEvent(com.ultreon.craft.text.ClickEvent.Action action, String value) implements Serializable {
    public static ClickEvent openUrl(URL url) {
        return new ClickEvent(Action.OPEN_URL, url.toString());
    }

    public static ClickEvent copyToClipboard(String text) {
        return new ClickEvent(Action.COPY_TO_CLIPBOARD, text);
    }

    public static ClickEvent runCommand(String text) {
        return new ClickEvent(Action.RUN_COMMAND, text);
    }

    public static ClickEvent suggestMessage(String text) {
        return new ClickEvent(Action.SUGGEST_MESSAGE, text);
    }

    public enum Action {
        OPEN_URL,
        COPY_TO_CLIPBOARD,
        RUN_COMMAND,
        SUGGEST_MESSAGE,
    }
}
