package com.ultreon.craft.client.gui.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.SelectionList;
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
    private static final Identifier DEFAULT_MOD_ICON = UltracraftClient.id("textures/gui/icons/missing_mod.png");
    private SelectionList<ModContainer> list;
    private static final Map<String, Texture> TEXTURES = new HashMap<>();

    public ModListScreen(Screen back) {
        super(back, Language.translate("craft.screen.mod_list"));
    }

    @Override
    public void init() {
        super.init();

        this.clearWidgets();
        this.list = this.add(new SelectionList<>(0, 0, 200, this.height, 48));
        this.list.setItemRenderer(this::renderItem);
        this.list.setSelectable(true);
        QuiltLoader.getAllMods().stream().sorted(Comparator.comparing(o -> o.metadata().name()))
                .filter(modContainer -> modContainer.getSourceType() != ModContainer.BasicSourceType.OTHER)
                .forEachOrdered(this.list::addEntry);
    }

    private void renderItem(Renderer renderer, ModContainer modContainer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        ModMetadata metadata = modContainer.metadata();
        var x = this.list.getX();

        renderer.drawText(metadata.name(), x + 50, y + this.list.getItemHeight() - 34);
        renderer.drawText("Version: " + metadata.version().raw(), x + 50, y + this.list.getItemHeight() - 34 + 12, Color.rgb(0x808080));

        this.drawIcon(renderer, metadata, x + 7, y + 7, 32);
    }

    private void drawIcon(Renderer renderer, ModMetadata metadata, int x, int y, int size) {
        Identifier iconId;
        @Nullable String iconPath = metadata.icon(128);
        Identifier overrideId = ModIconOverrides.get(metadata.id());
        TextureManager textureManager = this.client.getTextureManager();
        if (overrideId != null) {
            textureManager.registerTexture(overrideId);
            iconId = textureManager.isTextureLoaded(overrideId) ? overrideId : ModListScreen.DEFAULT_MOD_ICON;
        } else if (iconPath != null) {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!ModListScreen.TEXTURES.containsKey(metadata.id())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                ModListScreen.TEXTURES.put(metadata.icon(128), texture);
            }
            Texture texture = ModListScreen.TEXTURES.computeIfAbsent(metadata.id(), s -> new Texture(Gdx.files.classpath(metadata.icon(128))));
            iconId = UltracraftClient.id("generated/mod_icon/" + metadata.id() + ".png");
            if (!textureManager.isTextureLoaded(iconId)) textureManager.registerTexture(iconId, texture);
            if (!textureManager.isTextureLoaded(iconId)) iconId = ModListScreen.DEFAULT_MOD_ICON;
        } else {
            iconId = ModListScreen.DEFAULT_MOD_ICON;
        }

        int texW = textureManager.getTexture(iconId).getWidth();
        int texH = textureManager.getTexture(iconId).getHeight();
        renderer.blit(iconId, x, y, size, size, 0, 0, texW, texH, texW, texH);
    }


    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);

        ModContainer selected = this.list.getSelected();
        if (selected != null) {
            ModMetadata metadata = selected.metadata();
            int x = 220;
            int y = 20;

            this.drawIcon(renderer, metadata, x, y, 64);

            int xIcon = x + 84;
            renderer.drawTextScaled(metadata.name(), 2, xIcon, y);
            renderer.drawText("ID: " + metadata.id(), xIcon, y + 24, Color.rgb(0x808080));
            renderer.drawText("Version: " + metadata.version().raw(), xIcon, y + 36, Color.rgb(0x808080));
            renderer.drawText(metadata.contributors().stream().findFirst().map(modContributor -> "Made By: " + modContributor.name()).orElse("Made By Anonymous"), xIcon, y + 54, Color.rgb(0x505050));

            y += 84;
            renderer.multiLineText(metadata.description(), x, y, Color.rgb(0x808080));
        }
    }

    public SelectionList<ModContainer> getList() {
        return this.list;
    }
}
