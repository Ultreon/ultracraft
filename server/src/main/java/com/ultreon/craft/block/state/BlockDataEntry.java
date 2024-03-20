package com.ultreon.craft.block.state;

import com.google.gson.JsonObject;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.data.types.BooleanType;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.IntType;
import de.marhali.json5.Json5Object;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public abstract class BlockDataEntry<T> {
    public final T value;

    public BlockDataEntry(T value) {
        this.value = value;
    }

    public static <T extends Enum<T>> BlockDataEntry<T> ofEnum(T value) {
        return new EnumProperty<>(value);
    }

    public static BlockDataEntry<Integer> of(int value, int min, int max) {
        return new IntProperty(value, min, max);
    }

    public static BlockDataEntry<Boolean> of(boolean value) {
        return new BooleanEntry(value);
    }

    public abstract BlockDataEntry<?> read(PacketBuffer packetBuffer);

    public abstract BlockDataEntry<?> load(IType<?> type);

    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <R> BlockDataEntry<R> cast(Class<R> type) {
        if (!type.isAssignableFrom(this.value.getClass())) {
            throw new IllegalArgumentException("Cannot cast " + this.value.getClass() + " to " + type);
        }
        return (BlockDataEntry<R>) this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public abstract IType<?> save();

    public abstract void write(PacketBuffer packetBuffer);

    public abstract BlockDataEntry<?> parse(Json5Object overrideObj);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockDataEntry<?> that = (BlockDataEntry<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public abstract BlockDataEntry<T> copy();

    public abstract BlockDataEntry<T> with(T apply);

    public BlockDataEntry<T> map(Function<T, T> o) {
        return this.with(o.apply(value));
    }

    private static class BooleanEntry extends BlockDataEntry<Boolean> {
        public BooleanEntry(boolean value) {
            super(value);
        }

        public BooleanEntry() {
            super(false);
        }

        @Override
        public BlockDataEntry<?> read(PacketBuffer packetBuffer) {
            return this.with(packetBuffer.readBoolean());
        }

        @Override
        public BlockDataEntry<?> load(IType<?> type) {
            return this.with(((BooleanType) type).getValue());
        }

        @Override
        public IType<?> save() {
            return new BooleanType(this.value);
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(this.value);
        }

        @Override
        public BlockDataEntry<?> parse(Json5Object overrideObj) {
            return this.with(overrideObj.get("value").getAsBoolean());
        }

        @Override
        public BlockDataEntry<Boolean> copy() {
            return new BooleanEntry(this.value);
        }

        @Override
        public BlockDataEntry<Boolean> with(Boolean apply) {
            return new BooleanEntry(apply);
        }
    }

    private static class IntProperty extends BlockDataEntry<Integer> {
        private final int min;
        private final int max;

        public IntProperty(int value, int min, int max) {
            super(value);
            this.min = min;
            this.max = max;
        }

        public IntProperty() {
            super(0);

            this.min = 0;
            this.max = 0;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public BlockDataEntry<?> read(PacketBuffer packetBuffer) {
            return this.with(packetBuffer.readInt());
        }

        @Override
        public BlockDataEntry<?> load(IType<?> type) {
            return this.with(((IntType) type).getValue());
        }

        @Override
        public IType<?> save() {
            return new IntType(this.value);
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeInt(this.value);
        }

        @Override
        public BlockDataEntry<?> parse(Json5Object overrideObj) {
            return this.with(overrideObj.get("value").getAsInt());
        }

        @Override
        public BlockDataEntry<Integer> copy() {
            return new IntProperty(this.value, this.min, this.max);
        }

        @Override
        public BlockDataEntry<Integer> with(Integer apply) {
            return new IntProperty(apply, this.min, this.max);
        }
    }

    private static class EnumProperty<T extends Enum<T>> extends BlockDataEntry<T> {
        public EnumProperty(T value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public BlockDataEntry<?> read(PacketBuffer packetBuffer) {
            return this.with((T) this.value.getClass().getEnumConstants()[packetBuffer.readInt()]);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BlockDataEntry<?> load(IType<?> type) {
            return this.with((T) this.value.getClass().getEnumConstants()[((IntType) type).getValue()]);
        }

        @Override
        public IType<?> save() {
            return new IntType(this.value.ordinal());
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeInt(this.value.ordinal());
        }

        @Override
        @SuppressWarnings("unchecked")
        public BlockDataEntry<?> parse(Json5Object overrideObj) {
            return this.with((T) Enum.valueOf(this.value.getClass(), overrideObj.get("value").getAsString().toUpperCase()));
        }

        @Override
        public BlockDataEntry<T> copy() {
            return new EnumProperty<>(this.value);
        }

        @Override
        public BlockDataEntry<T> with(T apply) {
            return new EnumProperty<>(apply);
        }

        @Override
        public String toString() {
            return '"' + this.value.name().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"") + '"';
        }
    }
}
