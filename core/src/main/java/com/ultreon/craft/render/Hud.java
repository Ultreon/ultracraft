package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.libs.commons.v0.Identifier;

public class Hud implements GameRenderable {
    private final UltreonCraft game;
    private final GlyphLayout layout = new GlyphLayout();

    private final Texture texture;


    public Hud(UltreonCraft game) {
        this.game = game;
        this.texture = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        Player player = this.game.player;
        if (player == null) return;

        renderHotbar(renderer, player);
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
                BakedCubeModel bakedBlockModel = this.game.getBakedBlockModel(block);
                TextureRegion front = bakedBlockModel.front();
                TextureRegion top = bakedBlockModel.top();
                renderer.setTextureColor(Color.white.darker());
                renderer.texture(front, ix, 5, 16, 6);
                renderer.setTextureColor(Color.white);
                renderer.texture(top, ix, 11, 16, 16);
            } else {
                TextureRegion texture = this.game.itemTextureAtlas.get(key.mapPath(path -> "textures/items/" + path + ".png"));
                renderer.setTextureColor(Color.white.darker());
                renderer.texture(texture, ix, 5, 16, 16);
                renderer.setTextureColor(Color.white);
                renderer.texture(texture, ix, 6, 16, 16);
            }
        }

        float healthRatio = player.getHealth() / player.getMaxHeath();
        if (healthRatio > 0.5f) renderer.setColor(Color.rgb(0x00b000));
        else if (healthRatio > 0.25f) renderer.setColor(Color.rgb(0xffb000));
        else renderer.setColor(Color.rgb(0xd00000));

        renderer.text((int)(healthRatio * 100) + "%", (int)((float)this.game.getScaledWidth() / 2) - 65, 37);
        if (key != null && selectedItem != Items.AIR) {
            ScissorStack.pushScissors(new Rectangle((int) ((float) this.game.getScaledWidth() / 2) - 38, 29, 71, 10));
            String name = selectedItem.getTranslation();
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
