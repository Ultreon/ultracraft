package com.ultreon.craft.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.util.Env;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final FusionYAML yaml;

    private T object;
    private Path configPath;
    public final Event<Reload> event = Event.create();
    private final FileWatcher fileWatcher = file -> UcConfiguration.this.reload();

    public UcConfiguration(String name, Env configEnv, T object) {
        FusionYAML.Builder builder = getBuilder();
        for (var member : object.getClass().getClasses()) {
            builder.addTypeAdapter(new MyTypeAdapter<>(member), member);
        }

        yaml = builder.build();

        ConfigEvents.LOAD.listen(loadingEnv -> this.load(name, configEnv, object, loadingEnv));
    }

    @NotNull
    private static FusionYAML.Builder getBuilder() {
        FusionYAML.Builder builder = new FusionYAML.Builder().flowStyle(DumperOptions.FlowStyle.BLOCK).onlyExposed(true);
        builder.addTypeAdapter(new ElementIDAdapter(), ElementID.class);
        return builder;
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
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to create config file!", e);
                return;
            }
        }

        try {
            this.object = GSON.fromJson(Gdx.files.absolute(this.configPath.toAbsolutePath().toString()).readString(), (Class<T>) object.getClass());
        } catch (GdxRuntimeException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
            return;
        } catch (Exception e) {
            this.object = object;
            this.save();
            return;
        }

        if (this.object == null) {
            this.object = object;
            this.save();
        }

        if (!existed) {
            this.save();
        } else {
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
            this.object = GSON.fromJson(Gdx.files.external(this.configPath.toString()).readString(), (Class<T>) this.object.getClass());

            if (this.object == null) {
                this.object = object;
                this.save();
            }
        } catch (GdxRuntimeException e) {
            CommonConstants.LOGGER.error(CommonConstants.EX_FAILED_TO_LOAD_CONFIG, e);
        }
    }

    public boolean save() {
        JsonElement serialize = GSON.toJsonTree(this.object, this.object.getClass());
        if (serialize == null) {
            return false;
        }
        if (serialize.getAsJsonObject().keySet().isEmpty()) {
            throw new RuntimeException("Failed to serialize object: " + this.object);
        }


        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.configPath)) {
            GSON.toJson(serialize, bufferedWriter);
        } catch (IOException e) {
            UltracraftServer.getWatchManager().watchFile(this.configPath.toFile(), fileWatcher);
            return false;
        }
        this.event.factory().onReload();
        return true;
    }

    @FunctionalInterface
    public interface Reload {
        void onReload();
    }

    private static class ElementIDAdapter extends TypeAdapter<ElementID> {
        @Override
        public YamlElement serialize(ElementID obj, Type type) {
            if (obj == null) {
                return null;
            }
            return new YamlPrimitive(obj.toString());
        }

        @Override
        public ElementID deserialize(YamlElement element, Type type) {
            if (element == null) {
                return null;
            }
            return ElementID.tryParse(element.toString());
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
