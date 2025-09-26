package com.interrupt.dungeoneer.editor;

import com.interrupt.dungeoneer.generator.GenInfo.Markers;

public class EditorMarker {
	public Markers type;
	public int x;
	public int y;
	public int rot = 0;
	
	public EditorMarker() { }
	
	public EditorMarker(Markers type, int x, int y) {
		this.type = type;
		this.x = x;
		this.y = y;
	}
}
