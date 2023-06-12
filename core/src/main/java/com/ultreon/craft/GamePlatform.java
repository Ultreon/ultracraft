package com.ultreon.craft;

import com.ultreon.craft.platform.GdxPlatform;
import com.ultreon.craft.platform.OperatingSystem;
import com.ultreon.craft.platform.PlatformType;

import org.slf4j.Logger;

public abstract class GamePlatform {
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
        };
    }

    public boolean canAccessData() {
        return switch (this.getPlatformType()) {
            case DESKTOP -> true;
            case MOBILE, WEB -> false;
        };
    }

    public void setupImGui() {

    }

    public void preInitImGui() {

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
        };
    }

    public void firstRender() {

    }
}
