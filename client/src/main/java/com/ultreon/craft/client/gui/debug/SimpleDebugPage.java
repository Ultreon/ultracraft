package com.ultreon.craft.client.gui.debug;

import com.badlogic.gdx.Gdx;

public class SimpleDebugPage implements DebugPage {
    @Override
    public void render(DebugRenderContext context) {
        var client = context.client();
        var player = client.player;

        context.left("FPS", Gdx.graphics.getFramesPerSecond())
                .left("TPS", client.getCurrentTps())
                .left("Ping", client.connection.getPing() + "ms");

        if (player != null) {
            context.left("Position", player.getBlockPos());
        }
    }
}
