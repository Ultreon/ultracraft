package com.ultreon.craft.util;

import com.ultreon.craft.CommonConstants;
import net.fabricmc.loader.api.FabricLoader;
import org.fusionyaml.library.FusionYAML;
import org.fusionyaml.library.object.YamlElement;
import org.fusionyaml.library.object.YamlPrimitive;
import org.fusionyaml.library.serialization.ObjectTypeAdapter;
import org.fusionyaml.library.serialization.TypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class YamlIo {
    private static final FusionYAML.@NotNull Builder YAML = YamlIo.getBuilder();

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> T readYaml(String path, T... clazz) {
        Class<?> type = clazz.getClass().componentType();

        try (BufferedReader reader = new BufferedReader(new FileReader(new File(FabricLoader.getInstance().getConfigDir().toString(), path)))) {
            return (T) YAML.build().deserialize(reader.lines().collect(Collectors.joining("\n")), type);
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to read " + path, e);
            return null;
        }
    }

    public static void writeYaml(String path, Object object) {
        if (object == null) return;
        Class<?> type = object.getClass();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(FabricLoader.getInstance().getConfigDir().toString(), path)))) {
            FusionYAML build = YAML.build();
            YamlElement serialize = build.serialize(object, type);
            build.toYAML(serialize, writer);
            writer.flush();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to write " + path, e);
        }
    }

    public static class AttemptTypeAdapter<T> extends TypeAdapter<T> {
        private final Constructor<T> constructor;

        public AttemptTypeAdapter(Class<T> member) {
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

    @NotNull
    public static FusionYAML.Builder getBuilder() {
        FusionYAML.Builder builder = new FusionYAML.Builder().flowStyle(DumperOptions.FlowStyle.BLOCK).onlyExposed(true);
        builder.addTypeAdapter(new ElementIDAdapter(), Identifier.class);
        return builder;
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
}
