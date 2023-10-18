package com.ultreon.craft.desktop.client.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.TextureManager;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.widget.SelectionList;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ModListScreen extends Screen {
    private SelectionList<ModContainer> list;
    private static final Map<String, Texture> TEXTURES = new HashMap<>();

    public ModListScreen(Screen back) {
        super(back, Language.translate("craft.screen.mod_list"));
    }

    @Override
    public void show() {
        super.show();

        this.clearWidgets();
        this.list = this.add(new SelectionList<>(0, 0, 200, this.height, 48));
        this.list.setItemRenderer(this::renderItem);
        this.list.setSelectable(true);
        QuiltLoader.getAllMods().stream().sorted(Comparator.comparing(o -> o.metadata().name()))
                .filter(modContainer -> {
                    System.out.println("modContainer.getSourceType() = " + modContainer.getSourceType());
                    return modContainer.getSourceType() != ModContainer.BasicSourceType.OTHER;
                })
                .forEachOrdered(this.list::addEntry);
    }

    private void renderItem(Renderer renderer, ModContainer modContainer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        ModMetadata metadata = modContainer.metadata();
        var x = this.list.getX();

        renderer.drawText(metadata.name(), x + 50, y + this.list.getItemHeight() - 34);
        renderer.drawText(metadata.id(), x + 50, y + this.list.getItemHeight() - 34 + 12, Color.rgb(0x808080));

        @Nullable String iconPath = metadata.icon(128);
        if (iconPath != null) {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!ModListScreen.TEXTURES.containsKey(metadata.id())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                ModListScreen.TEXTURES.put(metadata.icon(128), texture);
            }
            Texture texture = ModListScreen.TEXTURES.get(metadata.id());
            this.game.getTextureManager().registerTexture(UltreonCraft.id("generated/mod_icon/" + metadata.id() + ".png"), texture);
            renderer.blit(texture, x + 2 , y + 2, 42, 42, 0, 0, texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());
        }
    }


    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);

        ModContainer selected = this.list.getSelected();
        if (selected != null) {
            ModMetadata metadata = selected.metadata();
            renderer.drawTextScaled(metadata.name(), 2, 220, this.height - 20);
            renderer.drawText(metadata.version().raw(), 220 + renderer.getFont().width(metadata.name()) * 2 + 10, this.height - 28);
            renderer.drawText(metadata.id(),220, this.height - 44);
            renderer.multiLineText(metadata.description(), 220, this.height - 68, Color.rgb(0x808080));
        }
    }

    public SelectionList<ModContainer> getList() {
        return this.list;
    }
}
