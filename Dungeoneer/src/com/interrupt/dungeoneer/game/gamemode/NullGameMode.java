package com.interrupt.dungeoneer.game.gamemode;

import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

/**
 * Game mode that does nothing but the very basics.
 */
public class NullGameMode implements GameModeInterface {
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
    public void onPlayerTookDamage(Player player, int damage, Weapon.DamageType damageType, Entity instigator) {
        if(damage < 0)
            Game.flash(Colors.HEAL_FLASH, 20);
        else
            Game.flash(Colors.HURT_FLASH, 20);
    }
}
