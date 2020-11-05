package com.interrupt.dungeoneer.entities;

import java.text.MessageFormat;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Sword;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.StringManager;

public class Door extends Entity {
	public enum DoorState {CLOSED, OPENING, OPEN, CLOSING, STUCK}
    public enum DoorOpenType {SLIDE, SLIDE_UP, ROTATE, ROTATE_UP}
    public enum DoorDirection {NORTH, SOUTH, EAST, WEST}
    public enum DoorType {NORMAL, TRAPDOOR}

    /** Door mesh filepath. */
    @EditorProperty(type = "FILE_PICKER", params = "meshes")
    public String doorMesh = "meshes/door_0.obj";

    /** Door texture filepath. */
    @EditorProperty(type = "FILE_PICKER")
    public String doorTexture = "door.png";
	
	@EditorProperty
	private float speed = 60;
	private float animateSpeed = 60;
	
	private transient Interpolation animateInterpolation = Interpolation.exp5;
	
	private float animateTime = speed;
	private float lastAnimateTime = animateTime;

	/** Current DoorState. */
	@EditorProperty
	public DoorState doorState = DoorState.CLOSED;

	/** How does door open. */
	@EditorProperty
	public DoorOpenType doorOpenType = DoorOpenType.ROTATE;

	/** Door's cardinal direction. */
	@EditorProperty
	public DoorDirection doorDirection = DoorDirection.NORTH;

	/** Door's type. */
	@EditorProperty
	public DoorType doorType = DoorType.NORMAL;

	/** Flip door open direction? */
	@EditorProperty
	public boolean doorInvertOpen = false;

	/** Is door locked? */
	@EditorProperty
	public boolean isLocked = false;

	/** Can door be unlocked with a key? */
	@EditorProperty
	public boolean takesKey = false;

	/** Does door stay open? */
	@EditorProperty
    public boolean getsStuckOpen = false;

	/** Id to trigger on open/close. */
	@EditorProperty
	public String triggersId = "";

	/** Filepath of sound to play on door open. */
	@EditorProperty
	public String openSound = "wood-door-open.mp3";

	/** Filepath of sound to play while door closing. */
	@EditorProperty
	public String closingSound = "wood-door-close.mp3";

	/** Filepath of sound to play when door done closing. */
	@EditorProperty
	public String closedSound = "wood-door-close-click.mp3";

	/** Can door be destroyed? */
	@EditorProperty
	public boolean breakable = true;

	/** Door maximum hit points. */
	@EditorProperty
	public int hp = 5;

	/** Door gib range starting sprite index. */
	@EditorProperty
	public int gibSpriteTexStart = 40;

	/** Door gib range ending sprite index. */
	@EditorProperty
	public int gibSpriteTexEnd = 42;

	/** Number of gibs to create when destroyed. */
	@EditorProperty
	public int gibNum = 14;

	/** Chance door is stuck closed. */
	@EditorProperty
	public float stuckChance = 0;

	/** Filepath of sound to play when door is destroyed. */
	@EditorProperty
	public String breakSound = null;

	public enum DoorTriggerMode {OPEN_CLOSE, LOCK_UNLOCK};

	/** Which action triggers door event. */
	@EditorProperty
	public DoorTriggerMode triggerMode = DoorTriggerMode.OPEN_CLOSE;

	/** Amount to shake when hit/stuck. */
	public float shakeAmount = 4f;

	public float shakeTimer = 0f;

	/** Random velocity range to apply to gibs. */
	public Vector3 gibVelocity = new Vector3(0.02f,0.02f,0.04f);

	/** Door collision origin. */
	public Vector3 startLoc = null;

	private float rot = 0;
	
	private transient float rotateAnimSolidPoint = 0.8f;

    public transient String lastMeshFile = null;
	
	public Vector3 dir = new Vector3(Vector3.Z);
	
	public Door() { artType = ArtType.texture; isSolid = true; this.dir = Vector3.Z; this.isActive = true; stepHeight = 0f; isDynamic = true; }

	public Vector3 tempDir = new Vector3();
	public Vector3 offset = new Vector3();

