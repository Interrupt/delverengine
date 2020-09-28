package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.gfx.drawables.DrawableText;

public class NeoText extends DirectionalEntity {

	@EditorProperty
	public String text = "Test";

	@EditorProperty
	public Color textColor = new Color(Color.WHITE);

	@EditorProperty
	public DrawableText.TextAlignment textAlignment = DrawableText.TextAlignment.CENTER;

	public NeoText() {
		this.drawable = new DrawableText(this.text);
	}
}
