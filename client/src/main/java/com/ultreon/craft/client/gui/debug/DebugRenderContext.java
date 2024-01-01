package com.ultreon.craft.client.gui.debug;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface DebugRenderContext {
    DebugRenderContext left();

    DebugRenderContext left(String text);

    DebugRenderContext left(String key, Object value);

    DebugRenderContext right();

    DebugRenderContext right(String text);

    DebugRenderContext right(String key, Object value);

    DebugRenderContext entryLine(int idx, String name, long nanos);

    DebugRenderContext entryLine(String name, String value);
    DebugRenderContext entryLine(int idx, String name);

    DebugRenderContext entryLine(TextObject value);

    DebugRenderContext entryLine();

    UltracraftClient client();

    default Vec3i block2sectionPos(BlockPos blockPos) {
        return new Vec3i(blockPos.x() / 16, blockPos.y() / 16, blockPos.z() / 16);
    }
}