	public Door(float x, float y, int tex)
	{
		super(x, y, tex, false);
		isSolid = true;
		
		artType = ArtType.texture;
		this.dir = Vector3.Z;
		
		DrawableSprite doorDrawable = new DrawableSprite(tex,artType);
		doorDrawable.billboard = false;
		drawable = doorDrawable;
		
		isDynamic = true;
	}
	
	public Door(Door toCopy) {
		collision.set(toCopy.collision);
		speed = toCopy.speed;
		animateTime = speed;
		tex = toCopy.tex;
		artType = toCopy.artType;
		doorOpenType = toCopy.doorOpenType;
		doorDirection = toCopy.doorDirection;
		doorType = toCopy.doorType;
		doorInvertOpen = toCopy.doorInvertOpen;
		isLocked = toCopy.isLocked;
		takesKey = toCopy.takesKey;
		getsStuckOpen = toCopy.getsStuckOpen;
		triggersId = toCopy.triggersId;
		openSound = toCopy.openSound;
		closingSound = toCopy.closingSound;
		closedSound = toCopy.closedSound;
		isSolid = toCopy.isSolid;
		rot = toCopy.rot;
		drawable = toCopy.drawable;
		doorMesh = toCopy.doorMesh;
		doorTexture = toCopy.doorTexture;
		breakable = toCopy.breakable;
		breakSound = toCopy.breakSound;
		hp = toCopy.hp;
		gibNum = toCopy.gibNum;
		gibSpriteTexStart = toCopy.gibSpriteTexStart;
		gibSpriteTexEnd = toCopy.gibSpriteTexEnd;
		stuckChance = toCopy.stuckChance;
		breakSound = toCopy.breakSound;
		isActive = true;
		isDynamic = true;
		
		if(stuckChance > 0) {
			if(Game.rand.nextFloat() * 100.0f < stuckChance) doorState = DoorState.STUCK;
		}
	}
	
	public void use(Player p, float projx, float projy)
	{	
		if((doorState==DoorState.OPEN)||(doorState==DoorState.OPENING)) {
            if (!getsStuckOpen){
                doClose(true);
            } else {
                Game.ShowMessage(StringManager.get("entities.Door.stuckText"), 3, 1f);
            }
        } 
		else if (doorState == DoorState.STUCK && breakable) {
			if(Game.rand.nextFloat() > 0.975f) {
				doOpen(true);
			}
			else {
				shakeTimer = 20f;
				Game.ShowMessage(StringManager.get("entities.Door.stuckText"), 3, 1f);
			}
		}
		else {
            if(!isLocked){
                    doOpen(true);
            } else {
                if (takesKey){
                    if(p.keys > 0) {
                        p.keys--;
                        isLocked=false;
                        Game.ShowMessage(StringManager.get("entities.Door.unlockedText"), 3, 1f);
                        doOpen(true);
                    } else {    
                        Game.ShowMessage(StringManager.get("entities.Door.lockedText"), 3, 1f);
                    }
                } else {    
                    Game.ShowMessage(StringManager.get("entities.Door.opensElsewhereText"), 3, 1f);
                }
            }
        }
	}
	
	public void doOpen(boolean fireTrigger){
        isLocked=false;
        
        if(hasRoomToOpen()) {
        	
        	animateSpeed = speed;
            animateInterpolation = Interpolation.exp5;
            
        	doorState=DoorState.OPENING;
        	if(fireTrigger) Game.instance.level.trigger(this, triggersId, "open");
        	Audio.playPositionedSound(openSound, new Vector3(x, y, z), 0.4f, 10f);
        }
    }

	public void doClose(boolean fireTrigger){
    	if(hasRoomToClose()) {
    		
            animateInterpolation = Interpolation.exp5;
            animateSpeed = speed;
            
    		doorState=DoorState.CLOSING;
    		if(fireTrigger) Game.instance.level.trigger(this, triggersId, "close");
    		Audio.playPositionedSound(closingSound, new Vector3(x, y, z), 0.3f, 10f);
    	}
    }
	
	private boolean hasRoomToOpen() {
		if(doorType == DoorType.TRAPDOOR && doorOpenType == DoorOpenType.ROTATE) {
			Entity encroaching = Game.instance.level.checkEntityCollision(x, y, z, collision, this);
			return encroaching == null;
		}
		
		return true;
	}
	
