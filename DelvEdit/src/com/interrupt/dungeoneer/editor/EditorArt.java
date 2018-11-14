package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.TextureAtlas;

public class EditorArt {
	
	private static boolean didInit = false;
	
	private static String[] atlasList = null;
	
	public static void initAtlases() {
		FileHandle itemFile = Game.getInternal("data/spritesheets.dat");
		TextureAtlas[] atlases = Game.fromJson(TextureAtlas[].class, itemFile);
		
		atlasList = new String[atlases.length + 1];
		int curAtlas = 0;

        atlasList[0] = "NONE";
		
		for(TextureAtlas atlas : atlases) {
			TextureAtlas.cacheAtlas(atlas, atlas.name);
			atlasList[1 + curAtlas++] = atlas.name;
		}

        didInit = true;
	}
	
	public static TextureAtlas getAtlas(String name) {
		if(!didInit) {
			initAtlases();
		}
		
		return TextureAtlas.getCachedRegion(name);
	}
	
	public static String[] getAtlasList() {
		if(!didInit) {
			initAtlases();
		}
		
		return atlasList;
	}
}
