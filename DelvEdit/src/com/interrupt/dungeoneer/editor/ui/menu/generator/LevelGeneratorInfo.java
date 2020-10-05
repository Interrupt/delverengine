package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

public class LevelGeneratorInfo implements Comparator<LevelGeneratorInfo>, Comparable<LevelGeneratorInfo> {
    public String name;
    public int sortOrder;
    public Array<LevelTemplateInfo> templates;

    @Override
    public int compare(LevelGeneratorInfo o1, LevelGeneratorInfo o2) {
        if (o1.sortOrder > o2.sortOrder) {
            return 1;
        } else if (o1.sortOrder > o2.sortOrder) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public int compareTo(LevelGeneratorInfo o) {
        return compare(this, o);
    }
}
