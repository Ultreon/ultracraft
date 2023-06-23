package com.ultreon.craft.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.config.entries.*;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.libs.collections.v0.maps.OrderedHashMap;

import java.io.*;
import java.util.UUID;

public class Config {
    public static final FileHandle FILE = Gdx.files.external("options.txt");

    private static final OrderedHashMap<String, ConfigEntry<?>> ENTRIES = new OrderedHashMap<>();
    private final FileHandle file;
    private final String id;

    public Config(String id) {
        this.file = getConfigDir().child(id + ".txt");
        this.id = id;
    }

    private ConfigEntry<Boolean> add(String key, boolean defaultValue, String comment) {
        ConfigEntry<Boolean> entry = new BooleanEntry(key, defaultValue, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<Integer> add(String key, int defaultValue, int min, int max, String comment) {
        ConfigEntry<Integer> entry = new IntEntry(key, defaultValue, min, max, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<Long> add(String key, long defaultValue, long min, long max, String comment) {
        ConfigEntry<Long> entry = new LongEntry(key, defaultValue, min, max, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<Float> add(String key, float defaultValue, float min, float max, String comment) {
        ConfigEntry<Float> entry = new FloatEntry(key, defaultValue, min, max, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<Double> add(String key, double defaultValue, double min, double max, String comment) {
        ConfigEntry<Double> entry = new DoubleEntry(key, defaultValue, min, max, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<String> add(String key, String defaultValue, String comment) {
        ConfigEntry<String> entry = new StringEntry(key, defaultValue, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    private ConfigEntry<UUID> add(String key, UUID defaultValue, String comment) {
        ConfigEntry<UUID> entry = new UUIDEntry(key, defaultValue, this).comment(comment);
        ENTRIES.put(key, entry);

        return entry;
    }

    public static void load() {
        try (BufferedReader reader = new BufferedReader(FILE.reader())) {
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("#")) {
                    continue;
                }
                String[] entryArr = s.split("=", 2);
                if (entryArr.length <= 1) {
                    continue;
                }

                ConfigEntry<?> entry = ENTRIES.get(entryArr[0]);
                if (entry != null) {
                    entry.readAndSet(entryArr[1]);
                }
            }
        } catch (FileNotFoundException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(FILE.writer(false))) {
            for (ConfigEntry<?> e : ENTRIES.values()) {
                String key = e.getKey();
                String value = e.write();

                String comment = e.getComment();
                if (comment != null && !comment.isBlank()) {
                    writer.write("# ");
                    writer.write(comment.trim().replaceAll("\r\n", " ").replaceAll("\r", " ").replaceAll("\n", " "));
                    writer.newLine();
                }
                writer.write(key);
                writer.write("=");
                writer.write(value);
                writer.newLine();
            }
        } catch (FileNotFoundException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FileHandle getConfigDir() {
        return UltreonCraft.CONFIG_DIR;
    }

    public static ConfigEntry<?>[] values() {
        return ENTRIES.values().toArray(new ConfigEntry[0]);
    }

    public String getId() {
        return this.id;
    }

    public FileHandle getFile() {
        return this.file;
    }
}
