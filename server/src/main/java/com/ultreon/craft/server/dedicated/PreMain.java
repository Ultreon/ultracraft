package com.ultreon.craft.server.dedicated;

import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Pre-main class for Ultracraft.
 * <p style="color: red;">NOTE: Internal API!</p>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@ApiStatus.Internal
public final class PreMain {
    /**
     * Production main method.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param args Arguments to pass to the game.
     */
    @ApiStatus.Internal
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        System.setProperty("fabric.skipMcProvider", "true");
        KnotClient.main(args);
    }
}
