package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.drawables.DrawableProjectedDecal;

public class ProjectedDecal extends DirectionalEntity {
	/** Direction to orient decal */
	@EditorProperty
	public Vector3 direction = new Vector3(0,0,0);
	
	public transient Camera perspective;

	/** Is projection orthographic? */
	@EditorProperty
	public boolean isOrtho = true;
	public transient boolean wasOrtho = true;

	/** Near clipping plane of projection. */
	@EditorProperty
	public float start = 0.2f;

	/** Far clipping plane of projection. */
	@EditorProperty
	public float end = 10f;

	/** Field of view for projection. */
	@EditorProperty
	public float fieldOfView = 20f;

	/** Width of decal. */
	@EditorProperty
	public float decalWidth = 0.99f;

	/** Height of decal. */
	@EditorProperty
	public float decalHeight = 0.99f;
	
	public ProjectedDecal() {
		artType = ArtType.texture;
		end = 3f;
		isDynamic = false;
	}
	
	public ProjectedDecal(ArtType artType, int tex, float size) {
		this.artType = artType;
		this.tex = tex;
		decalWidth = size;
		decalHeight = size;
		isDynamic = false;
	}
	
	public ProjectedDecal(Camera perspective) {
		drawable = new DrawableProjectedDecal();
		this.perspective = perspective;
		isDynamic = false;
	}
	
	public ProjectedDecal(ProjectedDecal decal) {
		artType = decal.artType;
		tex = decal.tex;
		spriteAtlas = decal.spriteAtlas;
		decalWidth = decal.decalWidth;
		decalHeight = decal.decalHeight;
		isDynamic = false;
	}

	@Override
	public void tick(Level level, float delta)
	{
		if(isDynamic) super.tick(level, delta);
	}
	
	public void updateDrawable() {
		if(perspective == null || isOrtho != wasOrtho) {
			if(!isOrtho)
				perspective = new PerspectiveCamera();
			else
				perspective = new OrthographicCamera();
			
			wasOrtho = isOrtho;
		}
		
		if(drawable == null) {
			drawable = new DrawableProjectedDecal();
		}
		
		// update only when we need to
		if(((DrawableProjectedDecal)drawable).generatedMesh == null) {
			if(perspective != null) {
				
				if(perspective instanceof PerspectiveCamera) {
					((PerspectiveCamera)perspective).fieldOfView = fieldOfView;
				}
				
				Vector2 clippedSize = TextureAtlas.getCachedRegion(spriteAtlas != null ? spriteAtlas : artType.toString()).getClippedSizeMod(tex);
				
				perspective.near = start;
				perspective.far = end;
				perspective.viewportHeight = decalHeight * clippedSize.y;
				perspective.viewportWidth = decalWidth * clippedSize.x;
				perspective.position.set(x, z, y);
				
				perspective.up.set(0, 1, 0);
				
				perspective.direction.set(direction.x, direction.z, direction.y);
				if(direction.x == 0 && direction.y == 0 && direction.z == 0) perspective.direction.set(0.0001f, 0, 0);
				
				perspective.rotate(roll, perspective.direction.x, perspective.direction.y, perspective.direction.z);
				perspective.rotate(Vector3.X, rotation.x);
				perspective.rotate(Vector3.Z, rotation.y);
				perspective.rotate(Vector3.Y, rotation.z);
				
				perspective.update();
			}
			
			if(drawable != null) {
				DrawableProjectedDecal drbl = (DrawableProjectedDecal)drawable;
                drbl.atlas = TextureAtlas.getCachedRegion(spriteAtlas != null ? spriteAtlas : artType.toString());
				drbl.perspective = perspective;
				drbl.update(this);
			}
		}
	}
	
	public void refresh() {
		perspective = null;
		
		if(drawable != null) {
			DrawableProjectedDecal drbl = (DrawableProjectedDecal)drawable;
			
			if(drbl.generatedMesh != null) drbl.generatedMesh.dispose();
			drbl.generatedMesh = null;
		}
	}
	
	/*@Override
	public void rotate90() {
		direction.rot(new Matrix4().rotate(Vector3.Z, 90f));
	}

	@Override
	public void rotate90Reversed() {
		direction.rot(new Matrix4().rotate(Vector3.Z, -90f));
	}*/
	
	public BoundingBox getFrustumBounds() {
		BoundingBox b = CachePools.getAABB(this);
		
		if(perspective != null && perspective.frustum != null) {
			Vector3 temp = new Vector3();
			for(Vector3 vec : perspective.frustum.planePoints) {
				temp.x = vec.x;
				temp.y = vec.y;
				temp.z = vec.z;
				
				b.ext(temp);
			}
		}
		
		b.min.y = 0;
		b.max.y = 0;
		
		return b;
	}
}
