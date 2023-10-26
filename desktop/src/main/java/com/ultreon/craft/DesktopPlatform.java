package com.ultreon.craft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.client.GameEnvironment;
import com.ultreon.craft.client.GamePlatform;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.desktop.client.gui.screen.ModIconOverrides;
import com.ultreon.craft.desktop.client.gui.screen.ModListScreen;
import com.ultreon.craft.desktop.mods.ClientModInit;
import com.ultreon.craft.desktop.mods.DedicatedServerModInit;
import com.ultreon.craft.desktop.mods.ModInit;
import com.ultreon.craft.desktop.util.util.ArgParser;
import com.ultreon.craft.desktop.util.util.ImGuiEx;
import com.ultreon.craft.client.platform.OperatingSystem;
import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.util.EnumUtils;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.resources.v0.ResourceManager;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Platform;
import org.oxbow.swingbits.dialog.task.TaskDialog;
import org.oxbow.swingbits.util.Strings;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

public class DesktopPlatform extends GamePlatform {
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final Logger LOGGER = LoggerFactory.getLogger("GamePlatform");
    private final String gameDir;
    private final GameEnvironment gameEnv;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;
    private final ImBoolean showImGui = new ImBoolean(false);
    private final ArgParser argParser;
    private final boolean packaged;

    public DesktopPlatform(ArgParser argParser) {
        this.argParser = argParser;
        this.gameDir = QuiltLoader.getGameDir().toAbsolutePath().toString();
        this.gameEnv = EnumUtils.byName(System.getProperty("ultracraft.environment", "normal").toUpperCase(Locale.ROOT), GameEnvironment.NORMAL);
        this.packaged = this.gameEnv == GameEnvironment.PACKAGED;
    }

