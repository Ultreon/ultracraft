package com.ultreon.craft.world;

import com.google.common.collect.Queues;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Queue;

public class ChunkBuilder {
    private final Queue<ChunkPos> chunksToLoad = Queues.synchronizedQueue(Queues.newArrayDeque());
    private final Thread thread;
    private boolean running = true;
    private final World world;

    public ChunkBuilder(World world) {
        super();
        this.world = world;

        this.thread = new Thread(this::run);
        this.thread.start();
    }

    private void run() {
        while (this.running) {
            try {
                ChunkPos poll = this.chunksToLoad.poll();
                if (poll != null) {
                    if (this.world.getChunk(poll) != null) continue;

                    Player player = UltreonCraft.get().player;
                    if (player == null) continue;

                    Vec3d pos = player.getPosition();
                    int startX = (int) (pos.x - this.world.getRenderDistance() * World.CHUNK_SIZE / 2);
                    int startZ = (int) (pos.z - this.world.getRenderDistance() * World.CHUNK_SIZE / 2);
                    int endX = (int) (pos.x + this.world.getRenderDistance() * World.CHUNK_SIZE / 2);
                    int endZ = (int) (pos.z + this.world.getRenderDistance() * World.CHUNK_SIZE / 2);

                    if (poll.x() >= startX && poll.x() <= endX && poll.z() >= startZ && poll.z() <= endZ) {
                        this.world.loadChunk(poll);
                        Thread.sleep(100);
                    }
                }
            } catch (InterruptedException e) {
                this.chunksToLoad.clear();
                return;
            }
        }

        this.chunksToLoad.clear();
    }

    public void shutdown() {
        this.running = false;
    }

    public void join() throws InterruptedException {
        this.running = false;
        this.thread.join();
    }

    public void defer(@NotNull ChunkPos chunkPos) {
        this.chunksToLoad.offer(chunkPos);
    }

    public void defer(@NotNull Collection<ChunkPos> chunks) {
        this.chunksToLoad.addAll(chunks);
    }

    public boolean isEmpty() {
        return this.chunksToLoad.isEmpty();
    }
}
