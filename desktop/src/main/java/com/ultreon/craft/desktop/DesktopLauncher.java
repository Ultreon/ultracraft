package com.ultreon.craft.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.ultreon.craft.CrashHandler;
import com.ultreon.craft.client.GameLibGDXWrapper;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.util.ArgParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class DesktopLauncher {
    private static DesktopPlatform platform;

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        try {
            CrashHandler.addHandler(crashLog -> {
                try {
                    DesktopInput.setCursorCaught(false);
                    Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
                    graphics.getWindow().setVisible(false);
                } catch (Exception e) {
                    UltracraftClient.LOGGER.error("Failed to hide cursor", e);
                }
            });

            DesktopLauncher.launch(argv);
        } catch (Exception | OutOfMemoryError e) {
            CrashHandler.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
        }
    }

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    private static void launch(String[] argv) {
        FlatMacLightLaf.setup();

        platform = new DesktopPlatform();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> UltracraftClient.LOGGER.error("GLFW Error: {}", description))) {
            try {
                new Lwjgl3Application(new GameLibGDXWrapper(argv), DesktopLauncher.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                UltracraftClient.crash(crashLog);
            } catch (Exception e) {
                UltracraftClient.crash(e);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setIdleFPS(10);
        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 0);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1);
        config.setInitialVisible(false);
        config.setTitle("Ultracraft");
        config.setWindowIcon(UltracraftClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        return config;
    }

    public static DesktopPlatform getPlatform() {
        return platform;
    }

    private static class WindowAdapter extends Lwjgl3WindowAdapter {
        private Lwjgl3Window window;

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(window);
            this.window = window;
        }

        @Override
        public void focusLost() {
            UltracraftClient.get().pause();

            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, false);
        }

        @Override
        public void focusGained() {
            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, true);
        }

        @Override
        public boolean closeRequested() {
            return UltracraftClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            UltracraftClient.get().filesDropped(files);

            WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(this.window, files);
        }

    }
}
