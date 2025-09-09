package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.tiles.Tile;

import java.util.Random;

public class MonsterSpawner extends DirectionalEntity {
	public MonsterSpawner() { artType = ArtType.sprite; tex = 0; spriteAtlas = "editor"; tex = 4; hidden = true; }
	
	@EditorProperty(group = "Spawns") public String monsterTheme = "DUNGEON";
	@EditorProperty(group = "Spawns") public String monsterName = "THIEF";
	@EditorProperty(group = "Spawns") boolean waitForTrigger = false;
	@EditorProperty(group = "Spawns") public int monsterLevelBoost = 0;
	@EditorProperty(group = "Spawns") public boolean destroyAfterSpawn = true;
	@EditorProperty(group = "Spawns") public boolean doTeleportEffect = false;
	@EditorProperty(group = "Spawns") public float spawnRadius = 0f;
	@EditorProperty(group = "Spawns") public int spawnAmount = 1;
	@EditorProperty(group = "Spawns") public boolean placeOnFloor = false;

	public int placeAttempts = 10;

	public void spawn(Level level) {
		if(isActive) {
			for (int i = 0; i < spawnAmount; i++) {
				Monster m;
				if (monsterName.equals(""))
					m = Game.instance.monsterManager.GetRandomMonster(monsterTheme);
				else
					m = Game.instance.monsterManager.GetMonster(monsterTheme, monsterName);

				if (m != null) {
					m.xa = xa;
					m.ya = ya;
					m.za = za;

					placeMonster(m, level);

					if (m.isActive) {
						m.Init(level, Game.instance.player.level + monsterLevelBoost);
						level.SpawnEntity(m);
						if (doTeleportEffect) {
							doEffect(new Vector3(m.x, m.y, m.z), level, m);
						}
					}
				}
			}

			if (this.destroyAfterSpawn) {
				isActive = false;
			}
		}
	}

	public void placeMonster(Monster m, Level level) {
		boolean didPlace = false;

		// don't bother with more attempts if we're just spawning here
		if(spawnRadius == 0)
			placeAttempts = 1;

		for(int i = 0; i < placeAttempts && !didPlace; i++) {
			Vector3 rad = new Vector3(0, Game.rand.nextFloat() * spawnRadius, 0);
			rad.rotate(Vector3.Z, Game.rand.nextFloat() * 360f);
			m.x = x + rad.x;
			m.y = y + rad.y;
			m.z = z;

			Tile t = level.getTile((int)m.x, (int)m.y);
			if(!t.blockMotion) {
				float floorHeight = t.getFloorHeight(m.x, m.y) + 0.5f;
				float ceilHeight = t.getCeilHeight(m.x, m.y) + 0.5f;

				// put on the floor?
				if (placeOnFloor || m.z < floorHeight) {
					m.z = floorHeight;
				}

				// pop out of the ceiling
				if (m.z + m.collision.z > ceilHeight) {
					m.z = ceilHeight - m.collision.z;
				}
			}

			boolean levelFree = level.isFree(m.x, m.y, m.z, m.collision, m.stepHeight, m.floating, null);
			boolean entityFree = level.checkEntityCollision(m.x, m.y, m.z, m.collision, m) == null;
			didPlace = levelFree && entityFree;
		}

		if(!didPlace)
			m.isActive = false;
	}

	@Override
	public void tick(Level level, float delta) {
		if(!waitForTrigger) {
			spawn(level);
		}
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		if(waitForTrigger) {
			spawn(Game.GetLevel());
		}
	}

	private void doEffect(Vector3 pos, Level level, Entity owner) {
		Random r = Game.rand;
		int particleCount = 20;
		particleCount *= Options.instance.gfxQuality;
		if(particleCount <= 0) particleCount = 1;

		for(int i = 0; i < particleCount; i++)
		{
			int speed = r.nextInt(45) + 10;
			Particle part = new Particle(pos.x + r.nextFloat() - 0.5f, pos.y + r.nextFloat() - 0.5f, pos.z + r.nextFloat() * 0.9f - 0.45f, 0f, 0f, 0f, 0, Color.ORANGE, true);
			part.floating = true;
			part.playAnimation(8, 12, speed);
			part.checkCollision = false;

			if(owner != null && owner instanceof Player) {
				// Push the particles in the camera direction to be more visible to the player
				part.x += Game.camera.direction.x * 0.5f;
				part.y += Game.camera.direction.z * 0.5f;
				part.z += Game.camera.direction.y * 0.5f - 0.25f;
			}

			level.SpawnNonCollidingEntity(part);
		}

		level.SpawnNonCollidingEntity( new DynamicLight(pos.x,pos.y,pos.z, new Vector3(Color.ORANGE.r * 2f, Color.ORANGE.g * 2f, Color.ORANGE.b * 2f)).startLerp(new Vector3(0,0,0), 40, true).setHaloMode(HaloMode.BOTH) );

		Audio.playPositionedSound("trap_tele.mp3", new Vector3(pos.x, pos.y, pos.z), 0.6f, 12f);
	}
}