	private boolean hasRoomToClose() {
		if(doorType == DoorType.TRAPDOOR && doorOpenType == DoorOpenType.ROTATE) {
			Entity encroaching = Game.instance.level.checkEntityCollision(startLoc.x, startLoc.y, startLoc.z, collision, this);
			return encroaching == null;
		}
		
		return true;
	}

	@Override
	public void tick(Level level, float delta)
	{		
		isDynamic = false;
		slideEffectTimer -= delta * 0.5f;
		
		if(startLoc == null) {
			startLoc = new Vector3(x, y, z);
			animateTime = speed;
		}
		
		lastAnimateTime = animateTime;
		
		if(doorState == DoorState.OPENING) {
			animateTime -= delta;
			if(animateTime <= 0) {
				doorState = DoorState.OPEN;
				animateTime = 0;
			}
		}
		else if(doorState == DoorState.CLOSING) {
			animateTime += delta;
			if(animateTime > speed) {
				doorState = DoorState.CLOSED;
				animateTime = speed;
				Audio.playPositionedSound(closedSound, new Vector3(x, y, z), 0.5f, 10f);
			}
		}
		
		if(doorOpenType == DoorOpenType.ROTATE) {
			isSolid = doorState == DoorState.CLOSED || doorState == DoorState.STUCK; 
		}
		
		// animate!
		if(animateTime != lastAnimateTime) {
			float animUnit = animateInterpolation.apply(1 - animateTime / animateSpeed);
			float nextX = startLoc.x;
			float nextY = startLoc.y;
			float nextZ = startLoc.z;
			
			if(doorOpenType == DoorOpenType.ROTATE && doorState == DoorState.OPENING) isSolid = animUnit < rotateAnimSolidPoint;
			
			float openMod = doorInvertOpen ? -1 : 1;
			rot = 0;
			
			if(doorOpenType == DoorOpenType.SLIDE) {				
				if(doorDirection == DoorDirection.EAST) {
					nextX = startLoc.x + (animUnit * 0.85f) * openMod;
				}
				else if(doorDirection == DoorDirection.WEST) {
					nextX = startLoc.x - (animUnit * 0.85f) * openMod;
				}
				else if(doorDirection == DoorDirection.NORTH) {
					nextY = startLoc.y + (animUnit * 0.85f) * openMod;
				}
				else if(doorDirection == DoorDirection.SOUTH) {
					nextY = startLoc.y - (animUnit * 0.85f) * openMod;
				}
			}
			else if(doorOpenType == DoorOpenType.SLIDE_UP) {
				nextZ = startLoc.z + (animUnit * 0.8f) * openMod;
			}
			else if(doorOpenType == DoorOpenType.ROTATE) {
			
				rot = animUnit * 86 * openMod;
				
				if(doorType == DoorType.NORMAL) {
					if(doorDirection == DoorDirection.EAST || doorDirection == DoorDirection.WEST) {
						if(doorDirection == DoorDirection.WEST) openMod *= -1f;
						nextX = startLoc.x + (rot / 98f) * 0.5f * openMod;
						if(doorDirection == DoorDirection.EAST && doorInvertOpen) openMod *= -1f;
						nextY = startLoc.y + (rot / 84f) * 0.5f * openMod;
					} else {
						openMod = -1f;
						if(doorDirection == DoorDirection.NORTH) openMod *= -1f;
						nextX = startLoc.x - (rot / 84f) * 0.5f * openMod;
						
						openMod = doorInvertOpen ? -1 : 1;
						if(doorDirection == DoorDirection.SOUTH) openMod *= -1f;
						nextY = startLoc.y + (rot / 98f) * 0.5f * openMod;
					}
				}
				else if(doorType == DoorType.TRAPDOOR) {
					nextY = startLoc.y - (rot / 84f) * 0.435f * openMod;
					nextZ = startLoc.z + (rot / 84f) * 0.51f * openMod;
				}
			}
			
			if(doorOpenType == DoorOpenType.ROTATE && doorType == DoorType.NORMAL) {
				Entity encroaching = null;
				if(animUnit < rotateAnimSolidPoint && doorState == DoorState.CLOSING) encroaching = level.checkEntityCollision(x, y, z, collision, this);
				
				if(encroaching == null) {
					if(doorState == DoorState.CLOSING && animUnit < rotateAnimSolidPoint) isSolid = true;
				} else {
					openMod = doorInvertOpen ? -1 : 1;
					animateTime = lastAnimateTime;
					animUnit = animateInterpolation.apply(1 - animateTime / animateSpeed);
					rot = animUnit * 86 * openMod;
				}
			}
			else if(doorOpenType == DoorOpenType.ROTATE && doorType == DoorType.TRAPDOOR) {
				x = nextX;
				y = nextY;
				z = nextZ;
			}
			else {
				Entity encroaching = level.checkEntityCollision(nextX, nextY, nextZ + (doorType != DoorType.TRAPDOOR ? 0 : -0.2f), collision, this);
				if(encroaching == null || !encroaching.isDynamic) {
					x = nextX;
					y = nextY;
					z = nextZ;
				}
				else {
					animateTime = lastAnimateTime;
					
					if(doorOpenType != DoorOpenType.SLIDE_UP) {
						encroaching.xa += (nextX - x) / 2f;
						encroaching.ya += (nextY - y) / 2f;
					}
				}
			}

			makeDust(animUnit);
		}
		
		// show mobile use message
		if(Game.isMobile)
		{
			if(Math.abs(Game.instance.player.x - x) < 1f && Math.abs(Game.instance.player.y - y) < 1f && Math.abs(Game.instance.player.z - z) < 1f) {
				Game.ShowUseMessage(MessageFormat.format(StringManager.get("entities.Door.mobileUseText"), getUseText()));
			}
		}
		
		this.color = level.GetLightmapAt(x, y, z);

		if(shakeTimer > 0) shakeTimer -= delta;

		tickAttached(level, delta);
	}

