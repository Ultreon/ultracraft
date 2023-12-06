package com.ultreon.craft.text;

import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Identifier;

public interface FormatSequence extends Iterable<TextElement> {
    boolean isBoldAt(int index);
    boolean isItalicAt(int index);
    boolean isUnderlinedAt(int index);
    boolean isStrikethroughAt(int index);
    Color getColorAt(int index);
    HoverEvent<?> getHoverEventAt(int index);
    ClickEvent getClickEventAt(int index);
    Identifier getFontAt(int index);
    TextObject getTextAt(int index);
}
