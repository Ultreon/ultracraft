package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.World;

public class DebugRenderer {
    private static final int OFFSET = 20;
    private final UltreonCraft game;
    private int y;

    public DebugRenderer(UltreonCraft game) {
        this.game = game;
    }

    public void render(Renderer renderer) {
        this.y = OFFSET;

        World world = this.game.world;

        // World
        if (world != null) {
            this.drawLine(renderer, "fps", Gdx.graphics.getFramesPerSecond());
            this.drawLine(renderer, "visible chunks", world.getRenderedChunks() + "/" + world.getTotalChunks());

            // Player
            Player player = this.game.player;
            if (player != null) {
                GridPoint3 blockPosition = player.blockPosition();
                this.drawLine(renderer, "block xyz", blockPosition);
                this.drawLine(renderer, "chunk xyz", this.block2sectionPos(blockPosition));
                this.drawLine(renderer, "chunk shown", world.getChunkAt(blockPosition) != null);
                this.drawLine(renderer, "region open", world.getRegionAt(blockPosition) != null);
            }
        }

        // Mobile platform.
        if (GamePlatform.instance.isMobile()) {
            // HUD
            Hud hud = this.game.hud;
            if (hud != null) {
                this.drawLine(renderer, "joystick", hud.getJoyStick());
            }
        }
    }

    private GridPoint3 block2sectionPos(GridPoint3 blockPos) {
        return new GridPoint3(blockPos.x / 16, blockPos.y / 16, blockPos.z / 16);
    }

    public void drawLine(Renderer renderer, String name, Object value) {
        this.y += 10;
        renderer.drawText(name + ": " + value, OFFSET, this.game.getScaledHeight() - this.y);
    }
}