package com.ultreon.craft.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.server.UltracraftServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UltracraftServerConfig {
    private static final Yaml YAML = new Yaml();
    private static UltracraftServerConfig instance = new UltracraftServerConfig();

    static {
        try {
            UltracraftServerConfig.instance = UltracraftServerConfig.YAML.loadAs(new FileReader("config/ultracraft_server.yml"), UltracraftServerConfig.class);
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
        try {
            Files.writeString(Path.of("config/ultracraft_client.yml"), UltracraftServerConfig.YAML.dumpAsMap(UltracraftServerConfig.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reload() {
        try {
            UltracraftServerConfig.instance = UltracraftServerConfig.YAML.loadAs(Files.readString(Path.of("config/ultracraft_server.yml")), UltracraftServerConfig.class);
        } catch (IOException e) {
            UltracraftServer.LOGGER.error("Failed to load config file!", e);
        }
    }
}
