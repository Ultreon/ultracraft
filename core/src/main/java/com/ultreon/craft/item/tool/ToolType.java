package com.ultreon.craft.item.tool;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;
import org.jetbrains.annotations.NotNull;

public class ToolType {
    public static final ToolType PICKAXE = register("pickaxe", new ToolType());

    @NotNull
    public Identifier getId() {
        Identifier key = Registries.TOOL_TYPES.getKey(this);
        if (key == null) throw new IllegalStateException("Tool type not registered");

        return key;
    }

    private static ToolType register(String name, ToolType type) {
        Registries.TOOL_TYPES.register(new Identifier(UltreonCraft.NAMESPACE, name), type);

        return type;
    }
}
