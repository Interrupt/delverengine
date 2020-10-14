package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.entities.AmbientSound;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.PathNode;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.tiles.Tile;

public class TriggeredElevator extends Trigger {

	public enum ElevatorType { FLOOR, CEILING, BOTH, OPPOSITE }
	public enum ElevatorState { NONE, MOVING, WAITING, RETURNING }
	public enum ReturnType { AUTO, WAITS }

	@EditorProperty
	public float moveSpeed = 0.1f;

	@EditorProperty
	public float moveAmount = 1.0f;

	@EditorProperty
	public float endWaitTime = 1.0f;

	@EditorProperty
	public ElevatorType elevatorType = ElevatorType.FLOOR;

	@EditorProperty
	public ReturnType returnType = ReturnType.AUTO;

	@EditorProperty
	public int squishDamage = 1;

	@EditorProperty
	public float squishMaxReverseTime = 60;

	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String startSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String endSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String returnStartSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String returnEndSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String movingSound = null;

	protected float hasMoved = 0;
	protected float waitTime = 0;
	protected float deltaBuffer = 0;
	protected ElevatorState state = ElevatorState.NONE;

	protected float amountFloorMovedSinceStart = 0;

	public TriggeredElevator() { hidden = true; spriteAtlas = "editor"; tex = 11; selfDestructs = false; }

	private transient Array<Entity> entitiesToMoveCache = new Array<>();

	private transient float squishDamageTimer = 0;
	private transient float squishTotalTimer = 0;
	private transient Entity squishing = null;

	private transient AmbientSound movingAmbientSound = null;
	
