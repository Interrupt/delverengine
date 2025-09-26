package com.interrupt.dungeoneer;

import com.interrupt.api.steam.NullSteamApi;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;

public class EditorStarter {
    public static void main(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.toLowerCase().endsWith("version")){
                    System.out.println(Game.VERSION);
                    System.exit(0);
                }
            }
        }

        // We must call this first to get the correct display options
        Options.loadOptions();

        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "DelvEdit");
        Editor.init();

        // Start the null steam API
        SteamApi.api = new NullSteamApi();
        SteamApi.api.init();
    }
}
