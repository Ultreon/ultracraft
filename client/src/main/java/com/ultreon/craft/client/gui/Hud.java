package com.ultreon.craft.client.gui;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.hud.OverlayManager;
import com.ultreon.craft.client.util.GameRenderable;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Hud implements GameRenderable {
    private final UltracraftClient client;

    private final @NotNull Texture widgetsTex;
    private final @NotNull Texture iconsTex;
    public int leftY;
    public int rightY;
    private int width;
    private int height;


    public Hud(UltracraftClient client) {
        this.client = client;
        this.widgetsTex = this.client.getTextureManager().getTexture(UltracraftClient.id("textures/gui/widgets.png"));
        this.iconsTex = this.client.getTextureManager().getTexture(UltracraftClient.id("textures/gui/icons.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        this.leftY = this.client.getScaledHeight();
        this.rightY = this.client.getScaledHeight();

        Player player = this.client.player;
        if (player == null) return;

        if (!this.client.isInThirdPerson() || !this.client.config.get().accessibility.hideHotbarWhenThirdPerson) {
            this.renderHotbar(renderer, player);
        }

        if (!player.isInvincible()) {
            this.renderHealth(renderer, player);
        }

        OverlayManager.render(renderer, deltaTime);

        this.renderCrosshair(renderer);
    }

    private void renderCrosshair(Renderer renderer) {
        renderer.invertOn();

        float x = this.width / 2f;
        float y = this.height / 2f;
        renderer.blit(UltracraftClient.id("textures/gui/crosshair.png"), x - 4.5f, y - 4.5f, 9, 9);

        renderer.invertOff();
    }

    private void renderHotbar(Renderer renderer, Player player) {
        int x = player.selected * 20;
        ItemStack selectedItem = player.getSelectedItem();
        Identifier key = Registries.ITEM.getId(selectedItem.getItem());

        renderer.blit(this.widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90, this.leftY - 43, 180, 41, 0, 42);
        renderer.blit(this.widgetsTex, (int)((float)this.client.getScaledWidth() / 2) - 90 + x, this.leftY - 26, 20, 24, 0, 83);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            this.drawHotbarSlot(renderer, allowed, index);
        }

        if (key != null && !selectedItem.isEmpty() && renderer.pushScissors((int) ((float) this.client.getScaledWidth() / 2) - 84, this.leftY - 44, 168, 12)) {
            TextObject name = selectedItem.getItem().getTranslation();
            renderer.textCenter(name, (int) ((float) this.client.getScaledWidth()) / 2, this.leftY - 41);
            renderer.popScissors();
        }

        this.leftY -= 47;
        this.rightY -= 47;
    }

    private void drawHotbarSlot(Renderer renderer, List<ItemSlot> allowed, int index) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) this.client.getScaledWidth() / 2) - 90 + index * 20 + 2;
        this.client.itemRenderer.render(item.getItem(), renderer, ix, this.client.getScaledHeight() - 24);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            renderer.textLeft(text, ix + 18 - this.client.font.width(text), this.client.getScaledHeight() - 7 - this.client.font.lineHeight, Color.WHITE, false);
        }
    }

    private void renderHealth(Renderer renderer, Player player) {
        int x = (int) ((float) this.client.getScaledWidth() / 2) - 81;

        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.blit(this.iconsTex, x + emptyHeartX * 8, this.leftY - 9, 9, 9, 16, 0);

        int heartX;
        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
            renderer.blit(this.iconsTex, x + heartX * 8, this.leftY - 9, 9, 9, 25, 0);

        if ((int) player.getHealth() % 2 == 1)
            renderer.blit(this.iconsTex, x + heartX * 8, this.leftY - 9, 9, 9, 34, 0);

        this.leftY -= 13;
    }

}
