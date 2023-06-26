package com.ultreon.craft;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.GLEmulation;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.ultreon.craft.util.ArgParser;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static final int[] SIZES = new int[]{16, 24,  32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};

	public static void main(String[] argv) {
		ArgParser argParser = new ArgParser(argv);
		GamePlatform.instance = new DesktopPlatform(argParser);

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(0);
		config.setIdleFPS(10);
		config.setOpenGLEmulation(GLEmulation.GL30, 3, 2);
		config.setInitialVisible(false);
//		config.setDecorated(false);
		config.setTitle("Ultreon Craft");
		config.setWindowIcon(getIcons());
		config.setWindowedMode(1280, 720);
		config.setWindowListener(new Lwjgl3WindowListener() {
			@Override
			public void created(Lwjgl3Window window) {

			}

			@Override
			public void iconified(boolean isIconified) {

			}

			@Override
			public void maximized(boolean isMaximized) {

			}

			@Override
			public void focusLost() {
				UltreonCraft.get().pause();
			}

			@Override
			public void focusGained() {

			}

			@Override
			public boolean closeRequested() {
				return UltreonCraft.get().closeRequested();
			}

			@Override
			public void filesDropped(String[] files) {
				UltreonCraft.get().filesDropped(files);
			}

			@Override
			public void refreshRequested() {

			}
		});
		new Lwjgl3Application(new UltreonCraft(argv), config);
	}

	private static String[] getIcons() {
		String[] icons = new String[SIZES.length];
		for (int i = 0, sizesLength = SIZES.length; i < sizesLength; i++) {
			var size = SIZES[i];
			icons[i] = "icons/icon_" + size + ".png";
		}

		return icons;
	}
}
