package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Tesselator;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.utils.JsonUtil;

public class EditorArt {
	
	private static boolean didInit = false;
	
	private static String[] atlasList = null;
	
	public static void initAtlases() {
		FileHandle itemFile = Game.getInternal("data/spritesheets.dat");
		TextureAtlas[] atlases = JsonUtil.fromJson(TextureAtlas[].class, itemFile);
		
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

	/**
	 * Reload all art assets.
	 *
	 * @apiNote This must be called from the same thread where OpenGL was initialized.
	 */
	public static void refresh() {
		try {
			Art.KillCache();

			GameManager.renderer.initTextures();
			GameManager.renderer.initShaders();

			GlRenderer.staticMeshPool.resetAndDisposeAllMeshes();
			Tesselator.tesselatorMeshPool.resetAndDisposeAllMeshes();

			Level level = Editor.app.getLevel();

			// reset all drawables now that we've reset stuff
			for (Entity e : level.entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}
			for (Entity e : level.static_entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}
			for (Entity e : level.non_collidable_entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}

			GameManager.renderer.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			level.isDirty = true;

			UiSkin.loadSkin();
		}

		catch(Exception ex) {
			Gdx.app.log("Editor", "Could not refresh: " + ex.getMessage());
		}
	}
}
