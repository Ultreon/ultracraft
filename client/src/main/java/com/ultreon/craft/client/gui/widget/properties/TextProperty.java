package com.ultreon.craft.client.gui.widget.properties;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;

public interface TextProperty<T extends Widget<T>> {
    @CanIgnoreReturnValue
    default T translation(String id, Object... args) {
        return this.text(TextObject.translation(id, args));
    }

    @CanIgnoreReturnValue
    default T text(String text) {
        return this.text(TextObject.literal(text));
    }

    TextObject getText();

    @CanIgnoreReturnValue
    T text(TextObject text);

    String getRawText();
}
