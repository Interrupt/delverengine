package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class Foliage extends Sprite {

    private enum foliageAttachment { FLOOR, CEILING, CENTER };

	private transient Entity lastColliding = null;
	private transient float collisionCheckTime = 0f;
	private transient float rustleTime = 0f;
	
	public String rustleSound = "rustle/rustle_grass_01.mp3,rustle/rustle_grass_02.mp3,rustle/rustle_grass_03.mp3,rustle/rustle_grass_04.mp3,rustle/rustle_grass_05.mp3,rustle/rustle_grass_06.mp3,rustle/rustle_grass_07.mp3";
	
	public Foliage() { isStatic = true; }

    private Vector2 rotOffset = new Vector2();

	/** Where to place foliage. */
	@EditorProperty
    public foliageAttachment foliagePosition = foliageAttachment.FLOOR;

	/** Ambient sway amount. */
    public float swayAmount = 4.5f;

    @Override
	public void init(Level level, Source source) {
		super.init(level, source);
		collisionCheckTime = Game.rand.nextFloat() * 60f;

		if(shader == null || shader.equals("")) {
			if(foliagePosition == foliageAttachment.CEILING) {
				shader = "grass_hanging";
			}
			else {
				shader = "grass";
			}

			boolean needsReplacement = (spriteAtlas != null || spriteAtlas.equals("sprite") || spriteAtlas.equals("dungeon_sprite"));
			if(needsReplacement) {
				if (tex == 12 || tex == 13) {
					// Mushrooms sway less
					shader = "grass_less";
				}
			}
		}
	}
}
