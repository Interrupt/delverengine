package com.interrupt.dungeoneer.gfx.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Entity.ArtType;

public abstract class Drawable {
	public Color color = Color.WHITE;
	public boolean fullbrite = false;
	public Vector3 dir = new Vector3(Vector3.Z).scl(-1f);
	public ArtType artType;
	public float scale = 1;
	public Vector3 drawOffset = new Vector3(Vector3.Zero);
	public float frustumCullSize = 0.55f;
	public String shader = null;
	public Entity.BlendMode blendMode = Entity.BlendMode.OPAQUE;

	public transient boolean isDirty = true;

	public void update(Entity e) {
		// implement in subclasses
	}

	public void refresh() {
		isDirty = true;
	}
}
