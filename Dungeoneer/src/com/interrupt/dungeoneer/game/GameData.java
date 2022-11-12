package com.interrupt.dungeoneer.game;

import com.interrupt.dungeoneer.game.gamemode.GameModeInterface;
import com.interrupt.dungeoneer.game.pathfinding.PathfindingInterface;

public class GameData {
    public GameData() { }

    /** The Game Mode to be used for the game */
    public GameModeInterface gameMode;

    /** The Pathfinding Interface to use for the game */
    public PathfindingInterface pathfindingMode;

    /** Tutorial level. Shown when starting a new save slot. */
    public Level tutorialLevel = null;

    /** End level. Shown when the game is beaten. */
    public Level endingLevel = null;

    /** Filepaths of entity data. */
    public String[] entityDataFiles = {"entities.dat"};

    /** Filepaths of monster data. */
    public String[] monsterDataFiles = {"monsters.dat"};

    /** Filepaths of item data. */
    public String[] itemDataFiles = {"items.dat"};

    /** Filepath of player data. */
    public String playerDataFile = "player.dat";

    /** Filepath of hud data. */
    public String hudDataFile = "hud.dat";

    /** Is player allowed to jump? */
    public boolean playerJumpEnabled = false;

    /** Whether the mod including this should override the packaged game data.
     * Setting this to true makes the mod basically the Delver version of an iWad in Doom. */
    public boolean overrideBaseGame = false;

    public void merge(GameData modData) {
        tutorialLevel = modData.tutorialLevel;
        endingLevel = modData.endingLevel;

        GameData defaultGameData = new GameData();

        // Merge some properties only if different!
        if(modData.gameMode != null && modData.gameMode != defaultGameData.gameMode) {
            gameMode = modData.gameMode;
        }
        if(modData.pathfindingMode != null && modData.pathfindingMode != defaultGameData.pathfindingMode) {
            pathfindingMode = modData.pathfindingMode;
        }
        if(modData.playerJumpEnabled != defaultGameData.playerJumpEnabled) {
            playerJumpEnabled = modData.playerJumpEnabled;
        }
        if(!arraysAreEqual(modData.entityDataFiles, defaultGameData.entityDataFiles)) {
            entityDataFiles = modData.entityDataFiles;
        }
        if(!arraysAreEqual(modData.monsterDataFiles, defaultGameData.monsterDataFiles)) {
            monsterDataFiles = modData.monsterDataFiles;
        }
        if(!arraysAreEqual(modData.itemDataFiles, defaultGameData.itemDataFiles)) {
            itemDataFiles = modData.itemDataFiles;
        }
    }

    private boolean arraysAreEqual(String[] first, String[] second) {
        return joinStringArray(first).equals(joinStringArray(second));
    }

    private String joinStringArray(String[] toJoin) {
        if(toJoin == null)
            return "";

        if(toJoin.length == 0)
            return "";

        StringBuilder s = new StringBuilder();
        for(int i = 0; i < toJoin.length; i++) {
            s.append(toJoin[i]);
            if(i < toJoin.length - 1) {
                s.append(',');
            }
        }
        return s.toString();
    }
}
