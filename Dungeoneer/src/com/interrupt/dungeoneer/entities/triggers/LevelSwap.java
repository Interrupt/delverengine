package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.v2.LevelSerializer;

public class LevelSwap extends Trigger {

    @EditorProperty
    public String levelFilename;

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
                Level currentLevel = Game.GetLevel();
                currentLevel.paste(level, (int) x - level.width / 2, (int) y - level.height / 2);

                currentLevel.initEntities(level.entities, Level.Source.LEVEL_START);
                currentLevel.initEntities(level.static_entities, Level.Source.LEVEL_START);
                currentLevel.initEntities(level.non_collidable_entities, Level.Source.LEVEL_START);

                currentLevel.updateLights(Level.Source.SPAWNED);
                GameManager.renderer.refreshChunksNear(x, y, Math.max(level.width, level.height));
            }
        } catch (Exception ex) {
            Gdx.app.log("LevelSwapTrigger", ex.getMessage());
        }
    }
}
