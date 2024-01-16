package com.ultreon.craft.android;

import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.Looper;
import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.Mod;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.RestartConfirmScreen;
import com.ultreon.craft.util.Env;
import com.ultreon.xeox.loader.XeoxModFile;
import de.mxapplications.openfiledialog.OpenFileDialog;
import org.mozilla.javascript.Context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import static androidx.core.app.ActivityCompat.startActivityForResult;

public class AndroidPlatform extends GamePlatform {
    public static final int IMPORT_MOD_CODE = 0x00000001;
    private final Map<String, Mod> mods = new IdentityHashMap<>();
    private final AndroidLauncher launcher;

    AndroidPlatform(AndroidLauncher launcher) {
        super();
        this.launcher = launcher;

        this.mods.put(CommonConstants.NAMESPACE, new BuiltinAndroidMod(CommonConstants.NAMESPACE, "Ultracraft", "0.1.0", "The game you are now playing", List.of("Ultreon Team")));
    }

    @Override
    public Collection<? extends Mod> getMods() {
        var list = new ArrayList<Mod>();
        list.addAll(super.getMods());
        list.addAll(this.mods.values());
        return list;
    }

    @Override
    public Optional<Mod> getMod(String id) {
        if (super.getMod(id).isPresent()) {
            return super.getMod(id);
        }

        return Optional.ofNullable(this.mods.get(id));
    }

    @Override
    public boolean isModLoaded(String id) {
        return super.isModLoaded(id) || this.mods.containsKey(id);
    }

    @Override
    public <T> void invokeEntrypoint(String name, Class<T> initClass, Consumer<T> init) {
        // TODO: Implement
    }

    @Override
    public Env getEnv() {
        return Env.CLIENT;
    }

    @Override
    public Path getConfigDir() {
        return Gdx.files.external("config").file().toPath();
    }

    @Override
    public Path getGameDir() {
        return Gdx.files.external(".").file().toPath();
    }

    @Override
    public boolean isMobile() {
        return true;
    }

    @Override
    public boolean openImportDialog() {
        launcher.runOnUiThread(() -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("application/zip");
            startActivityForResult(
                    this.launcher,
                    Intent.createChooser(chooseFile, "Choose mod file"),
                    IMPORT_MOD_CODE,
                    null
            );
        });
        return false;
    }

    public void performImport() {
        // You can use the API that requires the permission.

        // Open android file dialog
        OpenFileDialog openFileDialog = new OpenFileDialog(this.launcher);
        openFileDialog.setFolderSelectable(false);
        openFileDialog.setTitle("Import Mod");
        openFileDialog.setOnCloseListener(new OpenFileDialog.OnCloseListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onOk(String pathName) {
                try {
                    XeoxModFile.importFile(new File(pathName));
                    UltracraftClient.get().showScreen(new RestartConfirmScreen());
                } catch (IOException e) {
                    Log.e("Ultracraft", "Failed to import mod file", e);
                }
            }
        });
        openFileDialog.show();
    }

    @Override
    public void prepare() {
        super.prepare();

        Looper.prepare();
    }

    @Override
    public Context enterXeoxContext() {
        return super.enterXeoxContext();
    }

    @Override
    public boolean isDevEnvironment() {
        return false;
    }

    public void handleSensorChange(SensorEvent event) {
        // TODO: Implement if needed
    }
}
