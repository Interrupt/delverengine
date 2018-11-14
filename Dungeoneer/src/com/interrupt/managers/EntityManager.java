package com.interrupt.managers;

import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.OrderedMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.serializers.v2.ThreadSafeLevelSerializer;

public class EntityManager {
	
	public static EntityManager instance = null;
	public static void setSingleton(EntityManager _instance) { instance = _instance; }
	
	public HashMap<String, OrderedMap<String, Entity>> entities;
    public Array<Entity> surprises;

    Random random;

    public EntityManager() { random = new Random(); }

	public Entity getEntityTemplate(String category, String name) {
        OrderedMap<String, Entity> catHash = entities.get(category);
        if(catHash != null) {
            if(!name.isEmpty()) {
                return catHash.get(name);
            }
            else if(catHash.size > 0) {
                Array<Entity> values = catHash.values().toArray();
                return values.get(Game.rand.nextInt(values.size));
            }
        }

        return null;
    }
	
	public Entity getEntity(String category, String name) {
		OrderedMap<String, Entity> catHash = entities.get(category);
		if(catHash != null) {
            if(!name.isEmpty()) {
                return Copy(catHash.get(name));
            }
            else if(catHash.size > 0) {
                Array<Entity> values = catHash.values().toArray();
                return Copy(values.get(Game.rand.nextInt(values.size)));
            }
		}
		
		return null;
	}

    public Entity getEntityWithSerializer(String category, String name, ThreadSafeLevelSerializer serializer) {
        OrderedMap<String, Entity> catHash = entities.get(category);
        if(catHash != null) {
            if(!name.isEmpty()) {
                return (Entity)serializer.copyObject(catHash.get(name));
            }
            else if(catHash.size > 0) {
                Array<Entity> values = catHash.values().toArray();
                return (Entity)serializer.copyObject(values.get(Game.rand.nextInt(values.size)));
            }
        }

        return null;
    }

    // sometimes we want to know how big an entity will be, without actually doing the full copy
    public Vector3 getEntityCollisionSize(String category, String name) {
        OrderedMap<String, Entity> catHash = entities.get(category);
        if(catHash != null) {
            if(!name.isEmpty()) {
                Entity e = catHash.get(name);
                return e != null ? e.collision : new Vector3();
            }
        }

        return new Vector3();
    }

    public void merge(EntityManager otherEntityManager) {
        for(String category : otherEntityManager.entities.keySet()) {
            if(entities.containsKey(category)) {
                // okay, walk through and merge stuff
                OrderedMap<String, Entity> c = entities.get(category);
                for(Entry<String, Entity> e : otherEntityManager.entities.get(category).entries()) {
                    c.put(e.key, e.value);
                }
            }
            else {
                // easy, just add that other category
                entities.put(category, otherEntityManager.entities.get(category));
            }
        }
    }

    public Entity GetRandomSurprise() {
            Array<Entity> surprises = EntityManager.instance.surprises;
            if (surprises == null || surprises.size == 0) {
                return null;
            }

            int r = random.nextInt(surprises.size);
            return Copy(surprises.get(r));
	}
	
	public Entity Copy(Entity tocopy)
	{
		return (Entity) KryoSerializer.copyObject(tocopy);
	}
}