package com.interrupt.dungeoneer.gfx.drawables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Breakable;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.managers.ShaderManager;

public class DrawableMesh extends Drawable {
	public String meshFile = "meshes/chair.obj";
	public String textureFile = "meshes.png";
	
	public transient Mesh loadedMesh;
	public transient Texture loadedTexture;
	
	public transient boolean couldLoad = true;
	
	private transient Matrix4 modelView = null;
	private transient Matrix4 combined = new Matrix4();
	private transient Matrix4 workspace = new Matrix4();
	private transient Vector3 workVec = new Vector3();

	public transient Vector3 scaleVector = null;
	
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public float rotX = 0;
	public float rotY = 0;
	public float rotZ = 0;
	
	public transient BoundingBox bbox = null;
	private transient BoundingBox frustrumCheckBox = new BoundingBox();
	
	public boolean isStaticMesh = false;
    public boolean addCollisionTriangles = false;

    public boolean bakeLighting = false;
    private transient Mesh bakedMesh = null;
    private transient Vector3 bakedRotation = new Vector3(0, 0, 0);

    public DrawableMesh() { }

	public DrawableMesh(String meshFile, String textureFile) {
		this.meshFile = meshFile;
		this.textureFile = textureFile;
	}
	
	public void draw (PerspectiveCamera camera, Color color, ShaderInfo shader, float fogStart, float fogEnd, Color fogColor, float time) {
		
		this.color = color;
		
		loadedTexture = Art.cachedTextures.get(textureFile);
		if(loadedTexture == null) {
			loadedTexture = Art.loadTexture(textureFile);
		}
		if(loadedTexture != null) GlRenderer.bindTexture(loadedTexture);

		if(bakeLighting && bakedMesh == null && Game.instance != null) {
			bakedMesh = GlRenderer.bakeMesh(Game.instance.level, this);
			bakedRotation.set(rotX, rotY, rotZ);
			bbox = bakedMesh.calculateBoundingBox();
		}

		if(shader != null) {
			combined.set(camera.combined).mul(modelView);

			ShaderInfo shaderOverride = null;

			if(bakedMesh != null && this.shader == null) {
				this.shader = "main";
			}

			if (this.shader != null && !this.shader.isEmpty()) {
				shaderOverride = ShaderManager.getShaderManager().getCompiledShader(this.shader);
			}

			if (shaderOverride != null) shader = shaderOverride;

			if(GameManager.renderer.renderingForPicking) {
				shader = ShaderManager.getShaderManager().getCompiledShader("mesh-picking");
			}

			if(bakedMesh != null && !GameManager.renderer.renderingForPicking) {
				shader.setAttributes(combined, 0, fogStart, fogEnd, time, Color.BLACK, fogColor, !fullbrite);
				shader.begin();
				try {
					bakedMesh.render(shader.shader, GL20.GL_TRIANGLES);
				}
				catch(Exception ex) {
					Gdx.app.log("DrawableMesh", ex.getMessage());
					bakedMesh = null;
				}
			}
			else if(loadedMesh != null) {
				shader.setAttributes(combined, 0, fogStart, fogEnd, time, color, fogColor, !fullbrite);
				shader.begin();
				try {
					loadedMesh.render(shader.shader, GL20.GL_TRIANGLES);
				}
				catch(Exception ex) {
					Gdx.app.log("DrawableMesh", ex.getMessage());
					loadedMesh = null;
				}
			}

			shader.end();
		}
	}
	
	public boolean isInFrustrum(PerspectiveCamera camera) {
		
		if(bbox != null) {
			frustrumCheckBox.set(bbox);
			frustrumCheckBox.mul(modelView);
			
			return camera.frustum.boundsInFrustum(frustrumCheckBox);
		}
		
		return true;
	}
	
	public void update(Entity e) {
		x = e.x;
		y = e.y;
		z = e.z;
		fullbrite = e.fullbrite;
		scale = e.scale;
		color = e.color;
		shader = e.getShader();
		update();
	}
	
	public void update() {

    	// Refresh everything
    	if(isDirty) {
    		isDirty = false;
    		loadedMesh = null;
    		couldLoad = true;
		}

		// make the model view
		Vector3 dirWithoutY = workVec.set(dir.x,0,dir.z);
		if(modelView == null) modelView = new Matrix4();

		modelView.setToTranslation(x + drawOffset.x, z - 0.5f + drawOffset.z, y + drawOffset.y);

		modelView.mul(workspace.setToLookAt(dirWithoutY, Vector3.Y));
		modelView.mul(workspace.setToRotation(Vector3.X, dir.y * -90f));
			
		modelView.rotate(Vector3.Y, rotZ - bakedRotation.z);
		modelView.rotate(Vector3.X, rotX - bakedRotation.x);
		modelView.rotate(Vector3.Z, rotY - bakedRotation.y);

		if(scaleVector != null) modelView.scale(scaleVector.x, scaleVector.y, scaleVector.z);
		else modelView.scale(scale, scale, scale);
			
		// load if not already
		if(loadedMesh == null && couldLoad) {
			try {
				loadedMesh = Art.loadObjMesh(meshFile);

				if(loadedMesh != null) {
					bbox = Art.getCachedMeshBounds(meshFile);
				}
				
				loadedTexture = Art.loadTexture(textureFile);

				// Uhoh!
				if(loadedMesh == null) {
					loadedMesh = Art.loadObjMesh("meshes/error_missing.obj");
				}
			}
			catch (Exception ex) {
				couldLoad = false;
			}
		}

		if(!bakeLighting && bakedMesh != null) {
			bakedMesh = null;
		}
	}

	public void setScaleVector(Vector3 scale) {
		if(scaleVector == null) {
			scaleVector = new Vector3(scale);
		}
		else {
			scaleVector.set(scale);
		}
	}
}
