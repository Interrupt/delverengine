package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

public class EntitySpawner extends DirectionalEntity {
	public EntitySpawner() { artType = ArtType.hidden; tex = 0; }

	public enum ItemPlacement { world, player_hotbar, player_inventory, player_equip }
	
	@EditorProperty(group = "Spawns") public String entityCategory = "Decorations";
	@EditorProperty(group = "Spawns") public String entityName = "Skull";
	@EditorProperty(group = "Spawns") public float spawnVelocity = 0.1f;
	@EditorProperty(group = "Spawns") public Vector3 spawnSpread = new Vector3(0,0,0);
	@EditorProperty(group = "Spawns") public boolean destroyAfterSpawn = false;
    @EditorProperty(group = "Spawns") public String spawnSound = null;
	@EditorProperty(group = "Spawns") ItemPlacement placement = ItemPlacement.world;
	
	@Override
	public void tick(Level level, float delta) {
		
	}
	
	@Override
	public void onTrigger(Entity instigator, String value) {
		if(!isActive) return;
		
		Entity spawned = Game.instance.entityManager.getEntity(entityCategory, entityName);
		if(spawned != null) {
			Vector3 speed = getDirection();
			speed.scl(spawnVelocity);
			
			spawned.xa = speed.x;
			spawned.ya = speed.y;
			spawned.za = speed.z;
			
			spawned.x = x + (Game.rand.nextFloat() * spawnSpread.x * 0.5f) - spawnSpread.x * 0.5f;
			spawned.y = y + (Game.rand.nextFloat() * spawnSpread.y * 0.5f) - spawnSpread.y * 0.5f;
			spawned.z = z + (Game.rand.nextFloat() * spawnSpread.z * 0.5f) - spawnSpread.z * 0.5f;

			if(placement == ItemPlacement.world)
				Game.instance.level.SpawnEntity(spawned);
			else
				placeInInventory(spawned);
		}

        if(spawnSound != null) {
            Audio.playPositionedSound(spawnSound, new Vector3(x,y,z), 1f, 12f);
        }
		
		if(destroyAfterSpawn) isActive = false;
	}

	public void placeInInventory(Entity e) {
		if(e == null)
			return;

		if(e instanceof Item) {
			Item item = (Item)e;

			if(Game.instance == null || Game.instance.player == null)
				return;

			Player p = Game.instance.player;

			// might need to initialize this?
			if(p.inventory != null) {
				if (p.inventory.size < p.inventorySize) {
					for (int i = 0; i < p.inventorySize; i++)
						p.inventory.add(null);
				}
			}

			boolean foundSpot = false;

			if(placement == ItemPlacement.player_hotbar || placement == ItemPlacement.player_equip) {
				foundSpot = p.addToInventory(item, placement == ItemPlacement.player_equip);
			}
			else {
				foundSpot = p.addToBackpack(item);
				if(!foundSpot) {
					foundSpot = p.addToInventory(item, placement == ItemPlacement.player_equip);
				}
			}

			if(!foundSpot) {
				p.throwItem(item, Game.GetLevel(), 0f, 0f);
			}
		}
	}
}
