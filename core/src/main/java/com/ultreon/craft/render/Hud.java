package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.init.Shaders;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.input.MobileInput;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.translations.v1.Language;

public class Hud implements GameRenderable {
    private final UltreonCraft game;
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture texture;
    private final Texture crosshair;
    private final Texture mobileTexture;
    private float joyStickX;
    private float joyStickY;
    private int stickPointer;
    private Vector2 joyStick;


    public Hud(UltreonCraft game) {
        this.game = game;
        this.texture = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));
        this.crosshair = game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/crosshair.png"));
        this.mobileTexture = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/mobile_widgets.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        Player player = this.game.player;
        if (player == null) return;

        this.renderHotbar(renderer, player);
        this.renderCrosshair(renderer, player);

        GameInput input = this.game.input;
        if (input instanceof MobileInput) {
            this.renderMobileHud(renderer, player, (MobileInput) input);
        }
    }

    private void renderCrosshair(Renderer renderer, Player player) {
        renderer.setShader(Shaders.XOR);

        int x = this.game.getScaledWidth() / 2;
        int y = this.game.getScaledHeight() / 2;
        renderer.texture(this.crosshair, x - 7, y - 7, 14, 14);

        renderer.unsetShader();
    }

    private void renderMobileHud(Renderer renderer, Player player, MobileInput input) {
        renderer.texture(this.mobileTexture, 20, 20, 50, 45, 0, 0);

        int joyStickX = 24 - 7 + 21;
        int joyStickY = 24 - 7 + 21;
        if (this.joyStick != null) {
            joyStickX = (int) (((this.joyStick.x + 1) / 2) * (48 - 14) + 21F);
            joyStickY = (int) (((this.joyStick.y + 1) / 2) * (48 - 14) + 21F);
        }

        renderer.texture(this.mobileTexture, joyStickX, joyStickY, 14, 18, 50, 0);

        renderer.texture(this.mobileTexture, 20, 20, 50, 5, 0, 45);
    }

    private void renderHotbar(Renderer renderer, Player player) {
        int x = player.selected * 18;
        Item selectedItem = player.getSelectedItem();
        Identifier key = Registries.ITEMS.getKey(selectedItem);

        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81, 0, 162, 39, 0, 42);
        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81 + x, 0, 18, 22, 0, 81);

        Item[] allowed = Player.ALLOWED;
        for (int i = 0, allowedLength = allowed.length; i < allowedLength; i++) {
            Item item = allowed[i];
            int ix = (int)((float)this.game.getScaledWidth() / 2) - 80 + i * 18;
            if (item instanceof BlockItem blockItem) {
                BakedCubeModel bakedBlockModel = this.game.getBakedBlockModel(blockItem.getBlock());
                if (bakedBlockModel == null) {
                    renderer.setTextureColor(Color.WHITE);
                    renderer.texture((TextureRegion) null, ix, 5, 16, 16);
                } else {
                    TextureRegion front = bakedBlockModel.front();
                    TextureRegion top = bakedBlockModel.top();
                    renderer.setTextureColor(Color.WHITE.darker());
                    renderer.texture(front, ix, 5, 16, 6);
                    renderer.setTextureColor(Color.WHITE);
                    renderer.texture(top, ix, 11, 16, 16);
                }
            } else {
                Identifier curKey = Registries.ITEMS.getKey(item);
                if (curKey == null) {
                    renderer.setTextureColor(Color.WHITE);
                    renderer.texture((TextureRegion) null, ix, 5, 16, 16);
                } else {
                    TextureRegion texture = this.game.itemTextureAtlas.get(curKey.mapPath(path -> "textures/items/" + path + ".png"));
                    renderer.setTextureColor(Color.WHITE.darker());
                    renderer.texture(texture, ix, 5, 16, 16);
                    renderer.setTextureColor(Color.WHITE);
                    renderer.texture(texture, ix, 6, 16, 16);
                }
            }
        }

        float healthRatio = player.getHealth() / player.getMaxHeath();
        Color color = healthRatio > 0.5f ? Color.rgb(0x00b000) : healthRatio > 0.25f ? Color.rgb(0xffb000) : Color.rgb(0xd00000);

        renderer.drawText((int)(healthRatio * 100) + "%", (int)((float)this.game.getScaledWidth() / 2) - 65, 37, color, false);
        if (key != null && selectedItem != Items.AIR) {
            ScissorStack.pushScissors(new Rectangle((int) ((float) this.game.getScaledWidth() / 2) - 38, 29, 71, 10));
            String name = selectedItem.getTranslation();
            renderer.setColor(Color.rgb(0xffffff));
            renderer.drawCenteredText(name, (int) ((float) this.game.getScaledWidth()) / 2, 49);
            ScissorStack.popScissors();
        }
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
