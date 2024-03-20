package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.events.MenuEvents;
import com.ultreon.craft.events.PlayerEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.item.UseItemContext;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketResult;
import com.ultreon.craft.network.packets.AbilitiesPacket;
import com.ultreon.craft.network.packets.s2c.*;
import com.ultreon.craft.registry.CommandRegistry;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.chat.Chat;
import com.ultreon.craft.text.Formatter;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.Gamemode;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Unit;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Server-side player implementation.
 * Represents an online player.
 * <p style="color: red;">NOTE: Should not be stored in collections, if you need to store a player in a collection. You should get the cached player instead,</p>
 *
 * @see UltracraftServer#getCachedPlayer(String)
 */
public non-sealed class ServerPlayer extends Player implements CacheablePlayer {
    public Connection connection;
    private final ServerWorld world;
    public int hotbarIdx;
    private final UUID uuid;
    private final String name;
    private final UltracraftServer server = UltracraftServer.get();
    private final Object2IntMap<ChunkPos> retryChunks = Object2IntMaps.synchronize(new Object2IntArrayMap<>());

    private final Cache<ChunkPos, S2CChunkCancelPacket> pendingChunks = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(notification -> {

    }).build();
    private final Cache<ChunkPos, Unit> failedChunks = CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).removalListener(notification -> {

    }).build();

    private final Set<ChunkPos> activeChunks = new CopyOnWriteArraySet<>();
    private final Set<ChunkPos> skippedChunks = new CopyOnWriteArraySet<>();
    public boolean blockBrokenTick = false;
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);
    private boolean sendingChunk;
    private boolean spawned;
    private boolean playedBefore;
    private final MutablePermissionMap permissions = new MutablePermissionMap();
    private boolean isAdmin;

    public ServerPlayer(EntityType<? extends Player> entityType, ServerWorld world, UUID uuid, String name, Connection connection) {
        super(entityType, world, name);
        this.world = world;
        this.uuid = uuid;
        this.name = name;

        this.connection = connection;

        this.permissions.allows.add(new Permission("*")); // FIXME: Allow custom default permissions.
    }

    /**
     * Kicks the player with the given message.
     *
     * @param message the kick message.
     */
    public void kick(String message) {
        this.connection.disconnect(message);
    }

    /**
     * Kicks the player with the given message as {@link TextObject}.
     *
     * @param message the kick message.
     */
    public void kick(TextObject message) {
        this.connection.disconnect(message);
    }

    /**
     * Respawn the player in the world.
     */
    public void respawn() {
        // Check if the world is not null
        assert this.world != null;

        // Remove player from the world if it exists in the world
        if (this.world.getEntity(this.getId()) == this) {
            this.world.despawn(this);
        }

        // Despawn player from the world
        this.world.despawn(this);

        try {
            // Get the spawn point for the player
            var spawnPoint = this.server.submit(this.world::getSpawnPoint).join();

            // Calculate the spawn position
            Vec3d spawnAt = spawnPoint.vec().d().add(0.5, 0, 0.5);

            // Set player's position, health, and status
            this.setPosition(spawnAt);
            this.health = this.getMaxHealth();
            this.isDead = false;
            this.damageImmunity = 40;

            // Spawn player at the calculated spawn point
            this.spawn(spawnAt, this.connection);
        } catch (Exception e) {
            // Log error if failed to spawn player
            UltracraftServer.LOGGER.error("Failed to spawn player!", e);
        }
    }

    /**
     * Called when the player takes damage.
     *
     * @param damage the damage dealt.
     * @param source the source of the damage.
     * @return true to cancel the damage.
     */
    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        // Check if the damage immunity is active
        if (this.damageImmunity > 0) return true;

        // Call the superclass to handle the damage as player/living entity. To get if the damage was cancelled.
        boolean noDamage = super.onHurt(damage, source);

        if (!noDamage) {
            // Play hurt sound
            this.playSound(this.getHurtSound(), 1.0f);
            this.connection.send(new S2CPlayerHurtPacket(damage, source));

            // DEBUG: Send debug message
            Chat.sendInfo(this, "Oww, that hurts! You lost approx. " + ((int) damage) + " HP.");
        }
        return noDamage;
    }

    /**
     * Spawns the entity at the specified position with the given connection.
     *
     * @param position   The position to spawn the entity
     * @param connection The connection used for spawning
     */
    @ApiStatus.Internal
    public void spawn(Vec3d position, Connection connection) {
        Preconditions.checkNotNull(position, "position");
        Preconditions.checkNotNull(connection, "connection");

        // Set the entity's connection and health, and position it at the specified position
        this.connection = connection;
        this.setHealth(this.getMaxHealth());
        this.setPosition(position);

        // Prepare and spawn the entity in the world
        this.world.prepareSpawn(this);
        this.world.spawn(this);

        // Send gamemode and respawn packets to the connection
        this.connection.send(new S2CGamemodePacket(this.getGamemode()));
        this.connection.send(new S2CRespawnPacket(this.getPosition()));

        // Mark the entity as spawned
        this.spawned = true;
    }

    public void markSpawned() {
        this.spawned = true;
    }

    /**
     * This method is called every tick to update the player's state.
     */
    @Override
    public void tick() {
        // Reset the blockBrokenTick flag
        this.blockBrokenTick = false;

        // Call the superclass tick method
        super.tick();

        // Check if the player's health has changed
        if (this.oldHealth != this.health) {
            // Send the updated health to the client
            this.connection.send(new S2CPlayerHealthPacket(this.health));
            // Update the old health value
            this.oldHealth = this.health;
        }

        // Get the currently open menu
        ContainerMenu menu = this.getOpenMenu();
        if (menu != null) {
            // Get the position of the menu
            BlockPos pos = menu.getPos();
            // Check if the distance between the player and the menu position is greater than 5
            if (pos != null && pos.vec().d().dst(this.getPosition()) > 5)
                // Auto-close the menu if the distance is greater than 5
                this.autoCloseMenu();
        }
    }

    private void autoCloseMenu() {
        this.connection.send(new S2CCloseMenuPacket());
    }

    @Override
    protected void move() {

    }

    /**
     * {@inheritDoc}
     * <p>
     * Also updates player old positions and send packets to nearby players.
     */
    @Override
    protected void onMoved() {
        // Check if the chunk is loaded and the entity is in an active chunk
        if (this.world.getChunk(this.getChunkPos()) == null) return;
        if (!this.isChunkActive(this.getChunkPos())) return;

        super.onMoved();

        // Set old position.
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        // Send position update packets to nearby players
        for (var player : this.server.getPlayers()) {
            if (player == this) continue;

            // Check if player is within entity render distance
            if (player.getPosition().dst(this.getPosition()) < this.server.getEntityRenderDistance())
                player.connection.send(new S2CPlayerPositionPacket(this.getUuid(), this.getPosition()));
        }
    }

    @Override
    public void teleportTo(int x, int y, int z) {
        super.teleportTo(x, y, z);

        this.connection.send(new S2CPlayerSetPosPacket(x + 0.5, y, z + 0.5));
    }

    @Override
    public void teleportTo(double x, double y, double z) {
        super.teleportTo(x, y, z);

        this.connection.send(new S2CPlayerSetPosPacket(x, y, z));
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isCache() {
        return false;
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.world, this.x, this.y, this.z, this.xRot, this.yRot);
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "ServerPlayer{'" + this.name + "' : " + this.getUuid() + "}";
    }

    /**
     * Called when a chunk is loaded, unloaded, or failed to load.
     *
     * @param pos    the position of the chunk
     * @param status the status of the chunk
     */
    public void onChunkStatus(@NotNull ChunkPos pos, Chunk.Status status) {
        // Handle the chunk status accordingly
        switch (status) {
            case FAILED -> this.handleFailedChunk(pos);
            case SKIP -> this.skippedChunks.add(pos);
            case SUCCESS -> this.handleClientLoadChunk(pos);
            case UNLOADED -> {
                this.activeChunks.remove(pos);
                this.skippedChunks.remove(pos);
                this.pendingChunks.invalidate(pos);
                this.failedChunks.invalidate(pos);
            }
        }

        // Handle chunk load failure if the status is failed
        if (status == Chunk.Status.FAILED)
            this.server.handleChunkLoadFailure(pos, "Chunk failed to load on client.");
    }

    private void handleClientLoadChunk(@NotNull ChunkPos pos) {
        this.setPosition(this.ox, this.oy, this.oz);
        if (DebugFlags.LOG_POSITION_RESET_ON_CHUNK_LOAD.enabled()) {
            Chat.sendInfo(this, "Position reset on chunk load.");
        }

        this.activeChunks.add(pos);
    }

    private void handleFailedChunk(@NotNull ChunkPos pos) {
        if (this.retryChunks.computeInt(pos, (chunkPos, integer) -> integer == null ? 1 : integer + 1) == 3) {
            this.pendingChunks.invalidate(pos);
            this.failedChunks.put(pos, Unit.INSTANCE);
        }
    }

    public void onChunkPending(ChunkPos pos) {
        this.pendingChunks.put(pos, new S2CChunkCancelPacket(pos));
    }

    /**
     * Refreshes the chunks around the player's position.
     *
     * @param refresher the ChunkRefresher object
     */
    public void refreshChunks(ChunkRefresher refresher) {
        // Get the player's position and chunk position
        var pos = this.getPosition();
        var chunkPos = this.getChunkPos();

        // Get the server and world objects
        var server = this.server;
        var world = this.world;

        // Get the needed chunks, chunks to load, and chunks to unload
        var needed = this.world.getChunksAround(pos);
        var toLoad = this.getChunksToLoad(needed);
        var toUnload = this.getChunksToUnload(needed);

        // Invalidate all pending chunks that are to be unloaded
        this.pendingChunks.invalidateAll(toUnload);

        // Remove skipped chunks if the player didn't move in between chunks
        if (this.oldChunkPos.equals(chunkPos)) {
            toLoad.removeAll(this.skippedChunks);
        } else {
            this.oldChunkPos = chunkPos;
        }

        // Remove all failed chunks
        toLoad.removeAll(this.pendingChunks.asMap().keySet());
        toLoad.removeAll(this.failedChunks.asMap().keySet());
        toLoad.removeAll(toUnload);

        // Call the static method to refresh chunks
        ServerPlayer.refreshChunks(refresher, server, world, chunkPos, toLoad, toUnload);
    }

    /**
     * Refreshes chunks around a specified chunk position.
     * <p>
     * NOTE: Internal API.
     *
     * @param refresher The ChunkRefresher object.
     * @param server The UltracraftServer object.
     * @param world The ServerWorld object.
     * @param chunkPos The central ChunkPos to compare against.
     * @param toLoad Set of ChunkPos to load, sorted based on distance from player position.
     * @param toUnload Set of ChunkPos to unload.
     */
    @ApiStatus.Internal
    public static void refreshChunks(ChunkRefresher refresher, UltracraftServer server, ServerWorld world, ChunkPos chunkPos, ListOrderedSet<ChunkPos> toLoad, ListOrderedSet<ChunkPos> toUnload) {
        // Sort the chunks to load based on distance from player position.
        List<ChunkPos> load = toLoad.stream().sorted((o1, o2) -> {
            Vec2d playerPos = new Vec2d(chunkPos.x(), chunkPos.z());
            Vec2d cPos1 = new Vec2d(o1.x(), o1.z());
            Vec2d cPos2 = new Vec2d(o2.x(), o2.z());

            return Double.compare(cPos1.dst(playerPos), cPos2.dst(playerPos));
        }).toList();

        // Load each chunk in the sorted order.
        for (ChunkPos loadingChunk : load) {
            ServerChunk chunk = world.getChunk(loadingChunk);
            if (chunk != null) {
                try {
                    server.sendChunk(loadingChunk, chunk);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Add all loading/unloading chunks to the refresher.
        refresher.addLoading(load);
        refresher.addUnloading(toUnload);
    }

    private ListOrderedSet<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
        return this.activeChunks.stream()
                .filter(pos -> !needed.contains(pos))
                .collect(Collectors.toCollection(ListOrderedSet::new));
    }

    private ListOrderedSet<ChunkPos> getChunksToLoad(List<ChunkPos> needed) {
        return needed.stream()
                .filter(chunkPos -> !this.activeChunks.contains(chunkPos))
                .collect(Collectors.toCollection(ListOrderedSet::new));
    }

    /**
     * Send a chunk to the client.
     *
     * @param pos   The position of the chunk.
     * @param chunk The chunk to send.
     */
    public void sendChunk(ChunkPos pos, Chunk chunk) {
        if (this.sendingChunk) return;

        this.onChunkPending(pos);
        this.connection.send(new S2CChunkDataPacket(pos, chunk.storage, chunk.biomeStorage, chunk.getBlockEntities()), PacketResult.onEither(() -> this.sendingChunk = false));
    }

    /**
     * {@inheritDoc}
     * To play the sound, this will send a {@link S2CPlaySoundPacket} over to the client.
     *
     * @param sound  The sound event to be played. Can be null.
     * @param volume The volume at which the sound should be played.
     */
    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        if (sound == null) return;
        this.connection.send(new S2CPlaySoundPacket(sound.getId(), volume));
    }

    /**
     * Send the player's abilities to the client.
     */
    @Override
    protected void sendAbilities() {
        this.connection.send(new S2CAbilitiesPacket(this.abilities));
    }

    /**
     * Handles the AbilitiesPacket received from the client.
     * If the player is trying to fly when flight is not allowed, disconnects them.
     *
     * @param packet The AbilitiesPacket received from the client.
     */
    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        // Check if the player is trying to fly
        boolean flying = packet.isFlying();
        // Check if flight is allowed
        boolean allowFlight = this.abilities.allowFlight;

        // If the player is trying to fly and flight is not allowed, disconnect them
        if (flying && !allowFlight) {
            this.connection.disconnect("Kicked for flying.");
            return;
        }

        // Call the superclass method to handle the AbilitiesPacket
        super.onAbilities(packet);

        // Update the flying status of the player
        this.abilities.flying = flying;
    }

    /**
     * Override method to open a menu.
     * If the menu open event is not canceled, opens the menu by sending a packet.
     *
     * @param menu The menu to be opened.
     */
    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        if (getOpenMenu() != null) {
            UltracraftServer.LOGGER.warn("Player {} tried to open menu {} but it was already open!", this.name, menu.getType().getId());
            this.closeMenu();
            return;
        }

        // Check if the menu open event is canceled, if so, return early
        if (MenuEvents.MENU_OPEN.factory().onMenuOpen(menu, this).isCanceled())
            return;

        // Call the superclass method to open the menu
        super.openMenu(menu);

        // Send a packet to open the container menu
        this.connection.send(new S2COpenMenuPacket(menu.getType().getId(), Arrays.asList(menu.slots)));
    }

    @Override
    public void setCursor(@NotNull ItemStack cursor) {
        this.connection.send(new S2CMenuCursorPacket(cursor));
        super.setCursor(cursor);
    }

    /**
     * Sets the gamemode and sends relevant packets if the gamemode has changed.
     *
     * @param gamemode the new gamemode to set
     */
    @Override
    public void setGamemode(@NotNull Gamemode gamemode) {
        Gamemode old = this.getGamemode();
        super.setGamemode(gamemode);

        // If the gamemode has changed, send relevant packets
        if (old != gamemode) {
            this.connection.send(new S2CGamemodePacket(gamemode));

            // Set the abilities of the player and send them to the client
            gamemode.setAbilities(this.abilities);
            this.connection.send(new S2CAbilitiesPacket(this.abilities));
        }
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return this.world;
    }

    public Vec2i getChunkVec() {
        return World.toChunkVec(this.getBlockPos());
    }

    public boolean isChunkActive(ChunkPos chunkPos) {
        return this.activeChunks.contains(chunkPos);
    }

    /**
     * Sets the initial items of the player.
     */
    public void setInitialItems() {
        // Check if the event for initial items is canceled
        if (PlayerEvents.INITIAL_ITEMS.factory().onPlayerInitialItems(this, this.inventory).isCanceled()) {
            return;
        }
        // Add initial items to the player's inventory
        this.inventory.addItem(Items.WOODEN_PICKAXE.defaultStack());
        this.inventory.addItem(Items.WOODEN_SHOVEL.defaultStack());
        this.inventory.addItem(new ItemStack(Items.CRATE, 32));
        this.inventory.addItem(new ItemStack(Items.BLAST_FURNACE, 32));
    }

    /**
     * Handles player movement from the client.
     *
     * @param x the x-coordinate received from the client
     * @param y the y-coordinate received from the client
     * @param z the z-coordinate received from the client
     */
    public void handlePlayerMove(double x, double y, double z) {
        ChunkPos chunkPos = World.toChunkPos((int) x, (int) y, (int) z);
        if (this.world.getChunk(chunkPos) == null) {
            UltracraftServer.LOGGER.warn("Player moved into a null chunk: %s".formatted(this.getName()));
            return;
        }
        if (!this.isChunkActive(chunkPos)) {
            UltracraftServer.LOGGER.warn("Player moved into an inactive chunk: %s".formatted(this.getName()));
            return;
        }

        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public void markPlayedBefore() {
        this.playedBefore = true;
    }

    public boolean hasPlayedBefore() {
        return this.playedBefore;
    }

    public void tabComplete(String input) {
        if (input.startsWith("/")) {
            input = input.substring(1);
            if (!input.contains(" ")) {
                this.connection.send(new S2CTabCompletePacket(TabCompleting.commands(new ArrayList<>(), input)));
                return;
            }

            String command;
            String[] argv;
            argv = input.split(" ");
            command = argv[0];
            argv = ArrayUtils.remove(argv, 0);

            Command baseCommand = CommandRegistry.get(command);
            if (baseCommand == null) {
                return;
            }

            if (input.endsWith(" ")) {
                argv = ArrayUtils.add(argv, "");
            }

            List<String> options = baseCommand.onTabComplete(this, new CommandContext(command), command, argv);
            if (options == null) options = Collections.emptyList();
            this.connection.send(new S2CTabCompletePacket(options));
        }
    }

    public void onMessageSent(String message) {
        for (ServerPlayer player : this.server.getPlayers()) {
            player.sendMessage(new Formatter(true, true, message, TextObject.empty(), TextObject.empty(), null, Color.WHITE).parse().getResult());
        }
    }

    @Override
    public void sendMessage(@NotNull TextObject textObj) {
        this.connection.send(new S2CChatPacket(textObj));
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.sendMessage(new Formatter(true, true, message, TextObject.empty(), TextObject.empty(), null, Color.WHITE).parse().getResult());
    }

    @Override
    public boolean hasExplicitPermission(@NotNull Permission permission) {
        return this.permissions.has(permission);
    }

    @Override
    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void makeAdmin() {
        this.isAdmin = true;
        this.resendCommands();
    }

    private void resendCommands() {
        this.connection.send(new S2CCommandSyncPacket(CommandRegistry.getCommandNames().toList()));
    }

    public UseResult useItem(HitResult hitResult, ItemStack stack, ItemSlot slot) {
        UseItemContext ctx = new UseItemContext(getWorld(), this, hitResult, stack);
        HitResult result = ctx.result();
        if (result == null)
            return UseResult.SKIP;

        Block block = result.block;
        if (block != null && !block.isAir()) {
            UseResult blockResult = block.use(ctx.world(), ctx.player(), stack.getItem(), new BlockPos(result.getPos()));

            if (blockResult == UseResult.DENY || blockResult == UseResult.ALLOW)
                return blockResult;
        }

        UseResult itemResult = stack.getItem().use(ctx);
        if (itemResult == UseResult.DENY)
            slot.update();

        return itemResult;
    }

    /**
     * Called when a block is placed by the player on the client side.
     * This is called from the packet when handled.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param block the block to place
     */
    public void placeBlock(int x, int y, int z, BlockMetadata block) {
        BlockPos blockPos = new BlockPos(x, y, z);
        if (block == null || !this.world.isLoaded(blockPos) || !this.world.get(blockPos).isReplaceable()) return;

        this.world.set(x, y, z, block, BlockFlags.SYNC | BlockFlags.UPDATE);
    }
}