	private void makeDust(float animateTime) {
		if((doorOpenType == DoorOpenType.SLIDE_UP || doorOpenType == DoorOpenType.SLIDE) && doorState == DoorState.OPENING) {
			if (slideEffectTimer <= 0) {
				slideEffectTimer = 4 + animateTime * 10f;

				for(int i = 1; i >= -1; i -= 2) {
					Particle p = CachePools.getParticle(x - xa, y - ya, z - za - 0.45f, 0, 0, 0, Game.rand.nextInt(3), Color.WHITE, false);

					// place on bottom edges
					p.x = x;
					p.y = y;

					if(doorOpenType == DoorOpenType.SLIDE) {
						p.z = z - 0.6f + Game.rand.nextFloat() * 0.1f + collision.z;
						if(Game.rand.nextBoolean()) {
							p.z -= collision.z;
						}
					}
					else if(doorOpenType == DoorOpenType.SLIDE_UP) {
						if(!doorInvertOpen) {
							p.z = z - 0.5f + Game.rand.nextFloat() * 0.1f;
						}
						else {
							p.z = z - 0.5f + Game.rand.nextFloat() * 0.1f + collision.z;
						}
					}

					float colY = collision.y * 0.8f;
					float colX = collision.x * 0.8f;

					if(doorOpenType == DoorOpenType.SLIDE) {
						if(doorDirection == DoorDirection.NORTH) {
							p.y -= colY;
						}
						else if(doorDirection == DoorDirection.SOUTH) {
							p.y += colY;
						}
						else if(doorDirection == DoorDirection.WEST) {
							p.x += colX;
						}
						else if(doorDirection == DoorDirection.EAST) {
							p.x -= colX;
						}
					}
					else if(doorOpenType == DoorOpenType.SLIDE_UP) {
						if (Game.rand.nextFloat() < 0.8f) {
							p.x += colX * (Game.rand.nextBoolean() ? 1f : -1f);
							p.y += colY * (Game.rand.nextBoolean() ? 1f : -1f);
						} else {
							p.x += -colX + (Game.rand.nextFloat() * 2f * colX);
							p.y += -colY + (Game.rand.nextFloat() * 2f * colY);
						}
					}

					p.checkCollision = false;
					p.floating = true;
					p.lifetime = (int) (30 * Game.rand.nextFloat()) + 60;
					p.shader = "dust";
					p.spriteAtlas = "dust_puffs";
					p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
					p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
					p.endColor = new Color(1f, 1f, 1f, 0f);
					p.scale = 0.5f;

					p.xa = (0.00125f * Game.rand.nextFloat());
					p.ya = (0.00125f * Game.rand.nextFloat());
					p.za = (0.00125f * Game.rand.nextFloat()) + 0.0025f;

					Game.GetLevel().SpawnNonCollidingEntity(p);
				}
			}
		}
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		if(triggerMode == DoorTriggerMode.OPEN_CLOSE || triggerMode == null) {
			if (!getsStuckOpen && (doorState == DoorState.OPEN || doorState == DoorState.OPENING)) doClose(false);
			else doOpen(false);
		}
		else if (triggerMode == DoorTriggerMode.LOCK_UNLOCK) {
			isLocked = !isLocked;
		}
	}
	
