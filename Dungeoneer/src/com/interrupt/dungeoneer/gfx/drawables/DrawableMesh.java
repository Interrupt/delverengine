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
	/** Mesh filepath. */
	public String meshFile = "meshes/chair.obj";

	/** Mesh texture filepath. */
	public String textureFile = "meshes.png";
	
	public transient Mesh loadedMesh;
	public transient Texture loadedTexture;
	
	public transient boolean couldLoad = true;
	
	private transient Matrix4 modelView = null;
	private transient Matrix4 combined = new Matrix4();

	public transient Vector3 scaleVector = null;

	/** Position x-coordinate. */
	public float x = 0;

	/** Position y-coordinate. */
	public float y = 0;

	/** Position z-coordinate. */
	public float z = 0;

	/** Rotation x-coordinate. */
	public float rotX = 0;

	/** Rotation y-coordinate. */
	public float rotY = 0;

	/** Rotation z-coordinate. */
	public float rotZ = 0;
	
	public transient BoundingBox bbox = null;
	private transient BoundingBox frustrumCheckBox = new BoundingBox();

	/** Is mesh static. Static meshes are combined and drawn more efficiently. */
	public boolean isStaticMesh = false;

    public boolean addCollisionTriangles = false;

    public boolean bakeLighting = false;
    private transient Mesh bakedMesh = null;
    private transient Vector3 bakedRotation = new Vector3(0, 0, 0);

    // Scratch work quaternions and vectors
    private transient Quaternion workQuat1 = new Quaternion();
    private transient Quaternion workQuat2 = new Quaternion();
    private transient Vector3 workVec1 = new Vector3();
    private transient Vector3 workVec2 = new Vector3();


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
		z = e.z + e.yOffset;
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
		if (modelView == null) modelView = new Matrix4();
		modelView.setToTranslation(x + drawOffset.x, z - 0.5f + drawOffset.z, y + drawOffset.y);

		// Rotate to face the direction, this time using Quaternions to support better rotation
		Vector3 tmp = workVec1.set(Vector3.Y).crs(dir).nor();
		Vector3 tmp2 = workVec2.set(dir).crs(tmp).nor();
		workQuat1.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);

		// Old way was flipped, new way should also be to not break old stuff
		workQuat2.set(Vector3.Y, 180f);
		workQuat1.mul(workQuat2);

		// Apply the quaternion rotation, as well as any other axis based rotation
		modelView.rotate(workQuat1);
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
