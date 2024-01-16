package com.ultreon.craft.android;

import android.app.Activity;
import android.content.Intent;
import android.hardware.*;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.client.GameLibGDXWrapper;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.ModImportFailedScreen;
import com.ultreon.craft.client.gui.screens.RestartConfirmScreen;
import com.ultreon.xeox.loader.XeoxLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.UUID;

public class AndroidLauncher extends AndroidApplication implements SensorEventListener {
    private AndroidPlatform androidPlatform;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;
        config.maxSimultaneousSounds = 256;
        config.useGyroscope = true;
        config.useCompass = true;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        androidPlatform = new AndroidPlatform(this);

        initialize(new GameLibGDXWrapper(new String[]{"--android"}), config);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AndroidPlatform.IMPORT_MOD_CODE && resultCode == Activity.RESULT_OK) {
            try {
                Uri contentDescriber = data.getData();
                File source = new File(contentDescriber.getPath());
                CommonConstants.LOGGER.debug("src is {}", source);

                FileHandle external = Gdx.files.external("temp");
                File tempFile = external.child(source.getName() + "_" + UUID.randomUUID() + ".tmp").file();
                tempFile.deleteOnExit();
                copy(source, tempFile);
                XeoxLoader.get().importMod(tempFile);
                tempFile.delete();
                UltracraftClient.get().showScreen(new RestartConfirmScreen());
            } catch (Exception e) {
                Log.e("Ultracraft", "Failed to import mod file", e);
                UltracraftClient.get().showScreen(new ModImportFailedScreen());
            }
        }
    }

    private void copy(File source, File destination) throws IOException {
        try (FileChannel in = new FileInputStream(source).getChannel();
             FileChannel out = new FileOutputStream(destination).getChannel()) {

            try {
                in.transferTo(0, in.size(), out);
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }
        }
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        androidPlatform.handleSensorChange(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
