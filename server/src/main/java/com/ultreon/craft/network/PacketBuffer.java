package com.ultreon.craft.network;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.data.TypeRegistry;
import com.ultreon.data.types.IType;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.util.EnumUtils;
import com.ultreon.libs.commons.v0.vector.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketBuffer extends ByteBuf {
    private static final int MAX_UBO_SIZE = 1024 * 1024 * 2;
    private final ByteBuf buf;

    public PacketBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    public String readUTF(int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        int len = this.readVarInt();
        if (len > max) throw new PacketOverflowException("string", len, max);
        byte[] bytes = new byte[len];
        this.buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @CanIgnoreReturnValue
    public ByteBuf writeUTF(String string, int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new PacketOverflowException("string", bytes.length, max);
        this.writeVarInt(bytes.length);
        this.buf.writeBytes(bytes);
        return this.buf;
    }

    public byte[] readByteArray(int max) {
        if (max < 0) throw new IllegalArgumentException(CommonConstants.EX_INVALID_DATA);
        int len = this.buf.readInt();
        if (len > max) throw new PacketOverflowException("byte array", len, max);
        byte[] bytes = new byte[len];
        this.buf.readBytes(bytes);
        return bytes;
    }

    public void writeByteArray(byte[] array, int max) {
        if (array.length > max) throw new DataOverflowException("byte array", array.length, max);
        this.buf.writeInt(array.length);
        this.buf.writeBytes(array);
    }

    public Identifier readId() {
        var location = this.readUTF(100);
        var path = this.readUTF(200);
        return new Identifier(location, path);
    }

    public void writeId(Identifier id) {
        this.writeUTF(id.namespace(), 100);
        this.writeUTF(id.path(), 200);
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.buf.readUnsignedByte();
    }

    @Override
    public ByteBuf writeByte(int value) {
        return this.buf.writeByte(value);
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return this.buf.readUnsignedShort();
    }

    @Override
    public ByteBuf writeShort(int value) {
        return this.buf.writeShort(value);
    }

    @Override
    public int readInt() {
        return this.buf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return this.buf.readUnsignedInt();
    }

    @Override
    public ByteBuf writeInt(int value) {
        return this.buf.writeInt(value);
    }

    @Override
    public long readLong() {
        return this.buf.readLong();
    }

    @Override
    public ByteBuf writeLong(long value) {
        return this.buf.writeLong(value);
    }

    @Override
    public float readFloat() {
        return this.buf.readFloat();
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return this.buf.writeFloat(value);
    }

    @Override
    public double readDouble() {
        return this.buf.readDouble();
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return this.buf.writeDouble(value);
    }

    @Override
    public short readShortLE() {
        return this.buf.readShortLE();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.buf.readUnsignedShortLE();
    }

    @Override
    public ByteBuf writeShortLE(int value) {
        return this.buf.writeShortLE(value);
    }

    @Override
    public int readIntLE() {
        return this.buf.readIntLE();
    }

    @Override
    public ByteBuf writeIntLE(int value) {
        return this.buf.writeIntLE(value);
    }

    @Override
    public long readUnsignedIntLE() {
        return this.buf.readUnsignedIntLE();
    }

    @Override
    public long readLongLE() {
        return this.buf.readLongLE();
    }

    @Override
    public ByteBuf writeLongLE(long value) {
        return this.buf.writeLongLE(value);
    }

    @Override
    public float readFloatLE() {
        return this.buf.readFloatLE();
    }

    @Override
    public ByteBuf writeFloatLE(float value) {
        return this.buf.writeFloatLE(value);
    }

    @Override
    public double readDoubleLE() {
        return this.buf.readDoubleLE();
    }

    @Override
    public char readChar() {
        return this.buf.readChar();
    }

    public ByteBuf writeChar(char value) {
        return this.buf.writeChar(value);
    }

    @Override
    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return this.buf.writeBoolean(value);
    }

    public UUID readUuid() {
        long mostSigBits = this.readLong();
        long leastSigBits = this.readLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public void writeUuid(UUID value) {
        long mostSigBits = value.getMostSignificantBits();
        long leastSigBits = value.getLeastSignificantBits();
        this.buf.writeLong(mostSigBits);
        this.buf.writeLong(leastSigBits);
    }

    public BitSet readBitSet() {
        int size = this.readVarInt();
        byte[] bytes = new byte[size];
        this.buf.readBytes(bytes);
        return BitSet.valueOf(bytes);
    }

    public ByteBuf writeBitSet(BitSet value) {
        byte[] bytes = value.toByteArray();
        this.writeVarInt(bytes.length);
        this.buf.writeBytes(bytes);
        return this;
    }

    public Vec2f readVec2f() {
        float x = this.readFloat();
        float y = this.readFloat();

        return new Vec2f(x, y);
    }

    public void writeVec2f(Vec2f vec) {
        this.buf.writeFloat(vec.x);
        this.buf.writeFloat(vec.y);
    }

    public Vec3f readVec3f() {
        float x = this.readFloat();
        float y = this.readFloat();
        float z = this.readFloat();

        return new Vec3f(x, y, z);
    }

    public void writeVec3f(Vec3f vec) {
        this.buf.writeFloat(vec.x);
        this.buf.writeFloat(vec.y);
        this.buf.writeFloat(vec.z);
    }

    public Vec4f readVec4f() {
        float x = this.readFloat();
        float y = this.readFloat();
        float z = this.readFloat();
        float w = this.readFloat();

        return new Vec4f(x, y, z, w);
    }

    public void writeVec4f(Vec4f vec) {
        this.buf.writeFloat(vec.x);
        this.buf.writeFloat(vec.y);
        this.buf.writeFloat(vec.z);
        this.buf.writeFloat(vec.w);
    }

    public Vec2d readVec2d() {
        double x = this.readDouble();
        double y = this.readDouble();

        return new Vec2d(x, y);
    }

    public void writeVec2f(Vec2d vec) {
        this.buf.writeDouble(vec.x);
        this.buf.writeDouble(vec.y);
    }

    public Vec3d readVec3d() {
        double x = this.readDouble();
        double y = this.readDouble();
        double z = this.readDouble();

        return new Vec3d(x, y, z);
    }

    public void writeVec3d(Vec3d vec) {
        this.buf.writeDouble(vec.x);
        this.buf.writeDouble(vec.y);
        this.buf.writeDouble(vec.z);
    }

    public Vec4d readVec4d() {
        double x = this.readDouble();
        double y = this.readDouble();
        double z = this.readDouble();
        double w = this.readDouble();

        return new Vec4d(x, y, z, w);
    }

    public void writeVec4d(Vec4d vec) {
        this.buf.writeDouble(vec.x);
        this.buf.writeDouble(vec.y);
        this.buf.writeDouble(vec.z);
        this.buf.writeDouble(vec.w);
    }

    public Vec2i readVec2i() {
        int x = this.readInt();
        int y = this.readInt();

        return new Vec2i(x, y);
    }

    public void writeVec2i(Vec2i vec) {
        this.buf.writeInt(vec.x);
        this.buf.writeInt(vec.y);
    }

    public Vec3i readVec3i() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new Vec3i(x, y, z);
    }

    public void writeVec3i(Vec3i vec) {
        this.buf.writeInt(vec.x);
        this.buf.writeInt(vec.y);
        this.buf.writeInt(vec.z);
    }

    public Vec4i readVec4i() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();
        int w = this.readInt();

        return new Vec4i(x, y, z, w);
    }

    public void writeVec4i(Vec4i vec) {
        this.buf.writeInt(vec.x);
        this.buf.writeInt(vec.y);
        this.buf.writeInt(vec.z);
        this.buf.writeInt(vec.w);
    }

    public BlockPos readBlockPos() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new BlockPos(x, y, z);
    }

    @CanIgnoreReturnValue
    public ByteBuf writeBlockPos(BlockPos pos) {
        this.buf.writeInt(pos.x());
        this.buf.writeInt(pos.y());
        this.buf.writeInt(pos.z());
        return this.buf;
    }

    public ChunkPos readChunkPos() {
        int x = this.readInt();
        int y = this.readInt();
        int z = this.readInt();

        return new ChunkPos(x, y, z);
    }

    @CanIgnoreReturnValue
    public ByteBuf writeChunkPos(ChunkPos pos) {
        this.buf.writeInt(pos.x());
        this.buf.writeInt(pos.y());
        this.buf.writeInt(pos.z());
        return this.buf;
    }

    public int readVarInt() {
        int value = 0;
        int shift = 0;
        int byteRead;

        do {
            byteRead = this.readByte();
            value |= (byteRead & 0x7F) << shift;
            shift += 7;
        } while ((byteRead & 0x80)!= 0);

        return value;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeVarInt(int value) {
        while ((value & ~0x7F)!= 0) {
            this.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }

        this.writeByte((byte) value);
        return this.buf;
    }

    public int getVarIntSize(int value) {
        int size = 0;
        while ((value & ~0x7F)!= 0) {
            size++;
            value >>>= 7;
        }
        return size + 1;
    }

    public void writeUbo(IType<?> ubo) {
        this.writeByte(ubo.id());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (var output = new DataOutputStream(bos)) {
            ubo.write(output);
            bos.flush();
        } catch (IOException ignored) {
            try {
                bos.close();
            } catch (IOException e) {
                throw new PacketException(e);
            }
        }

        this.writeByteArray(bos.toByteArray(), PacketBuffer.MAX_UBO_SIZE);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends IType<?>> T readUbo(T... typeGetter) {
        T data;
        int id = this.readUnsignedByte();
        byte[] bytes = this.readByteArray(PacketBuffer.MAX_UBO_SIZE);

        try(DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            Class<?> componentType = typeGetter.getClass().getComponentType();
            if (id != TypeRegistry.getId(componentType)) throw new PacketException("Id doesn't match requested type.");
            data = (T) TypeRegistry.read(TypeRegistry.getId(componentType), stream);
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return data;
    }

    @Override
    public int capacity() {
        return this.buf.capacity();
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return this.buf.capacity(newCapacity);
    }

    @Override
    public int maxCapacity() {
        return this.buf.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.buf.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.buf.order();
    }

    @Override
    public ByteBuf order(ByteOrder order) {
        return this.buf.order(order);
    }

    @Override
    public ByteBuf unwrap() {
        return this.buf.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.buf.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.buf.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.buf.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.buf.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int readerIndex) {
        return this.buf.readerIndex(readerIndex);
    }

    @Override
    public int writerIndex() {
        return this.buf.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int writerIndex) {
        return this.buf.writerIndex(writerIndex);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.buf.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return this.buf.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.buf.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.buf.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.buf.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.buf.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.buf.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.buf.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return this.buf.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.buf.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.buf.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.buf.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.buf.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.buf.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.buf.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minWritableBytes) {
        return this.buf.ensureWritable(minWritableBytes);
    }

    @Override
    public int ensureWritable(int minWritableBytes, boolean force) {
        return this.buf.ensureWritable(minWritableBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.buf.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.buf.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.buf.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.buf.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return this.buf.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.buf.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return this.buf.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return this.buf.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return this.buf.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.buf.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return this.buf.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return this.buf.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return this.buf.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.buf.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return this.buf.getUnsignedIntLE(index);
    }

    @Override
    public long getLong(int index) {
        return this.buf.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return this.buf.getLongLE(index);
    }

    @Override
    public char getChar(int index) {
        return this.buf.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.buf.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.buf.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return this.buf.getBytes(index, dst, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return this.buf.getBytes(index, dst, dstIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return this.buf.getBytes(index, dst);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return this.buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return this.buf.getBytes(index, out, length);
    }

    @Override
    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return this.buf.getBytes(index, out, position, length);
    }

    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.buf.getCharSequence(index, length, charset);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return this.buf.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return this.buf.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return this.buf.setShort(index, value);
    }

    @Override
    public ByteBuf setShortLE(int index, int value) {
        return this.buf.setShortLE(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return this.buf.setMedium(index, value);
    }

    @Override
    public ByteBuf setMediumLE(int index, int value) {
        return this.buf.setMediumLE(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return this.buf.setInt(index, value);
    }

    @Override
    public ByteBuf setIntLE(int index, int value) {
        return this.buf.setIntLE(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return this.buf.setLong(index, value);
    }

    @Override
    public ByteBuf setLongLE(int index, long value) {
        return this.buf.setLongLE(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return this.buf.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return this.buf.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return this.buf.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return this.buf.setBytes(index, src, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return this.buf.setBytes(index, src, srcIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return this.buf.setBytes(index, src);
    }

    @Override
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return this.buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return this.buf.setBytes(index, in, length);
    }

    @Override
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return this.buf.setBytes(index, in, position, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return this.buf.setZero(index, length);
    }

    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.buf.setCharSequence(index, sequence, charset);
    }

    @Override
    public int readMedium() {
        return this.buf.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.buf.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.buf.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.buf.readUnsignedMediumLE();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.buf.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.buf.readSlice(length);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return this.buf.readRetainedSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return this.buf.readBytes(dst, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return this.buf.readBytes(dst, dstIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer dst) {
        return this.buf.readBytes(dst);
    }

    @Override
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return this.buf.readBytes(out, length);
    }

    @Override
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return this.buf.readBytes(out, length);
    }

    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.buf.readCharSequence(length, charset);
    }

    @Override
    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return this.buf.readBytes(out, position, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return this.buf.skipBytes(length);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return this.buf.writeMedium(value);
    }

    @Override
    public ByteBuf writeMediumLE(int value) {
        return this.buf.writeMediumLE(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return this.buf.writeChar(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return this.buf.writeBytes(src, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return this.buf.writeBytes(src, srcIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer src) {
        return this.buf.writeBytes(src);
    }

    @Override
    public int writeBytes(InputStream in, int length) throws IOException {
        return this.buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return this.buf.writeBytes(in, length);
    }

    @Override
    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return this.buf.writeBytes(in, position, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return this.buf.writeZero(length);
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.buf.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return this.buf.indexOf(fromIndex, toIndex, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.buf.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.buf.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.buf.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteProcessor processor) {
        return this.buf.forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        return this.buf.forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor processor) {
        return this.buf.forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return this.buf.forEachByteDesc(index, length, processor);
    }

    @Override
    public ByteBuf copy() {
        return this.buf.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.buf.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.buf.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.buf.retainedSlice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.buf.slice(index, length);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return this.buf.retainedSlice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.buf.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.buf.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.buf.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.buf.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.buf.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.buf.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.buf.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.buf.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.buf.hasArray();
    }

    @Override
    public byte[] array() {
        return this.buf.array();
    }

    @Override
    public int arrayOffset() {
        return this.buf.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.buf.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.buf.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.buf.toString(charset);
    }

    @Override
    public @NotNull String toString(int index, int length, Charset charset) {
        return this.buf.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.buf.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.buf.equals(obj);
    }

    @Override
    public int compareTo(ByteBuf buffer) {
        return this.buf.compareTo(buffer);
    }

    @Override
    public String toString() {
        return this.buf.toString();
    }

    @Override
    public ByteBuf retain(int increment) {
        return this.buf.retain(increment);
    }

    @Override
    public int refCnt() {
        return this.buf.refCnt();
    }

    @Override
    public ByteBuf retain() {
        return this.buf.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.buf.touch();
    }

    @Override
    public ByteBuf touch(Object hint) {
        return this.buf.touch(hint);
    }

    @Override
    public boolean release() {
        return this.buf.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.buf.release(decrement);
    }

    @Override
    public int maxFastWritableBytes() {
        return this.buf.maxFastWritableBytes();
    }

    @Override
    public float getFloatLE(int index) {
        return this.buf.getFloatLE(index);
    }

    @Override
    public double getDoubleLE(int index) {
        return this.buf.getDoubleLE(index);
    }

    @Override
    public ByteBuf setFloatLE(int index, float value) {
        return this.buf.setFloatLE(index, value);
    }

    @Override
    public ByteBuf setDoubleLE(int index, double value) {
        return this.buf.setDoubleLE(index, value);
    }

    @Override
    public ByteBuf writeDoubleLE(double value) {
        return this.buf.writeDoubleLE(value);
    }

    @Override
    public boolean isContiguous() {
        return this.buf.isContiguous();
    }

    @Override
    public ByteBuf asByteBuf() {
        return this.buf.asByteBuf();
    }

    public short[] readShortArray() {
        int len = this.readVarInt();
        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readShort();
        }

        return array;
    }

    public short[] readShortArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readShort();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeShortArray(short[] array) {
        this.writeVarInt(array.length);
        for (short s : array) {
            this.writeShort(s);
        }

        return this.buf;
    }

    public int[] readMediumArray() {
        int len = this.readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readMedium();
        }

        return array;
    }

    public int[] readMediumArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readMedium();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeMediumArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeMedium(i);
        }

        return this.buf;
    }

    public int[] readIntArray() {
        int len = this.readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readInt();
        }

        return array;
    }

    public int[] readIntArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readInt();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeIntArray(int[] array) {
        this.writeVarInt(array.length);
        for (int i : array) {
            this.writeInt(i);
        }

        return this.buf;
    }

    public long[] readLongArray() {
        int len = this.readVarInt();
        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readLong();
        }

        return array;
    }

    public long[] readLongArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readLong();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeLongArray(long[] array) {
        this.writeVarInt(array.length);
        for (long l : array) {
            this.writeLong(l);
        }

        return this.buf;
    }
    public float[] readFloatArray() {
        int len = this.readVarInt();
        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readFloat();
        }

        return array;
    }

    public float[] readFloatArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readFloat();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeFloatArray(float[] array) {
        this.writeVarInt(array.length);
        for (float f : array) {
            this.writeFloat(f);
        }

        return this.buf;
    }
    public double[] readDoubleArray() {
        int len = this.readVarInt();
        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readDouble();
        }

        return array;
    }

    public double[] readDoubleArray(int max) {
        int len = this.readVarInt();
        if (len > max) {
            throw new PacketException(CommonConstants.EX_ARRAY_TOO_LARGE.formatted(max, len));
        }

        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = this.readDouble();
        }

        return array;
    }

    @CanIgnoreReturnValue
    public ByteBuf writeDoubleArray(double[] array) {
        this.writeVarInt(array.length);
        for (double d : array) {
            this.writeDouble(d);
        }

        return this.buf;
    }

    public <T> List<T> readList(Function<PacketBuffer, T> decoder) {
        int size = this.readInt();
        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    public <T> List<T> readList(Function<PacketBuffer, T> decoder, int max) {
        int size = this.readInt();
        if (size > max) {
            throw new PacketException("List too large, max = %d, actual = %d".formatted(max, size));
        }

        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    @CanIgnoreReturnValue
    public <T> ByteBuf writeList(List<T> list, BiConsumer<PacketBuffer, T> encoder) {
        this.writeInt(list.size());
        for (T item : list) {
            encoder.accept(this, item);
        }

        return this.buf;
    }

    public <K, V> Map<K, V> readMap(Function<PacketBuffer, K> keyDecoder, Function<PacketBuffer, V> valueDecoder) {
        int size = this.readInt();
        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> Map<K, V> readMap(Function<PacketBuffer, K> keyDecoder, Function<PacketBuffer, V> valueDecoder, int max) {
        int size = this.readInt();
        if (size > max) {
            throw new PacketException("Map too large, max = %d, actual = %d".formatted(max, size));
        }

        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    @CanIgnoreReturnValue
    public <K, V> ByteBuf writeMap(Map<K, V> map, BiConsumer<PacketBuffer, K> keyEncoder, BiConsumer<PacketBuffer, V> valueEncoder) {
        this.writeMedium(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyEncoder.accept(this, entry.getKey());
            valueEncoder.accept(this, entry.getValue());
        }

        return this.buf;
    }

    public <F, S> Pair<F, S> readPair(Function<PacketBuffer, F> firstDecoder, Function<PacketBuffer, S> secondDecoder) {
        return new Pair<>(firstDecoder.apply(this), secondDecoder.apply(this));
    }

    public <F, S> ByteBuf writePair(Pair<F, S> pair, BiConsumer<PacketBuffer, F> firstEncoder, BiConsumer<PacketBuffer, S> secondEncoder) {
        firstEncoder.accept(this, pair.getFirst());
        secondEncoder.accept(this, pair.getSecond());
        return this.buf;
    }

    public ItemStack readItemStack() {
        return ItemStack.load(this.readUbo());
    }

    @CanIgnoreReturnValue
    public ByteBuf writeItemStack(ItemStack stack) {
        this.writeUbo(stack.save());
        return this.buf;
    }

    public TextObject readTextObject() {
        return TextObject.deserialize(this.readUbo());
    }

    public void writeTextObject(TextObject message) {
        this.writeUbo(message.serialize());
    }

    public <T extends Enum<T>> T readEnum(T fallback) {
        return EnumUtils.byOrdinal(this.readVarInt(), fallback);
    }
}
