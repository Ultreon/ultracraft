package com.ultreon.craft.network;

import com.ultreon.craft.util.AES256;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class DecryptionHandler extends MessageToByteEncoder<ByteBuf> {
    private final SecretKey key;

    public DecryptionHandler(SecretKey key) {
        this.key = key;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readableBytes = msg.readableBytes();
        if (readableBytes == 0) return;
        if (readableBytes > 8388608)
            throw new DecoderException("Encrypted packet is too large");

        // Decrypt the packet
        byte[] encrypted = new byte[readableBytes];
        msg.readBytes(encrypted);

        byte[] decrypted = AES256.decrypt(encrypted, this.key);
        out.writeBytes(decrypted);
    }

    public SecretKey getHmac() {
        return this.key;
    }
}
