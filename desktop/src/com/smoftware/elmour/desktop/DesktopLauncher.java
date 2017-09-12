package com.smoftware.elmour.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.smoftware.elmour.ElmourGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

		//cfg.width = 1280;
		//cfg.height = 720;

		// fullscreen
		//cfg.fullscreen = true;
		// vSync
		//cfg.vSyncEnabled = true;

		Application app = new LwjglApplication(new ElmourGame(), cfg);

		Gdx.app = app;
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
