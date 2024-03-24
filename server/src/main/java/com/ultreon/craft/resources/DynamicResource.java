package com.ultreon.craft.resources;

public abstract class DynamicResource implements Resource {
    private byte[] data;

    protected DynamicResource() {

    }

    public static DynamicResource of(Loader loader) {
        return new DynamicResource() {

            @Override
            protected byte[] dynamicLoad() {
                return loader.get();
            }
        };
    }

    @Override
    public void load() {
        if (this.data == null) {
            this.data = this.dynamicLoad();
        }
    }

    @Override
    public boolean isLoaded() {
        return this.data != null;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    protected abstract byte[] dynamicLoad();
    @FunctionalInterface
    public interface Loader {

        byte[] get();
    }
}
