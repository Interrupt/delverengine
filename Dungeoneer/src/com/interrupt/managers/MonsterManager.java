package com.interrupt.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class MonsterManager {

    public static MonsterManager instance = null;
    public static void setSingleton(MonsterManager _instance) { instance = _instance; }
	
	public HashMap<String, Array<Monster>> monsters;
	
	Random random;
	
	public MonsterManager() { random = new Random(); }
	
	public Monster GetRandomMonster(String levelTheme) {
		
		if(levelTheme == null || !monsters.containsKey(levelTheme) ) return null;
		int r = random.nextInt(monsters.get(levelTheme).size);
		return Copy( Monster.class, monsters.get(levelTheme).get(r) );
	}
	
	public Monster GetMonster(String levelTheme, String name) {
		if( monsters.containsKey(levelTheme) ) {
			Array<Monster> list = monsters.get(levelTheme);
			for(int i = 0; i < list.size; i++) {
				if(list.get(i).name.equals(name))
					return Copy( Monster.class, list.get(i) );
			}
		}
		return null;
	}
	
	public Monster Copy(Class<?> type, Monster tocopy)
	{
		return (Monster) KryoSerializer.copyObject(tocopy);
	}

    public void merge(MonsterManager otherMonsterManager) {
        for(String category : otherMonsterManager.monsters.keySet()) {
            if(monsters.containsKey(category)) {
                // walk through and merge stuff
                Array<Monster> bucket = monsters.get(category);
                for(Monster m : otherMonsterManager.monsters.get(category)) {
                    mergeMonster(bucket, m);
                }
            }
            else {
                // easy, just add that other category
                monsters.put(category, otherMonsterManager.monsters.get(category));
            }
        }
    }

    // If a monster name already exists that is the same as the new one, replace it. Otherwise add it.
    public void mergeMonster(Array bucket, Monster monster) {
        int foundIndex = -1;
        for(int i = 0; i < bucket.size && foundIndex == -1; i++) {
            Monster m = (Monster)bucket.get(i);

            if(m.name != null && m.name.equals(monster.name)) {
                foundIndex = i;
            }
        }

        // if this already exists, replace it. Otherwise just add it
        if(foundIndex != -1)
            bucket.set(foundIndex, monster);
        else
            bucket.add(monster);
    }
}
