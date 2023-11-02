package com.ultreon.craft.network.api;

import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.api.packet.ModPacket;
import io.netty.handler.codec.DecoderException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class PacketRegisterContext {
    private final NetworkChannel channel;
    private int id;

    PacketRegisterContext(NetworkChannel channel, int id) {
        this.channel = channel;
        this.id = id;
    }


    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends ModPacket<T>> int register(Function<PacketBuffer, T> construct, T... type) {
        final int id = this.id++;
        final Constructor<T> declaredConstructor;

        Class<T> clazz = (Class<T>) type.getClass().getComponentType();

        try {
            declaredConstructor = clazz.getDeclaredConstructor(PacketBuffer.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("ModPacket " + construct.getClass().getName() + " is missing a constructor that takes a FriendlyByteBuf as an argument.", e);
        }

        if (!declaredConstructor.canAccess(null)) {
            try {
                declaredConstructor.setAccessible(true);
            } catch (SecurityException e) {
                throw new RuntimeException("Can't access constructor of " + construct.getClass().getName() + ".", e);
            }
        }

        this.channel.register(
                clazz, ModPacket::toBytes,
                buffer -> {
                    T t;
                    try {
                        t = declaredConstructor.newInstance(buffer);
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Couldn't decode packet " + construct.getClass().getName() + " because it couldn't be instantiated.", e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Couldn't decode packet " + construct.getClass().getName() + " because it couldn't be accessed.", e);
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof DecoderException ex) {
                            throw ex;
                        } else if (e.getCause() instanceof InstantiationException ex) {
                            if (ex.getCause() instanceof DecoderException ex2) {
                                throw ex2;
                            }
                        }
                        throw new RuntimeException("Couldn't decode packet " + construct.getClass().getName() + " because it threw an exception.", e);
                    }
                    return t;
                },
                ModPacket::handlePacket);

        return id;
    }
}
