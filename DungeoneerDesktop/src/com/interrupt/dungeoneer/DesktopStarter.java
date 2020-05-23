package com.interrupt.dungeoneer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.ModManager;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.modding.ScriptLoader;

public class DesktopStarter {
	public static void main(String[] args) {

		Options.loadOptions();
		
		DisplayMode defaultMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Delver Engine");

		/*config.fullscreen = Options.instance.fullScreen;
		config.width = defaultMode.width;
		config.height = defaultMode.height;
		config.vSyncEnabled = Options.instance.vsyncEnabled;
		config.samples = Options.instance.antiAliasingSamples;
		config.stencil = 8;
		config.foregroundFPS = Options.instance.fpsLimit;*/

        config.useVsync(false);
        config.setWindowedMode(800, 600);

		/*if(!config.fullscreen) {
			config.width *= 0.8;
			config.height *= 0.8;
		}*/

		// More sounds! Libgdx sets these settings low by default
		//config.audioDeviceBufferCount *= 2;
		//config.audioDeviceSimultaneousSources *= 2;

		config.setAudioConfig(16, 512, 9);

		config.setWindowIcon(Files.FileType.Internal, "icon-128.png", "icon-32.png", "icon-16.png");
		
		if(args != null) {
			for(String arg : args) {
				if(arg.toLowerCase().endsWith("debug=true")) {
					Game.isDebugMode = true;
				}
				else if(arg.toLowerCase().endsWith("debug-collision=true")) {
					Game.drawDebugBoxes = true;
				}
				else if(arg.toLowerCase().endsWith("enable-mod-classes=true")) {
					ModManager.setScriptingApi(new ScriptLoader());
				}
			}
		}

		new Lwjgl3Application(new GameApplication(), config);
	}
}
