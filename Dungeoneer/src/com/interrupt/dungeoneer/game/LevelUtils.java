package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Stairs;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class LevelUtils {
    private static Boolean checkIsValidLevel(Level tocheck, int dungeonlevel) {
        if(tocheck == null) return false;

        Array<LevelInterface> levels = Game.buildLevelLayout();
        if(dungeonlevel < levels.size) {
            // look for exit markers
            for(EditorMarker m : tocheck.editorMarkers) {
                if(m.type == GenInfo.Markers.exitLocation || m.type == GenInfo.Markers.stairDown) return true;
            }

            // look for exit entities
            for(Entity e : tocheck.entities) {
                if(e instanceof Stairs && ((Stairs)e).direction == Stairs.StairDirection.down) return true;
            }
        }
        else {
            return true;
        }

        return false;
    }

    public void initEntities(Level level, Array<Entity> entityList, Level.Source source) {
        if(entityList == null)
            return;

        for(int i = 0; i < entityList.size; i++) {
            Entity e = entityList.get(i);

            // Might need to override the sprite atlas
            /*if(source == Level.Source.LEVEL_START && spriteAtlasOverrides != null) {
                overrideSpriteAtlas(e);
            }*/

            if(e.drawable != null) {
                e.drawable.refresh();
            }

            e.init(level, source);
        }
    }

    /*public void tickEntityList(float delta, Array<Entity> list, boolean inEditor) {

        // update everyone in the list
        Entity e = null;
        int entity_index;
        for(entity_index = 0; entity_index < list.size; entity_index++)
        {
            e = list.get(entity_index);

            if(!inEditor) {
                if(e.skipTick) {
                    e.skipTick = false;
                    continue;
                }

                e.tick(this, delta);
            }
            else {
                e.editorTick(this, delta);
            }

            if(!e.isActive) toDelete.add(e);
        }

        // remove the newly inactive items from the list
        for(entity_index = 0 ; entity_index < toDelete.size; entity_index++) {
            e = toDelete.get(entity_index);

            // clear this guy from some caches if needed
            if(e instanceof Particle) CachePools.freeParticle((Particle)e);
            e.onDispose();
            list.removeValue(e,true);
        }

        // clear the list for next time
        toDelete.clear();
    }*/

    public static void makeUniqueEntityId(String idPrefix, Entity e) {
        e.makeEntityIdUnique(idPrefix);
    }

    public static boolean checkIfTextureAtlasesMatch(String atlasOne, String atlasTwo) {
        if(atlasOne == null) atlasOne = TextureAtlas.cachedRepeatingAtlases.firstKey();
        if(atlasTwo == null) atlasTwo = TextureAtlas.cachedRepeatingAtlases.firstKey();
        return atlasOne.equals(atlasTwo);
    }
}
