package com.ultreon.craft.client.network;

import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.api.events.ClientChunkEvents;
import com.ultreon.craft.client.api.events.ClientPlayerEvents;
import com.ultreon.craft.client.gui.screens.*;
import com.ultreon.craft.client.gui.screens.container.ContainerScreen;
import com.ultreon.craft.client.gui.screens.container.InventoryScreen;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.player.RemotePlayer;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.network.packets.AddPermissionPacket;
import com.ultreon.craft.network.packets.InitialPermissionsPacket;
import com.ultreon.craft.network.packets.RemovePermissionPacket;
import com.ultreon.craft.network.packets.c2s.C2SChunkStatusPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHurtPacket;
import com.ultreon.craft.network.packets.s2c.S2CTimePacket;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ExitCodes;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.Gamemode;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final Connection connection;
    private final Map<Identifier, NetworkChannel> channels = new HashMap<>();
    private final PacketContext context;
    private final UltracraftClient client = UltracraftClient.get();
    private long ping = 0;
    private boolean disconnected;

    public InGameClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
        this.context = new PacketContext(null, connection, EnvType.CLIENT);
    }

    public NetworkChannel registerChannel(Identifier id) {
        NetworkChannel networkChannel = NetworkChannel.create(id);
        this.channels.put(id, networkChannel);
        return networkChannel;
    }

    @Override
    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, EnvType.CLIENT));
    }

    @Override
    public NetworkChannel getChannel(Identifier channelId) {
        return this.channels.get(channelId);
    }

    @Override
    public void onPlayerHealth(float newHealth) {
        if (this.client.player != null) {
            this.client.player.onHealthUpdate(newHealth);
        }
    }

    @Override
    public void onRespawn(Vec3d pos) {
        LocalPlayer player = this.client.player;
        if (this.client.player != null) {
            player.setPosition(pos);
            player.resurrect();
        }

        if (!(this.client.screen instanceof WorldLoadScreen)) {
            this.client.showScreen(null);
        }

        UltracraftClient.LOGGER.debug("Player respawned at %s".formatted(pos)); //! DEBUG
    }

    @Override
    public void onPlayerSetPos(Vec3d pos) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.setPosition(pos);
            player.setVelocity(new Vec3d());
        }
    }

    @Override
    public void onChunkData(ChunkPos pos, Storage<BlockMetadata> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        try {
            LocalPlayer player = this.client.player;
            if (player == null/* || new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > this.client.settings.renderDistance.getConfig()*/) {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
                return;
            }

            CompletableFuture.runAsync(() -> {
                ClientWorld world = this.client.world;

                if (world == null) {
                    this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                    return;
                }

                ClientChunk data = new ClientChunk(world, pos, storage, biomeStorage, blockEntities);
                ClientChunkEvents.RECEIVED.factory().onClientChunkReceived(data);
                world.loadChunk(pos, data);
            }, this.client.chunkLoadingExecutor).exceptionally(throwable -> {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                UltracraftClient.LOGGER.error("Failed to load chunk:", throwable);
                return null;
            });
        } catch (Exception e) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            UltracraftClient.LOGGER.error("Hard error while loading chunk:", e);
            UltracraftClient.LOGGER.debug("What, why? Pls no!!!");

            Runtime.getRuntime().halt(ExitCodes.FATAL_ERROR);
        }
    }

    @Override
    public void onChunkCancel(ChunkPos pos) {
        this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder(2 * byteArray.length);
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    @Override
    public void onDisconnect(String message) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            ClientPlayerEvents.PLAYER_LEFT.factory().onPlayerLeft(player, message);
        }

        this.client.connection.closeAll();
        this.disconnected = true;

        this.client.submit(() -> {
            this.client.renderWorld = false;
            @Nullable ClientWorld world = this.client.world;
            if (world != null) {
                world.dispose();
                this.client.world = null;
            }
            @Nullable WorldRenderer worldRenderer = this.client.worldRenderer;
            if (worldRenderer != null) {
                worldRenderer.dispose();
                this.client.worldRenderer = null;
            }

            var close = this.connection.close();
            if (close != null) {
                try {
                    close.sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    UltracraftClient.LOGGER.error("Failed to close connection", e);
                }
            }
            var future = this.connection.closeGroup();
            if (future != null) {
                try {
                    future.sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    UltracraftClient.LOGGER.error("Failed to close Netty event group", e);
                }
            }

            if (this.client.integratedServer != null) {
                this.client.integratedServer.shutdown();
                this.client.integratedServer = null;
            }

            this.client.showScreen(new DisconnectedScreen(message, !this.connection.isMemoryConnection()));
        });
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return this.context;
    }

    @Override
    public void onPlayerPosition(PacketContext ctx, UUID player, Vec3d pos) {
        // Update the remote player's position in the local multiplayer data.
        var data = this.client.getMultiplayerData();
        RemotePlayer remotePlayer = data != null ? data.getRemotePlayerByUuid(player) : null;
        if (remotePlayer == null) return;

        remotePlayer.setPosition(pos);
    }

    @Override
    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    @Override
    public void onPlaySound(Identifier sound, float volume) {
        this.client.playSound(Registries.SOUND_EVENT.get(sound), volume);
    }

    @Override
    public void onAddPlayer(UUID uuid, String name, Vec3d position) {
        if (this.client.getMultiplayerData() != null) {
            this.client.getMultiplayerData().addPlayer(uuid, name, position);
        } else {
            throw new IllegalStateException("Multiplayer data is null");
        }
    }

    @Override
    public void onRemovePlayer(UUID uuid) {
        if (this.client.getMultiplayerData() != null) {
            this.client.getMultiplayerData().removePlayer(uuid);
        } else {
            throw new IllegalStateException("Multiplayer data is null");
        }
    }

    @Override
    public void onBlockSet(BlockPos pos, BlockMetadata block) {
        ClientWorld world = this.client.world;
        if (this.client.world != null) {
            this.client.submit(() -> world.set(pos, block));
        }
    }

    @Override
    public void onMenuItemChanged(int index, ItemStack stack) {
        var player = this.client.player;

        if (player != null) {
            ContainerMenu openMenu = player.getOpenMenu();
            if (openMenu != null) {
                openMenu.setItem(index, stack);
            }

            if (this.client.screen instanceof ContainerScreen screen) {
                screen.emitUpdate();
            }
        }
    }

    @Override
    public void onInventoryItemChanged(int index, ItemStack stack) {
        var player = this.client.player;

        if (player != null) {
            Inventory inventory = player.inventory;
            inventory.setItem(index, stack);

            if (this.client.screen instanceof InventoryScreen screen) {
                screen.emitUpdate();
            }
        }
    }

    @Override
    public void onMenuCursorChanged(ItemStack cursor) {
        var player = this.client.player;
        if (this.client.player != null) {
            ContainerMenu openMenu = player.getOpenMenu();
            if (openMenu != null) {
                this.client.player.setCursor(cursor);
            }
        }
    }

    @Override
    public void onOpenContainerMenu(Identifier menuTypeId, List<ItemStack> items) {
        var menuType = Registries.MENU_TYPE.get(menuTypeId);
        LocalPlayer player = this.client.player;
        if (player == null) return;
        if (menuType != null) {
            client.execute(() -> player.onOpenMenu(menuType, items));
        }
    }

    @Override
    public void onAddPermission(AddPermissionPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onRemovePermission(RemovePermissionPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onInitialPermissions(InitialPermissionsPacket packet) {
        var player = this.client.player;
        if (player != null) {
            player.getPermissions().onPacket(packet);
        }
    }

    @Override
    public void onChatReceived(TextObject message) {
        ChatScreen.addMessage(message);
    }

    @Override
    public void onTabCompleteResult(String[] options) {
        Screen screen = this.client.screen;
        if (screen instanceof ChatScreen chatScreen) {
            UltracraftClient.invoke(() -> chatScreen.onTabComplete(options));
        }
    }

    @Override
    public void onAbilities(AbilitiesPacket packet) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.onAbilities(packet);
        }
    }

    @Override
    public void onPlayerHurt(S2CPlayerHurtPacket packet) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.onHurt(packet);
        }
    }

    public long getPing() {
        return this.ping;
    }

    @Override
    public void onPing(long serverTime, long time) {
        this.ping = System.currentTimeMillis() - time;
        this.connection.onPing(this.ping);
    }

    @Override
    public void onGamemode(Gamemode gamemode) {
        LocalPlayer player = this.client.player;
        if (player != null) {
            player.setGamemode(gamemode);
        }
    }

    @Override
    public void onBlockEntitySet(BlockPos pos, BlockEntityType<?> blockEntity) {
        UltracraftClient.invoke(() -> {
            ClientWorld world = client.world;
            if (world != null) {
                world.setBlockEntity(pos, blockEntity.create(world, pos));
            }
        });
    }

    @Override
    public void onTimeChange(PacketContext ctx, S2CTimePacket.Operation operation, int time) {
        if (this.client.world != null) {
            int daytime = this.client.world.getDaytime();
            switch (operation) {
                case SET -> this.client.world.setDaytime(time);
                case ADD -> this.client.world.setDaytime(daytime + time);
                case SUB -> this.client.world.setDaytime(daytime - time);
            }
        }
    }

    @Override
    public void onAddEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        if (this.client.world != null) {
            this.client.world.addEntity(id, type, position, pipeline);
            UltracraftClient.get().notifications.add("Added entity: " + id, "Element ID: " + type.getId());
        } else {
            UltracraftClient.get().notifications.add("Failed to add entity: " + id, "Element ID: " + type.getId());
        }
    }

    @Override
    public void onEntityPipeline(int id, MapType pipeline) {
        ClientWorld world = this.client.world;
        if (world != null) {
            this.client.execute(() -> {
                Entity entity = world.getEntity(id);
                entity.onPipeline(pipeline);
            });
        }
    }

    @Override
    public void onCloseContainerMenu() {
        var player = this.client.player;
        if (player != null) {
            this.client.execute(player::closeMenu);
        }
    }

    @Override
    public boolean isDisconnected() {
        return disconnected;
    }
}
