package com.interrupt.dungeoneer.entities;

import com.interrupt.managers.MonsterManager;

public class MonsterPrefab extends Prefab {

	public MonsterPrefab() { artType = ArtType.hidden; }
	public MonsterPrefab(String category, String name) { artType = ArtType.hidden; this.category = category; this.name = name; }

	@Override
	public Entity GetEntity(String category, String name) {
		return MonsterManager.instance.GetMonster(category, name);
	}
}
