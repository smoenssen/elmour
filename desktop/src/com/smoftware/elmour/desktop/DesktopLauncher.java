package com.smoftware.elmour.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.smoftware.elmour.ElmourGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		Application app = new LwjglApplication(new ElmourGame(), config);

		Gdx.app = app;
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}
}
