package com.interrupt.dungeoneer;

//import javax.imageio.ImageIO;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.Bitmap;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Tesselator;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.ui.UiSkin;

public class Art {
	public static Bitmap font = loadBitmap("font.png", true);
	public static int TILESIZE = 16;
	public static String fontchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.,:()$!#<>%^?/+-'           abcdefghijklmnopqrstuvwxyz";
	
	private static HashMap<String,Mesh> cachedMeshes = new HashMap<String,Mesh>();
	private static HashMap<String,BoundingBox> cachedMeshBounds = new HashMap<String,BoundingBox>();
	
	public static HashMap<String,Texture> cachedTextures = new HashMap<String, Texture>();
	
	public static ObjLoader modelLoader = new ObjLoader();
	
	static public Pixmap loadPixmap(String filename)
	{
		return new Pixmap(Game.findInternalFileInMods(filename));
	}
	
	static public Texture loadTexture(String filename)
	{
		if(cachedTextures.containsKey(filename)) return cachedTextures.get(filename);
		
		Texture t = null;
		try {
			Pixmap base = new Pixmap(Game.findInternalFileInMods(filename));
			t = new Texture(base);
			cachedTextures.put(filename, t);
		}
		catch(Exception ex) {
			Pixmap error = new Pixmap(2,2, Format.RGB888);
			error.setColor(Color.CYAN);
			error.drawRectangle(0, 0, 2, 2);
			
			t = new Texture(error);
			cachedTextures.put(filename, t);
		}

		//Gdx.app.log("Textures", "Loaded texture " + filename);
		
		return t;
	}
	
	static public Bitmap loadBitmap(String filename, boolean killAlpha)
	{	
		try {
			Pixmap img = new Pixmap(Game.findInternalFileInMods(filename));
			
			int w = img.getWidth();
			int h = img.getHeight();
			
			Bitmap loaded = new Bitmap(w, h);
			for(int x = 0; x < w; x++) {
				for(int y = 0; y < h; y++) {
					Integer pixel = img.getPixel(x, y);
					
					int r = (pixel >> 24) & 0xff;
					int g = (pixel >> 16) & 0xff;
					int b = (pixel >> 8) & 0xff;
					int a = pixel & 0xff;
					
					loaded.pixels[x + y * w] = (a << 24) | (r << 16) | (g << 8) | b;
				}
			}
			
			if(!killAlpha) return loaded;
			
			for(int i = 0; i < w * h; i++)
			{
				// we don't need no alpha
				int pixel = loaded.pixels[i];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				
				pixel = (r << 16) | (g << 8) | b;
				loaded.pixels[i] = pixel;
			}
			
			return loaded;
		} catch (Exception ex) {
			return null;
		}
	}
	
	static public Bitmap loadBitmapFromFilehandle(FileHandle file)
	{	
		Pixmap img = new Pixmap(file);
		boolean killAlpha = false;
		
		int w = img.getWidth();
		int h = img.getHeight();
		
		Bitmap loaded = new Bitmap(w, h);
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				Integer pixel = img.getPixel(x, y);
				
				int r = (pixel >> 24) & 0xff;
				int g = (pixel >> 16) & 0xff;
				int b = (pixel >> 8) & 0xff;
				int a = pixel & 0xff;
				
				loaded.pixels[x + y * w] = (a << 24) | (r << 16) | (g << 8) | b;
			}
		}
		
		if(!killAlpha) return loaded;
		
		for(int i = 0; i < w * h; i++)
		{
			// we don't need no alpha
			int pixel = loaded.pixels[i];
			int r = (pixel >> 16) & 0xff;
			int g = (pixel >> 8) & 0xff;
			int b = pixel & 0xff;
			
			pixel = (r << 16) | (g << 8) | b;
			loaded.pixels[i] = pixel;
		}
		
		return loaded;
	}
	
	static public Mesh loadObjMesh(String filename) {
		// try getting the cached mesh first
		if(cachedMeshes.containsKey(filename)) return cachedMeshes.get(filename);
		
		// otherwise load and cache
		FileHandle fh = Game.findInternalFileInMods(filename);
		if(fh.exists()) {
			Model loadedModel = modelLoader.loadModel(fh, true);
			Mesh loadedMesh = loadedModel.meshes.get(0);
			
			cachedMeshes.put(filename, loadedMesh);
			cachedMeshBounds.put(filename, loadedMesh.calculateBoundingBox());
			
			return loadedMesh;
		}

		//Gdx.app.log("Art", "Loaded mesh " + filename);
		
		return null;
	}
	
	static public BoundingBox getCachedMeshBounds(String filename) {
		return cachedMeshBounds.get(filename);
	}
	
	static public void KillCache() {
		ArrayMap<Texture, Boolean> texturesToDispose = new ArrayMap<Texture, Boolean>();

		Gdx.app.log("Art", "Disposing Textures and Meshes");

		// Go collect all of the loaded textures
		for(Texture t : cachedTextures.values()) {
			texturesToDispose.put(t, true);
		}
		cachedTextures.clear();

		for(TextureAtlas atlas : TextureAtlas.cachedAtlases.values()) {
			if(atlas.texture != null)
				texturesToDispose.put(atlas.texture, true);

			atlas.didLoad = false;
		}

		// Dispose all the found textures
		for(int i = 0; i < texturesToDispose.size; i++) {
			Texture t = texturesToDispose.getKeyAt(i);
			t.dispose();
		}

		// Dispose cached meshes
		for(Mesh m : cachedMeshes.values()) {
			if(m != null) {
				m.dispose();
			}
		}
		cachedMeshes.clear();
		cachedMeshBounds.clear();

		// Might have some pooled meshes to clear
		CachePools.clearMeshPool();
	}

	public static void refresh() {
		try {
			Art.KillCache();

			Game.instance.loadManagers();
			GameManager.renderer.initTextures();
			GameManager.renderer.initShaders();

			GlRenderer.staticMeshPool.resetAndDisposeAllMeshes();
			Tesselator.tesselatorMeshPool.resetAndDisposeAllMeshes();

			// reset all drawables now that we've reset stuff
			for (Entity e : Game.GetLevel().entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}
			for (Entity e : Game.GetLevel().static_entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}
			for (Entity e : Game.GetLevel().non_collidable_entities) {
				if(e.drawable != null) {
					e.drawable.refresh();
					e.drawable.update(e);
				}
			}

			GameManager.renderer.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Game.GetLevel().isDirty = true;

			UiSkin.loadSkin();
		}
		catch(Exception ex) {
			Gdx.app.log("Delver", "Could not refresh: " + ex.getMessage());
		}
	}
}
