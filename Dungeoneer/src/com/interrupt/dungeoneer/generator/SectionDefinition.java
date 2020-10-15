package com.interrupt.dungeoneer.generator;

import java.util.Random;

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

    /** Array of level template tiers spawn probabilities. Use indices of the `levelTemplates` array. */
    public Array<Integer> levelTemplateTiers = null;

    private Random randomGenerator = new Random();

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

    /** Picks a level template tier. */
    private int pickLevelTemplateTier() {
        if (levelTemplateTiers == null || levelTemplateTiers.size <= 0) {
            return 0;
        }

        int tier = 0;

        try {
            tier = levelTemplateTiers.get(randomGenerator.nextInt(levelTemplateTiers.size));
        } catch (IndexOutOfBoundsException exception) {
            tier = 0;
        }

        return tier;
    }

    /** Picks a level template. */
    private Level pickLevelTemplate(int floor) {
        int tier = pickLevelTemplateTier();
        Level levelTemplate;

        try {
            levelTemplate = levelTemplates.get(tier);
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

    /** Does the `SectionDefinition` have at least one floor? */
    private boolean hasFloors() {
        return floors > 0;
    }

    /** Does the `SectionDefinition` have a transition level? */
    private boolean hasTransitionLevel() {
        return transitionLevel != null;
    }
}
