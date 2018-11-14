package com.interrupt.dungeoneer.game;

public class GameData {
    public GameData() { }
    public Level tutorialLevel = null;
    public Level endingLevel = null;
    public String[] entityDataFiles = {"entities.dat"};
    public String[] monsterDataFiles = {"monsters.dat"};
    public String[] itemDataFiles = {"items.dat"};
    public String playerDataFile = "player.dat";
    public boolean playerJumpEnabled = false;

    public void merge(GameData modData) {
        tutorialLevel = modData.tutorialLevel;
        endingLevel = modData.endingLevel;

        GameData defaultGameData = new GameData();

        // Merge some properties only if different!
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
