package com.interrupt.dungeoneer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
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
                else if (arg.toLowerCase().endsWith("skip-intro=true")) {
                    Game.skipIntro = true;
                }
                else if (arg.toLowerCase().endsWith("version")){
                    System.out.println(Game.VERSION);
                    System.exit(0);
                }
            }
        }

        // We must call this first to get the correct display options
        Options.loadOptions();

        DisplayMode defaultMode = Lwjgl3ApplicationConfiguration.getDisplayMode();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Delver Engine");
        config.setWindowedMode((int)(defaultMode.width * 0.8f), (int)(defaultMode.height * 0.8f));

        if (Options.instance.fullScreen) {
            config.setFullscreenMode(defaultMode);
        }

        config.useVsync(Options.instance.vsyncEnabled);
        config.setBackBufferConfig(8,8,8,8,16,8,Options.instance.antiAliasingSamples);
        config.setForegroundFPS(Options.instance.fpsLimit);

        // More sounds! Libgdx sets these settings low by default
        config.setAudioConfig(32, 512, 18);

        // Enable OpenGL Emulation over ANGLE for better cross platform support
        if (Options.instance.renderingEngine == Options.RenderingEngine.ANGLE) {
            config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);
        }

        config.setWindowIcon(Files.FileType.Internal, "icon-128.png"); // 128x128 icon (mac OS)
        config.setWindowIcon(Files.FileType.Internal, "icon-32.png");  // 32x32 icon (Windows + Linux)
        config.setWindowIcon(Files.FileType.Internal, "icon-16.png");  // 16x16 icon (Windows)

        new Lwjgl3Application(new GameApplication(), config);
    }
}
