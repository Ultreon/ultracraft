package com.ultreon.craft.network;

import com.ultreon.craft.util.AES256;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.SecretKey;

public class EncryptionHandler extends MessageToByteEncoder<ByteBuf> {
    private final SecretKey key;

    public EncryptionHandler(SecretKey key) {
        this.key = key;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        // Encrypt the packet
        byte[] unencrypted = new byte[msg.readableBytes()];
        msg.readBytes(unencrypted);

        byte[] encrypted = AES256.encrypt(unencrypted, this.key);
        out.writeBytes(encrypted);
    }

    public SecretKey getHmac() {
        return this.key;
    }
}
