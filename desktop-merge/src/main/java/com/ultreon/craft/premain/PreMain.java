package com.ultreon.craft.premain;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.google.common.collect.Lists;
import com.ultreon.craft.premain.win32.CLibrary;
import com.ultreon.craft.premain.win32.Kernel32;
import com.ultreon.gameprovider.craft.UltracraftGameprovider;
import org.jetbrains.annotations.NotNull;
import net.fabricmc.loader.impl.launch.knot.KnotClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PreMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> argv = Lists.newArrayList(args);
        System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
        if (argv.remove("--packaged")) {
            PreMain.setDirectory();
        }
        KnotClient.main(args);
    }

    private static void setDirectory() {
        @NotNull Path launchPath = UltracraftGameprovider.getDataDir();
        if (SharedLibraryLoader.isWindows) {
            Kernel32.INSTANCE.SetCurrentDirectoryW(launchPath.toString().toCharArray());
        } else if (SharedLibraryLoader.isLinux || SharedLibraryLoader.isMac) {
            CLibrary.INSTANCE.chdir(launchPath.toString());
        }
    }
}
