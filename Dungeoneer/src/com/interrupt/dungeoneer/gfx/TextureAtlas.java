package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.managers.ShaderManager;

/** Class for working with texture atlases. */
public class TextureAtlas {
	/** Size of sprite in pixels. */
	public int spriteSize = 16;

	/** Number of columns. */
	public int columns = 16;

	/** Number of rows to split texture into. */
	public int rows = 1;

	/** Height to width ratio of sprite. */
	public int rowScale = 1;

	/** Filepath to texture file. Relative to asset directory. */
	public String filename = "";

	/** Internal name used to reference this object. */
	public String name = "";

	/** Name of shader to be used for this object. */
	public String shader = null;

	/** Scale of sprite. */
	public float scale = 1;

	/** Y-axis offset of sprite. */
	public float y_offset = 0;
	
	public Texture texture = null;

	private TextureRegion[] sprite_regions = null;
	private TextureRegion[] clipped_regions = null;
	private Vector2[] sprite_offsets = null;
	private Vector2[] clipped_size_mod = null;

	/** Use bilinear texture filtering. */
	public boolean filter = false;

	public boolean isRepeatingAtlas = false;
	public boolean didLoad = false;
	
	public TextureAtlas() { }
	
	public static ArrayMap<String, TextureAtlas> cachedAtlases = new ArrayMap<String, TextureAtlas>();
	public static TextureAtlas getCachedRegion(String cachekey) {
		TextureAtlas found = cachedAtlases.get(cachekey);

		if(found != null && !found.didLoad) {
			found.loadIfNeeded();
		}

		return found;
	}
    public static void cacheAtlas(TextureAtlas atlas, String cachekey) {
        cachedAtlases.put(cachekey, atlas);
    }

    public static ArrayMap<String, TextureAtlas> cachedRepeatingAtlases = new ArrayMap<String, TextureAtlas>();
    public static TextureAtlas getCachedRepeatingRegion(String cachekey) { return cachedRepeatingAtlases.get(cachekey); }
    public static void cacheRepeatingAtlas(TextureAtlas atlas, String cachekey) { cachedRepeatingAtlases.put(cachekey, atlas); }

    public static Array<TextureAtlas> getAllRepeatingAtlases() {
        Array<TextureAtlas> allAtlases = new Array<TextureAtlas>();
        for(int i = 0; i < cachedRepeatingAtlases.size; i++) {
            allAtlases.add(cachedRepeatingAtlases.getValueAt(i));
        }
        return allAtlases;
    }

    public static Array<TextureAtlas> getAllSpriteAtlases() {
        Array<TextureAtlas> allAtlases = new Array<TextureAtlas>();
        for(int i = 0; i < cachedAtlases.size; i++) {
            allAtlases.add(cachedAtlases.getValueAt(i));
        }
        return allAtlases;
    }

    public static TextureAtlas getAtlasByIndex(byte index) {
        return cachedAtlases.getValueAt(index);
    }

    public static TextureAtlas getRepeatingAtlasByIndex(String index) {
    	TextureAtlas atlas = cachedRepeatingAtlases.get(index);
		if(atlas != null) {
			atlas.loadIfNeeded();
			return atlas;
		}

		Gdx.app.log("TextureAtlas", index + " not found");
        return cachedRepeatingAtlases.firstValue();
    }
    public static TextureAtlas getSpriteAtlasByIndex(String index) {
        return cachedAtlases.get(index);
    }

    public static TextureAtlas bindTextureAtlasByIndex(byte index) {
        TextureAtlas atlas = cachedAtlases.getValueAt(index);
        if(atlas != null && atlas.texture != null) atlas.texture.bind();
        return atlas;
    }

    public static TextureAtlas bindRepeatingTextureAtlasByIndex(String index) {
        TextureAtlas atlas = cachedRepeatingAtlases.get(index);
        if(atlas != null && atlas.texture != null) {
        	GlRenderer.bindTexture(atlas.texture);
		}
		return atlas;
    }
	
	public TextureAtlas(int columns, String filename, float scale) {
		this.columns = columns;
		this.filename = filename;
		this.scale = scale;
		load();
	}
	
