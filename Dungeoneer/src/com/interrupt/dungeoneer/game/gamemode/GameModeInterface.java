package com.interrupt.dungeoneer.game.gamemode;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.screens.LevelChangeScreen;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

/**
 * A GameModeInterface is a container for gameplay logic.
 */
public interface GameModeInterface {
    // Base game ticking and starting
    void tickGame(Level level, float delta, GameInput input);
    void onGameStart(Game game);

    // Win / Lose states
    void onWin(Game game);
    void onGameOver(Game game);

    // Logic for Player and Monster deaths
    void onMonsterDeath(Actor monster);
    void onMonsterTookDamage(Actor monster, int damage, Weapon.DamageType damageType, Entity instigator);
    void onPlayerDeath(Player player);
    void onPlayerTookDamage(Player player, int damage, Weapon.DamageType damageType, Entity instigator);

    // Creates the Screen to show when switching levels
    // Overridable as some GameModes might want to show end of level stats, etc...
    LevelChangeScreen getLevelChangeScreen();

    // Handle level loading and entity spawning
    void preLevelInit(Level.Source source, Level level);
    void handleEditorMarker(Level level, EditorMarker marker, Vector2 levelOffset, Vector2 tileOffset);

    // Handle serializing the game state, which is for holding overall game progression
    void loadGameState(int saveSlot);
    void saveGameState(int saveSlot);

    // Return the list of level definitions for the game.
    // This list defines the structure of the game. Level 1, Level 2, etc...
    Array<Level> getGameLevelLayout();
}
