package com.ultreon.craft.world;

import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.util.HexTable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class Palette<T> {
    private final OrderedMap<Index, T> indexMap = new OrderedMap<>();
    private final OrderedMap<T, Index> objectMap = new OrderedMap<>();
    private final List<Index> indices = new ArrayList<>();
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private short size = 0;

    public Palette(Encoder<T> encoder, Decoder<T> decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    // Add a block to the palette
    public Index add(T obj) {
        var ref = new Object() {
            Index index = Palette.this.objectMap.get(obj);
        };

        Optional<Index> any = this.indices.stream().filter(loop -> ref.index != null && loop.value == ref.index.value).findAny();
        if (any.isPresent()) {
            return any.get();
        }

        ref.index = new Index(this.size++);
        this.objectMap.put(obj, ref.index);
        this.indexMap.put(ref.index, obj);
        this.indices.add(ref.index);
        return ref.index;
    }

    // Remove a block from the palette
    public boolean remove(T obj) {
        var ref = new Object() {
            final Index index = Palette.this.objectMap.get(obj);

        };

        if (ref.index != null) {
            Index index = ref.index;

            // Update the index mapping for other blocks
            this.indices.removeIf(loop -> loop.value == index.value);

            int i = this.objectMap.indexOf(obj);
            this.objectMap.removeEntry(i);
            this.indexMap.removeEntry(i);
            this.indexMap.get(index);
            int value = index.getValue();
            index.invalidate();
            for (Index key : this.indices) {
                key.update(value);
            }

            this.size--;
            return true;
        }
        return false;
    }

    // Get the block from an index
    public T get(Index index) {
        return this.objectMap.entrySet().stream().filter(indexTEntry -> indexTEntry.getValue().value == index.value).map(Map.Entry::getKey).findFirst().orElse(null);
    }


    // Get the block from an index
    public Index get(T index) {
        return this.objectMap.get(index);
    }

    // Serialize the palette to a byte array
    public byte[] serializePalette() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        dos.writeShort(this.indexMap.size());
        int i = 0;
        for (T obj : this.indexMap.valueList()) {
            if (obj == null) throw new IllegalStateException("Chunk has not defined every block, undefined value at " + i);
            this.encoder.encode(dos, obj);
            i++;
        }

        dos.close();
        return bos.toByteArray();
    }

    // Deserialize the palette from a byte array
    public void deserializePalette(byte[] data) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);

        this.objectMap.clear();
        this.indexMap.clear();
        this.indices.clear();
        this.size = 0;

        int size = dis.readUnsignedShort();
        for (int i = 0; i < size; i++) {
            T obj = this.decoder.decode(dis);
            this.add(obj);
        }

        dis.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Palette<?> that = (Palette<?>) o;
        return Objects.equals(this.objectMap, that.objectMap) && Objects.equals(this.indexMap, that.indexMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.objectMap, this.indexMap);
    }

    public void clear() {
        this.objectMap.clear();
        this.indexMap.clear();
        this.indices.clear();
    }

    @Override
    public String toString() {
        return "Palette{\n" +
                "  indexMap=" + this.indexMap + ",\n" +
                "  indices=" + this.indices + "\n" +
                '}';
    }

    public static class Index implements Comparable<Index> {
        public static final Index INVALID = new Index(-1) {
            @Override
            void update(int value) {
                throw new UnsupportedOperationException();
            }
        };
        private short value;
    
        public Index(short value) {
            this.value = value;
        }

        public Index(int value) {
            this((short) value);
        }

        public short getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Index index)) return false;
//            if (this.value == -1 && index.value == -1) return true;
            return this.getValue() == index.getValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getValue());
        }

        @Override
        public String toString() {
            return "(" + this.value + ")";
        }
    
        void update(int value) {
            if (this.value == -1 || value == -1) return;
            if (this.value > value) {
                this.value--;
            } else if (this.value == value) {
                throw new ArithmeticException("Removed value is the value this key references.");
            }
        }

        public void invalidate() {
            this.value = -1;
        }

        public boolean isInvalid() {
            return this.value == -1;
        }

        @Override
        public int compareTo(@NotNull Index o) {
            return Short.compare(this.value, o.value);
        }
    }

    public interface Encoder<T> {
        void encode(DataOutputStream output, T value) throws IOException;
    }

    public interface Decoder<T> {
        T decode(DataInputStream output) throws IOException;
    }
}
