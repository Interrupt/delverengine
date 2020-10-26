package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.*;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;
import com.interrupt.helpers.WaterEdges;
import com.interrupt.managers.TileManager;

public class Tesselator {

	public class TuplePool extends Pool<FloatTuple> {
		@Override
		protected FloatTuple newObject() {
			return new FloatTuple();
		}

		private Array<FloatTuple> used = new Array<FloatTuple>();

		public FloatTuple get(float x, float y) {
			FloatTuple t = obtain();
			t.set(x, y);

			if(!used.contains(t, true))
				used.add(t);

			return t;
		}

		public void freeAll() {
			freeAll(used);
			used.clear();
		}
	}

	private FloatArray verts = new FloatArray(500);
	private ShortArray indices = new ShortArray(500);
	
	private Mesh mesh;
	private boolean hasBuilt = false;
	private int builtLength;
	private int indx;
	public Array<Vector3> collisionTriangles;
	
	// init some calculation stuff we'll need
	private Vector3 normal = new Vector3(Vector3.Z);
	private FloatTuple vert_uv1 = new FloatTuple(), vert_uv2 = new FloatTuple(), vert_uv3 = new FloatTuple(), vert_uv4 = new FloatTuple();
	
	// a wall is a pair of heights to start drawing at, and a pair of end heights
	private Array<FloatTuple> starts = new Array<FloatTuple>();
	private Array<FloatTuple> ends = new Array<FloatTuple>();
	private FloatTuple x_offsets = new FloatTuple(0f, 1f);
	private FloatTuple y_offsets = new FloatTuple(0f, 0f);

	// caches
	private TuplePool tuplePool = new TuplePool();

	private Array<Vector3> darkVertices = new Array<Vector3>();

	private IntMap<Float> pitAreas = new IntMap<Float>();

	OverworldChunk overworldChunk = null;
	
	// how high walls should be before being subdivided for lighting purposes
	private float subdivideSize = 1.25f;

	// Mesh Pooling
	public final static StaticMeshPool tesselatorMeshPool = new StaticMeshPool();
	public final static VertexAttributes meshPoolAttributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
	
	public Tesselator()
	{	
		if(Options.instance.graphicsDetailLevel == 1) subdivideSize = 20f;
		else if(Options.instance.graphicsDetailLevel == 2) subdivideSize = 2.5f;
		else if(Options.instance.graphicsDetailLevel == 3) subdivideSize = 1.25f;
		
		initialize();
	}
	
	public void initialize () {
		verts.clear();
		indices.clear();
		tuplePool.clear();
		collisionTriangles = new Array<Vector3>();
	}
	
	public void addVertex(float posx, float posy, float posz, float u, float v, float color)
	{
		addVertex(posx,posy,posz,u,v,color,true);
	}

    public void addVertex(float posx, float posy, float posz, float u, float v, Level level, boolean showLights)
    {
        addVertex(posx,posy,posz,u,v, showLights ? getLightColorAt(level, posx, posy, posz, Vector3.Z) : Color.WHITE.toFloatBits(),true);
    }
	
	public void addVertex(float posx, float posy, float posz, float u, float v, float color, boolean addCollision)
	{
		verts.add(posx);
		verts.add(posy);
		verts.add(posz);
		verts.add(color);
		verts.add(u);
		verts.add(v);
		
		if(addCollision)
			addCollisionVertex(posx,posy,posz);
	}
	
	public void addCollisionVertex(float posx, float posy, float posz)
	{
		collisionTriangles.add(new Vector3(posx,posy,posz));
	}
	
	public void addVertex(float posx, float posy, float posz, float u, float v, float red, float green, float blue)
	{
		float color = Color.toFloatBits(red, green, blue, 1f);
		addVertex(posx, posy, posz, u, v, color);
	}
	
	// add indices for a triangle
	public void finishTriangle()
	{
		short v = (short)((verts.size / 6) - 3);
		indices.add((short)(v));
		indices.add((short)(v + 1));
		indices.add((short)(v + 2));
		
		// fix the collision triangle normal
		Vector3 vert1 = collisionTriangles.get(collisionTriangles.size - 1);
		Vector3 vert2 = collisionTriangles.get(collisionTriangles.size - 3);
		
		collisionTriangles.set(collisionTriangles.size - 1, vert2);
		collisionTriangles.set(collisionTriangles.size - 3, vert1);
	}
	
	// add indices for a quad
	public void finishQuad()
	{
		short v = (short)((verts.size / 6) - 4);
		indices.add((short)(v));
		indices.add((short)(v + 2));
		indices.add((short)(v + 1));
		indices.add((short)(v + 1));
		indices.add((short)(v + 2));
		indices.add((short)(v + 3));
		
		collisionTriangles.add(new Vector3(collisionTriangles.get(collisionTriangles.size - 2)));
		collisionTriangles.add(new Vector3(collisionTriangles.get(collisionTriangles.size - 4)));
	}
	
	public void refresh()
	{
		hasBuilt = false;
		freeMesh();
	}
	
	public void clear()
	{
		indx = 0;
		hasBuilt = false;
		
		if(collisionTriangles != null)
			collisionTriangles.clear();

		verts.clear();
		indices.clear();

		freeMesh();
	}

	private void freeMesh() {
		if(mesh != null) {
			synchronized (Tesselator.tesselatorMeshPool) {
				Tesselator.tesselatorMeshPool.freeMesh(mesh);
				mesh = null;
			}
		}
	}
	
	public void build() {
		build(indx);
	}
	
	protected void build (int verticesPosition) {
		int indexCount = 32000;
		int vertexCount = indexCount * 9;

		if((Game.instance != null && Game.instance.level instanceof OverworldLevel) || GameManager.renderer.editorIsRendering) {
			// Use the existing mesh, if one is available
			if(mesh == null) {
				synchronized (Tesselator.tesselatorMeshPool) {
					mesh = Tesselator.tesselatorMeshPool.obtain(Tesselator.meshPoolAttributes, vertexCount, indexCount, true);
				}
			}
		}
		else {
			if(mesh != null) {
				synchronized (Tesselator.tesselatorMeshPool) {
					Tesselator.tesselatorMeshPool.freeMesh(mesh);
				}
			}

			// Grab a new mesh from the pool
			synchronized (Tesselator.tesselatorMeshPool) {
				mesh = Tesselator.tesselatorMeshPool.obtain(Tesselator.meshPoolAttributes, verts.size, indices.size, false);
			}
		}

		mesh.setVertices(verts.items, 0, Math.min(verts.size, vertexCount));
		mesh.setIndices(indices.items, 0, Math.min(indices.size, indexCount));
		
		builtLength = verts.size;
		hasBuilt = true;
	}
	
