package com.ultreon.craft.text;

import com.ultreon.craft.entity.Player;

import java.util.List;

public class ParseResult {
    private final MutableText result;
    private final MutableText textResult;
    private final TextObject[] textObject;

    @Deprecated
    public TextObject[] getMutableTexts() {
        return this.textObject;
    }

    public ParseResult(List<Player> pinged, TextObject prefix, TextObject textPrefix, MutableText textObj) {
        this.textObject = textObj.extras.toArray(new TextObject[0]);

        MutableText result = MutableText.literal("");
        result = result.append(prefix);

        MutableText textResult = MutableText.literal("");
        textResult = textResult.append(textPrefix);

        result = result.append(textObj);
        textResult = textResult.append(textObj);

        this.result = result;
        this.textResult = textResult;
    }

    public MutableText getResult() {
        return this.result;
    }

    public MutableText getTextResult() {
        return this.textResult;
    }
}