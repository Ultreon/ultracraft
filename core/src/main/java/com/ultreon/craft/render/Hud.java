package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.input.MobileInput;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Hud implements GameRenderable {
    private final UltreonCraft game;

    private final @NotNull Texture widgetsTex;
    private final @NotNull Texture iconsTex;
    private final @NotNull Texture mobileTex;
    private int stickPointer;
    private Vector2 joyStick;
    public int leftY;
    public int rightY;


    public Hud(UltreonCraft game) {
        this.game = game;
        this.widgetsTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));
        this.iconsTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/icons.png"));
        this.mobileTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/mobile_widgets.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        this.leftY = this.game.getScaledHeight();
        this.rightY = this.game.getScaledHeight();

        Player player = this.game.player;
        if (player == null) return;

        this.renderHotbar(renderer, player);
        this.renderHealth(renderer, player);

        GameInput input = this.game.input;
        if (input instanceof MobileInput) {
            this.renderMobileHud(renderer, player, (MobileInput) input);
        } else {
            this.renderCrosshair(renderer, player);
        }
    }

    private void renderCrosshair(Renderer renderer, Player player) {
        renderer.flush();
        renderer.enableInvert();

        float x = this.game.getScaledWidth() / 2f;
        float y = this.game.getScaledHeight() / 2f;
        renderer.blit(UltreonCraft.id("textures/gui/crosshair.png"), x - 4.5f, y - 4.5f, 9, 9);

        renderer.flush();
        renderer.disableInvert();
    }

    private void renderMobileHud(Renderer renderer, Player player, MobileInput input) {
        renderer.blit(this.mobileTex, 20, 25, 50, 45, 0, 0);

        int joyStickX = 24 - 7 + 21;
        int joyStickY = 24 - 7 + 21;
        if (this.joyStick != null) {
            joyStickX = (int) (((this.joyStick.x + 1) / 2) * (48 - 14) + 21F);
            joyStickY = (int) (((this.joyStick.y + 1) / 2) * (48 - 14) + 21F);
        }

        renderer.blit(this.mobileTex, joyStickX, joyStickY, 14, 18, 50, 0);
        renderer.blit(this.mobileTex, 20, 20, 50, 5, 0, 45);

        Vec2i touchPos = input.getTouchPos();
        renderer.setColor(Color.argb(0x7fffffff));
        renderer.circle(touchPos.x, touchPos.y, 30);
        renderer.setColor(Color.argb(0xffffffff));
        renderer.circle(touchPos.x, touchPos.y, 30 * this.game.getBreakProgress());
    }

    private void renderHotbar(Renderer renderer, Player player) {
        int x = player.selected * 20;
        ItemStack selectedItem = player.getSelectedItem();
        Identifier key = Registries.ITEMS.getKey(selectedItem.getItem());

        renderer.blit(this.widgetsTex, (int)((float)this.game.getScaledWidth() / 2) - 90, this.leftY - 43, 180, 41, 0, 42);
        renderer.blit(this.widgetsTex, (int)((float)this.game.getScaledWidth() / 2) - 90 + x, this.leftY - 26, 20, 24, 0, 83);

        //TODO use item renderer
        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        for (int i = 0, allowedLength = allowed.size(); i < allowedLength; i++) {
            ItemStack item = allowed.get(i).getItem();
            int ix = (int)((float)this.game.getScaledWidth() / 2) - 90 + i * 20 + 2;
            this.game.itemRenderer.render(item.getItem(), renderer, ix, this.game.getScaledHeight() - 24);
            int count = item.getCount();
            if (!item.isEmpty() && count > 1) {
                String text = Integer.toString(count);
                renderer.drawText(text, ix + 18 - this.game.font.width(text), this.game.getScaledHeight() - 7 - this.game.font.lineHeight, Color.WHITE, false);
            }
        }

        if (key != null && !selectedItem.isEmpty()) {
            if (renderer.pushScissors((int) ((float) this.game.getScaledWidth() / 2) - 84, this.leftY - 44, 168, 12)) {
                String name = selectedItem.getItem().getTranslation();
                renderer.drawCenteredText(name, (int) ((float) this.game.getScaledWidth()) / 2, this.leftY - 41);
                renderer.popScissors();
            }
        }

        this.leftY -= 47;
        this.rightY -= 47;
    }

    private void renderHealth(Renderer renderer, Player player) {
        int x = (int) ((float) this.game.getScaledWidth() / 2) - 81;

        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.blit(this.iconsTex, x + emptyHeartX * 8, this.leftY - 9, 9, 9, 16, 0);

        int heartX;
        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
            renderer.blit(this.iconsTex, x + heartX * 8, this.leftY - 9, 9, 9, 25, 0);

        if ((int) player.getHealth() % 2 == 1)
            renderer.blit(this.iconsTex, x + heartX * 8, this.leftY - 9, 9, 9, 34, 0);

        this.leftY -= 13;
    }

    public boolean touchDown(int screenX, int screenY, int pointer) {
        screenX /= (int) this.game.getGuiScale();
        screenY /= (int) this.game.getGuiScale();
        if (this.game.input instanceof MobileInput) {
            if (screenX >= 20 && screenX <= 70 &&
                    screenY >= 20 && screenY <= 70) {
                this.stickPointer = pointer;
                this.joyStick = new Vector2((screenX - 20F - 25F) / 25F, (screenY - 20F - 25F) / 25F);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"UnnecessaryReturnStatement", "unused"})
    public void touchUp(int screenX, int screenY, int pointer) {
        if (this.stickPointer == pointer) {
            this.joyStick = null;
            this.stickPointer = -1;
            return;
        }
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenX /= (int) this.game.getGuiScale();
        screenY /= (int) this.game.getGuiScale();
        Vector2 stickTouch = this.joyStick;
        if (this.stickPointer == pointer && stickTouch != null) {
            float x = Mth.clamp((screenX - 20F - 25F) / 25F, -1, 1);
            float y = Mth.clamp((screenY - 20F - 25F) / 25F, -1, 1);
            stickTouch.set(x, y);
        }
        return false;
    }

    public Vector2 getJoyStick() {
        return this.joyStick;
    }
}
