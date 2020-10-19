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

    /** Array of level template indices corresponding to spawn probabilities. Use indices of the `levelTemplates` array. */
    public Array<Integer> levelTemplateDistribution = null;

    /** Returns an array of level templates, ordered by floors, transition level last. */
    public Array<Level> buildLevels() {
        Array<Level> levels = new Array<>();
        Level levelTemplate;

        if(hasFloors() && hasLevelTemplates()) {
            for(int floor = 0; floor < floors; floor++) {
                levelTemplate = pickLevelTemplate(floor);
                if (levelTemplate != null) {
                    levels.add(levelTemplate);
                }
            }
        }

        if (hasTransitionLevel()) {
            levels.add(pickTransitionLevel());
        }

        return levels;
    }

    /** Picks a level template index. */
    private int pickLevelTemplateIndex() {
        int index = -1;

        if (hasLevelTemplateDistribution()) {
            try {
                index = levelTemplateDistribution.random();
            } catch (IndexOutOfBoundsException exception) {
                index = -1;
            }
        }

        return index;
    }

    /** Picks a level template. */
    private Level pickLevelTemplate(int floor) {
        int index = pickLevelTemplateIndex();
        Level levelTemplate;

        try {
            levelTemplate = (index == -1) ? levelTemplates.random() : levelTemplates.get(index);
        } catch (IndexOutOfBoundsException exception) {
            return null;
        }

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

    /** Does the `SectionDefinition` have at least one index set? */
    private boolean hasLevelTemplateDistribution() {
        return levelTemplateDistribution != null && levelTemplateDistribution.size > 0;
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
