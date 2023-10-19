package com.ultreon.craft;

import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.ultreon.craft.desktop.mods.DesktopModPreInit;
import com.ultreon.craft.desktop.util.util.ArgParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
@ApiStatus.NonExtendable
public final class DesktopLauncher {
	public static final int[] SIZES = new int[]{16, 24,  32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};

	public static final Logger LOGGER = LoggerFactory.getLogger("Launcher");
	private static DesktopPlatform platform;

	@ApiStatus.Internal
	public static void main(String[] argv) {
		ArgParser argParser = new ArgParser(argv);

		GamePlatform.instance = DesktopLauncher.platform = new DesktopPlatform(argParser);

        DesktopLauncher.launch(argv);
	}

	private static void launch(String[] argv) {
		EntrypointUtil.invoke(DesktopModPreInit.ENTRYPOINT_KEY, DesktopModPreInit.class, DesktopLauncher::setupDesktopMods);
        DesktopLauncher.logDebug();

		new Lwjgl3Application(new GameLibGDXWrapper(argv), DesktopLauncher.createConfig());
	}

	@NotNull
	private static Lwjgl3ApplicationConfiguration createConfig() {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.useVsync(true);
		config.setForegroundFPS(120);
		config.setOpenGLEmulation(GLEmulation.GL30, 3, 0);
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
				UltreonCraft.get().pause();
			}

            @Override
			public boolean closeRequested() {
				return UltreonCraft.get().closeRequested();
			}

			@Override
			public void filesDropped(String[] files) {
				UltreonCraft.get().filesDropped(files);
			}

        });
		return config;
	}

	private static void logDebug() {
		if (DesktopLauncher.platform.isPackaged()) DesktopLauncher.LOGGER.debug("Running in the JPackage environment.");
		else DesktopLauncher.LOGGER.debug("Local directory: " + System.getProperty("user.dir"));
		DesktopLauncher.LOGGER.debug("Java Version: " + System.getProperty("java.version"));
		DesktopLauncher.LOGGER.debug("Java Vendor: " + System.getProperty("java.vendor"));;
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
