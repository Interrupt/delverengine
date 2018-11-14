package com.interrupt.dungeoneer.gfx.drawables;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Intersector.SplitTriangle;
import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;

public class DrawableProjectedDecal extends Drawable {
	public transient Mesh generatedMesh;
	public transient Camera perspective;
	public transient Matrix4 combined = new Matrix4();
	public transient boolean hasMesh = false;
	public transient Entity owner = null;
	
	public transient TextureAtlas atlas = null;

    public static transient float ProjectedDecalOffsetMod = 0.005f;
    public static transient float ProjectedDecalNormalMod = 0.4f;
	
	// working vars
	private static transient SplitTriangle split = new SplitTriangle(3);
	private static transient Array<Vector3> vertsBehind = new Array<Vector3>();
	private static transient Frustum r = new Frustum();
	private static transient Array<Vector3> triangles = new Array<Vector3>();
	private static transient Array<Vector3> filteredList = new Array<Vector3>(200);
	private static transient Color workColor = new Color();
	private static transient Vector3 workVector1 = new Vector3();
	private static transient Vector3 workVector2 = new Vector3();
	private static transient Vector3 workVector3 = new Vector3();
	private static transient Array<Vector3> usedVectors = new Array<Vector3>();
	private static Pool<Vector3> vector3Pool = new Pool<Vector3>(32) {
    	@Override
        protected Vector3 newObject () {
                return new Vector3();
        }
    };
	
	public DrawableProjectedDecal() { }

	public DrawableProjectedDecal(Camera perspective) {
		this.perspective = perspective;
	}
	
	public Vector3 getWorkVector() {
		Vector3 vec = vector3Pool.obtain();
		vec.set(0,0,0);
		usedVectors.add(vec);
		return vec;
	}
	
	public void freeWorkVector(Vector3 v) {
		if(usedVectors.contains(v, true)) vector3Pool.free(v);
		usedVectors.removeValue(v, true);
	}
	
	public void freeWorkVectors() {
		vector3Pool.freeAll(usedVectors);
		usedVectors.clear();
	}
	
	public void draw (PerspectiveCamera camera, Color color, ShaderInfo shader, float fogStart, float fogEnd, Color fogColor, float time) {
		
		if(!hasMesh) return;
		
		if (generatedMesh != null && shader != null) {			
			combined.set(camera.combined);
			
			shader.setAttributes(combined, 0, fogStart, fogEnd, time, (fullbrite) ? color : Color.BLACK, fogColor, !fullbrite && GameManager.renderer.enableLighting);
	        shader.begin();
	        generatedMesh.render(shader.shader, GL20.GL_TRIANGLES);
	        shader.end();
			
		}
	}
	
	public boolean isInFrustrum(PerspectiveCamera camera) {
		return true;
	}
	
	public void update(Entity e) {
		fullbrite = e.fullbrite;
		artType = e.artType;

		if(atlas == null) {
            if(e.spriteAtlas != null) {
                atlas = TextureAtlas.getCachedRegion(e.spriteAtlas);
            }
            else {
                refreshTextureAtlas();
            }
        }
		owner = e;
	}
	
