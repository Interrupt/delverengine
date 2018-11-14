package com.interrupt.dungeoneer.entities;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;

public class Npc extends Entity {
	public Npc() { artType = ArtType.entity; shadowType = ShadowType.BLOB; collision.set(0.2f, 0.2f, 0.5f); }
	private SpriteAnimation baseAnimation = null;
	private SpriteAnimation[] idleAnimations = null;
	private SpriteAnimation playing = null;

	private SpriteAnimation triggeredAnimation = null;
	
	private int animationsBetweenIdles = 2;
	private int idleCounter = 0;

	@EditorProperty( group = "End Game" )
	public boolean appearsDuringEndgame = true;

	@Override
	public void init(Level level, Level.Source source) {
		if(Game.instance != null && Game.instance.player != null) {
			// Might need to despawn this during the endgame
			if(Game.instance.player.isHoldingOrb) {
				if(!appearsDuringEndgame) {
					isActive = false;
				}
			}
		}
	}
	
	@Override
	public void tick(Level level, float delta) {
		
		if(playing != null && playing.done) playing = null;
		
		if(playing == null) {
			// sometimes, play an idle animation
			if(idleAnimations != null && idleAnimations.length > 0 && Game.rand.nextFloat() < 0.8 && idleCounter > animationsBetweenIdles) {
				// pick an idle animation to play
				playing = idleAnimations[Game.rand.nextInt(idleAnimations.length)];
				idleCounter = 0;
			}
			else if(baseAnimation != null) playing = baseAnimation;
			
			if(playing != null) {
				playing.play();
				playing.done = false;
				idleCounter++;
			}
		}
		
		if(playing != null) playing.animate(delta, this);
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		if(triggeredAnimation != null && playing != triggeredAnimation) {
			playing = triggeredAnimation;
			playing.play();
			playing.done = false;
		}
	}
}
