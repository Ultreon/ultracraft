package com.ultreon.craft.client.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.util.ImGuiEx;
import com.ultreon.craft.world.ChunkPos;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ImGuiOverlay {
    public static final ImFloat I_GAMMA = new ImFloat(1.5f);
    public static final ImFloat U_CAP = new ImFloat(0.45f);
    public static final ImFloat U_RADIUS = new ImFloat(0.45f);
    public static final ImFloat U_INTENSITY = new ImFloat(1.5f);
    public static final ImFloat U_MULTIPLIER = new ImFloat(1000.0f);
    public static final ImFloat U_DEPTH_TOLERANCE = new ImFloat(0.0001f);
    public static final ImInt MODEL_VIEWER_LIST_INDEX = new ImInt(0);
    public static final ImBoolean SHOW_RENDER_PIPELINE = new ImBoolean(false);
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_SHADER_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_MODEL_VIEWER = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);
    private static final ImBoolean SHOW_PROFILER = new ImBoolean(false);

    private static final ChunkPos RESET_CHUNK = new ChunkPos(17, 18);
    protected static final String[] keys = {"A", "B", "C"};
    protected static final Double[] values = {0.1, 0.3, 0.6};
    private static final Vector3 TRANSLATE_TMP = new Vector3();
    private static final Vector3 SCALE_TMP = new Vector3();
    private static final Quaternion ROTATE_TMP = new Quaternion();

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;
    private static final GuiEditor guiEditor = new GuiEditor();
    private static boolean triggerLoadWorld;
    private static ImPlotContext imPlotCtx;
    private static String[] modelViewerList = new String[0];

    public static void setupImGui() {
        UltracraftClient.LOGGER.info("Setting up ImGui");

        UltracraftClient.get().deferClose(GLFWErrorCallback.create((error, description) -> UltracraftClient.LOGGER.error("GLFW Error: {}", description)).set());
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        synchronized (ImGuiOverlay.class) {
            ImGui.createContext();
            ImGuiOverlay.imPlotCtx = ImPlot.createContext();
            ImGuiOverlay.isContextCreated = true;
        }
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

        UltracraftClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 110");
        });
    }

    public static void preInitImGui() {
        synchronized (ImGuiOverlay.class) {
            ImGuiOverlay.imGuiGlfw = new ImGuiImplGlfw();
            ImGuiOverlay.imGuiGl3 = new ImGuiImplGl3();
            ImGuiOverlay.isImplCreated = true;
        }
    }

    public static boolean isChunkSectionBordersShown() {
        return ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    public static void renderImGui(UltracraftClient client) {
        if (!ImGuiOverlay.SHOW_IM_GUI.get()) return;

        ImGuiOverlay.imGuiGlfw.newFrame();

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(client.getWidth(), 18);
        ImGui.setNextWindowCollapsed(true);

        if (Gdx.input.isCursorCatched()) {
            ImGui.getIO().setMouseDown(new boolean[5]);
            ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        ImGuiOverlay.renderDisplay();

        if (ImGui.begin("MenuBar", ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.MenuBar |
                ImGuiInputTextFlags.AllowTabInput)) {
            ImGuiOverlay.renderMenuBar();
            ImGui.end();
        }

        ImGuiOverlay.renderWindows(client);

        ImGuiOverlay.handleTriggers();

        ImGui.render();
        ImGuiOverlay.imGuiGl3.renderDrawData(ImGui.getDrawData());

        ImGuiOverlay.handleInput();
    }

    private static void renderDisplay() {
        if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                Path filePathName = Path.of(ImGuiFileDialog.getFilePathName());
                UltracraftClient.invoke(() -> UltracraftClient.get().startWorld(filePathName));
            }
            ImGuiFileDialog.close();
        }
    }

    private static void handleTriggers() {
        if (ImGuiOverlay.triggerLoadWorld) {
            ImGuiOverlay.triggerLoadWorld = false;
            ImGuiFileDialog.openModal("Main::loadWorld", "Choose Folder", null, UltracraftClient.getGameDir().toAbsolutePath().toString(), "", 1, 7, ImGuiFileDialogFlags.None);
        }
    }

    private static void renderWindows(UltracraftClient client) {
        if (ImGuiOverlay.SHOW_PLAYER_UTILS.get()) ImGuiOverlay.showPlayerUtilsWindow(client);
        if (ImGuiOverlay.SHOW_GUI_UTILS.get()) ImGuiOverlay.showGuiEditor(client);
        if (ImGuiOverlay.SHOW_UTILS.get()) ImGuiOverlay.showUtils(client);
        if (ImGuiOverlay.SHOW_CHUNK_DEBUGGER.get()) ImGuiOverlay.showChunkDebugger(client);
        if (ImGuiOverlay.SHOW_SHADER_EDITOR.get()) ImGuiOverlay.showShaderEditor();
        if (ImGuiOverlay.SHOW_MODEL_VIEWER.get()) ImGuiOverlay.showModelViewer();
    }

    private static void showModelViewer() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Model Viewer", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button("Reload")) {
                modelViewerList = UltracraftClient.get().entityModelManager.getRegistry().keySet().stream().map(EntityType::getId).map(Objects::toString).sorted(String.CASE_INSENSITIVE_ORDER).toArray(String[]::new);
            }

            ImGui.text("Select Model:");
            ImGui.sameLine();
            ImGui.listBox("##ModelViewer::ListBox", MODEL_VIEWER_LIST_INDEX, modelViewerList);

            if (modelViewerList.length == 0) {
                ImGui.text("No models found");
            } else {

                String s = modelViewerList[MODEL_VIEWER_LIST_INDEX.get()];
                Identifier id = new Identifier(s);
                EntityType<?> entityType = Registries.ENTITY_TYPE.get(id);
                if (entityType != null) {
                    Model model = UltracraftClient.get().entityModelManager.getFinished(entityType);
                    if (model != null) {
                        if (ImGui.treeNode("Model")) {
                            ImGui.text("Model Name:");
                            ImGui.sameLine();
                            ImGui.text(s);

                            if (ImGui.treeNode("Nodes")) {
                                for (Node node : model.nodes) {
                                    drawNode(node);
                                }

                                ImGui.treePop();
                            }

                            ImGui.treePop();
                        }
                    }
                }
            }

            if (ImGui.button("Close")) {
                ImGuiOverlay.SHOW_MODEL_VIEWER.set(false);
            }

        }
        ImGui.end();
    }

    private static void drawNode(Node node) {
        if (ImGui.treeNode(node.id)) {
            ImGui.text("Name:");
            ImGui.sameLine();
            ImGui.text(node.id);

            ImGui.text("Local Transform:");
            ImGui.treePush();
            drawTransform(node.localTransform, node);
            ImGui.treePop();

            ImGui.text("Global Transform:");
            ImGui.treePush();
            drawTransform(node.globalTransform, node);
            ImGui.treePop();

            for (Node child : node.getChildren()) {
                drawNode(child);
            }

            ImGui.treePop();
        }
    }

    private static void drawTransform(Matrix4 node, Node node1) {
        Vector3 translation = node.getTranslation(TRANSLATE_TMP);
        drawVec3("Translation:", translation);

        Vector3 scale = node1.localTransform.getScale(SCALE_TMP);
        drawVec3("Scale:", scale);

        Quaternion rotation = node1.localTransform.getRotation(ROTATE_TMP);
        ImGui.text("Rotation:");
        ImGui.sameLine();
        ImGui.text("X: " + rotation.x + " Y: " + rotation.y + " Z: " + rotation.z + " W: " + rotation.w);
    }

    private static void drawVec3(String name, Vector3 vec3) {
        ImGui.text(name);
        ImGui.sameLine();
        ImGui.text("X: " + vec3.x + " Y: " + vec3.y + " Z: " + vec3.z);
    }

    private static void handleInput() {
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.O))
            ImGuiOverlay.triggerLoadWorld = true;
        else if (Gdx.input.isKeyJustPressed(Input.Keys.P))
            ImGuiOverlay.SHOW_PLAYER_UTILS.set(!ImGuiOverlay.SHOW_PLAYER_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.G))
            ImGuiOverlay.SHOW_GUI_UTILS.set(!ImGuiOverlay.SHOW_GUI_UTILS.get());
        else if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.set(!ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get());
    }

    private static void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Load World...", "Ctrl+O")) {
                    ImGuiOverlay.triggerLoadWorld = true;
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Edit")) {
                ImGui.menuItem("Player Editor", "Ctrl+P", ImGuiOverlay.SHOW_PLAYER_UTILS);
                ImGui.menuItem("Gui Editor", "Ctrl+G", ImGuiOverlay.SHOW_GUI_UTILS);
                ImGui.menuItem("Shader Editor", "", ImGuiOverlay.SHOW_SHADER_EDITOR);
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("View")) {
                ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
                ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
                ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
                ImGui.menuItem("InspectionRoot", "Ctrl+P", ImGuiOverlay.SHOW_PROFILER);
                ImGui.menuItem("Render Pipeline", null, ImGuiOverlay.SHOW_RENDER_PIPELINE);
                ImGui.menuItem("Model Viewer", null, ImGuiOverlay.SHOW_MODEL_VIEWER);
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Resources")) {
                if (ImGui.menuItem("Reload Resources", "F1+R")) {
                    UltracraftClient.get().reloadResourcesAsync();
                }
                ImGui.endMenu();
            }

            ImGui.text(" FPS: " + Gdx.graphics.getFramesPerSecond() + " ");
            ImGui.sameLine();
            ImGui.text(" Client TPS: " + Gdx.graphics.getFramesPerSecond() + " ");
            ImGui.sameLine();
            UltracraftServer server = UltracraftServer.get();
            if (server != null) {
                ImGui.text(" Server TPS: " + server.getCurrentTps() + " ");
                ImGui.sameLine();
            }
            ImGui.text(" Frame ID: " + Gdx.graphics.getFrameId() + " ");
            ImGui.endMenuBar();
        }
    }

    private static void showChunkDebugger(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Chunk Debugging", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button("Reset chunk at %s".formatted(ImGuiOverlay.RESET_CHUNK))) {
                CompletableFuture.runAsync(() -> {
                    ClientWorld world = client.world;
                    UltracraftClient.invokeAndWait(() -> {
                        if (world != null) {
                            world.unloadChunk(ImGuiOverlay.RESET_CHUNK);
                        }
                    });
                    UltracraftServer.invokeAndWait(() -> client.integratedServer.getWorld().regenerateChunk(ImGuiOverlay.RESET_CHUNK));
                });
            }
            ImGui.end();
        }
    }

    private static void showShaderEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Shader Editor", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma", ImGuiOverlay.I_GAMMA::get, ImGuiOverlay.I_GAMMA::set);
            ImGui.end();
        }
    }

    private static void showPlayerUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Player Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.text("Id:", client.player::getId);
            ImGuiEx.text("Dead:", client.player::isDead);
            ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", client.player::getWalkingSpeed, client.player::setWalkingSpeed);
            ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", client.player::getFlyingSpeed, client.player::setFlyingSpeed);
            ImGuiEx.editFloat("Gravity:", "PlayerGravity", () -> client.player.gravity, v -> client.player.gravity = v);
            ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", () -> client.player.jumpVel, v -> client.player.jumpVel = v);
            ImGuiEx.editFloat("Health:", "PlayerHealth", client.player::getHealth, client.player::setHealth);
            ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", client.player::getMaxHealth, client.player::setMaxHeath);
            ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", () -> client.player.noGravity, v -> client.player.noGravity = v);
            ImGuiEx.editBool("Flying:", "PlayerFlying", client.player::isFlying, client.player::setFlying);
            ImGuiEx.editBool("Allow Flight:", "PlayerAllowFlight", client.player::isAllowFlight, v -> {});
            ImGuiEx.bool("On Ground:", () -> client.player.onGround);
            ImGuiEx.bool("Colliding:", () -> client.player.isColliding);
            ImGuiEx.bool("Colliding X:", () -> client.player.isCollidingX);
            ImGuiEx.bool("Colliding Y:", () -> client.player.isCollidingY);
            ImGuiEx.bool("Colliding Z:", () -> client.player.isCollidingZ);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerX", client.player::getX, v -> client.player.setX(v));
                ImGuiEx.editDouble("Y:", "PlayerY", client.player::getY, v -> client.player.setY(v));
                ImGuiEx.editDouble("Z:", "PlayerZ", client.player::getZ, v -> client.player.setZ(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Velocity")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerVelocityX", () -> client.player.velocityX, v -> client.player.velocityX = v);
                ImGuiEx.editDouble("Y:", "PlayerVelocityY", () -> client.player.velocityY, v -> client.player.velocityY = v);
                ImGuiEx.editDouble("Z:", "PlayerVelocityZ", () -> client.player.velocityZ, v -> client.player.velocityZ = v);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Rotation")) {
                ImGui.treePush();
                ImGuiEx.editFloat("X:", "PlayerXRot", client.player::getXRot, v -> client.player.setXRot(v));
                ImGuiEx.editFloat("Y:", "PlayerYRot", client.player::getYRot, v -> client.player.setYRot(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Player Input")) {
                ImGui.treePush();
                ImGuiEx.bool("Forward", () -> client.playerInput.forward);
                ImGuiEx.bool("Backward", () -> client.playerInput.backward);
                ImGuiEx.bool("Left", () -> client.playerInput.strafeLeft);
                ImGuiEx.bool("Right", () -> client.playerInput.strafeRight);
                ImGuiEx.bool("Up", () -> client.playerInput.up);
                ImGuiEx.bool("Down", () -> client.playerInput.down);
                ImGui.treePop();
            }
            ImGui.end();
        }
    }

    private static void showGuiEditor(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Editor", ImGuiOverlay.getDefaultFlags())) {
            ImGuiOverlay.guiEditor.render(client);
        }
        ImGui.end();
    }

    private static void showUtils(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.slider("FOV", "GameFOV", (int) client.camera.fieldOfView, 10, 150, i -> client.camera.fieldOfView = i);
        }
        ImGui.end();
    }

    private static int getDefaultFlags() {
        boolean cursorCaught = Gdx.input.isCursorCatched();
        var flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    public static boolean isShown() {
        return ImGuiOverlay.SHOW_IM_GUI.get();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
    }

    public static boolean isProfilerShown() {
        return ImGuiOverlay.SHOW_PROFILER.get();
    }

    public static void dispose() {
        synchronized (ImGuiOverlay.class) {
            if (ImGuiOverlay.isImplCreated) {
                ImGuiOverlay.imGuiGl3.dispose();
                ImGuiOverlay.imGuiGlfw.dispose();
            }

            if (ImGuiOverlay.isContextCreated) {
                ImGui.destroyContext();
                ImPlot.destroyContext(ImGuiOverlay.imPlotCtx);
            }
        }
    }
}
