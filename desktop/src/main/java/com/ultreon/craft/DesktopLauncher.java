package com.ultreon.craft;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.ultreon.craft.client.GameLibGDXWrapper;
import com.ultreon.craft.client.GamePlatform;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.desktop.mods.DesktopModPreInit;
import com.ultreon.craft.desktop.util.util.ArgParser;
import com.ultreon.libs.crash.v0.CrashLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;

import javax.swing.*;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
@ApiStatus.NonExtendable
public final class DesktopLauncher {
	public static final int[] SIZES = new int[]{16, 24,  32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};

	public static final Logger LOGGER = LogManager.getLogger("Launcher");
	private static DesktopPlatform platform;

	@ApiStatus.Internal
	public static void main(String[] argv) {
		try {
			ArgParser argParser = new ArgParser(argv);

			GamePlatform.instance = DesktopLauncher.platform = new DesktopPlatform(argParser);

			try {
				DesktopLauncher.launch(argv);
			} catch (Error e) {
                DesktopLauncher.platform.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
			}
		} catch (Throwable t) {
			try {
				DesktopLauncher.LOGGER.error("Launch failed!", t);
				UltracraftClient.crash(t);
			} catch (Throwable throwable) {
				try {
					DesktopLauncher.LOGGER.fatal("Fatal error occurred when trying to launch the game!", throwable);
				} catch (Throwable ignored) {
					Runtime.getRuntime().halt(2);
				}
			}
		}
	}

	private static void launch(String[] argv) {
		EntrypointUtil.invoke(DesktopModPreInit.ENTRYPOINT_KEY, DesktopModPreInit.class, DesktopLauncher::setupDesktopMods);
        DesktopLauncher.logDebug();

		FlatMacLightLaf.setup();

		// Before initializing LibGDX or creating a window:
		try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> DesktopLauncher.LOGGER.error("GLFW Error: " + description))) {
			try {
				new Lwjgl3Application(new GameLibGDXWrapper(argv), DesktopLauncher.createConfig());
			} catch (Throwable t) {
				DesktopLauncher.LOGGER.fatal("Failed to create LWJGL3 Application:", t);
				JOptionPane.showMessageDialog(null, t.getMessage() + "\n\nCheck the debug.log file for more info!", "Launch failed!", JOptionPane.ERROR_MESSAGE);
				Runtime.getRuntime().halt(1);
			}
		}
	}

	@NotNull
	private static Lwjgl3ApplicationConfiguration createConfig() {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true);
		config.setForegroundFPS(120);
		config.setIdleFPS(10);
		config.setBackBufferConfig(8, 8, 8, 8, 8, 0, 0);
		config.setInitialVisible(false);
		config.setTitle("Ultracraft");
		config.setWindowIcon(DesktopLauncher.getIcons());
		config.setWindowedMode(1280, 720);
		config.setWindowListener(new Lwjgl3WindowAdapter() {
			@Override
			public void created(Lwjgl3Window window) {
				Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
				Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
				Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
				Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);
			}

			@Override
			public void focusLost() {
				UltracraftClient.get().pause();
			}

            @Override
			public boolean closeRequested() {
				return UltracraftClient.get().tryShutdown();
			}

			@Override
			public void filesDropped(String[] files) {
				UltracraftClient.get().filesDropped(files);
			}

        });
		return config;
	}

	private static void logDebug() {
		if (DesktopLauncher.platform.isPackaged()) DesktopLauncher.LOGGER.debug("Running in the JPackage environment.");
		else DesktopLauncher.LOGGER.debug("Local directory: " + System.getProperty("user.dir"));
		DesktopLauncher.LOGGER.debug("Java Version: " + System.getProperty("java.version"));
		DesktopLauncher.LOGGER.debug("Java Vendor: " + System.getProperty("java.vendor"));
        DesktopLauncher.LOGGER.debug("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
	}

	private static String[] getIcons() {
		String[] icons = new String[DesktopLauncher.SIZES.length];
		for (int i = 0, sizesLength = DesktopLauncher.SIZES.length; i < sizesLength; i++) {
			var size = DesktopLauncher.SIZES[i];
			icons[i] = "icons/icon_" + size + ".png";
		}

		return icons;
	}

	/**
	 * Check whether the application is packaged using JPackage.
	 * @return true if in the JPackage environment.
	 */
	public static boolean isPackaged() {
		return DesktopLauncher.platform.isPackaged();
	}

	private static void setupDesktopMods(DesktopModPreInit desktopModPreInit) {
		desktopModPreInit.onInitializeDesktop(DesktopLauncher.platform);
	}
}
