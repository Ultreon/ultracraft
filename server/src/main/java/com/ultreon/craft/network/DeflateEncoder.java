package com.ultreon.craft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.Deflater;

public class DeflateEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Deflater deflater;
    private final byte[] dataBuffer = new byte[8192];

    public DeflateEncoder() {
        this.deflater = new Deflater();
    }

    protected void encode(ChannelHandlerContext ctx, ByteBuf source, ByteBuf dest) {
        int length = source.readableBytes();
        PacketBuffer buffer = new PacketBuffer(dest);

//        if (length < 256) {
//            buffer.writeVarInt(0);
            byte[] bytes = new byte[length];
            source.readBytes(bytes);

            try {
                Files.write(Paths.get("packet_encoding_" + System.currentTimeMillis() + ".ucpacket"), bytes);
            } catch (IOException e) {
                Connection.LOGGER.error("Failed to dump packet buffer to file:", e);
            }

            buffer.writeBytes(bytes);
            return;
//        }
//
//        buffer.writeVarInt(length);
//
//        byte[] data = new byte[source.readableBytes()];
//        source.readBytes(data);
//        this.deflater.setInput(data);
//
//        try (FileOutputStream fos = new FileOutputStream("packet_encoding_compressed_" + System.currentTimeMillis() + ".ucpacketdump")) {
//            while (!this.deflater.finished()) {
//                int count = this.deflater.deflate(this.dataBuffer);
//
//                if (count == 0) {
//                    break;
//                }
//                buffer.writeBytes(this.dataBuffer, 0, count);
//                fos.write(this.dataBuffer, 0, count);
//            }
//        } catch (IOException e) {
//            Connection.LOGGER.error("Failed to dump packet buffer to file:", e);
//        }
//
//        this.deflater.reset();
    }
}
