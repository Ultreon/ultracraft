package com.ultreon.craft.premain;

import net.fabricmc.loader.impl.launch.knot.KnotClient;

public class DevMain {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        System.setProperty("fabric.development", "true");
        KnotClient.main(args);
    }
}
