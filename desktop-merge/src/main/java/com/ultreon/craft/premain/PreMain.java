package com.ultreon.craft.premain;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.google.common.collect.Lists;
import com.ultreon.gameprovider.craft.UltracraftGameprovider;
import org.quiltmc.loader.impl.launch.knot.KnotClient;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PreMain {
    public static void main(String[] args) throws IOException {
        List<String> argv = Lists.newArrayList(args);
        if (argv.remove("--packaged")) {
            PreMain.relaunch(argv);
        }
        KnotClient.main(args);
    }

    private static void relaunch(List<String> argv) throws IOException {
        if (SharedLibraryLoader.isWindows) {
            argv.add(0, "Ultracraft.exe");
            Runtime.getRuntime().exec(argv.toArray(new String[]{}), new String[]{}, UltracraftGameprovider.getDataDir().toFile());
            System.exit(0);
        }
    }
}
