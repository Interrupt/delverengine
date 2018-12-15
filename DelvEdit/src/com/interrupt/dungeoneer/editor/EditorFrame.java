package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.interrupt.dungeoneer.*;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.editor.gfx.SurfacePickerDecal;
import com.interrupt.dungeoneer.editor.history.EditorHistory;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.TextureRegionPicker;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.Entity.EditorState;
import com.interrupt.dungeoneer.entities.areas.Area;
import com.interrupt.dungeoneer.entities.triggers.BasicTrigger;
import com.interrupt.dungeoneer.entities.triggers.ButtonModel;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.generator.DungeonGenerator;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.dungeoneer.gfx.*;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.interfaces.Directional;
import com.interrupt.dungeoneer.partitioning.TriangleSpatialHash;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.ExitTile;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.StringManager;
import com.noise.PerlinNoise;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map.Entry;

public class EditorFrame implements ApplicationListener {

    private EditorClipboard clipboard = null;

	public enum ControlPointType { floor, ceiling, northCeil, northFloor, eastCeil, eastFloor, southCeil, southFloor, westCeil, westFloor, vertex };
	public enum ControlVertex { slopeNW, slopeNE, slopeSW, slopeSE, ceilNW, ceilNE, ceilSW, ceilSE }
	public enum DragMode { NONE, XY, X, Y, Z }
	public enum MoveMode { NONE, DRAG, ROTATE }

	public EditorHistory history = new EditorHistory();

	public Color controlPointColor = new Color(1f, 0.4f, 0f, 1f);

	public Vector3 tempVec1 = new Vector3();
	public Vector3 tempVec2 = new Vector3();
	public Vector3 tempVec3 = new Vector3();
	public Vector3 tempVec4 = new Vector3();
	public Vector3 tempVec5 = new Vector3();

	public int pickedWallTexture = 0;
	public int pickedCeilingTexture = 1;
	public int pickedFloorTexture = 2;
    public int pickedWallBottomTexture = 0;

    public String pickedWallTextureAtlas = "texture";
    public String pickedWallBottomTextureAtlas = "texture";

    public String pickedFloorTextureAtlas = "texture";
    public String pickedCeilingTextureAtlas = "texture";

    public String lastTextureRegionPickerSelection = "texture";

	private static transient PerlinNoise perlinNoise = new PerlinNoise(1, 1f, 2f, 1f, 1);

	GameApplication gameApp = null;

	public boolean canDelete = true;

    protected static TriangleSpatialHash triangleSpatialHash = new TriangleSpatialHash(1);
    protected static Array<Triangle> staticMeshCollisionTriangles = new Array<Triangle>();

    protected Array<Vector3> spatialWorkerList = new Array<Vector3>();

	public enum TileSurface {Ceiling, Floor, UpperWall, LowerWall};

    public class PickedSurface {
    	public TileEdges edge;
    	public TileSurface tileSurface;
    	public boolean isPicked = false;
    	Vector3 position = new Vector3();
	}

	public PickedSurface pickedSurface = new PickedSurface();

    Matrix4 selectionTempMatrix = new Matrix4();
	Matrix4 selectionTempMatrix2 = new Matrix4();
	BoundingBox selectionTempBoundingBox = new BoundingBox();

	Vector3 selectionTempVector1 = new Vector3();
	Vector3 selectionTempVector2 = new Vector3();

	FrameBuffer pickerFrameBuffer = null;
	Pixmap pickerPixelBuffer = null;
	Color pickedPixelBufferColor = new Color();

	SpriteBatch pickViz = null;

	int lastInputX = -1;
	int lastInputY = -1;

	private class ControlPointVertex {
		Tile tile;
		ControlVertex vertex = ControlVertex.slopeNE;

		public ControlPointVertex(Tile tile, ControlVertex vertex) {
			this.tile = tile;
			this.vertex = vertex;
		}
	}

	private class ControlPoint {
		public Vector3 point = null;
		public ControlPointType controlPointType;

		public Array<ControlPointVertex> vertices = new Array<ControlPointVertex>();

		public ControlPoint(Vector3 point, ControlPointType type) {
			this.point = point;
			controlPointType = type;
		}

		public ControlPoint(Vector3 point, ControlPointVertex vertex) {
			this.point = point;
			controlPointType = ControlPointType.vertex;
			vertices.add(vertex);
		}

		public boolean isCeiling() {
			return controlPointType == ControlPointType.northCeil || controlPointType == ControlPointType.eastCeil || controlPointType == ControlPointType.southCeil || controlPointType == ControlPointType.westCeil;
		}

		public boolean isFloor() {
			return controlPointType == ControlPointType.northFloor || controlPointType == ControlPointType.eastFloor || controlPointType == ControlPointType.southFloor || controlPointType == ControlPointType.westFloor;
		}

		public boolean isNorthCeiling() {
			return controlPointType == ControlPointType.northCeil || controlPointType == ControlPointType.ceiling;
		}

		public boolean isSouthCeiling() {
			return controlPointType == ControlPointType.southCeil || controlPointType == ControlPointType.ceiling;
		}

		public boolean isEastCeiling() {
			return controlPointType == ControlPointType.eastCeil || controlPointType == ControlPointType.ceiling;
		}

		public boolean isWestCeiling() {
			return controlPointType == ControlPointType.westCeil || controlPointType == ControlPointType.ceiling;
		}

		public boolean isNorthFloor() {
			return controlPointType == ControlPointType.northFloor || controlPointType == ControlPointType.floor;
		}

		public boolean isSouthFloor() {
			return controlPointType == ControlPointType.southFloor || controlPointType == ControlPointType.floor;
		}

		public boolean isEastFloor() {
			return controlPointType == ControlPointType.eastFloor || controlPointType == ControlPointType.floor;
		}

		public boolean isWestFloor() {
			return controlPointType == ControlPointType.westFloor || controlPointType == ControlPointType.floor;
		}
	}

	private JFrame window = null;

	private GameInput input;
    public EditorInput editorInput;
    private InputMultiplexer inputMultiplexer;

	private int curWidth;
    private int curHeight;

	private boolean rightClickWasDown = false;

	public TesselatorGroups tesselators;

    public PerspectiveCamera camera = new PerspectiveCamera();

    protected Pixmap wallPixmap;
    protected Texture selectionTex;
    protected Texture walltex;
    protected TextureRegion wallTextures[];
    protected TextureRegion editorSprites[];
    protected Texture meshtex;

    protected HashMap<Entity.ArtType, TextureRegion[]> spriteAtlases = new HashMap<Entity.ArtType, TextureRegion[]>();

    protected EntityManager entityManager;

    Level level = null;

    float camX = 7.5f;
    float camY = 8;
    float camZ = 4.5f;

    float rotX = 0;
    float rotY = 20f;
    double rota = 0;
	double rotya = 0;
	float rotYClamp = 1.571f;

    double walkVel = 0.05;
	double walkSpeed = 0.15;
	double rotSpeed = 0.009;
	double maxRot = 0.8;

	boolean readFloorMove, readCeilMove, readRotate;
	private int readFloorMoveCount, readCeilMoveCount;

	Mesh cubeMesh;
    Mesh gridMesh;

	GlRenderer renderer = null;

    private boolean slopePointMode = false;
    private boolean slopeEdgeMode = false;
    private int slopeSelNum = 0;

    private boolean selected = false;

    private boolean tileDragging = false;
    private int selectionX = 0;
    private int selectionY = 0;
    private int selectionWidth = 1;
    private int selectionHeight = 1;
	private Vector2 selectionHeights = new Vector2();
    private boolean vertexSelectionMode = false;

    public float time = 0;

    public String curFileName = "";

	protected DecalBatch spriteBatch;
    protected DecalBatch pointBatch;

    public EditorUi editorUi = null;

    Image wallPickerButton = null;
    Image bottomWallPickerButton = null;
    Image ceilPickerButton = null;
    Image floorPickerButton = null;
    CheckBox paintAdjacent;

    protected Pool<Decal> decalPool = new Pool<Decal>(128) {
    	@Override
        protected Decal newObject () {
                return Decal.newDecal(1, 1, editorSprites[0]);
        }
    };
    protected Array<Decal> usedDecals = new Array<Decal>(256);

    private int messageTimer = 0;
    private String message = "";

	private Player player = null;

    private boolean showLights = false;

	private Entity hoveredEntity = null;
    private Entity pickedEntity = null;
    private boolean movingEntity = false;
	private DragMode dragMode = DragMode.NONE;
    private MoveMode moveMode = MoveMode.DRAG;
    private Vector3 dragStart = null;

    private Vector3 rotateStart = null;
    private Vector3 rotateStartIntersection = null;

    private Array<Entity> additionalSelected = new Array<Entity>();

    private boolean readLeftClick = false;
    private boolean readRightClick = false;

	private Plane dragPlane = null;
    private Vector3 dragOffset = null;

    public HashMap<String, Array<Mesh>> staticMeshBatch = null;

    private ShapeRenderer lineRenderer;
    private ShapeRenderer pointRenderer;
    private ShapeRenderer boxRenderer;

    private boolean showCollisionBoxes = false;

	Color hoveredColor = new Color(0.5f, 1f, 0.5f, 1f);
	Color selectedColor = new Color(1f, 0.5f, 0.5f, 1f);

    Vector3 intersection = new Vector3();
	Vector3 testPos = new Vector3();
	Vector3 tempVector1 = new Vector3();

	Color tempColor = new Color();

	DrawableSprite unknownEntityMarker = new DrawableSprite();

	Array<ControlPoint> controlPoints = new Array<ControlPoint>();
	ControlPoint pickedControlPoint = null;
	public boolean movingControlPoint = false;

	Color colorDarkRed = new Color(0.8f,0,0,0.3f);
	Color colorDarkBlue = new Color(0,0,0.8f,0.3f);
	Color colorDarkGreen = new Color(0,0.8f,0,0.3f);

	Vector3 xGridStart = new Vector3();
	Vector3 xGridEnd = new Vector3();
	Vector3 yGridStart = new Vector3();
	Vector3 yGridEnd = new Vector3();

	// ground plane intersection
	Vector3 intpos = new Vector3();

	Plane p = new Plane(new Vector3(0,1,0), 0.5f);

	Vector3 rayOutVector = new Vector3();

	private boolean isOnWindows = System.getProperty("os.name").startsWith("Windows");

	public Editor editor;

	/**
	 * @wbp.parser.entryPoint
	 */
	public EditorFrame(JFrame window, Editor editor) {
		this.window = window;
		this.editor = editor;
	}

	public void init(){
		input = new GameInput();
		Gdx.input.setInputProcessor( input );

        renderer.init();

        tesselators = new TesselatorGroups();

		cubeMesh = genCube();
		spriteBatch = new DecalBatch(new SpriteGroupStrategy(camera, null, GlRenderer.worldShaderInfo, 1));

		unknownEntityMarker.tex = 1000;
		unknownEntityMarker.artType = ArtType.hidden;

		StringManager.init();

		Game.init();

		// load the entity templates
		try {
			entityManager = Game.getModManager().loadEntityManager(Game.gameData.entityDataFiles);
			EntityManager.setSingleton(entityManager);
		} catch (Exception ex) {
			// whoops
			Gdx.app.log("Editor", "Error loading entities.dat: " + ex.getMessage());
		}
	}

	@Override
	public void dispose() {
		if(gameApp != null) gameApp.dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void render() {

		if(gameApp == null)
			GlRenderer.time += Gdx.graphics.getDeltaTime();

		GlRenderer.worldShaderInfo.setAttributes(camera.combined,
				0,
				1000,
				1000,
				time,
				Color.BLACK,
				Color.BLACK);

		GlRenderer.waterShaderInfo.setAttributes(camera.combined,
				0,
				1000,
				1000,
				time * 62f,
				Color.BLACK,
				Color.BLACK);

        GlRenderer.waterEdgeShaderInfo.setAttributes(camera.combined,
                0,
                1000,
                1000,
                time * 62f,
                Color.BLACK,
                Color.BLACK);

		GlRenderer.fogShaderInfo.setAttributes(camera.combined,
				0,
				1000,
				1000,
				time * 62f,
				Color.BLACK,
				Color.BLACK);

		if(gameApp != null) {
			if(GameApplication.editorRunning) {
				try {
					gameApp.render();
				}
				catch(Exception ex) {
					Gdx.app.error("Delver", ex.getMessage());
					GameApplication.editorRunning = false;
				}
			}
			else {
				gameApp = null;
				Gdx.app.getInput().setCursorCatched(false);
				Gdx.input.setInputProcessor( inputMultiplexer );
				input.clear();

				camX = Game.instance.player.x;
				camZ = Game.instance.player.z + 0.5f;
				camY = Game.instance.player.y;

				rotX = Game.instance.player.rot + 3.14159265f;
				rotY = -(Game.instance.player.yrot - 18.9f);

				Audio.stopLoopingSounds();

				tesselators.refresh();
			}

			return;
		}

        tick();
        draw();

		renderer.clearLights();
		renderer.clearDecals();

		if(!editorUi.isShowingMenuOrModal() && pickedControlPoint == null && hoveredEntity == null && pickedEntity == null) {
			updatePickedSurface();
		}

		if((!Gdx.input.isButtonPressed(Buttons.LEFT) || editorUi.isShowingMenuOrModal()) && pickedControlPoint == null && hoveredEntity == null && pickedEntity == null) {
			renderPickedSurface();
		}

        Stage stage = editorUi.getStage();
        if(stage != null) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
	}

	Vector3 t_dragVector = new Vector3();
	Vector3 t_dragVector2 = new Vector3();
	Vector3 t_dragOffset = new Vector3();
	Plane t_dragPlane = new Plane();

	Vector3 intersectNormal = new Vector3();
	Vector3 intersectTemp = new Vector3();
	Array<Entity> selectedEntities = new Array<Entity>();
	public void draw() {
		GL20 gl = renderer.getGL();
        GlRenderer.clearBoundTexture();
		GameManager.renderer = renderer;

		time += Gdx.graphics.getDeltaTime();

		if(renderer.loadedLevel != level) {
			renderer.setLevelToRender(level);
		}

		renderer.editorIsRendering = player == null;
		renderer.enableLighting = showLights;

		level.fogStart = 500f;
		level.fogEnd = 500f;
		level.viewDistance = 500f;

		GlRenderer.fogStart = level.fogStart;
		GlRenderer.fogEnd = level.fogEnd;
		GlRenderer.viewDistance = level.viewDistance;

		camera.direction.set(0, 0, 1);
		camera.up.set(0, 1, 0);
		camera.rotate(rotY * 57.2957795f, 1f, 0, 0);
		camera.rotate((float)(rotX + 3.14) * 57.2957795f, 0, 1f, 0);
		camera.update();

		renderer.updateDynamicLights(camera);
		renderer.updateShaderAttributes();

		// init line renderer
		if(lineRenderer == null) lineRenderer = new ShapeRenderer();
		lineRenderer.setProjectionMatrix(camera.combined);
		lineRenderer.begin(ShapeType.Line);

		if(pointRenderer == null) pointRenderer = new ShapeRenderer();
		pointRenderer.setProjectionMatrix(camera.combined);
		pointRenderer.begin(ShapeType.Filled);
		pointRenderer.identity();
		pointRenderer.translate(-(Gdx.graphics.getWidth() / 2f), -(Gdx.graphics.getHeight() / 2f), -1f);

		if(boxRenderer == null) boxRenderer = new ShapeRenderer();
		boxRenderer.setProjectionMatrix(camera.combined);

		Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl.glClearColor(0, 0, 0, 1);

		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

		gl.glClearColor(0.2235f, 0.2235f, 0.2235f, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

		Tesselate();

        tesselators.world.render();

        GlRenderer.waterShaderInfo.setScrollSpeed(0f);
        tesselators.water.render();

        GlRenderer.waterShaderInfo.setScrollSpeed(0.03f);
        tesselators.waterfall.render();

        gl.glDepthFunc(GL20.GL_LEQUAL);

        GlRenderer.waterEdgeShaderInfo.setAttribute("u_noise_mod", 1f);
        GlRenderer.waterEdgeShaderInfo.setAttribute("u_waveMod", 1f);

        Gdx.gl.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
        Gdx.gl.glPolygonOffset(-1f, -1f);

        tesselators.waterEdges.render();

        GlRenderer.waterEdgeShaderInfo.setAttribute("u_noise_mod", 4f);
        GlRenderer.waterEdgeShaderInfo.setAttribute("u_waveMod", 0f);

        tesselators.waterfallEdges.render();

		gl.glDisable(GL20.GL_CULL_FACE);

        Gdx.gl.glDisable(GL20.GL_POLYGON_OFFSET_FILL);

		camera.far = 500f;
		camera.update();

		if(player == null) {
			for(int i = 0; i < level.editorMarkers.size; i++) {
				renderMarker(level.editorMarkers.get(i));
			}
		}

		// set selection data
		for(int i = 0; i < level.entities.size; i++) {
			Entity e = level.entities.get(i);
			if(e == pickedEntity) e.editorState = EditorState.picked;
			else if(additionalSelected.contains(e, true)) e.editorState = EditorState.picked;
			else if(e == hoveredEntity) e.editorState = EditorState.hovered;
			else e.editorState = EditorState.none;

			if(e.editorState != EditorState.none) {
				selectedEntities.add(e);
			}
		}

		for(int i = 0; i < level.non_collidable_entities.size; i++) {
			Entity e = level.non_collidable_entities.get(i);
			if(e == pickedEntity) e.editorState = EditorState.picked;
			else if(e == hoveredEntity) e.editorState = EditorState.hovered;
			else e.editorState = EditorState.none;

			if(e.editorState != EditorState.none) {
				selectedEntities.add(e);
			}
		}

		// draw entities
		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);

		renderer.renderEntities(level);
		renderer.renderMeshes();

		Gdx.gl.glEnable(GL20.GL_ALPHA);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		if(staticMeshBatch == null) {
			staticMeshCollisionTriangles.clear();
			EditorCachePools.freeAllCaches();

			// sort the mesh entities into buckets, determined by their texture
			HashMap<String, Array<Entity>> meshesByTexture = new HashMap<String, Array<Entity>>();
			groupStaticMeshesByTexture(level.entities, meshesByTexture);

			// make a static mesh from each entity bucket
			staticMeshBatch = new HashMap<String, Array<Mesh>>();
			for(String key : meshesByTexture.keySet()) {
				staticMeshBatch.put(key, mergeStaticMeshes(level, meshesByTexture.get(key)));
			}

			// cleanup, don't need the mesh buckets anmyore
			meshesByTexture.clear();

			refreshTriangleSpatialHash();
		}
		if(staticMeshBatch != null) {
			for(Entry<String, Array<Mesh>> meshBuckets : staticMeshBatch.entrySet()) {
				Texture t = Art.cachedTextures.get(meshBuckets.getKey());
				if(t != null) GlRenderer.bindTexture(t);
				if(meshBuckets.getValue() != null) {
					GlRenderer.worldShaderInfo.setAttributes(camera.combined,
							0,
							1000,
							1000,
							time,
							Color.BLACK,
							Color.BLACK);

					GlRenderer.worldShaderInfo.begin();
					for(Mesh m : meshBuckets.getValue()) {
						m.render(GlRenderer.worldShaderInfo.shader, GL20.GL_TRIANGLES);
					}
					GlRenderer.worldShaderInfo.end();
				}
			}
		}

		renderer.renderDecals();
		renderer.renderStencilPasses();
		renderer.renderTransparentEntities();

		renderer.clearBoundTexture();
		renderer.renderSelectedEntities(selectedEntities, EditorState.hovered, hoveredColor, GL20.GL_DST_COLOR, GL20.GL_ZERO);
		renderer.clearBoundTexture();
		renderer.renderSelectedEntities(selectedEntities, EditorState.picked, selectedColor, GL20.GL_DST_COLOR, GL20.GL_ZERO);
		selectedEntities.clear();

		Gdx.gl20.glClear(GL20.GL_STENCIL_BUFFER_BIT);

        // draw a grid
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_ALPHA);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		GlRenderer.EnableBlending(true);

		if(gridMesh != null && player == null) {
			GlRenderer.worldShaderInfo.setAttributes(camera.combined,
					0,
					10,
					90,
					time,
                    Color.BLACK,
					Color.BLACK);

			Gdx.gl.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_ONE);
			Gdx.gl.glDepthMask(false);

			GlRenderer.worldShaderInfo.begin();
			gridMesh.render(GlRenderer.worldShaderInfo.shader,GL20.GL_LINES);
			GlRenderer.worldShaderInfo.end();

			Gdx.gl.glDepthMask(true);
		}