	@Override
	public void updateDrawable() {
		// init the drawable
		DrawableMesh doorDrawable = null;
		if(!(drawable instanceof DrawableMesh) || (lastMeshFile != null && !lastMeshFile.equals(doorMesh))) {
			doorDrawable = new DrawableMesh(doorMesh, doorTexture);
			drawable = doorDrawable;
            lastMeshFile = doorMesh;
		}
		
		if(drawable != null) {  
			doorDrawable = (DrawableMesh) drawable;
			drawable.update(this);
			
			doorDrawable.x = x;
			doorDrawable.y = y;
			doorDrawable.z = z;
		}
		
		// setup draw offset
		if(doorDirection == DoorDirection.EAST) {
			drawable.drawOffset.x = 0.5f;
			drawable.drawOffset.y = 0;
		}
		else if(doorDirection == DoorDirection.WEST) {
			drawable.drawOffset.x = -0.5f;
			drawable.drawOffset.y = 0;
		}
		else if(doorDirection == DoorDirection.NORTH) {
			drawable.drawOffset.x = 0;
			drawable.drawOffset.y = -0.5f;
		}
		else if(doorDirection == DoorDirection.SOUTH) {
			drawable.drawOffset.x = 0;
			drawable.drawOffset.y = 0.5f;
		}
		
		// trapdoors face up
		drawable.dir.set(Vector3.X).scl(-1f);
		if(doorType == DoorType.TRAPDOOR) {
			drawable.dir.rotate(Vector3.Z, -90f);
			yOffset = -0.5f;
		}
		else {
			yOffset = 0;
		}
		
		// set the rotation based on the door direction
		if(doorDirection == DoorDirection.EAST) {
			drawable.dir.rotate(Vector3.Y, 90f);
		}
		else if(doorDirection == DoorDirection.SOUTH) {
			drawable.dir.rotate(Vector3.Y, 0f);
		}
		else if(doorDirection == DoorDirection.NORTH) {
			drawable.dir.rotate(Vector3.Y, 180f);
		}
		else if(doorDirection == DoorDirection.WEST) {
			drawable.dir.rotate(Vector3.Y, 270f);
		}
		
		if(rot != 0) {
			if(doorType == DoorType.NORMAL)
				drawable.dir.rotate(Vector3.Y, -rot);
			else if(doorType == DoorType.TRAPDOOR)
				drawable.dir.rotate(Vector3.X, -rot);
		}

		if(shakeTimer > 0) {
			if(doorDirection == DoorDirection.SOUTH || doorDirection == DoorDirection.NORTH)
				drawable.dir.z += ((float)Math.sin(Game.instance.time * 0.5f) * shakeTimer * (shakeAmount * 0.005f)) * 0.05f;
			else
				drawable.dir.x += ((float)Math.sin(Game.instance.time * 0.5f) * shakeTimer * (shakeAmount * 0.005f)) * 0.05f;
		}

		// attachments should rotate with the door!
		if(attachmentTransform == null) {
			attachmentTransform = new Vector3(x, y , z);
			tempDir.set(drawable.dir.z, drawable.dir.x, drawable.dir.y).scl(-0.5f);
		}
		if(attachmentTransform != null) {
			offset.set(drawable.dir.z, drawable.dir.x, drawable.dir.y).scl(-0.5f);
			tempDir.sub(offset);

			attachmentTransform.set(tempDir).add(x, y, z);

			tempDir.set(offset);
		}
	}

