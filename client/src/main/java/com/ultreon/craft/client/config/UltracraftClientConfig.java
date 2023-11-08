package com.ultreon.craft.client.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.server.UltracraftServer;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UltracraftClientConfig {
    private static final Yaml YAML = new Yaml();
    private static UltracraftClientConfig instance = new UltracraftClientConfig();

    static {
        try {
            UltracraftClientConfig.instance = UltracraftClientConfig.YAML.loadAs(new FileReader("config/ultracraft_client.yml"), UltracraftClientConfig.class);
        } catch (FileNotFoundException ignored) {
            UltracraftClientConfig.instance.save();
        } catch (RuntimeException e) {
            UltracraftServer.LOGGER.error("Failed to load config file!", e);
        }
    }

    public int renderDistance = 16;
    public int entityRenderDistance = 12;
    public boolean enableDebugUtils = FabricLoader.getInstance().isDevelopmentEnvironment();
    public boolean enable4xScreenshot = true;

    private UltracraftClientConfig() {

    }

    @CanIgnoreReturnValue
    public static UltracraftClientConfig get() {
        return UltracraftClientConfig.instance;
    }

    public void save() {
        try {
            Files.writeString(Path.of("config/ultracraft_client.yml"), UltracraftClientConfig.YAML.dumpAsMap(UltracraftClientConfig.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reload() {
        try {
            UltracraftClientConfig.instance = UltracraftClientConfig.YAML.loadAs(new FileReader("config/ultracraft_client.yml"), UltracraftClientConfig.class);
        } catch (FileNotFoundException e) {
            UltracraftServer.LOGGER.error("Failed to load config file!", e);
        }
    }
}
