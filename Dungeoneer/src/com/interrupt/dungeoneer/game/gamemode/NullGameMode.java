package com.interrupt.dungeoneer.game.gamemode;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.screens.LevelChangeScreen;

/**
 * Game mode that does nothing but the very basics.
 */
public class NullGameMode implements GameModeInterface {
    protected GameStateInterface gameState = new NullGameState();

    @Override
    public void tickGame(Level level, float delta, GameInput input) {

    }

    @Override
    public void onGameStart(Game game) {

    }

    @Override
    public void onWin(Game game) {
        GameApplication.ShowMainMenuScreen();
    }

    @Override
    public void onGameOver(Game game) {
        GameApplication.ShowMainMenuScreen();
    }

    @Override
    public void onPlayerDeath(Player player) {

    }

    @Override
    public void onMonsterDeath(Actor died) {

    }

    @Override
    public void onMonsterTookDamage(Actor monster, int damage, Weapon.DamageType damageType, Entity instigator) {

    }

    @Override
    public void onPlayerTookDamage(Player player, int damage, Weapon.DamageType damageType, Entity instigator) {
        if(damage < 0)
            Game.flash(Colors.HEAL_FLASH, 20);
        else
            Game.flash(Colors.HURT_FLASH, 20);
    }

    @Override
    public LevelChangeScreen getLevelChangeScreen() {
        return new LevelChangeScreen();
    }

    @Override
    public void preLevelInit(Level.Source source, Level level) {

    }

    @Override
    public void handleEditorMarker(Level level, EditorMarker marker, Vector2 levelOffset, Vector2 tileOffset) {

    }

    @Override
    public void loadGameState(int saveSlot) {

    }

    @Override
    public void saveGameState(int saveSlot) {

    }

    @Override
    public Array<Level> getGameLevelLayout() {
        // Use the level layout in data/dungeons.dat
        return Game.loadDataLevels();
    }
}
