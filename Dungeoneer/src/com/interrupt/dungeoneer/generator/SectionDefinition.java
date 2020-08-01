package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class SectionDefinition {
    public SectionDefinition() { }

    /** What kind of loot should we drop here? */
    public int difficultyLevel = 1;

    /** When should we let this section appear? */
    public int sortOrder = 1;

    /** Name of section. */
    public String name = "My Section";

    /** Number of levels in this section. */
    public int floors = 2;

    /** Transition level to use after all floors in this section. If numFloors is 0, just use this level. */
    public Level transitionLevel = null;

    /** Array of possible level templates to use when making floors. */
    public Array<Level> levelTemplates = null;

    public Array<Level> buildLevels() {
        Array<Level> l = new Array<Level>();

        // make the levels for this dungeon section
        for(int i = 0; i < floors; i++) {
            if(levelTemplates != null && levelTemplates.size > 0) {
                Level picked = levelTemplates.first();
                Level copy = (Level)KryoSerializer.copyObject(picked);

                if(copy.levelName == null || copy.levelName.equals(""))
                    copy.levelName = name + " " + (i + 1);

                copy.dungeonLevel = difficultyLevel + i;
                l.add(copy);
            }
        }

        // add a transition level if one was set
        if(transitionLevel != null) {
            Level copy = (Level)KryoSerializer.copyObject(transitionLevel);
            copy.dungeonLevel = difficultyLevel + floors;
            l.add(copy);
        }

        return l;
    }
}