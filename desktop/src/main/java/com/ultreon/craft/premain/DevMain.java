package com.ultreon.craft.premain;

import net.fabricmc.loader.impl.launch.knot.KnotClient;
import org.jetbrains.annotations.ApiStatus;

/**
 * Development main class.
 * <p style="color: red;">NOTE: Internal API!</p>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@ApiStatus.Internal
public final class DevMain {
    /**
     * Development main method.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param args Arguments passed to the game.
     */
    @ApiStatus.Internal
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        System.setProperty("fabric.development", "true");
        KnotClient.main(args);
    }
}
