package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.*;
import com.interrupt.dungeoneer.collision.Collidor;
import com.interrupt.dungeoneer.collision.CollisionTriangle;
import com.interrupt.dungeoneer.editor.file.EditorFile;
import com.interrupt.dungeoneer.editor.gfx.SurfacePickerDecal;
import com.interrupt.dungeoneer.editor.gizmos.Gizmo;
import com.interrupt.dungeoneer.editor.gizmos.GizmoProvider;
import com.interrupt.dungeoneer.editor.history.EditorHistory;
import com.interrupt.dungeoneer.editor.selection.AdjacentTileSelectionInfo;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.SaveChangesDialog;
import com.interrupt.dungeoneer.editor.ui.TextureRegionPicker;
import com.interrupt.dungeoneer.editor.ui.menu.generator.GeneratorInfo;
import com.interrupt.dungeoneer.editor.utils.LiveReload;
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
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.generator.RoomGenerator;
import com.interrupt.dungeoneer.generator.GenInfo.Markers;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.SpriteGroupStrategy;
import com.interrupt.dungeoneer.gfx.TextureAtlas;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;
import com.interrupt.dungeoneer.interfaces.Directional;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.tiles.Tile.TileSpaceType;
import com.interrupt.helpers.FloatTuple;
import com.interrupt.helpers.TileEdges;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.MonsterManager;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;
import com.noise.PerlinNoise;

import javax.swing.*;
import java.util.HashMap;

public class EditorApplication implements ApplicationListener {
	public JFrame frame;
	public EditorUi ui = null;
	public PerspectiveCamera camera = new PerspectiveCamera();
	public EditorCameraController cameraController = null;
	public EditorHistory history;
	public Player player = null;
	public Level level = null;
	public GlRenderer renderer = null;
	public EditorFile file = null;

	public enum ControlPointType { floor, ceiling, northCeil, northFloor, eastCeil, eastFloor, southCeil, southFloor, westCeil, westFloor, vertex };
	public enum ControlVertex { slopeNW, slopeNE, slopeSW, slopeSE, ceilNW, ceilNE, ceilSW, ceilSE }
	public enum DragMode { NONE, XY, X, Y, Z }
	public enum MoveMode { NONE, DRAG, ROTATE }

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

	protected Array<Vector3> spatialWorkerList = new Array<Vector3>();

	public enum TileSurface {Ceiling, Floor, UpperWall, LowerWall};

    public class PickedSurface {
    	public TileEdges edge;
    	public TileSurface tileSurface;
    	public boolean isPicked = false;
    	Vector3 position = new Vector3();
	}

	public PickedSurface pickedSurface = new PickedSurface();

	Vector3 selectionTempVector1 = new Vector3();

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
		public Vector3 point;
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

	private GameInput input;
    public EditorInput editorInput;
    private InputMultiplexer inputMultiplexer;

	private int curWidth;
    private int curHeight;

	private boolean rightClickWasDown = false;

    protected Pixmap wallPixmap;
    protected Texture selectionTex;
    protected Texture walltex;
    protected TextureRegion wallTextures[];
    protected TextureRegion editorSprites[];
    protected Texture meshtex;

    protected HashMap<Entity.ArtType, TextureRegion[]> spriteAtlases = new HashMap<Entity.ArtType, TextureRegion[]>();

    protected EntityManager entityManager;
    protected MonsterManager monsterManager;

	Mesh cubeMesh;
    Mesh gridMesh;

    private boolean slopePointMode = false;
    private boolean slopeEdgeMode = false;
    private int slopeSelNum = 0;

    public boolean selected = false;

    private boolean tileDragging = false;

    private boolean vertexSelectionMode = false;

    public float time = 0;

	protected DecalBatch spriteBatch;
    protected DecalBatch pointBatch;

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

	private boolean showLights = false;
	private boolean lightsDirty = true;

    private boolean movingEntity = false;
	private DragMode dragMode = DragMode.NONE;
    private MoveMode moveMode = MoveMode.DRAG;
    private Vector3 dragStart = null;

    private Vector3 rotateStart = null;
    private Vector3 rotateStartIntersection = null;

    private boolean readLeftClick = false;
    private boolean readRightClick = false;

	private Plane dragPlane = null;
    private Vector3 dragOffset = null;

    public HashMap<String, Array<Mesh>> staticMeshBatch = null;

    private ShapeRenderer lineRenderer;
    private ShapeRenderer pointRenderer;
    private ShapeRenderer boxRenderer;

    private boolean showGizmos = false;

	Color hoveredColor = new Color(0.5f, 1f, 0.5f, 1f);
	Color selectedColor = new Color(1f, 0.5f, 0.5f, 1f);

    Vector3 intersection = new Vector3();
	Vector3 tempVector1 = new Vector3();

	DrawableSprite unknownEntityMarker = new DrawableSprite();

	Array<ControlPoint> controlPoints = new Array<ControlPoint>();
	ControlPoint pickedControlPoint = null;
	public boolean movingControlPoint = false;

	Vector3 xGridStart = new Vector3();
	Vector3 xGridEnd = new Vector3();
	Vector3 yGridStart = new Vector3();
	Vector3 yGridEnd = new Vector3();

	// ground plane intersection
	Vector3 intpos = new Vector3();

	Plane p = new Plane(new Vector3(0,1,0), 0.5f);

	Vector3 rayOutVector = new Vector3();

	private LiveReload liveReload;

	private TileSelection entireLevelSelection;

	public GeneratorInfo generatorInfo;

	public EditorApplication() {
		frame = new JFrame("DelvEdit");

		Graphics.DisplayMode defaultMode = LwjglApplicationConfiguration.getDesktopDisplayMode();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "New Level - DelvEdit";
		config.fullscreen = false;
		config.width = defaultMode.width;
		config.height = defaultMode.height;
		config.vSyncEnabled = true;
		config.foregroundFPS = 120;
		config.backgroundFPS = 30;
		config.stencil = 8;

		config.addIcon("icon-128.png", Files.FileType.Internal); // 128x128 icon (mac OS)
		config.addIcon("icon-32.png", Files.FileType.Internal);  // 32x32 icon (Windows + Linux)
		config.addIcon("icon-16.png", Files.FileType.Internal);  // 16x16 icon (Windows)

		new LwjglApplication(this, config) {
		    public void close() {
		        Editor.dispose();
		        super.exit();
		        System.exit(0);
            }

		    @Override
            public void exit() {
                if (!file.isDirty()) {
                    close();
                }

                Dialog savePrompt = new SaveChangesDialog() {
                    @Override
                    public void onSave() {
                        Editor.app.file.save();
                    }

                    @Override
                    public void onDontSave() {
                        close();
                    }
                };

                savePrompt.show(Editor.app.ui.getStage());
            }
        };
	}

	public void init(){
        renderer = new GlRenderer();
        EditorArt.initAtlases();

		input = new GameInput();
		Gdx.input.setInputProcessor( input );

		editorInput = new EditorInput();

        renderer.init();
		renderer.enableLighting = showLights;

		cubeMesh = genCube();
		spriteBatch = new DecalBatch(new SpriteGroupStrategy(camera, null, GlRenderer.worldShaderInfo, 1));

		unknownEntityMarker.tex = 1000;
		unknownEntityMarker.artType = ArtType.hidden;

		liveReload = new LiveReload();
		liveReload.init();

		cameraController = new EditorCameraController();
		cameraController.init();

		StringManager.init();
		Game.init();

		loadEntities();
		loadMonsters();

		Gdx.input.setCursorCatched(false);
		generatorInfo = new GeneratorInfo();
        initTextures();

        pickedWallTextureAtlas = pickedWallBottomTextureAtlas = pickedFloorTextureAtlas = pickedCeilingTextureAtlas =
		TextureAtlas.cachedRepeatingAtlases.firstKey();

		createEmptyLevel(17, 17);
	}

	/** Load entity templates */
	public void loadEntities() {
		try {
			entityManager = Game.getModManager().loadEntityManager(Game.gameData.entityDataFiles);
			EntityManager.setSingleton(entityManager);
		} catch (Exception ex) {
			// whoops
			Gdx.app.log("Editor", "Error loading entities.dat: " + ex.getMessage());
		}
	}

	/** Load monster templates. */
	public void loadMonsters() {
		try {
			monsterManager = Game.getModManager().loadMonsterManager(Game.gameData.monsterDataFiles);
			MonsterManager.setSingleton(monsterManager);
		} catch (Exception ex) {
			// whoops
			Gdx.app.log("Editor", "Error loading monsters.dat: " + ex.getMessage());
		}
	}