	@Override
	public void rotate90() {
		if (doorDirection == DoorDirection.NORTH) {
			doorDirection = DoorDirection.EAST;
		} else if (doorDirection == DoorDirection.EAST) {
			doorDirection = DoorDirection.SOUTH;
		} else if (doorDirection == DoorDirection.SOUTH) {
			doorDirection = DoorDirection.WEST;
		} else if (doorDirection == DoorDirection.WEST) {
			doorDirection = DoorDirection.NORTH;
		}

		super.rotate90();
	}

	@Override
	public void rotate90Reversed() {
		if (doorDirection == DoorDirection.NORTH) {
			doorDirection = DoorDirection.WEST;
		} else if (doorDirection == DoorDirection.WEST) {
			doorDirection = DoorDirection.SOUTH;
		} else if (doorDirection == DoorDirection.SOUTH) {
			doorDirection = DoorDirection.EAST;
		} else if (doorDirection == DoorDirection.EAST) {
			doorDirection = DoorDirection.NORTH;
		}

		super.rotate90Reversed();
	}

	public String getUseText() {
		if(doorState == DoorState.OPEN || doorState == DoorState.OPENING) return StringManager.get("entities.Door.closeUseText");
		return StringManager.get("entities.Door.openUseText");
	}
	
	@Override
	public void init(Level level, Source source) {
		if(source == Source.LEVEL_START || startLoc == null) {
			startLoc = new Vector3(x, y, z);
			animateTime = speed;
		}
		
		Audio.loadSound(openSound);
		Audio.loadSound(closingSound);
		Audio.loadSound(closedSound);
		
		super.init(level, source);
	}
	
	public void doHitEffect(float xLoc, float yLoc, float zLoc, Sword sword, Level lvl) {
		if(hp > 0) {
			if(breakSound != null)
				Audio.playPositionedSound(breakSound, new Vector3(x,y,z), 0.1f, Game.rand.nextFloat() * 0.1f + 0.95f, 12);
			else
				Audio.playPositionedSound(sword.wallHitSound, new Vector3(x,y,z), 0.25f, Game.rand.nextFloat() * 0.1f + 0.95f, 12);
		}
		
		Color hitColor = sword.getEnchantmentColor();
		boolean fullBright = sword.getDamageType() != DamageType.PHYSICAL;
		
		if(fullBright) {
			// make a light at this location
			DynamicLight l = new DynamicLight(xLoc, yLoc, zLoc, new Vector3(hitColor.r * 0.85f, hitColor.g * 0.85f, hitColor.b * 0.85f));
			l.startLerp(new Vector3(0,0,0), 20, true);
			lvl.non_collidable_entities.add(l);
		}
		
		Random r = Game.rand;
		for(int ii = 0; ii < r.nextInt(5) + 3; ii++)
		{
			lvl.SpawnNonCollidingEntity(CachePools.getParticle(xLoc, yLoc, zLoc + 0.6f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.01f - 0.005f, r.nextFloat() * 0.03f - 0.015f, 420 + r.nextInt(500), 1f, 0f, 0, hitColor, fullBright)) ;
		}

		if(breakable) {
			Particle part = CachePools.getParticle(xLoc, yLoc, zLoc + 0.2f, "dust_puffs", 5);
			part.floating = true;
			part.checkCollision = false;
			part.shader = "spark";
			part.spriteAtlas = "dust_puffs";
			part.lifetime = 15;
			part.scale = 0.7f;
			part.color.set(Color.WHITE);
			part.color.a = 0.32f;
			part.endColor = new Color(part.color);
			part.endColor.a = 1;
			part.fullbrite = true;
			lvl.SpawnNonCollidingEntity(part);
		}
		
		Game.instance.player.shake(0.8f);
	}
	
