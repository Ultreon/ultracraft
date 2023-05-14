package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class Hud implements GameRenderable {
    private final UltreonCraft game;
    private final GlyphLayout layout = new GlyphLayout();

    public Hud(UltreonCraft game) {
        this.game = game;
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        Player player = this.game.player;
        if (player == null) return;

        Texture texture = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));

        int x = player.selected * 18;
        Block selectedBlock = player.getSelectedBlock();
        Identifier key = Registries.BLOCK.getKey(selectedBlock);

        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81, 0, 162, 39, 0, 42);
        renderer.texture(texture, (int)((float)this.game.getScaledWidth() / 2) - 81 + x, 0, 18, 22, 0, 81);

        Block[] allowed = Player.ALLOWED;
        for (int i = 0, allowedLength = allowed.length; i < allowedLength; i++) {
            Block block = allowed[i];
            int ix = (int)((float)this.game.getScaledWidth() / 2) - 80 + i * 18;
            UV front = block.getModel().front();
            UV top = block.getModel().top();
            Texture blocks = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/blocks.png"));
            renderer.setTextureColor(Color.white.darker());
            renderer.texture(blocks, ix, 5, 16, 6, front.u() * 16, front.v() * 16);
            renderer.setTextureColor(Color.white);
            renderer.texture(blocks, ix, 11, 16, 16, top.u() * 16, top.v() * 16);
        }

        float healthRatio = player.getHealth() / player.getMaxHeath();
        if (healthRatio > 0.5f) renderer.setColor(Color.rgb(0x00b000));
        else if (healthRatio > 0.25f) renderer.setColor(Color.rgb(0xffb000));
        else renderer.setColor(Color.rgb(0xd00000));

        renderer.text((int)(healthRatio * 100) + "%", (int)((float)this.game.getScaledWidth() / 2) - 65, 37);
        if (key != null && !selectedBlock.isAir()) {
            ScissorStack.pushScissors(new Rectangle((int) ((float) this.game.getScaledWidth() / 2) - 38, 29, 71, 10));
            String name = "block." + key.location() + "." + key.path().replaceAll("/", ".");
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