	public TextureAtlas(int columns, Texture texture, Pixmap pixmap) {
		this.texture = texture;
		this.columns = columns;
		generateAtlas(pixmap);
	}
	
	public void load() {
		texture = Art.loadTexture(filename);
		Pixmap pixmap = Art.loadPixmap(filename);
		generateAtlas(pixmap);
        pixmap.dispose();
	}

    public void loadRepeating() {
        Pixmap pixmap = Art.loadPixmap(filename);
        spriteSize = pixmap.getWidth() / columns;

        // Clamp to a valid size
        if(rowScale < 1) {
        	rowScale = 1;
		}

        int spriteVerticalSize = spriteSize * rowScale;

        final int atlasHeight = pixmap.getHeight() / spriteVerticalSize;
        int next_pow = GetNextPowerOf2(columns * atlasHeight);
        Pixmap remappedWall = new Pixmap(spriteSize * next_pow, spriteVerticalSize, Pixmap.Format.RGBA8888);

        // redraw them horizontally
        int pos = 0;
        for(int y = 0; y < atlasHeight; y++) {
            for(int x = 0; x < columns; x++) {
                remappedWall.drawPixmap(pixmap, pos * spriteSize, 0, x * spriteSize, y * spriteVerticalSize, spriteSize, spriteVerticalSize);
                pos++;
            }
        }

        texture = new Texture(remappedWall);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        if(filter) texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        sprite_regions = new TextureRegion[columns * atlasHeight];
        sprite_offsets = new Vector2[columns * atlasHeight];
        clipped_size_mod = new Vector2[columns * atlasHeight];

        float xMod = (1f / pos) * 0.001f;

        for(int i = 0; i < sprite_regions.length; i++) {
            TextureRegion wallRegion = new TextureRegion(texture, i * spriteSize, 0, spriteSize, spriteVerticalSize);

            // clamp the region some
            wallRegion.setU(wallRegion.getU() + xMod);
            wallRegion.setU2(wallRegion.getU2() - xMod * 2f);

            sprite_regions[i] = wallRegion;
            sprite_offsets[i] = new Vector2();
            clipped_size_mod[i] = new Vector2();
        }

        clipped_regions = sprite_regions;
        pixmap.dispose();
    }
	
