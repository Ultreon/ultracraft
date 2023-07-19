package com.ultreon.craft.render.gui.screens;

import com.badlogic.gdx.Input;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.Renderer;

public class ItemRenderTestScreen extends Screen {
    private static final int MAX_STATE = 7;
    private int state;
    private boolean bg = true;

    public ItemRenderTestScreen() {
        super(ItemRenderTestScreen.class.getName());
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        if (this.bg) super.renderBackground(renderer);

        renderer.drawCenteredText("STATE [" + this.state + "]", this.width / 2, this.height - 20);
        renderer.drawCenteredText("BG [" + this.bg + "]", this.width / 2, this.height - 30);

        if (this.state == 0) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, 16, 16);
        if (this.state == 1) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, 16, -16);
        if (this.state == 2) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, -16, -16);
        if (this.state == 3) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, -16, 16);
        if (this.state == 4) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, this.getLastMouseX(), this.getLastMouseY());
        if (this.state == 5) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, this.getLastMouseX(), -this.getLastMouseY());
        if (this.state == 6) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, -this.getLastMouseX(), -this.getLastMouseY());
        if (this.state == 7) this.game.itemRenderer.render(Blocks.GRASS_BLOCK, renderer, -this.getLastMouseX(), this.getLastMouseY());
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.SPACE) {
            this.state++;
            if (this.state > MAX_STATE) this.state = 0;
        }
        if (keyCode == Input.Keys.ENTER) {
            this.bg = !this.bg;
        }
        return super.keyPress(keyCode);
    }
}
