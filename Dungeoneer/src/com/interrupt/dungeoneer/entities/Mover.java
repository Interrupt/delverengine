package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.helpers.AnimationHelper;
import com.interrupt.helpers.InterpolationHelper.InterpolationMode;

public class Mover extends Model {
	
	public Mover() { meshFile = "meshes/grate.obj"; }
	
	AnimationHelper animation = null;
	
	public enum MoverStartMode { STARTS_IMMEDIATELY, ON_PLAYER_TOUCH, ON_ANY_TOUCH, ON_TRIGGER }
	public enum MoverEndMode { RETURNS, LOOPS, STOPS, REPEATS, PRESSURE }
	
	@EditorProperty public MoverEndMode moverEndMode = MoverEndMode.RETURNS;
	@EditorProperty public MoverStartMode moverMode = MoverStartMode.ON_PLAYER_TOUCH;
	@EditorProperty public Vector3 movesBy = new Vector3(0,0,0.5f);
	@EditorProperty public Vector3 rotatesBy = new Vector3(0,0,0);
	@EditorProperty public float moveTime = 1f;
	@EditorProperty private int squishDamage = 1;
	@EditorProperty( group = "Triggers" ) public String triggersIdAtStart = null;
	@EditorProperty( group = "Triggers" ) public String triggersIdWhenDone = null;
	@EditorProperty public InterpolationMode interpolationMode = InterpolationMode.exp5;
	@EditorProperty public float startWait = 0;
	@EditorProperty public float endWait = 0;
	@EditorProperty public float squishMaxReverseTime = 2;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false ) private String startSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false ) private String endSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false ) private String returnStartSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false ) private String returnEndSound = null;
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false ) private String movingSound = null;
	
	private transient AmbientSound movingAmbientSound = null;
	
	private transient Array<Entity> entityCollisionCache = new Array<Entity>();
	private transient Array<Entity> entityToMoveCache = new Array<Entity>();
	private transient Array<Entity> entityStandingOnUsCache = new Array<Entity>();
	
	private transient boolean didStart = false;
	private transient float squishDamageTimer = 0;
	private transient float squishTotalTimer = 0;
	private float delayTimer = 0;
	
	private transient boolean hasSomethingOn = false;
	private transient boolean pressureStart = false;

	private boolean wasMoving = false;
	
	private transient Vector3 rotationVelocity = new Vector3();
	
	public void startMoving() {
		Vector3 startPos = new Vector3(x,y,z);
		animation = new AnimationHelper(startPos, new Vector3(rotation), new Vector3(movesBy).add(startPos), new Vector3(rotatesBy).add(rotation),moveTime * 60f,interpolationMode);
		Game.instance.level.trigger(this, triggersIdAtStart, "");
		
		delayTimer = startWait;
		pressureStart = true;
	}
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		
		if(source == Source.LEVEL_START && moverMode == MoverStartMode.STARTS_IMMEDIATELY) {
			startMoving();
		}
	}
	
	@Override
	public void tick(Level level, float delta) {
		isDynamic = false;
		squishDamageTimer += delta;
		if(delayTimer > 0) {
			delayTimer -= delta * 0.016f;
		}
		
		if(!didStart && delayTimer <= 0 && animation != null) {
			didStart = true;
			if(animation.isReversed() && returnStartSound != null) Audio.playPositionedSound(returnStartSound, new Vector3(x,y,z), 0.7f, 14f);
			else if(!animation.isReversed() && startSound != null) Audio.playPositionedSound(startSound, new Vector3(x,y,z), 0.7f, 14f);
		}
		
		if(animation != null && delayTimer <= 0) {
			boolean wasDone = animation.isDonePlaying();
			float startAnimPosition = animation.getAnimationPosition();
			animation.tickAnimation(delta);
			
			rotationVelocity.set(rotation);
			
			float previousX = x;
			float previousY = y;
			float previousZ = z;
			
			rotation.set(animation.getCurrentRotation());
			Vector3 position = animation.getCurrentPosition();
			
			float xVel = position.x - previousX;
			float yVel = position.y - previousY;
			float zVel = position.z - previousZ;
			
			rotationVelocity.set(rotation.x - rotationVelocity.x, rotation.y - rotationVelocity.y, rotation.z - rotationVelocity.z);
			float rotZVel = rotationVelocity.z;

			boolean isMoving = xVel != 0 || yVel != 0 || zVel != 0 || rotZVel != 0;
			
			if(isMoving || (wasMoving && moverEndMode == MoverEndMode.PRESSURE)) {
				entityCollisionCache.addAll(level.getEntitiesColliding(position.x, position.y, position.z, this));
				entityStandingOnUsCache.addAll(level.getEntitiesColliding(position.x, position.y, position.z + 0.06f, this));
			}

			wasMoving = isMoving;
			
			boolean canMove = true;
			for(Entity e : entityCollisionCache) {
				if(e.isDynamic) {
					// push entities if they have room to move
					float pushedEntityX = e.x + xVel;
					float pushedEntityY = e.y + yVel;
					float pushedEntityZ = e.z + zVel;
					
					Array<Entity> willCollide = level.getEntitiesColliding(pushedEntityX, pushedEntityY, pushedEntityZ, e);
					if(willCollide.size > 0 && willCollide.get(0) != this) {
						canMove = false;
						
						if(squishDamageTimer > 20 && squishDamage > 0) {
							if(!willCollide.get(0).isDynamic) {
								if(e instanceof Actor) {
									Actor squishing = (Actor)e;
									squishing.takeDamage(squishDamage, Weapon.DamageType.PHYSICAL, this);
								}
							}
						}
					}
					else {
						float checkStepHeight = zVel > 0 ? e.stepHeight : 0;

						if(level.isFree(pushedEntityX, pushedEntityY, pushedEntityZ, e.collision, checkStepHeight, e.floating, null)) {
							entityToMoveCache.add(e);
						}
						else {
							// squishing an entity!
							canMove = false;
							
							if(squishDamageTimer > 20 && squishDamage > 0) {
								if(e instanceof Actor) {
									Actor squishing = (Actor)e;
									squishing.takeDamage(squishDamage, Weapon.DamageType.PHYSICAL, this);
								}
							}
						}
					}
				}
			}
			
			hasSomethingOn = entityStandingOnUsCache.size > 0;
			
			// reverse if something
			if(moverEndMode == MoverEndMode.PRESSURE) {
				if(!hasSomethingOn) {
					if(!animation.isReversed()) {
						animation.reverse();
						delayTimer = 0;
					}
					else if(animation.isDonePlaying()) {
						animation = null;
						didStart = false;
						if(returnEndSound != null) Audio.playPositionedSound(returnEndSound, new Vector3(x,y,z), 0.7f, 14f);
						return;
					}
				}
				else if(hasSomethingOn && animation.isReversed()) {
					animation.reverse();
					
					if(pressureStart) {
						Game.instance.level.trigger(this, triggersIdWhenDone, "");
						delayTimer = endWait;
					}
					
					pressureStart = false;
				}
			}

			// Don't just keep crushing forever
			if(canMove) {
				squishTotalTimer = 0;
			}
			else {
				squishTotalTimer += delta;
				if(squishMaxReverseTime >= 0 && squishTotalTimer > squishMaxReverseTime * 100f) {
					animation.reverse();
				}
			}
			
			if(isMoving && canMove) {
				// update position and velocity
				x += xVel;
				y += yVel;
				z += zVel;
				
				xa = xVel;
				ya = yVel;
				za = zVel;
				
				// move any entities found to be colliding with us
				for(Entity e : entityToMoveCache) {
					if(e.isDynamic) {
						e.x += xVel;
						e.y += yVel;
						e.z += zVel;
						e.physicsSleeping = false;
					}
				}
				
				// move any entities standing on us
				for(Entity e : entityStandingOnUsCache) {
					if(e.isDynamic) {
						e.physicsSleeping = false;
						
						float xnew = xVel + e.x;
						float ynew = yVel + e.y;
						
						// is this rotating?
						if(rotZVel != 0) {
							// move entities on us around center
							float maxRotation = 10f * delta;
							if(rotZVel > 0) rotZVel = Math.min(rotZVel, maxRotation);
							if(rotZVel < 0) rotZVel = Math.max(rotZVel, -maxRotation);
							
							// 1.57079633 is 90 degrees in radians
							float rotRadians = rotZVel * 0.0174532925f;
							float rotSin = (float)Math.sin(-rotRadians);
							float rotCos = (float)Math.cos(-rotRadians);

							float offsetX = e.x - x;
							float offsetY = e.y - y;
							
							xnew = offsetX * rotCos - offsetY * rotSin;
							ynew = offsetX * rotSin + offsetY * rotCos;
							xnew += xVel + x;
							ynew += yVel + y;
							
							// rotate the entity to match
							if(e instanceof DirectionalEntity) {
								DirectionalEntity directional = (DirectionalEntity)e;
								directional.rotation.z += rotZVel;
							}
							else if(e instanceof Player) {
								Player p = (Player) e;
								p.rot += rotRadians;
							}
						}
						
						// move the entity, if the new spot will be free
						if(level.collidesWorldOrEntities(xnew, ynew, e.z, e.collision, e)) {
							e.x = xnew;
							e.y = ynew;
							if(zVel > 0.03f * delta) e.za = zVel;
						}
					}
				}
			}
			else {
				xa = 0;
				ya = 0;
				za = 0;
				if(!canMove) animation.setAnimationPosition(startAnimPosition);
			}
			
			if(moverEndMode != MoverEndMode.STOPS && animation.isDonePlaying() && moverEndMode != MoverEndMode.PRESSURE) {
				// fire the done trigger
				String triggersId = triggersIdWhenDone;

				if(moverEndMode == MoverEndMode.LOOPS) {
					animation.reverse();
					delayTimer = animation.isReversed() ? startWait : endWait;
					if(endSound != null) Audio.playPositionedSound(endSound, new Vector3(x,y,z), 0.7f, 14f);
					triggersId = animation.isReversed() ? triggersIdAtStart : triggersIdWhenDone;
				}
				else if(moverEndMode == MoverEndMode.RETURNS && !animation.isReversed()) {
					animation.reverse();
					delayTimer = endWait;
					if(endSound != null) Audio.playPositionedSound(endSound, new Vector3(x,y,z), 0.7f, 14f);
				}
				else if(moverEndMode == MoverEndMode.RETURNS && animation.isReversed()) {
					animation = null;
					delayTimer = startWait;
					if(returnEndSound != null) Audio.playPositionedSound(returnEndSound, new Vector3(x,y,z), 0.7f, 14f);
				}
				else if(moverEndMode == MoverEndMode.REPEATS) {
					animation = null;
					startMoving();
				}
				didStart = false;

				if(triggersId != null) Game.instance.level.trigger(this, triggersId, "");
			}
			else if(!wasDone && animation.isDonePlaying() && moverEndMode != MoverEndMode.PRESSURE) {
				// fire the done trigger
				if(triggersIdWhenDone != null) Game.instance.level.trigger(this, triggersIdWhenDone, "");

				if(endSound != null) Audio.playPositionedSound(endSound, new Vector3(x,y,z), 0.7f, 14f);
				didStart = false;
			}
		}
		
		if(animation == null || animation.isDonePlaying() || animation.isPaused() || delayTimer > 0.0) {
			xa = 0;
			ya = 0;
			za = 0;
		}
		
		// play the looping moving sound
		if(movingSound != null && !movingSound.equals("")) {
			if(animation != null && !animation.isDonePlaying()) {
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
		
		entityCollisionCache.clear();
		entityToMoveCache.clear();
		entityStandingOnUsCache.clear();
		if(squishDamageTimer > 20) squishDamageTimer = 0;
	}
	
	@Override
	public void onTrigger(Entity instigator, String value) {
		if(animation == null) startMoving();
		else if(animation.isDonePlaying()) animation.reverse();
	}
	
	@Override
	public void encroached(Player player)
	{
		if(moverMode == MoverStartMode.ON_PLAYER_TOUCH || moverMode == MoverStartMode.ON_ANY_TOUCH) {
			hasSomethingOn = true;
			if(animation == null)
				startMoving();
		}
	}
	
	@Override
	public void encroached(Entity hit)
	{
		if(moverMode == MoverStartMode.ON_ANY_TOUCH) {
			hasSomethingOn = true;
			if(animation == null)
				startMoving();
		}
	}

	public void updateDrawable() {
		if(meshFile == null || (lastMeshFile != meshFile)) {
			
			String pickedMeshFile = meshFile;
			if(meshFile.contains(",")) {
				String[] files = meshFile.split(",");
				pickedMeshFile = files[Game.rand.nextInt(files.length)];
			}
			
			String pickedTextureFile = textureFile;
			if(textureFile.contains(",")) {
				String[] files = textureFile.split(",");
				pickedTextureFile = files[Game.rand.nextInt(files.length)];
			}
			
			drawable = new DrawableMesh(pickedMeshFile, pickedTextureFile);
		}
		
		if(drawable != null) {
			DrawableMesh drbl = (DrawableMesh)drawable;
			drbl.rotX = rotation.x;
			drbl.rotY = rotation.y;
			drbl.rotZ = rotation.z;
			drbl.scale = scale;
			drbl.textureFile = textureFile;
			drbl.isStaticMesh = false;
			drbl.bakeLighting = bakeLighting;
			drbl.drawOffset.z = yOffset;
			
			drawable.update(this);
		}
		
		lastMeshFile = meshFile;
	}
	
	@Override
	public void rotate90() {
		super.rotate90();
		movesBy.rotate(Vector3.Z, 90f);
	}
	
	public void onDispose() {
		super.onDispose();
		if(movingAmbientSound != null) movingAmbientSound.onDispose();
	}

	@Override
	public void makeEntityIdUnique(String idPrefix) {
		super.makeEntityIdUnique(idPrefix);
		triggersIdAtStart = makeUniqueIdentifier(triggersIdAtStart, idPrefix);
		triggersIdWhenDone = makeUniqueIdentifier(triggersIdWhenDone, idPrefix);
	}
}
