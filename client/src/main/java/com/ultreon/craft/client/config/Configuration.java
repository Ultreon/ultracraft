package com.ultreon.craft.client.config;

import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.config.entries.*;
import com.ultreon.craft.client.config.gui.ConfigEntry;
import com.ultreon.craft.client.util.DuplicateException;
import com.ultreon.libs.collections.v0.maps.OrderedHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Configuration {
    public static final FileHandle FILE = UltracraftClient.data("settings.txt");

    private static final Map<String, Configuration> CONFIGS = new HashMap<>();
    private final Map<String, ConfigEntry<?>> entries = new OrderedHashMap<>();
    private final FileHandle handle;
    private final String id;

    public Configuration(String id) {
        if (Configuration.CONFIGS.containsKey(id)) {
            throw new DuplicateException("Duplicate configuration id: " + id);
        }

        this.handle = UltracraftClient.getConfigDir().child(id + ".txt");
        this.id = id;

        Configuration.CONFIGS.put(id, this);
    }

    Configuration() {
        this.handle = Configuration.FILE;
        this.id = CommonConstants.NAMESPACE;
    }

    protected ConfigEntry<Boolean> add(String key, boolean defaultValue, String comment) {
        ConfigEntry<Boolean> entry = new BooleanEntry(key, defaultValue, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<Integer> add(String key, int defaultValue, int min, int max, String comment) {
        ConfigEntry<Integer> entry = new IntEntry(key, defaultValue, min, max, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<Long> add(String key, long defaultValue, long min, long max, String comment) {
        ConfigEntry<Long> entry = new LongEntry(key, defaultValue, min, max, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<Float> add(String key, float defaultValue, float min, float max, String comment) {
        ConfigEntry<Float> entry = new FloatEntry(key, defaultValue, min, max, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<Double> add(String key, double defaultValue, double min, double max, String comment) {
        ConfigEntry<Double> entry = new DoubleEntry(key, defaultValue, min, max, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<String> add(String key, String defaultValue, String comment) {
        ConfigEntry<String> entry = new StringEntry(key, defaultValue, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected <T> ConfigEntry<T> add(String key, T defaultValue, Function<String, T> reader, Function<T, String> writer, String comment) {
        ConfigEntry<T> entry = new ConfigEntry<>(key, defaultValue, this) {
            @Override
            protected T read(String text) {
                return reader.apply(text);
            }

            @Override
            public String write() {
                return writer.apply(this.get());
            }

        }.comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    protected ConfigEntry<UUID> add(String key, UUID defaultValue, String comment) {
        ConfigEntry<UUID> entry = new UUIDEntry(key, defaultValue, this).comment(comment);
        this.entries.put(key, entry);

        return entry;
    }

    public void reload() {
        if (!this.handle.exists()) {
            this.save();
        }
        try (BufferedReader reader = new BufferedReader(this.handle.reader())) {
            String s;
            while ((s = reader.readLine()) != null) {
                String[] entryArr = s.split("=", 2);
                if (s.startsWith("#") || entryArr.length <= 1)
                    continue;

                ConfigEntry<?> entry = this.entries.get(entryArr[0]);
                if (entry != null) {
                    entry.readAndSet(entryArr[1]);
                }
            }
        } catch (FileNotFoundException ignored) {
            // ignored
        } catch (Exception e) {
            UltracraftClient.LOGGER.error("Failed to reload configuration", e);
        }
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(this.handle.writer(false))) {
            for (ConfigEntry<?> e : this.entries.values()) {
                String key = e.getKey();
                String value = e.write();

                String comment = e.getComment();
                if (comment != null && !comment.isEmpty()) {
                    writer.write("# ");
                    writer.write(comment.trim().replace("\r\n", " ").replace("\r", " ").replace("\n", " "));
                    writer.newLine();
                }
                writer.write(key);
                writer.write("=");
                writer.write(value);
                writer.newLine();
            }
        } catch (FileNotFoundException ignored) {
            // ignored
        } catch (Exception e) {
            UltracraftClient.LOGGER.error("Failed to save configuration", e);
        }
    }

    public ConfigEntry<?>[] values() {
        return this.entries.values().toArray(new ConfigEntry[0]);
    }

    public String getId() {
        return this.id;
    }

    public FileHandle getHandle() {
        return this.handle;
    }

    @Nullable
    public static Configuration getConfig(String id) {
        return Configuration.CONFIGS.get(id);
    }
}
