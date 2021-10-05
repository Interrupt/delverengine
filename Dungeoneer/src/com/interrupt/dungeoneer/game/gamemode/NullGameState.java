package com.interrupt.dungeoneer.game.gamemode;

/**
 * Game state that does nothing
 */
public class NullGameState implements GameStateInterface {
    public NullGameState() {
        // Needs a default constructor to be serializable
    }

    @Override
    public void postLoad() {

    }
}
