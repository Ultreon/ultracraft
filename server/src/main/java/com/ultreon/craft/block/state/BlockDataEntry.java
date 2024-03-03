package com.ultreon.craft.block.state;

import com.google.common.base.Preconditions;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.data.types.BooleanType;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.IntType;

public abstract class BlockDataEntry<T> {
    public T value;

    public BlockDataEntry(T value) {
        this.value = value;
    }

    public abstract BlockDataEntry<?> read(PacketBuffer packetBuffer);

    public abstract BlockDataEntry<?> load(IType<?> type);

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <R> BlockDataEntry<R> cast(Class<R> type) {
        if (!type.isAssignableFrom(this.value.getClass())) {
            throw new IllegalArgumentException("Cannot cast " + this.value.getClass() + " to " + type);
        }
        return (BlockDataEntry<R>) this;
    }

    public abstract IType<?> save();

    public abstract void write(PacketBuffer packetBuffer);

    public static class BooleanProperty extends BlockDataEntry<Boolean> {
        public BooleanProperty(boolean value) {
            super(value);
        }

        public BooleanProperty() {
            super(false);
        }

        @Override
        public BlockDataEntry<?> read(PacketBuffer packetBuffer) {
            this.value = packetBuffer.readBoolean();
            return this;
        }

        @Override
        public BlockDataEntry<?> load(IType<?> type) {
            this.value = ((BooleanType) type).getValue();
            return this;
        }

        @Override
        public IType<?> save() {
            return new BooleanType(this.value);
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeBoolean(this.value);
        }
    }

    public static class IntProperty extends BlockDataEntry<Integer> {
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
            this.value = packetBuffer.readInt();
            return this;
        }

        @Override
        public BlockDataEntry<?> load(IType<?> type) {
            this.value = ((IntType) type).getValue();
            return this;
        }

        @Override
        public void setValue(Integer value) {
            Preconditions.checkNotNull(value, "Value cannot be null");

            if (value < min) {
                this.value = min;
            } else if (value > max) {
                this.value = max;
            } else {
                this.value = value;
            }
        }

        @Override
        public IType<?> save() {
            return new IntType(this.value);
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeInt(this.value);
        }
    }

    public static class EnumProperty<T extends Enum<T>> extends BlockDataEntry<T> {
        public EnumProperty(T value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public BlockDataEntry<?> read(PacketBuffer packetBuffer) {
            this.value = (T) this.value.getClass().getEnumConstants()[packetBuffer.readInt()];
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BlockDataEntry<?> load(IType<?> type) {
            this.value = (T) this.value.getClass().getEnumConstants()[((IntType) type).getValue()];
            return this;
        }

        @Override
        public IType<?> save() {
            return new IntType(this.value.ordinal());
        }

        @Override
        public void write(PacketBuffer packetBuffer) {
            packetBuffer.writeInt(this.value.ordinal());
        }
    }
}