	public void renderMesh (ShaderInfo shader) {
		if(!hasBuilt) build(indx);
		if(builtLength == 0) return;
		
		if (shader != null) {
			try {
				mesh.render(shader.shader, GL20.GL_TRIANGLES);
			}
			catch(Exception ex) {
				Gdx.app.log("RenderMesh", ex.getMessage());
			}
		}
	}
	
	public Boolean HasBuilt()
	{
		return hasBuilt;
	}
	
	public void dispose() {
		if(mesh != null) {
			mesh.dispose();
		}
	}
	
	public BoundingBox calculateBoundingBox() {
		if(mesh == null || builtLength == 0) return new BoundingBox();
		return mesh.calculateBoundingBox();
	}
	
	public boolean empty() {
		return builtLength == 0;
	}

	public Tile getTileOrNull(Level level, int x, int y) {
		return level.getTileOrNull(x, y);
	}

	public Tile getTile(Level level, int x, int y) {
		if(overworldChunk != null) {
			return overworldChunk.getTile(x - overworldChunk.xChunk * overworldChunk.width, y - overworldChunk.yChunk * overworldChunk.height);
		}
		return level.getTile(x, y);
	}

	private FloatTuple t_tuple1 = new FloatTuple(0, 0);
	private FloatTuple t_tuple2 = new FloatTuple(0, 0);

	// where all the magic happens
	public void Tesselate(Level level, GlRenderer renderer, WorldChunk chunk, int xOffset, int yOffset, int width, int height, TesselatorGroups tesselators, boolean makeFloors, boolean makeCeilings, boolean makeWalls, boolean showLights)
	{
        tesselators.clear();

		if(collisionTriangles != null)
			collisionTriangles.clear();

        String defaultTextureAtlas = TextureAtlas.cachedRepeatingAtlases.firstKey();
		if(level.genTheme != null && level.genTheme.defaultTextureAtlas != null) {
			defaultTextureAtlas = level.genTheme.defaultTextureAtlas;
		}

		// setup!
		verts.clear();
		indices.clear();
		darkVertices.clear();
		pitAreas.clear();
		tuplePool.freeAll();
		
		float fullBrightColor = Color.WHITE.toFloatBits();
		
		FloatTuple ceilPairTuple = t_tuple1.set(0, 0);
		FloatTuple floorPairTuple = t_tuple2.set(0, 0);

		if(chunk != null)
			overworldChunk = chunk.overworldChunk;
		
		for(int chunk_y = 0; chunk_y < height; chunk_y++)
		{
			for(int chunk_x = 0; chunk_x < width; chunk_x++)
			{	
				int x = chunk_x + xOffset;
				int y = chunk_y + yOffset;
				
				// grab the current tile, and all the adjacent ones
				Tile c = getTileOrNull(level, x, y);
				if (c == null) continue;
				Tile e = getTile(level, x - 1, y);
				Tile n = getTile(level, x, y - 1);
				Tile w = getTile(level, x + 1, y);
				Tile s = getTile(level, x, y + 1);
				
				float cFloorHeight = c.floorHeight;
				float cCeilHeight = c.ceilHeight;
				
				// init some objects we'll need later
				normal.set(Vector3.Z);
				
				// a wall is a pair of heights to start drawing at, and a pair of end heights
				starts.clear();
				ends.clear();
				x_offsets.set(0f, 1f);
				y_offsets.set(0f, 0f);
				
				if(!c.hide && (!c.renderSolid))
				{
                    Tesselator selectedTesselator = null;
                    String selectedAtlas = c.floorTexAtlas != null ? c.floorTexAtlas : defaultTextureAtlas;

			        // floor
                    selectedTesselator = tesselators.world.getTesselatorByAtlas(selectedAtlas);
					if(c.data.isWater) selectedTesselator = tesselators.water.getTesselatorByAtlas(selectedAtlas);

					TextureAtlas atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
					TextureRegion reg = atlas.getSprite(c.floorTex);

                    boolean skipFloorAndCeiling = c.floorAndCeilingAreSameHeight();
					
					if(makeFloors && !skipFloorAndCeiling) {
						if(c.floorTexRot == 0) {
							vert_uv1.set(reg.getU2(), reg.getV2());
							vert_uv2.set(reg.getU(), reg.getV2());
							vert_uv3.set(reg.getU2(), reg.getV());
							vert_uv4.set(reg.getU(), reg.getV());
						}
						else if(c.floorTexRot == 1) {
							vert_uv1.set(reg.getU2(), reg.getV());
							vert_uv2.set(reg.getU2(), reg.getV2());
							vert_uv3.set(reg.getU(), reg.getV());
							vert_uv4.set(reg.getU(), reg.getV2());
						}
						else if(c.floorTexRot == 2) {
							vert_uv1.set(reg.getU(), reg.getV());
							vert_uv2.set(reg.getU2(), reg.getV());
							vert_uv3.set(reg.getU(), reg.getV2());
							vert_uv4.set(reg.getU2(), reg.getV2());
						}
						else if(c.floorTexRot == 3) {
							vert_uv1.set(reg.getU(), reg.getV2());
							vert_uv2.set(reg.getU(), reg.getV());
							vert_uv3.set(reg.getU2(), reg.getV2());
							vert_uv4.set(reg.getU2(), reg.getV());
						}
						
						if(c.data.darkenFloor) {
							pitAreas.put(x + y * 100, c.floorHeight);

							darkVertices.add(new Vector3(x + 1, cFloorHeight + c.slopeSW, y + 1));
							darkVertices.add(new Vector3(x + 1, cFloorHeight + c.slopeNW, y));
							darkVertices.add(new Vector3(x, cFloorHeight + c.slopeSE, y + 1));
							darkVertices.add(new Vector3(x, cFloorHeight + c.slopeNE, y));
						}

						// adjust verts for texture scale
						adjustFloorUvs(vert_uv1, reg, x, y, c.floorTexRot, atlas);
						adjustFloorUvs(vert_uv2, reg, x, y, c.floorTexRot, atlas);
						adjustFloorUvs(vert_uv3, reg, x, y, c.floorTexRot, atlas);
						adjustFloorUvs(vert_uv4, reg, x, y, c.floorTexRot, atlas);
						
						// add floor
						if (c.tileSpaceType==TileSpaceType.OPEN_SE) {
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeSW, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeNW, y, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeNW, y, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeSE, y + 1, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeSE, y + 1, normal) : fullBrightColor);
							
							selectedTesselator.finishTriangle();
						}
						else if (c.tileSpaceType==TileSpaceType.OPEN_SW) {
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeSW, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeNE, y, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeSE, y + 1, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeSE, y + 1, normal) : fullBrightColor);
							
							selectedTesselator.finishTriangle();
						}
						else if (c.tileSpaceType==TileSpaceType.OPEN_NE) {
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeSW, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeNW, y, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeNW, y, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeNE, y, normal) : fullBrightColor);
							