		Gdx.gl.glEnable(GL20.GL_ALPHA);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);

		spriteBatch.flush();

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		pointBatch.flush();
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		decalPool.freeAll(usedDecals);
		usedDecals.clear();

		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_ALPHA);


		boolean shouldDrawBox = !pickedSurface.isPicked;
		if(pickedSurface.isPicked && editorInput.isButtonPressed(Input.Buttons.LEFT) && !editorUi.isShowingMenuOrModal()) {
			shouldDrawBox = true;
		}

		if(pickedEntity == null && hoveredEntity == null || tileDragging) {
			if(!selected || (!(pickedControlPoint != null || movingControlPoint) &&
                    editorInput.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())) {

				Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
				Vector3 start = tempVec1.set(ray.origin);
				Vector3 end = tempVec2.set(intpos.x, intpos.y, intpos.z);
				Float distance = start.sub(end).len();

				end = ray.getEndPoint(rayOutVector, distance + 0.005f);

				selectionX = (int)end.x;
				selectionY = (int)end.z;

				if(selectionX < 0) selectionX = 0;
				if(selectionX >= level.width) selectionX = level.width - 1;

				if(selectionY < 0) selectionY = 0;
				if(selectionY >= level.height) selectionY = level.height - 1;

                selectionWidth = selectionHeight = 1;
			}
			else if(editorInput.isButtonPressed(Input.Buttons.LEFT)) {
				if(Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
					Tile pTile = level.getTile((int)intpos.x, (int)intpos.z);

					if(pickedSurface.tileSurface == TileSurface.Floor) {
						pickedControlPoint = new ControlPoint(new Vector3(intpos.x, pTile.getFloorHeight(0.5f, 0.5f), intpos.z), ControlPointType.floor);
					}
					else if(pickedSurface.tileSurface == TileSurface.Ceiling) {
						pickedControlPoint = new ControlPoint(new Vector3(intpos.x, pTile.getCeilHeight(0.5f, 0.5f), intpos.z), ControlPointType.ceiling);
					}

					selected = true;
					movingControlPoint = true;
				}
				else {

					Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), p, intpos);

					if (pickedControlPoint != null || movingControlPoint) {
						// don't modify selection area!
						movingControlPoint = true;
					} else {

						float selectXMod = 0.8f;
						float selectYMod = 0.8f;
						if (intpos.x < selectionX) selectXMod *= -1f;
						if (intpos.z < selectionY) selectYMod *= -1f;

						selectionWidth = ((int) (intpos.x + selectXMod) - selectionX);
						selectionHeight = ((int) (intpos.z + selectYMod) - selectionY);

						if (selectionWidth == 0) selectionWidth = 1;
						if (selectionHeight == 0) selectionHeight = 1;

						controlPoints.clear();
					}
				}

				tileDragging = true;
			}
			else {
				if(movingControlPoint) {
                    controlPoints.clear();
                }
				pickedControlPoint = null;
				movingControlPoint = false;
			}

            GlRenderer.bindTexture(selectionTex);

			int scaleWidth = selectionWidth;
			int scaleHeight = selectionHeight;
			int selX = selectionX;
			int selY = selectionY;

			Tile t = level.getTileOrNull(selX,selY);
			if(t!= null && !t.renderSolid) {
				float selZ = t.floorHeight;
				if(intpos != null) {
					selZ = (int)((intpos.y - 0.001f) * 16) / 16f;
				}

				if(!selected) selectionHeights.set(t.ceilHeight, selZ);
			}
			else {
				TextureAtlas atlas = TextureAtlas.getRepeatingAtlasByIndex(pickedWallTextureAtlas);
				float size = atlas.rowScale * atlas.scale;
				if(!selected) selectionHeights.set(size - 0.5f, -0.5f);
			}

			if(slopePointMode || slopeEdgeMode)
				drawSlopeLines(slopeSelNum, slopeEdgeMode);

			if(scaleWidth < 0) selX += 1;
			if(scaleHeight < 0) selY += 1;

			float xOffset = scaleWidth > 0 ? 0.015f : - 0.015f;
			float yOffset = scaleHeight > 0 ? 0.015f : - 0.015f;

			if(shouldDrawBox) {
				boxRenderer.translate(selX + xOffset, selectionHeights.x + 0.008f, selY + yOffset);
				boxRenderer.setColor(0.75f, 0.75f, 0.75f, 0.5f);
				boxRenderer.begin(ShapeType.Line);

				boxRenderer.box(0, 0, 0, scaleWidth - (xOffset * 2f), (selectionHeights.y - selectionHeights.x) - 0.015f, -scaleHeight + (yOffset * 2f));
				boxRenderer.end();
				boxRenderer.identity();
			}
		}

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		if(pickedEntity instanceof ProjectedDecal) renderProjection(((ProjectedDecal)pickedEntity).perspective);
		else if(pickedEntity instanceof Mover) renderMoverVizualization((Mover)pickedEntity);
		for(Entity picked : additionalSelected) {
			if(picked instanceof ProjectedDecal) renderProjection(((ProjectedDecal)picked).perspective);
			else if(picked instanceof Mover) renderMoverVizualization((Mover)picked);
		}

		// ROTATE
		if(moveMode == MoveMode.ROTATE && pickedEntity instanceof Directional) {
			Directional pickedDirectional = (Directional) pickedEntity;
			dragPlane = new Plane(new Vector3(0,-1,0), pickedEntity.z);

			if(Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intpos)) {

				if(rotateStart == null) {
					rotateStart = new Vector3(pickedDirectional.getRotation());
					rotateStartIntersection = new Vector3(intpos);
				}

				Vector3 rotateDirection = new Vector3(
						pickedEntity.x - intpos.x,
						pickedEntity.y - intpos.z,
						pickedEntity.z - intpos.y).nor();

				Vector3 rotateStartDirection = new Vector3(
						pickedEntity.x - rotateStartIntersection.x,
						pickedEntity.y - rotateStartIntersection.z,
						pickedEntity.z - rotateStartIntersection.y).nor();

				float yaw = (float)Math.atan2(rotateDirection.x, rotateDirection.y);
				float startYaw = (float)Math.atan2(rotateStartDirection.x, rotateStartDirection.y);

				if(dragMode == DragMode.X) {
					pickedDirectional.setRotation(rotateStart.x + (yaw - startYaw) * 57.2957795f, rotateStart.y, rotateStart.z);
				}
				else if (dragMode == DragMode.Y) {
					pickedDirectional.setRotation(rotateStart.x, rotateStart.y + (yaw - startYaw) * 57.2957795f, rotateStart.z);
				}
				else {
					pickedDirectional.setRotation(rotateStart.x, rotateStart.y,rotateStart.z + (yaw - startYaw) * 57.2957795f);
				}

				if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
					float rx = pickedDirectional.getRotation().x;
					float ry = pickedDirectional.getRotation().y;
					float rz = pickedDirectional.getRotation().z;

					if(dragMode == DragMode.X)
						//pickedDirectional.getRotation().x = (int)(Math.floor(pickedDirectional.getRotation().x) * 0.04444444444444) / 0.04444444444444f;
						pickedDirectional.setRotation((int)(Math.floor(pickedDirectional.getRotation().x) * 0.04444444444444) / 0.04444444444444f, ry, rz);
					else if (dragMode == DragMode.Y)
						//pickedDirectional.getRotation().y = (int)(Math.floor(pickedDirectional.getRotation().y) * 0.04444444444444) / 0.04444444444444f;
						pickedDirectional.setRotation(rx, (int)(Math.floor(pickedDirectional.getRotation().y) * 0.04444444444444) / 0.04444444444444f, rz);
					else
						//pickedDirectional.getRotation().z = (int)(Math.floor(pickedDirectional.getRotation().z) * 0.04444444444444) / 0.04444444444444f;
						pickedDirectional.setRotation(rx, ry, (int)(Math.floor(pickedDirectional.getRotation().z) * 0.04444444444444) / 0.04444444444444f);
				}

				refreshEntity(pickedEntity);
			}
		}

		// drag tile
		if(selected && (!readLeftClick || movingControlPoint)) {
			int selX = selectionX;
			int selY = selectionY;
			int selWidth = selectionWidth;
			int selHeight = selectionHeight;

			if(selWidth < 0) {
				selX = selX + selWidth;
				selWidth *= -1;
				selX += 1;
			}
			if(selHeight < 0) {
				selY = selY + selHeight;
				selHeight *= -1;
				selY += 1;
			}

			Tile startTile = level.getTile(selectionX, selectionY);

			// init the control point list if needed
			if(controlPoints.size == 0) {

				if(!vertexSelectionMode) {
					// floor and ceiling control points
					controlPoints.add(new ControlPoint(new Vector3(selX + (selWidth / 2f), startTile.floorHeight, selY + (selHeight / 2f)), ControlPointType.floor));
					controlPoints.add(new ControlPoint(new Vector3(selX + (selWidth / 2f), startTile.ceilHeight, selY + (selHeight / 2f)), ControlPointType.ceiling));

					// ceiling edges
					Vector3 northEdgeCeil = new Vector3(selX + (selWidth / 2f), startTile.ceilHeight, selY);
					Vector3 southEdgeCeil = new Vector3(selX + (selWidth / 2f), startTile.ceilHeight, selY + selHeight);
					Vector3 westEdgeCeil = new Vector3(selX, startTile.ceilHeight, selY + (selHeight / 2f));
					Vector3 eastEdgeCeil = new Vector3(selX + selWidth, startTile.ceilHeight, selY + (selHeight / 2f));

					controlPoints.add(new ControlPoint(northEdgeCeil, ControlPointType.northCeil));
					controlPoints.add(new ControlPoint(southEdgeCeil, ControlPointType.southCeil));
					controlPoints.add(new ControlPoint(westEdgeCeil, ControlPointType.westCeil));
					controlPoints.add(new ControlPoint(eastEdgeCeil, ControlPointType.eastCeil));

					// floor edges
					Vector3 northEdgeFloor = new Vector3(selX + (selWidth / 2f), startTile.floorHeight, selY);
					Vector3 southEdgeFloor = new Vector3(selX + (selWidth / 2f), startTile.floorHeight, selY + selHeight);
					Vector3 westEdgeFloor = new Vector3(selX, startTile.floorHeight, selY + (selHeight / 2f));
					Vector3 eastEdgeFloor = new Vector3(selX + selWidth, startTile.floorHeight, selY + (selHeight / 2f));

					controlPoints.add(new ControlPoint(northEdgeFloor, ControlPointType.northFloor));
					controlPoints.add(new ControlPoint(southEdgeFloor, ControlPointType.southFloor));
					controlPoints.add(new ControlPoint(westEdgeFloor, ControlPointType.westFloor));
					controlPoints.add(new ControlPoint(eastEdgeFloor, ControlPointType.eastFloor));
				}
				else {
					for(int xx = selX; xx < selX + selWidth; xx++) {
						for(int yy = selY; yy < selY + selHeight; yy++) {
							Tile current = level.getTileOrNull(xx, yy);

							if(current != null && !current.renderSolid) {
								if(current.tileSpaceType != TileSpaceType.OPEN_SE) {
									controlPoints.add(new ControlPoint(new Vector3(xx, current.ceilHeight + current.ceilSlopeNE, yy), new ControlPointVertex(current,ControlVertex.ceilNE)));
									controlPoints.add(new ControlPoint(new Vector3(xx, current.floorHeight + current.slopeNE, yy), new ControlPointVertex(current,ControlVertex.slopeNE)));
								}

								if(current.tileSpaceType != TileSpaceType.OPEN_SW) {
									controlPoints.add(new ControlPoint(new Vector3(xx + 1, current.ceilHeight + current.ceilSlopeNW, yy), new ControlPointVertex(current,ControlVertex.ceilNW)));
									controlPoints.add(new ControlPoint(new Vector3(xx + 1, current.floorHeight + current.slopeNW, yy), new ControlPointVertex(current,ControlVertex.slopeNW)));
								}

								if(current.tileSpaceType != TileSpaceType.OPEN_NE) {
									controlPoints.add(new ControlPoint(new Vector3(xx, current.ceilHeight + current.ceilSlopeSE, yy + 1), new ControlPointVertex(current,ControlVertex.ceilSE)));
									controlPoints.add(new ControlPoint(new Vector3(xx, current.floorHeight + current.slopeSE, yy + 1), new ControlPointVertex(current,ControlVertex.slopeSE)));
								}

								if(current.tileSpaceType != TileSpaceType.OPEN_NW) {
									controlPoints.add(new ControlPoint(new Vector3(xx + 1, current.ceilHeight + current.ceilSlopeSW, yy + 1), new ControlPointVertex(current,ControlVertex.ceilSW)));
									controlPoints.add(new ControlPoint(new Vector3(xx + 1, current.floorHeight + current.slopeSW, yy + 1), new ControlPointVertex(current,ControlVertex.slopeSW)));
								}
							}
						}
					}

					// filter out duplicate vertices
					ArrayMap<String, ControlPoint> reduceMap = new ArrayMap<String, ControlPoint>();
					for(ControlPoint point : controlPoints) {
						String key = point.point.x + "," + point.point.y + "," + point.point.z;
						ControlPoint found = reduceMap.get(key);

						if(found != null) found.vertices.addAll(point.vertices);
						else reduceMap.put(key, point);
					}

					controlPoints.clear();
					for(ControlPoint point : reduceMap.values()) {
						controlPoints.add(point);
					}
				}
			}

			Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			if(!movingControlPoint) pickedControlPoint = null;
			for(ControlPoint point : controlPoints) {
				if(!editorUi.isShowingContextMenu()) {
					if (pickedControlPoint == null && Intersector.intersectRaySphere(ray, point.point, 0.12f, intersection)) {
						pickedControlPoint = point;
					}
				}

				if(!movingControlPoint || pickedControlPoint == point)
					drawPoint(point.point, 5f, pickedControlPoint == point ? Color.WHITE : controlPointColor);
			}

			// draw lines
			if(!vertexSelectionMode) {
				for(int xx = selX; xx < selX + selWidth; xx++) {
					Tile north = level.getTile(xx, selY);
					Tile south = level.getTile(xx, selY + selHeight - 1);

					// ceil north
					drawLine(tempVec1.set(xx, north.ceilSlopeNE + north.ceilHeight, selY), tempVec2.set(xx + 1f,north.ceilSlopeNW + north.ceilHeight,selY), 2f, pickedControlPoint != null && pickedControlPoint.isNorthCeiling() ? Color.WHITE : Color.RED);

					// ceil south
					drawLine(tempVec1.set(xx, south.ceilSlopeSE + south.ceilHeight, selY + selHeight), tempVec2.set(xx + 1f,south.ceilSlopeSW + south.ceilHeight,selY + selHeight), 2f, pickedControlPoint != null && pickedControlPoint.isSouthCeiling() ? Color.WHITE : Color.RED);

					// floor north
					drawLine(tempVec1.set(xx, north.slopeNE + north.floorHeight, selY), tempVec2.set(xx + 1f,north.slopeNW + north.floorHeight,selY), 2f, pickedControlPoint != null && pickedControlPoint.isNorthFloor() ? Color.WHITE : Color.RED);

					// floor south
					drawLine(tempVec1.set(xx, south.slopeSE + south.floorHeight, selY + selHeight), tempVec2.set(xx + 1f,south.slopeSW + south.floorHeight,selY + selHeight), 2f, pickedControlPoint != null && pickedControlPoint.isSouthFloor() ? Color.WHITE : Color.RED);
				}

				for(int yy = selY; yy < selY + selHeight; yy++) {
					Tile west = level.getTile(selX, yy);
					Tile east = level.getTile(selX + selWidth - 1, yy);

					// ceil west
					drawLine(tempVec1.set(selX, west.ceilSlopeNE + west.ceilHeight, yy), tempVec2.set(selX,west.ceilSlopeSE + west.ceilHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isWestCeiling() ? Color.WHITE : Color.RED);

					// ceil east
					drawLine(tempVec1.set(selX + selWidth, east.ceilSlopeNW + east.ceilHeight, yy), tempVec2.set(selX + selWidth,east.ceilSlopeSW + east.ceilHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isEastCeiling() ? Color.WHITE : Color.RED);

					// floor west
					drawLine(tempVec1.set(selX, west.slopeNE + west.floorHeight, yy), tempVec2.set(selX,west.slopeSE + west.floorHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isWestFloor() ? Color.WHITE : Color.RED);

					// floor east
					drawLine(tempVec1.set(selX + selWidth, east.slopeNW + east.floorHeight, yy), tempVec2.set(selX + selWidth,east.slopeSW + east.floorHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isEastFloor() ? Color.WHITE : Color.RED);
				}
			}
		}

		if(movingControlPoint && pickedControlPoint != null) {
			if(dragPlane == null) {
				Vector3 vertDir = t_dragVector.set(camera.direction);
				vertDir.y = 0;

				t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
				Plane vert = t_dragPlane;

				float len = vert.distance(pickedControlPoint.point);

				t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
				dragPlane = t_dragPlane;
			}

			if(dragStart == null) {
				dragStart = t_dragVector.set(pickedControlPoint.point);
			}

			if(Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intpos)) {

				intpos.y = (int)(intpos.y * 16) / 16f;

				dragOffset = tempVec2.set(dragStart.x - intpos.x,dragStart.y - intpos.y,dragStart.z - intpos.z);

				int selX = selectionX;
				int selY = selectionY;
				int selWidth = selectionWidth;
				int selHeight = selectionHeight;

				if(selWidth < 0) {
					selX = selX + selWidth;
					selWidth *= -1;
					selX += 1;
				}
				if(selHeight < 0) {
					selY = selY + selHeight;
					selHeight *= -1;
					selY += 1;
				}

				for(int x = selX; x < selX + selWidth; x++) {
					for(int y = selY; y < selY + selHeight; y++) {
						Tile t = level.getTileOrNull(x,y);
						if(t != null) {
							if(pickedControlPoint.controlPointType == ControlPointType.floor) {
								t.floorHeight -= dragOffset.y;
								t.packHeights();

                                if(t.getMinOpenHeight() < 0f) {
                                    t.compressFloorAndCeiling(true);
                                }
							}
							else if(pickedControlPoint.controlPointType == ControlPointType.ceiling) {
								t.ceilHeight -= dragOffset.y;
								t.packHeights();

                                if(t.getMinOpenHeight() < 0f) {
                                    t.compressFloorAndCeiling(false);
                                }
							}
							else if(pickedControlPoint.controlPointType == ControlPointType.northCeil || pickedControlPoint.controlPointType == ControlPointType.northFloor) {

								float mod = 1 - ((float)y - (float)selY) / (float)selHeight;
								if(pickedControlPoint.controlPointType == ControlPointType.northCeil) {
									t.ceilSlopeNE -= dragOffset.y * mod;
									t.ceilSlopeNW -= dragOffset.y * mod;
								}
								else {
									t.slopeNE -= dragOffset.y * mod;
									t.slopeNW -= dragOffset.y * mod;
								}

								if(selHeight > 1) {
									mod = 1 - ((float)y - (float)selY + 1f) / (float)selHeight;
									if(pickedControlPoint.controlPointType == ControlPointType.northCeil) {
										t.ceilSlopeSE -= dragOffset.y * mod;
										t.ceilSlopeSW -= dragOffset.y * mod;
									}
									else {
										t.slopeSE -= dragOffset.y * mod;
										t.slopeSW -= dragOffset.y * mod;
									}
								}
								t.packHeights();
							}
							else if(pickedControlPoint.controlPointType == ControlPointType.southCeil || pickedControlPoint.controlPointType == ControlPointType.southFloor) {

								float mod = ((float)y - (float)selY + 1) / (float)selHeight;
								if(pickedControlPoint.controlPointType == ControlPointType.southCeil) {
									t.ceilSlopeSE -= dragOffset.y * mod;
									t.ceilSlopeSW -= dragOffset.y * mod;
								}
								else {
									t.slopeSE -= dragOffset.y * mod;
									t.slopeSW -= dragOffset.y * mod;
								}

								if(selHeight > 1) {
									mod = ((float)y - (float)selY) / (float)selHeight;
									if(pickedControlPoint.controlPointType == ControlPointType.southCeil) {
										t.ceilSlopeNE -= dragOffset.y * mod;
										t.ceilSlopeNW -= dragOffset.y * mod;
									}
									else {
										t.slopeNE -= dragOffset.y * mod;
										t.slopeNW -= dragOffset.y * mod;
									}
								}
								t.packHeights();
							}
							else if(pickedControlPoint.controlPointType == ControlPointType.westCeil || pickedControlPoint.controlPointType == ControlPointType.westFloor) {

								float mod = 1 - ((float)x - (float)selX) / (float)selWidth;
								if(pickedControlPoint.controlPointType == ControlPointType.westCeil) {
									t.ceilSlopeNE -= dragOffset.y * mod;
									t.ceilSlopeSE -= dragOffset.y * mod;
								}
								else {
									t.slopeNE -= dragOffset.y * mod;
									t.slopeSE -= dragOffset.y * mod;
								}

								if(selWidth > 1) {
									mod = 1 - ((float)x - (float)selX + 1f) / (float)selWidth;
									if(pickedControlPoint.controlPointType == ControlPointType.westCeil) {
										t.ceilSlopeNW -= dragOffset.y * mod;
										t.ceilSlopeSW -= dragOffset.y * mod;
									}
									else {
										t.slopeNW -= dragOffset.y * mod;
										t.slopeSW -= dragOffset.y * mod;
									}
								}
								t.packHeights();
							}
							else if(pickedControlPoint.controlPointType == ControlPointType.eastCeil || pickedControlPoint.controlPointType == ControlPointType.eastFloor) {

								float mod = ((float)x - (float)selX + 1) / (float)selWidth;
								if(pickedControlPoint.controlPointType == ControlPointType.eastCeil) {
									t.ceilSlopeNW -= dragOffset.y * mod;
									t.ceilSlopeSW -= dragOffset.y * mod;
								}
								else {
									t.slopeNW -= dragOffset.y * mod;
									t.slopeSW -= dragOffset.y * mod;
								}

								if(selWidth > 1) {
									mod = ((float)x - (float)selX) / (float)selWidth;
									if(pickedControlPoint.controlPointType == ControlPointType.eastCeil) {
										t.ceilSlopeNE -= dragOffset.y * mod;
										t.ceilSlopeSE -= dragOffset.y * mod;
									}
									else {
										t.slopeNE -= dragOffset.y * mod;
										t.slopeSE -= dragOffset.y * mod;
									}
								}
								t.packHeights();
							}
						}
					}
				}

				if(pickedControlPoint.controlPointType == ControlPointType.vertex) {
					for(ControlPointVertex v : pickedControlPoint.vertices) {
						Tile t = v.tile;

						if(v.vertex == ControlVertex.ceilNE) {
							t.ceilSlopeNE = pickedControlPoint.point.y - t.ceilHeight;
						}
						else if(v.vertex == ControlVertex.ceilSE) {
							t.ceilSlopeSE = pickedControlPoint.point.y - t.ceilHeight;
						}
						else if(v.vertex == ControlVertex.ceilNW) {
							t.ceilSlopeNW = pickedControlPoint.point.y - t.ceilHeight;
						}
						else if(v.vertex == ControlVertex.ceilSW) {
							t.ceilSlopeSW = pickedControlPoint.point.y - t.ceilHeight;
						}
						else if(v.vertex == ControlVertex.slopeNE) {
							t.slopeNE = pickedControlPoint.point.y - t.floorHeight;
						}
						else if(v.vertex == ControlVertex.slopeSE) {
							t.slopeSE = pickedControlPoint.point.y - t.floorHeight;
						}
						else if(v.vertex == ControlVertex.slopeNW) {
							t.slopeNW = pickedControlPoint.point.y - t.floorHeight;
						}
						else if(v.vertex == ControlVertex.slopeSW) {
							t.slopeSW = pickedControlPoint.point.y - t.floorHeight;
						}
						t.packHeights();
					}
				}

				pickedControlPoint.point.y -= dragOffset.y;

				refresh();

				dragStart.set(intpos);
			}
		}
		else {
			// Drag entities around
			if(movingEntity == true && pickedEntity != null) {
				if(dragPlane == null) {

					if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
						// Make a copy
						Entity copy = Game.fromJson(pickedEntity.getClass(), Game.toJson(pickedEntity));
						level.entities.add(copy);

                        pickEntity(copy);

						Array<Entity> copies = new Array<Entity>();
						for(Entity selected : additionalSelected) {
							Entity newCopy = Game.fromJson(selected.getClass(), Game.toJson(selected));
							level.entities.add(newCopy);
							copies.add(newCopy);
						}

						additionalSelected.clear();
						additionalSelected.addAll(copies);
                        editorUi.showEntityPropertiesMenu(this);
					}

					if(dragMode == DragMode.Y) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0f);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(pickedEntity.x, pickedEntity.z, pickedEntity.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(dragMode == DragMode.Z) {
						Vector3 vertDir = t_dragVector.set(camera.direction);
						vertDir.y = 0;

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(pickedEntity.x, pickedEntity.z, pickedEntity.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(dragMode == DragMode.X) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(pickedEntity.x, pickedEntity.z, pickedEntity.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(pickedEntity.x, pickedEntity.z - 0.5f, pickedEntity.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);

						dragPlane = t_dragPlane;
						dragMode = DragMode.XY;
					}
					else {
						Vector3 vertDir = t_dragVector.set(camera.direction);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(pickedEntity.x, pickedEntity.z, pickedEntity.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
				}

				if(dragStart == null)
					dragStart = new Vector3(pickedEntity.x, pickedEntity.y, pickedEntity.z);

				if(moveMode == MoveMode.DRAG && Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intpos)) {
					if(dragOffset == null) {
						dragOffset = t_dragOffset.set(pickedEntity.x - intpos.x, pickedEntity.y - intpos.z, pickedEntity.z - intpos.y);
					}

					float startX = pickedEntity.x;
					float startY = pickedEntity.y;
					float startZ = pickedEntity.z;

					pickedEntity.x = intpos.x + dragOffset.x;
					pickedEntity.y = intpos.z + dragOffset.y;
					pickedEntity.z = intpos.y + dragOffset.z;

					if(dragMode == DragMode.XY) {
						pickedEntity.z = dragStart.z;
					}
					if(dragMode == DragMode.Y) {
						pickedEntity.z = dragStart.z;
						pickedEntity.y = dragStart.y;
					}
					else if(dragMode == DragMode.Z) {
						pickedEntity.x = dragStart.x;
						pickedEntity.y = dragStart.y;
					}
					else if(dragMode == DragMode.X) {
						pickedEntity.x = dragStart.x;
					}

					if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
						pickedEntity.x = (int)(pickedEntity.x * 8) / 8f;
						pickedEntity.y = (int)(pickedEntity.y * 8) / 8f;
						pickedEntity.z = (int)(pickedEntity.z * 8) / 8f;
					}

					float movedX = startX - pickedEntity.x;
					float movedY = startY - pickedEntity.y;
					float movedZ = startZ - pickedEntity.z;

					for(Entity selected : additionalSelected) {
						selected.x -= movedX;
						selected.y -= movedY;
						selected.z -= movedZ;
					}

                    refreshEntity(pickedEntity);
                    for(Entity selected : additionalSelected) {
                        refreshEntity(selected);
                    }
				}
			}
			else {
				dragOffset = null;
				dragStart = null;
				dragPlane = null;
			}

			if(pickedEntity == null) {
				dragPlane = null;
				dragMode = DragMode.NONE;
				moveMode = MoveMode.DRAG;
			}

			// Draw rotation circles
			if(moveMode == MoveMode.ROTATE) {
				if(dragMode == DragMode.X) {
					drawXCircle(pickedEntity.x, pickedEntity.z - 0.49f, pickedEntity.y, 2f, Color.GREEN);
				}
				else if(dragMode == DragMode.Y) {
					drawYCircle(pickedEntity.x, pickedEntity.z - 0.49f, pickedEntity.y, 2f, Color.RED);
				}
				else {
					drawZCircle(pickedEntity.x, pickedEntity.z - 0.49f, pickedEntity.y, 2f, Color.BLUE);
				}

				if(pickedEntity instanceof Directional) {
					Directional dirEntity = (Directional)pickedEntity;

					Vector3 dirEnd = dirEntity.getDirection();
					dirEnd.x += pickedEntity.x;
					dirEnd.y += pickedEntity.y;
					dirEnd.z += pickedEntity.z;

					drawLine(new Vector3(pickedEntity.x, pickedEntity.z - 0.49f, pickedEntity.y), new Vector3(dirEnd.x, dirEnd.z - 0.49f,dirEnd.y), 2f, Color.WHITE);
				}
			}

			if(pickedEntity != null && ((hoveredEntity == null || additionalSelected.contains(hoveredEntity, true)) || hoveredEntity == pickedEntity || movingEntity)) {
				Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
				Gdx.gl.glEnable(GL20.GL_ALPHA);
				Gdx.gl.glEnable(GL20.GL_BLEND);

				if(moveMode == MoveMode.DRAG) {
					if(dragMode == DragMode.Z) {
						Vector3 startLine = tempVec3.set(pickedEntity.x, pickedEntity.z - 10f, pickedEntity.y);
						Vector3 endLine = tempVec4.set(pickedEntity.x, pickedEntity.z + 10f, pickedEntity.y);
						this.drawLine(startLine, endLine, 2, Color.BLUE);
					}
					else if(dragMode == DragMode.X) {
						Vector3 startLine = tempVec3.set(pickedEntity.x, pickedEntity.z - 0.5f, pickedEntity.y - 10f);
						Vector3 endLine = tempVec4.set(pickedEntity.x, pickedEntity.z - 0.5f, pickedEntity.y + 10f);
						this.drawLine(startLine, endLine, 2, Color.GREEN);
					}
					else if(dragMode == DragMode.Y) {
						Vector3 startLine = tempVec3.set(pickedEntity.x - 10f, pickedEntity.z - 0.5f, pickedEntity.y);
						Vector3 endLine = tempVec4.set(pickedEntity.x + 10f, pickedEntity.z - 0.5f, pickedEntity.y);
						this.drawLine(startLine, endLine, 2, Color.RED);
					}
					else if(dragMode == DragMode.XY || (!movingEntity && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))) {
						Vector3 startLine = tempVec3.set(pickedEntity.x, pickedEntity.z - 0.5f, pickedEntity.y - 10f);
						Vector3 endLine = tempVec4.set(pickedEntity.x, pickedEntity.z - 0.5f, pickedEntity.y + 10f);
						this.drawLine(startLine, endLine, 2, Color.GREEN);

						startLine = tempVec3.set(pickedEntity.x - 10f, pickedEntity.z - 0.5f, pickedEntity.y);
						endLine = tempVec4.set(pickedEntity.x + 10f, pickedEntity.z - 0.5f, pickedEntity.y);
						this.drawLine(startLine, endLine, 2, Color.RED);
					}
				}
			}
		}

		if(!selected) {
			pickedControlPoint = null;
			controlPoints.clear();
		}

		Gdx.gl.glDisable( GL20.GL_ALPHA );
		Gdx.gl.glDisable(GL20.GL_BLEND);

		if(messageTimer > 0) {
			float fontSize = Math.min(renderer.camera2D.viewportWidth, renderer.camera2D.viewportHeight) / 15;
			renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
			renderer.uiBatch.begin();

			float xOffset = message.length() / 2.0f;
			float yPos = (int)((1 * -fontSize * 1.2) / 2 + fontSize * 1.2);

			renderer.drawText(message, -xOffset * fontSize + fontSize * 0.1f, -yPos, fontSize, Color.BLACK);
			renderer.drawText(message, -xOffset * fontSize, -yPos, fontSize, Color.WHITE);

			renderer.uiBatch.end();
		}

		// Render shapes!
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_ALPHA);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		Gdx.gl.glLineWidth(3f);
		//Gdx.gl.glPointSize(6f);

		lineRenderer.end();
		pointRenderer.end();

		Gdx.gl.glLineWidth(1);
		//Gdx.gl.glPointSize(1f);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		if(player == null) {
			lineRenderer.begin(ShapeType.Line);
			drawLine(xGridStart, xGridEnd, 2f, colorDarkRed);
			drawLine(yGridStart, yGridEnd, 2f, colorDarkGreen);
			lineRenderer.end();
		}

		Gdx.gl.glDisable(GL20.GL_BLEND);
		renderCollisionBoxes();
		renderTriggerLines();
	}

	private void GlPickEntity() {
		if(pickerFrameBuffer != null && pickerPixelBuffer != null && renderer.entitiesForPicking != null) {
			try {
				pickerFrameBuffer.begin();

				int pickX = Gdx.input.getX();
				int pickY = Gdx.graphics.getHeight() - Gdx.input.getY();

				// get the pixels
				Gdx.gl.glReadPixels(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pickerPixelBuffer.getPixels());

				// get the hovered pixel
				int pickedPixel = pickerPixelBuffer.getPixel(pickX, pickY);
				pickerFrameBuffer.end();

				// unpack the entity index
				pickedPixelBufferColor.set(pickedPixel);
				int r = (int)(pickedPixelBufferColor.r * 255);
				int g = (int)(pickedPixelBufferColor.g * 255);
				int b = (int)(pickedPixelBufferColor.b * 255);
				int index = (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);

				hoveredEntity = renderer.entitiesForPicking.get(index);
				//Gdx.app.log("Picking", pickedPixelBufferColor.toString());
			}
			catch (Exception ex) {
				Gdx.app.log("Picking", ex.getMessage());
			}
			finally {
				pickerFrameBuffer.end();
			}
		}

		GlRenderer.EnableBlending(false);
	}

	private void RenderEntitiesForPicking() {
		if(pickerFrameBuffer == null)
			return;

		if(renderer.entitiesForPicking != null)
			renderer.entitiesForPicking.clear();

		// render for picking!
		pickerFrameBuffer.begin();

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);

		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);

		Tesselate();

		// render world, keep depth buffer
		tesselators.world.render();

		GlRenderer.waterShaderInfo.setScrollSpeed(0f);
		tesselators.water.render();

		GlRenderer.waterShaderInfo.setScrollSpeed(0.03f);
		tesselators.waterfall.render();

		GlRenderer.waterEdgeShaderInfo.setAttribute("u_noise_mod", 1f);
		GlRenderer.waterEdgeShaderInfo.setAttribute("u_waveMod", 1f);

		Gdx.gl.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
		Gdx.gl.glPolygonOffset(-1f, -1f);

		tesselators.waterEdges.render();

		GlRenderer.waterEdgeShaderInfo.setAttribute("u_noise_mod", 4f);
		GlRenderer.waterEdgeShaderInfo.setAttribute("u_waveMod", 0f);

		tesselators.waterfallEdges.render();

		Gdx.gl.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// LEQUAL
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);

		renderer.renderEntitiesForPicking(level);
		pickerFrameBuffer.end();
	}

	private void refreshTriangleSpatialHash() {
		GlRenderer.triangleSpatialHash = triangleSpatialHash;
		triangleSpatialHash.Flush();

		// add the collision triangles to the spatial hash
		for(int i = 0; i < staticMeshCollisionTriangles.size; i++) {
			triangleSpatialHash.AddTriangle(staticMeshCollisionTriangles.get(i));
		}

		tesselators.world.addCollisionTriangles(triangleSpatialHash);
		tesselators.water.addCollisionTriangles(triangleSpatialHash);
		tesselators.waterfall.addCollisionTriangles(triangleSpatialHash);
	}

	private void refreshEntity(Entity theEntity) {
		if(theEntity instanceof Light && showLights) {
			refreshLights();
		}
		else if(theEntity instanceof ProjectedDecal) {
			((ProjectedDecal)theEntity).refresh();
		}
		else if(theEntity instanceof Model) {
			if(!((Model)theEntity).isDynamic) staticMeshBatch = null;
		}
		else if(theEntity instanceof Prefab) {
			Array<Entity> contains = ((Prefab)theEntity).entities;
			for(int i = 0; i < contains.size; i++) {
				refreshEntity(contains.get(i));
			}
        }
		else if(theEntity instanceof Group) {
			Array<Entity> contains = ((Group)theEntity).entities;
			for(int i = 0; i < contains.size; i++) {
				refreshEntity(contains.get(i));
			}
		}
	}

	public void setPickedWallTexture(int tex, String wallTexAtlas) {
		pickedWallTexture = tex;
        pickedWallTextureAtlas = wallTexAtlas != null ? wallTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey();
        wallPickerButton.setDrawable(new TextureRegionDrawable(TextureAtlas.getRepeatingAtlasByIndex(pickedWallTextureAtlas).getSprite(tex)));
	}

    public void setPickedWallBottomTexture(int tex, String wallBottomTexAtlas) {
        pickedWallBottomTexture = tex;
        pickedWallBottomTextureAtlas = wallBottomTexAtlas != null ? wallBottomTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey();
        bottomWallPickerButton.setDrawable(new TextureRegionDrawable(TextureAtlas.getRepeatingAtlasByIndex(pickedWallBottomTextureAtlas).getSprite(tex)));
    }

	public void setPickedCeilingTexture(int tex, String ceilTexAtlas) {
		pickedCeilingTexture = tex;
        pickedCeilingTextureAtlas = ceilTexAtlas != null ? ceilTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey();
        ceilPickerButton.setDrawable(new TextureRegionDrawable(TextureAtlas.getRepeatingAtlasByIndex(pickedCeilingTextureAtlas).getSprite(tex)));
	}

	public void setPickedFloorTexture(int tex, String floorTexAtlas) {
		pickedFloorTexture = tex;
        pickedFloorTextureAtlas = floorTexAtlas != null ? floorTexAtlas : TextureAtlas.cachedRepeatingAtlases.firstKey();
        floorPickerButton.setDrawable(new TextureRegionDrawable(TextureAtlas.getRepeatingAtlasByIndex(pickedFloorTextureAtlas).getSprite(tex)));
	}

	public void groupStaticMeshesByTexture(Array<Entity> entities, HashMap<String, Array<Entity>> meshesByTexture) {
		for(Entity e : entities) {
			if(e instanceof Group) {
				groupStaticMeshesByTexture(((Group) e).entities, meshesByTexture);
			}
			else {
				if(e.drawable != null && !e.isDynamic && e.drawable instanceof DrawableMesh) {
					DrawableMesh drbl = (DrawableMesh)e.drawable;

					if(!meshesByTexture.containsKey(drbl.textureFile))  {
						meshesByTexture.put(drbl.textureFile, new Array<Entity>());
					}

					meshesByTexture.get(drbl.textureFile).add(e);
				}
			}
		}
	}

	public void renderCollisionBoxes() {
		lineRenderer.begin(ShapeType.Line);

		if(showCollisionBoxes) {
			for(Entity e : level.entities) {
				if(e.isSolid || e instanceof Area) {
					renderCollisionBox(e);
				}
			}
		} else {
			if(pickedEntity != null) renderCollisionBox(pickedEntity);
			for(Entity selected : additionalSelected) {
				renderCollisionBox(selected);
			}
		}

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glLineWidth(1f);
		lineRenderer.setColor(Color.WHITE);
		lineRenderer.end();
		Gdx.gl.glLineWidth(1f);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	}

	public void renderCollisionBox(Entity e) {
		if(!e.isSolid && !(e instanceof Area)) return;

		float zStart = e.z - 0.5f;
		float zEnd = zStart + e.collision.z;

		lineRenderer.setColor(Color.LIGHT_GRAY);

		lineRenderer.line(e.x - e.collision.x, zStart, e.y + e.collision.y, e.x + e.collision.x, zStart, e.y + e.collision.y);
		lineRenderer.line(e.x - e.collision.x, zStart, e.y - e.collision.y, e.x + e.collision.x, zStart, e.y - e.collision.y);
		lineRenderer.line(e.x + e.collision.x, zStart, e.y - e.collision.y, e.x + e.collision.x, zStart, e.y + e.collision.y);
		lineRenderer.line(e.x - e.collision.x, zStart, e.y - e.collision.y, e.x - e.collision.x, zStart, e.y + e.collision.y);

		lineRenderer.line(e.x - e.collision.x, zEnd, e.y + e.collision.y, e.x + e.collision.x, zEnd, e.y + e.collision.y);
		lineRenderer.line(e.x - e.collision.x, zEnd, e.y - e.collision.y, e.x + e.collision.x, zEnd, e.y - e.collision.y);
		lineRenderer.line(e.x + e.collision.x, zEnd, e.y - e.collision.y, e.x + e.collision.x, zEnd, e.y + e.collision.y);
		lineRenderer.line(e.x - e.collision.x, zEnd, e.y - e.collision.y, e.x - e.collision.x, zEnd, e.y + e.collision.y);

		lineRenderer.line(e.x - e.collision.x, zStart, e.y + e.collision.y, e.x - e.collision.x, zEnd, e.y + e.collision.y);
		lineRenderer.line(e.x - e.collision.x, zStart, e.y - e.collision.y, e.x - e.collision.x, zEnd, e.y - e.collision.y);
		lineRenderer.line(e.x + e.collision.x, zStart, e.y + e.collision.y, e.x + e.collision.x, zEnd, e.y + e.collision.y);
		lineRenderer.line(e.x + e.collision.x, zStart, e.y - e.collision.y, e.x + e.collision.x, zEnd, e.y - e.collision.y);
	}

	public void renderProjection(Camera perspective) {
		if(perspective == null) return;

		for(int i = 0; i < 4; i++) {
			Vector3 startPoint = perspective.frustum.planePoints[i];
			Vector3 endPoint = i != 3 ? perspective.frustum.planePoints[i + 1] : perspective.frustum.planePoints[0];

			lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}

		for(int i = 0; i < 4; i++) {
			Vector3 startPoint = perspective.frustum.planePoints[i];
			Vector3 endPoint = perspective.frustum.planePoints[i + 4];

			lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}

		for(int i = 4; i < 8; i++) {
			Vector3 startPoint = perspective.frustum.planePoints[i];
			Vector3 endPoint = i != 7 ? perspective.frustum.planePoints[i + 1] : perspective.frustum.planePoints[4];

			lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
		}

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glLineWidth(1f);
		lineRenderer.setColor(Color.CYAN);
		lineRenderer.flush();
	}

	public void renderMoverVizualization(Mover e) {
		float zStart = e.z - 0.5f;
		float zEnd = zStart + e.collision.z;

		lineRenderer.setColor(Color.WHITE);

		lineRenderer.line(e.x, e.z - 0.5f, e.y, e.x + e.movesBy.x, e.z + e.movesBy.z - 0.5f, e.y + e.movesBy.y);

		lineRenderer.setColor(Color.LIGHT_GRAY);

		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);

		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);

		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y, e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x - e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x - e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y + e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y + e.collision.y + e.movesBy.y);
		lineRenderer.line(e.x + e.collision.x + e.movesBy.x, zStart + e.movesBy.z, e.y - e.collision.y + e.movesBy.y, e.x + e.collision.x + e.movesBy.x, zEnd + e.movesBy.z, e.y - e.collision.y + e.movesBy.y);

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glLineWidth(1f);
		lineRenderer.setColor(Color.CYAN);
		lineRenderer.flush();
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	}

	public void renderTriggerLines() {
		lineRenderer.begin(ShapeType.Line);

		for(int i = 0; i < level.entities.size; i++) {
			renderTriggerLines(level.entities.get(i));
		}

		lineRenderer.end();
	}

	public void renderTriggerLines(Entity e) {
		if(e instanceof Trigger) {
			Trigger t = (Trigger)e;
			Array<Entity> matches = level.getEntitiesById(t.triggersId);
			if(matches.size > 0) {
				if(e == pickedEntity)
					lineRenderer.setColor(Color.WHITE);
				else
					lineRenderer.setColor(Color.MAGENTA);

				for(Entity triggered : matches) {
					lineRenderer.line(e.x, e.z, e.y, triggered.x, triggered.z, triggered.y);
				}
			}
		}
		else if(e instanceof BasicTrigger) {
			BasicTrigger t = (BasicTrigger)e;
			Array<Entity> matches = level.getEntitiesById(t.triggersId);
			if(matches.size > 0) {
				if(e == pickedEntity)
					lineRenderer.setColor(Color.WHITE);
				else
					lineRenderer.setColor(Color.MAGENTA);

				for(Entity triggered : matches) {
					lineRenderer.line(e.x, e.z, e.y, triggered.x, triggered.z, triggered.y);
				}
			}
		}

		if(e instanceof ButtonModel) {
			ButtonModel t = (ButtonModel)e;
			Array<Entity> matches = level.getEntitiesById(t.triggersId);
			if(matches.size > 0) {
				if(e == pickedEntity)
					lineRenderer.setColor(Color.WHITE);
				else
					lineRenderer.setColor(Color.MAGENTA);

				for(Entity triggered : matches) {
					lineRenderer.line(e.x, e.z, e.y, triggered.x, triggered.z, triggered.y);
				}
			}
		}
	}

	private void drawSlopeLines(int num, boolean edge) {

		if(!selected) return;

		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null) {
					if(edge) {
						if(num == 0)
							drawLine(tempVec1.set(x, t.getNEFloorHeight(), y), tempVec2.set(x + 1, t.getNWFloorHeight(), y), 4f, Color.WHITE);
						else if(num == 1)
							drawLine(tempVec1.set(x, t.getNEFloorHeight(), y), tempVec2.set(x, t.getSEFloorHeight(), y + 1), 4f, Color.WHITE);
						else if(num == 2)
							drawLine(tempVec1.set(x, t.getSEFloorHeight(), y + 1), tempVec2.set(x + 1, t.getSWFloorHeight(), y + 1), 4f, Color.WHITE);
						else if(num == 3)
							drawLine(tempVec1.set(x + 1, t.getSWFloorHeight(), y + 1), tempVec2.set(x + 1, t.getNWFloorHeight(), y), 4f, Color.WHITE);
						else if(num == 4)
							drawLine(tempVec1.set(x, t.getNECeilHeight(), y), tempVec2.set(x + 1, t.getNWCeilHeight(), y), 4f, Color.WHITE);
						else if(num == 5)
							drawLine(tempVec1.set(x, t.getNECeilHeight(), y), tempVec2.set(x, t.getSECeilHeight(), y + 1), 4f, Color.WHITE);
						else if(num == 6)
							drawLine(tempVec1.set(x, t.getSECeilHeight(), y + 1), tempVec2.set(x + 1, t.getSWCeilHeight(), y + 1), 4f, Color.WHITE);
						else
							drawLine(tempVec1.set(x + 1, t.getSWCeilHeight(), y + 1), tempVec2.set(x + 1, t.getNWCeilHeight(), y), 4f, Color.WHITE);
					}
					else {
						if(num == 1)
							drawPoint(tempVec1.set(x, t.getNEFloorHeight(), y), 6f, Color.WHITE);
						else if(num == 0)
							drawPoint(tempVec1.set(x + 1, t.getNWFloorHeight(), y), 6f, Color.WHITE);
						else if(num == 2)
							drawPoint(tempVec1.set(x, t.getSEFloorHeight(), y + 1), 6f, Color.WHITE);
						else if(num == 3)
							drawPoint(tempVec1.set(x + 1, t.getSWFloorHeight(), y + 1), 6f, Color.WHITE);
						else if(num == 4)
							drawPoint(tempVec1.set(x + 1, t.getNECeilHeight(), y), 6f, Color.WHITE);
						else if(num == 5)
							drawPoint(tempVec1.set(x, t.getNWCeilHeight(), y), 6f, Color.WHITE);
						else if(num == 6)
							drawPoint(tempVec1.set(x, t.getSECeilHeight(), y + 1), 6f, Color.WHITE);
						else
							drawPoint(tempVec1.set(x + 1, t.getSWCeilHeight(), y + 1), 6f, Color.WHITE);
					}
				}
			}
		}
	}

	public FrameBuffer CreateFrameBuffer(FrameBuffer previousBuffer, int width, int height, boolean hasDepth, boolean hasStencil) {
		int newWidth = width;
		int newHeight = height;

		// Might not need to do anything here
		if(previousBuffer != null && previousBuffer.getWidth() == newWidth && previousBuffer.getHeight() == newHeight)
			return previousBuffer;

		// Resize the frame buffer, or just create it
		if(previousBuffer != null) {
			try {
				previousBuffer.dispose();
			}
			catch(Exception ex) {
				Gdx.app.log("FrameBufferResize", ex.getMessage());
			}
		}

		try {
			return new FrameBuffer(Format.RGBA8888, newWidth, newHeight, hasDepth, hasStencil);
		}
		catch (Exception ex) {
			Gdx.app.log("FrameBufferResize", ex.getMessage());
			return null;
		}
	}

	@Override
	public void resize(int width, int height) {

		if(gameApp != null) gameApp.resize(width, height);
        if(editorUi != null) editorUi.resize(width, height);

		Gdx.gl.glViewport(0, 0, width, height);

		Gdx.app.log("DelverGameScreen", "LibGdx Resize");
		if(width != curWidth || height != curHeight) {
			curWidth = width;
			curHeight = height;
		}

		float aspectRatio = (float) width / (float) height;
		camera = new PerspectiveCamera(60, 2f * aspectRatio, 2f);
        camera.near = 0.2f;
        camera.far = 80f;

        renderer.setSize(width, height);
        camera = renderer.camera;
		camera.near = 0.2f;
		camera.far = 80f;

        GameManager.renderer = renderer;

        if(spriteBatch != null) spriteBatch.dispose();
        spriteBatch = new DecalBatch(new SpriteGroupStrategy(camera, null, GlRenderer.worldShaderInfo, 1));

        if(pointBatch != null) pointBatch.dispose();
        pointBatch = new DecalBatch(new SpriteGroupStrategy(camera, null, GlRenderer.worldShaderInfo, 1));

		pickerFrameBuffer = CreateFrameBuffer(pickerFrameBuffer, width, height, true, true);

		if(pickerPixelBuffer != null)
			pickerPixelBuffer.dispose();

		pickerPixelBuffer = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA8888);
	}

	@Override
	public void resume() {
	}

	@Override
	public void create() {

		renderer = new GlRenderer();

        EditorArt.initAtlases();
		init();

		Gdx.input.setCursorCatched(false);

		initTextures();

        pickedWallTextureAtlas = pickedWallBottomTextureAtlas = pickedFloorTextureAtlas = pickedCeilingTextureAtlas =
                TextureAtlas.cachedRepeatingAtlases.firstKey();

		level = new Level(17,17);
		Tile t = new Tile();
		t.floorHeight = -0.5f;
		t.ceilHeight = 0.5f;
		level.setTile(7, 7, t);

		gridMesh = genGrid(level.width,level.height);
	}

	public void save(String filename) {

		level.preSaveCleanup();

		// cleanup some of the tiles
		for(int x = 0; x < level.width; x++) {
			for(int y = 0; y < level.height; y++) {
				Tile cTile = level.getTileOrNull(x, y);
				if(cTile == null) {

					// if any tiles around are not solid, make this a real tile
					boolean makeRealTile = false;
					for(int xx = x - 1; xx <= x + 1; xx += 2) {
						for(int yy = y - 1; yy <= y + 1; yy += 2) {
							Tile tile = level.getTile(xx, yy);
							if(!tile.renderSolid) makeRealTile = true;
						}
					}

					if(makeRealTile) {
						Tile t = new Tile();
						t.renderSolid = true;
						t.blockMotion = true;
						level.setTile(x, y, t);
					}
				}
				else {
					if(cTile.wallTex == 6 && !(cTile instanceof ExitTile) && cTile.IsSolid()) {
						ExitTile exitTile = new ExitTile();
						Tile.copy(cTile, exitTile);
						level.setTile(x,  y, exitTile);
					}
				}
			}
		}

		// write as json
		if(filename.endsWith(".dat")) {
			Game.toJson(level, Gdx.files.absolute(filename));
		}
		else {
			KryoSerializer.saveLevel(Gdx.files.absolute(filename), level);
		}
	}

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void tick() {
		if(messageTimer > 0) messageTimer--;

		if(editorInput.isButtonPressed(Input.Buttons.RIGHT)) {
			rightClickWasDown = true;
		}
		else {
			if(rightClickWasDown)
				rightClickWasDown = false;
		}

		if(editorUi.isShowingMenuOrModal())
			return;

		// get picked entity
		Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

		float worldHitDistance = 10000000f;

		// get plane intersection
		if(intpos != null) {
			if (!Gdx.input.isTouched()) {
				p.set(0, 1, 0, 0.5f);
			} else {
				p.set(0, 1, 0, -intpos.y);
			}
		}

		// try world intersection
		Intersector.intersectRayPlane(ray, p, intpos);
		if (Collidor.intersectRayForwardFacingTriangles(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), camera, triangleSpatialHash.getAllTriangles(), intpos, intersectNormal)) {
			intersectTemp.set(intpos).sub(camera.position).nor();
			intpos.add(intersectTemp.x * -0.05f, 0.0001f, intersectTemp.z * -0.05f);
			worldHitDistance = intersectTemp.set(intpos).sub(camera.position).len();
		}

		if(tileDragging) {
			tileDragging = Gdx.input.isButtonPressed(Buttons.LEFT);
		}

		if(Gdx.input.isKeyJustPressed(Keys.TAB)) {
			pickedEntity = null;
			hoveredEntity = null;
		}

		// Try to pick an entity
		if(pickedControlPoint == null && !tileDragging && !Gdx.input.isKeyPressed(Keys.TAB)) {
			if(Gdx.input.getX() != lastInputX || Gdx.input.getY() != lastInputY) {
				lastInputX = Gdx.input.getX();
				lastInputY = Gdx.input.getY();
				RenderEntitiesForPicking();
				GlPickEntity();
			}
		}

		if(editorInput.isButtonPressed(Input.Buttons.LEFT)) {
			if(movingControlPoint || pickedControlPoint != null) {
				// don't select entities
			}
			else if(hoveredEntity == null && pickedEntity == null) {
				selected = true;
			}
			else {
				if(!readLeftClick) {
					if(pickedEntity != null && hoveredEntity != null && hoveredEntity != pickedEntity && !additionalSelected.contains(hoveredEntity, true) && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                        pickAdditionalEntity(hoveredEntity);
					}
					else if(pickedEntity != null && pickedEntity == hoveredEntity || additionalSelected.contains(hoveredEntity, true)) {
						movingEntity = true;
					}
					else {
						clearEntitySelection();
                        pickEntity(hoveredEntity);
					}
				}
			}
		}
		else {
            if(movingControlPoint || movingEntity) {
                history.saveState(level);
            }

			movingEntity = false;
			movingControlPoint = false;
		}
		if(pickedEntity == null) movingEntity = false;

		boolean turnLeft = editorInput.isKeyPressed(Keys.LEFT) || (Gdx.input.getDeltaX() < 0 && Gdx.input.isButtonPressed(Buttons.MIDDLE));
		boolean turnRight = editorInput.isKeyPressed(Keys.RIGHT) || (Gdx.input.getDeltaX() > 0 && Gdx.input.isButtonPressed(Buttons.MIDDLE));
		boolean turnUp = editorInput.isKeyPressed(Keys.UP) || (Gdx.input.getDeltaY() > 0 && Gdx.input.isButtonPressed(Buttons.MIDDLE));
		boolean turnDown = editorInput.isKeyPressed(Keys.DOWN) || (Gdx.input.getDeltaY() < 0 && Gdx.input.isButtonPressed(Buttons.MIDDLE));

		if(turnLeft) {
			rota += rotSpeed;
			if(rota > maxRot) rota = maxRot;
		}
		else if(turnRight) {
			rota -= rotSpeed;
			if(rota < -maxRot) rota = -maxRot;
		}

		rotX += rota;
		rota *= 0.8;

		if(turnUp) {
			rotya += rotSpeed * 0.6f;
			if(rotya > maxRot) rotya = maxRot;
		}
		else if(turnDown) {
			rotya -= rotSpeed * 0.6f;
			if(rotya < -maxRot) rotya = -maxRot;
		}

		rotY += rotya;
		
		if (rotY < -rotYClamp) rotY = -rotYClamp;
		if (rotY > rotYClamp) rotY = rotYClamp;

		rotya *= 0.8;

		float xm = 0f;
		float zm = 0f;

		if(editorInput.isKeyPressed(Keys.A)) {
			xm = -1f;
		}
		if(editorInput.isKeyPressed(Keys.D)) {
			xm = 1f;
		}

		if(editorInput.isKeyPressed(Keys.W)) {
			zm = -1f;
		}
		if(editorInput.isKeyPressed(Keys.S)) {
			zm = 1f;
		}

		if (editorInput.isKeyPressed(Keys.SHIFT_LEFT)) {
			xm *= 2.0f;
			zm *= 2.0f;
		}

		camZ += (zm * Math.sin(rotY)) * walkSpeed;
		zm *= Math.cos(rotY);
		camX += (xm * Math.cos(rotX) + zm * Math.sin(rotX)) * walkSpeed;
		camY += (zm * Math.cos(rotX) - xm * Math.sin(rotX)) * walkSpeed;

		if(player != null) {
			player.rot = rotX;
			player.yrot = rotY;

			player.xa += (xm * Math.cos(rotX) + zm * Math.sin(rotX)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
			player.ya += (zm * Math.cos(rotX) - xm * Math.sin(rotX)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
		}

		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		// Tile editing mode?
		if(pickedEntity == null) {
			if(Gdx.input.isKeyPressed(Keys.NUM_1)) {
				paintSurfaceAtCursor();
			}
			else if(Gdx.input.isKeyPressed(Keys.NUM_2)) {
				pickTextureAtSurface();
			}
		}
		else {
			// rotate entity by 90 degrees
			if(Gdx.input.isKeyPressed(Keys.T)) {
				if(!readRotate) {
					readRotate = true;
					pickedEntity.rotate90();
					for(Entity e : additionalSelected) { e.rotate90(); }
					refreshEntity(pickedEntity);
				}
			}
			else readRotate = false;
		}

		if(player != null) {
			player.inEditor = true;
			level.editorTick(player, Gdx.graphics.getDeltaTime() * 60f);
		}
		else {
			level.editorTick(player, 0);
		}

		camera.position.x = camX;
		camera.position.y = camZ;
		camera.position.z = camY;

		if(editorInput.isButtonPressed(Input.Buttons.LEFT)) {
			readLeftClick = true;
		}
		else {
			readLeftClick = false;
		}

		if(editorInput.isButtonPressed(Input.Buttons.RIGHT)) {
			readRightClick = true;
		}
		else {
			readRightClick = false;
		}

		input.tick();
        editorInput.tick();

		CachePools.clearOnTick();
	}

    public void undo() {
		try {
			Level undid = history.undo();
			if (undid != null) {
				level = undid;
				level.init(Source.EDITOR);
				controlPoints.clear();

				selected = false;
				pickedEntity = null;
				additionalSelected.clear();

				refresh();
			}
		}
		catch(Exception ex) {
			Gdx.app.error("Editor", ex.getMessage(), ex);
		}
    }

    public void redo() {
		try {
			level = history.redo();
			level.init(Source.EDITOR);
			controlPoints.clear();

			selected = false;
			pickedEntity = null;
			additionalSelected.clear();

			refresh();
		}
		catch(Exception ex) {
			Gdx.app.error("Editor", ex.getMessage(), ex);
		}
    }

    public void toggleCollisionBoxes() {
        showCollisionBoxes = !showCollisionBoxes;
    }

    public void clearSelection() {
        if(slopePointMode || slopeEdgeMode) {
            slopePointMode = false;
            slopeEdgeMode = false;
        }
        else {
            clearEntitySelection();
		}
		
		if (editorUi.isShowingContextMenu()) {
			editorUi.hideContextMenu();
		}

        history.saveState(level);
    }

    public void toggleLights() {
        showLights = !showLights;
        refreshLights();
    }

    public void setPlaneHeightMode() {
        vertexSelectionMode = false;
        controlPoints.clear();
    }

    public void setVertexHeightMode() {
        vertexSelectionMode = true;
        controlPoints.clear();
    }

    public void toggleVertexHeightMode() {
        vertexSelectionMode = !vertexSelectionMode;
        controlPoints.clear();
    }

	public void testLevel() {
		gameApp = new GameApplication();
		GameApplication.editorRunning = true;

		Level previewLevel = (Level) KryoSerializer.copyObject(level);
		if(previewLevel.theme == null) previewLevel.theme = "TEST";

        previewLevel.levelName = "EDITOR LEVEL";
		previewLevel.genTheme = DungeonGenerator.GetGenData(previewLevel.theme);

		gameApp.createFromEditor(previewLevel);

		Game.instance.player.x = camera.position.x;
		Game.instance.player.y = camera.position.z;
		Game.instance.player.z = camera.position.y - 0.4f;
		Game.instance.player.rot = rotX - 3.14159265f;
		Game.instance.player.yrot = -rotY + 18.9f;
		Game.isDebugMode = true;
	}

	public void initTextures() {
		Pixmap wallorig = Art.loadPixmap("textures.png");
		wallPixmap = wallorig;

		final int wallSize = wallorig.getWidth() / 4;
		final int wallHeight = wallorig.getHeight() / wallSize;
		int next_pow = GetNextPowerOf2(4 * wallHeight);
		Pixmap remappedWall = new Pixmap(wallSize * next_pow, wallSize, Format.RGBA8888);

		// wall texture atlas
		int pos = 0;
		for(int y = 0; y < wallHeight; y++) {
			for(int x = 0; x < 4; x++) {
				remappedWall.drawPixmap(wallorig, pos * wallSize, 0, x * wallSize, y * wallSize, wallSize, wallSize);
				pos++;
			}
		}
		walltex = new Texture(remappedWall);
		walltex.setWrap(TextureWrap.ClampToEdge, TextureWrap.Repeat);

		// wall texture atlases
		wallTextures = new TextureRegion[4 * wallHeight];

		for(int i = 0; i < wallTextures.length; i++)
		{
			wallTextures[i] = new TextureRegion(walltex, i * wallSize, 0, wallSize, wallSize);
		}

		Pixmap selPixmap = new Pixmap(2,2,Format.RGBA8888);
		selPixmap.setColor(new Color(0.9f,0.9f,0.9f,0.5f));
		selPixmap.fillRectangle(0, 0, 2, 2);

		selectionTex = new Texture(selPixmap);

		meshtex = Art.loadTexture("meshes.png");

		editorSprites = loadAtlas("editor.png", 4, true);

		spriteAtlases.put(Entity.ArtType.entity, loadAtlas("entities.png", 16, false));
		spriteAtlases.put(Entity.ArtType.sprite, loadAtlas("sprites.png", 4, false));
		spriteAtlases.put(Entity.ArtType.item, loadAtlas("items.png", 8, false));
		spriteAtlases.put(Entity.ArtType.weapon, loadAtlas("items.png", 8, false));
		spriteAtlases.put(Entity.ArtType.door, loadAtlas("textures.png", 4, false));
		spriteAtlases.put(Entity.ArtType.texture, loadAtlas("textures.png", 4, false));
		spriteAtlases.put(Entity.ArtType.particle, loadAtlas("particles.png", 8, false));

        setupHud(wallTextures);
	}

    private void setupHud(TextureRegion[] wallTextures) {

        editorUi = new EditorUi(editor, this);

        wallPickerButton = new Image(new TextureRegionDrawable(wallTextures[0]));
        wallPickerButton.setScaling(Scaling.stretch);

        bottomWallPickerButton = new Image(new TextureRegionDrawable(wallTextures[0]));
        bottomWallPickerButton.setScaling(Scaling.stretch);

        ceilPickerButton = new Image(new TextureRegionDrawable(wallTextures[1]));
        ceilPickerButton.setScaling(Scaling.stretch);

        floorPickerButton = new Image(new TextureRegionDrawable(wallTextures[2]));
        floorPickerButton.setScaling(Scaling.stretch);

        Stage stage = editorUi.getStage();
        Table wallPickerLayoutTable = new Table();
        wallPickerLayoutTable.setFillParent(true);
        wallPickerLayoutTable.align(Align.left | Align.top).pad(6f).padTop(150f);

        Label wallLabel = new Label("Upper Wall", editorUi.getSmallSkin());
        Label wallBottomLabel = new Label("Lower Wall", editorUi.getSmallSkin());
        Label ceilingLabel = new Label("Ceiling", editorUi.getSmallSkin());
        Label floorLabel = new Label("Floor", editorUi.getSmallSkin());

        wallPickerLayoutTable.add(wallPickerButton).width(50f).height(50f).align(Align.left).padBottom(6f);
        wallPickerLayoutTable.add(wallLabel).align(Align.left);
        wallPickerLayoutTable.row();
        wallPickerLayoutTable.add(bottomWallPickerButton).width(50f).height(50f).align(Align.left).padBottom(6f);
        wallPickerLayoutTable.add(wallBottomLabel).align(Align.left);
        wallPickerLayoutTable.row();
        wallPickerLayoutTable.add(ceilPickerButton).width(50f).height(50f).align(Align.left).padBottom(6f);
        wallPickerLayoutTable.add(ceilingLabel).align(Align.left);
        wallPickerLayoutTable.row();
        wallPickerLayoutTable.add(floorPickerButton).width(50f).height(50f).align(Align.left);
        wallPickerLayoutTable.add(floorLabel).align(Align.left);
        wallPickerLayoutTable.row();

        paintAdjacent = new CheckBox("Paint adjacent", editorUi.getSmallSkin());
        paintAdjacent.setChecked(true);
        wallPickerLayoutTable.add(paintAdjacent).colspan(2).padLeft(-10f);

        wallPickerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextureRegionPicker picker = new TextureRegionPicker("Pick Upper Wall Texture", EditorUi.smallSkin, lastTextureRegionPickerSelection, TextureAtlas.getAllRepeatingAtlases()) {
                    @Override
                    public void result(Integer value, String atlas) {
                        setPickedWallTexture(value, atlas);
						lastTextureRegionPickerSelection = atlas;

						if(selected)
                        	doPaint();
                    }
                };
				editorUi.showModal(picker);
                event.handle();
            }
        });

        bottomWallPickerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextureRegionPicker picker = new TextureRegionPicker("Pick Lower Wall Texture", EditorUi.smallSkin, lastTextureRegionPickerSelection, TextureAtlas.getAllRepeatingAtlases()) {
                    @Override
                    public void result(Integer value, String atlas) {
                        setPickedWallBottomTexture(value, atlas);
						lastTextureRegionPickerSelection = atlas;

						if(selected)
                        	doPaint();
                    }
                };
				editorUi.showModal(picker);
                event.handle();
            }
        });

        floorPickerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextureRegionPicker picker = new TextureRegionPicker("Pick Floor Texture", EditorUi.smallSkin, lastTextureRegionPickerSelection, TextureAtlas.getAllRepeatingAtlases()) {
                    @Override
                    public void result(Integer value, String atlas) {
                        setPickedFloorTexture(value, atlas);
						lastTextureRegionPickerSelection = atlas;

						if(selected)
                        	doPaint();
                    }
                };
                editorUi.showModal(picker);
                event.handle();
            }
        });

        ceilPickerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextureRegionPicker picker = new TextureRegionPicker("Pick Ceiling Texture", EditorUi.smallSkin, lastTextureRegionPickerSelection, TextureAtlas.getAllRepeatingAtlases()) {
                    @Override
                    public void result(Integer value, String atlas) {
                        setPickedCeilingTexture(value, atlas);
						lastTextureRegionPickerSelection = atlas;

						if(selected)
                        	doPaint();
                    }
                };
				editorUi.showModal(picker);
				event.handle();
            }
        });

        stage.addActor(wallPickerLayoutTable);
        editorUi.initUi();

        editorInput = new EditorInput(this);
        inputMultiplexer = new InputMultiplexer();

        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(editorInput);
        inputMultiplexer.addProcessor(input);

        setInputProcessor();
    }

    public void setInputProcessor() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public Tesselator tesselator = new Tesselator();
    public void Tesselate()
	{
        if(!tesselators.world.needsTesselation()) return;

        if(renderer.chunks != null)
			renderer.chunks.clear();

		int width = level.width;
		int height = level.height;

		int xOffset = 0;
		int yOffset = 0;

		boolean makeFloors = true;
		boolean makeCeilings = true;
		boolean makeWalls = true;

		tesselator.Tesselate(level, renderer, null, xOffset, yOffset, width, height, tesselators, makeFloors, makeCeilings, makeWalls, showLights);

        refreshTriangleSpatialHash();
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

	public Mesh genCube () {
        Mesh mesh = new Mesh(true, 24, 36, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.Normal,
                3, "a_normal"), new VertexAttribute(Usage.TextureCoordinates, 2, "a_texcoords"));

        float[] cubeVerts = {-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f,
                -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
                0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f,
                -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f, 0.5f, -0.5f,};

        float[] cubeNormals = {0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,};

        float[] cubeTex = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f,};

        float[] vertices = new float[24 * 8];
        int pIdx = 0;
        int nIdx = 0;
        int tIdx = 0;
        for (int i = 0; i < vertices.length;) {
                vertices[i++] = cubeVerts[pIdx++];
                vertices[i++] = cubeVerts[pIdx++];
                vertices[i++] = cubeVerts[pIdx++];
                vertices[i++] = cubeNormals[nIdx++];
                vertices[i++] = cubeNormals[nIdx++];
                vertices[i++] = cubeNormals[nIdx++];
                vertices[i++] = cubeTex[tIdx++];
                vertices[i++] = cubeTex[tIdx++];
        }

        short[] indices = {0, 2, 1, 0, 3, 2, 4, 5, 6, 4, 6, 7, 8, 9, 10, 8, 10, 11, 12, 15, 14, 12, 14, 13, 16, 17, 18, 16, 18, 19,
                20, 23, 22, 20, 22, 21};

        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        return mesh;
	}

	public Mesh genGrid (int height, int width) {

        Array<Float> cubeVertArray = new Array<Float>();
        for(int x = 0; x <= width; x++) {
        	cubeVertArray.addAll( new Float[] {0f, -0.495f, (float) x} );
        	cubeVertArray.addAll( new Float[] {height * 1f, -0.495f, (float) x} );
        }

        for(int y = 0; y <= height; y++) {
        	cubeVertArray.addAll( new Float[] {(float) y, -0.495f, 0f} );
        	cubeVertArray.addAll( new Float[] {(float) y, -0.495f, width * 1f} );
        }

        float[] cubeVerts = new float[cubeVertArray.size];

		for(int i = 0; i < cubeVertArray.size; i++) {
			cubeVerts[i] = cubeVertArray.get(i);
		}

		cubeVertArray.shrink();

        float[] vertices = new float[cubeVertArray.size + (cubeVertArray.size / 3) * 4];
        int i = 0;
        for (int pIdx = 0; pIdx < cubeVertArray.size;) {
        		// vertex position
                vertices[i++] = cubeVerts[pIdx++];
                vertices[i++] = cubeVerts[pIdx++];
                vertices[i++] = cubeVerts[pIdx++];

				// vertex color
				vertices[i++] = 0.3f;
				vertices[i++] = 0.3f;
				vertices[i++] = 0.3f;
				vertices[i++] = 1f;
        }

        short[] indices = new short[cubeVertArray.size];
        for(i = 0; i < indices.length;) {
        	indices[i++] = (short)(i - 1);
			indices[i++] = (short)(i - 1);
			indices[i++] = (short)(i - 1);
        }

		Mesh mesh = new Mesh(true, vertices.length, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        xGridStart = new Vector3(0, -0.495f, level.height / 2f);
		xGridEnd = new Vector3(level.width, -0.495f, level.height / 2f);
		yGridStart = new Vector3(level.width / 2f, -0.495f, 0);
		yGridEnd = new Vector3(level.width / 2f, -0.495f, level.height);

        return mesh;
	}

	public void drawLine(Vector3 start, Vector3 end, float width, Color color) {
		lineRenderer.setColor(color);
		lineRenderer.line(start.x, start.y, start.z, end.x, end.y, end.z);
	}

	public void drawZCircle(float startX, float startY, float startZ, float radius, Color color) {
		lineRenderer.setColor(color);

		int segments = 30;
		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin((float)i / (float)segments * 7f);
			float cos = (float)Math.cos((float)i / (float)segments * 7f);

			float nextsin = (float)Math.sin((float)(i + 1) / (float)segments * 7f);
			float nextcos = (float)Math.cos((float)(i + 1) / (float)segments * 7f);

			lineRenderer.line(startX + sin * radius, startY, startZ + cos * radius, startX + nextsin * radius, startY, startZ + nextcos * radius);
		}
	}

	public void drawXCircle(float startX, float startY, float startZ, float radius, Color color) {
		lineRenderer.setColor(color);

		int segments = 30;
		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin((float)i / (float)segments * 7f);
			float cos = (float)Math.cos((float)i / (float)segments * 7f);

			float nextsin = (float)Math.sin((float)(i + 1) / (float)segments * 7f);
			float nextcos = (float)Math.cos((float)(i + 1) / (float)segments * 7f);

			lineRenderer.line(startX, startY + cos * radius, startZ + sin * radius, startX, startY + nextcos * radius, startZ + nextsin * radius);
		}
	}

	public void drawYCircle(float startX, float startY, float startZ, float radius, Color color) {
		lineRenderer.setColor(color);

		int segments = 30;
		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin((float)i / (float)segments * 7f);
			float cos = (float)Math.cos((float)i / (float)segments * 7f);

			float nextsin = (float)Math.sin((float)(i + 1) / (float)segments * 7f);
			float nextcos = (float)Math.cos((float)(i + 1) / (float)segments * 7f);

			lineRenderer.line(startX + sin * radius, startY + cos * radius, startZ, startX + nextsin * radius, startY + nextcos * radius, startZ);
		}
	}

	private void drawPoint(Vector3 start, float width, Color color) {
		Vector3 pos1 = tempVec5.set(start);
		Vector3 pos2 = tempVec4.set(camera.position);

		pos2.sub(pos1);

        Decal sd = getDecal();
		sd.setRotation(tempVector1.set(camera.direction.x, camera.direction.y, camera.direction.z).nor().scl(-1f), Vector3.Y);
		sd.setScale((pos2.len() / camera.far) * (pos2.len() * 0.5f) + 1);
		sd.setTextureRegion(editorSprites[17]);
		sd.setPosition(start.x, start.y, start.z);
		sd.setColor(color.r, color.g, color.b, color.a);

		pointBatch.add(sd);
	}

	private void renderMarker(EditorMarker marker)
	{
		if(marker == null) return;
		Tile t = level.getTile(marker.x, marker.y);

        Decal sd = getDecal();
		sd.setTextureRegion(editorSprites[0]);
		sd.setPosition(marker.x + 0.5f, camera.position.y, marker.y + 0.5f);

		if(marker.type == Markers.loot) sd.setTextureRegion(editorSprites[5]);
		else if(marker.type == Markers.monster) sd.setTextureRegion(editorSprites[4]);
		else if(marker.type == Markers.torch) sd.setTextureRegion(editorSprites[0]);
		else if(marker.type == Markers.key) sd.setTextureRegion(editorSprites[10]);
		else if(marker.type == Markers.boss) sd.setTextureRegion(editorSprites[9]);
		else if(marker.type == Markers.stairDown) sd.setTextureRegion(editorSprites[11]);
		else if(marker.type == Markers.stairUp) sd.setTextureRegion(editorSprites[7]);
		else if(marker.type == Markers.door) sd.setTextureRegion(editorSprites[1]);
		else if(marker.type == Markers.decor) sd.setTextureRegion(editorSprites[8]);
		else if(marker.type == Markers.decorPile) sd.setTextureRegion(editorSprites[18]);
		else if(marker.type == Markers.playerStart) sd.setTextureRegion(editorSprites[3]);
		else if(marker.type == Markers.exitLocation) sd.setTextureRegion(editorSprites[6]);
		else if(marker.type == Markers.secret) sd.setTextureRegion(editorSprites[2]);

		sd.setRotation(selectionTempVector1.set(camera.direction.x, camera.direction.y * 0.1f, camera.direction.z).nor().scl(-1f), Vector3.Y);
		sd.setScale(1);

		sd.setPosition(marker.x + 0.5f, t != null ? t.floorHeight + 0.5f : 0, marker.y + 0.5f);

		sd.setColor(1f, 1f, 1f, 1f);

		sd.setWidth(1);
		sd.setHeight(1);

		spriteBatch.add(sd);
	}

	// Expects a hex value as integer and returns the appropriate Color object.
    // @param hex
    //            Must be of the form 0xAARRGGBB
    // @return the generated Color object
   private Color colorFromHex(long hex)
   {
           float a = (hex & 0xFF000000L) >> 24;
           float r = (hex & 0xFF0000L) >> 16;
           float g = (hex & 0xFF00L) >> 8;
           float b = (hex & 0xFFL);

           return new Color(r/255f, g/255f, b/255f, a/255f);
   }


    // Expects a hex value as String and returns the appropriate Color object
    // @param s The hex string to create the Color object from
    // @return

   private Color colorFromHexString(String s)
   {
           if(s.startsWith("0x"))
                   s = s.substring(2);

           if(s.length() != 8) // AARRGGBB
                   throw new IllegalArgumentException("String must have the form AARRGGBB");

           return colorFromHex(Long.parseLong(s, 16));
   }

   private Color getColor(int r, int g, int b, int a) {
	   String rr = Integer.toHexString(r);
	   String gg = Integer.toHexString(g);
	   String bb = Integer.toHexString(b);
	   String aa = Integer.toHexString(a);

	   if(rr.length() == 1) rr = "0" + rr;
	   if(gg.length() == 1) gg = "0" + gg;
	   if(bb.length() == 1) bb = "0" + bb;
	   if(aa.length() == 1) aa = "0" + aa;

	   return colorFromHex(Long.parseLong(aa + rr + gg + bb, 16));
   }

   public void addEntityMarker(Markers selectedItem) {
	   	int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

	   clearSelectedMarkers();

	   if(selectedItem != Markers.none) {
		   for(int x = selX; x < selX + selWidth; x++) {
				for(int y = selY; y < selY + selHeight; y++) {
				   EditorMarker eM = new EditorMarker(selectedItem, x, y);
				   level.editorMarkers.add(eM);
				}
		   }
	   }

	   refreshLights();

       history.saveState(level);
   }

   public boolean selectionHasEntityMarker() {
	   	int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

	   for(EditorMarker marker : level.editorMarkers) {
		   if(marker.x >= selX && marker.x < selX + selWidth && marker.y >= selY && marker.y < selY + selHeight) return true;
	   }

	   return false;
   }

   public void addEntity(Entity e) {
	   if(selected) {
		   int x = selectionX;
		   int y = selectionY;

		   String objCopy = Game.toJson(e);

		   Entity copy = Game.fromJson(e.getClass(), objCopy);
		   copy.x = x + 0.5f;
		   copy.y = y + 0.5f;

		   Tile at = level.getTileOrNull(x, y);
		   if(at != null) copy.z = at.floorHeight + 0.5f;

		   level.entities.add(copy);
	   }

       history.saveState(level);

	   refreshLights();
   }

   public void clearSelectedMarkers() {
	   	int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

	   for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				if(level.editorMarkers != null && level.editorMarkers.size > 0) {
					Array<EditorMarker> toDelete = new Array<EditorMarker>();

					for(int i = 0; i < level.editorMarkers.size; i++) {
						EditorMarker m = level.editorMarkers.get(i);
						if(m.x == x && m.y == y) toDelete.add(m);
					}

					for(EditorMarker m : toDelete) {
						level.editorMarkers.removeValue(m, true);
					}
				}
			}
		}

	   refreshLights();
   }

	public void refresh() {
		gridMesh.dispose();
		gridMesh = null;

		gridMesh = genGrid(level.width,level.height);

		refreshLights();
	}

	public void refreshLights() {
        tesselators.refresh();

		if(showLights) {
			level.cleanupLightCache();
			level.updateLights(Source.EDITOR);
		}

		for(Entity entity : level.entities) {
		   if(entity instanceof Sprite && !entity.isDynamic) {
			   if(!showLights && !entity.fullbrite) entity.color = Color.WHITE;
			   else entity.init(level, Source.EDITOR);
		   }
           else if(entity instanceof ProjectedDecal) {
               ProjectedDecal decal = (ProjectedDecal)entity;
               decal.drawable = null;
           }
		}

		staticMeshBatch = null;
		refreshTriangleSpatialHash();
	}

	public void setTile(Tile tocopy) {
		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		Tile selected = level.getTile(selectionX, selectionY);

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x, y);

				if(t == null) {
					t = new Tile();
				}

                Tile.copy(tocopy, t);
				level.setTile(x, y, t);

                t.eastTex = t.westTex = t.northTex = t.southTex = null;
                t.bottomEastTex = t.bottomWestTex = t.bottomNorthTex = t.bottomSouthTex = null;
			}
		}

        // Paint directional tiles
        if(paintAdjacent.isChecked()) {
            if (selY - 1 >= 0) {
                for (int x = selX; x < selX + selWidth; x++) {
                    int y = selY - 1;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.southTex = tocopy.wallTex;
                    t.southTexAtlas = tocopy.wallTexAtlas;
                    t.bottomSouthTex = tocopy.wallBottomTex;
                    t.bottomSouthTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selY + selHeight < level.height) {
                for (int x = selX; x < selX + selWidth; x++) {
                    int y = selY + selHeight;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.northTex = tocopy.wallTex;
                    t.northTexAtlas = tocopy.wallTexAtlas;
                    t.bottomNorthTex = tocopy.wallBottomTex;
                    t.bottomNorthTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selX - 1 >= 0) {
                for (int y = selY; y < selY + selHeight; y++) {
                    int x = selX - 1;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.westTex = tocopy.wallTex;
                    t.westTexAtlas = tocopy.wallTexAtlas;
                    t.bottomWestTex = tocopy.wallBottomTex;
                    t.bottomWestTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selX + selWidth < level.width) {
                for (int y = selY; y < selY + selHeight; y++) {
                    int x = selX + selWidth;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.eastTex = tocopy.wallTex;
                    t.eastTexAtlas = tocopy.wallTexAtlas;
                    t.bottomEastTex = tocopy.wallBottomTex;
                    t.bottomEastTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }
        }

		refreshLights();
	}

	public void paintTile(Tile tocopy) {
		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x, y);

				if(t != null) {
					t.wallTex = tocopy.wallTex;
					t.floorTex = tocopy.floorTex;
					t.ceilTex = tocopy.ceilTex;
                    t.wallBottomTex = tocopy.wallBottomTex;
                    t.wallTexAtlas = tocopy.wallTexAtlas;
                    t.wallBottomTexAtlas = tocopy.wallBottomTexAtlas;
                    t.floorTexAtlas = tocopy.floorTexAtlas;
                    t.ceilTexAtlas = tocopy.ceilTexAtlas;
				}
				else {
					t = new Tile();
					t.wallTex = tocopy.wallTex;
                    t.wallBottomTex = tocopy.wallBottomTex;
					t.floorTex = tocopy.floorTex;
					t.ceilTex = tocopy.ceilTex;
                    t.wallTexAtlas = tocopy.wallTexAtlas;
                    t.wallBottomTexAtlas = tocopy.wallBottomTexAtlas;
                    t.floorTexAtlas = tocopy.floorTexAtlas;
                    t.ceilTexAtlas = tocopy.ceilTexAtlas;
					t.blockMotion = true;
					t.renderSolid = true;
					level.setTile(x, y, t);
				}

                t.eastTex = t.westTex = t.northTex = t.southTex = null;
                t.eastTexAtlas = t.westTexAtlas = t.northTexAtlas = t.southTexAtlas = null;

                t.bottomEastTex = t.bottomWestTex = t.bottomNorthTex = t.bottomSouthTex = null;
                t.bottomEastTexAtlas = t.bottomWestTexAtlas = t.bottomNorthTexAtlas = t.bottomSouthTexAtlas = null;

                t.init(Source.EDITOR);
			}
		}

        if(paintAdjacent.isChecked()) {
            if (selY - 1 >= 0) {
                for (int x = selX; x < selX + selWidth; x++) {
                    int y = selY - 1;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.southTex = tocopy.wallTex;
                    t.southTexAtlas = tocopy.wallTexAtlas;
                    t.bottomSouthTex = tocopy.wallBottomTex;
                    t.bottomSouthTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selY + selHeight < level.height) {
                for (int x = selX; x < selX + selWidth; x++) {
                    int y = selY + selHeight;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.northTex = tocopy.wallTex;
                    t.northTexAtlas = tocopy.wallTexAtlas;
                    t.bottomNorthTex = tocopy.wallBottomTex;
                    t.bottomNorthTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selX - 1 >= 0) {
                for (int y = selY; y < selY + selHeight; y++) {
                    int x = selX - 1;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.westTex = tocopy.wallTex;
                    t.westTexAtlas = tocopy.wallTexAtlas;
                    t.bottomWestTex = tocopy.wallBottomTex;
                    t.bottomWestTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }

            if (selX + selWidth < level.width) {
                for (int y = selY; y < selY + selHeight; y++) {
                    int x = selX + selWidth;
                    Tile t = level.getTileOrNull(x, y);

                    if (t == null) {
                        t = new Tile();
                        t.blockMotion = true;
                        t.renderSolid = true;
                        level.setTile(x, y, t);
                    }
                    t.eastTex = tocopy.wallTex;
                    t.eastTexAtlas = tocopy.wallTexAtlas;
                    t.bottomEastTex = tocopy.wallBottomTex;
                    t.bottomEastTexAtlas = tocopy.wallBottomTexAtlas;
                }
            }
        }

		refreshLights();
	}

	public void clearTiles() {
		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile n = level.getTile(x, y - 1);
				Tile s = level.getTile(x, y + 1);
				Tile e = level.getTile(x - 1, y);
				Tile w = level.getTile(x + 1, y);

				if(n.blockMotion && s.blockMotion && e.blockMotion && w.blockMotion) {
					level.setTile(x, y, null);
				}
				else {
					Tile t = Tile.NewSolidTile();
					t.wallTex = (byte)pickedWallTexture;
					t.wallTexAtlas = pickedWallTextureAtlas;
					level.setTile(x, y, t);
				}
			}
		}

		clearSelectedMarkers();

		refreshLights();
	}

	public void rotateFloorTex(int value) {
    	if(pickedEntity != null) return;

		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null) {
					t.floorTexRot += value;
					t.floorTexRot %= 4;
				}
			}
		}

		refreshLights();
	}

    public Entity copyEntity(Entity entity) {
        return Game.fromJson(entity.getClass(), Game.toJson(entity));
    }

    public void copy() {
        clipboard = new EditorClipboard();

        if(pickedEntity != null) {
            Entity copy = copyEntity(pickedEntity);
            clipboard.offset = new Vector3(pickedEntity.x, pickedEntity.y, 0);
            copy.x -= (int)pickedEntity.x + 1;
            copy.y -= (int)pickedEntity.y + 1;
            float heightOffset = copy.z - level.getTile((int)pickedEntity.x, (int)pickedEntity.y).getFloorHeight(0.5f, 0.5f);
            copy.z = heightOffset;

            clipboard.entities.add(copy);

            if(additionalSelected != null) {
                for(Entity e : additionalSelected) {
                    Entity aCopy = copyEntity(e);
                    aCopy.x -= (int)pickedEntity.x + 1;
                    aCopy.y -= (int)pickedEntity.y + 1;
                    aCopy.z = heightOffset + (aCopy.z - pickedEntity.z);
                    clipboard.entities.add(aCopy);
                }
            }
        }

        int selX = selectionX;
        int selY = selectionY;
        int selWidth = selectionWidth;
        int selHeight = selectionHeight;

        if(selWidth < 0) {
            selX = selX + selWidth;
            selWidth *= -1;
            selX += 1;
        }
        if(selHeight < 0) {
            selY = selY + selHeight;
            selHeight *= -1;
            selY += 1;
        }

        if(pickedEntity == null) {
            clipboard.tiles = new Tile[selWidth][selHeight];
            clipboard.selWidth = selWidth;
            clipboard.selHeight = selHeight;
            clipboard.offset = new Vector3(selX, selY, 0);

            for (int x = selX; x < selX + selWidth; x++) {
                for (int y = selY; y < selY + selHeight; y++) {
                    Tile t = level.getTileOrNull(x, y);

                    if(t != null) t = (Tile) KryoSerializer.copyObject(t);
                    clipboard.tiles[x - selX][y - selY] = t;
                }
            }
        }
        else {
            clipboard.selWidth = 0;
            clipboard.selHeight = 0;
        }
    }

    public void paste() {
        if(clipboard != null) {
            int selX = selectionX;
            int selY = selectionY;
            int selWidth = selectionWidth;
            int selHeight = selectionHeight;

            if(selWidth < 0) {
                selX = selX + selWidth;
                selX += 1;
            }
            if(selHeight < 0) {
                selY = selY + selHeight;
                selY += 1;
            }

            for(int x = 0; x < clipboard.selWidth; x++) {
                for(int y = 0; y < clipboard.selHeight; y++) {
                    if(clipboard.tiles[x][y] == null)
                        level.setTile(x + selX, y + selY, null);
                    else {
                        Tile t = new Tile();
                        Tile.copy(clipboard.tiles[x][y],t);
                        level.setTile(x + selX, y + selY, t);
                    }
                }
            }

            for(Entity e : clipboard.entities) {
                Entity copy = copyEntity(e);
                copy.x += selX + 1;
                copy.y += selY + 1;

                Tile copyAt = level.getTileOrNull(selX, selY);
                if(copyAt != null) {
                	copy.z += copyAt.getFloorHeight(0.5f, 0.5f);
				}

                level.entities.add(copy);
            }

            // save undo history
            history.saveState(level);
        }

        refreshLights();
    }

	public void rotateAngle() {
        if(pickedEntity != null) return;

		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null) {
					t.blockMotion = false;
					t.renderSolid = false;
					t.tileSpaceType = TileSpaceType.values()[(t.tileSpaceType.ordinal() + 1) % TileSpaceType.values().length];
				}
			}
		}

		refreshLights();
	}

	public void rotateCeilTex(int value) {
		if(pickedEntity != null) return;

		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null) {
					t.ceilTexRot += value;
					t.ceilTexRot %= 4;
				}
			}
		}

		refreshLights();
	}

	public void moveFloor(int value) {
		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null)
					t.floorHeight += (value * 0.0625f);
			}
		}

		refreshLights();
	}

	public void moveCeiling(int value) {
		int selX = selectionX;
		int selY = selectionY;
		int selWidth = selectionWidth;
		int selHeight = selectionHeight;

		if(selWidth < 0) {
			selX = selX + selWidth;
			selWidth *= -1;
			selX += 1;
		}
		if(selHeight < 0) {
			selY = selY + selHeight;
			selHeight *= -1;
			selY += 1;
		}

		for(int x = selX; x < selX + selWidth; x++) {
			for(int y = selY; y < selY + selHeight; y++) {
				Tile t = level.getTileOrNull(x,y);
				if(t != null)
					t.ceilHeight += (value * 0.0625f);
			}
		}

		refreshLights();
	}

	public void doFloorMoveUp() {
		try {
			moveFloor(1);
		} catch( Exception ex ) {
			moveFloor(1);
		}
	}

	public void doFloorMoveDown() {
		try {
			moveFloor(-1);
		} catch( Exception ex ) {
			moveFloor(-1);
		}
	}

	public void doCeilMoveUp() {
		try {
			moveCeiling(1);
		} catch( Exception ex ) {
			moveCeiling(1);
		}
	}

	public void doCeilMoveDown() {
		try {
			moveCeiling(-1);
		} catch( Exception ex ) {
			moveCeiling(-1);
		}
	}

	Vector3 t_pickerVector = new Vector3();
	public void updatePickedSurface() {
		if (Collidor.intersectRayForwardFacingTriangles(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), camera, triangleSpatialHash.getAllTriangles(), t_pickerVector, intersectNormal)) {
			t_pickerVector.add(intersectTemp.x * 0.0001f, intersectTemp.y * 0.0001f, intersectTemp.z * 0.0001f);
			pickedSurface.position.set(t_pickerVector);
			pickedSurface.isPicked = true;

			Tile t = level.getTileOrNull((int)pickedSurface.position.x, (int)pickedSurface.position.z);
			if(t == null) {
				t = Tile.solidWall;
			}

			float floorHeight = t.getFloorHeight(pickedSurface.position.x, pickedSurface.position.z);
			float ceilHeight = t.getCeilHeight(pickedSurface.position.x, pickedSurface.position.z);

			if(t.data.isWater) {
				floorHeight += 0.4f;
			}

			if(t_pickerVector.x % 1.0 < 0.001f) pickedSurface.edge = TileEdges.East;
			if(t_pickerVector.x % 1.0 > 0.999f) pickedSurface.edge = TileEdges.West;
			if(t_pickerVector.z % 1.0 > 0.999f) pickedSurface.edge = TileEdges.South;
			if(t_pickerVector.z % 1.0 < 0.001f) pickedSurface.edge = TileEdges.North;
			boolean isLowerWall = intpos.y <= floorHeight && !t.blockMotion;

			if(Math.abs(floorHeight - pickedSurface.position.y) < 0.015f) {
				pickedSurface.tileSurface = TileSurface.Floor;
			}
			else if(Math.abs(ceilHeight - pickedSurface.position.y) < 0.015f) {
				pickedSurface.tileSurface = TileSurface.Ceiling;
			}
			else if(isLowerWall) {
				pickedSurface.tileSurface = TileSurface.LowerWall;
			}
			else {
				pickedSurface.tileSurface = TileSurface.UpperWall;
			}
		}
		else {
			pickedSurface.isPicked = false;
		}
	}

	SurfacePickerDecal surfacePickerDecal = null;
	public void renderPickedSurface() {
		if(surfacePickerDecal == null) {
			surfacePickerDecal = SurfacePickerDecal.newDecal(1f, 1f, editorSprites[17]);
		}
		surfacePickerDecal.setBlending(1, 1);
		surfacePickerDecal.setScale(2f, 2f);
		surfacePickerDecal.setTextureRegion(renderer.flashRegion);
		surfacePickerDecal.setColor(1f, 0f, 0f, 0.25f);

		if (pickedSurface.isPicked) {
			Tile t = level.getTileOrNull((int)pickedSurface.position.x, (int)pickedSurface.position.z);
			if(t == null) {
				t = Tile.solidWall;
			}

			SurfacePickerDecal d = surfacePickerDecal;

			// Which surface should we paint?
			if(pickedSurface.tileSurface == TileSurface.Floor) {
				d.setPosition((int)pickedSurface.position.x + 0.5f, t.floorHeight, (int)pickedSurface.position.z + 0.5f);
				d.setRotation(Vector3.Y, Vector3.Y);

				d.setTopLeftOffset(0, 0, t.slopeNE);
				d.setTopRightOffset(0, 0, t.slopeNW);
				d.setBottomLeftOffset(0, 0, t.slopeSE);
				d.setBottomRightOffset(0, 0, t.slopeSW);

				spriteBatch.add(d);
			}
			else if(pickedSurface.tileSurface == TileSurface.Ceiling) {
				d.setPosition((int)pickedSurface.position.x + 0.5f, t.ceilHeight, (int)pickedSurface.position.z + 0.5f);
				d.setRotation(Vector3.Y, Vector3.Y);

				d.setTopLeftOffset(0, 0, t.ceilSlopeNE);
				d.setTopRightOffset(0, 0, t.ceilSlopeNW);
				d.setBottomLeftOffset(0, 0, t.ceilSlopeSE);
				d.setBottomRightOffset(0, 0, t.ceilSlopeSW);

				spriteBatch.add(d);
			}
			else {
				// Draw a wall!
				int xLoc = (int)pickedSurface.position.x;
				int yLoc = (int)pickedSurface.position.z;

				float drawOffsetX = 0.5f;
				float drawOffsetY = 0.5f;

				d.setScale(1f, 1f);
				d.setRotation(Vector3.X, Vector3.Y);

				if(pickedSurface.edge == TileEdges.East) {
					xLoc -= 1f;
					drawOffsetX = 0.99f;
					d.setRotation(90, 0, 0);
				}
				else if(pickedSurface.edge == TileEdges.West) {
					xLoc += 1;
					drawOffsetX = 0.01f;
					d.setRotation(270, 0, 0);
				}
				else if(pickedSurface.edge == TileEdges.North) {
					yLoc -= 1;
					drawOffsetY = 0.99f;
					d.setRotation(0, 0, 0);
				}
				else if(pickedSurface.edge == TileEdges.South) {
					yLoc += 1;
					drawOffsetY = 0.01f;
					d.setRotation(180, 0, 0);
				}

				d.setPosition(xLoc + drawOffsetX, 0f, yLoc + drawOffsetY);

				Tile tile = level.getTile(xLoc, yLoc);
				setSurfacePickerSizes(pickedSurface.edge, tile, t, d, pickedSurface.tileSurface == TileSurface.UpperWall);
				spriteBatch.add(d);
			}
		}

		spriteBatch.flush();
	}

	public void setSurfacePickerSizes(TileEdges dir, Tile c, Tile checkDir, SurfacePickerDecal surface, boolean upper) {

		TileEdges oppositeDir = Tile.opposite(dir);

		FloatTuple ceilPair = null;
		FloatTuple floorPair = null;

		if(checkDir.IsSolid() || checkDir.isTileEdgeVisible(dir, c)) {
			// fully solid wall! make a wall from floor to ceiling
			ceilPair = c.getCeilingPair(dir);
			floorPair = c.getFloorPair(dir);
		}
		else {
			// add any partial segments that are exposed
			if(!upper) {
				if (checkDir.IsHigher(dir, c)) {
					ceilPair = checkDir.getFloorPair(oppositeDir).reverse();
					floorPair = new FloatTuple(Math.min(checkDir.getFloorPair(oppositeDir).val1, c.getFloorPair(dir).val1), Math.min(checkDir.getFloorPair(oppositeDir).val2, c.getFloorPair(dir).val2));
				}
			}
			else {
				if (checkDir.IsCeilLower(dir, c)) {
					ceilPair = new FloatTuple(Math.max(checkDir.getCeilingPair(oppositeDir).val1, c.getCeilingPair(dir).val1), Math.max(checkDir.getCeilingPair(oppositeDir).val2, c.getCeilingPair(dir).val2));
					floorPair = checkDir.getCeilingPair(oppositeDir).reverse();
				}
			}
		}

		if(ceilPair != null && floorPair != null) {
			surface.setTopLeftOffset(0, ceilPair.val2 - 0.5f, 0);
			surface.setTopRightOffset(0, ceilPair.val1 - 0.5f, 0);
			surface.setBottomLeftOffset(0, floorPair.val2 + 0.5f, 0);
			surface.setBottomRightOffset(0, floorPair.val1 + 0.5f, 0);
		}
	}

	public void paintSurfaceAtCursor() {
		if(pickedSurface.isPicked) {
			Tile t = level.getTileOrNull((int)pickedSurface.position.x, (int)pickedSurface.position.z);
			if(t == null) {
				t = Tile.NewSolidTile();
				level.setTile((int)pickedSurface.position.x, (int)pickedSurface.position.z, t);
			}

			boolean isUpperWall = pickedSurface.tileSurface == TileSurface.UpperWall;
			byte wallTex = (byte)pickedWallTexture;
			String wallTexAtlas = pickedWallTextureAtlas;

			// Use the bottom texture atlas?
			if(pickedSurface.tileSurface == TileSurface.LowerWall && !t.blockMotion) {
				wallTex = (byte)pickedWallBottomTexture;
				wallTexAtlas = pickedWallBottomTextureAtlas;
				isUpperWall = false;
			}

			// Which surface should we paint?
			if(pickedSurface.tileSurface == TileSurface.Floor) {
				t.floorTex = (byte)pickedFloorTexture;
				t.floorTexAtlas = pickedFloorTextureAtlas;
			}
			else if(pickedSurface.tileSurface == TileSurface.Ceiling) {
				t.ceilTex = (byte)pickedCeilingTexture;
				t.ceilTexAtlas = pickedCeilingTextureAtlas;
			}
			else if(pickedSurface.edge == TileEdges.East) {
				if(isUpperWall) {
					t.eastTex = wallTex;
					t.eastTexAtlas = wallTexAtlas;
				}
				else {
					t.bottomEastTex = wallTex;
					t.bottomEastTexAtlas = wallTexAtlas;
				}
			}
			else if(pickedSurface.edge == TileEdges.West) {
				if(isUpperWall) {
					t.westTex = wallTex;
					t.westTexAtlas = wallTexAtlas;
				}
				else {
					t.bottomWestTex = wallTex;
					t.bottomWestTexAtlas = wallTexAtlas;
				}
			}
			else if(pickedSurface.edge == TileEdges.North) {
				if(isUpperWall) {
					t.northTex = wallTex;
					t.northTexAtlas = wallTexAtlas;
				}
				else {
					t.bottomNorthTex = wallTex;
					t.bottomNorthTexAtlas = wallTexAtlas;
				}
			}
			else if(pickedSurface.edge == TileEdges.South) {
				if(isUpperWall) {
					t.southTex = wallTex;
					t.southTexAtlas = wallTexAtlas;
				}
				else {
					t.bottomSouthTex = wallTex;
					t.bottomSouthTexAtlas = wallTexAtlas;
				}
			}

			t.init(Source.EDITOR);

			history.saveState(level);
			refreshLights();
		}
	}

	public void pickTextureAtSurface() {
		if(pickedSurface.isPicked) {
			Tile t = level.getTile((int)pickedSurface.position.x, (int)pickedSurface.position.z);
			boolean isUpperWall = pickedSurface.tileSurface == TileSurface.UpperWall;

			// Use the bottom texture atlas?
			if(pickedSurface.tileSurface == TileSurface.LowerWall && !t.blockMotion) {
				isUpperWall = false;
			}

			// Which surface should we pick?
			if(pickedSurface.tileSurface == TileSurface.Floor) {
				setPickedFloorTexture(t.floorTex, t.floorTexAtlas);
			}
			else if(pickedSurface.tileSurface == TileSurface.Ceiling) {
				setPickedCeilingTexture(t.ceilTex, t.ceilTexAtlas);
			}
			else {
				if(isUpperWall) {
					setPickedWallTexture(t.getWallTex(pickedSurface.edge), t.getWallTexAtlas(pickedSurface.edge));
				}
				else {
					setPickedWallBottomTexture(t.getWallBottomTex(pickedSurface.edge), t.getWallBottomTexAtlas(pickedSurface.edge));
				}
			}
		}
	}

	public void pickNewSurfaceTexture() {
		if(!pickedSurface.isPicked)
			return;

		TextureRegionPicker picker = new TextureRegionPicker("Pick Texture", EditorUi.smallSkin, lastTextureRegionPickerSelection, TextureAtlas.getAllRepeatingAtlases()) {
			@Override
			public void result(Integer value, String atlas) {

				lastTextureRegionPickerSelection = atlas;

				if(pickedSurface.tileSurface == TileSurface.Ceiling) {
					setPickedCeilingTexture(value, atlas);
				}
				else if(pickedSurface.tileSurface == TileSurface.Floor) {
					setPickedFloorTexture(value, atlas);
				}
				else if(pickedSurface.tileSurface == TileSurface.UpperWall) {
					setPickedWallTexture(value, atlas);
				}
				else if(pickedSurface.tileSurface == TileSurface.LowerWall) {
					setPickedWallBottomTexture(value, atlas);
				}

				paintSurfaceAtCursor();
			}
		};

		editorUi.showModal(picker);
	}

	public TextureRegion[] loadAtlas(String texture, int spritesHorizontal, boolean filter) {
		Texture spriteTextures = Art.loadTexture(texture);

		if(filter) spriteTextures.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		int spriteSize = spriteTextures.getWidth() / spritesHorizontal;
		int numRows = spriteTextures.getHeight() / spriteSize;
		TextureRegion[] region = new TextureRegion[spritesHorizontal * numRows];

		for(int x = 0; x < spritesHorizontal; x++) {
			for(int y = 0; y < numRows; y++) {
				region[x + y * spritesHorizontal] = new TextureRegion(spriteTextures, x * spriteSize, y * spriteSize, spriteSize, spriteSize);
			}
		}

		return region;
	}

	public Color GetLightmapAt(float posx, float posy, float posz) {
		Color t = level.GetLightmapAt(posx, posz, posz);

		if(t == null) {
			return Color.BLACK;
		}

		return level.GetLightmapAt(posx, posy, posz);
	}

	Array<Vector3> t_collisionTriangles = new Array<Vector3>();
	VertexAttributes t_staticMeshVertexAttributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

	public Array<Mesh> mergeStaticMeshes(Level level, Array<Entity> entities) {
		short indexOffset = 0;
		int vertexCount = 0;

		ShortArray indices = new ShortArray(500);
		FloatArray vertices = new FloatArray(500);

		Array<Mesh> created = new Array<Mesh>();

		for(Entity e : entities) {
			if(e.drawable != null && e.drawable instanceof DrawableMesh) {
				DrawableMesh drbl = (DrawableMesh)e.drawable;

				if(drbl.loadedMesh != null && drbl.isStaticMesh) {

					// may need to chunk up the meshes
					if(drbl.loadedMesh.getNumIndices() + indexOffset >= 32000 && vertexCount > 0) {

						Mesh m = EditorCachePools.getStaticMesh(t_staticMeshVertexAttributes, vertexCount, indices.size, true);

						// pack the array
						indices.shrink();
						vertices.shrink();

						m.setIndices(indices.toArray());
						m.setVertices(vertices.toArray());

						indices = new ShortArray(500);
						vertices = new FloatArray(500);

						vertexCount = 0;
						indexOffset = 0;

						created.add(m);
					}

					VertexAttributes attributes = drbl.loadedMesh.getVertexAttributes();

					int positionOffset = 0;
					int uvOffset = 0;
					int attSize = 0;

					for(int i = 0; i < attributes.size(); i++) {
						VertexAttribute attrib = attributes.get(i);

						if(attrib.usage == VertexAttribute.Position().usage) {
							positionOffset = attSize;
						}
						else if(attrib.usage == VertexAttribute.TexCoords(0).usage) {
							uvOffset = attSize;
						}

						attSize += attrib.numComponents;
					}

					short[] ind = new short[drbl.loadedMesh.getNumIndices()];
					float[] verts = new float[drbl.loadedMesh.getNumVertices() * attSize];

					if(ind.length > 0)
						drbl.loadedMesh.getIndices(ind);
					else {
						ind = new short[drbl.loadedMesh.getNumVertices()];

						for(int i = 0; i < drbl.loadedMesh.getNumVertices(); i++) {
							ind[i] = (short)i;
						}
					}

					drbl.loadedMesh.getVertices(verts);

					Matrix4 matrix = new Matrix4().setToTranslation(e.x, e.z - 0.5f, e.y);
					matrix.scale(drbl.scale, drbl.scale, drbl.scale);
					matrix.mul(new Matrix4().setToRotation(Vector3.X, drbl.dir.y * -90f));

					matrix.rotate(Vector3.Y, drbl.rotZ);
					matrix.rotate(Vector3.X, drbl.rotX);
					matrix.rotate(Vector3.Z, drbl.rotY);

					// translate the vertices by the model matrix
					Matrix4.mulVec(matrix.val, verts, 0, drbl.loadedMesh.getNumVertices(), attSize);

					if(ind != null && verts != null) {
						for(int i = 0; i < ind.length; i++) {
							indices.add((short)(ind[i] + indexOffset));
						}
						indexOffset += ind.length;

						for(int i = 0; i < drbl.loadedMesh.getNumVertices() * attSize; i+= attSize) {

							vertices.add(verts[i + positionOffset]);
							vertices.add(verts[i + positionOffset + 1]);
							vertices.add(verts[i + positionOffset + 2]);

							if (e.isSolid) {
								t_collisionTriangles.add(EditorCachePools.getCollisionVector(verts[i + positionOffset], verts[i + positionOffset + 1], verts[i + positionOffset + 2]));
							}

							float c = Color.WHITE.toFloatBits();
							if(!e.fullbrite && showLights) {
								c = level.getLightColorAt(verts[i + positionOffset], verts[i + positionOffset + 2], verts[i + positionOffset + 1], null, new Color()).toFloatBits();
							}

							vertices.add(c);
							vertices.add(verts[i + uvOffset]);
							vertices.add(verts[i + uvOffset + 1]);

							vertexCount++;
						}
					}
				}
			}
		}

		if(vertexCount == 0) return null;

		for (int i = 0; i < t_collisionTriangles.size; i += 3) {
			Triangle triangle = EditorCachePools.getTriangle();
			triangle.v1.set(t_collisionTriangles.get(i + 2));
			triangle.v2.set(t_collisionTriangles.get(i + 1));
			triangle.v3.set(t_collisionTriangles.get(i));

			staticMeshCollisionTriangles.add(triangle);
		}
		t_collisionTriangles.clear();

		Mesh m = EditorCachePools.getStaticMesh(t_staticMeshVertexAttributes, vertexCount, indices.size, true);

		// pack the array
		indices.shrink();
		vertices.shrink();

		m.setIndices(indices.toArray());
		m.setVertices(vertices.toArray());

		created.add(m);
		return created;
	}

	public void doCarve() {
        if(pickedEntity != null) return;

		Tile t = new Tile();
		t.wallTex = (byte)pickedWallTexture;
        t.wallTexAtlas = pickedWallTextureAtlas;
		t.floorTex = (byte)pickedFloorTexture;
        t.floorTexAtlas = pickedFloorTextureAtlas;
		t.ceilTex = (byte)pickedCeilingTexture;
        t.ceilTexAtlas = pickedCeilingTextureAtlas;
        t.wallBottomTex = (byte)pickedWallBottomTexture;
        t.wallBottomTexAtlas = pickedWallBottomTextureAtlas;
		t.blockMotion = false;
		t.tileSpaceType = TileSpaceType.EMPTY;
		t.renderSolid = t.blockMotion;

		Tile selectedTile = level.getTile(selectionX, selectionY);
		t.floorHeight = selectedTile.floorHeight;
		t.ceilHeight = selectedTile.ceilHeight;

		if(pickedSurface == null || !pickedSurface.isPicked) {
			TextureAtlas atlas = TextureAtlas.getRepeatingAtlasByIndex(pickedWallTextureAtlas);
			float size = atlas.rowScale * atlas.scale;
			t.ceilHeight = size - 0.5f;
		}

		t.floorHeight = selectionHeights.y;

		setTile(t);

        // save undo history
        history.saveState(level);
	}

	public void doPaint() {
        if(pickedEntity != null) return;

		Tile t = new Tile();
		t.wallTex = (byte)pickedWallTexture;
        t.wallTexAtlas = pickedWallTextureAtlas;
		t.floorTex = (byte)pickedFloorTexture;
        t.floorTexAtlas = pickedFloorTextureAtlas;
		t.ceilTex = (byte)pickedCeilingTexture;
        t.ceilTexAtlas = pickedCeilingTextureAtlas;
        t.wallBottomTex = (byte)pickedWallBottomTexture;
        t.wallBottomTexAtlas = pickedWallBottomTextureAtlas;
        t.blockMotion = false;
        t.tileSpaceType = TileSpaceType.EMPTY;
		t.renderSolid = t.blockMotion;

		Tile selectedTile = level.getTile(selectionX, selectionY);
		t.floorHeight = selectedTile.floorHeight;
		t.ceilHeight = selectedTile.ceilHeight;

		paintTile(t);

        // save undo history
        history.saveState(level);
	}

	public void doDelete() {
        if(pickedEntity != null) {
            level.entities.removeValue(pickedEntity, true);

            for(Entity selEntity : additionalSelected) {
                level.entities.removeValue(selEntity, true);
            }

            clearEntitySelection();
            refreshLights();

            canDelete = false;
        }
        else {
            clearTiles();
        }

        // save undo history
        history.saveState(level);
	}

	public void doPick() {
		Tile t = level.getTile(selectionX, selectionY);
		if(t != null) {
			setPickedWallTexture(t.wallTex, t.wallTexAtlas);
			setPickedCeilingTexture(t.ceilTex, t.ceilTexAtlas);
			setPickedFloorTexture(t.floorTex, t.floorTexAtlas);
            setPickedWallBottomTexture(t.wallBottomTex != null ? t .wallBottomTex : t.wallTex, t.wallBottomTexAtlas);
		}
	}

    public void flattenFloor() {
        int selX = selectionX;
        int selY = selectionY;
        int selWidth = selectionWidth;
        int selHeight = selectionHeight;

        float matchFloorHeight = level.getTile(selectionX, selectionY).floorHeight;
        for(int x = selX; x < selX + selWidth; x++) {
            for(int y = selY; y < selY + selHeight; y++) {
                Tile t = level.getTileOrNull(x, y);
                if(t != null) {
                    t.floorHeight = matchFloorHeight;
                    t.slopeNE = t.slopeNW = t.slopeSE = t.slopeSW = 0;
                }
            }
        }
        refreshLights();
    }

    public void flattenCeiling() {
        int selX = selectionX;
        int selY = selectionY;
        int selWidth = selectionWidth;
        int selHeight = selectionHeight;

        float matchCeilHeight = level.getTile(selectionX, selectionY).ceilHeight;
        for(int x = selX; x < selX + selWidth; x++) {
            for(int y = selY; y < selY + selHeight; y++) {
                Tile t = level.getTileOrNull(x, y);
                if(t != null) {
                    t.ceilHeight = matchCeilHeight;
                    t.ceilSlopeNE = t.ceilSlopeNW = t.ceilSlopeSE = t.ceilSlopeSW = 0;
                }
            }
        }
        refreshLights();
    }

    public void toggleSimulation() {
        if(player == null) {
            player = new Player();
            player.x = camX - 0.5f;
            player.y = camY - 0.5f;
            player.z = camZ;
            player.rot = rotX - 3.14159265f;
            player.yrot = rotY;

            for(Entity e : level.entities) { e.editorStartPreview(level); }
        } else {
            player = null;

            for(Entity e : level.entities) { e.editorStopPreview(level); }
			Audio.stopLoopingSounds();
        }
    }

    public void createNewLevel(int width, int height) {
        level = new Level(width,height);
        refresh();

        camX = level.width / 2;
        camZ = 4.5f;
        camY = level.height / 2;
    }

    public void resizeLevel(int levelWidth, int levelHeight) {
        Level oldLevel = level;
        level = new Level(levelWidth,levelHeight);

        int offsetX = (level.width - oldLevel.width) / 2;
        int offsetY = (level.height - oldLevel.height) / 2;

        if(offsetX < 0) offsetX = 0;
        if(offsetY < 0) offsetY = 0;

        level.paste(oldLevel, offsetX, offsetY);

        refresh();
    }

	public void clearEntitySelection() {
		selected = false;
		tileDragging = false;

		selectionHeight = 1;
		selectionWidth = 1;

		pickedEntity = null;
		additionalSelected.clear();
		controlPoints.clear();
		pickedControlPoint = null;

		additionalSelected.clear();

        history.saveState(level);

        editorUi.showEntityPropertiesMenu(this);
	}

    public void pickEntity(Entity entity) {
        pickedEntity = entity;
        editorUi.showEntityPropertiesMenu(this);
    }

    public void pickAdditionalEntity(Entity entity) {
        additionalSelected.add(entity);
        editorUi.showEntityPropertiesMenu(this);
    }

    public Level getLevel() { return level; }

    public int getSelectionX() {
        return selectionX;
    }

    public int getSelectionY() {
        return selectionY;
    }

	public Vector3 getIntersection() {
		if(pickedSurface.isPicked)
			return new Vector3(intpos);

		float floorPos = level.getTile(selectionX, selectionY).floorHeight;
		return new Vector3(getSelectionX(), floorPos, getSelectionY());
	}

    public Entity getPickedEntity() { return pickedEntity; }

    public Entity getPickedOrHoveredEntity() {
        if(pickedEntity != null) return pickedEntity;
        return hoveredEntity;
    }

    public Entity getHoveredEntity() {
        return hoveredEntity;
    }

    public Array<Entity> getAdditionalSelectedEntities() {
        return additionalSelected;
    }

    public MoveMode getMoveMode() {
        return moveMode;
    }

    public Array<Triangle> GetCollisionTriangles() {
        return triangleSpatialHash.getAllTriangles();
    }

    public Array<Vector3> TriangleArrayToVectorList(Array<Triangle> triangles) {
        spatialWorkerList.clear();

        for(Triangle t : triangles) {
            spatialWorkerList.add(t.v1);
            spatialWorkerList.add(t.v2);
            spatialWorkerList.add(t.v3);
        }

        return spatialWorkerList;
    }

    public void setDragMode(DragMode dragMode) {
        if(pickedEntity == null) return;
        this.dragMode = dragMode;

        if(dragMode == DragMode.Y) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(pickedEntity.x, pickedEntity.z, pickedEntity.y));
            dragPlane = new Plane(vertDir, -len);
        }
        if(dragMode == DragMode.X) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(pickedEntity.x, pickedEntity.z, pickedEntity.y));
            dragPlane = new Plane(vertDir, -len);
        }
        if(dragMode == DragMode.Z) {
            Vector3 vertDir = new Vector3(camera.direction);
            vertDir.y = 0;

            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(pickedEntity.x, pickedEntity.z, pickedEntity.y));
            dragPlane = new Plane(vertDir, -len);
        }
    }

    public void setMoveMode(MoveMode moveMode) {
        this.moveMode = moveMode;
        if(moveMode == MoveMode.ROTATE) rotateStart = null;
    }

    public Decal getDecal() {
        Decal sd = decalPool.obtain();
        sd.setScale(1f);
        sd.setScale(1f, 1f);
        sd.setWidth(1f);
        sd.setHeight(1f);
        sd.transformationOffset = Vector2.Zero;
        usedDecals.add(sd);
        return sd;
    }

	private void vizualizePicking() {
		if(pickViz == null)
			pickViz = new SpriteBatch();

		pickViz.begin();
		pickViz.draw(pickerFrameBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
		pickViz.end();
	}
}
