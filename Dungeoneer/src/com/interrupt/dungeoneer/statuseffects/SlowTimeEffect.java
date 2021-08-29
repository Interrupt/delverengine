package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class SlowTimeEffect extends StatusEffect {

    /* How much time should move based on the normal during this duration. */
    float timeMod = 0.4f;

    /* How much the player should move based on normal. */
    float playerTimeSpeed = 1.0f;

	public SlowTimeEffect() {
		this(1800);
	}

	public SlowTimeEffect(int time) {
		this.name = StringManager.get("statuseffects.SlowTimeEffect.defaultNameText");
		this.timer = time;
		this.statusEffectType = StatusEffectType.SLOWTIME;
	}

	@Override
	public void doTick(Actor owner, float delta) {
	    // If something reset the time mod back from underneath us, then force it again.
        Game game = Game.instance;
        if(game != null)
            game.SetGameTimeSpeed(timeMod, playerTimeSpeed);
	}

	@Override
	public void onStatusBegin(Actor owner) {
        Game game = Game.instance;
        if(game != null)
            game.SetGameTimeSpeed(timeMod, playerTimeSpeed);
	}

	@Override
	public void onStatusEnd(Actor owner) {
        Game game = Game.instance;
        if(game != null)
            game.SetGameTimeSpeed(1.0f, 1.0f);
	}
}
