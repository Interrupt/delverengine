package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Editor {
    public static EditorApplication app;
    public static EditorOptions options;
    public static EditorActions actions;

	public Editor() {
        JFrame frame = new JFrame("DelvEdit");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                super.windowClosing(e);
            }
        });

        Graphics.DisplayMode defaultMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "New Level - DelvEdit";
        config.fullscreen = false;
        config.width = defaultMode.width;
        config.height = defaultMode.height;
        config.vSyncEnabled = true;
        config.foregroundFPS = 120;
        config.backgroundFPS = 30;
        config.stencil = 8;

        config.addIcon("icon-128.png", Files.FileType.Internal); // 128x128 icon (mac OS)
        config.addIcon("icon-32.png", Files.FileType.Internal);  // 32x32 icon (Windows + Linux)
        config.addIcon("icon-16.png", Files.FileType.Internal);  // 16x16 icon (Windows)
		
		Editor.app = new EditorApplication(frame);
		new LwjglApplication(Editor.app, config);

		Editor.options = EditorOptions.fromLocalFiles();
		Editor.actions = new EditorActions();
	}

    public void dispose() {
	    EditorOptions.toLocalFiles(options);
    }
}
