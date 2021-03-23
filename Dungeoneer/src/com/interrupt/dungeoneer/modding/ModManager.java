package com.interrupt.dungeoneer.modding;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.utils.JsonUtil;

public class ModManager {
    /** Array of sources to look for mod content. */
    public Array<ModSource> sources = new Array<>();

    /** Locally available mods, enabled or disabled. */
    public Map<String, Boolean> mods = new HashMap<>();

    private static final String MOD_MANAGER_JSON_FILE_NAME = "mod-manager.json";

    public ModManager() {
        load();
    }

    private void load() {
        sources.add(new LocalFileSystemModSource("mods"));
        // sources.addAll(SteamApi.api.getWorkshopFolders());

        FileHandle modManagerFile = Game.getFile(Options.getOptionsDir() + MOD_MANAGER_JSON_FILE_NAME);

        if (modManagerFile.exists()) {
            ModManager modManager = JsonUtil.fromJson(ModManager.class, modManagerFile);

            sources.addAll(modManager.sources);
        }
    }
}