	@Override
	public void hit(float projx, float projy, int damage, float knockback, DamageType damageType, Entity instigator) {
		super.hit(projx, projy, damage, knockback, damageType, instigator);
		if(breakable) { 
			hp -= damage;

			shakeTimer = 40f;
			
			if(hp <= 0) {
				gib(Game.instance.level, new Vector3(projx * 0.04f, projy * 0.04f, 0));
			}

			// start on fire sometimes when taking fire damage
			if(damageType == DamageType.FIRE && Game.rand.nextBoolean()) {

				if(attached != null) {
					int fireCount = 0;
					for (int i = 0; i < attached.size; i++) {
						if(attached.get(i) instanceof Fire) fireCount++;
					}
					if(fireCount >= 3) return;
				}

				Fire f = new Fire();
				f.lifeTime = 300;
				f.playAnimation();
				f.hurtTimer = f.hurtTime * 0.75f;
				f.spreadTimer = f.spreadTime * 0.75f;
				f.z = Game.rand.nextFloat() * collision.z * 0.8f;
				f.x = ((Game.rand.nextFloat() - 0.5f) * 2f) * collision.x;
				f.y = ((Game.rand.nextFloat() - 0.5f) * 2f) * collision.y;
				attach(f);
			}
			
			// TODO: Better door stuck open effect
			/*else {
				if(Game.rand.nextFloat() < 0.2f + knockback * 0.5f && doorState == DoorState.STUCK) {
					doOpen(true);
					animateSpeed = speed;
					animateInterpolation = Interpolation.exp5Out;
					animateTime = speed;
				}
			}*/
		}
	}
	
	public void gib(Level level, Vector3 gibVel) {
		isActive = false;
		
		for(int i = 0; i < gibNum; i++) {
			int range = gibSpriteTexEnd - gibSpriteTexStart;
			int ptex = gibSpriteTexStart;
			float gibLifetime = 2000;
			
			Particle p = CachePools.getParticle(x, y, z + (collision.z * 0.95f * Game.rand.nextFloat()), (Game.rand.nextFloat() * gibVelocity.x) - gibVelocity.x * 0.5f + gibVel.x, (Game.rand.nextFloat() * gibVelocity.y) - gibVelocity.y * 0.5f + gibVel.y, (Game.rand.nextFloat() * gibVelocity.z) - gibVelocity.z * 0.5f, gibLifetime + Game.rand.nextFloat() * gibLifetime, ptex, Color.WHITE, false);
			p.movementRotateAmount = 6f;
			
			// Make most of the gibs the small variant.
			if(i < gibNum * 0.5f) {
				p.tex = gibSpriteTexStart + range;
				p.mass = 0.1f;
				p.lifetime = gibLifetime * 0.65f;
			}
			// Otherwise choose at random from the remaining gibs.
			else {
				p.tex = gibSpriteTexStart + Game.rand.nextInt(range);
			}
			
			level.non_collidable_entities.add(p);
		}
		
		Audio.playPositionedSound(breakSound, new Vector3(x,y,z), 0.6f, 12);
	}

	public void placeFromPrefab(Level level) {
		Tile west = level.getTile((int)x - 1, (int)y);
		Tile east = level.getTile((int)x + 1, (int)y);
		Tile north = level.getTile((int)x, (int)y + 1);
		Tile south = level.getTile((int)x, (int)y - 1);

		boolean westIsSolid = west.IsSolid() || (west.floorHeight + 0.5f > z + collision.z * 0.5f);
		boolean eastIsSolid = east.IsSolid() || (east.floorHeight + 0.5f > z + collision.z * 0.5f);
		boolean northIsSolid = north.IsSolid() || (north.floorHeight + 0.5f > z + collision.z * 0.5f);
		boolean southIsSolid = south.IsSolid() || (south.floorHeight + 0.5f > z + collision.z * 0.5f);

		boolean northSouth = northIsSolid && southIsSolid;
		boolean eastWest = westIsSolid && eastIsSolid;

		if(doorDirection == DoorDirection.NORTH || doorDirection == DoorDirection.SOUTH) {
			if(eastWest && !northSouth) {
				rotate90();
			}
		}
		else if(doorDirection == DoorDirection.EAST || doorDirection == DoorDirection.WEST) {
			if(northSouth && !eastWest) {
				rotate90();
			}
		}
	}
}
