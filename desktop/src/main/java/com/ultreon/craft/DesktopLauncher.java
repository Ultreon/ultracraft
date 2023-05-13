package com.ultreon.craft;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static final int[] SIZES = new int[]{16, 24,  32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};

	public static void main(String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(240);
		config.setTitle("Ultreon Craft");
		config.setWindowIcon(getIcons());
		config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new UltreonCraft(arg), config);
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
