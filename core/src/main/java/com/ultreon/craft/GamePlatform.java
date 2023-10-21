package com.ultreon.craft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.platform.GdxPlatform;
import com.ultreon.craft.platform.OperatingSystem;
import com.ultreon.craft.platform.PlatformType;

import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.resources.v0.ResourceManager;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

public abstract class GamePlatform {
    @UnknownNullability
    public static GamePlatform instance;

    public abstract Logger getLogger(String name);

    public abstract OperatingSystem getOperatingSystem();

    public PlatformType getPlatformType() {
        return this.getOperatingSystem().getType();
    }

    public GdxPlatform getGdxPlatform() {
        return this.getOperatingSystem().getGdxPlatform();
    }

    public boolean isMobile() {
        return this.getPlatformType() == PlatformType.MOBILE;
    }

    public boolean supportsQuit() {
        return switch (this.getPlatformType()) {
            case DESKTOP -> true;
            case MOBILE, WEB -> false;
            default -> throw new IllegalArgumentException();
        };
    }

    public boolean canAccessData() {
        return switch (this.getPlatformType()) {
            case DESKTOP -> true;
            case MOBILE, WEB -> false;
            default -> throw new IllegalArgumentException();
        };
    }

    public void setupImGui() {

    }

    public void preInitImGui() {

    }

    public boolean isChunkSectionBordersShown() {
        return false;
    }

    public void renderImGui(UltreonCraft game) {

    }

    public boolean isShowingImGui() {
        return false;
    }

    public void setShowingImGui(boolean value) {

    }

    public boolean hasKeyInput() {
        return switch (this.getPlatformType()) {
            case DESKTOP, WEB -> true;
            case MOBILE -> false;
            default -> throw new IllegalArgumentException();
        };
    }

    public void firstRender() {

    }

    public void dispose() {

    }

    public boolean isDesktop() {
        return this.getPlatformType() == PlatformType.DESKTOP;
    }

    public boolean isWeb() {
        return this.getPlatformType() == PlatformType.WEB;
    }

    public FileHandle dataFile(String path) {
        return Gdx.files.external(path);
    }

    public void openModList() {

    }

    public boolean isModsSupported() {
        return false;
    }

    public static FileHandle data(String path) {
        return instance.dataFile(path);
    }

    public void setupMods() {

    }

    public void setupModsClient() {

    }

    public void setupModsServer() {

    }

    public void importModResources(ResourceManager resourceManager) {

    }

    public void handleCrash(CrashLog crashLog) {

    }

    public boolean isDevelopmentEnvironment() {
        return false;
    }
}
