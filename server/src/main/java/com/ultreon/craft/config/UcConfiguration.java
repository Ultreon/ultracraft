package com.ultreon.craft.config;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.server.UltracraftServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.fusionyaml.library.FusionYAML;
import org.fusionyaml.library.configurations.FileConfiguration;
import org.fusionyaml.library.object.YamlElement;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UcConfiguration<T> {
    private final FusionYAML yaml = new FusionYAML.Builder().flowStyle(DumperOptions.FlowStyle.BLOCK).build();
    @Nullable
    private FileConfiguration config = null;

    private T object;
    private Path configPath;

    @SuppressWarnings({"unchecked"})
    public UcConfiguration(String name, EnvType configEnv, T object) {
        if (FabricLoader.getInstance().getEnvironmentType() != configEnv) {
            this.config = null;
            return;
        }

        ConfigEvents.LOAD.listen(loadingEnv -> {
            if (loadingEnv != configEnv) return;

            this.configPath = FabricLoader.getInstance().getConfigDir().resolve(name + ".yml");
            boolean existed = Files.exists(this.configPath);
            if (Files.notExists(this.configPath)) {
                try {
                    var element = this.yaml.serialize(object, object.getClass());

                    Files.createDirectories(this.configPath.getParent());
                    Files.writeString(this.configPath, this.yaml.toYAML(element));
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("Failed to create config file!", e);
                    return;
                }
            }

            try {
                this.object = this.yaml.deserialize(Files.readString(this.configPath), (Class<T>) object.getClass());
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to load config file!", e);
                return;
            }

            UltracraftServer.getWatchManager().watchFile(this.configPath.toFile(), file -> UcConfiguration.this.reload());

            if (!existed) this.save();
            else {
                this.reload(false);
                this.save();
            }
        });
    }

    public void reload() {
        this.reload(true);
    }

    public T get() {
        return this.object;
    }

    public void set(T object) {
        this.object = object;
        this.save();
    }

    @SuppressWarnings("unchecked")
    private void reload(boolean reloadFile) {
        if (this.config == null) return;

        if (reloadFile) {
            try {
                this.config.reload();

                try {
                    this.object = this.yaml.deserialize(Files.readString(this.configPath), (Class<T>) this.object.getClass());
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("Failed to load config file!", e);
                }
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to load config file!", e);
            }
        }
    }

    public void save() {
        if (this.config == null) return;

        YamlElement serialize = this.yaml.serialize(this.config, this.object.getClass());

        try {
            Files.writeString(this.configPath, this.yaml.toYAML(serialize));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
