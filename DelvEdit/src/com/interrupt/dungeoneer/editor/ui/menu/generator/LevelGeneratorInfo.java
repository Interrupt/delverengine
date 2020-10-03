package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

public class LevelGeneratorInfo implements Comparator<LevelGeneratorInfo> {
    public String name;
    public Array<LevelTemplateInfo> templates;

	@Override
	public int compare(LevelGeneratorInfo o1, LevelGeneratorInfo o2) {
        return o1.name.compareTo(o2.name);
	}
}