	@Override
	public void doTriggerEvent(String value) {
		if(state == ElevatorState.NONE) {
			waitTime = 0;
			state = ElevatorState.MOVING;

			if(startSound != null)
				Audio.playPositionedSound(startSound, new Vector3(x,y,z), 0.7f, 14f);

		} else if(state == ElevatorState.WAITING) {
			if (waitTime > endWaitTime) {
				waitTime = 0;
				state = ElevatorState.RETURNING;

				if(returnStartSound != null)
					Audio.playPositionedSound(returnStartSound, new Vector3(x,y,z), 0.7f, 14f);
			}
		}

		super.doTriggerEvent(value);
	}

	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);

		// Do squish damage when there is something squishy
		if(squishing != null) {
			squishDamageTimer += delta;

			if(squishDamageTimer > 20 && squishDamage > 0 )
				squishing.hit(0, 0, squishDamage, 0, Weapon.DamageType.PHYSICAL, this);

			squishTotalTimer += delta;

			if(squishMaxReverseTime >= 0 && squishTotalTimer > squishMaxReverseTime) {
				if(state == ElevatorState.MOVING)
					state = ElevatorState.RETURNING;
				else if(state == ElevatorState.RETURNING)
					state = ElevatorState.MOVING;

				squishTotalTimer = 0;
				squishDamageTimer = 0;
			}

			if(squishDamageTimer > 20) squishDamageTimer = 0;
		} else {
			squishTotalTimer = 0;
		}

		// Handle moving sound
		if(movingSound != null && !movingSound.equals("")) {
			if(state == ElevatorState.MOVING || state == ElevatorState.RETURNING) {
				if(movingAmbientSound == null) movingAmbientSound = new AmbientSound(x,y,z,movingSound,1f,1f,13f);
				movingAmbientSound.setPosition(x, y, z);
				movingAmbientSound.tick(level, delta);
			}
			else {
				if(movingAmbientSound != null) {
					movingAmbientSound.volume = 0;
					movingAmbientSound.stop();
					movingAmbientSound = null;
				}
			}
		}

		// Handle moving
		float moving = 0;
		float hasMovedAtStart = hasMoved;

		// Put a cap on how often the level updates
		deltaBuffer += delta;

		if(deltaBuffer >= 1f) {
			if (state == ElevatorState.MOVING) {
				moving = moveSpeed * deltaBuffer * 0.1f;
				if(moveAmount < 0) moving *= -1;

				hasMoved += moving;

				if (Math.abs(hasMoved) > Math.abs(moveAmount)) {
					moving -= hasMoved - moveAmount;
					hasMoved = moveAmount;

					state = ElevatorState.WAITING;

					if(endSound != null)
						Audio.playPositionedSound(endSound, new Vector3(x,y,z), 0.7f, 14f);
				}
			} else if (state == ElevatorState.RETURNING) {
				moving = -moveSpeed * deltaBuffer * 0.1f;
				if(moveAmount < 0) moving *= -1;

				hasMoved += moving;

				if (moveAmount > 0 && hasMoved < 0 || moveAmount < 0 && hasMoved > 0) {
					moving += 0 - hasMoved;
					hasMoved = 0;
					state = ElevatorState.NONE;

					if(returnEndSound != null)
						Audio.playPositionedSound(returnEndSound, new Vector3(x,y,z), 0.7f, 14f);
				}
			} else if (state == ElevatorState.WAITING) {
				waitTime += delta;

				if(returnType == ReturnType.AUTO) {
					if (waitTime > endWaitTime) {
						waitTime = 0;
						state = ElevatorState.RETURNING;

						if(returnStartSound != null)
							Audio.playPositionedSound(returnStartSound, new Vector3(x,y,z), 0.7f, 14f);
					}
				}
			}

			deltaBuffer = 0;
		}

		if(moving != 0) {
			// Figure out the bounds and center of this elevator
			int minX = (int)Math.floor(x - collision.x);
			int maxX = (int)Math.ceil(x + collision.x);
			int minY = (int)Math.floor(y - collision.y);
			int maxY = (int)Math.ceil(y + collision.y);

			float xSize = (maxX - minX) / 2f;
			float ySize = (maxY - minY) / 2f;

			// Find any entities that are nearby the elevator and could be colliding
			Array<Entity> entitiesHere = level.getEntitiesEncroaching2d(minX + xSize, minY + ySize, xSize, ySize, this);

			// Move tiles, to see where they will end up
			adjustWorldTiles(level, moving);

			// How much will the floor move?
			float floorMoveAmount = 0f;
			if(elevatorType != ElevatorType.CEILING) {
				floorMoveAmount = moving;
			}

			// Reset squish entity
			squishing = null;

			// Make sure the elevator is not blocked from moving by dynamic objects, and keep track of the ones on top of us
			boolean canMove = true;
			for(int i = 0; i < entitiesHere.size && canMove; i++) {
				Entity e = entitiesHere.get(i);
				if(e.isDynamic) {
					Collision hitLoc = new Collision();

					// Check ceiling collision
					level.isFree(e.x, e.y, e.z, e.collision, 0.001f, e.floating, hitLoc);
					if(hitLoc.colType == Collision.CollisionType.ceiling) {
						canMove = false;
						squishing = e;
						continue;
					}

					// Move entities if the floor is moving
					if(floorMoveAmount != 0f) {
						boolean isFloorFree = level.isFree(e.x, e.y, e.z + floorMoveAmount, e.collision, 0.001f, e.floating, hitLoc);
						if (isFloorFree || hitLoc.colType == Collision.CollisionType.floor) {
							// Make sure we are touching the floor inside the bounds of the elevator
							if (floorMoveAmount > 0 && (hitLoc.colPos.x > minX && hitLoc.colPos.x < maxX && hitLoc.colPos.y > minY && hitLoc.colPos.y < maxY)) {
								entitiesToMoveCache.add(e);
							} else if(floorMoveAmount < 0 && (isFloorFree || (hitLoc.colPos.x > minX && hitLoc.colPos.x < maxX && hitLoc.colPos.y > minY && hitLoc.colPos.y < maxY))) {
								entitiesToMoveCache.add(e);
							}
						} else if(hitLoc.colType == Collision.CollisionType.ceiling) {
							canMove = false;
							squishing = e;
						}
					}
				}
			}

			// Put the elevator back if we were blocked
			if(!canMove) {
				// Can't actually move, so put things back.
				adjustWorldTiles(level, -moving);
				hasMoved = hasMovedAtStart;
				return;
			}

			// We could move, so update the entities standing on us
			for(int i = 0; i < entitiesToMoveCache.size; i++) {
				Entity e = entitiesToMoveCache.get(i);
				if(moving > -0.1f)
					e.z += moving;
				else
					e.za += moving * 0.01f;

				e.physicsSleeping = false;
			}

			// Move the trigger as much as the floor, so that a touch could trigger it again
			z += floorMoveAmount;
			amountFloorMovedSinceStart += floorMoveAmount;

			// Done with the cache
			entitiesToMoveCache.clear();

			// The world changed here, retesselate
			markWorldAsDirty(level);
		}
	}

	private void adjustWorldTiles(Level level, float thisMoveAmount) {
		int minX = (int)Math.floor(x - collision.x);
		int maxX = (int)Math.ceil(x + collision.x);
		int minY = (int)Math.floor(y - collision.y);
		int maxY = (int)Math.ceil(y + collision.y);

		for(int tileX = minX; tileX < maxX; tileX++) {
			for(int tileY = minY; tileY < maxY; tileY++) {
				Tile t = level.getTileOrNull(tileX, tileY);

				if(t != null && !t.IsSolid()) {
					if (elevatorType == ElevatorType.FLOOR || elevatorType == ElevatorType.BOTH) {
						t.floorHeight += thisMoveAmount;
						t.offsetBottomWallSurfaces(thisMoveAmount);
					}
					if (elevatorType == ElevatorType.CEILING || elevatorType == ElevatorType.BOTH) {
						t.ceilHeight += thisMoveAmount;
						t.offsetTopWallSurfaces(thisMoveAmount);
					}
					if (elevatorType == ElevatorType.OPPOSITE) {
						t.floorHeight += thisMoveAmount;
						t.ceilHeight -= thisMoveAmount;
						t.offsetTopWallSurfaces(-thisMoveAmount);
						t.offsetBottomWallSurfaces(thisMoveAmount);
					}

					// Make this tile totally solid when fully closed
					t.blockMotion = t.getMinOpenHeight() <= 0.0f;
				}
			}
		}
	}

	private void markWorldAsDirty(Level level) {
		// Mark this area as dirty to force world Tesselation here
		int minX = (int)Math.floor(x - collision.x);
		int maxX = (int)Math.ceil(x + collision.x);
		int minY = (int)Math.floor(y - collision.y);
		int maxY = (int)Math.ceil(y + collision.y);

		for(int tileX = minX; tileX < maxX; tileX++) {
			for (int tileY = minY; tileY < maxY; tileY++) {
				markWorldAsDirty(tileX, tileY);
				markWorldAsDirty(tileX + 1, tileY);
				markWorldAsDirty(tileX - 1, tileY);
				markWorldAsDirty(tileX, tileY + 1);
				markWorldAsDirty(tileX, tileY - 1);

				// Stop pathfinding to this tile if it has moved too much
				Tile t = level.getTileOrNull(tileX, tileY);
				if(t == null)
					continue;

				PathNode n = Game.pathfinding.GetNodeAt(tileX + 0.5f, tileY + 0.5f, t.floorHeight);
				if(n == null)
					continue;

				n.setEnabled(!t.blockMotion && Math.abs(amountFloorMovedSinceStart) < 0.5f);
			}
		}
	}

	private void markWorldAsDirty(int xPos, int yPos) {
		WorldChunk chunk = GameManager.renderer.GetWorldChunkAt(xPos, yPos);
		if(chunk != null) {
			chunk.needsRetessellation = true;
		}
	}

	public void onDispose() {
		super.onDispose();
		if(movingAmbientSound != null) movingAmbientSound.onDispose();
	}
}
