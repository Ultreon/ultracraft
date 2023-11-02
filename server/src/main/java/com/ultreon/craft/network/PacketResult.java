package com.ultreon.craft.network;

import com.ultreon.craft.network.packets.Packet;

import java.util.function.Supplier;

public interface PacketResult {
    static PacketResult onFailure(Supplier<Packet<?>> supplier) {
        return new PacketResult() {
            @Override
            public Packet<?> onFailure() {
                return supplier.get();
            }
        };
    }

    static PacketResult onSuccess(Runnable func) {
        return new PacketResult() {
            @Override
            public void onSuccess() {
                func.run();
            }
        };
    }

    static PacketResult onEither(Runnable func) {
        return new PacketResult() {
            @Override
            public void onSuccess() {
                func.run();
            }

            @Override
            public Packet<?> onFailure() {
                func.run();
                return PacketResult.super.onFailure();
            }
        };
    }

    default void onSuccess() {

    }

    default Packet<?> onFailure() {
        return null;
    }
}
