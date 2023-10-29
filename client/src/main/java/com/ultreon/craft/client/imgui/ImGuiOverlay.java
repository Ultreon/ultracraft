package com.ultreon.craft.client.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.ChunkPos;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.concurrent.CompletableFuture;

public class ImGuiOverlay {
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);

    private static final ChunkPos RESET_CHUNK = new ChunkPos(17, 18);

    private static ImGuiImplGlfw imGuiGlfw;
    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;

    public static void setupImGui() {
        UltracraftClient.LOGGER.info("Setting up ImGui");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        ImGui.createContext();
        ImGuiOverlay.isContextCreated = true;
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

        UltracraftClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGlfw.init(windowHandle, true);
            ImGuiOverlay.imGuiGl3.init("#version 150");
        });
    }

    public static void preInitImGui() {
        ImGuiOverlay.imGuiGlfw = new ImGuiImplGlfw();
        ImGuiOverlay.imGuiGl3 = new ImGuiImplGl3();
        ImGuiOverlay.isImplCreated = true;
    }

    public static boolean isChunkSectionBordersShown() {
        return ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    public static void renderImGui(UltracraftClient client) {
        if (ImGuiOverlay.SHOW_IM_GUI.get()) {
            ImGuiOverlay.imGuiGlfw.newFrame();

            ImGui.newFrame();
            ImGui.setNextWindowPos(0, 0);
            ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 18);
            ImGui.setNextWindowCollapsed(true);

            if (Gdx.input.isCursorCatched()) {
                ImGui.getIO().setMouseDown(new boolean[5]);
                ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            if (ImGui.begin("MenuBar", ImGuiWindowFlags.NoMove |
                    ImGuiWindowFlags.NoCollapse |
                    ImGuiWindowFlags.AlwaysAutoResize |
                    ImGuiWindowFlags.NoTitleBar |
                    ImGuiWindowFlags.MenuBar |
                    ImGuiInputTextFlags.AllowTabInput)) {
                if (ImGui.beginMenuBar()) {
                    if (ImGui.beginMenu("View")) {
                        ImGui.menuItem("Show Player Utils", null, ImGuiOverlay.SHOW_PLAYER_UTILS);
                        ImGui.menuItem("Show Gui Utils", null, ImGuiOverlay.SHOW_GUI_UTILS);
                        ImGui.endMenu();
                    }
                    if (ImGui.beginMenu("Debug")) {
                        ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
                        ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
                        ImGui.menuItem("Show Chunk Section Borders", null, ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
                        ImGui.endMenu();
                    }

                    ImGui.text(" Frames Per Second: " + Gdx.graphics.getFramesPerSecond() + "   Frames ID: " + Gdx.graphics.getFrameId());
                    ImGui.endMenuBar();
                }
                ImGui.end();
            }

            if (ImGuiOverlay.SHOW_PLAYER_UTILS.get()) ImGuiOverlay.showPlayerUtilsWindow(client);
            if (ImGuiOverlay.SHOW_GUI_UTILS.get()) ImGuiOverlay.showGuiUtilsWindow(client);
            if (ImGuiOverlay.SHOW_UTILS.get()) ImGuiOverlay.showUtils(client);
            if (ImGuiOverlay.SHOW_CHUNK_DEBUGGER.get()) ImGuiOverlay.showChunkDebugger(client);

            ImGui.render();
            ImGuiOverlay.imGuiGl3.renderDrawData(ImGui.getDrawData());
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

    private static void showPlayerUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Player Utils", ImGuiOverlay.getDefaultFlags())) {
            ImGuiEx.text("Id:", () -> client.player.getId());
            ImGuiEx.text("Dead:", () -> client.player.isDead());
            ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", client.player.getWalkingSpeed(), v -> client.player.setWalkingSpeed(v));
            ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", client.player.getFlyingSpeed(), v -> client.player.setFlyingSpeed(v));
            ImGuiEx.editFloat("Gravity:", "PlayerGravity", client.player.gravity, v -> client.player.gravity = v);
            ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", client.player.jumpVel, v -> client.player.jumpVel = v);
            ImGuiEx.editFloat("Health:", "PlayerHealth", client.player.getHealth(), v -> client.player.setHealth(v));
            ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", client.player.getMaxHeath(), v -> client.player.setMaxHeath(v));
            ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", client.player.noGravity, v -> client.player.noGravity = v);
            ImGuiEx.editBool("Flying:", "PlayerFlying", client.player.isFlying(), v -> client.player.setFlying(v));
            ImGuiEx.editBool("Spectating:", "PlayerSpectating", client.player.isSpectating(), v -> client.player.setSpectating(v));
            ImGuiEx.bool("On Ground:", () -> client.player.onGround);
            ImGuiEx.bool("Colliding:", () -> client.player.isColliding);
            ImGuiEx.bool("Colliding X:", () -> client.player.isCollidingX);
            ImGuiEx.bool("Colliding Y:", () -> client.player.isCollidingY);
            ImGuiEx.bool("Colliding Z:", () -> client.player.isCollidingZ);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerX", client.player.getX(), v -> client.player.setX(v));
                ImGuiEx.editDouble("Y:", "PlayerY", client.player.getY(), v -> client.player.setY(v));
                ImGuiEx.editDouble("Z:", "PlayerZ", client.player.getZ(), v -> client.player.setZ(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Velocity")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerVelocityX", client.player.velocityX, v -> client.player.velocityX = v);
                ImGuiEx.editDouble("Y:", "PlayerVelocityY", client.player.velocityY, v -> client.player.velocityY = v);
                ImGuiEx.editDouble("Z:", "PlayerVelocityZ", client.player.velocityZ, v -> client.player.velocityZ = v);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Rotation")) {
                ImGui.treePush();
                ImGuiEx.editFloat("X:", "PlayerXRot", client.player.getXRot(), v -> client.player.setXRot(v));
                ImGuiEx.editFloat("Y:", "PlayerYRot", client.player.getYRot(), v -> client.player.setYRot(v));
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

    private static void showGuiUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Utils", ImGuiOverlay.getDefaultFlags())) {
            Screen currentScreen = client.screen;
            ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
            if (currentScreen != null) {
                GuiComponent exactWidgetAt = currentScreen.getExactWidgetAt((int) (Gdx.input.getX() / client.getGuiScale()), (int) (Gdx.input.getY() / client.getGuiScale()));
                if (exactWidgetAt != null) {
                    client.shapes.setColor(1.0F, 0.0F, 1.0F, 1.0F);
                    client.shapes.rectangle(
                            exactWidgetAt.getX() * client.getGuiScale(), exactWidgetAt.getY() * client.getGuiScale(),
                            exactWidgetAt.getWidth() * client.getGuiScale(), exactWidgetAt.getHeight() * client.getGuiScale()
                    );
                }
                ImGuiEx.text("Widget:", () -> exactWidgetAt == null ? null : exactWidgetAt.getClass().getSimpleName());
            }
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

    public static boolean isShowingImGui() {
        return ImGuiOverlay.SHOW_IM_GUI.get();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
    }

    public static void dispose() {
        if (ImGuiOverlay.isImplCreated) {
            ImGuiOverlay.imGuiGl3.dispose();
            ImGuiOverlay.imGuiGlfw.dispose();
        }

        if (ImGuiOverlay.isContextCreated) {
            ImGui.destroyContext();
        }
    }
}
