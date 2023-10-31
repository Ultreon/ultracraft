package com.ultreon.craft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.Inflater;

public class DeflateDecoder extends ByteToMessageDecoder {
    private final boolean logOversized;
    private final Inflater inflater;

    public DeflateDecoder(boolean logOversized) {
        this.logOversized = logOversized;
        this.inflater = new Inflater();
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() != 0) {
            PacketBuffer buffer = new PacketBuffer(in);

//            int length = buffer.readVarInt();
//            if (this.logOversized && length > 1024 * 1024 * 8) {
//                Connection.LOGGER.warn("Received oversized packet: {} bytes", length);
//            }

//            if (length > 0) {
//                byte[] data = new byte[buffer.readableBytes()];
//
//                try {
//                    Files.write(Paths.get("packet_decoding_compressed_" + System.currentTimeMillis() + ".ucpacketdump"), data);
//                } catch (Exception e) {
//                    Connection.LOGGER.error("Failed to dump packet buffer to file:", e);
//                }
//
//                buffer.readBytes(data);
//                this.inflater.setInput(data);
//
//                ByteBuf outputBuf = Unpooled.buffer(length);
//
//                try (FileOutputStream fos = new FileOutputStream("packet_decoding_decompressed_" + System.currentTimeMillis() + ".ucpacket")) {
//                    while (!this.inflater.finished()) {
//                        byte[] buf = new byte[1024];
//                        int read = this.inflater.inflate(buf);
//                        if (read == 0) {
//                            break;
//                        }
//
//                        outputBuf.writeBytes(buf, 0, read);
//                        fos.write(buf, 0, read);
//                    }
//                } catch (Exception e) {
//                    Connection.LOGGER.error("Failed to dump packet buffer to file:", e);
//                }
//            } else {
                // Read packet uncompressed
                byte[] data = new byte[buffer.readableBytes()];
                buffer.readBytes(data);

                try {
                    Files.write(Paths.get("packet_decoding_" + System.currentTimeMillis() + ".ucpacketdump"), data);
                } catch (Exception e) {
                    Connection.LOGGER.error("Failed to dump packet buffer to file:", e);
                }

                ByteBuf byteBuf = Unpooled.wrappedBuffer(data, 0, data.length);
                out.add(byteBuf);
//            }
        }
    }
}