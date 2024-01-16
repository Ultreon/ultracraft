package com.ultreon.craft.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.ultreon.craft.server.UltracraftServer;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class UltracraftServerConfig {
    private static final Gson GSON = new Gson();
    private static UltracraftServerConfig instance = new UltracraftServerConfig();

    static {
        try {
            UltracraftServerConfig.instance = UltracraftServerConfig.GSON.fromJson(new FileReader("config/ultracraft_server.json"), UltracraftServerConfig.class);
        } catch (FileNotFoundException ignored) {
            UltracraftServerConfig.instance.save();
        } catch (RuntimeException e) {
            UltracraftServer.LOGGER.error("Failed to load config file!", e);
        }
    }

    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public int autoSaveInterval = 60;
    public int initialAutoSaveDelay = 120;
    public boolean debugChunkPacketDump = false;
    public boolean debugBlockDataDump = false;
    public boolean debugWarnChunkBuildOverload = false;

    private UltracraftServerConfig() {

    }

    @CanIgnoreReturnValue
    public static UltracraftServerConfig get() {
        return UltracraftServerConfig.instance;
    }

    public void save() {
        Gdx.files.external("config/ultracraft_client.yml").writeString(UltracraftServerConfig.GSON.toJson(this), false);
    }

    public static void reload() {
        try {
            UltracraftServerConfig.instance = UltracraftServerConfig.GSON.fromJson(Gdx.files.external("config/ultracraft_server.yml").readString(), UltracraftServerConfig.class);
        } catch (GdxRuntimeException e) {
            UltracraftServer.LOGGER.error("Failed to load config file!", e);
        }
    }
}