							selectedTesselator.finishTriangle();
						}
						else if (c.tileSpaceType==TileSpaceType.OPEN_NW) {
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeNE, y, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeSE, y + 1, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeSE, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeNW, y, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeNW, y, normal) : fullBrightColor);
							
							selectedTesselator.finishTriangle();
						}
						else {
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeSW, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeSE, y + 1, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeSE, y + 1, normal) : fullBrightColor);
							selectedTesselator.addVertex(x + 1, cFloorHeight + c.slopeNW, y, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x + 1, cFloorHeight + c.slopeNW, y, normal) : fullBrightColor);
							selectedTesselator.addVertex(x, cFloorHeight + c.slopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cFloorHeight + c.slopeNE, y, normal) : fullBrightColor);
							
							selectedTesselator.finishQuad();
						}

                        if(c.data.isWater) {
                            addWaterEdges(x, y, c, n, s, w, e, tesselators, level, showLights);
                        }
					}
					
					// ceiling
                    selectedAtlas = c.ceilTexAtlas != null ? c.ceilTexAtlas : defaultTextureAtlas;
                    selectedTesselator = tesselators.world.getTesselatorByAtlas(selectedAtlas);

					atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
                    reg = atlas.getSprite(c.ceilTex);
					
					if(makeCeilings && !c.skyCeiling() && !skipFloorAndCeiling) {
						normal.set(Vector3.Z).scl(-1f);
						
						// choose ceiling quad texture rotation
						if(c.ceilTexRot == 0) {
							vert_uv1.set(reg.getU(), reg.getV2());
							vert_uv2.set(reg.getU(), reg.getV());
							vert_uv3.set(reg.getU2(), reg.getV2());
							vert_uv4.set(reg.getU2(), reg.getV());
						}
						else if(c.ceilTexRot == 1) {
							vert_uv1.set(reg.getU2(), reg.getV2());
							vert_uv2.set(reg.getU(), reg.getV2());
							vert_uv3.set(reg.getU2(), reg.getV());
							vert_uv4.set(reg.getU(), reg.getV());
						}
						else if(c.ceilTexRot == 2) {
							vert_uv1.set(reg.getU2(), reg.getV());
							vert_uv2.set(reg.getU2(), reg.getV2());
							vert_uv3.set(reg.getU(), reg.getV());
							vert_uv4.set(reg.getU(), reg.getV2());
						}
						else if(c.ceilTexRot == 3) {
							vert_uv1.set(reg.getU(), reg.getV());
							vert_uv2.set(reg.getU2(), reg.getV());
							vert_uv3.set(reg.getU(), reg.getV2());
							vert_uv4.set(reg.getU2(), reg.getV2());
						}

						// adjust verts for texture scale
						adjustCeilingUvs(vert_uv1, reg, x, y, c.ceilTexRot, atlas);
						adjustCeilingUvs(vert_uv2, reg, x, y, c.ceilTexRot, atlas);
						adjustCeilingUvs(vert_uv3, reg, x, y, c.ceilTexRot, atlas);
						adjustCeilingUvs(vert_uv4, reg, x, y, c.ceilTexRot, atlas);
						
						// add ceiling
						if (c.tileSpaceType==TileSpaceType.OPEN_SE) {
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ?  getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeSE, y + 1, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeSE, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeNW, y, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeNW, y, normal) : fullBrightColor);

                            selectedTesselator.finishTriangle();
						}
						else if(c.tileSpaceType == TileSpaceType.OPEN_NE)
						{
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ?  getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeNE, y, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeNW, y, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeNW, y, normal) : fullBrightColor);

                            selectedTesselator.finishTriangle();
						}
						else if(c.tileSpaceType == TileSpaceType.OPEN_SW)
						{
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ?  getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeSE, y + 1, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeSE, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeNE, y, normal) : fullBrightColor);

                            selectedTesselator.finishTriangle();
						}
						else if(c.tileSpaceType == TileSpaceType.OPEN_NW)
						{
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeNW, y, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeNW, y, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeSE, y + 1, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeSE, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeNE, y, normal) : fullBrightColor);

                            selectedTesselator.finishTriangle();
						}
						else {
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, vert_uv1.val1, vert_uv1.val2, showLights ?  getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeSW, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x + 1, cCeilHeight + c.ceilSlopeNW, y, vert_uv2.val1, vert_uv2.val2, showLights ? getLightColorAt(level,x + 1, cCeilHeight + c.ceilSlopeNW, y, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeSE, y + 1, vert_uv3.val1, vert_uv3.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeSE, y + 1, normal) : fullBrightColor);
                            selectedTesselator.addVertex(x, cCeilHeight + c.ceilSlopeNE, y, vert_uv4.val1, vert_uv4.val2, showLights ? getLightColorAt(level,x, cCeilHeight + c.ceilSlopeNE, y, normal) : fullBrightColor);

                            selectedTesselator.finishQuad();
						}
					}
					
					if(makeWalls) {
                        selectedAtlas = c.wallTexAtlas != null ? c.wallTexAtlas : defaultTextureAtlas;
                        selectedTesselator = tesselators.world.getTesselatorByAtlas(selectedAtlas);

						// check for and add any angled walls in this space
						if (c.tileSpaceType != TileSpaceType.EMPTY || c.tileSpaceType != TileSpaceType.SOLID) {
							boolean makeAngledWall = false;
							if (c.tileSpaceType==TileSpaceType.OPEN_SE) {
				        		normal.set((float)Math.sin(Math.PI/4),0,(float)Math.cos(Math.PI/4));
				        		
				        		x_offsets.set(0, 1);
				        		y_offsets.set(1, 0);

				        		ceilPairTuple.set(c.getSECeilHeight(), c.getNWCeilHeight());
				        		floorPairTuple.set(c.getSEFloorHeight(), c.getNWFloorHeight());
				        		
				        		makeAngledWall = true;
							}
							else if (c.tileSpaceType==TileSpaceType.OPEN_SW) {
				        		normal.set((float)Math.sin(-Math.PI/4),0,(float)Math.cos(-Math.PI/4));
				        		
				        		x_offsets.set(0, 1);
				        		y_offsets.set(0, 1);

				        		ceilPairTuple.set(c.getNECeilHeight(), c.getSWCeilHeight());
				        		floorPairTuple.set(c.getNEFloorHeight(), c.getSWFloorHeight());
				        		
				        		makeAngledWall = true;
							}
							else if (c.tileSpaceType==TileSpaceType.OPEN_NE) {
				        		normal.set((float)Math.sin(3*Math.PI/4),0,(float)Math.cos(3*Math.PI/4));
				        		
				        		x_offsets.set(1, 0);
				        		y_offsets.set(1, 0);

				        		ceilPairTuple.set(c.getSWCeilHeight(), c.getNECeilHeight());
				        		floorPairTuple.set(c.getSWFloorHeight(), c.getNEFloorHeight());
				        		
				        		makeAngledWall = true;
							}
							else if (c.tileSpaceType==TileSpaceType.OPEN_NW) {
				        		normal.set((float)Math.sin(-3*Math.PI/4),0,(float)Math.cos(-3*Math.PI/4));
				        		
				        		x_offsets.set(1, 0);
				        		y_offsets.set(0, 1);

				        		ceilPairTuple.set(c.getNWCeilHeight(), c.getSECeilHeight());
				        		floorPairTuple.set(c.getNWFloorHeight(), c.getSEFloorHeight());
				        		
				        		makeAngledWall = true;
							}
							
							if(makeAngledWall) {
								atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
								reg = atlas.getSprite(c.wallTex);

								// subdivide the wall if needed
								float h = Math.min(ceilPairTuple.val1, ceilPairTuple.val2) - Math.max(floorPairTuple.val1, floorPairTuple.val2);
								if(h > subdivideSize) {
									float start = Math.min(ceilPairTuple.val1, ceilPairTuple.val2);
									starts.add(ceilPairTuple);
									
									start -= start % subdivideSize;
									
									while(start > Math.max(floorPairTuple.val1, floorPairTuple.val2)) {
										ends.add(tuplePool.get(start,start));
										starts.add(tuplePool.get(start,start));
										start -= subdivideSize;
									}
									
									ends.add(floorPairTuple);
								}
								else {
									starts.add(ceilPairTuple);
				        			ends.add(floorPairTuple);
								}

								float texU1 = renderer.GetTexUAt(x + x_offsets.val1, x + x_offsets.val2, 0, 0, 0f, reg, atlas);
								float texU2 = renderer.GetTexUAt(x + x_offsets.val1, x + x_offsets.val2, 0, 0, 1f, reg, atlas);
								
								// now have enough info to add vertices for all the collected wall segments
								for(int i = 0; i < starts.size; i++)
								{
									selectedTesselator.addVertex(x + x_offsets.val1, ends.get(i).val1 , y + y_offsets.val1, texU1, renderer.GetTexVAt(ends.get(i).val1, atlas) + reg.getV(), showLights ? getLightColorAt(level,x + x_offsets.val1, ends.get(i).val1, y + y_offsets.val1, normal) : fullBrightColor);
									selectedTesselator.addVertex(x + x_offsets.val1, starts.get(i).val1, y + y_offsets.val1, texU1, renderer.GetTexVAt(starts.get(i).val1, atlas) + reg.getV(), showLights ? getLightColorAt(level,x + x_offsets.val1, starts.get(i).val1, y + y_offsets.val1, normal) : fullBrightColor);
									selectedTesselator.addVertex(x + x_offsets.val2, ends.get(i).val2, y + y_offsets.val2, texU2, renderer.GetTexVAt(ends.get(i).val2, atlas) + reg.getV(), showLights ? getLightColorAt(level,x + x_offsets.val2, ends.get(i).val2, y + y_offsets.val2, normal) : fullBrightColor);
									selectedTesselator.addVertex(x + x_offsets.val2, starts.get(i).val2, y + y_offsets.val2, texU2, renderer.GetTexVAt(starts.get(i).val2, atlas) + reg.getV(), showLights ? getLightColorAt(level,x + x_offsets.val2, starts.get(i).val2, y + y_offsets.val2, normal) : fullBrightColor);
									selectedTesselator.finishQuad();
								}
								
								starts.clear();
								ends.clear();
								tuplePool.freeAll();
							}
						}
						
						// check for walls to be drawn in each of the cardinal directions
						for(TileEdges dir : TileEdges.values()) {
							Tile checkDir = null;
							if(dir == TileEdges.North) {
								checkDir = s;
								normal.set(0, -1, 0);
							} else if(dir == TileEdges.South) {
								checkDir = n;
								normal.set(0, 1, 0);
							} else if(dir == TileEdges.West) {
								checkDir = e;
								normal.set(-1, 0, 0);
							} else {
								checkDir = w;
								normal.set(0, 0, 1);
							}
							
							TileEdges oppositeDir = Tile.opposite(dir);

							// Caching the various tile checks here for performance and readability.
							boolean showWall;
							boolean isOtherSolid = checkDir.IsSolid();
							boolean isOtherSky = checkDir.isSky();
							boolean isOtherEdgeVisible = checkDir.isTileEdgeVisible(dir, c);
							boolean isOtherFloorHigher = checkDir.IsHigher(dir, c);
							boolean isOtherCeilLower = checkDir.IsCeilLower(dir, c);

							// Determine if any portion of the wall of the tile in the current direction should be shown.
							if (c.isSky() && isOtherSolid) {
								// This makes the edge of an outdoor map transparent for sky tiles.
								showWall = false;
							} else if (isOtherSky && (isOtherFloorHigher && !isOtherSolid)) {
								// Make sure we show the lower portion of a raised sky tile.
								showWall = true;
							} else {
								// Look at the tile in the check direction to find walls (either fully solid, or partial segments)
								showWall = (!isOtherSky && (isOtherEdgeVisible || (!isOtherSolid && (isOtherFloorHigher || isOtherCeilLower))));
							}

							// add wall floor and ceiling height pairs
							if (showWall) {
								Integer bottomWallIndex = null;

								starts.clear();
								ends.clear();
								
								if (isOtherSolid || isOtherEdgeVisible) {
									// fully solid wall! make a wall from floor to ceiling
									FloatTuple ceilPair = c.getCeilingPair(dir, tuplePool);
									FloatTuple floorPair = c.getFloorPair(dir, tuplePool);
									
									// subdivide if needed
									float h = Math.min(ceilPair.val1, ceilPair.val2) - Math.max(floorPair.val1, floorPair.val2);
									if(h > subdivideSize) {
										float start = Math.min(ceilPair.val1, ceilPair.val2);
										starts.add(c.getCeilingPair(dir, tuplePool));
										
										start -= start % subdivideSize;
										
										while(start > Math.max(floorPair.val1, floorPair.val2)) {
											ends.add(tuplePool.get(start,start));
											starts.add(tuplePool.get(start,start));
											start -= subdivideSize;
										}
										
										ends.add(c.getFloorPair(dir, tuplePool));
									}
									else {
										starts.add(c.getCeilingPair(dir, tuplePool));
										ends.add(c.getFloorPair(dir, tuplePool));
									}
								}
								else {
									// add any partial segments that are exposed
									// Show the floor portion that is higher than this tile.
									if (isOtherFloorHigher) {
										bottomWallIndex = starts.size;
										
										FloatTuple ceilPair = checkDir.getFloorPair(oppositeDir, tuplePool).reverse();
										FloatTuple floorPair = tuplePool.get(Math.min(checkDir.getFloorPair(oppositeDir, tuplePool).val1, c.getFloorPair(dir, tuplePool).val1), Math.min(checkDir.getFloorPair(oppositeDir, tuplePool).val2, c.getFloorPair(dir, tuplePool).val2));
										
										// subdivide if needed
										float h = Math.min(ceilPair.val1, ceilPair.val2) - Math.max(floorPair.val1, floorPair.val2);
										if(h > subdivideSize) {
											float start = Math.min(ceilPair.val1, ceilPair.val2);
											starts.add(ceilPair);
											
											while(start > Math.max(floorPair.val1, floorPair.val2)) {
												ends.add(tuplePool.get(start,start));
												starts.add(tuplePool.get(start,start));
												start -= subdivideSize;
												bottomWallIndex++;
											}
											
											ends.add(floorPair);
										}
										else {
											starts.add(ceilPair);
											ends.add(floorPair);
										}
									}
									// Show the ceiling portion that is lower than this tile, but not if it is a sky tile.
									if (isOtherCeilLower && !isOtherSky) {
										
										FloatTuple ceilPair = tuplePool.get(Math.max(checkDir.getCeilingPair(oppositeDir, tuplePool).val1, c.getCeilingPair(dir, tuplePool).val1), Math.max(checkDir.getCeilingPair(oppositeDir, tuplePool).val2, c.getCeilingPair(dir, tuplePool).val2));
										FloatTuple floorPair = checkDir.getCeilingPair(oppositeDir, tuplePool).reverse();
										
										// subdivide if needed
										float h = Math.min(ceilPair.val1, ceilPair.val2) - Math.max(floorPair.val1, floorPair.val2);
										if(h > subdivideSize) {
											float start = Math.min(ceilPair.val1, ceilPair.val2);
											starts.add(ceilPair);
											
											while(start > Math.max(floorPair.val1, floorPair.val2)) {
												ends.add(tuplePool.get(start,start));
												starts.add(tuplePool.get(start,start));
												start -= subdivideSize;
											}
											
											ends.add(floorPair);
										}
										else {
											starts.add(ceilPair);
											ends.add(floorPair);
										}
									}
								}
								
								// set the wall segment vertex x & y offset locations to draw at, based on the direction
								// start with south
								normal.set(Vector3.Y).scl(-1f);
								
								x_offsets.set(0f, 1f);
								y_offsets.set(0f, 0f);
								
								if(dir == TileEdges.North) {
									normal.set(Vector3.Y);
									x_offsets.set(1f, 0f);
									y_offsets.set(1f, 1f);
								}
								else if(dir == TileEdges.West) {
									normal.set(Vector3.X).scl(-1f);
									x_offsets.set(0f, 0f);
									y_offsets.set(1f, 0f);
								}
								else if(dir == TileEdges.East) {
									normal.set(Vector3.X);
									x_offsets.set(1f, 1f);
									y_offsets.set(0f, 1f);
								}
								
								// now have enough info to add vertices for all the collected wall segments
								for(int i = 0; i < starts.size; i++)
								{
                                    selectedAtlas = checkDir.getWallTexAtlas(dir);
                                    if(selectedAtlas == null) selectedAtlas = defaultTextureAtlas;

                                    selectedTesselator = tesselators.world.getTesselatorByAtlas(selectedAtlas);

									atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
                                    reg = atlas.getSprite(checkDir.getWallTex(dir));
					        		
					        		// paint the bottom wall
					        		if(bottomWallIndex != null && bottomWallIndex >= i) {
					        			// if a wall is from a water or lava tile, make a waterfall or lavafall
					        			if(checkDir.data.isWater) {

                                            selectedAtlas = checkDir.floorTexAtlas;
                                            if(selectedAtlas == null) selectedAtlas = defaultTextureAtlas;

											atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
						        			reg = atlas.getSprite(TileManager.instance.getFlowTexture(checkDir));
                                            selectedTesselator = tesselators.waterfall.getTesselatorByAtlas(selectedAtlas);

                                            ends.get(i).val1 -= 0.1f;
                                            ends.get(i).val2 -= 0.1f;
					        			}
					        			else {
                                            selectedAtlas = checkDir.getWallBottomTexAtlas(dir);
                                            if(selectedAtlas == null) selectedAtlas = defaultTextureAtlas;

                                            selectedTesselator = tesselators.world.getTesselatorByAtlas(selectedAtlas);

											atlas = TextureAtlas.getRepeatingAtlasByIndex(selectedAtlas);
                                            reg = atlas.getSprite(checkDir.getWallBottomTex(dir));
					        			}
					        		}

					        		float texU1 = renderer.GetTexUAt(x + x_offsets.val1, x + x_offsets.val2, y + y_offsets.val1, y + y_offsets.val2, 0f, reg, atlas);
					        		float texU2 = renderer.GetTexUAt(x + x_offsets.val1, x + x_offsets.val2, y + y_offsets.val1, y + y_offsets.val2, 1f, reg, atlas);
					        		float v;

					        		if(bottomWallIndex != null && bottomWallIndex >= i) {
										v = reg.getV() + checkDir.getBottomWallYOffset(dir) / (atlas.scale * atlas.rowScale);
									} else {
										v = reg.getV() + checkDir.getWallYOffset(dir) / (atlas.scale * atlas.rowScale);
									}

					        		// This is ugly!
                                    selectedTesselator.addVertex(x + x_offsets.val1, ends.get(i).val1 , y + y_offsets.val1, texU1, renderer.GetTexVAt(ends.get(i).val1, atlas) + v, showLights ? getLightColorAt(level,x + x_offsets.val1, ends.get(i).val1, y + y_offsets.val1, normal) : fullBrightColor);
                                    selectedTesselator.addVertex(x + x_offsets.val1, starts.get(i).val1, y + y_offsets.val1, texU1, renderer.GetTexVAt(starts.get(i).val1, atlas) + v, showLights ? getLightColorAt(level,x + x_offsets.val1, starts.get(i).val1, y + y_offsets.val1, normal) : fullBrightColor);
                                    selectedTesselator.addVertex(x + x_offsets.val2, ends.get(i).val2, y + y_offsets.val2, texU2, renderer.GetTexVAt(ends.get(i).val2, atlas) + v, showLights ? getLightColorAt(level,x + x_offsets.val2, ends.get(i).val2, y + y_offsets.val2, normal) : fullBrightColor);
                                    selectedTesselator.addVertex(x + x_offsets.val2, starts.get(i).val2, y + y_offsets.val2, texU2, renderer.GetTexVAt(starts.get(i).val2, atlas) + v, showLights ? getLightColorAt(level,x + x_offsets.val2, starts.get(i).val2, y + y_offsets.val2, normal) : fullBrightColor);
                                    selectedTesselator.finishQuad();

									tuplePool.freeAll();
								}
							}
						}
					}
				}
			}
		}

        tesselators.build();
	}

    private boolean shouldAddEdge(Tile current, Tile adjacent, WaterEdges edge) {

        if(current.isWater()) {
            // Check if the corners are high enough
			if(adjacent.blockMotion) return true;
			
            if (edge == WaterEdges.North) {
                return (Math.abs(current.getNEFloorHeight() - adjacent.getSEFloorHeight()) > 0.15f &&
                        Math.abs(current.getNWFloorHeight() - adjacent.getSWFloorHeight()) > 0.15f);
            } else if (edge == WaterEdges.South) {
                return (Math.abs(current.getSEFloorHeight() - adjacent.getNEFloorHeight()) > 0.15f &&
                        Math.abs(current.getSWFloorHeight() - adjacent.getNWFloorHeight()) > 0.15f);
            } else if (edge == WaterEdges.East) {
                return (Math.abs(current.getSEFloorHeight() - adjacent.getSWFloorHeight()) > 0.15f &&
                        Math.abs(current.getNEFloorHeight() - adjacent.getNWFloorHeight()) > 0.15f);
            } else if (edge == WaterEdges.West) {
                return (Math.abs(current.getNWFloorHeight() - adjacent.getNEFloorHeight()) > 0.15f &&
                        Math.abs(current.getSWFloorHeight() - adjacent.getSEFloorHeight()) > 0.15f);
            }
        }

        return true;
    }

    private Tesselator getTesselator(Tile current, Tile adjacent, TesselatorGroups tesselators) {

		Tile highest = current.floorHeight >= adjacent.floorHeight || adjacent.data.edgeTex == null ? current : adjacent;

        String textureAtlas = highest.floorTexAtlas;
		if(textureAtlas == null || !TextureAtlas.cachedRepeatingAtlases.containsKey(textureAtlas))
			textureAtlas = TextureAtlas.cachedRepeatingAtlases.firstKey();

        if((adjacent.floorHeight != current.floorHeight && adjacent.data.isWater) || (adjacent.floorHeight < current.floorHeight && !adjacent.data.isWater && !adjacent.blockMotion)) {
            return tesselators.waterfallEdges.getTesselatorByAtlas(textureAtlas);
        }
        else {
            return tesselators.waterEdges.getTesselatorByAtlas(textureAtlas);
        }
    }

    private TextureRegion getEdgeTexRegion(Tile current, Tile adjacent) {
        Tile highest = current.floorHeight >= adjacent.floorHeight || adjacent.data.edgeTex == null ? current : adjacent;

		String textureAtlas = highest.floorTexAtlas;
		if(textureAtlas == null || !TextureAtlas.cachedRepeatingAtlases.containsKey(textureAtlas))
			textureAtlas = TextureAtlas.cachedRepeatingAtlases.firstKey();

		if(highest.data.flowEdgeTex != null && ((adjacent.floorHeight != current.floorHeight && adjacent.data.isWater) || (adjacent.floorHeight < current.floorHeight && !adjacent.data.isWater && !adjacent.blockMotion))) {
            return TextureAtlas.getRepeatingAtlasByIndex(textureAtlas).getSprite(highest.data.flowEdgeTex);
        }
        else {
            return TextureAtlas.getRepeatingAtlasByIndex(textureAtlas).getSprite(highest.data.edgeTex);
        }
    }

    private void addWaterEdges(int x, int y, Tile c, Tile n, Tile s, Tile w, Tile e, TesselatorGroups tesselators, Level level, boolean showLights) {
        if(c.data.edgeTex == null) return;

        final float fullBrightColor = Color.WHITE.toFloatBits();

        final float heightOffset = 0.0025f;
        final float depthOffset = 0f;

        if(shouldAddEdge(c, n, WaterEdges.North)) {
            Tesselator tesselator = getTesselator(c, n, tesselators);
            TextureRegion reg = getEdgeTexRegion(c, n);
            WaterEdges edge = WaterEdges.North;
            Array<Triangle> triangles = WaterEdgeSplitter.split(c, edge);
            for(int ti = 0; ti < triangles.size; ti++) {
                Triangle t = triangles.get(ti);
                tesselator.addVertex(t.v1.x + x, t.v1.y, t.v1.z + y, WaterEdgeSplitter.getTexU(t.v1, edge, reg), WaterEdgeSplitter.getTexV(t.v1, edge, reg), showLights ? getLightColorAt(level, t.v1.x + x, t.v1.y, t.v1.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v2.x + x, t.v2.y, t.v2.z + y, WaterEdgeSplitter.getTexU(t.v2, edge, reg), WaterEdgeSplitter.getTexV(t.v2, edge, reg), showLights ? getLightColorAt(level, t.v2.x + x, t.v2.y, t.v2.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v3.x + x, t.v3.y, t.v3.z + y, WaterEdgeSplitter.getTexU(t.v3, edge, reg), WaterEdgeSplitter.getTexV(t.v3, edge, reg), showLights ? getLightColorAt(level, t.v3.x + x, t.v3.y, t.v3.z + y, normal) : fullBrightColor);
                tesselator.finishTriangle();
            }

            // waterfall edges
            if(n.floorHeight < c.floorHeight) {
                tesselator.addVertex(x, c.getNEFloorHeight() + heightOffset, y, reg.getU(), reg.getV2(), level, showLights);
                tesselator.addVertex(x, c.getNEFloorHeight() - 0.2f, y - depthOffset, reg.getU(), reg.getV() + 0.8f, level, showLights);
                tesselator.addVertex(x + 1f, c.getNWFloorHeight() + heightOffset, y, reg.getU2(), reg.getV2(), level, showLights);
                tesselator.addVertex(x + 1f, c.getNWFloorHeight() - 0.2f, y - depthOffset, reg.getU2(), reg.getV() + 0.8f, level, showLights);
                tesselator.finishQuad();

				if(n.isWater()) {
					tesselator.addVertex(x + 1f, n.getSWFloorHeight() - depthOffset, y - depthOffset, reg.getU(), reg.getV2(), level, showLights);
					tesselator.addVertex(x + 1f, n.getSWFloorHeight() + 0.2f, y - depthOffset, reg.getU(), reg.getV() + 0.8f, level, showLights);
					tesselator.addVertex(x, n.getSEFloorHeight() - depthOffset, y - depthOffset, reg.getU2(), reg.getV2(), level, showLights);
					tesselator.addVertex(x, n.getSEFloorHeight() + 0.2f, y - depthOffset, reg.getU2(), reg.getV() + 0.8f, level, showLights);
					tesselator.finishQuad();
				}
            }
        }

        if(shouldAddEdge(c, s, WaterEdges.South)) {
            Tesselator tesselator = getTesselator(c, s, tesselators);
            TextureRegion reg = getEdgeTexRegion(c, s);
            WaterEdges edge = WaterEdges.South;
            Array<Triangle> triangles = WaterEdgeSplitter.split(c, edge);
            for(int ti = 0; ti < triangles.size; ti++) {
                Triangle t = triangles.get(ti);
                tesselator.addVertex(t.v1.x + x, t.v1.y, t.v1.z + y, WaterEdgeSplitter.getTexU(t.v1, edge, reg), WaterEdgeSplitter.getTexV(t.v1, edge, reg), showLights ? getLightColorAt(level, t.v1.x + x, t.v1.y, t.v1.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v2.x + x, t.v2.y, t.v2.z + y, WaterEdgeSplitter.getTexU(t.v2, edge, reg), WaterEdgeSplitter.getTexV(t.v2, edge, reg), showLights ? getLightColorAt(level, t.v2.x + x, t.v2.y, t.v2.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v3.x + x, t.v3.y, t.v3.z + y, WaterEdgeSplitter.getTexU(t.v3, edge, reg), WaterEdgeSplitter.getTexV(t.v3, edge, reg), showLights ? getLightColorAt(level, t.v3.x + x, t.v3.y, t.v3.z + y, normal) : fullBrightColor);
                tesselator.finishTriangle();
            }

            // waterfall edges
            if(s.floorHeight < c.floorHeight) {
                tesselator.addVertex(x + 1f, c.getSWFloorHeight() + heightOffset, y + 1f, reg.getU2(), reg.getV2(), level, showLights);
                tesselator.addVertex(x + 1f, c.getSWFloorHeight() - 0.2f, y + 1 + depthOffset, reg.getU2(), reg.getV() + 0.8f, level, showLights);
                tesselator.addVertex(x, c.getSEFloorHeight() + heightOffset, y + 1f, reg.getU(), reg.getV2(), level, showLights);
                tesselator.addVertex(x, c.getSEFloorHeight() - 0.2f, y + 1 + depthOffset, reg.getU(), reg.getV() + 0.8f, level, showLights);
                tesselator.finishQuad();

				if(s.isWater()) {
					tesselator.addVertex(x, s.getNEFloorHeight() - depthOffset, y + 1 + depthOffset, reg.getU2(), reg.getV2(), level, showLights);
					tesselator.addVertex(x, s.getNEFloorHeight() + 0.2f, y + 1 + depthOffset, reg.getU2(), reg.getV() + 0.8f, level, showLights);
					tesselator.addVertex(x + 1f, s.getNWFloorHeight() - depthOffset, y + 1 + depthOffset, reg.getU(), reg.getV2(), level, showLights);
					tesselator.addVertex(x + 1f, s.getNWFloorHeight() + 0.2f, y + 1 + depthOffset, reg.getU(), reg.getV() + 0.8f, level, showLights);
					tesselator.finishQuad();
				}
            }
        }

        if(shouldAddEdge(c, e, WaterEdges.East)) {
            Tesselator tesselator = getTesselator(c, e, tesselators);
            TextureRegion reg = getEdgeTexRegion(c, e);
            WaterEdges edge = WaterEdges.East;
            Array<Triangle> triangles = WaterEdgeSplitter.split(c, edge);
            for(int ti = 0; ti < triangles.size; ti++) {
                Triangle t = triangles.get(ti);
                tesselator.addVertex(t.v1.x + x, t.v1.y, t.v1.z + y, WaterEdgeSplitter.getTexU(t.v1, edge, reg), WaterEdgeSplitter.getTexV(t.v1, edge, reg), showLights ? getLightColorAt(level, t.v1.x + x, t.v1.y, t.v1.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v2.x + x, t.v2.y, t.v2.z + y, WaterEdgeSplitter.getTexU(t.v2, edge, reg), WaterEdgeSplitter.getTexV(t.v2, edge, reg), showLights ? getLightColorAt(level, t.v2.x + x, t.v2.y, t.v2.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v3.x + x, t.v3.y, t.v3.z + y, WaterEdgeSplitter.getTexU(t.v3, edge, reg), WaterEdgeSplitter.getTexV(t.v3, edge, reg), showLights ? getLightColorAt(level, t.v3.x + x, t.v3.y, t.v3.z + y, normal) : fullBrightColor);
                tesselator.finishTriangle();
            }

            // waterfall edges
            if(e.floorHeight < c.floorHeight) {
                tesselator.addVertex(x, c.getSEFloorHeight() + heightOffset, y + 1, reg.getU(), reg.getV2(), level, showLights);
                tesselator.addVertex(x - depthOffset, c.getSEFloorHeight() - 0.2f, y + 1, reg.getU(), reg.getV() + 0.8f, level, showLights);
                tesselator.addVertex(x, c.getNEFloorHeight() + heightOffset, y, reg.getU2(), reg.getV2(), level, showLights);
                tesselator.addVertex(x - depthOffset, c.getNEFloorHeight() - 0.2f, y, reg.getU2(), reg.getV() + 0.8f, level, showLights);
                tesselator.finishQuad();

				if(e.isWater()) {
					tesselator.addVertex(x - depthOffset, e.getNWFloorHeight() - depthOffset, y, reg.getU(), reg.getV2(), level, showLights);
					tesselator.addVertex(x - depthOffset, e.getNWFloorHeight() + 0.2f, y, reg.getU(), reg.getV() + 0.8f, level, showLights);
					tesselator.addVertex(x - depthOffset, e.getSWFloorHeight() - depthOffset, y + 1, reg.getU2(), reg.getV2(), level, showLights);
					tesselator.addVertex(x - depthOffset, e.getSWFloorHeight() + 0.2f, y + 1, reg.getU2(), reg.getV() + 0.8f, level, showLights);
					tesselator.finishQuad();
				}
            }
        }

        if(shouldAddEdge(c, w, WaterEdges.West)) {
            Tesselator tesselator = getTesselator(c, w, tesselators);
            TextureRegion reg = getEdgeTexRegion(c, w);
            WaterEdges edge = WaterEdges.West;
            Array<Triangle> triangles = WaterEdgeSplitter.split(c, edge);
            for(int ti = 0; ti < triangles.size; ti++) {
                Triangle t = triangles.get(ti);
                tesselator.addVertex(t.v1.x + x, t.v1.y, t.v1.z + y, WaterEdgeSplitter.getTexU(t.v1, edge, reg), WaterEdgeSplitter.getTexV(t.v1, edge, reg), showLights ? getLightColorAt(level, t.v1.x + x, t.v1.y, t.v1.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v2.x + x, t.v2.y, t.v2.z + y, WaterEdgeSplitter.getTexU(t.v2, edge, reg), WaterEdgeSplitter.getTexV(t.v2, edge, reg), showLights ? getLightColorAt(level, t.v2.x + x, t.v2.y, t.v2.z + y, normal) : fullBrightColor);
                tesselator.addVertex(t.v3.x + x, t.v3.y, t.v3.z + y, WaterEdgeSplitter.getTexU(t.v3, edge, reg), WaterEdgeSplitter.getTexV(t.v3, edge, reg), showLights ? getLightColorAt(level, t.v3.x + x, t.v3.y, t.v3.z + y, normal) : fullBrightColor);
                tesselator.finishTriangle();
            }

            // waterfall edges
            if(w.floorHeight < c.floorHeight) {
                tesselator.addVertex(x + 1, c.getNWFloorHeight() + heightOffset, y, reg.getU2(), reg.getV2(), level, showLights);
                tesselator.addVertex(x + 1 + depthOffset, c.getNWFloorHeight() - 0.2f, y, reg.getU2(), reg.getV() + 0.8f, level, showLights);
                tesselator.addVertex(x + 1, c.getSWFloorHeight() + heightOffset, y + 1, reg.getU(), reg.getV2(), level, showLights);
                tesselator.addVertex(x + 1 + depthOffset, c.getSWFloorHeight() - 0.2f, y + 1, reg.getU(), reg.getV() + 0.8f, level, showLights);
                tesselator.finishQuad();

				if(w.isWater()) {
					tesselator.addVertex(x + 1 + depthOffset, w.getSEFloorHeight() - depthOffset, y + 1, reg.getU2(), reg.getV2(), level, showLights);
					tesselator.addVertex(x + 1 + depthOffset, w.getSEFloorHeight() + 0.2f, y + 1, reg.getU2(), reg.getV() + 0.8f, level, showLights);
					tesselator.addVertex(x + 1 + depthOffset, w.getNEFloorHeight() - depthOffset, y, reg.getU(), reg.getV2(), level, showLights);
					tesselator.addVertex(x + 1 + depthOffset, w.getNEFloorHeight() + 0.2f, y, reg.getU(), reg.getV() + 0.8f, level, showLights);
					tesselator.finishQuad();
				}
            }
        }
    }

    Color ct = new Color();
    public float getLightColorAt(Level level, float posx, float posy, float posz, Vector3 normal)
	{
		Color tempColor = ct.set(Color.WHITE);
		tempColor = level.getLightColorAt(posx, posz, posy, normal, tempColor);

		for(int xx = -1; xx < 1; xx++) {
			for(int yy = -1; yy < 1; yy++) {
				Tile t = level.getTileOrNull((int)posx + xx, (int)posz + yy);
				if(t != null && t.data != null && t.data.darkenFloor) {
					tempColor.a = posy - t.floorHeight;
					if(tempColor.a < 0) tempColor.a = 0;
					if(tempColor.a > 1) tempColor.a = 1;
				}
			}
		}

		if(normal != null && normal.x == 0 && normal.z == 0) {
			if(normal.y == 1 || normal.y == -1) {
				tempColor.r *= 0.9f;
				tempColor.g *= 0.9f;
				tempColor.b *= 0.9f;
			}
		}

		tempColor.mul(tempColor.a);
		return tempColor.toFloatBits();
	}

	private static void adjustFloorUvs(FloatTuple uv, TextureRegion region, float xLoc, float yLoc, int rot, TextureAtlas atlas) {
    	if(atlas.scale == 1f && atlas.rowScale == 1) {
    		// Nothing to do, skip this
    		return;
		}

		int textureScale = (int)atlas.scale;
		int y_textureScale = atlas.rowScale * textureScale;

		float uMod = (uv.val1 - region.getU()) / (region.getU2() - region.getU());
		float vMod = (uv.val2 - region.getV()) / (region.getV2() - region.getV());
		if(uMod == 0f)      uMod = 0.00001f;
		else if(uMod == 1f) uMod = 0.99999f;
		if(vMod == 0f)      vMod = 0.00001f;
		else if(vMod == 1f) vMod = 0.99999f;

		float x;
		float y;
		if(rot == 1) {
			y = (xLoc - vMod) + 1;
			x = (yLoc + uMod);
		}
		else if(rot == 2) {
			x = (xLoc - uMod) + 1;
			y = (yLoc - vMod) + 1;
		}
		else if(rot == 3) {
			y = (xLoc + vMod);
			x = (yLoc - uMod) + 1;
		}
		else {
			x = (xLoc + uMod);
			y = (yLoc + vMod);
		}

		float um = (x % textureScale) / (float)textureScale;
		float vm = (y % y_textureScale) / (float)y_textureScale;
		if(rot == 1)
			um = 1.0f - um;
		if(rot == 2) {
			um = 1.0f - um;
			vm = 1.0f - vm;
		}
		if(rot == 3)
			vm = 1.0f - vm;
		uv.val1 = (region.getU2() - region.getU()) * um + region.getU();
		uv.val2 = (region.getV2() - region.getV()) * vm + region.getV();
	}

	private static void adjustCeilingUvs(FloatTuple uv, TextureRegion region, float xLoc, float yLoc, int rot, TextureAtlas atlas) {
		if(atlas.scale == 1f && atlas.rowScale == 1) {
			// Nothing to do, skip this
			return;
		}

		int textureScale = (int)atlas.scale;
		int y_textureScale = atlas.rowScale * textureScale;

		float uMod = (uv.val1 - region.getU()) / (region.getU2() - region.getU());
		float vMod = (uv.val2 - region.getV()) / (region.getV2() - region.getV());
		if(uMod == 0f)      uMod = 0.00001f;
		else if(uMod == 1f) uMod = 0.99999f;
		if(vMod == 0f)      vMod = 0.00001f;
		else if(vMod == 1f) vMod = 0.99999f;

		float x;
		float y;
		if(rot == 1) {
			y = (xLoc + vMod);
			x = (yLoc + uMod);
		}
		else if(rot == 2) {
			x = (xLoc + uMod);
			y = (yLoc - vMod) + 1;
		}
		else if(rot == 3) {
			y = (xLoc - vMod) + 1;
			x = (yLoc - uMod) + 1;
		}
		else {
			x = (xLoc - uMod) + 1;
			y = (yLoc + vMod);
		}

		float um = (x % textureScale) / (float)textureScale;
		float vm = (y % y_textureScale) / (float)y_textureScale;
		if(rot == 0)
			vm = 1.0f - vm;
		else if(rot == 2) {
			um = 1.0f - um;
		}
		else if(rot == 3) {
			vm = 1.0f - vm;
			um = 1.0f - um;
		}
		uv.val1 = (region.getU2() - region.getU()) * um + region.getU();
		uv.val2 = (region.getV2() - region.getV()) * vm + region.getV();
	}
}
