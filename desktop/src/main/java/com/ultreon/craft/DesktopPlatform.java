package com.ultreon.craft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.desktop.client.gui.screen.ModIconOverrides;
import com.ultreon.craft.desktop.client.gui.screen.ModListScreen;
import com.ultreon.craft.desktop.util.util.ArgParser;
import com.ultreon.craft.desktop.util.util.ImGuiEx;
import com.ultreon.craft.desktop.mods.ClientModInit;
import com.ultreon.craft.desktop.mods.DedicatedServerModInit;
import com.ultreon.craft.desktop.mods.ModInit;
import com.ultreon.craft.platform.OperatingSystem;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.craft.render.gui.screens.Screen;
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
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import static com.ultreon.craft.UltreonCraft.id;

public class DesktopPlatform extends GamePlatform {
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
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
        UltreonCraft.LOGGER.info("Setting up ImGui");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        ImGui.createContext();
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

        UltreonCraft.invokeAndWait(() -> {
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
    public void renderImGui(UltreonCraft game) {
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
                        ImGui.endMenu();
                    }

                    ImGui.text(" Frames Per Second: " + Gdx.graphics.getFramesPerSecond() + "   Frames ID: " + Gdx.graphics.getFrameId());
                    ImGui.endMenuBar();
                }
                ImGui.end();
            }

            if (DesktopPlatform.SHOW_PLAYER_UTILS.get()) this.showPlayerUtilsWindow(game);
            if (DesktopPlatform.SHOW_GUI_UTILS.get()) this.showGuiUtilsWindow(game);
            if (DesktopPlatform.SHOW_UTILS.get()) this.showUtils(game);

            ImGui.render();
            this.imGuiGl3.renderDrawData(ImGui.getDrawData());
        }
    }

    private void showPlayerUtilsWindow(UltreonCraft game) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (game.player != null && ImGui.begin("Player Utils", this.getDefaultFlags())) {
            ImGuiEx.text("Id:", () -> game.player.getId());
            ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", game.player.getWalkingSpeed(), v -> game.player.setWalkingSpeed(v));
            ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", game.player.getFlyingSpeed(), v -> game.player.setFlyingSpeed(v));
            ImGuiEx.editFloat("Gravity:", "PlayerGravity", game.player.gravity, v -> game.player.gravity = v);
            ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", game.player.jumpVel, v -> game.player.jumpVel = v);
            ImGuiEx.editFloat("Health:", "PlayerHealth", game.player.getHealth(), v -> game.player.setHealth(v));
            ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", game.player.getMaxHeath(), v -> game.player.setMaxHeath(v));
            ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", game.player.noGravity, v -> game.player.noGravity = v);
            ImGuiEx.editBool("Flying:", "PlayerFlying", game.player.isFlying(), v -> game.player.setFlying(v));
            ImGuiEx.editBool("Spectating:", "PlayerSpectating", game.player.isSpectating(), v -> game.player.setSpectating(v));
            ImGuiEx.bool("On Ground:", () -> game.player.onGround);
            ImGuiEx.bool("Colliding:", () -> game.player.isColliding);
            ImGuiEx.bool("Colliding X:", () -> game.player.isCollidingX);
            ImGuiEx.bool("Colliding Y:", () -> game.player.isCollidingY);
            ImGuiEx.bool("Colliding Z:", () -> game.player.isCollidingZ);

            if (ImGui.collapsingHeader("Position")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerX", game.player.getX(), v -> game.player.setX(v));
                ImGuiEx.editDouble("Y:", "PlayerY", game.player.getY(), v -> game.player.setY(v));
                ImGuiEx.editDouble("Z:", "PlayerZ", game.player.getZ(), v -> game.player.setZ(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Velocity")) {
                ImGui.treePush();
                ImGuiEx.editDouble("X:", "PlayerVelocityX", game.player.velocityX, v -> game.player.velocityX = v);
                ImGuiEx.editDouble("Y:", "PlayerVelocityY", game.player.velocityY, v -> game.player.velocityY = v);
                ImGuiEx.editDouble("Z:", "PlayerVelocityZ", game.player.velocityZ, v -> game.player.velocityZ = v);
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Rotation")) {
                ImGui.treePush();
                ImGuiEx.editFloat("X:", "PlayerXRot", game.player.getXRot(), v -> game.player.setXRot(v));
                ImGuiEx.editFloat("Y:", "PlayerYRot", game.player.getYRot(), v -> game.player.setYRot(v));
                ImGui.treePop();
            }
            if (ImGui.collapsingHeader("Player Input")) {
                ImGui.treePush();
                ImGuiEx.bool("Forward", () -> game.playerInput.forward);
                ImGuiEx.bool("Backward", () -> game.playerInput.backward);
                ImGuiEx.bool("Left", () -> game.playerInput.strafeLeft);
                ImGuiEx.bool("Right", () -> game.playerInput.strafeRight);
                ImGuiEx.bool("Up", () -> game.playerInput.up);
                ImGuiEx.bool("Down", () -> game.playerInput.down);
                ImGui.treePop();
            }
        }
        ImGui.end();
    }

    private void showGuiUtilsWindow(UltreonCraft game) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("GUI Utils", this.getDefaultFlags())) {
            Screen currentScreen = game.currentScreen;
            ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
            if (currentScreen != null) {
                GuiComponent exactWidgetAt = currentScreen.getExactWidgetAt((int) (Gdx.input.getX() / game.getGuiScale()), (int) (Gdx.input.getY() / game.getGuiScale()));
                if (exactWidgetAt != null) {
                    game.shapes.setColor(1.0F, 0.0F, 1.0F, 1.0F);
                    game.shapes.rectangle(
                            exactWidgetAt.getX() * game.getGuiScale(), exactWidgetAt.getY() * game.getGuiScale(),
                            exactWidgetAt.getWidth() * game.getGuiScale(), exactWidgetAt.getHeight() * game.getGuiScale()
                    );
                }
                ImGuiEx.text("Widget:", () -> exactWidgetAt == null ? null : exactWidgetAt.getClass().getSimpleName());
            }
        }
        ImGui.end();
    }

    private void showUtils(UltreonCraft game) {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Utils", this.getDefaultFlags())) {
            ImGui.button("Respawn");
            if (ImGui.isItemClicked()) {
                game.respawnAsync();
            }
            ImGuiEx.slider("FOV", "GameFOV", (int) game.camera.fieldOfView, 10, 150, i -> game.camera.fieldOfView = i);
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
        UltreonCraft.get().showScreen(new ModListScreen(UltreonCraft.get().currentScreen));
    }

    @Override
    public boolean isModsSupported() {
        return true;
    }

    @Override
    public void setupMods() {
        super.setupMods();

        ModIconOverrides.set("craft", UltreonCraft.id("icon.png"));
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
        crashLog.writeToFile(new File(GamePlatform.data("game-crashes").file(), crashLog.getDefaultFileName()));
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