	public void projectDecal(Array<Vector3> tris, Level level, TextureRegion region) {
		
		hasMesh = false;
		vertsBehind.clear();
		triangles.clear();
		filteredList.clear();
		
		Camera p = perspective;
        p.update();
		
		r.update(p.invProjectionView);
		
		int trisSize = tris.size;
		for(int i = 0; i < trisSize; i++) { filteredList.add(tris.get(i)); }
				
		for(Plane testplane : r.planes) {
			
			triangles.clear();
			triangles.addAll(filteredList);
			filteredList.clear();
			
			for(int i = 0; i < triangles.size; i += 3) {
				Vector3 p1 = triangles.get(i);
				Vector3 p2 = triangles.get(i + 1);
				Vector3 p3 = triangles.get(i + 2);
				
				float[] triangle = new float[] { p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z };
				
				Intersector.splitTriangle(triangle, testplane, split);
				
				for(int v = 0; v < split.numFront * 9; v += 9) {
					Vector3 cut1 = getWorkVector().set(split.front[v], split.front[v + 1], split.front[v + 2]);
					Vector3 cut2 = getWorkVector().set(split.front[v + 3], split.front[v + 4], split.front[v + 5]);
					Vector3 cut3 = getWorkVector().set(split.front[v + 6], split.front[v + 7], split.front[v + 8]);
					
					filteredList.add(cut1);
					filteredList.add(cut2);
					filteredList.add(cut3);
				}
			}
		}
		
		for(int i = 0; i < filteredList.size; i += 3) {
			Vector3 p1 = filteredList.get(i);
			Vector3 p2 = filteredList.get(i + 1);
			Vector3 p3 = filteredList.get(i + 2);
			
			Vector3 c1 = workVector1.set(p2).sub(p1);
			Vector3 c2 = workVector2.set(p3).sub(p1);
			Vector3 nor = c2.crs(c1).nor();
			
			float dp = nor.dot(workVector3.set(p.direction.nor())) * -1f;
			
			if(dp > ProjectedDecalNormalMod) {
				vertsBehind.add(p1);
				vertsBehind.add(p2);
				vertsBehind.add(p3);
			}
		}
		
		generatedMesh = new Mesh(Mesh.VertexDataType.VertexArray, false, vertsBehind.size, vertsBehind.size,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE  + "0"));
		
		short[] indices = new short[vertsBehind.size];
		for (int i = 0; i < vertsBehind.size; i++) {
			indices[i] = (short)(i);
		}
		generatedMesh.setIndices(indices);
		
		float[] vertices = new float[vertsBehind.size * 6];
		
		int vertnum = 0;
		for(int i = 0; i < vertsBehind.size; i++) {
			Vector3 currentVert = vertsBehind.get(i);
			vertices[vertnum++] = currentVert.x - perspective.direction.x * ProjectedDecalOffsetMod;
			vertices[vertnum++] = currentVert.y - perspective.direction.y * ProjectedDecalOffsetMod;
			vertices[vertnum++] = currentVert.z - perspective.direction.z * ProjectedDecalOffsetMod;
			
			workColor.set(Color.WHITE);
			
			if(!fullbrite && GameManager.renderer.enableLighting) {
				level.getLightColorAt(currentVert.x, currentVert.z, currentVert.y, null, workColor);
				workColor.a = 1;

				vertices[vertnum++] = workColor.toFloatBits();
			}
			else
				vertices[vertnum++] = Color.WHITE.toFloatBits();
			
			float frustLength = p.far - p.near;
			float frontWidth = workVector1.set(r.planePoints[0]).add(workVector2.set(r.planePoints[1]).scl(-1f)).len();
			float backWidth = workVector1.set(r.planePoints[4]).add(workVector2.set(r.planePoints[5]).scl(-1f)).len();
			float frontHeight = workVector1.set(r.planePoints[0]).add(workVector2.set(r.planePoints[3]).scl(-1f)).len();
			float backHeight = workVector1.set(r.planePoints[4]).add(workVector2.set(r.planePoints[7]).scl(-1f)).len();
			
			float distmod = r.planes[0].distance(currentVert) / frustLength;
			float thisWidth = (backWidth - frontWidth) * distmod + frontWidth;
			float thisHeight = (backHeight - frontHeight) * distmod + frontHeight;
			
			float u1 = r.planes[2].distance(currentVert) / (thisWidth);
			float v1 = r.planes[4].distance(currentVert) / (thisHeight);
			
			vertices[vertnum++] = (1 - u1) * region.getU() + u1 * region.getU2();
			vertices[vertnum++] = (1 - v1) * region.getV() + v1 * region.getV2();
		}
		
		if(vertnum > 0)
			generatedMesh.setVertices(vertices, 0, vertnum);
		
		hasMesh = vertnum > 0;
		
		freeWorkVectors();
	}
	
	public void refresh() {
		if(generatedMesh != null) generatedMesh.dispose();
        atlas = null;
		generatedMesh = null;
	}

	public void refreshTextureAtlas() {
		atlas = TextureAtlas.getCachedRegion(artType.toString());
	}
}
