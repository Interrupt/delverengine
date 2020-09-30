package com.interrupt.dungeoneer.generator;

import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;

public class TexturePainter {
	/** Wall texture index mapping. */
	protected HashMap<String, Array<Float>> wall = null;

	/** Floor texture index mapping. */
	protected HashMap<String, Array<Float>> floor = null;

	/** Ceiling texture index mapping. */
	protected HashMap<String, Array<Float>> ceiling = null;
	
	public TexturePainter() { }
	
	public byte GetWallTexture(Byte tex) {
		return(GetTextureFromList(tex, wall));
	}
	
	public byte GetFloorTexture(Byte tex) {
		return(GetTextureFromList(tex, floor));
	}
	
	public byte GetCeilingTexture(Byte tex) {
		return(GetTextureFromList(tex, ceiling));
	}
	
	public byte GetTextureFromList(Byte tex, HashMap<String, Array<Float>> list) {
		String index = Integer.toString(tex);
		if(list != null && list.containsKey(index)) {
			Array<Float> l = list.get(index);
			float num = l.get(Game.rand.nextInt(l.size));
			return (byte)num;
		}
		
		return tex;
	}
}
