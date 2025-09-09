package com.interrupt.dungeoneer.gfx.shaders;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.interrupt.dungeoneer.gfx.GlRenderer;

public class WaterShaderInfo extends ShaderInfo {
	
	int u_waterSpeed;
	float scrollSpeed = 0;
	
	public WaterShaderInfo(ShaderProgram shader) {
		super(shader);
	}
	
	@Override
	public void init() {
		super.init();
		u_waterSpeed = shader.getUniformLocation("u_waterSpeed");
	}
	
	public void setAttributes(Matrix4 projectionView, int texture, float fogStart, float fogEnd, float time, Color ambientColor, Color fogColor, float scrollSpeed) {
		super.setAttributes(projectionView, texture, fogStart, fogEnd, time, ambientColor, fogColor);
		this.scrollSpeed = scrollSpeed;
	}
	
	public void setScrollSpeed(float scrollSpeed) {
		this.scrollSpeed = scrollSpeed;
	}
	
	@Override
	public void begin() {
		super.begin();
		shader.setUniformf(u_waterSpeed, scrollSpeed);
	}
}
