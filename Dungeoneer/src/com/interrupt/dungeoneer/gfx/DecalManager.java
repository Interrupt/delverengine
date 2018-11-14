package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.drawables.DrawableProjectedDecal;

public class DecalManager {
	private static Array<DrawableProjectedDecal> decals = new Array<DrawableProjectedDecal>();
	private static int maxDecals = 2;
	
	public static boolean addDecal(DrawableProjectedDecal d) {
		
		if(maxDecals <= 0) return false;
		
		decals.add(d);
		
		if(decals.size > maxDecals) {
			int toDelete = decals.size - maxDecals;
			for(int i = 0; i < toDelete; i++) {
				removeOldest();
			}
		}
		
		return true;
	}
	
	private static void removeOldest() {
		DrawableProjectedDecal last = decals.get(0);
		if(last != null && last.owner != null) last.owner.isActive = false;
		
		decals.removeValue(last, true);
	}
	
	public static void setQuality(float graphicsQuality) {
		if(graphicsQuality < 0.3f) maxDecals = 0;
		else {
			maxDecals = (int)(40 * (graphicsQuality + 0.3f));
		}
	}
}
