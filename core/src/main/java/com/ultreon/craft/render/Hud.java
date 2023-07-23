package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.input.MobileInput;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.translations.v1.Language;

public class Hud implements GameRenderable {
    private final UltreonCraft game;
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture widgetsTex;
    private final Texture iconsTex;
    private final Texture crosshairTex;
    private final Texture mobileTex;
    private float joyStickX;
    private float joyStickY;
    private int stickPointer;
    private Vector2 joyStick;
    public int leftHeight;
    public int rightHeight;


    public Hud(UltreonCraft game) {
        this.game = game;
        this.widgetsTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));
        this.iconsTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/icons.png"));
        this.crosshairTex = game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/crosshair.png"));
        this.mobileTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/mobile_widgets.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        this.leftHeight = 0;
        this.rightHeight = 0;

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
        int x = this.game.getScaledWidth() / 2;
        int y = this.game.getScaledHeight() / 2;
        renderer.texture(this.crosshairTex, x - 8, y - 8, 16, 16);
    }

    private void renderMobileHud(Renderer renderer, Player player, MobileInput input) {
        renderer.texture(this.mobileTex, 20, 25, 50, 45, 0, 0);

        int joyStickX = 24 - 7 + 21;
        int joyStickY = 24 - 7 + 21;
        if (this.joyStick != null) {
            joyStickX = (int) (((this.joyStick.x + 1) / 2) * (48 - 14) + 21F);
            joyStickY = (int) (((this.joyStick.y + 1) / 2) * (48 - 14) + 21F);
        }

        renderer.texture(this.mobileTex, joyStickX, joyStickY, 14, 18, 50, 0);
        renderer.texture(this.mobileTex, 20, 20, 50, 5, 0, 45);

        Vec2i touchPos = input.getTouchPos();
        renderer.setColor(Color.argb(0x7fffffff));
        renderer.circle(touchPos.x, touchPos.y, 30);
        renderer.setColor(Color.argb(0xffffffff));
        renderer.circle(touchPos.x, touchPos.y, 30 * this.game.getBreakProgress());
    }

    private void renderHotbar(Renderer renderer, Player player) {
        int x = player.selected * 20;
        Block selectedBlock = player.getSelectedBlock();
        Identifier key = Registries.BLOCK.getKey(selectedBlock);

        renderer.texture(this.widgetsTex, (int)((float)this.game.getScaledWidth() / 2) - 90, 2, 180, 41, 0, 42);
        renderer.texture(this.widgetsTex, (int)((float)this.game.getScaledWidth() / 2) - 90 + x, 2, 20, 24, 0, 83);

        Block[] allowed = Player.allowed;
        for (int i = 0, allowedLength = allowed.length; i < allowedLength; i++) {
            Block block = allowed[i];
            int ix = (int)((float)this.game.getScaledWidth() / 2) - 90 + i * 20 + 2;
            this.game.itemRenderer.render(block, renderer, ix + 8, 8);
        }

        if (key != null && !selectedBlock.isAir()) {
            renderer.pushScissors((int) ((float) this.game.getScaledWidth() / 2) - 84, 32, 168, 12);
            String name = Language.translate(key.location() + ".block." + key.path() + ".name");
            renderer.drawCenteredText(name, (int) ((float) this.game.getScaledWidth()) / 2, 41);
            renderer.popScissors();
        }

        this.leftHeight += 47;
        this.rightHeight += 47;
    }

    private void renderHealth(Renderer renderer, Player player) {
        int x = (int) ((float) this.game.getScaledWidth() / 2) - 81;

        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.texture(this.iconsTex, x + emptyHeartX * 8, this.leftHeight, 9, 9, 16, 0);

        int heartX;
        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
            renderer.texture(this.iconsTex, x + heartX * 8, this.leftHeight, 9, 9, 25, 0);

        if ((int) player.getHealth() % 2 == 1)
            renderer.texture(this.iconsTex, x + heartX * 8, this.leftHeight, 9, 9, 34, 0);

        this.leftHeight += 13;
    }

    public boolean touchDown(int screenX, int screenY, int pointer) {
        screenY = this.game.getHeight() - screenY;
        screenX /= this.game.getGuiScale();
        screenY /= this.game.getGuiScale();
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
        screenY = this.game.getHeight() - screenY;
        screenX /= this.game.getGuiScale();
        screenY /= this.game.getGuiScale();
        if (this.stickPointer == pointer) {
            this.joyStick = null;
            this.stickPointer = -1;
            return;
        }
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        screenY = this.game.getHeight() - screenY;
        screenX /= this.game.getGuiScale();
        screenY /= this.game.getGuiScale();
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
