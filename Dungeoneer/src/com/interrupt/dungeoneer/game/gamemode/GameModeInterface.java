package com.interrupt.dungeoneer.game.gamemode;

import com.badlogic.gdx.math.Vector2;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

public interface GameModeInterface {
    void tickGame(Level level, float delta, GameInput input);
    void onGameStart(Game game);

    void onWin(Game game);
    void onGameOver(Game game);

    void onMonsterDeath(Actor died);
    void onPlayerDeath(Player player);
    void onPlayerTookDamage(Player player, int damage, Weapon.DamageType damageType, Entity instigator);

    void preLevelInit(Level.Source source, Level level);
    void handleEditorMarker(Level level, EditorMarker marker, Vector2 levelOffset, Vector2 tileOffset);
}
