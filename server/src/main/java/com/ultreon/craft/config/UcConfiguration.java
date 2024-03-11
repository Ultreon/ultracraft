package com.ultreon.craft.config;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.core.util.FileWatcher;
import org.fusionyaml.library.FusionYAML;
import org.fusionyaml.library.object.YamlElement;
import org.fusionyaml.library.object.YamlPrimitive;
import org.fusionyaml.library.serialization.ObjectTypeAdapter;
import org.fusionyaml.library.serialization.TypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

public final class UcConfiguration<T> {
    private final FusionYAML yaml;

    private T object;
    private Path configPath;
    public final Event<Reload> event = Event.create();
    private final FileWatcher fileWatcher = file -> UcConfiguration.this.reload();

    public UcConfiguration(String name, EnvType configEnv, T object) {
        FusionYAML.Builder builder = getBuilder();
        for (var member : object.getClass().getNestMembers()) {
            builder.addTypeAdapter(new MyTypeAdapter<>(member), member);
        }

        yaml = builder.build();

        ConfigEvents.LOAD.subscribe(loadingEnv -> this.load(name, configEnv, object, loadingEnv));
    }

    @NotNull
    private static FusionYAML.Builder getBuilder() {
        FusionYAML.Builder builder = new FusionYAML.Builder().flowStyle(DumperOptions.FlowStyle.BLOCK).onlyExposed(true);
        builder.addTypeAdapter(new ElementIDAdapter(), Identifier.class);
        return builder;
    }

    @SuppressWarnings({"unchecked"})
    private void load(String name, EnvType configEnv, T object, EnvType loadingEnv) {
        if (loadingEnv != configEnv) return;

        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(name + ".yml");
        boolean existed = Files.exists(this.configPath);
        if (Files.notExists(this.configPath)) {
            try {
                var element = this.yaml.serialize(object, object.getClass());

                Files.createDirectories(this.configPath.getParent());
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.configPath)) {
                    this.yaml.toYAML(element, bufferedWriter);
                }
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to create config file!", e);
                return;
            }
        }

        try {
            this.object = this.yaml.deserialize(Files.readString(this.configPath), (Class<T>) object.getClass());
        } catch (IOException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
            return;
        } catch (Exception e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
            this.object = object;
            this.save();
            return;
        }

        if (this.object == null) {
            CommonConstants.LOGGER.warn("Config file {} was empty", this.configPath);
            this.object = object;
            this.save();
        }

        if (!existed) {
            CommonConstants.LOGGER.info("Config file {} was created", this.configPath);
            this.save();
        } else {
            CommonConstants.LOGGER.info("Config file {} was loaded", this.configPath);
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
        try {
            var object = this.object;
            this.object = this.yaml.deserialize(Files.readString(this.configPath), (Class<T>) this.object.getClass());

            if (this.object == null) {
                this.object = object;
                this.save();
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
        }
    }

    public boolean save() {
        UltracraftServer.getWatchManager().unwatchFile(this.configPath.toFile());

        YamlElement serialize = this.yaml.serialize(this.object, this.object.getClass());
        if (serialize == null) {
            return false;
        }
        if (serialize.getAsYamlObject().keySet().isEmpty()) {
            throw new RuntimeException("Failed to serialize object: " + this.object);
        }


        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.configPath)) {
            this.yaml.toYAML(serialize, bufferedWriter);
        } catch (IOException e) {
            UltracraftServer.getWatchManager().watchFile(this.configPath.toFile(), fileWatcher);
            return false;
        }
        this.event.factory().onReload();
        UltracraftServer.getWatchManager().watchFile(this.configPath.toFile(), fileWatcher);
        return true;
    }

    @FunctionalInterface
    public interface Reload {
        void onReload();
    }

    private static class ElementIDAdapter extends TypeAdapter<Identifier> {
        @Override
        public YamlElement serialize(Identifier obj, Type type) {
            if (obj == null) {
                return null;
            }
            return new YamlPrimitive(obj.toString());
        }

        @Override
        public Identifier deserialize(YamlElement element, Type type) {
            if (element == null) {
                return null;
            }
            return Identifier.tryParse(element.toString());
        }
    }

    private static class MyTypeAdapter<T> extends TypeAdapter<T> {
        private final Constructor<T> constructor;

        public MyTypeAdapter(Class<T> member) {
            super();

            try {
                this.constructor = member.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public YamlElement serialize(Object obj, Type type) {
            return new ObjectTypeAdapter<>(new FusionYAML()).serialize(obj, type);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T deserialize(YamlElement element, Type type) {
            if (element == null) {
                try {
                    return constructor.newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            Object deserialize = new ObjectTypeAdapter<>(new FusionYAML()).deserialize(element, type);
            if (deserialize == null) {
                try {
                    return constructor.newInstance();
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return (T) deserialize;
        }
    }
}
