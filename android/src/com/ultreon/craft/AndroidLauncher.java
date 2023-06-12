package com.ultreon.craft;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// use the full display, even if we have a device with a notch
		Window applicationWindow = this.getApplicationWindow();
		WindowManager.LayoutParams attrib = applicationWindow.getAttributes();
		attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

		GamePlatform.instance = new AndroidPlatform();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		this.initialize(new UltreonCraft(new String[]{}), config);
	}
}
