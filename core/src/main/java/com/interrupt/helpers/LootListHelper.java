package com.interrupt.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

import java.util.HashMap;
import java.util.Set;

public class LootListHelper<T> {

    private String name = "UNKNOWN";

    public LootListHelper() { }
    public LootListHelper(String name) {
        this.name = name;
    }

    Array<T> t_found = new Array<T>();
    public T GetLeveledLoot(int level, HashMap<String, Array<T>> lootList) {
        t_found.clear();

        if(lootList == null)
            return null;

        // lower bounds
        int minLevel = (int)Math.max(level * 0.5, 1);

        // clamp at the top
        int max = getMaxDifficultyLevel(lootList.keySet());
        if(level > max) {
            level = max;
        }

        if(minLevel > max)
            minLevel = max;

        // find all items in range
        for(String key : lootList.keySet()) {
            try {
                int lvl = Integer.parseInt(key);
                if(lvl >= minLevel && lvl <= level) {
                    Array<T> list = lootList.get(key);
                    if(list != null) {
                        t_found.addAll(lootList.get(key));
                    }
                }
            }
            catch(Exception ex) {
                Gdx.app.log("ItemManager: " + name, ex.getMessage());
            }
        }

        if(t_found.size == 0)
            return null;

        t_found.shuffle();
        return Copy(t_found.first());
    }

    private T Copy(T tocopy)
    {
        return (T)KryoSerializer.copyObject(tocopy);
    }

    private int getMaxDifficultyLevel(Set<String> keys) {
        Object[] keysArray = keys.toArray();
        if(keysArray.length == 0) return 1;
        return Integer.valueOf(keysArray[keysArray.length - 1].toString());
    }
}