	/** Creates an empty level with given `width` and `height`. */
	public void createEmptyLevel(int width, int height) {
		level = new Level(width, height);

		Tile t = new Tile();
		t.floorHeight = -0.5f;
		t.ceilHeight = 0.5f;
		level.setTile(width / 2, height / 2, t);

		cleanEditorState();
		cameraController.setDefaultPositionAndRotation();
	}

	/** Generates a level based on a `template` level. */
	public void generateLevelFromTemplate(Level template) {
		level.clear();

		level.theme = template.theme;
		level.generated = true;
		level.dungeonLevel = 0;

		GenTheme genTheme = DungeonGenerator.GetGenData(template.theme);
		int chunkTiles = genTheme.getChunkTileSize();
		int mapChunks = genTheme.getMapChunks();
		level.crop(0, 0, chunkTiles * mapChunks, chunkTiles * mapChunks);

		level.roomGeneratorChance = template.roomGeneratorChance;
		level.roomGeneratorType = template.roomGeneratorType;
		level.generate(Level.Source.EDITOR);

		cleanEditorState();
	}

	/** Generates a single room based on a `template` level. */
	public void generateRoomFromTemplate(Level template) {
		level.clear();

		GenTheme genTheme = DungeonGenerator.GetGenData(template.theme);
		int chunkTiles = genTheme.getChunkTileSize();
		Level generatedLevel = new Level(chunkTiles, chunkTiles);

		generatedLevel.roomGeneratorType = template.roomGeneratorType;

		RoomGenerator generator = new RoomGenerator(generatedLevel, template.roomGeneratorType);
		generator.generate(true, true, true, true);

		level.crop(0, 0, generatedLevel.width, generatedLevel.height);
		level.paste(generatedLevel, 0, 0);
		level.theme = template.theme;

		cleanEditorState();
	}

	/** Should be called after manually setting the `level` in the editor. */
	private void cleanEditorState() {
		entireLevelSelection = TileSelection.Rect(0, 0, level.width, level.height);
		refresh();

		history = new EditorHistory();
		file = new EditorFile();

		history.saveState(level);
		file.markClean();
	}

	@Override
	public void dispose() {
		if(gameApp != null) {
			gameApp.dispose();
		}

		cameraController.dispose();
		liveReload.dispose();
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

				float x = Game.instance.player.x;
				float z = Game.instance.player.z + Game.instance.player.eyeHeight;
				float y = Game.instance.player.y;

				float rotationX = Game.instance.player.rot;
				float rotationY = -Game.instance.player.yrot;

				cameraController.setPosition(x, y, z);
				cameraController.setRotation(rotationX, rotationY);

                refreshLights();

				Audio.stopLoopingSounds();
			}

