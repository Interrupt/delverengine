package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
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

	protected float hasMoved = 0;
	protected float waitTime = 0;
	protected float deltaBuffer = 0;
	protected ElevatorState state = ElevatorState.NONE;

	public TriggeredElevator() { hidden = true; spriteAtlas = "editor"; tex = 11; }

	boolean elevatorMoving = false;
	
	@Override
	public void doTriggerEvent(String value) {
		if(state == ElevatorState.NONE) {
			waitTime = 0;
			state = ElevatorState.MOVING;
		} else if(state == ElevatorState.WAITING) {
			if (waitTime > endWaitTime) {
				waitTime = 0;
				state = ElevatorState.RETURNING;
			}
		}

		super.doTriggerEvent(value);
	}

	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);

		float moving = 0;

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
				}
			} else if (state == ElevatorState.RETURNING) {
				moving = -moveSpeed * deltaBuffer * 0.1f;
				if(moveAmount < 0) moving *= -1;

				hasMoved += moving;

				if (moveAmount > 0 && hasMoved < 0 || moveAmount < 0 && hasMoved > 0) {
					moving += 0 - hasMoved;
					hasMoved = 0;
					state = ElevatorState.NONE;
				}
			} else if (state == ElevatorState.WAITING) {
				waitTime += delta;

				if(returnType == ReturnType.AUTO) {
					if (waitTime > endWaitTime) {
						waitTime = 0;
						state = ElevatorState.RETURNING;
					}
				}
			}

			deltaBuffer = 0;
		}

		if(moving != 0) {
			for(int tileX = (int)x - (int)collision.x; tileX < x + collision.x; tileX++) {
				for(int tileY = (int)y - (int)collision.y; tileY < y + collision.y; tileY++) {
					Tile t = level.getTileOrNull(tileX, tileY);

					if(t != null) {
						if (elevatorType == ElevatorType.FLOOR || elevatorType == ElevatorType.BOTH) {
							t.floorHeight += moving;
						}
						if (elevatorType == ElevatorType.CEILING || elevatorType == ElevatorType.BOTH) {
							t.ceilHeight += moving;
						}
						if (elevatorType == ElevatorType.OPPOSITE) {
							t.floorHeight += moving;
							t.ceilHeight -= moving;
						}

						// Make this tile totally solid when fully closed
						t.blockMotion = t.getMinOpenHeight() <= 0.0f;

						markWorldAsDirty(tileX, tileY);
						markWorldAsDirty(tileX + 1, tileY);
						markWorldAsDirty(tileX - 1, tileY);
						markWorldAsDirty(tileX, tileY + 1);
						markWorldAsDirty(tileX, tileY - 1);
					}
				}
			}
		}
	}

	private void markWorldAsDirty(int xPos, int yPos) {
		WorldChunk chunk = GameManager.renderer.GetWorldChunkAt(xPos, yPos);
		if(chunk != null) {
			chunk.needsRetessellation = true;
		}
	}
}
