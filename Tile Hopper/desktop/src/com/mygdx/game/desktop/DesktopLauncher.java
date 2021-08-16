package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Tile Hopper";
		config.width = 800;
		config.height = 480;
		config.foregroundFPS = 200;
		config.resizable = false;
		new LwjglApplication(new MyGame(), config);
	}
}
