package com.ultreon.craft.client.management;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.model.block.BlockModelRegistry;
import com.ultreon.craft.resources.ReloadContext;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.RegistryKey;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ultreon.craft.client.UltracraftClient.id;

public class TextureAtlasManager implements Manager<TextureAtlas> {
    private final Map<Identifier, TextureAtlas> atlasMap = new LinkedHashMap<>();
    private final UltracraftClient client;

    public TextureAtlasManager(UltracraftClient client) {
        this.client = client;
    }

    @Override
    public TextureAtlas register(@NotNull Identifier id, @NotNull TextureAtlas atlas) {
        atlasMap.put(id, atlas);
        return atlas;
    }

    public @Nullable TextureAtlas get(Identifier id) {
        return atlasMap.get(id);
    }

    @Override
    public void reload(ReloadContext context) {
        for (TextureAtlas atlas : List.copyOf(atlasMap.values())) {
            context.submit(atlas::dispose);
        }

        atlasMap.clear();

        this.client.blocksTextureAtlas = this.register(id("block"), BlockModelRegistry.stitch(this.client.getTextureManager()));

        TextureStitcher itemTextures = new TextureStitcher(id("item"));
        for (Map.Entry<RegistryKey<Item>, Item> e : Registries.ITEM.entries()) {
            if (e.getValue() == Items.AIR || e.getValue() instanceof BlockItem) continue;

            Identifier texId = e.getKey().element().mapPath(path -> "textures/items/" + path + ".png");
            Texture tex = this.client.getTextureManager().getTexture(texId);
            itemTextures.add(texId, tex);
        }
        this.client.itemTextureAtlas = this.register(id("item"), itemTextures.stitch());
    }
}