	public void generateAtlas(Pixmap pixmap) {
		spriteSize = texture.getWidth() / columns;
		int numRows = texture.getHeight() / spriteSize;

		rows = numRows;
		
		sprite_regions = new TextureRegion[columns * numRows];
		clipped_regions = new TextureRegion[columns * numRows];
		sprite_offsets = new Vector2[columns * numRows];
		clipped_size_mod = new Vector2[columns * numRows];
		
		for(int x = 0; x < columns; x++) {
			for(int y = 0; y < numRows; y++) {
				TextureRegion region = new TextureRegion(texture, x * spriteSize, y * spriteSize, spriteSize, spriteSize);
				TextureRegion clipped_region = new TextureRegion();
				
				Vector2 offset = new Vector2(0, 0);
				
				int num = x + y * columns;
				
				float xMod = (1f / columns) * 0.001f;
				float yMod = (1f / numRows) * 0.001f;
				
				// clamp the region to visible pixels if possible
				if(pixmap != null) {
					clipped_region = clampRegion(region, pixmap);
					clipped_regions[num] = clipped_region;
					
					float regStartX = clipped_region.getRegionX() % spriteSize;
					float regEndX = regStartX + (clipped_region.getRegionWidth() % spriteSize);
					if(clipped_region.getRegionWidth() == spriteSize) regEndX = regStartX + spriteSize;
					
					// find the new center
					float xRegCenter = (float)(regEndX - regStartX) / 2f;
					// convert into 0 - 1 space
					xRegCenter = (xRegCenter + regStartX) / (float)spriteSize;
					
					// get Y offset
					float spriteY = (clipped_region.getRegionY() / (float)spriteSize + clipped_region.getRegionHeight() / ((float)spriteSize*2)) - 0.5f;
					spriteY -= (num) / columns;
					
					offset.x = 0.5f - xRegCenter;
					offset.y = spriteY *= 1f;
					
					// clamp the region some
					clipped_region.setU(clipped_region.getU() + xMod);
					clipped_region.setV(clipped_region.getV() + yMod);
					clipped_region.setU2(clipped_region.getU2() - xMod * 2f);
					clipped_region.setV2(clipped_region.getV2() - yMod * 2f);
				} else {
					clipped_regions[num] = region;
				}
				
				clipped_size_mod[num] = new Vector2(clipped_regions[num].getRegionWidth() / (float)spriteSize, clipped_regions[num].getRegionHeight() / (float)spriteSize);
				
				// clamp the region some
				region.setU(region.getU() + xMod);
				region.setV(region.getV() + yMod);
				region.setU2(region.getU2() - xMod * 2f);
				region.setV2(region.getV2() - yMod * 2f);
				
				sprite_regions[num] = region;
				sprite_offsets[num] = offset;
			}
		}
		
		if(filter) texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	public void loadIfNeeded() {
		if(!didLoad) {
			if(isRepeatingAtlas)
				loadRepeating();
			else
				load();

			didLoad = true;
		}
	}
	
	public TextureRegion getSprite(int num) {
		loadIfNeeded();
        if(num >= sprite_regions.length) return sprite_regions[0];
		return sprite_regions[num];
	}
	
	public TextureRegion getClippedSprite(int num) {
		loadIfNeeded();
		if(num >= clipped_regions.length) return clipped_regions[0];
    	return clipped_regions[num];
	}
	
	public Vector2 getSpriteOffset(int num) {
		loadIfNeeded();
		if(num >= sprite_offsets.length) return sprite_offsets[0];
    	return sprite_offsets[num];
	}

	public Vector2 getClippedSizeMod(int num) {
		loadIfNeeded();
		if(num >= clipped_size_mod.length) return clipped_size_mod[0];
		return clipped_size_mod[num];
	}
	
	// trim whitespace from a region
	protected TextureRegion clampRegion(TextureRegion region, Pixmap baseTexture) {
		TextureRegion inRegion = new TextureRegion(region);
		
    	int startX = inRegion.getRegionX();
    	int endX = startX + inRegion.getRegionWidth();
    	
    	int startY = inRegion.getRegionY();
    	int endY = startY + inRegion.getRegionHeight();
    	
    	int foundStartX = endX;
    	int foundStartY = endY;
    	int foundEndX = startX;
    	int foundEndY = startY;
    	
    	for(int x = startX; x < endX; x++) {
    		for(int y = startY; y < endY; y++) {
    			int pixel = baseTexture.getPixel(x, y);
				int a = pixel & 0xff;
				
				if(a > 10) {
					if(x < foundStartX) foundStartX = x;
					if(y < foundStartY) foundStartY = y;
					if(x > foundEndX) foundEndX = x;
					if(y > foundEndY) foundEndY = y;
				}
    		}
    	}
    	
    	inRegion.setRegion(foundStartX, foundStartY, (foundEndX + 1) - foundStartX, (foundEndY + 1) - foundStartY);
    	
    	return inRegion;
    }

    public TextureRegion[] getSpriteRegions() {
    	if(!didLoad) {
    		loadIfNeeded();
		}
    	return sprite_regions;
	}

	public TextureRegion[] getClippedRegions() {
		if(!didLoad) {
			loadIfNeeded();
		}
    	return clipped_regions;
	}

	public Vector2[] getSpriteOffsets() {
		if(!didLoad) {
			loadIfNeeded();
		}
    	return sprite_offsets;
	}

	public Vector2[] getClippedSizeMod() {
		if(!didLoad) {
			loadIfNeeded();
		}
    	return clipped_size_mod;
	}

	public int getTotalRegions() {
    	if(sprite_regions == null)
    		return 0;

		return sprite_regions.length;
	}

	public ShaderInfo getShader() {
    	if(shader == null || shader.isEmpty())
    		return null;

		// If a custom shader is set for the atlas, try to return or load it
		return ShaderManager.getShaderManager().getCompiledShader(shader);
	}

    public int GetNextPowerOf2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;

        return v;
    }

    @Override
    public String toString() {
        return name;
    }
}
