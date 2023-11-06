package com.ultreon.craft.client.gui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.SelectionList;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Identifier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ModListScreen extends Screen {
    private static final Identifier DEFAULT_MOD_ICON = UltracraftClient.id("textures/gui/icons/missing_mod.png");
    private SelectionList<ModContainer> list;
    private Button<?> configButton;
    private Button<?> backButton;
    private static final Map<String, Texture> TEXTURES = new HashMap<>();

    public ModListScreen(Screen back) {
        super(TextObject.translation("ultracraft.screen.mod_list"), back);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.list = builder.<ModContainer>selectionList(48, () -> new Bounds(0, 0, 200, this.size.height - 52))
                .itemRenderer(this::renderItem)
                .selectable(true)
                .entries(FabricLoader.getInstance()
                        .getAllMods()
                        .stream()
                        .sorted((a, b) -> a.getMetadata().getName().compareToIgnoreCase(b.getMetadata().getName()))
                        .filter(modContainer -> modContainer.getOrigin().getKind() != ModOrigin.Kind.NESTED)
                        .toList());

        this.configButton = builder.button(() -> new Position(5, this.size.height - 51), caller -> {
                })
                .width(190)
                .translation("ultracraft.screen.mod_list.config")
                .enabled(false);

        this.backButton = builder.button(() -> new Position(5, this.size.height - 26), this::onBack)
                .translation("ultracraft.ui.back")
                .width(190);
    }

    public void onBack(Button<?> button) {
        this.back();
    }

    private void renderItem(Renderer renderer, ModContainer modContainer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        ModMetadata metadata = modContainer.getMetadata();
        var x = this.list.getX();

        renderer.drawTextLeft(metadata.getName(), x + 50, y + this.list.getItemHeight() - 34);
        renderer.drawTextLeft("Version: " + metadata.getVersion().getFriendlyString(), x + 50, y + this.list.getItemHeight() - 34 + 12, Color.rgb(0x808080));

        this.drawIcon(renderer, metadata, x + 7, y + 7, 32);
    }

    private void drawIcon(Renderer renderer, ModMetadata metadata, int x, int y, int size) {
        Identifier iconId;
        @Nullable String iconPath = metadata.getIconPath(128).orElse(null);
        Identifier overrideId = ModIconOverrides.get(metadata.getId());
        TextureManager textureManager = this.client.getTextureManager();
        if (overrideId != null) {
            textureManager.registerTexture(overrideId);
            iconId = textureManager.isTextureLoaded(overrideId) ? overrideId : ModListScreen.DEFAULT_MOD_ICON;
        } else if (iconPath != null) {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!ModListScreen.TEXTURES.containsKey(metadata.getId())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

                ModListScreen.TEXTURES.put(iconPath, texture);
            }
            Texture texture = ModListScreen.TEXTURES.computeIfAbsent(metadata.getId(), s -> new Texture(Gdx.files.classpath(metadata.getIconPath(128).orElse(null))));
            iconId = UltracraftClient.id("generated/mod_icon/" + metadata.getId().replaceAll("-", "_") + ".png");
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
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        ModContainer selected = this.list.getSelected();
        if (selected != null) {
            ModMetadata metadata = selected.getMetadata();
            int x = 220;
            int y = 20;

            this.drawIcon(renderer, metadata, x, y, 64);

            int xIcon = x + 84;
            renderer.drawTextScaledLeft(metadata.getName(), 2, xIcon, y);
            renderer.drawTextLeft("ID: " + metadata.getId(), xIcon, y + 24, Color.rgb(0x808080));
            renderer.drawTextLeft("Version: " + metadata.getVersion().getFriendlyString(), xIcon, y + 36, Color.rgb(0x808080));
            renderer.drawTextLeft(metadata.getAuthors().stream().findFirst().map(modContributor -> "Made By: " + modContributor.getName()).orElse("Made By Anonymous"), xIcon, y + 54, Color.rgb(0x505050));

            y += 84;
            renderer.multiLineText(metadata.getDescription(), x, y, Color.rgb(0x808080));
        }
    }

    public SelectionList<ModContainer> getList() {
        return this.list;
    }

    public Button<?> getConfigButton() {
        return this.configButton;
    }

    public Button<?> getBackButton() {
        return this.backButton;
    }
}