			return;
		}

        tick();
        draw();

		renderer.clearLights();
		renderer.clearDecals();

		if(!ui.isShowingMenuOrModal() && pickedControlPoint == null && Editor.selection.hovered == null && Editor.selection.picked == null) {
			updatePickedSurface();
		}

		if((!Gdx.input.isButtonPressed(Buttons.LEFT) || ui.isShowingMenuOrModal()) && pickedControlPoint == null && Editor.selection.hovered == null && Editor.selection.picked == null) {
			renderPickedSurface();
		}

        Stage stage = ui.getStage();
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

	// Track the starting point of the selection area.
	int selStartX = 0;
	int selStartY = 0;

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

        if (!showLights) {
            GlRenderer.fogStart = 500f;
            GlRenderer.fogEnd = 500f;
            GlRenderer.fogColor.set(Color.BLACK);
        }
        else {
            GlRenderer.fogStart = level.fogStart;
            GlRenderer.fogEnd = level.fogEnd;
            GlRenderer.fogColor.set(level.fogColor);
        }

		GlRenderer.viewDistance = level.viewDistance;

		cameraController.draw();

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

		if(showLights && lightsDirty) {
			lightsDirty = false;
			level.updateLights(Source.EDITOR);
		}

        // Draw skybox
        if(showLights && level.skybox != null) {
            level.skybox.x = camera.position.x;
            level.skybox.z = camera.position.y;
            level.skybox.y = camera.position.z;
            level.skybox.scale = 4f;
            level.skybox.fullbrite = true;
            level.skybox.update();

            // draw sky
            Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
            renderer.renderSkybox(level.skybox);
            Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
            Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        }

        renderer.Tesselate(level);
        renderer.renderWorld(level);

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
			if(e == Editor.selection.picked) {
				e.editorState = EditorState.picked;
			}
			else if(Editor.selection.isSelected(e)) {
				e.editorState = EditorState.picked;
			}
			else if(e == Editor.selection.hovered) {
				e.editorState = EditorState.hovered;
			}
			else {
				e.editorState = EditorState.none;
			}

			if(e.editorState != EditorState.none) {
				selectedEntities.add(e);
			}
		}

		for(int i = 0; i < level.non_collidable_entities.size; i++) {
			Entity e = level.non_collidable_entities.get(i);
			if(e == Editor.selection.picked) {
				e.editorState = EditorState.picked;
			}
			else if(e == Editor.selection.hovered) {
				e.editorState = EditorState.hovered;
			}
			else {
				e.editorState = EditorState.none;
			}

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
		if(pickedSurface.isPicked && editorInput.isButtonPressed(Input.Buttons.LEFT) && !ui.isShowingMenuOrModal()) {
			shouldDrawBox = true;
		}

		// Always draw the box when tiles are selected
		shouldDrawBox |= selected;

		// don't draw the box when freelooking
		if(shouldDrawBox && !selected && Gdx.input.isCursorCatched()) {
			shouldDrawBox = false;
		}

		shouldDrawBox = ui.isShowingModal() ? false : shouldDrawBox;

		if(Editor.selection.picked == null && Editor.selection.hovered == null || tileDragging) {
			if(!selected || (!(pickedControlPoint != null || movingControlPoint) &&
                    editorInput.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())) {

				Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
				Vector3 start = tempVec1.set(ray.origin);
				Vector3 end = tempVec2.set(intpos.x, intpos.y, intpos.z);
				float distance = start.sub(end).len();

				end = ray.getEndPoint(rayOutVector, distance + 0.005f);

				// Tile selection bounding
				Editor.selection.tiles.x = Math.min(level.width - 1, Math.max(0, (int)end.x));
				Editor.selection.tiles.y = Math.min(level.height - 1, Math.max(0, (int)end.z));

				shouldDrawBox &= entireLevelSelection.contains((int)end.x, (int)end.z);

				selStartX = Editor.selection.tiles.x;
				selStartY = Editor.selection.tiles.y;

				Editor.selection.tiles.setStartTile(selStartX, selStartY);

				controlPoints.clear();
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
						// The user is dragging the selection area to set its size. Update the selection area based on their mouse position.
						int newX = MathUtils.clamp((int) intpos.x, 0, level.width - 1);
						int newY = MathUtils.clamp((int) intpos.z, 0, level.height - 1);

						Editor.selection.tiles.width = Math.abs(selStartX - newX) + 1;
						Editor.selection.tiles.height = Math.abs(selStartY - newY) + 1;
						// Always make this the lowest corner so that the selection size, which is relative to this, is positive.
						Editor.selection.tiles.x = Math.min(newX, selStartX);
						Editor.selection.tiles.y = Math.min(newY, selStartY);

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

			Tile t = Editor.selection.tiles.first();
			if(t == null || t.renderSolid) {
				// TODO: Set cursor height to match atlas.
				//TextureAtlas atlas = TextureAtlas.getRepeatingAtlasByIndex(pickedWallTextureAtlas);
				//float size = atlas.rowScale * atlas.scale;
				//if(!selected) selectionHeights.set(size - 0.5f, -0.5f);
			}

			if(slopePointMode || slopeEdgeMode)
				drawSlopeLines(slopeSelNum, slopeEdgeMode);

			// Draw selection
			if(shouldDrawBox) {
				boxRenderer.setColor(0.75f, 0.75f, 0.75f, 0.5f);
				boxRenderer.begin(ShapeType.Line);

				BoundingBox bounds = Editor.selection.tiles.getBounds();
				boxRenderer.box(bounds.min.x, bounds.min.z, bounds.min.y, bounds.getWidth(), bounds.getDepth(), -bounds.getHeight());

				boxRenderer.end();
			}
		}

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		if(Editor.selection.picked instanceof Mover) {
			renderMoverVizualization((Mover) Editor.selection.picked);
		}
		for(Entity selectedEntity : Editor.selection.selected) {
			if(selectedEntity instanceof Mover) {
				renderMoverVizualization((Mover)selectedEntity);
			}
		}

		// ROTATE
		if(moveMode == MoveMode.ROTATE && Editor.selection.picked instanceof Directional) {
			Directional pickedDirectional = (Directional) Editor.selection.picked;
			dragPlane = new Plane(new Vector3(0,-1,0), Editor.selection.picked.z);

			if(Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intpos)) {

				if(rotateStart == null) {
					rotateStart = new Vector3(pickedDirectional.getRotation());
					rotateStartIntersection = new Vector3(intpos);
				}

				Vector3 rotateDirection = new Vector3(
						Editor.selection.picked.x - intpos.x,
						Editor.selection.picked.y - intpos.z,
						Editor.selection.picked.z - intpos.y).nor();

				Vector3 rotateStartDirection = new Vector3(
						Editor.selection.picked.x - rotateStartIntersection.x,
						Editor.selection.picked.y - rotateStartIntersection.z,
						Editor.selection.picked.z - rotateStartIntersection.y).nor();

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

				refreshEntity(Editor.selection.picked);
			}
		}

		// drag tile
		if(selected && (!readLeftClick || movingControlPoint)) {
			int selX = Editor.selection.tiles.x;
			int selY = Editor.selection.tiles.y;
			int selWidth = Editor.selection.tiles.width;
			int selHeight = Editor.selection.tiles.height;

			Tile startTile = Editor.selection.tiles.first();

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
                    for (TileSelectionInfo info : Editor.selection.tiles) {
                        Tile current = info.tile;

                        if(current != null && !current.renderSolid) {
                            if(current.tileSpaceType != TileSpaceType.OPEN_SE) {
                                controlPoints.add(new ControlPoint(new Vector3(info.x, current.ceilHeight + current.ceilSlopeNE, info.y), new ControlPointVertex(current,ControlVertex.ceilNE)));
                                controlPoints.add(new ControlPoint(new Vector3(info.x, current.floorHeight + current.slopeNE, info.y), new ControlPointVertex(current,ControlVertex.slopeNE)));
                            }

                            if(current.tileSpaceType != TileSpaceType.OPEN_SW) {
                                controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.ceilHeight + current.ceilSlopeNW, info.y), new ControlPointVertex(current,ControlVertex.ceilNW)));
                                controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.floorHeight + current.slopeNW, info.y), new ControlPointVertex(current,ControlVertex.slopeNW)));
                            }

                            if(current.tileSpaceType != TileSpaceType.OPEN_NE) {
                                controlPoints.add(new ControlPoint(new Vector3(info.x, current.ceilHeight + current.ceilSlopeSE, info.y + 1), new ControlPointVertex(current,ControlVertex.ceilSE)));
                                controlPoints.add(new ControlPoint(new Vector3(info.x, current.floorHeight + current.slopeSE, info.y + 1), new ControlPointVertex(current,ControlVertex.slopeSE)));
                            }

                            if(current.tileSpaceType != TileSpaceType.OPEN_NW) {
                                controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.ceilHeight + current.ceilSlopeSW, info.y + 1), new ControlPointVertex(current,ControlVertex.ceilSW)));
                                controlPoints.add(new ControlPoint(new Vector3(info.x + 1, current.floorHeight + current.slopeSW, info.y + 1), new ControlPointVertex(current,ControlVertex.slopeSW)));
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
				if(!ui.isShowingContextMenu()) {
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
					if(!north.renderSolid)
						drawLine(tempVec1.set(xx, north.ceilSlopeNE + north.ceilHeight, selY), tempVec2.set(xx + 1f,north.ceilSlopeNW + north.ceilHeight,selY), 2f, pickedControlPoint != null && pickedControlPoint.isNorthCeiling() ? Color.WHITE : Color.RED);

					// ceil south
					if(!south.renderSolid)
						drawLine(tempVec1.set(xx, south.ceilSlopeSE + south.ceilHeight, selY + selHeight), tempVec2.set(xx + 1f,south.ceilSlopeSW + south.ceilHeight,selY + selHeight), 2f, pickedControlPoint != null && pickedControlPoint.isSouthCeiling() ? Color.WHITE : Color.RED);

					// floor north
					if(!north.renderSolid)
						drawLine(tempVec1.set(xx, north.slopeNE + north.floorHeight, selY), tempVec2.set(xx + 1f,north.slopeNW + north.floorHeight,selY), 2f, pickedControlPoint != null && pickedControlPoint.isNorthFloor() ? Color.WHITE : Color.RED);

					// floor south
					if(!south.renderSolid)
						drawLine(tempVec1.set(xx, south.slopeSE + south.floorHeight, selY + selHeight), tempVec2.set(xx + 1f,south.slopeSW + south.floorHeight,selY + selHeight), 2f, pickedControlPoint != null && pickedControlPoint.isSouthFloor() ? Color.WHITE : Color.RED);
				}

				for(int yy = selY; yy < selY + selHeight; yy++) {
					Tile west = level.getTile(selX, yy);
					Tile east = level.getTile(selX + selWidth - 1, yy);

					// ceil west
					if(!west.renderSolid)
						drawLine(tempVec1.set(selX, west.ceilSlopeNE + west.ceilHeight, yy), tempVec2.set(selX,west.ceilSlopeSE + west.ceilHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isWestCeiling() ? Color.WHITE : Color.RED);

					// ceil east
					if(!east.renderSolid)
						drawLine(tempVec1.set(selX + selWidth, east.ceilSlopeNW + east.ceilHeight, yy), tempVec2.set(selX + selWidth,east.ceilSlopeSW + east.ceilHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isEastCeiling() ? Color.WHITE : Color.RED);

					// floor west
					if(!west.renderSolid)
						drawLine(tempVec1.set(selX, west.slopeNE + west.floorHeight, yy), tempVec2.set(selX,west.slopeSE + west.floorHeight,yy + 1), 2f, pickedControlPoint != null && pickedControlPoint.isWestFloor() ? Color.WHITE : Color.RED);

					// floor east
					if(!east.renderSolid)
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

				int selX = Editor.selection.tiles.x;
				int selY = Editor.selection.tiles.y;
				int selWidth = Editor.selection.tiles.width;
				int selHeight = Editor.selection.tiles.height;

                for (TileSelectionInfo info : Editor.selection.tiles) {
                    Tile t = info.tile;
                    if (t == null) {
                        continue;
                    }

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

                        float mod = 1 - ((float)info.y - (float)selY) / (float)selHeight;
                        if(pickedControlPoint.controlPointType == ControlPointType.northCeil) {
                            t.ceilSlopeNE -= dragOffset.y * mod;
                            t.ceilSlopeNW -= dragOffset.y * mod;
                        }
                        else {
                            t.slopeNE -= dragOffset.y * mod;
                            t.slopeNW -= dragOffset.y * mod;
                        }

                        if(selHeight > 1) {
                            mod = 1 - ((float)info.y - (float)selY + 1f) / (float)selHeight;
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

                        float mod = ((float)info.y - (float)selY + 1) / (float)selHeight;
                        if(pickedControlPoint.controlPointType == ControlPointType.southCeil) {
                            t.ceilSlopeSE -= dragOffset.y * mod;
                            t.ceilSlopeSW -= dragOffset.y * mod;
                        }
                        else {
                            t.slopeSE -= dragOffset.y * mod;
                            t.slopeSW -= dragOffset.y * mod;
                        }

                        if(selHeight > 1) {
                            mod = ((float)info.y - (float)selY) / (float)selHeight;
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

                        float mod = 1 - ((float)info.x - (float)selX) / (float)selWidth;
                        if(pickedControlPoint.controlPointType == ControlPointType.westCeil) {
                            t.ceilSlopeNE -= dragOffset.y * mod;
                            t.ceilSlopeSE -= dragOffset.y * mod;
                        }
                        else {
                            t.slopeNE -= dragOffset.y * mod;
                            t.slopeSE -= dragOffset.y * mod;
                        }

                        if(selWidth > 1) {
                            mod = 1 - ((float)info.x - (float)selX + 1f) / (float)selWidth;
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

                        float mod = ((float)info.x - (float)selX + 1) / (float)selWidth;
                        if(pickedControlPoint.controlPointType == ControlPointType.eastCeil) {
                            t.ceilSlopeNW -= dragOffset.y * mod;
                            t.ceilSlopeSW -= dragOffset.y * mod;
                        }
                        else {
                            t.slopeNW -= dragOffset.y * mod;
                            t.slopeSW -= dragOffset.y * mod;
                        }

                        if(selWidth > 1) {
                            mod = ((float)info.x - (float)selX) / (float)selWidth;
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
			if(movingEntity && Editor.selection.picked != null) {
				if(dragPlane == null) {

					if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
						// Make a copy
						Entity copy = JsonUtil.fromJson(Editor.selection.picked.getClass(), JsonUtil.toJson(Editor.selection.picked));
						level.entities.add(copy);

                        pickEntity(copy);

						Array<Entity> copies = new Array<Entity>();
						for(Entity selected : Editor.selection.selected) {
							Entity newCopy = JsonUtil.fromJson(selected.getClass(), JsonUtil.toJson(selected));
							level.entities.add(newCopy);
							copies.add(newCopy);
						}

						Editor.selection.selected.clear();
						Editor.selection.selected.addAll(copies);
                        ui.showEntityPropertiesMenu(true);
					}

					if(dragMode == DragMode.Y) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0f);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(dragMode == DragMode.Z) {
						Vector3 vertDir = t_dragVector.set(camera.direction);
						vertDir.y = 0;

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(dragMode == DragMode.X) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
					else if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
						Vector3 vertDir = t_dragVector.set(Vector3.Y);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);

						dragPlane = t_dragPlane;
						dragMode = DragMode.XY;
					}
					else {
						Vector3 vertDir = t_dragVector.set(camera.direction);

						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
						Plane vert = t_dragPlane;

						float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
						t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
						dragPlane = t_dragPlane;
					}
				}

				if(dragStart == null)
					dragStart = new Vector3(Editor.selection.picked.x, Editor.selection.picked.y, Editor.selection.picked.z);

				if(moveMode == MoveMode.DRAG && Intersector.intersectRayPlane(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intpos)) {
					if(dragOffset == null) {
						dragOffset = t_dragOffset.set(Editor.selection.picked.x - intpos.x, Editor.selection.picked.y - intpos.z, Editor.selection.picked.z - intpos.y);
					}

					float startX = Editor.selection.picked.x;
					float startY = Editor.selection.picked.y;
					float startZ = Editor.selection.picked.z;

					Editor.selection.picked.x = intpos.x + dragOffset.x;
					Editor.selection.picked.y = intpos.z + dragOffset.y;
					Editor.selection.picked.z = intpos.y + dragOffset.z;

					if(dragMode == DragMode.XY) {
						Editor.selection.picked.z = dragStart.z;
					}
					if(dragMode == DragMode.Y) {
                        Editor.selection.picked.x = dragStart.x;
					}
					else if(dragMode == DragMode.Z) {
						Editor.selection.picked.x = dragStart.x;
						Editor.selection.picked.y = dragStart.y;
					}
					else if(dragMode == DragMode.X) {
                        Editor.selection.picked.y = dragStart.y;
					}

					if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT)) {
						Editor.selection.picked.x = (int)(Editor.selection.picked.x * 8) / 8f;
						Editor.selection.picked.y = (int)(Editor.selection.picked.y * 8) / 8f;
						Editor.selection.picked.z = (int)(Editor.selection.picked.z * 8) / 8f;
					}

					float movedX = startX - Editor.selection.picked.x;
					float movedY = startY - Editor.selection.picked.y;
					float movedZ = startZ - Editor.selection.picked.z;

					for(Entity selected : Editor.selection.selected) {
						selected.x -= movedX;
						selected.y -= movedY;
						selected.z -= movedZ;
					}

                    refreshEntity(Editor.selection.picked);
                    for(Entity selected : Editor.selection.selected) {
                        refreshEntity(selected);
                    }
				}
			}
			else {
				dragOffset = null;
				dragStart = null;
				dragPlane = null;
			}

			if(Editor.selection.picked == null) {
				dragPlane = null;
				dragMode = DragMode.NONE;
				moveMode = MoveMode.DRAG;
			}

			// Draw rotation circles
			if(moveMode == MoveMode.ROTATE) {
				if(dragMode == DragMode.X) {
					drawXCircle(Editor.selection.picked.x, Editor.selection.picked.z - 0.49f, Editor.selection.picked.y, 2f, EditorColors.X_AXIS);
				}
				else if(dragMode == DragMode.Y) {
					drawYCircle(Editor.selection.picked.x, Editor.selection.picked.z - 0.49f, Editor.selection.picked.y, 2f, EditorColors.Y_AXIS);
				}
				else {
					drawZCircle(Editor.selection.picked.x, Editor.selection.picked.z - 0.49f, Editor.selection.picked.y, 2f, EditorColors.Z_AXIS);
				}

				if(Editor.selection.picked instanceof Directional) {
					Directional dirEntity = (Directional) Editor.selection.picked;

					Vector3 dirEnd = dirEntity.getDirection();
					dirEnd.x += Editor.selection.picked.x;
					dirEnd.y += Editor.selection.picked.y;
					dirEnd.z += Editor.selection.picked.z;

					drawLine(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z - 0.49f, Editor.selection.picked.y), new Vector3(dirEnd.x, dirEnd.z - 0.49f,dirEnd.y), 2f, Color.WHITE);
				}
			}

			if(Editor.selection.picked != null && ((Editor.selection.hovered == null || Editor.selection.isSelected(Editor.selection.hovered)) || Editor.selection.hovered == Editor.selection.picked || movingEntity)) {
				Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
				Gdx.gl.glEnable(GL20.GL_ALPHA);
				Gdx.gl.glEnable(GL20.GL_BLEND);

				if(moveMode == MoveMode.DRAG) {
					if(dragMode == DragMode.Z) {
						Vector3 startLine = tempVec3.set(Editor.selection.picked.x, Editor.selection.picked.z - 10f, Editor.selection.picked.y);
						Vector3 endLine = tempVec4.set(Editor.selection.picked.x, Editor.selection.picked.z + 10f, Editor.selection.picked.y);
						this.drawLine(startLine, endLine, 2, EditorColors.Z_AXIS);
					}
					else if(dragMode == DragMode.X) {
						Vector3 startLine = tempVec3.set(Editor.selection.picked.x - 10f, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y);
						Vector3 endLine = tempVec4.set(Editor.selection.picked.x + 10f, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y);
						this.drawLine(startLine, endLine, 2, EditorColors.X_AXIS);
					}
					else if(dragMode == DragMode.Y) {
						Vector3 startLine = tempVec3.set(Editor.selection.picked.x, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y -10f);
						Vector3 endLine = tempVec4.set(Editor.selection.picked.x, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y + 10f);
						this.drawLine(startLine, endLine, 2, EditorColors.Y_AXIS);
					}
					else if(dragMode == DragMode.XY || (!movingEntity && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))) {
						Vector3 startLine = tempVec3.set(Editor.selection.picked.x, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y - 10f);
						Vector3 endLine = tempVec4.set(Editor.selection.picked.x, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y + 10f);
						this.drawLine(startLine, endLine, 2, EditorColors.Y_AXIS);

						startLine = tempVec3.set(Editor.selection.picked.x - 10f, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y);
						endLine = tempVec4.set(Editor.selection.picked.x + 10f, Editor.selection.picked.z - 0.5f, Editor.selection.picked.y);
						this.drawLine(startLine, endLine, 2, EditorColors.X_AXIS);
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
			drawLine(xGridStart, xGridEnd, 2f, EditorColors.X_AXIS_DARK);
			drawLine(yGridStart, yGridEnd, 2f, EditorColors.Y_AXIS_DARK);
			lineRenderer.end();
		}

		Gdx.gl.glDisable(GL20.GL_BLEND);
		renderTriggerLines();

		if (showGizmos) {
			drawAllGizmos();
		}
		else {
			drawPickedGizmos();
		}
	}

	/** Draw Gizmos for the picked Entity and selected Entities. */
	private void drawPickedGizmos() {
		drawGizmo(Editor.selection.picked);

		for (Entity selected : Editor.selection.selected) {
			drawGizmo(selected);
		}
	}

	/** Draw Gizmos for all Entities in the level. */
	private void drawAllGizmos() {
		for (Entity entity : level.entities) {
			drawGizmo(entity);
		}
	}

	/** Draw Gizmo for the given entity. */
	private void drawGizmo(Entity entity) {
		if (entity == null) {
			return;
		}

		Gizmo gizmo = GizmoProvider.getGizmo(entity.getClass());
		gizmo.draw(entity);
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

				Editor.selection.hovered = renderer.entitiesForPicking.get(index);
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

		renderer.Tesselate(level);
		renderer.renderWorld(level);

		// Pull the vertices a bit closer to the camera this time around, to stop z-fighting with the depth of
		// previously drawn objects.
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
		Gdx.gl.glPolygonOffset(-0.15f, 1);

		// LEQUAL
		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);

		renderer.renderEntitiesForPicking(level);
		pickerFrameBuffer.end();

		// Put things back to normal
		Gdx.gl.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
	}

	private void refreshTriangleSpatialHash() {
		GlRenderer.triangleSpatialHash.Flush();
	}

	protected void refreshEntity(Entity theEntity) {
		if(theEntity instanceof Light && showLights) {
			markWorldAsDirty((int)theEntity.x, (int)theEntity.y, (int)((Light)theEntity).range + 1);
			((Light)theEntity).clearCanSee();
			lightsDirty = true;
		}
		else if(theEntity instanceof ProjectedDecal) {
			((ProjectedDecal)theEntity).refresh();
			((ProjectedDecal)theEntity).updateDrawable();
		}
		else if(theEntity instanceof Model) {
			Model m = (Model)theEntity;
			if(!m.isDynamic) {
				// Todo: Get range from the bounding box
				markWorldAsDirty((int)theEntity.x, (int)theEntity.y, 5);
			}
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

	public void turnPickedEntityLeft() {
		if(Editor.selection.picked != null) {
			Editor.selection.picked.rotate90();
			for(Entity e : Editor.selection.selected) {
				e.rotate90();
			}
			refreshEntity(Editor.selection.picked);
		}
	}

	public void turnPickedEntityRight() {
		if(Editor.selection.picked != null) {
			Editor.selection.picked.rotate90Reversed();
			for(Entity e : Editor.selection.selected) {
				e.rotate90Reversed();
			}
			refreshEntity(Editor.selection.picked);
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
				if(e == Editor.selection.picked)
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
				if(e == Editor.selection.picked)
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
				if(e == Editor.selection.picked)
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

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            int x = info.x;
            int y = info.y;

            if (t != null) {
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
        if(ui != null) ui.resize(width, height);

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

		pickerFrameBuffer = CreateFrameBuffer(pickerFrameBuffer, width, height, true, false);

		if(pickerPixelBuffer != null)
			pickerPixelBuffer.dispose();

		pickerPixelBuffer = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Format.RGBA8888);
	}

	@Override
	public void resume() {
	}

	@Override
	public void create() {
		init();
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

		if(ui.isShowingMenuOrModal())
			return;

		// get picked entity
		Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());

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
		if (Collidor.intersectRayForwardFacingTriangles(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), camera, GlRenderer.triangleSpatialHash.getAllTriangles(), intpos, intersectNormal)) {
			intersectTemp.set(intpos).sub(camera.position).nor();
			intpos.add(intersectTemp.x * -0.05f, 0.0001f, intersectTemp.z * -0.05f);
		}

		if(tileDragging) {
			tileDragging = Gdx.input.isButtonPressed(Buttons.LEFT);
		}

		if(Gdx.input.isKeyJustPressed(Keys.TAB)) {
			Editor.selection.clear();
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
			else if(Editor.selection.hovered == null && Editor.selection.picked == null) {
				selected = true;
			}
			else {
				if(!readLeftClick) {
					if(Editor.selection.picked != null && Editor.selection.hovered != null && Editor.selection.hovered != Editor.selection.picked && !Editor.selection.isSelected(Editor.selection.hovered) && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
                        pickAdditionalEntity(Editor.selection.hovered);
					}
					else if(Editor.selection.picked != null && Editor.selection.picked == Editor.selection.hovered || Editor.selection.isSelected(Editor.selection.hovered)) {
						movingEntity = true;
					}
					else {
						clearEntitySelection();
                        pickEntity(Editor.selection.hovered);
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
		if(Editor.selection.picked == null) movingEntity = false;

		// Tile editing mode?
		if(Editor.selection.picked == null) {
			if(Gdx.input.isKeyPressed(Keys.NUM_1)) {
				paintSurfaceAtCursor();
			}
			else if(Gdx.input.isKeyPressed(Keys.NUM_2)) {
				pickTextureAtSurface();
			}
		}

		if(player != null) {
			player.inEditor = true;
			level.editorTick(player, Gdx.graphics.getDeltaTime() * 60f);
		}
		else {
			level.editorTick(player, 0);
		}

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

		// Tick subsystems.
		input.tick();
        editorInput.tick();
        cameraController.tick();
        liveReload.tick();

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
				Editor.selection.picked = null;
				Editor.selection.selected.clear();

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
			Editor.selection.picked = null;
			Editor.selection.selected.clear();

			refresh();
		}
		catch(Exception ex) {
			Gdx.app.error("Editor", ex.getMessage(), ex);
		}
    }

    public void toggleGizmos() {
        showGizmos = !showGizmos;
    }

    public void clearSelection() {
        if(slopePointMode || slopeEdgeMode) {
            slopePointMode = false;
            slopeEdgeMode = false;
        }
        else {
            clearEntitySelection();
		}

		if (ui.isShowingContextMenu()) {
			ui.hideContextMenu();
		}
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

	public void testLevel(boolean useCameraPosition) {
		editorInput.resetKeys();

		gameApp = new GameApplication();
		GameApplication.editorRunning = true;

		Level previewLevel = (Level) KryoSerializer.copyObject(level);
		if(previewLevel.theme == null) previewLevel.theme = "TEST";

        previewLevel.levelName = "EDITOR LEVEL";
		previewLevel.genTheme = DungeonGenerator.GetGenData(previewLevel.theme);

		gameApp.createFromEditor(previewLevel);
		Game.isDebugMode = true;

		EditorMarker startMarker = null;
		if (!useCameraPosition) {
			for (EditorMarker marker : level.editorMarkers) {
				if (marker.type == Markers.playerStart || marker.type == Markers.stairUp) {
					startMarker = marker;
					break;
				}
			}
		}

		if (!useCameraPosition && startMarker != null) {
			Game.instance.player.x = startMarker.x + 0.5f;
			Game.instance.player.y = startMarker.y + 0.5f;
			Game.instance.player.z = previewLevel.getTile((int) Game.instance.player.x, (int) Game.instance.player.y)
					.getFloorHeight() + 0.5f;

			Game.instance.player.rot = (float) Math.toRadians(-(startMarker.rot + 180f));
		} else {
			Vector3 cameraPosition = cameraController.getPosition();
			Vector2 cameraRotation = cameraController.getRotation();

			Game.instance.player.x = cameraPosition.x;
			Game.instance.player.y = cameraPosition.y - Game.instance.player.eyeHeight;
			Game.instance.player.z = cameraPosition.z;
			Game.instance.player.rot = cameraRotation.x;
			Game.instance.player.yrot = -cameraRotation.y;
		}
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
		ui = new EditorUi();

        wallPickerButton = new Image(new TextureRegionDrawable(wallTextures[0]));
        wallPickerButton.setScaling(Scaling.stretch);

        bottomWallPickerButton = new Image(new TextureRegionDrawable(wallTextures[0]));
        bottomWallPickerButton.setScaling(Scaling.stretch);

        ceilPickerButton = new Image(new TextureRegionDrawable(wallTextures[1]));
        ceilPickerButton.setScaling(Scaling.stretch);

        floorPickerButton = new Image(new TextureRegionDrawable(wallTextures[2]));
        floorPickerButton.setScaling(Scaling.stretch);

        Stage stage = ui.getStage();
        Table wallPickerLayoutTable = new Table();
        wallPickerLayoutTable.setFillParent(true);
        wallPickerLayoutTable.align(Align.left | Align.top).pad(6f).padTop(150f);

        Label wallLabel = new Label("Upper Wall", ui.getSmallSkin());
        Label wallBottomLabel = new Label("Lower Wall", ui.getSmallSkin());
        Label ceilingLabel = new Label("Ceiling", ui.getSmallSkin());
        Label floorLabel = new Label("Floor", ui.getSmallSkin());

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

        paintAdjacent = new CheckBox("Paint adjacent", ui.getSmallSkin());
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
				ui.showModal(picker);
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
				ui.showModal(picker);
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
                ui.showModal(picker);
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
				ui.showModal(picker);
				event.handle();
            }
        });

        stage.addActor(wallPickerLayoutTable);
        ui.initUi();

        inputMultiplexer = new InputMultiplexer();

        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(editorInput);
        inputMultiplexer.addProcessor(input);

        setInputProcessor();

		ui.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void setInputProcessor() {
        Gdx.input.setInputProcessor(inputMultiplexer);
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

		Mesh mesh = new Mesh(false, vertices.length, indices.length, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"));
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

		float tau = (float)Math.PI * 2;
		int segments = 48;
		float step = tau / segments;

		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin(i * step) * radius;
			float cos = (float)Math.cos(i * step) * radius;

			float nextsin = (float)Math.sin((i + 1) * step) * radius;
			float nextcos = (float)Math.cos((i + 1) * step) * radius;

			lineRenderer.line(startX + sin, startY, startZ + cos, startX + nextsin, startY, startZ + nextcos);
		}
	}

	public void drawXCircle(float startX, float startY, float startZ, float radius, Color color) {
		lineRenderer.setColor(color);

		float tau = (float)Math.PI * 2;
		int segments = 48;
		float step = tau / segments;

		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin(i * step) * radius;
			float cos = (float)Math.cos(i * step) * radius;

			float nextsin = (float)Math.sin((i + 1) * step) * radius;
			float nextcos = (float)Math.cos((i + 1) * step) * radius;

			lineRenderer.line(startX, startY + cos, startZ + sin, startX, startY + nextcos, startZ + nextsin);
		}
	}

	public void drawYCircle(float startX, float startY, float startZ, float radius, Color color) {
		lineRenderer.setColor(color);

		float tau = (float)Math.PI * 2;
		int segments = 48;
		float step = tau / segments;

		for(int i = 0; i < segments; i++) {
			float sin = (float)Math.sin(i * step) * radius;
			float cos = (float)Math.cos(i * step) * radius;

			float nextsin = (float)Math.sin((i + 1) * step) * radius;
			float nextcos = (float)Math.cos((i + 1) * step) * radius;

			lineRenderer.line(startX + sin, startY + cos, startZ, startX + nextsin, startY + nextcos, startZ);
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
	   clearSelectedMarkers();

	   if(selectedItem != Markers.none) {
           for (TileSelectionInfo info : Editor.selection.tiles) {
               EditorMarker eM = new EditorMarker(selectedItem, info.x, info.y);
               level.editorMarkers.add(eM);
               markWorldAsDirty(info.x, info.y, 4);
		   }
	   }

       history.saveState(level);
   }

   public boolean selectionHasEntityMarker() {
	   for(EditorMarker marker : level.editorMarkers) {
		   if (Editor.selection.tiles.contains(marker.x, marker.y)) return true;
	   }

	   return false;
   }

   public void addEntity(Entity e) {
	   level.entities.add(e);
	   e.init(level, Source.EDITOR);
	   markWorldAsDirty((int)e.x, (int)e.y, 4);
   }

    public void clearSelectedMarkers() {
        if (level.editorMarkers == null || level.editorMarkers.size == 0) {
            return;
        }

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Array<EditorMarker> toDelete = new Array<EditorMarker>();
            for (EditorMarker m : level.editorMarkers) {
                if (m.x == info.x && m.y == info.y) {
                    toDelete.add(m);
                }
            }

            if (toDelete.size > 0) {
                markWorldAsDirty(info.x, info.y, 4);
            }

            for (EditorMarker m : toDelete) {
                level.editorMarkers.removeValue(m, true);
            }
        }
    }

	public void refresh() {
		if (gridMesh != null) {
			gridMesh.dispose();
			gridMesh = null;
		}

		gridMesh = genGrid(level.width, level.height);

		refreshLights();
	}

	public void markWorldAsDirty(int xPos, int yPos, int radius) {
		int startX = (xPos - radius) / 17;
		int startY = (yPos - radius) / 17;
		int endX = (xPos + radius) / 17;
		int endY = (yPos + radius) / 17;

		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				WorldChunk chunk = renderer.GetWorldChunkAt(x * 17, y * 17);

				if(chunk == null) {
					// No chunk here yet, so make one
					chunk = new WorldChunk(renderer);
					chunk.setOffset(x * 17, y * 17);
					chunk.setSize(17, 17);
					renderer.chunks.add(chunk);
				}

				chunk.hasBuilt = false;
			}
		}
	}

	public void refreshLights() {
		level.isDirty = true;

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
	    // Selected tiles.
        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
            }

            Tile.copy(tocopy, t);
            level.setTile(info.x, info.y, t);

            t.eastTex = t.westTex = t.northTex = t.southTex = null;
            t.bottomEastTex = t.bottomWestTex = t.bottomNorthTex = t.bottomSouthTex = null;

            markWorldAsDirty(info.x, info.y, 1);
        }

        if(!paintAdjacent.isChecked()) {
            return;
        }

        // Adjacent tiles.
        for (AdjacentTileSelectionInfo info : Editor.selection.tiles.adjacent) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
                t.blockMotion = true;
                t.renderSolid = true;
                level.setTile(info.x, info.y, t);
            }

            switch (info.dir) {
                case NORTH:
                    t.northTex = tocopy.wallTex;
                    t.northTexAtlas = tocopy.wallTexAtlas;
                    t.bottomNorthTex = tocopy.wallBottomTex;
                    t.bottomNorthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case SOUTH:
                    t.southTex = tocopy.wallTex;
                    t.southTexAtlas = tocopy.wallTexAtlas;
                    t.bottomSouthTex = tocopy.wallBottomTex;
                    t.bottomSouthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case EAST:
                    t.eastTex = tocopy.wallTex;
                    t.eastTexAtlas = tocopy.wallTexAtlas;
                    t.bottomEastTex = tocopy.wallBottomTex;
                    t.bottomEastTexAtlas = tocopy.wallBottomTexAtlas;
                    break;

                case WEST:
                    t.westTex = tocopy.wallTex;
                    t.westTexAtlas = tocopy.wallTexAtlas;
                    t.bottomWestTex = tocopy.wallBottomTex;
                    t.bottomWestTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
            }
        }
	}

	public void paintTile(Tile tocopy) {
        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
                t.blockMotion = true;
                t.renderSolid = true;
                level.setTile(info.x, info.y, t);
            }

            t.wallTexAtlas = tocopy.wallTexAtlas;
            t.wallTex = tocopy.wallTex;
            t.wallBottomTexAtlas = tocopy.wallBottomTexAtlas;
            t.wallBottomTex = tocopy.wallBottomTex;
            t.floorTex = tocopy.floorTex;
            t.floorTexAtlas = tocopy.floorTexAtlas;
            t.ceilTex = tocopy.ceilTex;
            t.ceilTexAtlas = tocopy.ceilTexAtlas;
            t.eastTex = t.westTex = t.northTex = t.southTex = null;
            t.eastTexAtlas = t.westTexAtlas = t.northTexAtlas = t.southTexAtlas = null;
            t.bottomEastTex = t.bottomWestTex = t.bottomNorthTex = t.bottomSouthTex = null;
            t.bottomEastTexAtlas = t.bottomWestTexAtlas = t.bottomNorthTexAtlas = t.bottomSouthTexAtlas = null;

            t.init(Source.EDITOR);

            markWorldAsDirty(info.x, info.y, 1);
		}

        if(!paintAdjacent.isChecked()) {
            return;
        }

        for (AdjacentTileSelectionInfo info : Editor.selection.tiles.adjacent) {
            Tile t = info.tile;
            if (t == null) {
                t = new Tile();
                t.blockMotion = true;
                t.renderSolid = true;
                level.setTile(info.x, info.y, t);
            }

            switch (info.dir) {
                case NORTH:
                    t.northTex = tocopy.wallTex;
                    t.northTexAtlas = tocopy.wallTexAtlas;
                    t.bottomNorthTex = tocopy.wallBottomTex;
                    t.bottomNorthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
                case SOUTH:
                    t.southTex = tocopy.wallTex;
                    t.southTexAtlas = tocopy.wallTexAtlas;
                    t.bottomSouthTex = tocopy.wallBottomTex;
                    t.bottomSouthTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
                case EAST:
                    t.eastTex = tocopy.wallTex;
                    t.eastTexAtlas = tocopy.wallTexAtlas;
                    t.bottomEastTex = tocopy.wallBottomTex;
                    t.bottomEastTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
                case WEST:
                    t.westTex = tocopy.wallTex;
                    t.westTexAtlas = tocopy.wallTexAtlas;
                    t.bottomWestTex = tocopy.wallBottomTex;
                    t.bottomWestTexAtlas = tocopy.wallBottomTexAtlas;
                    break;
            }
        }
	}

	public void clearTiles() {
        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile n = level.getTile(info.x, info.y - 1);
            Tile s = level.getTile(info.x, info.y + 1);
            Tile e = level.getTile(info.x - 1, info.y);
            Tile w = level.getTile(info.x + 1, info.y);

            if(n.blockMotion && s.blockMotion && e.blockMotion && w.blockMotion) {
                level.setTile(info.x, info.y, null);
            }
            else {
                Tile t = Tile.NewSolidTile();
                t.wallTex = (byte)pickedWallTexture;
                t.wallTexAtlas = pickedWallTextureAtlas;
                level.setTile(info.x, info.y, t);
            }

            markWorldAsDirty(info.x, info.y, 1);
		}

		clearSelectedMarkers();
	}

	public void rotateFloorTex(int value) {
    	if(Editor.selection.picked != null) return;

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.floorTexRot += value;
                t.floorTexRot %= 4;
            }

            markWorldAsDirty(info.x, info.y, 1);
		}
	}

	public void rotateAngle() {
        if(Editor.selection.picked != null) return;

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.blockMotion = false;
                t.renderSolid = false;
                t.tileSpaceType = TileSpaceType.values()[(t.tileSpaceType.ordinal() + 1) % TileSpaceType.values().length];
            }

            markWorldAsDirty(info.x, info.y, 1);
		}
	}

    public void rotateCeilTex(int value) {
        if(Editor.selection.picked != null) return;

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.ceilTexRot += value;
                t.ceilTexRot %= 4;
            }

            markWorldAsDirty(info.x, info.y, 1);
        }
    }

	public void moveFloor(int value) {
        if(Editor.selection.picked != null) return;

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.floorHeight += (value * 0.0625f);
            }

            markWorldAsDirty(info.x, info.y, 1);
        }
	}

	public void moveCeiling(int value) {
        if(Editor.selection.picked != null) return;

        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.ceilHeight += (value * 0.0625f);
            }

            markWorldAsDirty(info.x, info.y, 1);
        }
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
		if (Collidor.intersectRayForwardFacingTriangles(camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), camera, GlRenderer.triangleSpatialHash.getAllTriangles(), t_pickerVector, intersectNormal)) {
			t_pickerVector.add(intersectTemp.x * 0.0001f, intersectTemp.y * 0.0001f, intersectTemp.z * 0.0001f);
			pickedSurface.position.set(t_pickerVector);
			pickedSurface.isPicked = true;

			Tile t = level.getTile((int)pickedSurface.position.x, (int)pickedSurface.position.z);

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
				float floorHeightOffset = 0f;
				if(t.data.isWater)
					floorHeightOffset = 0.08f;

				d.setPosition((int)pickedSurface.position.x + 0.5f, t.floorHeight + floorHeightOffset, (int)pickedSurface.position.z + 0.5f);
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
			markWorldAsDirty((int)pickedSurface.position.x, (int)pickedSurface.position.z, 1);
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

		ui.showModal(picker);
	}

	public void fillSurfaceTexture() {
		if(pickedSurface == null)
			return;

		int xPos = (int)pickedSurface.position.x;
		int yPos = (int)pickedSurface.position.z;

		Tile t = level.getTileOrNull(xPos, yPos);
		if(t == null)
			return;

		history.saveState(level);

		if(pickedSurface.tileSurface == TileSurface.Floor) {
			floodFillFloorTexture(xPos, yPos, t.floorTex, t.floorTexAtlas, t.floorHeight);
		}
		else if (pickedSurface.tileSurface == TileSurface.Ceiling) {
			floodFillCeilingTexture(xPos, yPos, t.ceilTex, t.ceilTexAtlas, t.ceilHeight);
		}
		else if (pickedSurface.tileSurface == TileSurface.UpperWall) {
			floodFillWallTexture(xPos, yPos, t.getWallTex(pickedSurface.edge), t.getWallTexAtlas(pickedSurface.edge), null);
		}
		else if (pickedSurface.tileSurface == TileSurface.LowerWall) {
			floodFillWallTexture(xPos, yPos, t.getWallBottomTex(pickedSurface.edge), t.getWallBottomTexAtlas(pickedSurface.edge), null);
		}

		history.saveState(level);
		refreshLights();
	}

	public void floodFillFloorTexture(int x, int y, byte checkTex, String checkAtlas, float lastHeight) {
		Tile t = level.getTileOrNull(x, y);
		if(t != null) {
			if(t.renderSolid) {
				return;
			}
			if(t.floorTex == (byte)pickedFloorTexture && t.floorTexAtlas == pickedFloorTextureAtlas) {
				return;
			}
			if(t.floorHeight != lastHeight || t.floorTex != checkTex || t.floorTexAtlas != checkAtlas) {
				return;
			}

			t.floorTex = (byte)pickedFloorTexture;
			t.floorTexAtlas = pickedFloorTextureAtlas;

			floodFillFloorTexture(x + 1, y, checkTex, checkAtlas, t.floorHeight);
			floodFillFloorTexture(x - 1, y, checkTex, checkAtlas, t.floorHeight);
			floodFillFloorTexture(x, y + 1, checkTex, checkAtlas, t.floorHeight);
			floodFillFloorTexture(x, y - 1, checkTex, checkAtlas, t.floorHeight);
		}
	}

	public void floodFillCeilingTexture(int x, int y, byte checkTex, String checkAtlas, float lastHeight) {
		Tile t = level.getTileOrNull(x, y);
		if(t != null) {
			if(t.renderSolid) {
				return;
			}
			if(t.ceilTex == (byte)pickedCeilingTexture && t.ceilTexAtlas == pickedCeilingTextureAtlas) {
				return;
			}
			if(t.ceilHeight != lastHeight || t.ceilTex != checkTex || t.ceilTexAtlas != checkAtlas) {
				return;
			}

			t.ceilTex = (byte)pickedCeilingTexture;
			t.ceilTexAtlas = pickedCeilingTextureAtlas;

			floodFillCeilingTexture(x + 1, y, checkTex, checkAtlas, t.ceilHeight);
			floodFillCeilingTexture(x - 1, y, checkTex, checkAtlas, t.ceilHeight);
			floodFillCeilingTexture(x, y + 1, checkTex, checkAtlas, t.ceilHeight);
			floodFillCeilingTexture(x, y - 1, checkTex, checkAtlas, t.ceilHeight);
		}
	}

	public void floodFillWallTexture(int x, int y, byte checkTex, String checkAtlas, Tile last) {
		Tile t = level.getTileOrNull(x, y);
		if(t == null) {
			return;
		}

		if(pickedSurface.tileSurface == TileSurface.UpperWall) {
			if (t.getWallTex(pickedSurface.edge) == pickedWallTexture && t.getWallTexAtlas(pickedSurface.edge) == pickedWallTextureAtlas) {
				return;
			}
			if (t.getWallTex(pickedSurface.edge) != checkTex || t.getWallTexAtlas(pickedSurface.edge) != checkAtlas) {
				return;
			}
		}
		else {
			if (t.getWallBottomTex(pickedSurface.edge) == pickedWallBottomTexture && t.getWallBottomTexAtlas(pickedSurface.edge) == pickedWallBottomTextureAtlas) {
				return;
			}
			if (t.getWallBottomTex(pickedSurface.edge) != checkTex || t.getWallBottomTexAtlas(pickedSurface.edge) != checkAtlas) {
				return;
			}
		}

		// Find the open tile we are facing
		Tile adjacent = null;
		if(pickedSurface.edge == TileEdges.North) {
			adjacent = level.getTileOrNull(x, y - 1);
		}
		else if(pickedSurface.edge == TileEdges.South) {
			adjacent = level.getTileOrNull(x, y + 1);
		}
		else if(pickedSurface.edge == TileEdges.East) {
			adjacent = level.getTileOrNull(x - 1, y);
		}
		else if(pickedSurface.edge == TileEdges.West) {
			adjacent = level.getTileOrNull(x + 1, y);
		}
		if(adjacent == null || adjacent.blockMotion) {
			return;
		}

		// Make sure there's a connection
		if(last != null) {
			if (adjacent.ceilHeight <= last.floorHeight || adjacent.floorHeight >= last.ceilHeight) {
				return;
			}
			if(adjacent.ceilHeight == t.ceilHeight && adjacent.floorHeight == t.floorHeight) {
				return;
			}
			if(pickedSurface.tileSurface == TileSurface.LowerWall) {
				// Is there actually even a lower wall?
				if(adjacent.floorHeight >= t.floorHeight) {
					return;
				}
			}
		}

		// Set texture, flood to the next
		int nextXOffset = 0;
		int nextYOffset = 0;

		if(pickedSurface.edge == TileEdges.North || pickedSurface.edge == TileEdges.South) {
			nextXOffset = 1;
		}
		else {
			nextYOffset = 1;
		}

		if(pickedSurface.tileSurface == TileSurface.UpperWall)
			t.setWallTexture(pickedSurface.edge, (byte)pickedWallTexture, pickedWallTextureAtlas);
		else
			t.setBottomWallTexture(pickedSurface.edge, (byte)pickedWallBottomTexture, pickedWallBottomTextureAtlas);

		// Two dimensional, a bit easier than floors or ceilings
		floodFillWallTexture(x + nextXOffset, y + nextYOffset, checkTex, checkAtlas, adjacent);
		floodFillWallTexture(x - nextXOffset, y - nextYOffset, checkTex, checkAtlas, adjacent);
	}

	public void panSurfaceY(float amt) {
		if(pickedSurface.isPicked) {
			Tile t = level.getTileOrNull((int) pickedSurface.position.x, (int) pickedSurface.position.z);
			if(t == null)
				return;

			boolean isUpperWall = pickedSurface.tileSurface == TileSurface.UpperWall;

			if(isUpperWall)
				t.offsetTopWallSurfaces(pickedSurface.edge, amt);
			else
				t.offsetBottomWallSurfaces(pickedSurface.edge, amt);

			markWorldAsDirty((int)pickedSurface.position.x, (int)pickedSurface.position.y, 1);
			history.saveState(level);
		}
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

	public void doCarve() {
        if(Editor.selection.picked != null) return;

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

		Tile selectedTile = Editor.selection.tiles.first();
		t.floorHeight = selectedTile.floorHeight;
		t.ceilHeight = selectedTile.ceilHeight;

		setTile(t);

        // save undo history
        history.saveState(level);
	}

	public void doPaint() {
        if(Editor.selection.picked != null) return;

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

		Tile selectedTile = Editor.selection.tiles.first();
		t.floorHeight = selectedTile.floorHeight;
		t.ceilHeight = selectedTile.ceilHeight;

		paintTile(t);

        // save undo history
        history.saveState(level);
	}

	public void doDelete() {
        if(Editor.selection.picked != null) {
            level.entities.removeValue(Editor.selection.picked, true);
			markWorldAsDirty((int) Editor.selection.picked.x, (int) Editor.selection.picked.y, 4);

            for(Entity selEntity : Editor.selection.selected) {
                level.entities.removeValue(selEntity, true);

				markWorldAsDirty((int)selEntity.x, (int)selEntity.y, 4);
            }

            clearEntitySelection();

            canDelete = false;
        }
        else {
            clearTiles();
        }

		refreshLights();

        // save undo history
        history.saveState(level);
	}

	public void doPick() {
		Tile t = Editor.selection.tiles.first();
		if(t != null) {
			setPickedWallTexture(t.wallTex, t.wallTexAtlas);
			setPickedCeilingTexture(t.ceilTex, t.ceilTexAtlas);
			setPickedFloorTexture(t.floorTex, t.floorTexAtlas);
            setPickedWallBottomTexture(t.wallBottomTex != null ? t .wallBottomTex : t.wallTex, t.wallBottomTexAtlas);
		}
	}

    public void flattenFloor() {
        if(Editor.selection.picked != null) return;

        float matchFloorHeight = Editor.selection.tiles.first().floorHeight;
        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.floorHeight = matchFloorHeight;
                t.slopeNE = t.slopeNW = t.slopeSE = t.slopeSW = 0;
            }

            markWorldAsDirty(info.x, info.y, 1);
        }
    }

    public void flattenCeiling() {
        if(Editor.selection.picked != null) return;

        float matchCeilHeight = Editor.selection.tiles.first().ceilHeight;
        for (TileSelectionInfo info : Editor.selection.tiles) {
            Tile t = info.tile;
            if (t != null) {
                t.ceilHeight = matchCeilHeight;
                t.ceilSlopeNE = t.ceilSlopeNW = t.ceilSlopeSE = t.ceilSlopeSW = 0;
            }

            markWorldAsDirty(info.x, info.y, 1);
        }
    }

    public void toggleSimulation() {
        if(player == null) {
        	Vector3 cameraPosition = cameraController.getPosition();
        	Vector2 cameraRotation = cameraController.getRotation();

            player = new Player();
            player.x = cameraPosition.x - 0.5f;
            player.y = cameraPosition.y - 0.5f;
            player.z = cameraPosition.z;
            player.rot = cameraRotation.x - 3.14159265f;
            player.yrot = cameraRotation.y;

            for(Entity e : level.entities) { e.editorStartPreview(level); }
        } else {
            player = null;

            for(Entity e : level.entities) { e.editorStopPreview(level); }
			Audio.stopLoopingSounds();
        }
    }

    public void viewSelected() {
		cameraController.viewSelected();
	}

	public float getEntityBoundingSphereRadius(Entity entity) {
		if (entity instanceof Light) {
			return ((Light)entity).range;
		}
		else if (entity instanceof DynamicLight) {
			return ((DynamicLight)entity).range;
		}

		return new Vector3(entity.collision.x, entity.collision.z / 2, entity.collision.y).len();
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

		Editor.selection.tiles.height = 1;
		Editor.selection.tiles.width = 1;

		Editor.selection.picked = null;
		Editor.selection.selected.clear();
		controlPoints.clear();
		pickedControlPoint = null;

        ui.showEntityPropertiesMenu(true);
	}

    public void pickEntity(Entity entity) {
        Editor.selection.picked = entity;
        ui.showEntityPropertiesMenu(true);
    }

    public void pickAdditionalEntity(Entity entity) {
        Editor.selection.selected.add(entity);
        ui.showEntityPropertiesMenu(true);
    }

    public Level getLevel() { return level; }

	public Vector3 getIntersection() {
		if(pickedSurface.isPicked)
			return new Vector3(intpos);

		float floorPos = Editor.selection.tiles.first().floorHeight;
		return new Vector3(Editor.selection.tiles.x, floorPos, Editor.selection.tiles.y);
	}

    public MoveMode getMoveMode() {
        return moveMode;
    }

    public Array<CollisionTriangle> GetCollisionTriangles() {
        return GlRenderer.triangleSpatialHash.getAllTriangles();
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
        if(Editor.selection.picked == null) return;
        this.dragMode = dragMode;

        if(dragMode == DragMode.Y) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane = new Plane(vertDir, -len);
        }
        if(dragMode == DragMode.X) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane = new Plane(vertDir, -len);
        }
        if(dragMode == DragMode.Z) {
            Vector3 vertDir = new Vector3(camera.direction);
            vertDir.y = 0;

            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
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

	public void moveTiles(int moveX, int moveY, float moveZ) {
		// Move Tiles
		if(selected) {
		    Array<TileSelectionInfo> tilesToMove = new Array<TileSelectionInfo>();
		    for (TileSelectionInfo info : Editor.selection.tiles) {
		        tilesToMove.add(info);
                level.setTile(info.x, info.y, null);
                markWorldAsDirty(info.x, info.y, 1);
            }

            for (TileSelectionInfo info : tilesToMove) {
                Tile t = info.tile;
                int newX = info.x + moveX;
                int newY = info.y + moveY;

                if(moveZ != 0 && t != null) {
                    t.floorHeight += moveZ;
                    t.ceilHeight += moveZ;
                }

                level.setTile(newX, newY, t);
                markWorldAsDirty(newX, newY, 1);
            }

            // Move markers
            for (TileSelectionInfo info : Editor.selection.tiles) {
                for (EditorMarker m : level.editorMarkers) {
                    if (m.x == info.x && m.y == info.y) {
                        m.x += moveX;
                        m.y += moveY;
                    }
                }
            }

			Editor.selection.tiles.x += moveX;
			Editor.selection.tiles.y += moveY;

			controlPoints.clear();
		}

		// Move Entities
		Array<Entity> allSelected = new Array<Entity>();
		if(Editor.selection.picked != null) {
			allSelected.add(Editor.selection.picked);
		}
		allSelected.addAll(Editor.selection.selected);

		for(Entity e : allSelected) {
			e.x += moveX;
			e.y += moveY;
			e.z += moveZ;
			markWorldAsDirty((int)e.x, (int)e.y, 1);
		}

		history.saveState(level);
	}

	private void vizualizePicking() {
		if(pickViz == null)
			pickViz = new SpriteBatch();

		pickViz.begin();
		pickViz.draw(pickerFrameBuffer.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
		pickViz.end();
	}

	public void updateTitle() {
	    String name = file.name();
	    if (file.isDirty()) {
	        name += "*";
        }

	    setTitle(name);
    }

	public void setTitle(String title) {
		Gdx.graphics.setTitle(title + " - DelvEdit - " + Game.VERSION);
	}
}
