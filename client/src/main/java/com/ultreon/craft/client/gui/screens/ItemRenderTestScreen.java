package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Input;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.client.gui.Renderer;

public class ItemRenderTestScreen extends Screen {
    private static final int MAX_STATE = 8;
    private int state = 8;
    private boolean bg = true;
    private int mx = 0;
    private int my = 0;

    public ItemRenderTestScreen() {
        super(ItemRenderTestScreen.class.getName());
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        if (this.bg) super.renderBackground(renderer);

        renderer.drawCenteredText("STATE [" + this.state + "]", this.width / 2, this.height - 20);
        renderer.drawCenteredText("BG [" + this.bg + "]", this.width / 2, this.height - 30);
        renderer.drawCenteredText("X [" + this.mx + "]" + "Y [" + this.my + "]", this.width / 2, this.height - 40);

        if (this.state == 0) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, 16, 16);
        if (this.state == 1) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, 16, -16);
        if (this.state == 2) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, -16, -16);
        if (this.state == 3) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, -16, 16);
        if (this.state == 4) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, this.getLastMouseX(), this.height - this.getLastMouseY());
        if (this.state == 5) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, this.getLastMouseX(), this.height - -this.getLastMouseY());
        if (this.state == 6) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, -this.getLastMouseX(), this.height - -this.getLastMouseY());
        if (this.state == 7) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, -this.getLastMouseX(), this.height - this.getLastMouseY());
        if (this.state == 8) this.client.itemRenderer.render(Items.GRASS_BLOCK, renderer, mx, my);
    }

    @Override
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.SPACE && ++this.state > MAX_STATE) this.state = 0;

        if (keyCode == Input.Keys.ENTER) this.bg = !this.bg;

        if (keyCode == Input.Keys.UP) this.my += 1;
        if (keyCode == Input.Keys.DOWN) this.my -= 1;
        if (keyCode == Input.Keys.LEFT) this.mx -= 1;
        if (keyCode == Input.Keys.RIGHT) this.mx += 1;
        return super.keyPress(keyCode);
    }
}
