package com.interrupt.dungeoneer;

import com.interrupt.api.steam.NullSteamApi;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.editor.Editor;

public class EditorStarter {
	public static void main(String[] args) {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DelvEdit");
        Editor.init();

        // Start the null steam API
        SteamApi.api = new NullSteamApi();
        SteamApi.api.init();
	}
}
