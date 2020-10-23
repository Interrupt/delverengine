package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.managers.EntityManager;

public class Torch extends Light {

	float ticks = 0;
	float switchtime = 10;

	/** Starting animation frame sprite index. */
	@EditorProperty
	public int texAnimStart = 4;

	/** Ending animation frame sprite index. */
	@EditorProperty
	public int texAnimEnd = 5;

	public enum TorchAnimateModes { RANDOM, LOOP }

	/** Torch animation mode. */
	@EditorProperty
	public TorchAnimateModes torchAnimateMode = TorchAnimateModes.RANDOM;

	/** Animation speed. */
	@EditorProperty
	public float animSpeed = 30f;

	protected SpriteAnimation animation = null;

	public ParticleEmitter emitter = null;
	
	public Torch() { spriteAtlas = "sprite"; hidden = false; collision.set(0.05f,0.05f,0.2f); fullbrite = true; haloMode = HaloMode.BOTH; haloOffset = 0.8f; }

	/** Looping ambient sound. */
	@EditorProperty
	public String audio = "torch.mp3";

	/** Make flies? */
	@EditorProperty
    public boolean makeFlies = false;

	/** Make ParticleEmitter? */
	@EditorProperty
	public boolean makeEmitter = true;
	
	public Torch(float x, float y, int tex, Color lightColor) {
		super(x, y, lightColor, 3.2f);
		artType = ArtType.sprite;
		collision.set(0.05f,0.05f,0.2f);
		this.tex = texAnimStart;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		if(torchAnimateMode == TorchAnimateModes.RANDOM) {
			ticks += delta;
			if (switchtime < ticks) {
				switchtime = ticks + 5;
				if (Game.rand.nextInt(10) < 5)
					tex = texAnimEnd;
				else {
					tex = texAnimStart;
				}
			}
		}
		else {
			if(animation != null && animation.playing) {
				animation.animate(delta, this);
				if(animation.done) isActive = false;	// die when done playing an animation
			}
		}
		
		if(emitter != null) {
			emitter.tick(level, delta);
		}
		
		color = lightColor;
	}
	
	@Override
	public void init(Level level, Source source) {
		this.tex = texAnimStart;
		
		super.init(level, source);
		collision.set(0.05f,0.05f,0.2f);

        if(source != Source.EDITOR) {
            if(emitter != null && makeEmitter) {
            	if(source == Source.LEVEL_START) {
					emitter.x += x;
					emitter.y += y;
					emitter.z += z + 0.7f;
				}
                emitter.persists = false;
                emitter.detailLevel = DetailLevel.HIGH;
                emitter.init(level, source);
                level.non_collidable_entities.add(emitter);
            }

            if(makeFlies) {
                Entity flies = EntityManager.instance.getEntity("Groups", "Flies");
                if (flies != null) {
                    flies.detailLevel = DetailLevel.HIGH;
                    if(source == Source.LEVEL_START) {
						flies.x = (int) x + 0.5f;
						flies.y = (int) y + 0.5f;
						flies.z = z + 0.225f;
					}
                    flies.persists = false;
                    level.non_collidable_entities.add(flies);
                }
            }

            if(audio != null) {
				AmbientSound sound = new AmbientSound(x, y, z, audio, 1f, 1f, 3.5f);
				sound.persists = false;
				level.non_collidable_entities.add(sound);
			}
        }

        if(torchAnimateMode == TorchAnimateModes.LOOP && animation == null) {
			this.playAnimation(new SpriteAnimation(texAnimStart, texAnimEnd, animSpeed, null), true, true);
		}
	}

	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);
		if(animation != null) animation.animate(delta, this);
	}

	public void editorStartPreview(Level level) { if(torchAnimateMode == TorchAnimateModes.LOOP) this.playAnimation(new SpriteAnimation(texAnimStart, texAnimEnd, animSpeed, null), true, true); }
	public void editorStopPreview(Level level) {
		if(animation != null) {
			tex = animation.start;
			stopAnimation();
			animation = null;
		}
	}

	// Start an animation on this sprite
	public void playAnimation(SpriteAnimation animation, boolean looping) {
		this.animation = animation;
		if(looping) this.animation.loop();
		else this.animation.play();
	}

	public void playAnimation(SpriteAnimation animation, boolean looping, boolean randomize) {
		playAnimation(animation, looping);

		if(randomize && this.animation != null) {
			animation.randomizeTime();
		}
	}

	public void stopAnimation() {
		animation = null;
	}
}
