package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class Text extends DirectionalEntity {

	@EditorProperty
	public String text = "Text";

	@EditorProperty
	public String fontAtlas = "font";

	@EditorProperty
	public float spacing = 1f;

	private transient boolean isDirty = true;
	private transient String lastTex = null;
	private transient Float lastScale = 1f;

	public Text()
	{
		spriteAtlas = "editor";
		floating = true;
		scale = 0.2f;
		isDynamic = false;
		tex = 11;
	}

	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
	}

	private transient Vector3 rotTemp = new Vector3();
	public void makeText() {
		if(attached != null)
			attached.clear();

		if(text == null)
			text = "NULL";

		for(int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			int charPos = Art.fontchars.indexOf(c);
			if(charPos < 0 || c == ' ')
				continue;

			float tx = x + i * spacing * scale;
			float ty = y + 0.001f;
			float tz = z;

			TextureAtlas fAtlas = TextureAtlas.cachedAtlases.get(fontAtlas);
			if(fAtlas != null) {
				Vector2 offset = fAtlas.getSpriteOffset(charPos);
				if(offset != null) {
					tz -= offset.y * scale * fAtlas.scale;
				}
			}

			rotTemp.set(tx - x, ty - y, tz - z);
			rotTemp.rotate(Vector3.X, -rotation.x);
			rotTemp.rotate(Vector3.Y, -rotation.y);
			rotTemp.rotate(Vector3.Z, -rotation.z);

			tx = rotTemp.x + x;
			ty = rotTemp.y + y;
			tz = rotTemp.z + z;

			SpriteDecal s = new SpriteDecal(0, 0, charPos);
			s.x = tx - x;
			s.y = ty - y;
			s.z = tz - z;
			s.spriteAtlas = fontAtlas;
			s.fullbrite = fullbrite;
			s.floating = true;
			s.isDynamic = true;
			s.isSolid = false;
			s.scale = scale;
			s.setRotation(0, 0, 180);
			s.rotate(rotation.x, rotation.y, rotation.z);

			attach(s);
		}

		lastTex = text;
		lastScale = scale;
	}

	@Override
	public void updateDrawable() {
		if(isDirty || drawable == null) {
			makeText();
			isDirty = false;
		}
		super.updateDrawable();

		if(drawable != null) {
			if(text.length() > 0 || !GameManager.renderer.editorIsRendering)
				drawable.scale = 0f;
			else
				drawable.scale = scale;
		}
	}

	@Override
	public void updateLight(Level level) {
		isDirty = true;
		super.updateLight(level);
	}

	@Override
	public void setRotation(float rotX, float rotY, float rotZ) {
		isDirty = true;
		super.setRotation(rotX, rotY, rotZ);
	}

	@Override
	public void editorTick(Level level, float delta) {
		super.editorTick(level, delta);
		tickAttached(level, delta);

		if(text != lastTex || scale != lastScale) {
			isDirty = true;
		}
	}

	@Override
	public void rotate90() {
		isDirty = true;
		super.rotate90();
	}

	@Override
	public void onTrigger(Entity instigator, String value) {
		text = value;
		isDirty = true;
	}
}
