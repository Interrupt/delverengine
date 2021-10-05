package com.interrupt.dungeoneer.game.gamemode;

/**
 * GameState objects hold any game-level specific progress that needs to be serialized alongside a GameMode
 */
public interface GameStateInterface {
    void postLoad();
}
