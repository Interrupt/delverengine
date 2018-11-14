package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.tiles.Tile;

public class Model extends DirectionalEntity {
	
	@EditorProperty( group = "Visual", type = "FILE_PICKER", params = "meshes")
	public String meshFile = null;
	
	@EditorProperty( group = "Visual", type = "FILE_PICKER", params = "")
	public String textureFile = "meshes.png";

	@EditorProperty( group = "Visual")
	public boolean receiveDecals = true;

	@EditorProperty( group = "Visual")
	public boolean bakeLighting = false;
	
	public transient String lastMeshFile = null;
    public transient String lastTextureFile = null;
	
	public Model() {
		
	}
	
	public Model(String meshFile) {
		this.meshFile = meshFile;
	}
	
	public Model(String meshFile, String textureFile) {
		this.meshFile = meshFile;
		this.textureFile = textureFile;
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		if(isActive) super.tick(level, delta);
	}
	
	public void updateDrawable() {
		if((meshFile == null || (lastMeshFile != meshFile)) || (textureFile == null || (lastTextureFile != textureFile))) {
			
			String pickedMeshFile = meshFile;
			if(meshFile.contains(",")) {
				String[] files = meshFile.split(",");
				pickedMeshFile = files[Game.rand.nextInt(files.length)];
			}
			
			String pickedTextureFile = textureFile;
			if(textureFile.contains(",")) {
				String[] files = textureFile.split(",");
				pickedTextureFile = files[Game.rand.nextInt(files.length)];
			}
			
			drawable = new DrawableMesh(pickedMeshFile, pickedTextureFile);
		}
		
		if(drawable != null && drawable instanceof DrawableMesh) {
			DrawableMesh drbl = (DrawableMesh)drawable;
			drbl.rotX = rotation.x;
			drbl.rotY = rotation.y;
			drbl.rotZ = rotation.z;
			drbl.scale = scale;
			drbl.isStaticMesh = !isDynamic && !bakeLighting;
			drbl.drawOffset.z = yOffset;
			drbl.bakeLighting = bakeLighting;
			
			drawable.update(this);
		}
		
		lastMeshFile = meshFile;
        lastTextureFile = textureFile;
	}
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		
		if(drawable != null && source != Source.EDITOR) {
            if(drawable instanceof DrawableMesh) {
                DrawableMesh drawableMesh = (DrawableMesh) drawable;
                meshFile = drawableMesh.meshFile;
                textureFile = drawableMesh.textureFile;
                drawableMesh.isStaticMesh = !isDynamic;
				drawableMesh.addCollisionTriangles = receiveDecals;
            }
		}
	}

	@Override
	public void resetDrawable() {
		lastMeshFile = null;
		lastTextureFile = null;
		super.updateDrawable();
	}
}
