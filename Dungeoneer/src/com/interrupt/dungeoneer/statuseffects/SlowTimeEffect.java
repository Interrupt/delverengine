package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.helpers.InterpolationHelper;
import com.interrupt.managers.StringManager;

public class SlowTimeEffect extends StatusEffect {

    /* How much time should move based on the normal during this duration. */
    float worldTimeMod = 0.4f;

    /* How much the player should move based on normal. */
    float playerTimeMod = 1.0f;

    /* How much the effect should be eased into */
    public float effectLerpScale = 5.0f;

    private float initialTimer = 0.0f;

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
        if(game == null)
            return;

        float lerpAlpha = ((initialTimer - timer) / initialTimer);
        float inOutLength = 1.0f - (1.0f / effectLerpScale);

        // Lerp the effect in and out. Only need to worry about easing out if we clamp output at 1.0
        if(inOutLength > 0) {
            if (lerpAlpha >= inOutLength) {
                lerpAlpha = 1.0f - lerpAlpha;
            }
            lerpAlpha *= effectLerpScale;
        }

        // Clamp at the top end and bottom ends to avoid things going screwy.
        lerpAlpha = Math.min(lerpAlpha, 1.0f);
        lerpAlpha = Math.max(lerpAlpha, 0.0f);

        float timeModLerp = InterpolationHelper.getInterpolator(InterpolationHelper.InterpolationMode.circle).apply(1.0f, worldTimeMod, lerpAlpha);
        float playerTimeModLerp = InterpolationHelper.getInterpolator(InterpolationHelper.InterpolationMode.circle).apply(1.0f, playerTimeMod, lerpAlpha);

        // Time is subjective. Need to handle if we have mucked with time, or another actor.
        if(owner instanceof Player) {
            // For us, we can slow down the game time
            game.SetGameTimeSpeed(timeModLerp, playerTimeModLerp);
        } else {
            // Not the player, make this Actor move really fast instead
            speedMod = 1.0f / worldTimeMod;
        }
	}

	@Override
	public void onStatusBegin(Actor owner) {
	    // Keep track of how long this status effect will last
        initialTimer = timer;
	}

	@Override
	public void onStatusEnd(Actor owner) {
        Game game = Game.instance;
        if(game == null)
            return;

        // Reset time back to normal
        game.SetGameTimeSpeed(1.0f, 1.0f);
	}
}
