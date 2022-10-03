package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Light;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.LevelInterface;
import com.interrupt.dungeoneer.serializers.v2.LevelSerializer;
import com.interrupt.dungeoneer.tiles.Tile;

public class LevelSwap extends Trigger {
    /** Filename of level to insert when triggered. */
    @EditorProperty
    public String levelFilename;

    /** Update lights after level is inserted? */
    @EditorProperty
    public boolean updateLights = true;

    private int rotAccumulator = 0;

    public LevelSwap() { }

    // triggers can be delayed, fire the actual trigger here
    public void doTriggerEvent(String value) {
        super.doTriggerEvent(value);

        // Load and place a level into this area
        // TODO: do this in a thread?
        try {
            FileHandle levelFile = Game.findInternalFileInMods(levelFilename);
            Level level = LevelSerializer.loadLevel(levelFile);

            if (level != null) {
                LevelInterface currentLevel = Game.GetLevel();

                // Rotate the sublevel to match
                for(int i = 0; i < rotAccumulator; i++) {
                    level.rotate90();
                }

                // Offset the tiles vertically
                for(int i = 0; i < level.tiles.length; i++) {
                    Tile t = level.tiles[i];
                    if(t != null) {
                        t.floorHeight += z;
                        t.ceilHeight += z;
                    }
                }

                currentLevel.paste(level, (int) x - level.width / 2, (int) y - level.height / 2);

                currentLevel.initEntities(level.entities, Level.Source.LEVEL_START);
                currentLevel.initEntities(level.static_entities, Level.Source.LEVEL_START);
                currentLevel.initEntities(level.non_collidable_entities, Level.Source.LEVEL_START);

                if(updateLights) updateLights(level, currentLevel);

                GameManager.renderer.refreshChunksNear(x, y, Math.max(level.width, level.height));
            }
        } catch (Exception ex) {
            Gdx.app.log("LevelSwapTrigger", ex.getMessage());
        }
    }

    public void updateLights(LevelInterface newLevel, LevelInterface existingLevel)
    {
        // light emitting entities
        Array<Entity> dynamicEntities = newLevel.getEntities();
        for(int i = 0; i < dynamicEntities.size; i++)
        {
            Entity e = dynamicEntities.get(i);
            if(e instanceof Light && e.isActive)
            {
                Light t = (Light)e;
                existingLevel.AddLight(t);
            }
        }

        Array<Entity> nonCollidableEntities = newLevel.getNonCollidableEntities();
        for(int i = 0; i < nonCollidableEntities.size; i++)
        {
            Entity e = nonCollidableEntities.get(i);
            if(e instanceof Light && e.isActive)
            {
                Light t = (Light)e;
                existingLevel.AddLight(t);
            }
        }

        Array<Entity> staticEntities = newLevel.getStaticEntities();
        for(int i = 0; i < staticEntities.size; i++)
        {
            Entity e = staticEntities.get(i);
            if(e instanceof Light && e.isActive)
            {
                Light t = (Light)e;
                existingLevel.AddLight(t);
            }
        }

        // light some entities
        for(Entity e : dynamicEntities) {
            e.updateLight(existingLevel);
        }
        for(Entity e : nonCollidableEntities) {
            e.updateLight(existingLevel);
        }
        for(Entity e : staticEntities) {
            e.updateLight(existingLevel);
        }
    }

    @Override
    public void rotate90() {
        rotAccumulator++;
    }
}
