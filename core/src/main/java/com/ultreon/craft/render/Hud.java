package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v0.Language;
import org.lwjgl.opengl.GL20;

public class Hud implements GameRenderable {
    private final UltreonCraft game;
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture texture;
    private final Texture crosshair;


    public Hud(UltreonCraft game) {
        this.game = game;
        this.texture = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));
        this.crosshair = game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/crosshair.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        Player player = this.game.player;
        if (player == null) return;

        this.renderHotbar(renderer, player);
        this.renderCrosshair(renderer, player);
    }

    private void renderCrosshair(Renderer renderer, Player player) {
        GL20.glEnable(GL20.GL_COLOR_LOGIC_OP);
        GL20.glLogicOp(GL20.GL_SET);

        int x = this.game.getScaledWidth() / 2;
        int y = this.game.getScaledHeight() / 2;
        renderer.texture(this.crosshair, x - 7, y - 7, 14, 14);

        GL20.glLogicOp(GL20.GL_XOR);
        GL20.glDisable(GL20.GL_COLOR_LOGIC_OP);
    }

    private void renderHotbar(Renderer renderer, Player player) {
        int x = player.selected * 18;
        Block selectedBlock = player.getSelectedBlock();
        Identifier key = Registries.BLOCK.getKey(selectedBlock);

        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81, 0, 162, 39, 0, 42);
        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81 + x, 0, 18, 22, 0, 81);

        Block[] allowed = Player.ALLOWED;
        for (int i = 0, allowedLength = allowed.length; i < allowedLength; i++) {
            Block block = allowed[i];
            int ix = (int)((float)this.game.getScaledWidth() / 2) - 80 + i * 18;
            BakedCubeModel bakedBlockModel = this.game.getBakedBlockModel(block);
            TextureRegion front = bakedBlockModel.front();
            TextureRegion top = bakedBlockModel.top();
            renderer.setTextureColor(Color.white.darker());
            renderer.texture(front, ix, 5, 16, 6);
            renderer.setTextureColor(Color.white);
            renderer.texture(top, ix, 11, 16, 16);
        }

        float healthRatio = player.getHealth() / player.getMaxHeath();
        if (healthRatio > 0.5f) renderer.setColor(Color.rgb(0x00b000));
        else if (healthRatio > 0.25f) renderer.setColor(Color.rgb(0xffb000));
        else renderer.setColor(Color.rgb(0xd00000));

        renderer.text((int)(healthRatio * 100) + "%", (int)((float)this.game.getScaledWidth() / 2) - 65, 37);
        if (key != null && !selectedBlock.isAir()) {
            ScissorStack.pushScissors(new Rectangle((int) ((float) this.game.getScaledWidth() / 2) - 38, 29, 71, 10));
            String name = Language.translate(key.location() + "/block/" + key.path() + "/name");
            renderer.setColor(Color.rgb(0xffffff));
            this.layout.setText(this.game.font, name);
            renderer.setColor(Color.rgb(0xffffff).darker().darker());
            renderer.text(name, (int) ((float) this.game.getScaledWidth() / 2) - this.layout.width / 2, 48);
            renderer.setColor(Color.rgb(0xffffff));
            renderer.text(name, (int) ((float) this.game.getScaledWidth() / 2) - this.layout.width / 2, 49);
            ScissorStack.popScissors();
        }
    }
}
