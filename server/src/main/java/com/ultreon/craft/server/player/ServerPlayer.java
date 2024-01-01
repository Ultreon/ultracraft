package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.api.commands.Command;
import com.ultreon.craft.api.commands.CommandContext;
import com.ultreon.craft.api.commands.TabCompleting;
import com.ultreon.craft.api.commands.perms.Permission;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.events.PlayerEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.menu.ContainerMenu;
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

    public void kick(String kick) {
        this.connection.disconnect(kick);
    }

    public void respawn() {
        assert this.world != null;
        if (this.world.getEntity(this.getId()) == this) {
            this.world.despawn(this);
        }

        this.world.despawn(this);
        try {
            var spawnPoint = this.server.submit(this.world::getSpawnPoint).join();

            Vec3d spawnAt = spawnPoint.vec().d().add(0.5, 0, 0.5);
            this.setPosition(spawnAt);
            this.health = this.getMaxHeath();
            this.isDead = false;
            this.damageImmunity = 40;
            this.spawn(spawnAt, this.connection);
        } catch (Exception e) {
            UltracraftServer.LOGGER.error("Failed to spawn player!", e);
        }
    }

    /**
     * @param damage the damage dealt.
     * @param source the source of the damage.
     * @return true to cancel the damage.
     */
    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (this.damageImmunity > 0) return true;
        boolean noDamage = super.onHurt(damage, source);
        if (!noDamage) {
            this.playSound(this.getHurtSound(), 1.0f);
            this.connection.send(new S2CPlayerHurtPacket(damage, source));
            Chat.sendInfo(this, "Oww, that hurts! You lost approx. " + ((int) damage) + " HP.");
        }
        return noDamage;
    }

    @ApiStatus.Internal
    public void spawn(Vec3d position, Connection connection) {
        Preconditions.checkNotNull(position, "position");
        Preconditions.checkNotNull(connection, "connection");

        this.connection = connection;
        this.setHealth(this.getMaxHeath());
        this.setPosition(position);
        this.world.prepareSpawn(this);
        this.world.spawn(this);
        this.connection.send(new S2CGamemodePacket(this.getGamemode()));
        this.connection.send(new S2CRespawnPacket(this.getPosition()));

        this.spawned = true;
    }

    public void markSpawned() {
        this.spawned = true;
    }

    @Override
    public void tick() {
        if (this.world.getChunk(this.getChunkPos()) == null) return;
        if (!this.isChunkActive(this.getChunkPos())) return;

        super.tick();

        if (this.oldHealth != this.health) {
            this.connection.send(new S2CPlayerHealthPacket(this.health));
            this.oldHealth = this.health;
        }

        ContainerMenu menu = this.getOpenMenu();
        if (menu != null) {
            BlockPos pos = menu.getPos();
            if (pos != null && pos.vec().d().dst(this.getPosition()) > 5)
                this.autoCloseMenu();
        }
    }

    private void autoCloseMenu() {
        this.connection.send(new S2CCloseContainerMenuPacket());
    }

    @Override
    protected void move() {

    }

    @Override
    protected void onMoved() {
        if (this.world.getChunk(this.getChunkPos()) == null) return;
        if (!this.isChunkActive(this.getChunkPos())) return;

        super.onMoved();

        // Send the new position to the client.
        if (this.world.getChunk(this.getChunkPos()) == null) {
            this.setPosition(this.ox, this.oy, this.oz);
            this.connection.send(new S2CPlayerSetPosPacket(this.getPosition()));
        }

        if (Math.abs(this.x - this.ox) < 0.001 &&
            Math.abs(this.y - this.oy) < 0.001 &&
            Math.abs(this.z - this.oz) < 0.001)
            return;

        // Set old position.
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        for (var player : this.server.getPlayers()) {
            if (player == this) continue;

            if (player.getPosition().dst(this.getPosition()) < this.server.getEntityRenderDistance())
                player.connection.send(new S2CPlayerPositionPacket(this.getUuid(), this.getPosition()));
        }
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

    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "ServerPlayer{'" + this.name + "'}";
    }

    public void onChunkStatus(@NotNull ChunkPos pos, Chunk.Status status) {
        switch (status) {
            case FAILED -> this.handleFailedChunk(pos);
            case SKIP -> this.skippedChunks.add(pos);
            case SUCCESS -> this.handleClientLoadChunk(pos);
            case UNLOADED -> this.activeChunks.remove(pos);
        }

        if (status == Chunk.Status.FAILED)
            this.server.handleChunkLoadFailure(pos, "Chunk failed to load on client.");
    }

    private boolean handleClientLoadChunk(@NotNull ChunkPos pos) {
        this.setPosition(this.ox, this.oy, this.oz);
        return this.activeChunks.add(pos);
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

    public void refreshChunks(ChunkRefresher refresher) {
        var pos = this.getPosition();
        var chunkPos = this.getChunkPos();
        var server = this.server;
        var world = this.world;
        var needed = this.world.getChunksAround(pos);
        var toLoad = this.getChunksToLoad(needed);
        var toUnload = this.getChunksToUnload(needed);

        // Invalidate all pending chunks that are to be unloaded.
        this.pendingChunks.invalidateAll(toUnload);

        // Remove skipped chunks if the player didn't move in between chunks.
        if (this.oldChunkPos.equals(chunkPos)) toLoad.removeAll(this.skippedChunks);
        else this.oldChunkPos = chunkPos;

        // Remove all failed chunks.
        toLoad.removeAll(this.pendingChunks.asMap().keySet());
        toLoad.removeAll(this.failedChunks.asMap().keySet());
        toLoad.removeAll(toUnload);

        ServerPlayer.refreshChunks(refresher, server, world, chunkPos, toLoad, toUnload);
    }

    @ApiStatus.Internal
    public static void refreshChunks(ChunkRefresher refresher, UltracraftServer server, ServerWorld world, ChunkPos chunkPos, ListOrderedSet<ChunkPos> toLoad, ListOrderedSet<ChunkPos> toUnload) {
        List<ChunkPos> load = toLoad.stream().sorted((o1, o2) -> {
            // Compare against player position.
            Vec2d playerPos = new Vec2d(chunkPos.x(), chunkPos.z());
            Vec2d cPos1 = new Vec2d(o1.x(), o1.z());
            Vec2d cPos2 = new Vec2d(o2.x(), o2.z());

            return Double.compare(cPos1.dst(playerPos), cPos2.dst(playerPos));
        }).toList();

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

        // Add all loading/unloading chunks.
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

    public void teleportTo(int x, int y, int z) {
        this.setPosition(x + 0.5, y, z + 0.5);
        this.connection.send(new S2CPlayerSetPosPacket(x + 0.5, y, z + 0.5));
    }

    public void teleportTo(double x, double y, double z) {
        this.setPosition(x, y, z);
        this.connection.send(new S2CPlayerSetPosPacket(x, y, z));
    }

    public void sendChunk(ChunkPos pos, Chunk chunk) {
        if (this.sendingChunk) return;

        this.onChunkPending(pos);
        this.connection.send(new S2CChunkDataPacket(pos, chunk.storage), PacketResult.onEither(() -> this.sendingChunk = false));
    }

    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        if (sound == null) return;
        this.connection.send(new S2CPlaySoundPacket(sound.getId(), volume));
    }

    @Override
    protected void sendAbilities() {
        this.connection.send(new S2CAbilitiesPacket(this.abilities));
    }

    @Override
    public void onAbilities(@NotNull AbilitiesPacket packet) {
        boolean flying = packet.isFlying();
        boolean allowFlight = this.abilities.allowFlight;
        System.out.println("allowFlight = " + allowFlight);
        if (flying && !allowFlight) {
            this.connection.disconnect("Kicked for flying.");
            return;
        }

        super.onAbilities(packet);
        this.abilities.flying = flying;
    }

    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        super.openMenu(menu);

        this.connection.send(new S2COpenContainerMenuPacket(this.inventory.getType().getId()));
    }

    @Override
    public void setCursor(@NotNull ItemStack cursor) {
        this.connection.send(new S2CMenuCursorPacket(cursor));
        super.setCursor(cursor);
    }

    @Override
    public void setGamemode(@NotNull Gamemode gamemode) {
        Gamemode old = this.getGamemode();
        super.setGamemode(gamemode);

        if (old != gamemode) {
            this.connection.send(new S2CGamemodePacket(gamemode));

            switch (gamemode) {
                case BUILDER, BUILDER_PLUS -> {
                    this.abilities.allowFlight = true;
                    this.abilities.instaMine = true;
                    this.abilities.invincible = true;
                    this.abilities.blockBreak = true;
                }
                case MINI_GAME -> {
                    this.abilities.allowFlight = false;
                    this.abilities.instaMine = false;
                    this.abilities.invincible = false;
                    this.abilities.blockBreak = false;
                }
                case SURVIVAL -> {
                    this.abilities.allowFlight = false;
                    this.abilities.instaMine = false;
                    this.abilities.invincible = false;
                    this.abilities.blockBreak = true;
                }
            }

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

    public void setInitialItems() {
        if (PlayerEvents.INITIAL_ITEMS.factory().onPlayerInitialItems(this, this.inventory).isCanceled()) {
            return;
        }

        this.inventory.addItem(Items.WOODEN_PICKAXE.defaultStack());
        this.inventory.addItem(Items.WOODEN_SHOVEL.defaultStack());
    }

    public void handlePlayerMove(double x, double y, double z) {
        if (this.world.getChunk(this.getChunkPos()) == null) return;
        if (!this.isChunkActive(this.getChunkPos())) return;

//        double dst = this.getPosition().dst(x, this.y, z);
//        if (dst > this.getSpeed() * this.runModifier * TPS) {
//            this.setPosition(this.x, this.y, this.z);
//            this.connection.send(new S2CPlayerSetPosPacket(this.getPosition()));
//            UltracraftServer.LOGGER.warn("Player moved too quickly: %s (distance: %s, max xz: %s)".formatted(this.getName(), dst, this.getSpeed() * this.runModifier * 1.5));
//            return;
//        }
        this.setPosition(x, y, z);
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

    public void execute(String input) {
        String command;
        String[] argv;
        if (!input.contains(" ")) {
            argv = new String[0];
            command = input;
        } else {
            argv = input.split(" ");
            command = argv[0];
            argv = ArrayUtils.remove(argv, 0);
        }

        UltracraftServer.LOGGER.info(this.getName() + " ran command: " + input);

        Command baseCommand = CommandRegistry.get(command);
        if (baseCommand == null) {
            Chat.sendError(this, "Unknown command&: " + command);
            return;
        }
        baseCommand.onCommand(this, new CommandContext(command), command, argv);
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
            System.out.println("options = " + options);
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
        String text = textObj.getText();
        Debugger.log("MESSAGE_SENT: " + text);
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
}
