package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;

import java.util.UUID;

public class Group extends DirectionalEntity {
	/** Array of grouped Entities. */
	public Array<Entity> entities = new Array<Entity>();
	
	protected Vector3 lastRot = new Vector3();
	protected Vector3 lastPosition = new Vector3();
	
	public transient Vector3 rotTemp = new Vector3();
	public transient Vector3 posTemp = new Vector3();

    public boolean placed = false;
	
	public Group() { }
	
	@Override
	public void tick(Level level, float delta) {
		for(Entity e : entities) {
			e.tick(level, delta);
		}
	}

	@Override
	public void init(Level level, Source source) {

		if(!isActive)
			return;

        setPosition(x, y, z);

		if(source != Source.EDITOR) {
			if(!lastRot.equals(rotation)) {
				float remainder = (rotation.z % 90) - rotation.z;
				int rotate90Mod = (int) (remainder / 90) % 4;
				if (rotate90Mod < 0) rotate90Mod = 4 + rotate90Mod;
				float rotOffset = (rotation.z % 90);

				Vector3 tempRot = new Vector3(rotation);

				lastRot.set(rotation);
				setRotation(rotation.x, rotation.y, -rotation.z);
				setRotation(tempRot.x, tempRot.y, rotOffset);
				if (rotate90Mod > 0) for (int i = 0; i < rotate90Mod; i++) {
					rotate90();
				}
			}

			for(Entity e : entities)  {
                if(!e.checkDetailLevel() || (e.spawnChance < 1f && Game.rand.nextFloat() > e.spawnChance)) continue;

				if(e.isDynamic) {
					if (this.xa != 0){
						e.xa += this.xa;
					}
					if (this.ya != 0) {
						e.ya += this.ya;
					}
					if (this.za != 0) {
						e.za += this.za;
					}

					level.entities.add(e);
				}
				else {
					level.static_entities.add(e);
				}

                e.init(level, source);

				// Bug fix to make sure doors get oriented properly
				if(e instanceof Door) {
					Door d = (Door)e;
					d.placeFromPrefab(level);
				}
			}

            makeIdsUnique();

            lastRot.set(rotation);
			isActive = false;
		}
		else {
			for (Entity e : entities) {
				e.init(level, source);
			}
		}
	}

    void makeIdsUnique() {
        String uuid = UUID.randomUUID().toString();
        for(Entity e : entities) {
        	Level.makeUniqueEntityId(uuid, e);
        }
    }

    @Override
	public void updateDrawable() {
		setPosition(x,y,z);
		setRotation(rotation.x, rotation.y, rotation.z);
		
		for(Entity e : entities) {
			e.editorState = editorState;
			e.setPosition(e.x, e.y, e.z);
			
			e.updateDrawable();
		}
	}

	public void updateCollision() {
		for (Entity e : entities) {
			if (e instanceof Group) {
				Group g = (Group)e;
				((Group) e).updateCollision();
			}
			else if (!e.isSolid) {
				continue;
			}

			if (!this.isSolid) {
				this.collision.set(0, 0, 0);
			}

			float maxX = e.x + e.collision.x - this.x;
			float minX = e.x - e.collision.x - this.x;
			float maxY = e.y + e.collision.y - this.y;
			float minY = e.y - e.collision.y - this.y;
			float maxZ = e.z + e.collision.z - this.z;
			float minZ = e.z - e.collision.z - this.z;

			this.collision.x = Math.max(Math.max(Math.abs(maxX), Math.abs(minX)), this.collision.x);
			this.collision.y = Math.max(Math.max(Math.abs(maxY), Math.abs(minY)), this.collision.y);
			this.collision.z = Math.max(Math.max(Math.abs(maxZ), Math.abs(minZ)), this.collision.z);
		}
	}
	
	@Override
	public void setRotation(float rotX, float rotY, float rotZ) {
		
		rotation.set(rotX, rotY, rotZ);
		
		lastRot.sub(rotation);
		
		if(lastRot.x != 0 || lastRot.y != 0 || lastRot.z != 0) {
			for(Entity e : entities) {
				rotTemp.set(e.x - x, e.y - y, e.z - z);
				rotTemp.rotate(Vector3.X, lastRot.x);
				rotTemp.rotate(Vector3.Y, lastRot.y);
				rotTemp.rotate(Vector3.Z, lastRot.z);
				
				e.x = rotTemp.x + x;
				e.y = rotTemp.y + y;
				e.z = rotTemp.z + z;

                e.rotate(lastRot.x, lastRot.y, -lastRot.z);
			}
		}
		
		lastRot.set(rotation);
	}
	
	@Override
	public void rotate90() {
		if(isActive) {
            if(!placed) setPosition(x, y, z);

			rotation.z -= 90;
            lastRot.sub(rotation);

            for(Entity e : entities) {
                rotTemp.set(e.x - x, e.y - y, e.z - z);
                rotTemp.rotate(Vector3.X, lastRot.x);
                rotTemp.rotate(Vector3.Y, lastRot.y);
                rotTemp.rotate(Vector3.Z, lastRot.z);

                e.x = rotTemp.x + x;
                e.y = rotTemp.y + y;
                e.z = rotTemp.z + z;
            }

			for(Entity entity : entities) {	
				entity.rotate90();
			}

            lastRot.set(rotation);
		}
	}

	@Override
	public void rotate90Reversed() {
		if(isActive) {
			if(!placed) setPosition(x, y, z);

			rotation.z += 90;
			lastRot.sub(rotation);

			for(Entity e : entities) {
				rotTemp.set(e.x - x, e.y - y, e.z - z);
				rotTemp.rotate(Vector3.X, lastRot.x);
				rotTemp.rotate(Vector3.Y, lastRot.y);
				rotTemp.rotate(Vector3.Z, lastRot.z);

				e.x = rotTemp.x + x;
				e.y = rotTemp.y + y;
				e.z = rotTemp.z + z;
			}

			for(Entity entity : entities) {
				entity.rotate90Reversed();
			}

			lastRot.set(rotation);
		}
	}

	/*public void rotate90() {
		if(isActive) {
			if(!placed) setPosition(x, y, z);

			rotation.z += 90;
			lastRot.add(rotation);

			for(Entity e : entities) {
				rotTemp.set(e.x - x, e.y - y, e.z - z);
				rotTemp.rotate(Vector3.X, lastRot.x);
				rotTemp.rotate(Vector3.Y, lastRot.y);
				rotTemp.rotate(Vector3.Z, lastRot.z);

				e.x = rotTemp.x + x;
				e.y = rotTemp.y + y;
				e.z = rotTemp.z + z;
			}

			for(Entity entity : entities) {
				entity.rotate90();
			}

			lastRot.set(rotation);
		}
	}*/

	@Override
	public void setPosition(float x, float y, float z) {
		posTemp.set(lastPosition);
		posTemp.sub(x,y,z);
		
		for(Entity entity : entities) {
			entity.setPosition(entity.x - posTemp.x, entity.y - posTemp.y, entity.z - posTemp.z);
		}
		
		lastPosition.set(x,y,z);
		
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void updateLight(Level level) {
		for(Entity e : entities) {
			e.updateLight(level);
		}
	}
}
