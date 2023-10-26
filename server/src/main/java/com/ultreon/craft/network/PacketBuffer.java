package com.ultreon.craft.network;

import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.data.DataIo;
import com.ultreon.data.types.IType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.*;
import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketBuffer {
    private static final int MAX_UBO_SIZE = 1024 * 1024 * 16;
    private final ByteBuf buf;

    public PacketBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    @Deprecated
    public String readString() {
        int len = this.buf.readInt();
        return this.buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    public String readString(int max) {
        int len = this.buf.readInt();
        if (len > max) throw new PacketOverflowException("string", len, max);
        return this.buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    public void writeString(String s, int max) {
        if (s.length() > max) throw new DataOverflowException("string", s.length(), max);
        this.buf.writeInt(s.length());
        this.buf.writeCharSequence(s, StandardCharsets.UTF_8);
    }

    public byte[] readByteArray(int max) {
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
        var location = this.readString(100);
        var path = this.readString(200);
        return new Identifier(location, path);
    }

    public void writeId(Identifier id) {
        this.writeString(id.location(), 100);
        this.writeString(id.path(), 200);
    }

    public byte readByte() {
        return this.buf.readByte();
    }

    public short readUnsignedByte() {
        return this.buf.readUnsignedByte();
    }

    public void writeByte(int value) {
        this.buf.writeByte(value);
    }

    public short readShort() {
        return this.buf.readShort();
    }

    public int readUnsignedShort() {
        return this.buf.readUnsignedShort();
    }

    public void writeShort(int value) {
        this.buf.writeShort(value);
    }

    public int readInt() {
        return this.buf.readInt();
    }

    public long readUnsignedInt() {
        return this.buf.readUnsignedInt();
    }

    public void writeInt(int value) {
        this.buf.writeInt(value);
    }

    public long readLong() {
        return this.buf.readLong();
    }

    public void writeLong(long value) {
        this.buf.writeLong(value);
    }

    public float readFloat() {
        return this.buf.readFloat();
    }

    public void writeFloat(float value) {
        this.buf.writeFloat(value);
    }

    public double readDouble() {
        return this.buf.readDouble();
    }

    public void writeDouble(double value) {
        this.buf.writeDouble(value);
    }

    public short readShortLE() {
        return this.buf.readShortLE();
    }

    public int readUnsignedShortLE() {
        return this.buf.readUnsignedShortLE();
    }

    public void writeShortLE(int value) {
        this.buf.writeShortLE(value);
    }

    public int readIntLE() {
        return this.buf.readIntLE();
    }

    public void writeIntLE(int value) {
        this.buf.writeIntLE(value);
    }

    public long readUnsignedIntLE() {
        return this.buf.readUnsignedIntLE();
    }

    public long readLongLE() {
        return this.buf.readLongLE();
    }

    public void writeLongLE(long value) {
        this.buf.writeLongLE(value);
    }

    public float readFloatLE() {
        return this.buf.readFloatLE();
    }

    public void writeFloatLE(float value) {
        this.buf.writeFloatLE(value);
    }

    public double readDoubleLE() {
        return this.buf.readDoubleLE();
    }

    public void writeDoubleLE(double value) {
        this.buf.writeDoubleLE(value);
    }

    public char readChar() {
        return this.buf.readChar();
    }

    public void writeChar(char value) {
        this.buf.writeChar(value);
    }

    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    public void writeBoolean(boolean value) {
        this.buf.writeBoolean(value);
    }

    public UUID readUuid() {
        long mostSigBits = this.buf.readLong();
        long leastSigBits = this.buf.readLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public void writeUuid(UUID value) {
        long mostSigBits = value.getMostSignificantBits();
        long leastSigBits = value.getLeastSignificantBits();
        this.buf.writeLong(mostSigBits);
        this.buf.writeLong(leastSigBits);
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

    public void writeBlockPos(BlockPos vec) {
        this.buf.writeInt(vec.x());
        this.buf.writeInt(vec.y());
        this.buf.writeInt(vec.z());
    }

    public ChunkPos readChunkPos() {
        int x = this.readInt();
        int z = this.readInt();

        return new ChunkPos(x, z);
    }

    public void writeChunkPos(ChunkPos vec) {
        this.buf.writeInt(vec.x());
        this.buf.writeInt(vec.z());
    }

    public void writeUbo(IType<?> ubo) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            DataIo.write(ubo, bos);
            bos.flush();
            bos.close();
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
    public final <T extends IType<?>> T readUbo(T... typeGetter) {
        T data = null;
        byte[] bytes = this.readByteArray(PacketBuffer.MAX_UBO_SIZE);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            data = DataIo.read(bis, typeGetter);
            bis.close();
        } catch (IOException ignored) {
            try {
                bis.close();
            } catch (IOException e) {
                throw new PacketException(e);
            }
        }
        return data;
    }
}
