package com.ultreon.craft.client.gui.debug;

public interface DebugPage {
    DebugPage EMPTY = new DebugPage() {
        @Override
        public void render(DebugRenderContext context) {
            // Empty debug page
        }
    };

    void render(DebugRenderContext context);
}
