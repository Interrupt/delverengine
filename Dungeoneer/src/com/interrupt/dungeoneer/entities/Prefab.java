package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.managers.EntityManager;

public class Prefab extends Group {
	/** Prefab category. */
	@EditorProperty
	public String category = "NONE";

	/** Prefab name. */
	@EditorProperty
	public String name = "NONE";
	
	public transient String loadedCategory = "NONE";
	public transient String loadedName = "NONE";
	
	public Prefab() { artType = ArtType.hidden; }
	public Prefab(String category, String name) { artType = ArtType.hidden; this.category = category; this.name = name; }
	
	protected String lastName = "";
	
	@Override
	public void tick(Level level, float delta) {
		// nothing!
		artType = ArtType.hidden;
	}
	
	@Override
	public void init(Level level, Source source) {
		// Force a refresh
		lastRot.set(0, 0, 0);
		loadedCategory = "NONE";
		loadedName = "NONE";

		updateDrawable();
		spawnPrefab(level, source);
	}

	public Entity GetEntity(String category, String name) {
		return EntityManager.instance.getEntity(category, name);
	}

	public void SpawnEntity(Level level, Entity e) {
		if (e instanceof Monster) {
			((Monster)e).Init(level, Game.instance.player.level);
		}
		level.addEntity(e);
	}

	public void spawnPrefab(Level level, Source source) {

		if(!isActive)
			return;

		setPosition(x, y, z);

		if(source != Source.EDITOR && isActive) {
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
				}

				SpawnEntity(level, e);

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
	}
	
	@Override
	public void rotate90() {
		updateDrawable();
		super.rotate90();
	}

	@Override
	public void rotate90Reversed() {
		updateDrawable();
		super.rotate90Reversed();
	}
	
	@Override
	public void updateDrawable() {
		if(!loadedCategory.equals(category) || !lastName.equals(name)) {
			entities.clear();
			
			loadedCategory = category;
			loadedName = name;
			lastName = name;
			
			if (loadedName.contains(",")) {
				String[] names = loadedName.split(",");
				loadedName = names[Game.rand.nextInt(names.length)].trim();
			}
			
			Entity copy = GetEntity(loadedCategory, loadedName);
			if(copy != null)
				entities.add(copy);
			else if(GameManager.renderer.editorIsRendering) {
				// Something bad happened, show at least *something* in the editor
				Sprite badSprite = new Sprite(0,0,11);
				badSprite.spriteAtlas = "editor";
				entities.add(badSprite);
			}
			
			lastRot.set(0,0,0);
			lastPosition.set(0,0,0);

			// Might also need to update this new drawable
			if(copy instanceof Prefab) {
				copy.updateDrawable();
			}
		}
		
		super.updateDrawable();
	}

	@Override
	public void editorTick(Level level, float delta) {
		if(entities != null) {
			for(int ii = 0; ii < entities.size; ii++) {
				Entity e = entities.get(ii);
				if(e.attached != null) {
					if(e.attachmentTransform == null) e.attachmentTransform = new Vector3(0,0,0);
					for (int i = 0; i < e.attached.size; i++) {
						Entity attachment = e.attached.get(i);
						attachment.x += x - e.attachmentTransform.x;
						attachment.y += y - e.attachmentTransform.y;
						attachment.z += z - e.attachmentTransform.z;
					}
					e.attachmentTransform.set(x, y, z);
				}
			}
		}
	}
}
