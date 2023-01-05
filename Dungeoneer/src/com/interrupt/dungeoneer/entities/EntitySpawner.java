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

	public void placeInInventory(Entity entity) {
		if(null == entity)
			return;

		if(!(entity instanceof Item))
            return;

        if(null == Game.instance || null == Game.instance.player)
            return;

        Item item = (Item)entity;
        Player player = Game.instance.player;

        boolean foundSpot = false;

        if(placement == ItemPlacement.player_hotbar || placement == ItemPlacement.player_equip) {
            foundSpot = player.addToInventory(item, placement == ItemPlacement.player_equip);
        }
        else {
            foundSpot = player.addToBackpack(item);
            if(!foundSpot) {
                foundSpot = player.addToInventory(item, placement == ItemPlacement.player_equip);
            }
        }

        if(!foundSpot) {
            player.throwItem(item, Game.GetLevel(), 0f, 0f);
        }
	}
}
