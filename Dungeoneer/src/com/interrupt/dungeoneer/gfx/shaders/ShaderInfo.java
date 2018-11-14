package com.interrupt.dungeoneer.gfx.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.FloatArray;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public class ShaderInfo {
	public ShaderProgram shader;
	
	int u_projectionViewMatrix;
	int u_texture;
	int u_fogStart;
	int u_fogEnd;
	int u_time;
	int u_AmbientColor;
	int u_FogColor;
	int u_DrawDistance;
	int u_UsedLights;
	int u_LightColors;
	int u_LightPositions;
	
	Matrix4 projectionView;
	int texture;
	float fogStart;
	float fogEnd;
	float time;
	float drawDistance;
	Color ambientColor = new Color();
	Color fogColor = new Color();
	boolean useLights = true;
	
	boolean hasBegun = false;

    ArrayMap<String, Integer> attributeLocations = new ArrayMap<String, Integer>();
    ArrayMap<Integer, Object> attributes = new ArrayMap<Integer, Object>();

    Texture[] textures = null;

    private static String[] TEXTURE_LOCATIONS = new String[] { "u_shader_texture_1", "u_shader_texture_2", "u_shader_texture_3", "u_shader_texture_4", "u_shader_texture_5" };

	public ShaderInfo(ShaderProgram shader) {
		this.shader = shader;
		init();
	}
	
	public void init() {
		u_projectionViewMatrix = shader.getUniformLocation("u_projectionViewMatrix");
		u_texture = shader.getUniformLocation("u_texture");
		u_fogStart = shader.getUniformLocation("u_fogStart");
		u_fogEnd = shader.getUniformLocation("u_fogEnd");
		u_time = shader.getUniformLocation("u_time");
		u_AmbientColor = shader.getUniformLocation("u_AmbientColor");
		u_FogColor = shader.getUniformLocation("u_FogColor");
		u_UsedLights = shader.getUniformLocation("u_UsedLights");
		u_DrawDistance = shader.getUniformLocation("u_drawDistance");
		
		u_LightColors = shader.getUniformLocation("u_LightColors[0]");
		if(u_LightColors == -1) u_LightColors = shader.getUniformLocation("u_LightColors");
		
		u_LightPositions = shader.getUniformLocation("u_LightPositions[0]");
		if(u_LightPositions == -1) u_LightPositions = shader.getUniformLocation("u_LightPositions");
	}
	
	public void setAttributes(Matrix4 projectionView, int texture, float fogStart, float fogEnd, float time, Color inAmbientColor, Color inFogColor) {
		this.projectionView = projectionView;
		this.texture = texture;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
		this.time = time;
		this.ambientColor.set(inAmbientColor);
		this.fogColor.set(inFogColor);

		if(GameManager.renderer != null) {
			this.drawDistance = Math.max(GameManager.renderer.camera.far, fogEnd);
		}

		useLights = true;
	}

    public void setAttribute(String name, Object value) {
        Integer location = attributeLocations.get(name);
        if(location == null) {
            location = shader.getUniformLocation(name);
            attributeLocations.put(name, location);
        }
        attributes.put(location, value);

        if(hasBegun) {
			Integer loc = attributeLocations.get(name);
			setShaderUniform(loc, value);
		}
    }
	
	public void setAttributes(Matrix4 projectionView, int texture, float fogStart, float fogEnd, float time, Color inAmbientColor, Color inFogColor, boolean useLights) {
		setAttributes(projectionView, texture, fogStart, fogEnd, time, inAmbientColor, inFogColor);
		this.useLights = useLights;
	}

	public void setTextures(String[] textureFiles, Texture.TextureFilter filtering) {
		if(textureFiles != null) {
			textures = new Texture[textureFiles.length];
			for (int i = 0; i < textures.length; i++) {
				try {
					Texture t = Art.loadTexture(textureFiles[i]);

					if(t != null) {
						t.setFilter(filtering, filtering);
					}

					textures[i] = t;
				}
				catch(Exception ex) {
					Gdx.app.error("Delver", "Was not able to load shader texture: " + textureFiles[i]);
				}
			}
		}
	}

	public void setShaderUniform(int loc, Object value) {
		try {
			if (value instanceof Integer) {
				shader.setUniformi(loc, (Integer) value);
			} else if (value instanceof Float) {
				shader.setUniformf(loc, (Float) value);
			} else if (value instanceof Vector2) {
				Vector2 val = (Vector2) value;
				shader.setUniformf(loc, val.x, val.y);
			} else if (value instanceof Vector3) {
				Vector3 val = (Vector3) value;
				shader.setUniformf(loc, val.x, val.y, val.z);
			} else if (value instanceof Color) {
				Color val = (Color) value;
				shader.setUniformf(loc, val.r, val.g, val.b, val.a);
			}
		}
		catch(Exception ex) {
			Gdx.app.log("Delver", "Error setting shader attribute");
		}
	}

	public void begin() {
		begin(0);
	}
	
	public void begin(int texOffset) {

		if(!hasBegun) {
			hasBegun = true;
			shader.begin();
		}

		if(projectionView != null) shader.setUniformMatrix(u_projectionViewMatrix, projectionView);
        shader.setUniformi(u_texture, 0);
        shader.setUniformf(u_fogStart, fogStart);
        shader.setUniformf(u_fogEnd, fogEnd);
        shader.setUniformf(u_time, time);
        shader.setUniformf(u_AmbientColor, ambientColor);
        shader.setUniformf(u_FogColor, fogColor.r, fogColor.g, fogColor.b, 1);
		shader.setUniformi(u_UsedLights, useLights ? GlRenderer.usedLights : 0);
        shader.setUniform4fv(u_LightColors, GlRenderer.lightColors, 0, GlRenderer.lightColors.length);
        shader.setUniform3fv(u_LightPositions, GlRenderer.lightPositions, 0, GlRenderer.lightPositions.length);
		shader.setUniformf(u_DrawDistance, drawDistance);

        if(textures != null) {
        	for(int i = textures.length; i > 0; i--) {
				Texture texture = textures[i - 1];
				if(texture != null) {
					texture.bind(i + texOffset);
					shader.setUniformi(TEXTURE_LOCATIONS[i - 1], i + texOffset);
				}
			}
		}

        // random attributes
        for(int i = 0; i < attributes.size; i++) {
            int loc = attributes.getKeyAt(i);
            Object value = attributes.getValueAt(i);
			setShaderUniform(loc, value);
        }
	}
	
	public void end() {
		hasBegun = false;
		shader.end();
	}

	public void updateLighting() {
		shader.setUniformi(u_UsedLights, useLights ? GlRenderer.usedLights : 0);
        shader.setUniform4fv(u_LightColors, GlRenderer.lightColors, 0, GlRenderer.lightColors.length);
        shader.setUniform3fv(u_LightPositions, GlRenderer.lightPositions, 0, GlRenderer.lightPositions.length);
	}
	
	public boolean hasBegun() {
		return hasBegun;
	}
}
