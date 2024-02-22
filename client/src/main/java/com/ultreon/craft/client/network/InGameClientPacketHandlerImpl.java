package com.ultreon.craft.client.network;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.ClientPlayerEvents;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.client.gui.screens.DisconnectedScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.WorldLoadScreen;
import com.ultreon.craft.client.gui.screens.container.ContainerScreen;
import com.ultreon.craft.client.gui.screens.container.InventoryScreen;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.player.RemotePlayer;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.collection.Storage;
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
import com.ultreon.craft.network.packets.c2s.C2SCloseContainerMenuPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHurtPacket;
import com.ultreon.craft.network.packets.s2c.S2CTimePacket;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final Connection connection;
    private final Map<ElementID, NetworkChannel> channels = new HashMap<>();
    private final PacketContext context;
    private final UltracraftClient client = UltracraftClient.get();
    private long ping = 0;

    public InGameClientPacketHandlerImpl(Connection connection) {
        this.connection = connection;
        this.context = new PacketContext(null, connection, EnvType.CLIENT);
    }

    public NetworkChannel registerChannel(ElementID id) {
        NetworkChannel networkChannel = NetworkChannel.create(id);
        this.channels.put(id, networkChannel);
        return networkChannel;
    }

    @Override
    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, EnvType.CLIENT));
    }

    @Override
    public NetworkChannel getChannel(ElementID channelId) {
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
    public void onChunkData(ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage, Map<BlockPos, BlockEntityType<?>> blockEntities) {
        LocalPlayer player = this.client.player;
        if (player == null/* || new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > this.client.settings.renderDistance.get()*/) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
            return;
        }

        CompletableFuture.runAsync(() -> {
            ClientWorld world = this.client.world;

            if (world == null) {
                this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
                return;
            }

            world.loadChunk(pos, new ClientChunk(world, pos, storage, biomeStorage, blockEntities));
        }, this.client.chunkLoadingExecutor);
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

            IntegratedServer server = this.client.getSingleplayerServer();
            server.shutdown();

            this.client.showScreen(new DisconnectedScreen(message));
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
        RemotePlayer remotePlayer = data.getRemotePlayerByUuid(player);
        if (remotePlayer == null) return;

        remotePlayer.setPosition(pos);
    }

    @Override
    public void onKeepAlive() {
        // Do not need to do anything since it's a keep-alive packet.
    }

    @Override
    public void onPlaySound(ElementID sound, float volume) {
        this.client.playSound(Registries.SOUND_EVENT.getElement(sound), volume);
    }

    @Override
    public void onAddPlayer(UUID uuid, String name, Vec3d position) {
        this.client.getMultiplayerData().addPlayer(uuid, name, position);
    }

    @Override
    public void onRemovePlayer(UUID uuid) {
        this.client.getMultiplayerData().removePlayer(uuid);
    }

    @Override
    public void onBlockSet(BlockPos pos, Block block) {
        ClientWorld world = this.client.world;
        if (this.client.world != null) {
            this.client.submit(() -> world.set(pos, block));
        }
    }

    @Override
    public void onMenuItemChanged(int index, ItemStack stack) {
        var player = this.client.player;

        if (player != null) {
            ContainerMenu openMenu = player.openMenu;
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
    public void onOpenContainerMenu(ElementID menuTypeId) {
        var menuType = Registries.MENU_TYPE.getElement(menuTypeId);
        LocalPlayer player = this.client.player;
        if (player == null) return;
        ContainerMenu o = menuType.create(this.client.world, player, null);
        if (o != null) {
            player.onOpenMenu(o);
        } else {
            this.client.connection.send(new C2SCloseContainerMenuPacket());
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
        }
    }

    @Override
    public void onEntityPipeline(int id, MapType pipeline) {
        if (this.client.world != null) {
            this.client.world.getEntity(id).onPipeline(pipeline);
        }
    }
}
