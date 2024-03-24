package com.ultreon.craft.client.util;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <R, T> Consumer<R> with(T object, BiConsumer<R, T> consumer) {
        return v -> consumer.accept(v, object);
    }

    public static <T> Runnable with(T object, Consumer<T> consumer) {
        return () -> consumer.accept(object);
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }

    public static Vec3d toCoreLibs(Vector3 vector) {
        return new Vec3d(vector.x, vector.y, vector.z);
    }
}
