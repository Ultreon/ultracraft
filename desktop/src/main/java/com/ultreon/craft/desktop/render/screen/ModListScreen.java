package com.ultreon.craft.desktop.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.widget.SelectionList;
import com.ultreon.libs.translations.v1.Language;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

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
        FabricLoader.getInstance().getAllMods().forEach(this.list::addEntry);
    }

    private void renderItem(Renderer renderer, ModContainer modContainer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
        ModMetadata metadata = modContainer.getMetadata();
        renderer.drawText(metadata.getName(), 50, this.list.getItemHeight() - 12);
        renderer.drawText(metadata.getId(), 50, this.list.getItemHeight() - 12 - 12, Color.rgb(0x808080));
        metadata.getIconPath(128).ifPresent(iconPath -> {
            FileHandle iconFileHandle = Gdx.files.internal(iconPath);
            if (!iconFileHandle.exists()) return;
            if (!TEXTURES.containsKey(metadata.getId())) {
                Texture texture = new Texture(iconFileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                TEXTURES.put(metadata.getId(), texture);
            }
            Texture texture = TEXTURES.get(metadata.getId());
            renderer.texture(texture, 3, 3, 42, 42, 0, 0, texture.getWidth(), texture.getHeight(), texture.getWidth(), texture.getHeight());
        });
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);

        ModContainer selected = this.list.getSelected();
        if (selected != null) {
            ModMetadata metadata = selected.getMetadata();
            renderer.drawTextScaled(metadata.getName(), 2, 220, this.height - 20);
            renderer.drawText(metadata.getVersion().getFriendlyString(), 220 + renderer.getFont().width(metadata.getName()) * 2 + 10, this.height - 28);
            renderer.drawText(metadata.getId(),220, this.height - 44);
            renderer.multiLineText(metadata.getDescription(), 220, this.height - 68, Color.rgb(0x808080));
        }
    }

    public SelectionList<ModContainer> getList() {
        return this.list;
    }
}
