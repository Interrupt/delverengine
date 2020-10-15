package com.interrupt.dungeoneer.generator;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class SectionDefinition {
    /** What kind of loot should we drop here? */
    public int difficultyLevel = 1;

    /** When should we let this section appear? */
    public int sortOrder = 1;

    /** Name of section. */
    public String name = "My Section";

    /** Number of levels in this section. */
    public int floors = 2;

    /** Transition level to use after all floors in this section. If `floors` is 0, just use this level. */
    public Level transitionLevel = null;

    /** Array of possible level templates to use when making floors. */
    public Array<Level> levelTemplates = null;

    /** Returns an array of level templates, ordered by floors, transition level last. */
    public Array<Level> buildLevels() {
        Array<Level> levels = new Array<>();

        if(hasFloors() && hasLevelTemplates()) {
            for(int floor = 0; floor < floors; floor++) {
                levels.add(pickLevelTemplate(floor));
            }
        }

        if (hasTransitionLevel()) {
            levels.add(pickTransitionLevel());
        }

        return levels;
    }

    /** Picks a level template. */
    private Level pickLevelTemplate(int floor) {
        Level levelTemplate = levelTemplates.first();
        Level level = copyLevelDefinition(levelTemplate);

        level.levelName = getLevelName(level, floor);
        level.dungeonLevel = getDungeonLevel(floor);

        return level;
    }

    /** Picks a transition level. */
    private Level pickTransitionLevel() {
        Level level = copyLevelDefinition(transitionLevel);
        level.dungeonLevel = getDungeonLevel(floors);
        return level;
    }

    /** Returns the level name. */
    private String getLevelName(Level level, int floor) {
        if(level.levelName == null || level.levelName.equals("")) {
            return name + " " + (floor + 1);
        }

        return level.levelName;
    }

    /** Returns the dungeon level. */
    private int getDungeonLevel(int floor) {
        return difficultyLevel + floor;
    }

    /** Returns a copy of a level definition. */
    private Level copyLevelDefinition(Level level) {
        return (Level)KryoSerializer.copyObject(level);
    }

    /** Does the `SectionDefinition` have at least one level template? */
    private boolean hasLevelTemplates() {
        return levelTemplates != null && levelTemplates.size > 0;
    }

    /** Does the `SectionDefinition` have at least one floor? */
    private boolean hasFloors() {
        return floors > 0;
    }

    /** Does the `SectionDefinition` have a transition level? */
    private boolean hasTransitionLevel() {
        return transitionLevel != null;
    }
}
