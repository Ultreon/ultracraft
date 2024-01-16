package com.ultreon.craft.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.Mod;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.desktop.imgui.ImGuiOverlay;
import com.ultreon.craft.util.Env;
import com.ultreon.xeox.loader.XeoxLoader;
import net.fabricmc.loader.api.FabricLoader;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DesktopPlatform extends GamePlatform {
    private final Map<String, FabricMod> mods = new IdentityHashMap<>();

    DesktopPlatform() {
        super();

        FlatMacDarkLaf.setup();
    }

    @Override
    public void preInitImGui() {
        ImGuiOverlay.preInitImGui();
    }

    @Override
    public void setupImGui() {
        ImGuiOverlay.setupImGui();
    }

    @Override
    public void renderImGui() {
        ImGuiOverlay.renderImGui(UltracraftClient.get());
    }

    @Override
    public void onFirstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    @Override
    public void onGameDispose() {
        ImGuiOverlay.dispose();
    }

    @Override
    public boolean isShowingImGui() {
        return ImGuiOverlay.isShown();
    }

    @Override
    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
    }

    @Override
    public boolean areChunkBordersVisible() {
        return ImGuiOverlay.isChunkSectionBordersShown();
    }

    @Override
    public boolean showRenderPipeline() {
        return ImGuiOverlay.SHOW_RENDER_PIPELINE.get();
    }

    @Override
    public Optional<Mod> getMod(String id) {
        return FabricLoader.getInstance().getModContainer(id).map(container -> (Mod) this.mods.computeIfAbsent(id, v -> new FabricMod(container))).or(() -> super.getMod(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id) || super.isModLoaded(id);
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(FabricLoader.getInstance().getAllMods().stream().map(container -> this.mods.computeIfAbsent(container.getMetadata().getId(), v -> new FabricMod(container))).collect(Collectors.toList()));
        list.addAll(super.getMods());
        return list;
    }

    @Override
    public boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        FabricLoader.getInstance().invokeEntrypoints(name, initClass, init);
    }

    @Override
    public Env getEnv() {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> Env.CLIENT;
            case SERVER -> Env.SERVER;
        };
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean openImportDialog() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
        int result = jFileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = jFileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                XeoxLoader.get().importMod(file);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isDesktop() {
        return true;
    }
}