    @Override
    public Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return switch (Platform.get()) {
            case LINUX -> OperatingSystem.LINUX;
            case MACOSX -> OperatingSystem.MAC_OS;
            case WINDOWS -> OperatingSystem.WINDOWS;
        };
    }

    @Override
    public void setupImGui() {
        UltracraftClient.LOGGER.info("Setting up ImGui");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        ImGui.createContext();
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

        UltracraftClient.invokeAndWait(() -> {
            this.imGuiGlfw.init(windowHandle, true);
            this.imGuiGl3.init("#version 150");
        });
    }

    @Override
    public void preInitImGui() {
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();
    }

    @Override
    public boolean isChunkSectionBordersShown() {
        return DesktopPlatform.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    @Override
    public void renderImGui(UltracraftClient client) {
        if (this.showImGui.get()) {
            this.imGuiGlfw.newFrame();

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
                        ImGui.menuItem("Show Player Utils", null, DesktopPlatform.SHOW_PLAYER_UTILS);
                        ImGui.menuItem("Show Gui Utils", null, DesktopPlatform.SHOW_GUI_UTILS);
                        ImGui.endMenu();
                    }
                    if (ImGui.beginMenu("Debug")) {
                        ImGui.menuItem("Utils", null, DesktopPlatform.SHOW_UTILS);
                        ImGui.menuItem("Show Chunk Section Borders", null, DesktopPlatform.SHOW_CHUNK_SECTION_BORDERS);
                        ImGui.endMenu();
                    }

                    ImGui.text(" Frames Per Second: " + Gdx.graphics.getFramesPerSecond() + "   Frames ID: " + Gdx.graphics.getFrameId());
                    ImGui.endMenuBar();
                }
                ImGui.end();
            }

            if (DesktopPlatform.SHOW_PLAYER_UTILS.get()) this.showPlayerUtilsWindow(client);
            if (DesktopPlatform.SHOW_GUI_UTILS.get()) this.showGuiUtilsWindow(client);
            if (DesktopPlatform.SHOW_UTILS.get()) this.showUtils(client);

            ImGui.render();
            this.imGuiGl3.renderDrawData(ImGui.getDrawData());
        }
    }

    private void showPlayerUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (client.player != null && ImGui.begin("Player Utils", this.getDefaultFlags())) {
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

    private void showGuiUtilsWindow(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Utils", this.getDefaultFlags())) {
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

    private void showUtils(UltracraftClient client) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Utils", this.getDefaultFlags())) {
            ImGuiEx.slider("FOV", "GameFOV", (int) client.camera.fieldOfView, 10, 150, i -> client.camera.fieldOfView = i);
        }
        ImGui.end();
    }

    private int getDefaultFlags() {
        boolean cursorCaught = Gdx.input.isCursorCatched();
        var flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    @Override
    public boolean isShowingImGui() {
        return this.showImGui.get();
    }

    @Override
    public void setShowingImGui(boolean value) {
        this.showImGui.set(value);
    }

    @Override
    public void firstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    @Override
    public void dispose() {
        this.imGuiGl3.dispose();
        this.imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    @Override
    public FileHandle dataFile(String path) {
        return Gdx.files.absolute(this.gameDir).child(path);
    }

    public ArgParser getArgParser() {
        return this.argParser;
    }

    @Override
    public void openModList() {
        UltracraftClient.get().showScreen(new ModListScreen(UltracraftClient.get().screen));
    }

    @Override
    public boolean isModsSupported() {
        return true;
    }

    @Override
    public void setupMods() {
        super.setupMods();

        ModIconOverrides.set("craft", UltracraftClient.id("icon.png"));
        ModIconOverrides.set("libgdx", new Identifier("libgdx", "icon.png"));

        // Invoke entry points.
        EntrypointUtil.invoke(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
    }

    @Override
    public void setupModsClient() {
        super.setupModsClient();

        // Invoke entry points.
        EntrypointUtil.invoke(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setupModsServer() {
        super.setupModsServer();

        // Invoke entry points.
        EntrypointUtil.invoke(DedicatedServerModInit.ENTRYPOINT_KEY, DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer);
    }

    @Override
    public void importModResources(ResourceManager resourceManager) {
        super.importModResources(resourceManager);

        for (ModContainer mod : QuiltLoader.getAllMods()) {
            for (Path rootPath : mod.getSourcePaths().stream().reduce(new ArrayList<>(), (objects, paths) -> {
                objects.addAll(paths);
                return objects;
            })) {
                try {
                    resourceManager.importPackage(rootPath);
                } catch (IOException e) {
                    DesktopPlatform.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e);
                }
            }
        }
    }

    @Override
    public void handleCrash(CrashLog crashLog) {
        File file = new File(GamePlatform.data("crash-reports").file(), crashLog.getDefaultFileName());
        crashLog.writeToFile(file);

        try {
            Gdx.input.setCursorCatched(false);
            Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
            graphics.getWindow().setVisible(false);
        } catch (Throwable ignored) {

        }

        try {
            SwingUtilities.invokeAndWait(() -> {
                TaskDialog dlg = new TaskDialog(null, "Game crashed!");

                String title = "Game crashed!";
                String description = "See crash report below:";
                boolean noMessage = Strings.isEmpty(title);

                dlg.setInstruction(noMessage ? description : title);
                dlg.setText(noMessage ? "" : description);

                dlg.setIcon(UIManager.getIcon(TaskDialog.StandardIcon.ERROR));
                dlg.setCommands(TaskDialog.StandardCommand.CANCEL.derive(TaskDialog.makeKey("Close")));

                JTextArea text = new JTextArea();
                text.setEditable(false);
                text.setFont(new Font("Monospaced", Font.PLAIN, 11));
                text.setText(crashLog.toString());
                text.setCaretPosition(0);

                JScrollPane scroller = new JScrollPane(text);
                scroller.setPreferredSize(new Dimension(400, 200));
                dlg.getDetails().setExpandableComponent(scroller);
                dlg.getDetails().setExpanded(noMessage);

                dlg.setResizable(true);
                dlg.setVisible(true);

            });
        } catch (Throwable ignored) {

        }
        Runtime.getRuntime().halt(1);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return QuiltLoader.isDevelopmentEnvironment();
    }

    public boolean isPackaged() {
        return this.packaged;
    }

    public GameEnvironment getGameEnv() {
        return this.gameEnv;
    }
}
