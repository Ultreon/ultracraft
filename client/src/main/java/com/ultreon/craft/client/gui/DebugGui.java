package com.ultreon.craft.client.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Mesh;
import com.ultreon.craft.client.IntegratedServer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.world.ChunkMesh;
import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.debug.ProfileData;
import com.ultreon.craft.debug.Section.FinishedSection;
import com.ultreon.craft.debug.ThreadSection.FinishedThreadSection;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class DebugGui {
    private static final int OFFSET = 10;
    private final UltracraftClient client;
    private int leftY;
    private ProfileData profile;
    private long lastUpdate;
    private @NotNull String currentPath = "/";
    private @Nullable Thread currentThread = null;
    private String idxInput = "";
    private String inspectCurrentPath = "/";
    private String inspectIdxInput = "";

    public DebugGui(UltracraftClient client) {
        this.client = client;
    }

    public void render(Renderer renderer) {
        this.leftY = DebugGui.OFFSET;

        @Nullable ClientWorld world = this.client.world;
        @Nullable WorldRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer != null && worldRenderer.isDisposed()) worldRenderer = null;
        if (world != null && world.isDisposed()) world = null;

        if (UltracraftClient.PROFILER.isProfiling() || this.client.isRenderingWorld()) {
            this.left(renderer, "FPS", Gdx.graphics.getFramesPerSecond());
            this.left(renderer, "TPS", this.client.getCurrentTps());
            this.left();
        }

        if (UltracraftClient.PROFILER.isProfiling()) {
            this.renderProfiler(renderer);
            return;
        }

        if (this.client.inspection.isInspecting()) {
            this.renderInspector(renderer);
            return;
        }

        if (!this.client.showDebugHud || this.client.world == null) return;

        this.left(renderer, "GDX Status");
        this.left(renderer, "Mesh Status", Mesh.getManagedStatus());
        this.left();

        // World
        @Nullable IntegratedServer integratedServer = this.client.integratedServer;
        if (integratedServer != null) {
            this.left(renderer, "Integrated Server");
            this.left(renderer, "Server TPS", integratedServer.getCurrentTps());
            this.left(renderer, "Packets", "rx = " + Connection.getPacketsReceived() + ", tx = " + Connection.getPacketsSent());
            this.left();
        }

        this.left(renderer, "Meshes");
        this.left(renderer, "Meshes Disposed", ChunkMesh.getMeshesDisposed());
        this.left(renderer, "Vertex Count", WorldRenderer.getVertexCount());
        this.left();

        if (world != null) {
            // Player
            Player player = this.client.player;
            if (player != null) {
                this.left(renderer, "Player");
                BlockPos blockPosition = player.blockPosition();
                Vec3i sectionPos = this.block2sectionPos(blockPosition);
                ChunkPos chunkPos = player.getChunkPos();
                ClientChunk chunk = world.getChunk(chunkPos);
                this.left(renderer, "XYZ", player.getPosition());
                this.left(renderer, "Block XYZ", blockPosition);
                this.left(renderer, "Chunk XYZ", sectionPos);
                if (chunk != null) {
                    this.left(renderer, "Chunk Offset", chunk.renderOffset);
                }
                this.left(renderer, "Chunk Shown", world.getChunk(chunkPos) != null);
                HitResult hitResult = this.client.hitResult;
                if (hitResult != null)
                    this.left(renderer, "Break Progress", world.getBreakProgress(new BlockPos(hitResult.getPos())));
                this.left();
            }

            this.left(renderer, "World");
            if (worldRenderer != null) {
                this.left(renderer, "Visible Chunks", worldRenderer.getVisibleChunks() + "/" + worldRenderer.getLoadedChunks());
            }

            this.left(renderer, "Chunk Mesh Disposes", WorldRenderer.getChunkMeshFrees());
            if (this.client.isSinglePlayer()) {
                this.left(renderer, "Chunk Loads", ServerWorld.getChunkLoads());
                this.left(renderer, "Chunk Unloads", ServerWorld.getChunkUnloads());
                this.left(renderer, "Chunk Saves", ServerWorld.getChunkSaves());
            }
            this.left(renderer, "Pool Free", WorldRenderer.getPoolFree());
            this.left(renderer, "Pool Max", WorldRenderer.getPoolMax());
            this.left(renderer, "Pool Peak", WorldRenderer.getPoolPeak());
            this.left();
        }
    }

    private void renderInspector(Renderer renderer) {
        String path = this.inspectCurrentPath;

        Comparator<InspectionNode<?>> comparator = Comparator.comparing(InspectionNode::getName);

        this.entryLine(renderer, TextObject.nullToEmpty(this.inspectIdxInput).setColor(Color.WHITE));

        this.entryLine(renderer, TextObject.literal(path).setColor(Color.AZURE).setBold(true).setUnderlined(true));
        this.entryLine(renderer);

        {
            @Nullable InspectionNode<?> node = this.client.inspection.getNode(path);
            if (node == null) {
                this.inspectCurrentPath = "/";
                return;
            }

            List<InspectionNode<?>> nodes = node.getNodes().values().stream().sorted(comparator).toList();
            for (int i = 0, nodeSize = nodes.size(); i < nodeSize; i++) {
                InspectionNode<?> curNode = nodes.get(i);
                this.entryLine(renderer, i, curNode.getName());
            }

            List<Pair<String, String>> elements = node.getElements().entrySet().stream().map(t -> new Pair<>(t.getKey(), t.getValue().get())).sorted(Comparator.comparing(Pair::getFirst)).toList();
            for (Pair<String, String> element : elements) {
                this.entryLine(renderer, element.getFirst(), element.getSecond());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            String input = this.inspectIdxInput;
            try {
                int idx = Integer.parseInt(input);

                @Nullable InspectionNode<?> node = this.client.inspection.getNode(path);
                if (node == null) {
                    this.inspectCurrentPath = "/";
                    return;
                }
                List<InspectionNode<?>> nodes = node.getNodes().values().stream().sorted(comparator).toList();

                if (nodes.isEmpty()) return;

                if (idx >= 0 && idx < nodes.size()) {
                    path += nodes.get(idx).getName() + "/";
                    this.inspectCurrentPath = path;
                }
                this.inspectIdxInput = "";
            } catch (NumberFormatException ignored) {
                this.inspectIdxInput = "";
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (this.inspectCurrentPath.equals("/")) {
                return;
            }

            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            this.inspectCurrentPath = path.substring(0, path.lastIndexOf("/")) + "/";
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) {
            this.inspectIdxInput = "";
        } else for (int num = 0; num < 10; num++) {
            int key = Input.Keys.NUMPAD_0 + num;
            if (Gdx.input.isKeyJustPressed(key)) {
                this.inspectIdxInput += String.valueOf(num);
                break;
            }
        }
    }

    public void updateProfiler() {
        if (this.lastUpdate + 1000 < System.currentTimeMillis()) {
            this.profile = UltracraftClient.PROFILER.collect();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    private void renderProfiler(Renderer renderer) {
        String path = this.currentPath;
        Thread thread = this.currentThread;

        if (this.profile == null) return;

        Comparator<FinishedSection> comparator = (o1, o2) -> {
            int compare = Long.compare(o1.getNanos(), o2.getNanos());
            if (compare == 0) {
                return o1.getName().compareTo(o2.getName());
            }

            return compare;
        };

        this.entryLine(renderer, TextObject.nullToEmpty(this.idxInput).setColor(Color.WHITE));

        if (thread == null) {
            this.entryLine(renderer, TextObject.literal("Thread View").setColor(Color.GREEN).setBold(true).setUnderlined(true));
            this.entryLine(renderer);

            List<Thread> threads = this.profile.getThreads().stream().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).toList();
            if (threads.isEmpty()) return;

            for (int i = 0, threadsSize = threads.size(); i < threadsSize; i++) {
                Thread t = threads.get(i);
                this.entryLine(renderer, i, t.getName());
            }
        } else {
            FinishedThreadSection threadSection = this.profile.getThreadSection(thread);
            List<FinishedSection> data;

            this.entryLine(renderer, TextObject.literal(path).setColor(Color.AZURE).setBold(true).setUnderlined(true));
            this.entryLine(renderer);

            if (path.equals("/")) {
                data = threadSection.getData().values().stream().sorted(comparator).toList();
            } else {
                FinishedSection section = this.profile.getSection(threadSection, path);
                if (section == null) {
                    this.currentThread = null;
                    this.currentPath = "/";
                    return;
                }

                data = section.getData().values().stream().sorted(comparator).toList();
            }
            for (int i = 0, sectionsSize = data.size(); i < sectionsSize; i++) {
                FinishedSection s = data.get(i);
                this.entryLine(renderer, i, s.getName(), s.getNanos());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER)) {
            String input = this.idxInput;
            try {
                int idx = Integer.parseInt(input);
                if (thread == null) {
                    List<Thread> threads = this.profile.getThreads().stream().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())).toList();
                    if (idx >= 0 && idx < threads.size()) {
                        thread = threads.get(idx);
                        this.currentThread = thread;
                        this.currentPath = "/";
                    }
                } else {
                    FinishedThreadSection threadSection = this.profile.getThreadSection(thread);
                    List<FinishedSection> data;

                    if (path.equals("/")) {
                        data = threadSection.getData().values().stream().sorted(comparator).toList();
                    } else {
                        FinishedSection section = this.profile.getSection(threadSection, path);
                        if (section == null) {
                            this.currentThread = null;
                            this.currentPath = "/";
                            return;
                        }
                        data = section.getData().values().stream().sorted(comparator).toList();
                    }

                    if (data.isEmpty()) return;

                    if (idx >= 0 && idx < data.size()) {
                        path += data.get(idx).getName() + "/";
                        this.currentPath = path;
                    }
                }
                this.idxInput = "";
            } catch (NumberFormatException ignored) {
                this.idxInput = "";
            }
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (this.currentPath.equals("/")) {
                this.currentThread = null;
                return;
            }

            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            this.currentPath = path.substring(0, path.lastIndexOf("/")) + "/";
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_DOT)) {
            this.idxInput = "";
        } else for (int num = 0; num < 10; num++) {
            int key = Input.Keys.NUMPAD_0 + num;
            if (Gdx.input.isKeyJustPressed(key)) {
                this.idxInput += String.valueOf(num);
                break;
            }
        }
    }

    private void left(Renderer renderer, MutableText mutableText) {
        int width = renderer.getFont().width(mutableText);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, width + 5, 12, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(mutableText, DebugGui.OFFSET, this.leftY);
        this.leftY += 13;
    }

    private Vec3i block2sectionPos(BlockPos blockPos) {
        return new Vec3i(blockPos.x() / 16, blockPos.y() / 16, blockPos.z() / 16);
    }

    public void left(Renderer renderer, String name, Object value) {
        MutableText textObject = TextObject.literal(name).append(": ").append(TextObject.literal(String.valueOf(value)).setColor(Color.LIGHT_GRAY));
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(textObject, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
    }

    public void left(Renderer renderer, String text) {
        MutableText textObject = TextObject.literal(text).setBold(true).setUnderlined(true).setColor(Color.GOLD);
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(textObject, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
    }

    public void left() {
        this.leftY += 11;
    }

    public void entryLine(Renderer renderer, int idx, String name, long nanos) {
        MutableText lText = TextObject.literal("[" + idx + "] ").setColor(Color.GOLD).append(TextObject.literal(name).setColor(Color.WHITE));
        MutableText rText;
        if (nanos < 10000.0)
            rText = TextObject.literal("< 0.01").setColor(Color.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(Color.rgb(0xa0a0a0)));
        else
            rText = TextObject.literal("%.2f".formatted(nanos / 1000000.0)).setColor(Color.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(Color.rgb(0xa0a0a0)));
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(lText, DebugGui.OFFSET, this.leftY);
        renderer.drawTextRight(rText, DebugGui.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
    }

    private void entryLine(Renderer renderer, int idx, String name) {
        MutableText text = TextObject.literal("[" + idx + "] ").setColor(Color.GOLD).append(TextObject.literal(name).setColor(Color.WHITE));
        int width = renderer.getFont().width(text);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(text, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
    }

    private void entryLine(Renderer renderer, TextObject text) {
        int width = renderer.getFont().width(text);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(text, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
    }

    public void entryLine(Renderer renderer, String name, String value) {
        MutableText lText = TextObject.literal(name).setColor(Color.WHITE);
        MutableText rText = TextObject.literal(value).setColor(Color.LIGHT_GRAY);
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, Color.BLACK.withAlpha(128));
        renderer.drawTextLeft(lText, DebugGui.OFFSET, this.leftY);
        renderer.drawTextRight(rText, DebugGui.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
    }

    public void entryLine(Renderer renderer) {
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, 304, 11, Color.BLACK.withAlpha(128));
        this.leftY += 11;
    }
}
