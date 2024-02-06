package com.ultreon.craft.client.world;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.network.packets.c2s.C2SBlockBreakPacket;
import com.ultreon.craft.network.packets.c2s.C2SBlockBreakingPacket;
import com.ultreon.craft.network.packets.c2s.C2SChunkStatusPacket;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import static com.badlogic.gdx.math.MathUtils.lerp;

public final class ClientWorld extends World implements Disposable {
    private static final int DAY_CYCLE = 24000;
    private static final Color DAY_COLOR = new Color(0.6F, 0.7F, 1.0F, 1.0F);
    private static final Color NIGHT_COLOR = new Color(0.05F, 0.075F, 0.15F, 1.0F);
    @NotNull
    private final UltracraftClient client;
    private final Map<ChunkPos, ClientChunk> chunks = new HashMap<>();
    private int chunkRefresh;
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);
    private int time = 0;

    public ClientWorld(@NotNull UltracraftClient client) {
        super();
        this.client = client;
    }

    @Override
    public int getRenderDistance() {
        return this.client.config.get().renderDistance;
    }

    @Override
    protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
        if (!UltracraftClient.isOnMainThread()) {
            return UltracraftClient.invokeAndWait(() -> this.unloadChunk(chunk, pos));
        }
        return this.chunks.remove(pos) == chunk;
    }

    @Override
    protected void checkThread() {
        if (!UltracraftClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
    }

    @Override
    public @Nullable ClientChunk getChunk(@NotNull ChunkPos pos) {
        synchronized (this) {
            return this.chunks.get(pos);
        }
    }

    @Override
    public ClientChunk getChunk(int x, int z) {
        return (ClientChunk) super.getChunk(x, z);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(@NotNull BlockPos pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        this.checkThread();

        return this.chunks.values();
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        this.checkThread();
        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void updateChunk(@Nullable Chunk chunk) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invokeAndWait(() -> this.updateChunk(chunk));
            return;
        }
        super.updateChunk(chunk);
    }

    @Override
    public void startBreaking(@NotNull BlockPos breaking, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.START));
        }
        super.startBreaking(breaking, breaker);
    }

    @Override
    public BreakResult continueBreaking(@NotNull BlockPos breaking, float amount, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.CONTINUE));
        }
        BreakResult breakResult = super.continueBreaking(breaking, amount, breaker);
        if (breakResult == BreakResult.BROKEN) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
            this.client.connection.send(new C2SBlockBreakPacket(breaking));
            this.set(breaking, Blocks.AIR);
        }
        return breakResult;
    }

    @Override
    public void stopBreaking(@NotNull BlockPos breaking, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
        }
        super.stopBreaking(breaking, breaker);
    }

    @Override
    public void onChunkUpdated(@NotNull Chunk chunk) {
        this.checkThread();

        super.onChunkUpdated(chunk);
    }

    @Override
    public void playSound(@NotNull SoundEvent sound, double x, double y, double z) {
        float range = sound.getRange();
        Player player = this.client.player;
        if (player != null) {
            player.playSound(sound, (float) ((range - player.getPosition().dst(x, y, z)) / range));
        }
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    public void loadChunk(ChunkPos pos, ClientChunk data) {
        var chunk = UltracraftClient.invokeAndWait(() -> this.chunks.get(pos));
        if (chunk == null) chunk = data;
        else {
            World.LOGGER.warn("Duplicate chunk packet detected! Chunk {}", pos);
            return;
        }
        LocalPlayer player = this.client.player;
        if (player == null) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            return;
        }
        if (new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > this.client.config.get().renderDistance) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
            return;
        }

        ClientChunk finalChunk = chunk;
        UltracraftClient.invoke(() -> {
            finalChunk.ready();
            synchronized (this) {
                this.chunks.put(pos, finalChunk);
            }
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
        });
    }

    public void tick() {
        this.time++;

        if (this.chunkRefresh-- <= 0) {
            this.chunkRefresh = 40;

            LocalPlayer player = this.client.player;
            if (player != null) {
                if (this.oldChunkPos.equals(player.getChunkPos())) return;
                this.oldChunkPos = player.getChunkPos();
                for (Iterator<Map.Entry<ChunkPos, ClientChunk>> iterator = this.chunks.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<ChunkPos, ClientChunk> entry = iterator.next();
                    ChunkPos chunkPos = entry.getKey();
                    ClientChunk clientChunk = entry.getValue();
                    if (new Vec2d(chunkPos.x(), chunkPos.z()).dst(player.getChunkPos().x(), player.getChunkPos().z()) > this.client.config.get().renderDistance) {
                        iterator.remove();
                        clientChunk.dispose();
                        this.updateNeighbours(clientChunk);

                        this.client.connection.send(new C2SChunkStatusPacket(chunkPos, Chunk.Status.UNLOADED));
                    }
                }
            }
        }
    }

    public Stream<Entity> getAllEntities() {
        return this.entities.values().stream();
    }

    public float getGlobalSunlight() {
        int daytime = this.getDaytime();
        final int riseSetDuration = ClientWorld.DAY_CYCLE / 24;
        if (daytime < riseSetDuration / 2) {
            return lerp(
                    0.25f, 1.0f,
                    0.5f + daytime / (float) riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - riseSetDuration / 2) {
            return 1.0f;
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + riseSetDuration / 2) {
            return lerp(
                    1.0f, 0.25f,
                    (daytime - ((float) ClientWorld.DAY_CYCLE / 2 - (float) riseSetDuration / 2)) / riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - riseSetDuration / 2) {
            return 0.25f;
        } else {
            return lerp(
                    0.25f, 1.0f,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) riseSetDuration / 2)) / riseSetDuration);
        }
    }

    public Color getSkyColor() {
        int daytime = this.getDaytime();
        final int riseSetDuration = ClientWorld.DAY_CYCLE / 24;
        if (daytime < riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    ClientWorld.DAY_COLOR, ClientWorld.NIGHT_COLOR,
                    0.5f + daytime / (float) riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - riseSetDuration / 2) {
            return ClientWorld.DAY_COLOR;
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + riseSetDuration / 2) {
            return ClientWorld.mixColors(
                    ClientWorld.NIGHT_COLOR, ClientWorld.DAY_COLOR,
                    (daytime - ((double) ClientWorld.DAY_CYCLE / 2 - (float) riseSetDuration / 2)) / riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - riseSetDuration / 2) {
            return ClientWorld.NIGHT_COLOR;
        } else {
            return ClientWorld.mixColors(
                    ClientWorld.DAY_COLOR, ClientWorld.NIGHT_COLOR,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) riseSetDuration / 2)) / riseSetDuration);
        }
    }

    public int getDaytime() {
        return this.time % DAY_CYCLE;
    }

    private static Color mixColors(Color color1, Color color2, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        int redPart = (int) (color1.getRed() * percent + color2.getRed() * inversePercent);
        int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inversePercent);
        int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inversePercent);
        int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inversePercent);
        return Color.rgba(redPart, greenPart, bluePart, alphaPart);
    }

    @Override
    public void dispose() {
        this.checkThread();

        super.dispose();

        synchronized (this) {
            this.chunks.forEach((chunkPos, clientChunk) -> clientChunk.dispose());
            this.chunks.clear();
        }
    }

    @Override
    public void setSync(int x, int y, int z, Block block) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invokeAndWait(() -> this.setSync(x, y, z, block));
            return;
        }

        this.set(x, y, z, block);
    }
    public void setDaytime(int daytime) {
        this.time = daytime;
    }
}
