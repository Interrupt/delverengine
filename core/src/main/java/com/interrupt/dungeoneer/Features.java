package com.interrupt.dungeoneer;

import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.GameData;
import com.interrupt.utils.JsonUtil;

public class Features {
    public static boolean playerJumpEnabled() {
        GameData gameData = JsonUtil.fromJson(GameData.class, Game.findInternalFileInMods("data/game.dat"));
        if (gameData == null) return false;

        return gameData.playerJumpEnabled;
    }
}
