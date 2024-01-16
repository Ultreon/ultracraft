package com.ultreon.craft.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Env;
import org.apache.logging.log4j.core.util.WatchManager;
import org.fusionyaml.library.FusionYAML;
import org.fusionyaml.library.configurations.FileConfiguration;
import org.fusionyaml.library.object.YamlElement;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UcConfiguration<T> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final FusionYAML yaml = new FusionYAML.Builder().flowStyle(DumperOptions.FlowStyle.BLOCK).build();
    @Nullable
    private FileConfiguration config = null;

    private T object;
    private Path configPath;
    public final Event<Reload> event = Event.create();

    public UcConfiguration(String name, Env configEnv, T object) {
        if (GamePlatform.get().getEnv() != configEnv) {
            this.config = null;
            return;
        }

        ConfigEvents.LOAD.listen(loadingEnv -> this.load(name, configEnv, object, loadingEnv));
    }

    @SuppressWarnings({"unchecked"})
    private void load(String name, Env configEnv, T object, Env loadingEnv) {
        if (loadingEnv != configEnv) return;

        this.configPath = GamePlatform.get().getConfigDir().resolve(name + ".json");
        boolean existed = Files.exists(this.configPath);
        if (Files.notExists(this.configPath)) {
            try {
                var element = GSON.toJson(object, object.getClass());

                Files.createDirectories(this.configPath.getParent());
                Gdx.files.absolute(this.configPath.toAbsolutePath().toString()).writeString(element, false);
            } catch (IOException | GdxRuntimeException e) {
                CommonConstants.LOGGER.error("Failed to create config file!", e);
                return;
            }
        }

        try {
            this.object = GSON.fromJson(Gdx.files.absolute(this.configPath.toAbsolutePath().toString()).readString(), (Class<T>) object.getClass());
        } catch (GdxRuntimeException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
            return;
        }

        WatchManager watchManager = UltracraftServer.getWatchManager();
        if (watchManager != null) watchManager.watchFile(this.configPath.toFile(), file -> UcConfiguration.this.reload());

        if (!existed) this.save();
        else {
            this.reload(false);
            this.save();
        }
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

    private void reload(boolean reloadFile) {
        if (this.config == null) return;

        if (reloadFile) {
            try {
                this.reloadFromFile();
                this.event.factory().onReload();
            } catch (IOException e) {
                CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
            }
        } else {
            this.event.factory().onReload();
        }
    }

    @SuppressWarnings("unchecked")
    private void reloadFromFile() throws IOException {
        if (this.config == null) {
            return;
        }

        this.config.reload();

        try {
            this.object = this.yaml.deserialize(Files.readString(this.configPath), (Class<T>) this.object.getClass());
        } catch (IOException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
        }
    }

    public boolean save() {
        if (this.config == null) return false;

        YamlElement serialize = this.yaml.serialize(this.config, this.object.getClass());

        try {
            Files.writeString(this.configPath, this.yaml.toYAML(serialize));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @FunctionalInterface
    public interface Reload {
        void onReload();
    }
}
