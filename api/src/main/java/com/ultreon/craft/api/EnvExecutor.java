package com.ultreon.craft.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.function.Supplier;

public class EnvExecutor {
    public static void runInEnv(EnvType env, Supplier<Runnable> runnable) {
        if (FabricLoader.getInstance().getEnvironmentType() == env) {
            runnable.get().run();
        }
    }

    public static <T> Optional<T> getInEnv(EnvType env, Supplier<Supplier<T>> supplier) {
        return FabricLoader.getInstance().getEnvironmentType() == env ? Optional.of(supplier.get().get()) : Optional.empty();
    }

    public static <T> T getInEnv(Supplier<Supplier<T>> clientSupplier, Supplier<Supplier<T>> serverSupplier) {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? clientSupplier.get().get() : serverSupplier.get().get();
    }

    public static void runInEnv(Supplier<Runnable> clientRunnable, Supplier<Runnable> serverRunnable) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) clientRunnable.get().run();
        else serverRunnable.get().run();
    }
}
