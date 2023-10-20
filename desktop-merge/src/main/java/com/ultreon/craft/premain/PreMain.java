package com.ultreon.craft.premain;

import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.google.common.collect.Lists;
import com.ultreon.craft.premain.win32.CLibrary;
import com.ultreon.craft.premain.win32.Kernel32;
import com.ultreon.gameprovider.craft.UltracraftGameprovider;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.launch.knot.KnotClient;

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

    private static void relaunch(List<String> argv) throws IOException, InterruptedException {
        @NotNull Path launchPath = UltracraftGameprovider.getDataDir();
        Files.createDirectories(launchPath);

        if (SharedLibraryLoader.isWindows) {
            File executable;
            if (new File(".").getCanonicalFile().getAbsoluteFile().getName().equals("bin")) {
                executable = new File("Ultracraft.exe");
            } else {
                executable = new File("bin/Ultracraft.exe");
            }

            if (!executable.exists())
                throw new IOException("Can't relaunch application since the executable doesn't exists: " + executable.getAbsolutePath());

            argv.add(0, executable.getAbsolutePath());
            System.out.println("Relaunching with command line: " + argv);
            System.exit(new ProcessBuilder(argv).directory(launchPath.toFile()).inheritIO().start().waitFor());
        }
        if (SharedLibraryLoader.isLinux) {
            File executable;
            if (new File(".").getCanonicalFile().getAbsoluteFile().getName().equals("bin")) {
                executable = new File("Ultracraft");
            } else {
                executable = new File("bin/Ultracraft");
            }

            if (!executable.exists())
                throw new IOException("Can't relaunch application since the executable doesn't exists: " + executable.getAbsolutePath());

            argv.add(0, executable.getAbsolutePath());
            System.out.println("Relaunching with command line: " + argv);
            System.exit(new ProcessBuilder(argv).directory(launchPath.toFile()).inheritIO().start().waitFor());
        }
    }
}
