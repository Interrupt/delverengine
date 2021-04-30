package com.interrupt.dungeoneer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;

public class DesktopStarter {
    public static void main(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.toLowerCase().endsWith("debug=true")) {
                    Game.isDebugMode = true;
                }
                else if (arg.toLowerCase().endsWith("debug-collision=true")) {
                    Game.drawDebugBoxes = true;
                }
                else if (arg.toLowerCase().endsWith("version")){
                    System.out.println(Game.VERSION);
                    System.exit(0);
                }
            }
        }

        // We must call this first to get the correct display options
        Options.loadOptions();

        DisplayMode defaultMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Delver Engine";
        config.fullscreen = Options.instance.fullScreen;
        config.width = defaultMode.width;
        config.height = defaultMode.height;
        config.vSyncEnabled = Options.instance.vsyncEnabled;
        config.samples = Options.instance.antiAliasingSamples;
        config.stencil = 8;
        config.foregroundFPS = Options.instance.fpsLimit;

        if (!config.fullscreen) {
            config.width *= 0.8;
            config.height *= 0.8;
        }

        // More sounds! Libgdx sets these settings low by default
        config.audioDeviceBufferCount *= 2;
        config.audioDeviceSimultaneousSources *= 2;

        config.addIcon("icon-128.png", Files.FileType.Internal); // 128x128 icon (mac OS)
        config.addIcon("icon-32.png", Files.FileType.Internal);  // 32x32 icon (Windows + Linux)
        config.addIcon("icon-16.png", Files.FileType.Internal);  // 16x16 icon (Windows)

        new LwjglApplication(new GameApplication(), config);
    }
}
