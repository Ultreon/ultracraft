package com.ultreon.craft.api.ubo;

import com.ultreon.craft.text.TextObject;
import com.ultreon.data.types.IType;

public class UboFormatter {
    public static TextObject format(IType<?> message) {
        return message.accept(new UboFormatterVisitor(0));
    }
    public static TextObject format(IType<?> message, int ident) {
        return message.accept(new UboFormatterVisitor(ident));
    }
}
