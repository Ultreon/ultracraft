package com.ultreon.craft;

public record LoadingContext(String namespace) {
    private static LoadingContext currentContext;

    public static LoadingContext get() {
        return LoadingContext.currentContext;
    }

    public static void withinContext(LoadingContext context, Runnable runnable) {
        LoadingContext.currentContext = context;
        runnable.run();
        LoadingContext.currentContext = null;
    }
}
