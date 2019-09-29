package com.smoftware.elmour.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.smoftware.elmour.main.ElmourGame;
import com.smoftware.elmour.sfx.WaterExperiment;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

		// Galaxy S9
		//cfg.width = 512;
		//cfg.height = 266;

		// fullscreen
		//cfg.fullscreen = true;
		// vSync
		cfg.vSyncEnabled = true;
		//cfg.vSyncEnabled = false; // Setting to false disables vertical sync
		//cfg.foregroundFPS = 40; // Setting to 0 disables foreground fps throttling
		//cfg.backgroundFPS = 0; // Setting to 0 disables background fps throttling

		Application app = new LwjglApplication(new ElmourGame(), cfg);

		//Application app = new LwjglApplication(new WaterExperiment(), cfg);

		Gdx.app = app;
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
