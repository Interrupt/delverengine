package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.collision.CollisionTriangle;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.entities.projectiles.Projectile;
import com.interrupt.dungeoneer.entities.triggers.TriggeredMessage;
import com.interrupt.dungeoneer.entities.triggers.TriggeredMusic;
import com.interrupt.dungeoneer.entities.triggers.TriggeredShop;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpFrame;
import com.interrupt.dungeoneer.gfx.animation.lerp3d.LerpedAnimation;
import com.interrupt.dungeoneer.gfx.decals.DDecal;
import com.interrupt.dungeoneer.gfx.drawables.*;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;
import com.interrupt.dungeoneer.gfx.shaders.WaterShaderInfo;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.partitioning.TriangleSpatialHash;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.dungeoneer.ui.EquipLoc;
import com.interrupt.dungeoneer.ui.FontBounds;
import com.interrupt.dungeoneer.ui.Hotbar;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.ShaderManager;
import com.interrupt.managers.TileManager;
import com.noise.PerlinNoise;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GlRenderer {
	protected com.interrupt.dungeoneer.game.Game game;
	public Level loadedLevel;

	public boolean editorIsRendering = false;
	public boolean enableLighting = true;

	public PerspectiveCamera camera;
	public OrthographicCamera camera2D;

	protected Texture flashTex;

	protected Pixmap map;
	protected Pixmap drawnMap;
	protected Texture mapTexture;
	public TextureRegion mapTextureRegion;
	public TextureRegion miniMap;
	public boolean showMap = false;

	public BitmapFont font = null;

	protected TextureAtlas wallTextures;
	protected TextureAtlas spriteTextures;
	protected TextureAtlas entityTextures;
	protected TextureAtlas itemTextures;
	protected TextureAtlas particleTextures;
	protected TextureAtlas fontAtlas;

	public TextureRegion healthBarTextureRegion;
	public TextureRegion flashRegion;

	protected boolean hasInitialized;

	protected float xPos;
	protected float yPos;
	protected float zPos;
	protected float rot;

	protected int x;
	protected int y;
	protected int i;

	// The game can scale the camera's field of view up or down depending on gameplay logic
	private float fieldOfViewMod = 1.0f;

	public static float time = 0;

	public Array<WorldChunk> chunks;

	// dynamic lights!
	public static short MAX_DYNAMIC_LIGHTS = 4;
	public static int usedLights = 0;
	public static DynamicLight[] lights = new DynamicLight[MAX_DYNAMIC_LIGHTS];
	public static float[] lightPositions = new float[MAX_DYNAMIC_LIGHTS * 3];
	public static float[] lightColors = new float[MAX_DYNAMIC_LIGHTS * 4];

	public static float eyeAdaptationAmount = 1f;

	public static ShaderProgram smoothLighting;
	public static ShaderProgram smoothLightingTransparent;
	public static ShaderProgram spriteShader;
	public static ShaderProgram uiShader;
	public static ShaderProgram blurShader;
	public static ShaderProgram waterShader;
	public static ShaderProgram fxaaShader;

	public static ShaderInfo worldShaderInfo;
	public static ShaderInfo modelShaderInfo;
	public static ShaderInfo spriteShaderInfo;
	public static ShaderInfo waterEdgeShaderInfo;
	public static WaterShaderInfo waterShaderInfo;
	public static ShaderInfo fogShaderInfo;

	public SpriteBatch uiBatch;

	public Array<DrawableText> textToRender = new Array<>();
	public SpriteBatch textBatch;

	protected ArrayMap<String, DecalBatch> opaqueSpriteBatches = new ArrayMap<String, DecalBatch>();
	protected ArrayMap<String, DecalBatch> transparentSpriteBatches = new ArrayMap<String, DecalBatch>();

	public static final Color INVBOXSELECTED = new Color(1f,1f,1f,0.6f);
	public static final Color INVBOXEQUIPPED = new Color(1f,0.5f,0.5f,0.6f);
	public static final Color INVBOX = new Color(0.3f,0.3f,0.3f,0.6f);
	public static final Color INVBOXHOVER = new Color(0.6f,0.6f,0.6f,0.6f);
	public static final Color INVBOX_CANT_EQUIP = new Color(0.3f,0.3f,0.3f,0.3f);
	public static final Color INVBOX_CAN_EQUIP = new Color(0.5f,1f,0.5f,0.6f);
	public static final Color INVBOX_CAN_EQUIP_HOVER = new Color(0.5f,1f,0.5f,0.8f);
	public static final Color INVBOX_NOT_AVAILABLE = new Color(1f,0.4f,0.4f,0.8f);

	public static final Color DEATH_COLOR = new Color(0.6f, 0f, 0f, 1f);

	public static Vector3 playerLightColor = new Vector3();

	public final Vector3 upVec = new Vector3(0, 1, 0);

	protected Vector3 tempVector1 = new Vector3();
	protected Vector3 tempLightWorkVector = new Vector3();

	protected Color tempColor = new Color();
	protected Color tempColor1 = new Color();
	protected Color tempColor2 = new Color();
	protected Color crosshairColor = new Color(1f,1f,1f,0.35f);

	protected String keystr = "";
	protected String healthText = "";

	protected Array<DrawableMesh> meshesToRender = new Array<DrawableMesh>();
	protected Array<Entity> decalsToRender = new Array<Entity>();

	protected boolean hasShaders = true;

	public int drawnEntities = 0;
	public int drawnChunks = 0;

	protected int lastDrawnHp = 0;
	protected int lastDrawnMaxHp = 0;

	protected Vector2 cameraBob = new Vector2();
	protected Vector3 forwardDirection = new Vector3();
	protected Vector3 rightDirection = new Vector3();
	protected Vector3 downDirection = new Vector3();

	protected Vector3 handMaxDirection = new Vector3();
	protected Vector3 handMaxDirectionOffhand = new Vector3();
	protected float handMaxLerpTime = 0f;

	protected ShapeRenderer collisionLineRenderer = null;

	Vector3 heldRotation = new Vector3();
	Vector3 heldTransform = new Vector3();
	Vector3 handLagRotation = null;
	Vector3 offhandLagRotation = null;

	Vector3 lastHeldRotation = new Vector3();
	Vector3 headRotation = new Vector3();

	public static Color fogColor = new Color(0f, 0f, 0f, 1f);
	public static float fogStart = 0;
	public static float fogEnd = 10;
	public static float viewDistance = 20f;

	protected Color heldItemColor = new Color(1f, 1f, 1f, 1f);

	public static DrawableMesh skybox = null;

	protected FrameBuffer canvasFrameBuffer = null;
	protected FrameBuffer fxaaFrameBuffer = null;
	protected FrameBuffer blurFrameBuffer1 = null;
	protected FrameBuffer blurFrameBuffer2 = null;
	protected FrameBuffer blurFrameBuffer3 = null;
	protected FrameBuffer blurFrameBufferPingPong = null;

	protected SpriteBatch postProcessBatch = null;

	protected float RADIAN_UNIT = 57.2957795f;

	public com.interrupt.dungeoneer.entities.Camera cutsceneCamera = null;

	public static TriangleSpatialHash triangleSpatialHash = new TriangleSpatialHash(1);
	protected Array<Vector3> spatialWorkerList = new Array<Vector3>();

	// keep track of the currently bound texture, so that we only bind when needed
	protected static Texture boundTexture = null;

	// setup a pool for decals to make accessing them faster
	protected Pool<DDecal> decalPool = new Pool<DDecal>(128) {
		@Override
		protected DDecal newObject() {
			return DDecal.newDecal(1, 1, flashRegion);
		}
	};
	protected Array<DDecal> usedDecals = new Array<DDecal>(256);

	// setup a pool for dynamic lights
	protected static Pool<DynamicLight> lightPool = new Pool<DynamicLight>(16) {
		@Override
		protected DynamicLight newObject() {
			return new DynamicLight();
		}
	};
	protected static Array<DynamicLight> usedLightPool = new Array<DynamicLight>(16);

	protected DynamicLightSorter lightSorter;

	// Color pool used for rendering only
	protected static Pool<Color> colorPool = new Pool<Color>(32) {
		@Override
		protected Color newObject () {
			return new Color();
		}
	};
	protected Array<Color> usedColorPool = new Array<Color>(16);

	private static transient PerlinNoise perlinNoise = new PerlinNoise(1, 1f, 2f, 1f, 1);

	public static final StaticMeshPool staticMeshPool = new StaticMeshPool();

	public boolean needToInit = true;

	public boolean renderingForPicking = false;
	public IntMap<Entity> entitiesForPicking = null;
	public Color entityPickColor = new Color();

	public void initTextures() {

		needToInit = false;

		// load and cache all the sprite atlases
		TextureAtlas[] atlases = Game.getModManager().getTextureAtlases("spritesheets.dat");
		for(TextureAtlas atlas : atlases) {
			TextureAtlas.cacheAtlas(atlas, atlas.name);
		}

		// load and cache all the wall texture atlases
		TextureAtlas[] wallAtlases = Game.getModManager().getTextureAtlases("walltextures.dat");
		for(TextureAtlas atlas : wallAtlases) {
			atlas.isRepeatingAtlas = true;
			TextureAtlas.cacheRepeatingAtlas(atlas, atlas.name);
		}

		// cache some basic atlases
		wallTextures = TextureAtlas.getCachedRegion(ArtType.texture.toString());
		spriteTextures = TextureAtlas.getCachedRegion(ArtType.sprite.toString());
		itemTextures = TextureAtlas.getCachedRegion(ArtType.item.toString());
		entityTextures = TextureAtlas.getCachedRegion(ArtType.entity.toString());
		particleTextures = TextureAtlas.getCachedRegion(ArtType.particle.toString());
		fontAtlas = TextureAtlas.getCachedRegion("font");

		// Corona Sprite
		t_fogSprite = new FogSprite();

		// Load the health bar UI
		Texture healthBarTexture = Art.loadTexture("ui/healthbar.png");
		healthBarTextureRegion = new TextureRegion(healthBarTexture);

		mapTexture = null;

		// Make a white texture to use for the screen flash
		Pixmap flashPixmap = new Pixmap(2,2,Format.RGBA8888);
		flashPixmap.setColor(Color.WHITE);
		flashPixmap.fill();

		if(flashTex != null) {
			flashTex.dispose();
		}

		flashTex = new Texture(flashPixmap);
		flashRegion = new TextureRegion(flashTex,0,0,2,2);

		// reset the skin when needed
		UiSkin.clearCache();
		font = UiSkin.getFont();

		// Reset UI and inventory
		if(Game.instance != null && Game.instance.player != null) {
			if(Game.hudManager.quickSlots != null)
				Game.hudManager.quickSlots.refresh();

			Game.instance.player.resetInventoryDrawables();
		}
	}

	protected TextureAtlas flattenWallTextures(TextureAtlas fromSpriteAtlas) {
		return null;
	}

	public static String getShaderPrefix() {
		if(Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS)
			return "android/";
		else
			return "";
	}

	public void initShaders() {
        String shaderPrefix = getShaderPrefix();

		ShaderManager sm = Game.getModManager().loadShaderManager();
		ShaderManager.setSingleton(sm);

        opaqueSpriteBatches.clear();
        transparentSpriteBatches.clear();

        smoothLighting = sm.loadShader(shaderPrefix, "main.vert", "main.frag");
        smoothLightingTransparent = sm.loadShader(shaderPrefix, "main.vert", "main-transparent.frag");
        spriteShader = sm.loadShader(shaderPrefix, "sprite.vert", "sprite.frag");
        waterShader = sm.loadShader(shaderPrefix, "water.vert", "water.frag");
        uiShader = sm.loadShader(shaderPrefix, "ui.vert", "ui.frag");
        uiShader.pedantic = false;

		blurShader = sm.loadShader(shaderPrefix, "blur.vert", "blur.frag");
		fxaaShader = sm.loadShader(shaderPrefix, "fxaa.vert", "fxaa.frag");

        worldShaderInfo = new ShaderInfo(smoothLighting);
        spriteShaderInfo = new ShaderInfo(spriteShader);
        waterShaderInfo = new WaterShaderInfo(waterShader);
        modelShaderInfo = new ShaderInfo(sm.loadShader(shaderPrefix, "main-dynamic.vert", "main.frag"));
        waterEdgeShaderInfo = new ShaderInfo(sm.loadShader(shaderPrefix, "water-edges.vert", "water-edges.frag"));
        fogShaderInfo = new ShaderInfo(sm.loadShader(shaderPrefix, "fog.vert", "fog.frag"));
	}

	public void init() {

		editorIsRendering = false;
		enableLighting = true;

		if(!GameApplication.editorRunning) {
			Art.KillCache();
		}

		hasInitialized = true;

		Gdx.app.log("DelverLifeCycle", "Initializing Renderer");

		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClearStencil(0);

		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glDepthFunc(GL20.GL_LESS);

		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl20.glCullFace(GL20.GL_BACK);
		Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);

		Gdx.app.log("DelverLifeCycle", " Textures");
		initTextures();
		Gdx.app.log("DelverLifeCycle", " Shaders");
		initShaders();
		Gdx.app.log("DelverLifeCycle", " Lights");
		initLights();

		uiBatch = new SpriteBatch();
		uiBatch.setShader(uiShader);

		textBatch = new SpriteBatch();

		postProcessBatch = new SpriteBatch();
		postProcessBatch.disableBlending();

		Game.ui = new Stage(new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

		if(GameManager.gameHasStarted) {
			Gdx.app.log("DelverLifeCycle", " Hud");
			initHud();
		}

		handLagRotation = null;
		offhandLagRotation = null;
	}

	public void initHud() {
		Gdx.app.log("DelverLifeCycle", "Initializing HUD");

		Game.ui.clear();
		Game.hudManager.quickSlots.init();
		Game.hudManager.backpack.init();
		Game.hud.init(itemTextures.getSpriteRegions());
	}

	public void render(Game game) {

		time += Gdx.graphics.getDeltaTime() * game.GetGameTimeScale();

		boolean inCutscene = cutsceneCamera != null && cutsceneCamera.isActive;

		this.game = game;
		if(game != null) {
			loadedLevel = game.level;
		}

		if(loadedLevel.rendererDirty) {
			if(chunks != null) {
				chunks.clear();
				chunks.shrink();
			}
			loadedLevel.rendererDirty = false;

			// Update fog and sky settings
			fogColor.set(loadedLevel.fogColor);
			fogStart = loadedLevel.fogStart;
			fogEnd = loadedLevel.fogEnd;
			viewDistance = loadedLevel.viewDistance;
			skybox = loadedLevel.skybox;
		}

		clearBoundTexture();

		// Update camera
		xPos = game.player.x;
		yPos = game.player.y;
		zPos = game.player.z + game.player.getStepUpValue() + game.player.eyeHeight;

		if(Options.instance.headBobEnabled) {
		    zPos += game.player.headbob;
        }

		rot = game.player.rot;

		camera.far = viewDistance;
		camera.up.set(0, 1, 0);
		camera.fieldOfView = Options.instance.fieldOfView * fieldOfViewMod;

		if(!inCutscene) {
			camera.position.x = xPos;
			camera.position.y = zPos;
			camera.position.z = yPos;
			camera.direction.set(0, 0, -1);


			if (Options.instance.headBobEnabled) {
				camera.rotate(Vector3.Z, Math.min(1f, Math.max(game.player.strafeCameraAngleMod, -1f)) * 5f);
			}

			float headRoll = game.player.getHeadRoll();
			if (headRoll != 0) camera.rotate(Vector3.Z, headRoll);

			// is the death animation playing?
			if (game.player.isDead && game.player.dyingAnimation != null) {
				camera.position.add(game.player.dyingAnimation.curTransform);
				camera.rotate(game.player.dyingAnimation.curRotation.x, 1, 0, 0);
				camera.rotate(game.player.dyingAnimation.curRotation.y, 0, 1, 0);
				camera.rotate(game.player.dyingAnimation.curRotation.z, 0, 0, 1);
			}

			camera.rotate(game.player.yrot * RADIAN_UNIT, 1f, 0, 0);
			camera.rotate((rot + 3.14f) * RADIAN_UNIT, 0, 1f, 0);

			rightDirection.set(camera.direction).crs(camera.up).nor();
			rightDirection.scl(-1);

			if (game.player.screenshakeAmount > 0) {
				camera.direction.rotate(rightDirection, game.player.screenshake.x);
				camera.direction.rotate(camera.up, game.player.screenshake.y);
			}

			// attack head bob
			lastHeldRotation.z = Math.max(heldRotation.z - lastHeldRotation.z, 0);
			lastHeldRotation.z *= 0.5f;

			headRotation.add(lastHeldRotation);
			headRotation.scl(1f - 6f * Gdx.graphics.getDeltaTime());
			camera.direction.rotate(rightDirection, headRotation.z * 0.05f);

			lastHeldRotation.set(heldRotation);
		}

		camera.update();

		startFrame();

		if(skybox != null) {
			skybox.x = camera.position.x;
			skybox.z = camera.position.y;
			skybox.y = camera.position.z;
			skybox.scale = 4f;
			skybox.fullbrite = true;
			skybox.update();

			// draw sky
			Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
			renderSkybox(skybox);
			Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		}

		updateDynamicLights(camera);
		updateShaderAttributes();

		// draw the static world
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		renderWorld(loadedLevel);

		// Debug only!
		if(Game.drawDebugBoxes) {
			renderPathfinding();
			renderCollisionBoxes(loadedLevel);
		}

		// draw entities
        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);

		Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
		renderEntities(loadedLevel);

		renderMeshes();
		renderDecals();

		if(!inCutscene) {
			// Draw held items
			Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);

			camera.fieldOfView = 60;
			camera.near = 0.05f;
			camera.update();
			drawHeldItem();
			drawOffhandItem();
			camera.fieldOfView = Options.instance.fieldOfView * fieldOfViewMod;
			camera.near = 0.05f;
			camera.update();

			Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		}

		renderStencilPasses();
		renderTransparentEntities();

		// reset decal pool
		clearDecals();

		// are we near a pit?
		Tile t = game.level.getTileOrNull((int)game.player.x, (int)game.player.y);
		if(t != null && t.data != null && t.data.darkenFloor) {
			if(game.player.z < t.floorHeight) {
				drawFlashOverlay(Color.BLACK);
			}
		}

		// is the death animation playing?
		if (game.player.isDead && game.player.dyingAnimation != null) {
			Game.flashColor.set(DEATH_COLOR);
			Game.flashColor.a = Math.min(game.player.dyingAnimation.timeMod() * game.player.dyingAnimation.timeMod() * 1.5f, 1f);
			drawFlashOverlay(Game.flashColor);
		} else {
			if (Game.flashTimer > 0) {
				Game.flashColor.a = Math.min(Game.flashTimer / Game.flashLength, 1f);
				drawFlashOverlay(Game.flashColor);
			}
		}

		endFrame();

		if(!inCutscene) {
			drawUI();
		}
	}

	public void clearDecals() {
		// reset decal pool
		// IMPORTANT! Call at the end of every frame
		decalPool.freeAll(usedDecals);
		usedDecals.clear();
	}

	private void drawUI() {
		// setup UI
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		uiBatch.setProjectionMatrix(camera2D.combined);

		Game.hudManager.backpack.yOffset = 1.7f;

		if(!Options.instance.hideUI || Game.instance.getShowingMenu()) {
			drawInventory(Game.hudManager.quickSlots);
			drawInventory(Game.hudManager.backpack);
			drawInventory(Game.hud.equipLocations);
		}

		if(loadedLevel != null && (loadedLevel.mapIsDirty || loadedLevel.dirtyMapTiles.size > 0))
			makeMapTextureForLevel(loadedLevel);

		if(miniMap != null && mapTexture != null && !Options.instance.hideUI) {
			miniMap.setRegion(game.player.x - 10, game.player.y - 10, 20, 20);

			float startX = game.player.x - 10 + 0.5f;
			float startY = game.player.y - 10 + 0.5f;
			float endX = startX + 20;
			float endY = startY + 20;

			int mapWidth = mapTexture.getWidth() / 4;
			int mapHeight = mapTexture.getHeight() / 4;

			miniMap.setRegion(startX / mapWidth, startY / mapHeight, endX / mapWidth, endY / mapHeight);

			float mapSize = 175f * Options.instance.uiSize * Game.getDynamicUiScale();

			uiBatch.begin();

			float markerX = camera2D.viewportWidth / 2f - (mapSize * 1.05f) + mapSize * 0.24f;
			float markerY = camera2D.viewportHeight / 2f - (mapSize * 0.75f) - mapSize * 0.05f;

			uiBatch.setColor(1f, 0.8f, 0.6f, 0.75f);
			uiBatch.draw(miniMap, camera2D.viewportWidth / 2f - mapSize * 1.05f, camera2D.viewportHeight / 2f - mapSize - mapSize * 0.05f, mapSize, mapSize);

			uiBatch.setColor(1f,1f,1f,0.4f);
			uiBatch.draw(itemTextures.getSprite(62),
					markerX,
					markerY,
					mapSize / 4f,
					mapSize / 4f,
					mapSize / 2f,
					mapSize / 2f,
					0.75f,
					0.75f,
					game.player.rot * RADIAN_UNIT + 180f);

			uiBatch.end();
		}

		// draw text
		uiBatch.begin();
		uiBatch.setColor(Color.WHITE);

		if(OverlayManager.instance.current() == null || !OverlayManager.instance.current().catchInput) {
			drawCrosshair();

			int textYPos = 0;
			if (Game.messageTimer > 0 && !OverlayManager.instance.shouldPauseGame()) {
				float fontSize = Math.min(camera2D.viewportWidth, camera2D.viewportHeight) / 15;
				float messagefontSize = fontSize * Game.messageScale * 0.35f;

				int yPos = (int) ((Game.message.size * -messagefontSize * 1.2) / 2 + messagefontSize * 1.2);
				for (int i = 0; i < Game.message.size; i++) {
					String messageIterator = Game.message.get(i);
					if (messageIterator != null) {
						if (messageIterator.length() > 0) {
							tempColor1.set(Color.WHITE);
							tempColor2.set(Color.BLACK);

							if (tempColor1.a < 0) tempColor1.a = 0;
							if (tempColor2.a < 0) tempColor2.a = 0;

							float xOffset = messageIterator.length() / 2.0f;
							drawText(messageIterator, -xOffset * messagefontSize + messagefontSize * 0.1f, -yPos, messagefontSize, tempColor2);
							drawText(messageIterator, -xOffset * messagefontSize, -yPos, messagefontSize, tempColor1);
						}
						if (i != Game.message.size - 1)
							yPos += (int) (messagefontSize * 1.4);
					}
				}

				textYPos = yPos + (int)(Game.message.size * messagefontSize * 0.5f);
			}

			if (Game.useMessage.size > 0) {
				if (textYPos != 0) textYPos += (int) (0.25f * Game.GetUiSize());
				float useFontSize = 0.25f * Game.GetUiSize();
				int yPos = textYPos + (int) (useFontSize);
				for (int i = 0; i < Game.useMessage.size; i++) {
					String messageIterator = Game.useMessage.get(i);
					if (messageIterator != null) {
						if (messageIterator.length() > 0) {
							float xOffset = messageIterator.length() / 2.0f;
							drawText(messageIterator, -xOffset * useFontSize + useFontSize * 0.1f, -yPos, useFontSize, Color.BLACK);
							drawText(messageIterator, -xOffset * useFontSize, -yPos, useFontSize, (i == 0 ? Game.useMessageColor : Color.WHITE));
						}
						yPos += (int) (useFontSize * 1.4);
					}
				}
			}
		}

		drawUi();

		uiBatch.end();

		if(!game.gameOver)
		{
			if(!Options.instance.hideUI || Game.instance.getShowingMenu()) Game.ui.draw();

			float uiSize = Game.GetUiSize();

			Item hoverItm = Game.hud.getMouseOverItem();
			if(hoverItm == null) hoverItm = game.player.hovering;

			if(Game.isMobile) {
				hoverItm = Game.hudManager.quickSlots.dragging;
				if(hoverItm == null) hoverItm = Game.hudManager.backpack.dragging;
				if(hoverItm == null) hoverItm = Game.hud.dragging;
			}

			if(hoverItm != null && (OverlayManager.instance.current() == null || !OverlayManager.instance.shouldPauseGame()))
			{
				uiBatch.begin();

				Integer uiTouchPointer = game.input.uiTouchPointer;
				if(uiTouchPointer == null) uiTouchPointer = 0;

				if(Game.isMobile) {
					this.drawTextOnScreen(hoverItm.GetInfoText(), Gdx.input.getX(uiTouchPointer) - Gdx.graphics.getWidth() / 2 + uiSize * 1.25f, -Gdx.input.getY(uiTouchPointer) + Gdx.graphics.getHeight() / 2, uiSize / 5, Color.WHITE, Color.BLACK);
				} else {
					Game.tooltip.show(game.input.getPointerX(uiTouchPointer), -game.input.getPointerY(uiTouchPointer) + Gdx.graphics.getHeight(), hoverItm);
				}

				uiBatch.end();
			}
			else if(Game.tooltip.isShowingItem()) {
				Game.tooltip.hide();
			}

			drawGamepadCursor();
		}
	}

    private void drawCrosshair() {
        if (!shouldDrawCrosshair()) {
            return;
        }

        float crosshairSize = 18f;
        drawText(
            "+",
            -0.5f * crosshairSize,
            -0.65f * crosshairSize,
            crosshairSize,
            crosshairColor
        );
    }

    private boolean shouldDrawCrosshair() {
        Item held = game.player.GetHeldItem();
        if (held == null) {
            return false;
        }

        if (Options.instance.hideUI) {
            return false;
        }

        return Options.instance.alwaysShowCrosshair
            || held.itemType == Item.ItemType.bow
            || held.itemType == Item.ItemType.junk
            || held.itemType == Item.ItemType.wand;
    }

	public void updateShaderAttributes() {
		Color ambientColor = loadedLevel.ambientColor;
		if(loadedLevel instanceof OverworldLevel) {
			ambientColor = ((OverworldLevel)loadedLevel).timeOfDayAmbientLightColor;
		}

		ShaderManager shaderManager = ShaderManager.getShaderManager();
		for(int i = 0; i < shaderManager.loadedShaders.size; i++) {
			ShaderInfo shader = shaderManager.loadedShaders.getValueAt(i);
			shader.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					ambientColor,
					fogColor);
		}
	}

	TextureRegion tempGlyphTextureRegion = new TextureRegion();
	public void renderTextBatches() {
		DecalBatch batch = getDecalBatch("sprite", Entity.BlendMode.OPAQUE);
		if (renderingForPicking)
			batch = getDecalBatch("picking", Entity.BlendMode.OPAQUE);

		// Global text scale modifier. Make a constant?
		float baseTextScale = 0.025f;

		Color tempColor = new Color();

		for (int i = 0; i < textToRender.size; i++) {
			DrawableText dT = textToRender.get(i);

			float curXPos = 0f, curZPos = 0f;

			GlyphLayout bounds = FontBounds.GetBounds(font, dT.text);
			float textWidth = bounds.width * dT.scale * baseTextScale;
			float textHeight = bounds.height * dT.scale * baseTextScale;

			int line = 0;
			float lineWidth = bounds.runs.size == line ? 0 : bounds.runs.get(line++).width * dT.scale * baseTextScale;

			BitmapFont.Glyph glyph = font.getData().getGlyph('X');

			float glyphWidth, glyphHeight = glyph.height * dT.scale * baseTextScale;

			// Draw a decal per-glyph for this text
			for (int ii = 0; ii < dT.text.length(); ii++) {
				char character = dT.text.charAt(ii);

				glyph = font.getData().getGlyph(character);

				if (glyph == null && character == '\n') { // Newline support is in DrawableText, replaces "\\n" with "\n" there to avoid issues with font boundary calculations.
					curXPos = 0;
					curZPos += glyphHeight;

					lineWidth = bounds.runs.size == line ? 0 : bounds.runs.get(line++).width * dT.scale * baseTextScale;
					continue;
				}

				glyphWidth = glyph.width * dT.scale * baseTextScale;

				float tx = dT.parentPosition.x + curXPos;
				float ty = dT.parentPosition.y + 0.001f; // Pull out a bit, to place directly on walls
				float tz = dT.parentPosition.z - curZPos + (glyphHeight * 0.5f); // Place font baseline directly on entity origin

				// Center text on origin
				tx -= textWidth * 0.5f - (textWidth - lineWidth) * dT.alignmentOffset;
				tz += textHeight * 0.5f;

				// Offset a tiny bit, because something was doing that in the glyph rendering code
				tx += 0.1f * dT.scale;

				// Increase the cursor position for next time
				curXPos += glyphWidth;

				// Update the position based on our rotation
				Vector3 rotTemp = new Vector3(tx - dT.parentPosition.x, ty - dT.parentPosition.y, tz - dT.parentPosition.z);
				rotTemp.rotate(Vector3.X, -dT.parentRotation.x);
				rotTemp.rotate(Vector3.Y, -dT.parentRotation.y);
				rotTemp.rotate(Vector3.Z, -dT.parentRotation.z);

				tx = rotTemp.x + dT.parentPosition.x;
				ty = rotTemp.y + dT.parentPosition.y;
				tz = rotTemp.z + dT.parentPosition.z - 0.5f;

				// Have everything, can now set up a sprite decal to draw
				DDecal sd = decalPool.obtain();
				usedDecals.add(sd);

				sd.setBlending(-1, -1);
				if (!renderingForPicking) {
					if (dT.blendMode == Entity.BlendMode.ALPHA) {
						sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					} else if (dT.blendMode == Entity.BlendMode.ADD) {
						sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
					}
				}

				sd.setPosition(tx, tz, ty);
				sd.transformationOffset = Vector2.Zero;

				sd.setRotation(0, 0, 0);
				sd.rotateY(dT.parentRotation.z);
				sd.rotateX(dT.parentRotation.x);
				sd.rotateZ(dT.parentRotation.y);

				if (dT.fullbrite) {
					sd.setColor(dT.color.r, dT.color.g, dT.color.b, 1.0f);
				} else {
					tempColor.set(GetLightmapAt(tx, tz, ty)).mul(dT.color);
					sd.setColor(tempColor.r, tempColor.g, tempColor.b, 1.0f);
				}

				if (renderingForPicking)
					sd.setColor(dT.pickingColor);

				sd.setScale(1f);
				sd.setWidth(glyphWidth);
				sd.setHeight(glyphHeight);

				tempGlyphTextureRegion.setTexture(font.getRegion().getTexture());
				tempGlyphTextureRegion.setRegion(glyph.u, glyph.v2, glyph.u2, glyph.v);
				sd.setTextureRegion(tempGlyphTextureRegion);

				batch.add(sd);
			}
			batch.flush();
		}

		textToRender.clear();
	}

	public void renderOpaqueSprites() {
		try {
			for (int i = 0; i < opaqueSpriteBatches.size; i++) {
				DecalBatch batch = opaqueSpriteBatches.getValueAt(i);
				batch.flush();
			}
		}
		catch(Exception ex) { }
	}

	public void renderTransparentEntities() {
		try {
			Gdx.gl20.glDepthMask(false);
			for (int i = 0; i < transparentSpriteBatches.size; i++) {
				DecalBatch batch = transparentSpriteBatches.getValueAt(i);
				batch.flush();
			}
			Gdx.gl20.glDepthMask(true);
		}
		catch(Exception ex) { }
	}

	public void renderDecals() {
		// draw decal meshes
		if(decalsToRender != null && decalsToRender.size > 0) {
			Gdx.gl20.glCullFace(GL20.GL_FRONT);

			Gdx.gl20.glEnable(GL20.GL_BLEND);
			Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

			Gdx.gl20.glDepthMask(false);

			for(Entity s : decalsToRender) {
				renderDrawableProjectedDecal(s);
			}

			Gdx.gl20.glDepthMask(true);
			Gdx.gl20.glDisable(GL20.GL_BLEND);

			decalsToRender.clear();
			Gdx.gl20.glCullFace(GL20.GL_BACK);
		}
	}

	public void drawFlashOverlay(Color color) {
		drawFlashOverlay(color, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected void drawFlashOverlay(Color color, int srcBlendFunction, int dstBlendFunction) {
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		uiBatch.begin();
		uiBatch.enableBlending();
		uiBatch.setColor(color);
		uiBatch.setBlendFunction(srcBlendFunction, dstBlendFunction);

		uiBatch.draw(flashRegion, -camera2D.viewportWidth / 2,-camera2D.viewportHeight / 2, camera2D.viewportWidth * 2, camera2D.viewportHeight * 2);
		uiBatch.end();
		uiBatch.disableBlending();
		uiBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	}

	public void renderMeshes() {
		// draw meshes
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		if(meshesToRender != null && meshesToRender.size > 0) {
			for(DrawableMesh m : meshesToRender) {
				renderMesh(m, modelShaderInfo);
			}
			meshesToRender.clear();
		}
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
	}

	public void updateDynamicLights(PerspectiveCamera camera) {
		usedLightPool.sort(lightSorter);

		for(i = 0; i < usedLightPool.size && i < MAX_DYNAMIC_LIGHTS; i++) {
			DynamicLight l = usedLightPool.get(i);

			tempLightWorkVector.set(l.position);
			tempLightWorkVector.mul(camera.combined); //convert to final render space

			lightPositions[(i * 3)] = tempLightWorkVector.x;
			lightPositions[(i * 3) + 1] = tempLightWorkVector.y;
			lightPositions[(i * 3) + 2] = tempLightWorkVector.z;

			lightColors[(i * 4)] = l.color.x;
			lightColors[(i * 4) + 1] = l.color.y;
			lightColors[(i * 4) + 2] = l.color.z;
			lightColors[(i * 4) + 3] = l.range * 0.3333f;
		}
	}

	public void renderWorld(Level loadedLvl) {
		Tesselate(loadedLvl);

		Color ambientColor = loadedLvl.ambientColor;
		if(loadedLvl instanceof OverworldLevel) {
			ambientColor = ((OverworldLevel)loadedLvl).timeOfDayAmbientLightColor;
		}

		if(worldShaderInfo != null) {
			worldShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					ambientColor,
					fogColor);
		}

		if(waterShaderInfo != null) {
			waterShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time * 62f,
					ambientColor,
					fogColor);
		}

		if(waterEdgeShaderInfo != null) {
			waterEdgeShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time * 62f,
					ambientColor,
					fogColor);
		}

		if(fogShaderInfo != null) {
			fogShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					ambientColor,
					fogColor);
		}

		drawnEntities = 0;
		drawnChunks = 0;

		Gdx.gl.glDepthFunc(GL20.GL_LESS);

		if(chunks != null) {
			chunks.sort(WorldChunk.sorter);

			// draw static mesh batches
			if(worldShaderInfo != null) worldShaderInfo.begin();
			for(WorldChunk chunk : chunks) {
				chunk.UpdateVisiblity(camera);
				if(chunk.visible) {
					chunk.renderStaticMeshBatch(worldShaderInfo);
				}
			}
			if(worldShaderInfo != null) worldShaderInfo.end();

			// draw walls / floors / ceilings
			for(WorldChunk chunk : chunks) {
				if(chunk.visible) {
					chunk.render();
					drawnChunks++;
				}
			}

			// draw water
			if(waterShaderInfo != null) {
				waterShaderInfo.setScrollSpeed(0f);
			}
			for(WorldChunk chunk : chunks) {
				if(chunk.visible) {
					chunk.renderWater();
				}
			}

			// draw waterfalls
			if(waterShaderInfo != null) {
				waterShaderInfo.setScrollSpeed(0.03f);
			}
			for(WorldChunk chunk : chunks) {
				if(chunk.visible) {
					chunk.renderWaterfall();
				}
			}

            Gdx.gl.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
            Gdx.gl.glPolygonOffset(-1f, -1f);

            // draw water edges
            Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
            if(waterEdgeShaderInfo != null) {
                waterEdgeShaderInfo.setAttribute("u_noise_mod", 1f);
                waterEdgeShaderInfo.setAttribute("u_waveMod", 1f);
            }
            for(WorldChunk chunk : chunks) {
                if(chunk.visible) {
                    chunk.renderWaterEdges();
                }
            }

            // draw waterfall edges
            Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
            if(waterEdgeShaderInfo != null) {
                waterEdgeShaderInfo.setAttribute("u_noise_mod", 4f);
                waterEdgeShaderInfo.setAttribute("u_waveMod", 0f);
            }
            for(WorldChunk chunk : chunks) {
                if(chunk.visible) {
                    chunk.renderWaterfallEdges();
                }
            }

            Gdx.gl.glDisable(GL20.GL_POLYGON_OFFSET_FILL);
		}
	}

	public void renderEntities(Level level) {

		Color ambientColor = level.ambientColor;
		if(level instanceof OverworldLevel) {
			ambientColor = ((OverworldLevel)level).timeOfDayAmbientLightColor;
		}

		if(spriteShaderInfo != null) {
			spriteShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					ambientColor,
					fogColor);
		}

		// render entities
		if(level.entities != null) {
			for(i = 0; i < level.entities.size; i++)
			{
				Render( level.entities.get( i ) );
			}
		}

		if(level.non_collidable_entities != null) {
			for(i = 0; i < level.non_collidable_entities.size; i++)
			{
				Render( level.non_collidable_entities.get( i ) );
			}
		}

		if(chunks != null) {
			for(int ci = 0; ci < chunks.size; ci++) {
				WorldChunk chunk = chunks.get(ci);
				if(chunk != null && chunk.visible) {
					if (chunk.entities != null) {
						for (i = 0; i < chunk.entities.size; i++) {
							Render(chunk.entities.get(i));
						}
					}

					if(chunk.overworldChunk != null) {
						OverworldChunk overworldChunk = chunk.overworldChunk;
						if (overworldChunk.entities != null) {
							for (i = 0; i < overworldChunk.entities.size; i++) {
								Render(overworldChunk.entities.get(i));
							}
						}
						if (overworldChunk.non_collidable_entities != null) {
							for (i = 0; i < overworldChunk.non_collidable_entities.size; i++) {
								Render(overworldChunk.non_collidable_entities.get(i));
							}
						}
					}
				}
			}
		}

		renderOpaqueSprites();
		renderTextBatches();
	}

	public void renderEntitiesForPicking(Level level) {
		Color ambientColor = Color.BLACK;

		renderingForPicking = true;

		if(spriteShaderInfo != null) {
			spriteShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					ambientColor,
					fogColor);
		}

		// render entities
		if(level.entities != null) {
			for(i = 0; i < level.entities.size; i++)
			{
				Entity e = level.entities.get( i );
				resetEntityPickColor(e);
				Render( e );
			}
		}

		if(level.static_entities != null) {
			for(i = 0; i < level.static_entities.size; i++)
			{
				Entity e = level.static_entities.get( i );
				resetEntityPickColor(e);
				Render( e );
			}
		}

		renderOpaqueSprites();
		renderTextBatches();

		renderingForPicking = false;
	}

	private Vector3 t_shadowScale = new Vector3();
	public void addShadowForEntity(Entity e) {
		if(!Options.instance.shadowsEnabled) return;

		if(e.shadowType != Entity.ShadowType.NONE) {
			String meshFile = e.shadowType == Entity.ShadowType.BLOB ? "meshes/shadows/blob.obj" : "meshes/shadows/rectangle.obj";
			t_shadowScale.set(e.collision.x, e.collision.x * 2.5f, e.collision.y);

			float zOffset = -0.026f;

			// if this is a model, we can set the shadow size a bit better
			if (e.drawable != null && e.drawable instanceof DrawableMesh) {
				BoundingBox bbox = ((DrawableMesh) e.drawable).bbox;
				if (bbox != null) {
					bbox.getDimensions(t_shadowScale);
					t_shadowScale.set(t_shadowScale.x * 0.5f * e.scale, 1f * e.scale, t_shadowScale.z * 0.5f * e.scale);
					t_shadowScale.y = Math.max(t_shadowScale.x, t_shadowScale.y);
				}
			}

			Tile t = loadedLevel.getTileOrNull((int)e.x, (int)e.y);
			if(t != null && !t.renderSolid) {
				zOffset = 0f;
				float floorHeight = t.getFloorHeight(e.x, e.y) + 0.5f;
				if(e.z <= floorHeight + 0.025f && floorHeight - e.z < e.collision.z - 0.005f) {
					zOffset = floorHeight - e.z;
				}
				zOffset += 0.0001f;
			}

			float rotation = e.shadowType != Entity.ShadowType.BLOB ? e.getRotation().z : 0;

			DrawableMesh shadowMesh = CachePools.getMesh(meshFile, e.x, e.y, e.z + zOffset, t_shadowScale, rotation);
			if(shadowMesh.isInFrustrum(camera))
				stencilShadows.add(shadowMesh);
		}
	}

	private Vector3 t_lightVector = new Vector3();
	private Color t_haloC = new Color();
	public void addHaloForEntity(Entity e, Entity.HaloMode haloMode) {
		if(!Options.instance.shadowsEnabled || Options.instance.graphicsDetailLevel < 2 || haloMode == Entity.HaloMode.NONE) return;
		if(renderingForPicking) return;

		t_shadowScale.set(0.5f, 0.5f, 0.5f);

		float offset = 0.1f + e.yOffset + (e.collision.z * 0.5f);
		float size = 0.5f * e.scale;
		Color color = e.color;

		float perlinMod = 1f;

		boolean animateHalo = true;
		if(e instanceof Light) {
			offset = ((Light) e).haloOffset;
			size = ((Light) e).haloSize;
			color = ((Light) e).getColor();
			animateHalo = ((Light) e).animateHalo;
		}
		else if(e instanceof com.interrupt.dungeoneer.entities.DynamicLight) {
			offset = ((com.interrupt.dungeoneer.entities.DynamicLight) e).haloOffset;
			size = ((com.interrupt.dungeoneer.entities.DynamicLight) e).haloSize;
			Vector3 vc = ((com.interrupt.dungeoneer.entities.DynamicLight) e).lightColor;
			animateHalo = ((com.interrupt.dungeoneer.entities.DynamicLight) e).lightType == com.interrupt.dungeoneer.entities.DynamicLight.LightType.fire;
			color = t_haloC.set(vc.x, vc.y, vc.z, 1f);
			perlinMod = 0f;
		}
		else if(e instanceof Particle) {
			size *= 0.3f;

			TextureAtlas atlas = TextureAtlas.getCachedRegion(e.spriteAtlas);
			int drawableTex = e.tex;

			// Use the 'Gfx Error' texture region if out of bounds!
			if(atlas == null || drawableTex >= atlas.getSpriteRegions().length) {
				atlas = TextureAtlas.getCachedRegion("sprite");
				drawableTex = 3;
			}

			TextureRegion spriteTex = atlas != null ? atlas.getClippedSprite(drawableTex) : null;
			if(spriteTex == null) return;

			Vector2 clippedSizeMod = atlas.getClippedSizeMod(drawableTex);
			Vector2 drawOffset = atlas.getSpriteOffset(drawableTex);

			offset = (float)((1.0 - drawOffset.y) - 0.5f) * atlas.scale;
			size += (clippedSizeMod.y * atlas.scale) * e.scale;
		}
		else if(e instanceof Projectile) {
			offset -= offset * 1.1f;
		}

		if(Options.instance.graphicsDetailLevel >= 4) {
			size *= 1.15f;
		}

		t_shadowScale.set(size, size, size);

		// scale based on distance to avoid getting clipped off by the camera
		Vector3 distance = t_lightVector.set(e.x, e.z, e.y).sub(camera.position);
		float dist = 1f - distance.len() * 0.08f;
		dist = Interpolation.circleOut.apply(dist);

		float perlinOffsetXY = 0;
		float perlinOffsetZ = 0;
		float perlinScale = 0;

		if(dist > 0) {
			if(haloMode == Entity.HaloMode.STENCIL_ONLY || haloMode == Entity.HaloMode.BOTH) {
				if(animateHalo) {
					perlinScale = (float) perlinNoise.getHeight(e.x + e.hashCode() * 0.1f, time * 2.5f) * 0.4f;
					perlinOffsetXY = (float) perlinNoise.getHeight(e.x + e.hashCode() * 0.1f, time * 3f) * 0.05f;
					perlinOffsetZ = (float) perlinNoise.getHeight(e.x + e.hashCode() * 0.1f, time * 3.5f) * 0.05f;

					perlinScale *= perlinMod;
					perlinOffsetXY *= perlinMod;
					perlinOffsetZ *= perlinMod;
				}

				t_shadowScale.scl(1f + perlinScale);

				DrawableMesh lightMesh = CachePools.getMesh("meshes/shadows/sphere.obj", e.x + perlinOffsetXY, e.y + perlinOffsetXY, e.z + offset + perlinOffsetZ, t_shadowScale.scl(dist), 0f);
				if (Options.instance.graphicsDetailLevel >= 3 && lightMesh.isInFrustrum(camera)) {
					addStencilHalo(color, lightMesh);
				}
			}
		}

		if(Options.instance.graphicsDetailLevel >= 1) {
			if(haloMode == haloMode.CORONA_ONLY || haloMode == haloMode.BOTH) {
				if (e instanceof Light && ((Light) e).corona != null) {
					Light l = (Light) e;
					addHaloSprite(e.x + perlinOffsetXY, e.y + perlinOffsetXY, e.z + offset + perlinOffsetZ - 0.5f, size * (1.5f + perlinScale), l.corona.tex, l.corona.texAtlas, color);
				} else {
					addHaloSprite(e.x + perlinOffsetXY, e.y + perlinOffsetXY, e.z + offset + perlinOffsetZ - 0.5f, size * (1.5f + perlinScale), (byte) 0, "fog_sprites", color);
				}
			}
		}
	}

	FogSprite t_fogSprite = new FogSprite();
	public void addHaloSprite(float x, float y, float z, float size, byte tex, String atlas, Color color) {
		FogSprite fogSprite = t_fogSprite;
		fogSprite.x = x;
		fogSprite.y = y;
		fogSprite.z = z;
		fogSprite.shader = "corona";
		fogSprite.spriteAtlas = atlas;
		fogSprite.tex = tex;
		fogSprite.fullbrite = true;
		fogSprite.tick(loadedLevel, 0f);
		fogSprite.scale = size * 1f;
		fogSprite.updateDrawable();
		fogSprite.drawable.color.set(color).mul(color).mul(1.7f);
		fogSprite.drawable.color.a = 0.75f;
		fogSprite.blendMode = Entity.BlendMode.ADD;
		fogSprite.yOffset = 0;

		if(fogSprite.drawable != null) {
			if(fogSprite.drawable instanceof DrawableSprite) {
				DrawableSprite ds = (DrawableSprite)fogSprite.drawable;
				if(ds.cameraPull == null) {
					ds.cameraPull = 0.05f;
				}
			}
		}

		renderDrawableSprite(x + rightDirection.x * 0.025f, y + rightDirection.z * 0.025f, z + rightDirection.y * 0.025f + 0.005f, true, fogSprite, (DrawableSprite)fogSprite.drawable);
	}

	Color t_stencilColor = new Color();
	public void renderStencilPasses() {

		boolean isUltra = Options.instance.graphicsDetailLevel >= 4;

		// add player shadow
		if(Game.isGameRunning() && !editorIsRendering) {
			Player e = Game.instance.player;
			t_shadowScale.set(e.collision.x, e.collision.x * 2f, e.collision.x);
			DrawableMesh shadowMesh = CachePools.getMesh("meshes/shadows/blob.obj", e.x, e.y, e.z + 0.2f, t_shadowScale, e.getRotation().z);
			stencilShadows.add(shadowMesh);
		}

		// draw shadows / halos!
		if(Options.instance.shadowsEnabled) {
			clearBoundTexture();

			// shadows first
			renderStencilPass(stencilShadows, loadedLevel.shadowColor, GL20.GL_DST_COLOR, GL20.GL_ZERO);

			// now passes for each halo color
			for (Color c : stencilHaloColors.keySet()) {

				// get the halo meshes to render
				Array<DrawableMesh> lightMeshes = stencilHaloColors.get(c);
				if (lightMeshes == null) continue;

				// set the render color
				float colorMul = isUltra ? 0.11f : 0.12f;
				Color color = t_stencilColor.set(c).mul(colorMul);
				color.a = 1.0f;

				// first pass
				renderStencilPass(lightMeshes, color, GL20.GL_ONE, GL20.GL_ONE);

				// Ultra detail level gets another pass for halos
				if (isUltra) {
					for (DrawableMesh m : lightMeshes) {
						m.scaleVector.scl(0.75f);
						m.update();
					}
					renderStencilPass(lightMeshes, color, GL20.GL_ONE, GL20.GL_ONE);
				}

				lightMeshes.clear();
			}
		}

		stencilShadows.clear();
	}

	private HashMap<Color, Array<DrawableMesh>> stencilHaloColors = new HashMap<Color, Array<DrawableMesh>>();
	public void addStencilHalo(Color color, DrawableMesh mesh) {
		if(!stencilHaloColors.containsKey(color)) {
			stencilHaloColors.put(new Color(color), new Array<DrawableMesh>());
		}
		Array<DrawableMesh> halos = stencilHaloColors.get(color);
		halos.add(mesh);
	}

	private Array<DrawableMesh> stencilShadows = new Array<DrawableMesh>();
	private void renderStencilPass(Array<DrawableMesh> meshes, Color color, int srcBlendFunction, int dstBlendFunction) {
		if(meshes == null || meshes.size == 0) return;

		GL20 gl = getGL();

		// setup the stencil buffer
		gl.glEnable(GL20.GL_STENCIL_TEST);
		gl.glColorMask(false, false, false ,false);
		gl.glDepthMask(false);

		// front pass
		gl.glEnable(GL20.GL_CULL_FACE);
		gl.glCullFace(GL20.GL_FRONT);
		gl.glStencilFunc(GL20.GL_ALWAYS, 0, 0xFFFFFFFF);
		gl.glStencilOp(GL20.GL_KEEP, GL20.GL_INCR, GL20.GL_KEEP);

		meshesToRender.addAll(meshes);
		renderMeshes();

		// back pass
		gl.glCullFace(GL20.GL_BACK);
		gl.glStencilFunc(GL20.GL_ALWAYS, 0, 0xFFFFFFFF);
		gl.glStencilOp(GL20.GL_KEEP, GL20.GL_DECR, GL20.GL_KEEP);

		meshesToRender.addAll(meshes);
		renderMeshes();

		// draw the shadows!
		gl.glColorMask(true, true, true,true);
		gl.glStencilFunc(GL20.GL_NOTEQUAL, 0, 0xFFFFFFFF);
		gl.glStencilOp(GL20.GL_REPLACE, GL20.GL_REPLACE, GL20.GL_REPLACE);

		drawFlashOverlay(color, srcBlendFunction, dstBlendFunction);

		// reset back to normal
		gl.glDisable(GL20.GL_STENCIL_TEST);
		gl.glDepthMask(true);

		// free all the cached meshes
		CachePools.freeMeshes();
	}

	public void renderSelectedEntities(Array<Entity> selected, Entity.EditorState editorState, Color color, int srcBlendFunction, int dstBlendFunction) {
		if(selected == null) return;

		GL20 gl = getGL();

		if(spriteShaderInfo != null) {
			spriteShaderInfo.setAttributes(camera.combined,
					0,
					fogStart,
					fogEnd,
					time,
					Color.BLACK,
					fogColor);
		}

		// setup the stencil buffer
		gl.glEnable(GL20.GL_STENCIL_TEST);
		gl.glColorMask(false, false, false ,false);
		gl.glDepthMask(false);

		gl.glDisable(GL20.GL_DEPTH_TEST);
		gl.glDisable(GL20.GL_CULL_FACE);
		gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFFFFFFFF);
		gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);

		for(Entity e : selected) {
			if(e.editorState == editorState) {
				Render(e);
			}
		}

		renderOpaqueSprites();
		renderTextBatches();
		renderMeshes();
		renderTransparentEntities();

		// draw the highlights
		gl.glColorMask(true, true, true,true);
		gl.glStencilFunc(GL20.GL_NOTEQUAL, 0, 0xFFFFFFFF);
		gl.glStencilOp(GL20.GL_REPLACE, GL20.GL_REPLACE, GL20.GL_REPLACE);

		drawFlashOverlay(color, srcBlendFunction, dstBlendFunction);

		// reset back to normal
		gl.glDisable(GL20.GL_STENCIL_TEST);
		gl.glEnable(GL20.GL_DEPTH_TEST);
		gl.glDepthMask(true);

		stencilHaloColors.clear();
	}

	protected void drawUi()
	{
	    if(Options.instance.hideUI && !Game.instance.getShowingMenu()) return;
	    fontAtlas.loadIfNeeded();

		boolean isPoisoned = game.player.isPoisoned();
		float healthSize = Game.GetUiSize() * 0.5f;

		//drawText("Entities rendered: " + drawnEntities, -camera2D.viewportWidth / 2 + healthSize / 2, -camera2D.viewportHeight / 2 + healthSize / 2, healthSize, Color.WHITE);

		if(game.player.statusEffects != null && game.player.statusEffects.size > 0) {
			for(int i = 0; i < game.player.statusEffects.size; i++) {
				StatusEffect effect = game.player.statusEffects.get(i);
				if (effect.timer < 10000){
					int rounded = (int) (effect.timer / 100f + 1);
					if(rounded > 0)
						drawText(rounded + " " + effect.name, -camera2D.viewportWidth / 2 + healthSize / 2, camera2D.viewportHeight / 2f - Game.GetUiSize() * 2.5f - (Game.GetUiSize() * 0.4f) * i, Game.GetUiSize() * 0.175f, Color.WHITE);
				}
				else {
					drawText(effect.name, -camera2D.viewportWidth / 2 + healthSize / 2, camera2D.viewportHeight / 2f - Game.GetUiSize() * 2.5f - (Game.GetUiSize() * 0.4f) * i, Game.GetUiSize() * 0.175f, Color.WHITE);
				}
			}
		}

		float healthUiSize = Game.GetUiSize() * 0.05f;
		float healthBarWidth = 64 * healthUiSize;
		float healthBarHeight = 16 * healthUiSize;
		float padX = 6 * healthUiSize;
		float padY = 3 * healthUiSize;

		float barHeight = 8f * healthUiSize;

		uiBatch.setColor(!isPoisoned ? Color.RED : Color.GREEN);
		uiBatch.draw(fontAtlas.getSprite(55), -camera2D.viewportWidth / 2 + padX * 1.75f, -camera2D.viewportHeight / 2 + padY + barHeight - 4 * healthUiSize, Math.max(0f, (healthBarWidth - padX) * game.player.hp / game.player.getMaxHp()), barHeight);

		// health bar!
		uiBatch.setColor(Color.WHITE);
		uiBatch.draw(healthBarTextureRegion, -camera2D.viewportWidth / 2 + padX, -camera2D.viewportHeight / 2 + padY, healthBarWidth, healthBarHeight);

		// only update the health string when needed
		if(lastDrawnHp != game.player.hp || lastDrawnMaxHp != game.player.getMaxHp()) {
			healthText = game.player.hp + "/" + game.player.getMaxHp();
			lastDrawnHp = game.player.hp;
			lastDrawnMaxHp = game.player.getMaxHp();
		}

		drawText(healthText, -camera2D.viewportWidth / 2f + healthSize * 5.5f - ((healthSize * 0.4f) * (healthText.length() - 1)), -camera2D.viewportHeight / 2f + padY + barHeight * 0.6f, healthUiSize * 3.75f, game.player.hp > game.player.getMaxHp() / 5 ? Color.WHITE : Color.RED);

		if(keystr != null && keystr.length() > 0) {
			drawText(keystr, -camera2D.viewportWidth / 2 + healthSize / 2, -camera2D.viewportHeight / 2 + healthSize * 1.8f, healthSize, Color.WHITE);
		}
	}

	public void Render(Entity s) {
		if(!s.isActive) return;

		// add a light halo if we have one
		Entity.HaloMode haloMode = s.getHaloMode();
		if(haloMode != Entity.HaloMode.NONE) addHaloForEntity(s, haloMode);

		// might have to hide this entity
		if(s.drawDistance != Entity.DrawDistance.FAR)
			updateVisiblityOfEntity(s);

		if((s.hidden || s.outOfDrawDistance) && !editorIsRendering) return;
		s.updateDrawable();

		if(s instanceof Group) { renderGroup((Group)s); return; }
		else if(s.drawable == null || s.drawable.artType == ArtType.hidden) {
			if(editorIsRendering) renderEntitySprite(s.x, s.y, s.z, s);
			return;
		}

		else if(s.drawable instanceof DrawableSprite)
		{
			if(s.drawable.artType == ArtType.entity)
				renderDrawableSprite(s.x, s.y, s.z, true, s, (DrawableSprite)s.drawable);
			else
				renderDrawableSprite(s.x, s.y, s.z, false, s, (DrawableSprite)s.drawable);
		}

		else if(s.drawable instanceof DrawableText) {
			renderDrawableText(s.x, s.y, s.z, s, (DrawableText)s.drawable);
		}
		else if(s.drawable instanceof DrawableBeam) {
			renderDrawableBeam(s.x, s.y, s.z, (DrawableBeam)s.drawable);
		}
		else if(s.drawable instanceof DrawableMesh)
		{
			DrawableMesh mesh = (DrawableMesh)s.drawable;

			if(renderingForPicking) {
				renderMesh(mesh, modelShaderInfo);
			}
			else {
				if (s.editorState != Entity.EditorState.none || (!mesh.isStaticMesh && mesh.isInFrustrum(camera)))
					meshesToRender.add(mesh);
			}
		}
		else if(s.drawable instanceof DrawableProjectedDecal) {
			if(editorIsRendering) renderEntitySprite(s.x, s.y, s.z, s);
			decalsToRender.add(s);
		}

		drawnEntities++;

		// Also draw any attached entities now
		Array<Entity> attached = s.getAttached();
		if(attached != null) {
			for(Entity attachment : attached) {
				Render(attachment);
			}
		}

		// add a shadow if we have one
		if(s.hasShadow() && !renderingForPicking) addShadowForEntity(s);
	}

	public void renderGroup(Group group) {
		for(Entity entity : group.entities) {
			Render(entity);
		}
	}

	protected void drawInventory(ArrayMap<String,EquipLoc> equipLocations) {
		for(EquipLoc equipLoc : equipLocations.values())
		{
			if(equipLoc == null || !equipLoc.visible) continue;

			float uiSize = Game.GetUiSize();

			int x = 0;
			int y = 0;
			uiBatch.begin();

			Item at = game.player.equippedItems.get(equipLoc.equipLoc);
			float xPos = -((uiSize * 1) / 2.0f) + uiSize * x;
			float yPos = camera2D.viewportHeight / 2 - (y + 1) * uiSize;

			float xOffset = equipLoc.xOffset * uiSize;
			float yOffset = equipLoc.yOffset * uiSize;

			// pick a color for this box
			uiBatch.enableBlending();

			if((Game.isMobile || !game.input.caughtCursor) && equipLoc.getMouseOverSlot() != null && equipLoc.getMouseOverSlot() == equipLoc.equipLoc)
				if(Game.dragging != null && Game.dragging.GetEquipLoc().equals(equipLoc.equipLoc))
					uiBatch.setColor(INVBOX_CAN_EQUIP_HOVER);
				else
					uiBatch.setColor(INVBOXHOVER);
			else if(Game.dragging != null) {
				if(Game.dragging.GetEquipLoc().equals(equipLoc.equipLoc))
					uiBatch.setColor(INVBOX_CAN_EQUIP);
				else
					uiBatch.setColor(INVBOX_CANT_EQUIP);
			}
			else {
				if(equipLoc.equipLoc.equals("OFFHAND") && game.player.isHoldingTwoHanded()) uiBatch.setColor(INVBOX_NOT_AVAILABLE);
				else uiBatch.setColor(INVBOX);
			}

			uiBatch.draw(itemTextures.getSprite(equipLoc.bgTex), xPos + xOffset, yPos - yOffset, uiSize, uiSize);

			if(at instanceof ItemStack && !( equipLoc.isDragging.containsKey(equipLoc)) ) {
				ItemStack stack = (ItemStack)at;
				drawText(CachePools.toString(stack.count), xPos + xOffset + uiSize * 0.95f - (CachePools.toString(stack.count).length() * uiSize * 0.2f), yPos - yOffset + uiSize * 0.65f, uiSize * 0.2f, Color.WHITE);
			}

			if(at instanceof Wand && !( equipLoc.isDragging.containsKey(equipLoc))) {
				Wand wand = (Wand)at;
				String chargeText = wand.getChargeText();
				drawText(chargeText, xPos + xOffset + uiSize * 0.95f - (chargeText.length() * uiSize * 0.2f), yPos - yOffset + uiSize * 0.65f, uiSize * 0.2f, Color.WHITE);
			}

			uiBatch.end();
		}
	}

	protected void drawInventory(Hotbar hotbar) {
		if(hotbar == null || !hotbar.visible) return;

		float uiSize = Game.GetUiSize();
		int invLength = hotbar.columns;

		float yOffset = hotbar.yOffset * uiSize;

		uiBatch.begin();
		for(int y = 0; y < hotbar.rows; y++)
		{
			for(int x = 0; x < hotbar.columns; x++)
			{
				final int i = x + (y * hotbar.columns);

				if(i + hotbar.invOffset >= game.player.inventory.size)
					continue;

				Item at = null;
				if(game.player.inventory.size > i + hotbar.invOffset)
					at = game.player.inventory.get(i + hotbar.invOffset);

				float xPos = -((uiSize * invLength) / 2.0f) + uiSize * x;
				float yPos = camera2D.viewportHeight / 2 - (y + 1) * uiSize;

				// pick a color for this box
				uiBatch.enableBlending();

				if(hotbar.gamepadPosition != null && x == hotbar.gamepadPosition)
					uiBatch.setColor(1,0,0,1);
				else if(game.player.selectedBarItem != null && (i + hotbar.invOffset) == game.player.selectedBarItem)
					uiBatch.setColor(INVBOXSELECTED);
				else if(at != null && game.player.equipped(at))
					uiBatch.setColor(INVBOXEQUIPPED);
				else if((Game.isMobile || !game.input.caughtCursor) && hotbar.getMouseOverSlot() != null && hotbar.getMouseOverSlot() == i)
					uiBatch.setColor(INVBOXHOVER);
				else
					uiBatch.setColor(INVBOX);

				uiBatch.draw(itemTextures.getSprite(127), xPos, yPos - yOffset, uiSize, uiSize);

				if(at instanceof ItemStack && !( hotbar.isDragging.containsKey(i + hotbar.invOffset)) ) {
					ItemStack stack = (ItemStack)at;
					drawText(CachePools.toString(stack.count), xPos + uiSize * 0.95f - (CachePools.toString(stack.count).length() * uiSize * 0.2f), yPos - yOffset + uiSize * 0.65f, uiSize * 0.2f, Color.WHITE);
				}

				if(at instanceof Wand && !( hotbar.isDragging.containsKey(i + hotbar.invOffset))) {
					Wand wand = (Wand)at;
					String chargeText = wand.getChargeText();
					drawText(chargeText, xPos + uiSize * 0.95f - (chargeText.length() * uiSize * 0.2f), yPos - yOffset + uiSize * 0.65f, uiSize * 0.2f, Color.WHITE);
				}
			}
		}

		if(hotbar == Game.hudManager.quickSlots) {
			for(int x = 0; x < hotbar.columns; x++) {
				float xPos = -((uiSize * invLength) / 2.0f) + uiSize * x + (uiSize * 0.05f);
				float yPos = camera2D.viewportHeight / 2 - (0 + 1) * uiSize + (uiSize * 0.05f);

				int str_pos = x + 1;
				if(str_pos == 10) str_pos = 0;

				this.drawText(CachePools.toString(str_pos), xPos, yPos, uiSize * 0.2f, Color.GRAY);
			}
		}
		uiBatch.end();
	}

	protected void drawGamepadCursor() {
		if(game.input.getGamepadCursorPosition() != null) {
			float uiSize = Game.GetUiSize();
			Vector2 gamepadCursorPos = game.input.getGamepadCursorPosition();

			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
			uiBatch.begin();
			uiBatch.disableBlending();
			uiBatch.setColor(Color.WHITE);
			uiBatch.draw(itemTextures.getSprite(126), -Gdx.graphics.getWidth() / 2f + gamepadCursorPos.x - uiSize / 2.6f, -Gdx.graphics.getHeight() / 2f + gamepadCursorPos.y - uiSize / 1.25f, uiSize, uiSize);
			uiBatch.end();
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		}
	}

	public void drawText(String text, float sPosX, float sPosY, float size, Color color)
	{
		if(text == null) return;

		font.setColor(color);
		//font.setScale(size * 0.14f);
		//font.drawMultiLine(uiBatch, text, sPosX, sPosY + size * 1.15f);
        //font.setScale(1f);

		font.getData().setScale(size * 0.14f);
		font.draw(uiBatch, text, sPosX, sPosY + size * 1.15f);
		font.getData().setScale(1f);
	}

	// TODO: Fix font drawing
    public void drawItemText(String name, String info, float sPosX, float sPosY, float size, Color color) {
        if(name == null || info == null) return;

        font.getData().setScale(size * 0.14f);

        float nameWidth, nameHeight = 0f;
        float infoWidth, infoHeight = 0f;

        GlyphLayout nameBounds = FontBounds.GetBounds(font, name);

        nameWidth = nameBounds.width;
        nameHeight = nameBounds.height;

        GlyphLayout infoBounds = FontBounds.GetBounds(font, info);

        float padding = size * 0.7f;
        float sWidth = Math.max(nameWidth, infoBounds.width);
        float sHeight = (nameHeight + padding) + infoBounds.height - size;

        //float sWidth = 50f;
        //float sHeight = 50f;

        //if text won't fit on screen horizontally then justify left, right, or centered as needed
        sPosX=sWidth>Gdx.graphics.getWidth()?-sWidth/2:Math.min(Math.max(-Gdx.graphics.getWidth()/2, sPosX), Gdx.graphics.getWidth()/2-sWidth);
        //if text won't fit on screen vertically then align top, bottom, or centered as needed
        sPosY=sHeight>Gdx.graphics.getHeight()?sHeight/2:Math.min(Math.max(-Gdx.graphics.getHeight()/2+sHeight, sPosY), Gdx.graphics.getHeight()/2);

        // Shadow
        font.setColor(Color.BLACK);
        font.draw(uiBatch, name, sPosX, sPosY + (size * 1.1f));
        font.draw(uiBatch, info, sPosX + size * 0.1f, sPosY + (size * 1.1f) - (nameHeight + padding));

        // Item Name
        font.setColor(color);
        font.draw(uiBatch, name, sPosX, sPosY + (size * 1.15f));

        // Info Text
        font.setColor(Color.WHITE);
        font.draw(uiBatch, info, sPosX, sPosY + (size * 1.15f) - (nameHeight + padding));

        font.getData().setScale(1f);
    }

	public void drawTextOnScreen(String text, float sPosX, float sPosY, float size, Color color, Color shadowColor)
	{
		//This method attempts to draw the text at the requested position but will adjust if the text flows off screen

		if(text == null) return;

		font.getData().setScale(size * 0.14f);

        GlyphLayout bounds = FontBounds.GetBounds(font, text);
		float sWidth=bounds.width;
		float sHeight=bounds.height;

		//if text won't fit on screen horizontally then justify left, right, or centered as needed
		sPosX=sWidth>Gdx.graphics.getWidth()?-sWidth/2:Math.min(Math.max(-Gdx.graphics.getWidth()/2, sPosX), Gdx.graphics.getWidth()/2-sWidth);
		//if text won't fit on screen vertically then align top, bottom, or centered as needed
		sPosY=sHeight>Gdx.graphics.getHeight()?sHeight/2:Math.min(Math.max(-Gdx.graphics.getHeight()/2+sHeight, sPosY), Gdx.graphics.getHeight()/2);

		if(shadowColor != null) {
			font.setColor(shadowColor);
			font.draw(uiBatch, text, sPosX, sPosY + size * 1.15f);
		}

		font.setColor(color);
		font.draw(uiBatch, text, sPosX, sPosY + size * 1.15f);

		font.getData().setScale(1f);
	}

	public void drawText(String text, float sPosX, float sPosY, float size, Color color, Color shadowColor)
	{
		if(text == null) return;

		font.getData().setScale(size * 0.14f);

		if(shadowColor != null) {
			font.setColor(shadowColor);
			font.draw(uiBatch, text, sPosX + size * 0.1f, sPosY + size * 1.1f);
		}

		font.setColor(color);
		font.draw(uiBatch, text, sPosX, sPosY + size * 1.15f);
		font.getData().setScale(1f);
	}

	public void drawTextRightJustified(String text, float sPosX, float sPosY, float size, Color color, Color shadowColor)
	{
		if(text == null) return;

		font.getData().setScale(size * 0.14f);
        GlyphLayout bounds = FontBounds.GetBounds(font, text);

		if(shadowColor != null) {
			font.setColor(shadowColor);
			font.draw(uiBatch, text, sPosX-bounds.width + size * 0.1f, sPosY + size * 1.1f);
			//font.draw(uiBatch, text, sPosX, sPosY + size * 1.1f);
		}

		font.setColor(color);
		//font.draw(uiBatch, text, sPosX, sPosY + size * 1.15f);
		font.draw(uiBatch, text, sPosX-bounds.width, sPosY + size * 1.15f);
        font.getData().setScale(1f);
	}

	public void drawCenteredText(String text, float sPosY, float size, Color color, Color shadowColor)
	{
		if(text == null) return;

		font.getData().setScale(size * 0.14f);
        GlyphLayout bounds = FontBounds.GetBounds(font, text);

		if(shadowColor != null) {
			font.setColor(shadowColor);
			font.draw(uiBatch, text, -bounds.width / 2f + size * 0.1f, sPosY + size * 1.1f);
			//font.draw(uiBatch, text, Gdx.graphics.getWidth() / 2f + size * 0.1f, sPosY + size * 1.1f);
		}

		font.setColor(color);
		font.draw(uiBatch, text, -bounds.width / 2f, sPosY + size * 1.15f);
		//font.draw(uiBatch, text, Gdx.graphics.getWidth() / 2f + size * 0.1f, sPosY + size * 1.15f);
        font.getData().setScale(1f);
	}

	public void drawCenteredTextAt(String text, float sPosX, float sPosY, float size, Color color, Color shadowColor)
	{
		if(text == null) return;

		font.getData().setScale(size * 0.14f);
        GlyphLayout bounds = FontBounds.GetBounds(font, text);

		if(shadowColor != null) {
			font.setColor(shadowColor);
			font.draw(uiBatch, text, sPosX + (-bounds.width / 2f + size * 0.1f), sPosY + size * 1.1f);
			//font.draw(uiBatch, text, sPosX + Gdx.graphics.getWidth() / 2f + size * 0.1f, sPosY + size * 1.1f);
		}

		font.setColor(color);
		font.draw(uiBatch, text, sPosX + (-bounds.width / 2f), sPosY + size * 1.15f);
		//font.draw(uiBatch, text, sPosX + Gdx.graphics.getWidth() / 2f + size * 0.1f, sPosY + size * 1.15f);
        font.getData().setScale(1f);
	}

	protected void drawHeldItem() {
		if(handLagRotation == null) handLagRotation = new Vector3(camera.direction);

		Item heldItem = game.player.GetHeldItem();
		if(heldItem == null) return;

		Vector3 rotation = heldRotation.set(0,0,0);
		Vector3 transform = heldTransform.set(0,0,0);
		int texOffset = 0;
		Vector3 offset = game.player.handOffset;

		if(game.player.handAnimation != null) {
			rotation.set(game.player.handAnimation.curRotation);
			transform.set(game.player.handAnimation.curTransform);
			texOffset = game.player.handAnimation.getTexOffset();
		}

		if(heldItem instanceof Bow) {
			transform.add(0, -0.09f, 0);
			rotation.add(0, 0, 0);
		}

		// hand bob
		cameraBob.set(game.player.xa, game.player.ya);
		transform.y -= game.player.headbob * 0.55f;

		// item transition
		if(game.player.doingHeldItemTransition) {
			if(game.player.heldItemTransition > game.player.heldItemTransitionEnd) game.player.doingHeldItemTransition = false;
			else
			{
				transform.y += -0.3f + (game.player.heldItemTransition / game.player.heldItemTransitionEnd) * 0.3f;
			}
		}

		TextureRegion held = heldItem.getHeldInventoryTextureRegion(texOffset);
		TextureAtlas atlas = heldItem.getTextureAtlas();

		// Move the hands down as we look up
		handMaxDirection.set(camera.direction);
		if(Game.instance.player.handAnimateTimer <= 0 && Game.instance.player.attackCharge <= 0) {
			handMaxLerpTime -= 2.5f * Gdx.graphics.getDeltaTime();
			if(handMaxLerpTime < 0) handMaxLerpTime = 0;

			// Give non-ranged items a max angle to look up
			if(!(heldItem instanceof Bow || heldItem instanceof Wand || heldItem instanceof Gun || !Options.instance.handLagEnabled || Game.instance.player.handLagStrength <= 0f)) {
				if (handMaxDirection.y > 0.25f)
					handMaxDirection.y = Interpolation.exp5.apply(0.25f, handMaxDirection.y, handMaxLerpTime);
			}

			handMaxDirection = handMaxDirection.nor();
		}
		else {
			handMaxLerpTime += 0.1f * Gdx.graphics.getDeltaTime();
			if(handMaxLerpTime > 1) handMaxLerpTime = 1f;
		}

        float handLagStrength = Game.instance.player.handLagStrength;
        if (!Options.instance.handLagEnabled) {
            handLagStrength = 0;
        }
		handMaxDirection.lerp(handLagRotation, handLagStrength);
		forwardDirection.set(camera.direction).lerp(handLagRotation, handLagStrength);
		downDirection.set(camera.up).nor();

		handLagRotation.set(handMaxDirection);

		// Handle drawable meshes. First, make sure a model is loaded if this was just in the inventory
		if(heldItem.shouldUseMesh(true))
			heldItem.updateHeldDrawable();

		if(heldItem.drawable instanceof DrawableMesh) {
			heldItem.x = camera.position.x;
			heldItem.z = camera.position.y + 0.5f - heldItem.yOffset; // Bump the held item up a bit, but negate the yOffset
			heldItem.y = camera.position.z;

			// translate forward
			heldItem.x += handMaxDirection.x * (transform.z + offset.z);
			heldItem.z += handMaxDirection.y * (transform.z + offset.z);
			heldItem.y += handMaxDirection.z * (transform.z + offset.z);

			// translate right
			heldItem.x += rightDirection.x * (transform.x + offset.x);
			heldItem.z += rightDirection.y * (transform.x + offset.x);
			heldItem.y += rightDirection.z * (transform.x + offset.x);

			// translate down
			heldItem.x += downDirection.x * (transform.y + offset.y);
			heldItem.z += downDirection.y * (transform.y + offset.y);
			heldItem.y += downDirection.z * (transform.y + offset.y);

			((DrawableMesh)heldItem.drawable).dir.set(camera.direction);
			((DrawableMesh)heldItem.drawable).rotX = rotation.x;
			((DrawableMesh)heldItem.drawable).rotY = rotation.y;
			((DrawableMesh)heldItem.drawable).rotZ = rotation.z;
			heldItem.drawable.update(heldItem);

			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
			renderMesh((DrawableMesh)heldItem.drawable, modelShaderInfo);
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
			return;
		}

		DDecal sd = decalPool.obtain();
		usedDecals.add(sd);

		if(sd.transformationOffset != null) sd.transformationOffset.set(0,0);
		sd.setWidth(1f);
		sd.setHeight(1f);
		sd.setScale(heldItem.scale * atlas.scale * 0.5f);
		sd.setTextureRegion(held);
		sd.setTextureAtlas(atlas);
		sd.setPosition(camera.position.x, camera.position.y, camera.position.z);
		sd.setRotation(camera.direction, camera.up);
		sd.setBlending(-1, -1);

		if(heldItem.drawable != null) {
			if (heldItem.drawable.blendMode == Entity.BlendMode.ALPHA) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			} else if (heldItem.drawable.blendMode == Entity.BlendMode.ADD) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			}
		}

		boolean invisible = (Game.instance != null && Game.instance.player != null && Game.instance.player.invisible);
		if(invisible) {
			sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}

		if( heldItem instanceof Decoration)
			sd.rotateY(150f);
		else
			sd.rotateY(100f);

		// translate forward
		sd.translateX(handMaxDirection.x * (transform.z + offset.z));
		sd.translateY(handMaxDirection.y * (transform.z + offset.z));
		sd.translateZ(handMaxDirection.z * (transform.z + offset.z));

		// translate right
		sd.translateX(rightDirection.x * (transform.x + offset.x));
		sd.translateY(rightDirection.y * (transform.x + offset.x));
		sd.translateZ(rightDirection.z * (transform.x + offset.x));

		// translate down
		sd.translateX(downDirection.x * (transform.y + offset.y));
		sd.translateY(downDirection.y * (transform.y + offset.y));
		sd.translateZ(downDirection.z * (transform.y + offset.y));

		sd.rotateX(rotation.x);
		sd.rotateY(rotation.y);
		sd.rotateZ(rotation.z);

		if(heldItem.fullbrite) {
			sd.setColor(heldItem.color);
		}
		else {
			// set the color from the lightmap at the decal position
			Color lightmapColor = GetLightmapAt(sd.getPosition().x, sd.getPosition().y, sd.getPosition().z);
			heldItemColor.lerp(lightmapColor.r, lightmapColor.g, lightmapColor.b, 1f, 4.5f * Gdx.graphics.getDeltaTime());
			sd.setColor(heldItemColor.r, heldItemColor.g, heldItemColor.b, 1.0f);
		}

		// draw!
		DecalBatch spriteBatch = invisible ? getDecalBatch("invisible", Entity.BlendMode.ALPHA) : getDecalBatch(heldItem.getShader(), heldItem.blendMode);
		spriteBatch.add(sd);
		spriteBatch.flush();

		// Clear bound texture after rendering a DecalBatch
		clearBoundTexture();

		// update item position, for attachment stuff
		heldItem.setPosition(sd.getX() + game.player.xa, sd.getZ() + game.player.ya, sd.getY() + 0.35f + game.player.za);

		heldItem.xa = game.player.xa;
		heldItem.ya = game.player.ya;
		heldItem.za = game.player.za;

		// any lights?
		Entity l = heldItem.getAttached(com.interrupt.dungeoneer.entities.DynamicLight.class);
		if(l != null) addHaloForEntity(l, l.getHaloMode());
	}

	protected void drawOffhandItem() {
	    if(game.player.isHoldingTwoHanded()) return;
		if(offhandLagRotation == null) offhandLagRotation = new Vector3(camera.direction);

		Item heldItem = game.player.GetHeldOffhandItem();
		if(heldItem == null) return;

		Vector3 rotation = heldRotation.set(0,0,0);
		Vector3 transform = heldTransform.set(0,0,0);
		int texOffset = 0;
        Vector3 offset = game.player.offhandOffset;

        if (heldItem instanceof Weapon) {
            Weapon weapon = (Weapon)heldItem;
            String animationName = weapon.attackAnimation;
            LerpedAnimation animation = Game.animationManager.getAnimation(animationName);
            if (animation != null) {
                LerpFrame frame = animation.frames.get(0);
                rotation.set(frame.rotation);
                transform.set(-frame.transform.x, frame.transform.y, frame.transform.z);
                texOffset = animation.curTexOffset;
            }
        }

		// hand bob
		cameraBob.set(game.player.xa, game.player.ya);
		transform.y += game.player.headbob * 0.35f;

		TextureRegion held = heldItem.getHeldInventoryTextureRegion(texOffset);
		TextureAtlas atlas = heldItem.getTextureAtlas();

		// Move the hands down as we look up
		handMaxDirectionOffhand.set(camera.direction);
		if(Game.instance.player.handAnimateTimer <= 0 && Game.instance.player.attackCharge <= 0) {
			handMaxLerpTime -= 2.5f * Gdx.graphics.getDeltaTime();
			if(handMaxLerpTime < 0) handMaxLerpTime = 0;

			// Give non-ranged items a max angle to look up
			if(!(!Options.instance.handLagEnabled || Game.instance.player.handLagStrength <= 0f)) {
				if (handMaxDirectionOffhand.y > 0.25f)
					handMaxDirectionOffhand.y = Interpolation.exp5.apply(0.25f, handMaxDirection.y, handMaxLerpTime);
			}

			handMaxDirectionOffhand = handMaxDirectionOffhand.nor();
		}
		else {
			handMaxLerpTime += 0.1f * Gdx.graphics.getDeltaTime();
			if(handMaxLerpTime > 1) handMaxLerpTime = 1f;
		}

        float offhandLagStrength = Game.instance.player.offhandLagStrength;
        if (!Options.instance.handLagEnabled) {
            offhandLagStrength = 0;
        }
		handMaxDirectionOffhand.lerp(offhandLagRotation, offhandLagStrength);
		forwardDirection.set(camera.direction).lerp(offhandLagRotation, offhandLagStrength);
		downDirection.set(camera.up).nor();

		offhandLagRotation.set(handMaxDirectionOffhand);

		// Handle drawable meshes. First, make sure a model is loaded if this was just in the inventory
		if(heldItem.shouldUseMesh(true))
			heldItem.updateHeldDrawable();

		if(heldItem.drawable instanceof DrawableMesh) {
			heldItem.x = camera.position.x;
			heldItem.z = camera.position.y + 0.5f - heldItem.yOffset; // Bump the held item up a bit, but negate the yOffset
			heldItem.y = camera.position.z;

			// translate forward
			heldItem.x += handMaxDirectionOffhand.x * (transform.z + offset.z);
			heldItem.z += handMaxDirectionOffhand.y * (transform.z + offset.z);
			heldItem.y += handMaxDirectionOffhand.z * (transform.z + offset.z);

			// translate right
			heldItem.x += rightDirection.x * (transform.x + offset.x);
			heldItem.z += rightDirection.y * (transform.x + offset.x);
			heldItem.y += rightDirection.z * (transform.x + offset.x);

			// translate down
			heldItem.x += downDirection.x * (transform.y + offset.y);
			heldItem.z += downDirection.y * (transform.y + offset.y);
			heldItem.y += downDirection.z * (transform.y + offset.y);

			((DrawableMesh)heldItem.drawable).dir.set(camera.direction);
			((DrawableMesh)heldItem.drawable).rotX = rotation.x;
			((DrawableMesh)heldItem.drawable).rotY = rotation.y;
			((DrawableMesh)heldItem.drawable).rotZ = rotation.z;
			heldItem.drawable.update(heldItem);

			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
			renderMesh((DrawableMesh)heldItem.drawable, modelShaderInfo);
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
			return;
		}

		DDecal sd = decalPool.obtain();
		usedDecals.add(sd);

		if(sd.transformationOffset != null) sd.transformationOffset.set(0,0);
		sd.setWidth(-1f);
		sd.setHeight(1f);
		sd.setScale(heldItem.scale * atlas.scale * 0.5f);
		sd.setTextureRegion(held);
		sd.setTextureAtlas(atlas);
		sd.setPosition(camera.position.x, camera.position.y, camera.position.z);
		sd.setRotation(camera.direction, camera.up);
		sd.setBlending(-1, -1);

		if(heldItem.drawable != null) {
			if (heldItem.drawable.blendMode == Entity.BlendMode.ALPHA) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			} else if (heldItem.drawable.blendMode == Entity.BlendMode.ADD) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			}
		}

		boolean invisible = (Game.instance != null && Game.instance.player != null && Game.instance.player.invisible);
		if(invisible) {
			sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}

		if( heldItem instanceof Weapon)
			sd.rotateY(-100f);
		else
			sd.rotateY(-150f);

		// translate forward
		sd.translateX(handMaxDirectionOffhand.x * (transform.z + offset.z));
		sd.translateY(handMaxDirectionOffhand.y * (transform.z + offset.z));
		sd.translateZ(handMaxDirectionOffhand.z * (transform.z + offset.z));

		// translate right
		sd.translateX(rightDirection.x * (transform.x + offset.x));
		sd.translateY(rightDirection.y * (transform.x + offset.x));
		sd.translateZ(rightDirection.z * (transform.x + offset.x));

		// translate down
		sd.translateX(downDirection.x * (transform.y + offset.y));
		sd.translateY(downDirection.y * (transform.y + offset.y));
		sd.translateZ(downDirection.z * (transform.y + offset.y));

		sd.rotateX(rotation.x);
		sd.rotateY(rotation.y);
		sd.rotateZ(rotation.z);

		if(heldItem.fullbrite) {
			sd.setColor(heldItem.color);
		}
		else {
			// set the color from the lightmap at the decal position
			Color lightmapColor = GetLightmapAt(sd.getPosition().x, sd.getPosition().y, sd.getPosition().z);
			heldItemColor.lerp(lightmapColor.r, lightmapColor.g, lightmapColor.b, 1f, 4.5f * Gdx.graphics.getDeltaTime());
			sd.setColor(heldItemColor.r, heldItemColor.g, heldItemColor.b, 1.0f);
		}

		// draw!
		DecalBatch spriteBatch = invisible ? getDecalBatch("invisible", Entity.BlendMode.ALPHA) : getDecalBatch(heldItem.getShader(), heldItem.blendMode);
		spriteBatch.add(sd);
		spriteBatch.flush();

		// Clear bound texture after rendering a DecalBatch
		clearBoundTexture();

		// update item position, for attachment stuff
		heldItem.setPosition(sd.getX() + game.player.xa, sd.getZ() + game.player.ya, sd.getY() + 0.35f + game.player.za);

		heldItem.xa = game.player.xa;
		heldItem.ya = game.player.ya;
		heldItem.za = game.player.za;

		// any lights?
		Entity l = heldItem.getAttached(com.interrupt.dungeoneer.entities.DynamicLight.class);
		if(l != null) addHaloForEntity(l, l.getHaloMode());
	}

	public void renderMesh(DrawableMesh s, ShaderInfo shader)
	{
		if(!s.fullbrite && enableLighting) {
			tempColor.set(GetLightmapAt(s.x, s.z, s.y));
			tempColor.a = 1;
		} else {
			tempColor.set(s.color.r,s.color.g,s.color.b,1f);
		}

		if(renderingForPicking)
			tempColor.set(entityPickColor);

		try {
			s.draw(camera, tempColor, shader, fogStart, fogEnd, fogColor, time);
		}
		catch(Exception ex) {
			Gdx.app.log("RenderMesh", ex.getMessage());
		}
	}

	public void renderSkybox(DrawableMesh s)
	{
		if(!s.fullbrite && enableLighting) {
			tempColor.set(GetLightmapAt(s.x, s.z, s.y));
			tempColor.a = 1;
		} else {
			tempColor.set(1f,1f,1f,1f);
		}

		s.fullbrite = true;

		try {
			s.draw(camera, tempColor, modelShaderInfo, 100, 100, fogColor, time);
		}
		catch(Exception ex) {
			Gdx.app.log("RenderMesh", ex.getMessage());
		}
	}

	DrawableSprite editor_sprite = new DrawableSprite();
	public void renderEntitySprite(float x, float y, float z, Entity s) {
		editor_sprite.tex = 11;
		editor_sprite.atlas = TextureAtlas.getCachedRegion("editor");

		if((s instanceof Light || s instanceof com.interrupt.dungeoneer.entities.DynamicLight)) editor_sprite.tex = 12;
		if(s instanceof ParticleEmitter) editor_sprite.tex = 14;
		if((s instanceof AmbientSound || s instanceof TriggeredMusic)) editor_sprite.tex = 15;
		if((s instanceof TriggeredMessage || s instanceof TriggeredShop)) editor_sprite.tex = 16;
		if((s instanceof ProjectedDecal)) editor_sprite.tex = 13;

		renderDrawableSprite(x, y, z, true, s, editor_sprite);
	}

	public void renderDrawableSprite(float x, float y, float z, boolean doXOffset, Entity entity, DrawableSprite drawable)
	{
		TextureAtlas atlas = drawable.atlas;
        int drawableTex = drawable.tex;

        if(atlas != null && !atlas.didLoad) {
        	atlas.loadIfNeeded();
		}

        // Use the 'Gfx Error' texture region if out of bounds!
		if(atlas == null || drawableTex >= atlas.getSpriteRegions().length) {
            atlas = TextureAtlas.getCachedRegion("sprite");
            drawableTex = 3;
        }

        TextureRegion spriteTex = atlas != null ? atlas.getClippedRegions()[drawableTex] : null;
		if(spriteTex == null) return;

		// check if this is even visible
		tempVector1.set(x + drawable.drawOffset.x, z + drawable.drawOffset.z, y + drawable.drawOffset.y);
		if(entity instanceof Weapon && drawable.rot != 0) {
			tempVector1.y -= atlas.y_offset * 1.6f;
		}
		else if(drawable.billboard) {
			tempVector1.y -= atlas.y_offset;
		}

		if(!camera.frustum.sphereInFrustum(tempVector1, drawable.frustumCullSize)) return;

		DDecal sd = decalPool.obtain();
		sd.setTextureRegion(spriteTex);
		sd.setTextureAtlas(atlas);
		sd.setPosition(x + drawable.drawOffset.x, camera.position.y, y + drawable.drawOffset.y);
		sd.transformationOffset = drawable.transformationOffset != null ? drawable.transformationOffset : Vector2.Zero;

		sd.setBlending(-1, -1);

		if(drawable.billboard) {
			sd.setRotation(tempVector1.set(camera.direction.x, camera.direction.y * 0.1f, camera.direction.z).nor().scl(-1f), upVec);
			sd.setScale(atlas.scale);
		} else {
			sd.setRotation(drawable.dir, Vector3.Y);
			sd.rotateY(drawable.rotation.z);
			sd.rotateX(drawable.rotation.x);
			sd.rotateZ(drawable.rotation.y);

			sd.setScale(atlas.scale);
		}

		if(!enableLighting || drawable.fullbrite || drawable.isStatic || (entity.hidden && editorIsRendering)) {
			sd.setColor(drawable.color.r, drawable.color.g, drawable.color.b, drawable.color.a);
		}
		else {
			Color lightmap = GetLightmapAt(x + drawable.drawOffset.x, z, y + drawable.drawOffset.y);
			if(drawable.colorLastFrame != null) {
				drawable.colorLastFrame.lerp(lightmap.r, lightmap.g, lightmap.b, 1f, 4.5f * Gdx.graphics.getDeltaTime());
				sd.setColor(drawable.colorLastFrame.r, drawable.colorLastFrame.g, drawable.colorLastFrame.b, drawable.color.a);
			}
			else {
				sd.setColor(lightmap.r, lightmap.g, lightmap.b, drawable.color.a);
			}
		}

		Vector2 clippedSizeMod = atlas.getClippedSizeMod()[drawableTex];
		Vector2 offset = atlas.getSpriteOffsets()[drawableTex];

		sd.setWidth(clippedSizeMod.x * drawable.scale * drawable.xScale);
		sd.setHeight(clippedSizeMod.y * drawable.scale);

		if(renderingForPicking) {
			sd.setColor(getPickColorForEntity(entity));
		}

		if(drawable.billboard) {
			if(drawable.scaleWithOffsets)
				sd.setY(-((offset.y * atlas.scale * drawable.scale) + atlas.y_offset) + z + drawable.drawOffset.z);
			else
				sd.setY(-((offset.y) + atlas.y_offset) + z + drawable.drawOffset.z);
		}
		else {
			sd.setY(z + drawable.drawOffset.z);
		}

		if(drawable.rot != 0) {
			sd.rotateZ(drawable.rot);
			if(entity instanceof Weapon) {
				sd.setY(z - atlas.y_offset * 1.6f + drawable.drawOffset.z);
			}
		}

		if(doXOffset) {
			Vector3 cameraDir = tempVector1.set(camera.direction.x, 0, camera.direction.z).nor();
			sd.setZ(y + (offset.x * -cameraDir.x));
			sd.setX(x + (offset.x * cameraDir.z));
		}

		if(drawable.cameraPull != null) {
			Vector3 cameraDir = tempVector1.set(sd.getPosition()).sub(camera.position).nor();
			sd.getPosition().add(cameraDir.scl(-drawable.cameraPull));
		}

		if(!renderingForPicking) {
			if (drawable.blendMode == Entity.BlendMode.ALPHA) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			} else if (drawable.blendMode == Entity.BlendMode.ADD) {
				sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			}
		}

		DecalBatch batch = getDecalBatch(drawable.shader, drawable.blendMode);
		batch.add(sd);

		usedDecals.add(sd);
	}

	public void renderDrawableText(float x, float y, float z, Entity entity, DrawableText drawable)
	{
		drawable.pickingColor.set(entityPickColor);
		textToRender.add(drawable);
	}

	Vector3 t_beam_axis = new Vector3();
	Vector3 beam_tmp = new Vector3();
	Vector3 t_beam_look = new Vector3();
	public void faceBeamDecalToCamera (Decal decal, float beamWidth, Vector3 endA, Vector3 endB, Camera camera) {
		t_beam_axis.set(endB).sub(endA); //the axis direction

		decal.setDimensions(t_beam_axis.len(), beamWidth);

		t_beam_axis.scl(0.5f);
		beam_tmp.set(endA).add(t_beam_axis); //the center point of the laser

		decal.setPosition(beam_tmp.x, beam_tmp.y, beam_tmp.z);

		t_beam_look.set(camera.position).sub(beam_tmp); //Laser center to camera. This is
		//the look vector you'd use if doing spherical billboarding, so it needs
		//to be adjusted.
		beam_tmp.set(t_beam_axis).crs(t_beam_look); //Axis cross look gives you the
		//right vector, the direction the right edge of the sprite should be
		//pointing. This is the same for spherical or cylindrical billboarding.
		t_beam_look.set(beam_tmp).crs(t_beam_axis); //Right cross axis gives you an adjusted
		//look vector that is perpendicular to the axis, i.e. cylindrical billboarding.

		decal.setRotation(t_beam_look.nor(), t_beam_axis); //Note that setRotation method requires
		decal.rotateZ(90f);
		//direction vector to be normalized beforehand.
	}

	Vector3 render_beam_tmp = new Vector3();
	Vector3 render_beam_tmp_2 = new Vector3();
	Vector3 render_beam_tmp_3 = new Vector3();
	public void renderDrawableBeam(float x, float y, float z, DrawableBeam drawable)
	{
		if(drawable.artType == ArtType.hidden) return;
		if(!camera.frustum.sphereInFrustum(tempVector1.set(x, z, y), 0.5f + drawable.size)) return;

		int drawableTex = drawable.tex;
		TextureAtlas atlas = drawable.atlas;

        // Use the 'Gfx Error' texture region if out of bounds!
        if(drawableTex >= atlas.getSpriteRegions().length) {
            atlas = TextureAtlas.getCachedRegion("sprite");
            drawableTex = 3;
        }

		TextureRegion spriteTex = atlas.getSprite(drawableTex);

		if(drawable.beamRenderMode == DrawableBeam.BeamRenderModes.LINE) {
			DDecal sd = decalPool.obtain();
			sd.setTextureRegion(spriteTex);
			sd.setTextureAtlas(atlas);
			sd.setScale(1f);
			sd.transformationOffset = Vector2.Zero;
			sd.setBlending(-1, -1);

			Vector3 lenDir = render_beam_tmp.set(drawable.dir).scl(drawable.size);
			faceBeamDecalToCamera(sd, (drawable.scale * atlas.scale) * 0.25f, render_beam_tmp_2.set(x, z + drawable.yOffset, y), render_beam_tmp_3.set(x, z + drawable.yOffset, y).sub(lenDir), camera);

			if (!enableLighting || drawable.fullbrite) {
				sd.setColor(drawable.color.r, drawable.color.g, drawable.color.b, (drawable.fullbrite && hasShaders) ? 0 : 1);
			} else {
				Color lightmap = GetLightmapAt(x + drawable.drawOffset.x, z, y + drawable.drawOffset.y);
				sd.setColor(lightmap.r, lightmap.g, lightmap.b, (drawable.fullbrite && hasShaders) ? 0 : 1);
			}

			if(!renderingForPicking) {
				if (drawable.blendMode == Entity.BlendMode.ALPHA) {
					sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				} else if (drawable.blendMode == Entity.BlendMode.ADD) {
					sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
				}
			}
			else {
				sd.setColor(entityPickColor);
			}

			DecalBatch spriteBatch = getDecalBatch(drawable.shader, drawable.blendMode);
			spriteBatch.add(sd);
			usedDecals.add(sd);
		}
		else if(drawable.beamRenderMode == DrawableBeam.BeamRenderModes.CROSS) {
			for(int i = 0; i < 2; i++) {
				DDecal sd = decalPool.obtain();
				sd.setTextureRegion(spriteTex);
				sd.setTextureAtlas(atlas);
				sd.setBlending(-1, -1);
				sd.setPosition(x, camera.position.y, y);

				sd.setRotation(drawable.dir, Vector3.Y);
				sd.rotateY(90f);
				sd.rotateX(i * 90f);
				sd.rotateX(drawable.rot);
				sd.setScale(atlas.scale);
				sd.transformationOffset = Vector2.Zero;

				sd.setPosition(x - (drawable.dir.x * drawable.size * 0.5f) * drawable.centerOffset, z - (drawable.dir.y * drawable.size * 0.5f) * drawable.centerOffset + drawable.yOffset, y - (drawable.dir.z * (drawable.size * 0.5f)) * drawable.centerOffset);

				if(drawable.beamCrossOffset != 0f) {
					Matrix4 rotMatrix = new Matrix4(sd.getRotation());

					if(i == 0)
						rotMatrix.rotate(0f, 0f, 1f, 90f);
					else
						rotMatrix.rotate(0f, 1f, 0f, -180f);

					Vector3 beamOffset = CachePools.getVector3().set(0,0,0).rot(rotMatrix);
					sd.translateX(drawable.beamCrossOffset * beamOffset.x);
					sd.translateZ(drawable.beamCrossOffset * beamOffset.z);
					sd.translateY(drawable.beamCrossOffset * beamOffset.y);
				}

				float sizeMod = atlas.spriteSize;
				if(sd.transformationOffset != null)
					sd.transformationOffset.set(0,0);

				sd.setWidth(drawable.size * atlas.scale);
				sd.setHeight(atlas.scale * drawable.scale);

				if(!enableLighting || drawable.fullbrite ) {
					sd.setColor(drawable.color.r, drawable.color.g, drawable.color.b, drawable.color.a);
				}
				else {
					Color lightmap = GetLightmapAt(x + drawable.drawOffset.x, z, y + drawable.drawOffset.y);
					sd.setColor(lightmap.r, lightmap.g, lightmap.b, drawable.color.a);
				}

				if(!renderingForPicking) {
					if (drawable.blendMode == Entity.BlendMode.ALPHA) {
						sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
					} else if (drawable.blendMode == Entity.BlendMode.ADD) {
						sd.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
					}
				}
				else {
					sd.setColor(entityPickColor);
				}

				DecalBatch spriteBatch = getDecalBatch(drawable.shader, drawable.blendMode);
				spriteBatch.add(sd);
				usedDecals.add(sd);
			}
		}
	}

	public void renderDrawableProjectedDecal(Entity e) {
		DrawableProjectedDecal d = (DrawableProjectedDecal) e.drawable;

		TextureAtlas atlas = d.atlas;

		if(d.generatedMesh == null) {
			if(atlas != null && atlas.texture != null) {
				if(!e.isStatic) {
					if(editorIsRendering || DecalManager.addDecal(d))
						d.projectDecal(TriangleArrayToVectorList(GetCollisionTrianglesIn(d.perspective.frustum), true), loadedLevel, atlas.getClippedSprite(e.tex));
					else
						e.isActive = false;
				}
				else {
					d.projectDecal(TriangleArrayToVectorList(GetCollisionTrianglesIn(d.perspective.frustum), true), loadedLevel, atlas.getClippedSprite(e.tex));
				}
			}
		}

		if(atlas != null && atlas.texture != null) bindTexture(atlas.texture);

		d.draw(camera, Color.WHITE, worldShaderInfo, fogStart, fogEnd, fogColor, 1);
	}

	public void setSize(int width, int height) {
		float aspectRatio = (float) width / (float) height;

		if(camera == null) {
			camera = new PerspectiveCamera(Options.instance.fieldOfView * fieldOfViewMod, 1f * aspectRatio, 1f);
			camera.near = 0.1f;
			camera.far = 15f;
		}
		else {
			camera.viewportWidth = 1f * aspectRatio;
			camera.viewportHeight = 1f;
			camera.fieldOfView = Options.instance.fieldOfView * fieldOfViewMod;
		}

		camera.update(true);
		lightSorter = new DynamicLightSorter(camera);
		if(loadedLevel != null) camera.far = viewDistance;

		if(camera2D == null) {
			camera2D = new OrthographicCamera(width, height);
		}
		else {
			camera2D.viewportWidth = width;
			camera2D.viewportHeight = height;
		}
		camera2D.update();

		if(Game.instance != null) Game.camera = camera;

		if(Game.ui != null) {
			Viewport viewport = Game.ui.getViewport();
			viewport.setWorldHeight(height);
			viewport.setWorldWidth(width);
			viewport.update(width, height, true);

			if(Game.instance != null)
				Game.instance.recalculateUiScale();
		}

		CreateFrameBuffers(width, height);

		Gdx.app.log("DelverLifeCycle", "Resized!");
	}

	public void CreateFrameBuffers(int width, int height) {
		try {
			canvasFrameBuffer = CreateFrameBuffer(canvasFrameBuffer, width, height, true, true);
			fxaaFrameBuffer = CreateFrameBuffer(fxaaFrameBuffer, width, height, false, false);
			blurFrameBuffer1 = CreateFrameBuffer(blurFrameBuffer1, width / 4, height / 4, false, false);
			blurFrameBuffer2 = CreateFrameBuffer(blurFrameBuffer2, width / 4, height / 4, false, false);
			blurFrameBuffer3 = CreateFrameBuffer(blurFrameBuffer3, width / 4, height / 4, false, false);
			blurFrameBufferPingPong = CreateFrameBuffer(blurFrameBufferPingPong, width / 4, height / 4, false, false);
		}
		catch(Exception ex) {
			Gdx.app.log("FrameBuffers", ex.getMessage());
		}
	}

	public FrameBuffer CreateFrameBuffer(FrameBuffer previousBuffer, int width, int height, boolean hasDepth, boolean hasStencil) {

		int newWidth = (int)Math.pow(2, Math.ceil(Math.log(width)/Math.log(2)));
		int newHeight = (int)Math.pow(2, Math.ceil(Math.log(height)/Math.log(2)));

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
			if(!Options.instance.enablePostProcessing)
				return null;

			return new FrameBuffer(Format.RGBA8888, newWidth, newHeight, hasDepth, hasStencil);
		}
		catch (Exception ex) {
			Gdx.app.log("FrameBufferResize", ex.getMessage());
			return null;
		}
	}

	public void refreshChunksNear(float xPos, float yPos, float range) {
		int startX = ((int)xPos - (int)range) / 17;
		int startY = ((int)yPos - (int)range) / 17;
		int endX = ((int)xPos + (int)range) / 17;
		int endY = ((int)yPos + (int)range) / 17;

		for(int x = startX; x <= endX; x++) {
			for(int y = startY; y <= endY; y++) {
				WorldChunk chunk = GetWorldChunkAt(x * 17, y * 17);
				if(chunk != null) {
					chunk.refresh();
				}
			}
		}
	}

	public WorldChunk GetWorldChunkAt(int x, int y) {
		if(chunks == null)
			return null;

		for(int i = 0; i < chunks.size; i++) {
			WorldChunk c = chunks.get(i);

			if(x >= c.xOffset && x < c.xOffset + c.width) {
				if(y >= c.yOffset && y < c.yOffset + c.height) {
					return c;
				}
			}
		}

		return null;
	}

	public void Tesselate(Level level)
	{
		if(level.isDirty) {
			if(chunks != null) {
				for (int i = 0; i < chunks.size; i++) {
					WorldChunk c = chunks.get(i);
					c.refresh();
				}
				chunks = null;
			}
		}
		level.isDirty = false;

		if(chunks == null) {
			chunks = new Array<WorldChunk>();
			triangleSpatialHash.Flush();

			int xChunks = (int)Math.ceil(level.width / 17f);
			int yChunks = (int)Math.ceil(level.height / 17f);

			boolean isOverworld = level instanceof OverworldLevel;
			if(!isOverworld) {
				for(int x = 0; x < xChunks; x++) {
					for(int y = 0; y < yChunks; y++) {
						WorldChunk c = new WorldChunk(this);
						c.setOffset(x * 17, y * 17);
						c.setSize(17, 17);

						c.Tesselate(level, this);

						if(!c.Empty()) {
							chunks.add(c);
							c.tesselators.world.addCollisionTriangles(triangleSpatialHash);
							c.tesselators.water.addCollisionTriangles(triangleSpatialHash, CollisionTriangle.TriangleCollisionType.WATER);
							c.tesselators.waterfall.addCollisionTriangles(triangleSpatialHash, CollisionTriangle.TriangleCollisionType.WATER);
						}
					}
				}
			}

			level.cleanupLightCache();
		}
		else {
			// Might need to update these chunks if lights have changed
			for (int i = 0; i < chunks.size; i++) {
				WorldChunk c = chunks.get(i);
				if(c != null && (!c.hasBuilt || c.needsRetessellation)) {
					triangleSpatialHash.dropWorldChunk(c);
					c.Tesselate(loadedLevel, this);
					c.tesselators.world.addCollisionTriangles(triangleSpatialHash);
					c.tesselators.water.addCollisionTriangles(triangleSpatialHash, CollisionTriangle.TriangleCollisionType.WATER);
					c.tesselators.waterfall.addCollisionTriangles(triangleSpatialHash, CollisionTriangle.TriangleCollisionType.WATER);
				}
			}
		}
	}

	public void UpdateDirtyWorldChunks(OverworldLevel olevel) {
		olevel.cleanupLightCache();

		// might need to update already made chunks
		for(WorldChunk wc : chunks) {
			if (wc.overworldChunk != null) {
				if(wc.overworldChunk.isDirty) {
					if(Game.rand.nextFloat() > 0.9f) {
						wc.overworldChunk.isDirty = false;
						wc.overworldChunk.TesselateOnThread(olevel, this);
					}
				}
			}
		}
	}

	public float GetFloorHeightAt(int posx, int posy)
	{
		Tile c = loadedLevel.getTile(posx, posy);
		return c.floorHeight;
	}

	public float GetCeilHeightAt(int posx, int posy)
	{
		Tile c = loadedLevel.getTile(posx, posy);
		return c.ceilHeight;
	}

	Color lightmapTempColor = new Color();
	public Color GetLightmapAt(float posx, float posy, float posz)
	{
		Color t = loadedLevel.getLightColorAt(posx, posz, posy, null, lightmapTempColor);
		if(t == null) return Color.BLACK;
		else return t;
	}

	public Color GetLightmapAt(Level level, float posx, float posy, float posz)
	{
		Color t = level.getLightColorAt(posx, posz, posy, null, lightmapTempColor);
		if(t == null) return Color.BLACK;
		else return t;
	}

	public float GetTexVAt(float posy, TextureAtlas atlas)
	{
		int scale = atlas.rowScale * (int)atlas.scale;
		if(scale == 1) {
			// Easy to figure out what the texture should be if the scale is 1
			return -posy + 0.5f;
		}

		return (-posy + scale - 0.5f) / scale;
	}

	public float GetTexUAt(float posx_start, float posx_end, float posy_start, float posy_end, float uMod, TextureRegion region, TextureAtlas atlas) {
		int textureScale = (int)atlas.scale;
		if(textureScale == 1) {
			// Can skip most of the work here if the scale is easy
			return (region.getU2() - region.getU()) * uMod + region.getU();
		}

		// Need to spread this texture along multiple walls, time to do some work
		if(uMod == 0f)      uMod = 0.00001f;
		else if(uMod == 1f) uMod = 0.99999f;
		float start;
		float end;
		if(posy_start == posy_end) {
			start = posx_start;
			end = posx_end;
		}
		else if(posx_start == posx_end) {
			start = posy_start;
			end = posy_end;
		}
		else {
			start = posx_start - posy_start;
			end = posx_end - posy_end;
		}
		float x = (end - start) * uMod + start;
		float m = (x % textureScale) / (float)textureScale;
		if(end < start) {
			m = 1.0f - m;
		}
		return (region.getU2() - region.getU()) * m + region.getU();
	}

	public GL20 getGL() {
		return Gdx.gl20;
	}

	public TextureRegion[] getWallTextures() {
		return wallTextures.getSpriteRegions();
	}

	public void makeMapTextureForLevel(Level level) {

		Color c = tempColor;

		// draw the base map once
		if(map == null || level.mapIsDirty) {
			map = new Pixmap(level.width*4, level.height*4, Format.RGBA8888);
			drawnMap = new Pixmap(level.width*4, level.height*4, Format.RGBA8888);

			map.setColor(new Color(0f,0f,0f,0f));
			map.fill();

			drawnMap.setColor(new Color(0f,0f,0f,0f));
			drawnMap.fill();

			for(int xx = 0; xx < level.width; xx++) {
				for(int yy = 0; yy < level.height; yy++) {
					drawMapTileBackground(xx, yy, level, drawnMap, c);
				}
			}

			for(int xx = 0; xx < level.width; xx++) {
				for(int yy = 0; yy < level.height; yy++) {
					drawMapTileLines(xx, yy, level, drawnMap, c);

					// make sure the initial seen tiles get drawn next
					Tile t = level.getTile(xx, yy);
					if(t != null && t != Tile.solidWall && !t.IsSolid() && t.seen) {
						level.dirtyMapTiles.add(new Vector2(xx, yy));
					}
				}
			}

			drawMapDoors(level, drawnMap, c);

			if(mapTexture != null) {
				mapTexture.dispose();
			}

			mapTexture = new Texture(GetNextPowerOf2(level.width * 4), GetNextPowerOf2(level.height * 4), map.getFormat());
			mapTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
			mapTextureRegion = new TextureRegion(mapTexture, 0, 0, level.width * 4,level.height * 4);
			miniMap = new TextureRegion(mapTexture, 0, 0, 19,19);
		}

		// draw seen tiles
		map.setBlending(Pixmap.Blending.SourceOver);
		for(Vector2 tileLoc : level.dirtyMapTiles) {
			for(int xx = (int)tileLoc.x - 1; xx <= (int)tileLoc.x + 1; xx++ ) {
				for(int yy = (int)tileLoc.y - 1; yy <= (int)tileLoc.y + 1; yy++ ) {
					updateMapTileVisability(xx, yy, level, map, drawnMap, c);
				}
			}
		}

		map.setColor(Color.GREEN);
		if(level.up != null) {
			int xLoc = (int)level.up.x;
			int yLoc = (int)level.up.y;
			if(level.getTile(xLoc, yLoc).seen)
				map.drawRectangle(xLoc*4, yLoc*4, 4, 4);
		}
		if(level.down != null) {
			int xLoc = (int)level.down.x;
			int yLoc = (int)level.down.y;
			if(level.getTile(xLoc, yLoc).seen)
				map.drawRectangle(xLoc*4, yLoc*4, 4, 4);
		}

		if(mapTextureRegion != null)
			mapTextureRegion.setRegion(0, 0, level.width*4, level.height*4);

		if(mapTexture != null)
			mapTexture.draw(map, 0, 0);

		level.dirtyMapTiles.clear();
		level.mapIsDirty = false;
	}

	public void drawMapDoors(Level level, Pixmap map, Color c) {
		for(Entity e : level.entities) {
			if(e instanceof Door) {
				Door d = (Door)e;
				if(d.doorDirection == Door.DoorDirection.NORTH || d.doorDirection == Door.DoorDirection.SOUTH) {
					int offset = 1;
					if(d.doorDirection == Door.DoorDirection.NORTH) offset = 0;
					map.drawLine((int)((d.startLoc.x) * 4) - offset, (int)((d.startLoc.y - d.collision.y) * 4), (int)((d.startLoc.x) * 4) - offset, (int)((d.startLoc.y + d.collision.y) * 4));
				}
				else {
					int offset = 1;
					if(d.doorDirection == Door.DoorDirection.EAST) offset = 0;
					map.drawLine((int)((d.startLoc.x - d.collision.x) * 4), (int)((d.startLoc.y) * 4) - offset, (int)((d.startLoc.x + d.collision.x) * 4), (int)((d.startLoc.y) * 4) - offset);
				}
			}
		}
	}

	public void drawMapTileBackground(int xx, int yy, Level level, Pixmap map, Color c) {
		Tile t = level.getTile(xx, yy);

		Random r = new Random();
		if(t != null && t != Tile.solidWall && !t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {
			if(t.IsFree()) c.set(Color.GRAY);
			if(t.IsFree() && t.getMinOpenHeight() < 0.6f) c.set(Color.GRAY).mul(0.55f);
			if(t.IsFree() && t.data.isWater) c.set(0, 0.5f, 1.0f, 1);

			Color mapColor = TileManager.instance.getMapColor(t);
			if(t.IsFree() && mapColor != null) c.set(mapColor);

			for (int yyy=0;yyy<=3;yyy++){
				int xStart, xStop;
				switch (t.tileSpaceType){
					case OPEN_SW: xStart=0; xStop=yyy; break;
					case OPEN_NW: xStart=0; xStop=3-yyy; break;
					case OPEN_NE: xStart=yyy; xStop=3; break;
					case OPEN_SE: xStart=4-yyy; xStop=3; break;
					default: xStop=3; xStart=0;
				}

				// draw floor
				for (int xxx=xStart;xxx<=xStop;xxx++){
					float randomColorOffset = r.nextFloat() * 0.08f;
					float rColor = c.r - randomColorOffset;
					float gColor = c.g - randomColorOffset;
					float bColor = c.b - randomColorOffset;
					if(rColor < 0) rColor = 0;
					if(gColor < 0) gColor = 0;
					if(bColor < 0) bColor = 0;

					map.drawPixel(xx*4+xxx,yy*4+yyy,Color.rgba8888(rColor,gColor,bColor,1f));
				}
			}
		}
	}

	public void drawMapTileLines(int xx, int yy, Level level, Pixmap map, Color c) {
		Tile t = level.getTile(xx, yy);
		Color cc = tempColor2;

		if(t != null && t != Tile.solidWall && !t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {

			map.setBlending(Pixmap.Blending.SourceOver);

			if(t.tileSpaceType != Tile.TileSpaceType.EMPTY && t.tileSpaceType != Tile.TileSpaceType.SOLID) {
				int lineStartX = 0;
				int lineStartY = 0;
				int lineEndX = 3;
				int lineEndY = 3;

				switch (t.tileSpaceType) {
					case OPEN_SE:
						lineStartY = 3;
						lineEndY = 0;
						break;
					case OPEN_SW:
						lineStartX = 3;
						lineEndX = 0;
						lineStartY = 3;
						lineEndY = 0;
						break;
					case OPEN_NW:
						lineStartX = 3;
						lineEndX = 0;
						lineStartY = 0;
						lineEndY = 3;
						break;
				}

				map.setColor(Color.BLACK);
				map.drawLine(lineStartX + xx * 4, lineStartY + yy * 4, lineEndX + xx * 4, lineEndY + yy * 4);
			}

			Tile westTile = level.getTileOrNull(xx - 1, yy);
			Tile eastTile = level.getTileOrNull(xx + 1, yy);
			Tile northTile = level.getTileOrNull(xx, yy - 1);
			Tile southTile = level.getTileOrNull(xx, yy + 1);

			if(!t.isWestSolid()) {
				float heightDifference = westTile == null ? 0 : westTile.getMaxFloorHeight() - t.getMaxFloorHeight();
				if (westTile == null || westTile.isEastSolid() || heightDifference >= 0.16f) {
					if(westTile == null || westTile.isEastSolid()) cc.set(Color.BLACK);
					else if(heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
					else if(heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
					else cc.set(0, 0, 0, 0.1f);

					map.setColor(cc);

					for (int yyy = 0; yyy < 4; yyy++) {
						map.drawPixel(xx * 4 - 1, yy * 4 + yyy);
					}
				}
			}
			if(!t.isEastSolid()) {
				float heightDifference = eastTile == null ? 0 : eastTile.getMaxFloorHeight() - t.getMaxFloorHeight();
				if (eastTile == null || eastTile.isWestSolid() || heightDifference >= 0.16f) {
					if(eastTile == null || eastTile.isWestSolid()) cc.set(Color.BLACK);
					else if(heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
					else if(heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
					else cc.set(0, 0, 0, 0.1f);

					map.setColor(cc);

					for (int yyy = 0; yyy < 4; yyy++) {
						map.drawPixel(xx * 4 + 3 + 1, yy * 4 + yyy);
					}
				}
			}
			if(!t.isNorthSolid()) {
				float heightDifference = northTile == null ? 0 : northTile.getMaxFloorHeight() - t.getMaxFloorHeight();
				if (northTile == null || northTile.isSouthSolid() || heightDifference >= 0.16f) {
					if(northTile == null || northTile.isSouthSolid()) cc.set(Color.BLACK);
					else if(heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
					else if(heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
					else cc.set(0, 0, 0, 0.1f);

					map.setColor(cc);

					for (int xxx = 0; xxx < 4; xxx++) {
						map.drawPixel(xx * 4 + xxx, yy * 4 - 1);
					}
				}
			}
			if(!t.isSouthSolid()) {
				float heightDifference = southTile == null ? 0f : southTile.getMaxFloorHeight() - t.getMaxFloorHeight();
				if (southTile == null || southTile.isNorthSolid() || heightDifference >= 0.16f) {
					if(southTile == null || southTile.isNorthSolid()) cc.set(Color.BLACK);
					else if(heightDifference > 0.5f) cc.set(0, 0, 0, 0.5f);
					else if(heightDifference > 0.18f) cc.set(0, 0, 0, 0.25f);
					else cc.set(0, 0, 0, 0.1f);

					map.setColor(cc);

					for (int xxx = 0; xxx < 4; xxx++) {
						map.drawPixel(xx * 4 + xxx, yy * 4 + 3 + 1);
					}
				}
			}
		}
	}

	public void updateMapTileVisability(int xx, int yy, Level level, Pixmap map, Pixmap drawnMap, Color c) {
		Tile t = level.getTile(xx, yy);

		// update the initial tile viz
		if(t != null && t != Tile.solidWall && t.seen) {
			// now make sure nearby walls get drawn too
			Tile westTile = level.getTileOrNull(xx - 1, yy);
			Tile eastTile = level.getTileOrNull(xx + 1, yy);
			Tile northTile = level.getTileOrNull(xx, yy - 1);
			Tile southTile = level.getTileOrNull(xx, yy + 1);

			boolean westSeen = (westTile != null && westTile.seen) || westTile == null;
			boolean eastSeen = (eastTile != null && eastTile.seen) || eastTile == null;
			boolean northSeen = (northTile != null && northTile.seen) || northTile == null;
			boolean southSeen = (southTile != null && southTile.seen) || southTile == null;

			for (int xxx = 0; xxx < 4; xxx++) {
				for (int yyy = 0; yyy < 4; yyy++) {
					c.set(drawnMap.getPixel(xxx + xx * 4, yyy + yy * 4));

					if(!t.IsSolid() && !t.floorAndCeilingAreSameHeight()) {
						if (!westSeen) c.a *= (1 - ((4 - xxx) / 4f)) * 0.75f + 0.1f;
						if (!eastSeen) c.a *= (((4 - xxx) / 4f)) * 0.75f + 0.1f;
						if (!northSeen) c.a *= (1 - ((4 - yyy) / 4f)) * 0.75f + 0.1f;
						if (!southSeen) c.a *= (((4 - yyy) / 4f)) * 0.75f + 0.1f;
					}

					map.setBlending(Pixmap.Blending.None);
					map.setColor(c);
					map.drawPixel(xxx + xx * 4, yyy + yy * 4);
				}
			}
		}
	}

	public static float FastSqrt(float x) {
		return 1f / FastInvSqrt(x);
	}

	public static float FastInvSqrt(float x) {
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x); // store floating-point bits in integer
		i = 0x5f3759d5 - (i >> 1); // initial guess for Newton's method
		x = Float.intBitsToFloat(i); // convert new bits into float
		x = x*(1.5f - xhalf*x*x); // One round of Newton's method
		return x;
	}

	public Array<CollisionTriangle> GetCollisionTrianglesNear(Entity e) {
		return triangleSpatialHash.getTrianglesAt(e.x, e.y, 2f);
	}

	public Array<CollisionTriangle> GetCollisionTrianglesIn(Frustum frustum) {
		return triangleSpatialHash.getTrianglesIn(frustum);
	}

	public Array<CollisionTriangle> GetCollisionTrianglesAlong(Ray ray, float length) {
		return triangleSpatialHash.getTrianglesAlong(ray, length);
	}

	public Array<CollisionTriangle> GetCollisionTrianglesAt(float x, float y, float colSize) {
		return triangleSpatialHash.getTrianglesAt(x, y, colSize);
	}

	public Array<Vector3> TriangleArrayToVectorList(Array<CollisionTriangle> triangles, boolean excludeWaterTriangles) {
		spatialWorkerList.clear();

		for(CollisionTriangle t : triangles) {
			if(excludeWaterTriangles && t.collisionType == CollisionTriangle.TriangleCollisionType.WATER)
				continue;

			spatialWorkerList.add(t.v1);
			spatialWorkerList.add(t.v2);
			spatialWorkerList.add(t.v3);
		}

		return spatialWorkerList;
	}

	private static Vector3 t_bakeWorkVector = new Vector3();
	private static Matrix4 t_bakeWorkMatrix = new Matrix4();
	public static Mesh bakeMesh(Level level, DrawableMesh drbl) {
		short indexOffset = 0;
		int vertexCount = 0;

		if(drbl != null) {
			if(drbl.loadedMesh != null) {
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

				ShortArray indices = new ShortArray(ind.length);
				FloatArray vertices = new FloatArray(verts.length);

				if(ind.length > 0)
					drbl.loadedMesh.getIndices(ind);
				else {
					ind = new short[drbl.loadedMesh.getNumVertices()];

					for(int i = 0; i < drbl.loadedMesh.getNumVertices(); i++) {
						ind[i] = (short)i;
					}
				}

				drbl.loadedMesh.getVertices(verts);

				synchronized (t_mergeMatrix) {
					Matrix4 matrix = t_mergeMatrix.setToTranslation(0, 0, 0);

					Vector3 dirWithoutY = t_bakeWorkVector.set(drbl.dir.x,0,drbl.dir.z);
					matrix.mul(t_bakeWorkMatrix.setToLookAt(dirWithoutY, Vector3.Y));
					matrix.mul(t_bakeWorkMatrix.setToRotation(Vector3.X, drbl.dir.y * -90f));

					matrix.rotate(Vector3.Y, drbl.rotZ);
					matrix.rotate(Vector3.X, drbl.rotX);
					matrix.rotate(Vector3.Z, drbl.rotY);

					matrix.scale(drbl.scale, drbl.scale, drbl.scale);

					// translate the vertices by the model matrix
					Matrix4.mulVec(matrix.val, verts, 0, drbl.loadedMesh.getNumVertices(), attSize);

					for (int i = 0; i < ind.length; i++) {
						indices.add((short) (ind[i] + indexOffset));
					}

					for (int i = 0; i < drbl.loadedMesh.getNumVertices() * attSize; i += attSize) {

						vertices.add(verts[i + positionOffset]);
						vertices.add(verts[i + positionOffset + 1]);
						vertices.add(verts[i + positionOffset + 2]);

						Color c = Color.WHITE;
						Color tempColor = t_staticMeshColor.set(Color.WHITE);

						if (!drbl.fullbrite) {
							c = level.getLightColorAt(verts[i + positionOffset] + drbl.x, verts[i + positionOffset + 2] + drbl.y, verts[i + positionOffset + 1] + drbl.z - 0.5f, null, tempColor);
						}

						vertices.add(c.toFloatBits());
						vertices.add(verts[i + uvOffset]);
						vertices.add(verts[i + uvOffset + 1]);
						vertexCount++;
					}
				}

				if(vertexCount == 0) return null;

				Mesh m = new Mesh(Mesh.VertexDataType.VertexArray, true, vertexCount, indices.size,
						new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

				// pack the array
				indices.shrink();
				vertices.shrink();

				m.setIndices(indices.toArray());
				m.setVertices(vertices.toArray());

				return m;
			}
		}

		return null;
	}

	public static Color t_staticMeshColor = new Color();
	public static Matrix4 t_mergeMatrix = new Matrix4();
	public static Matrix4 t_mergeMatrix_2 = new Matrix4();
	public static Array<Mesh> mergeStaticMeshes(Level level, Array<Entity> entities, List<Vector3> collisionTriangles) {
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

						VertexAttributes attributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
								new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
								new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

						Mesh m;
						synchronized (staticMeshPool) {
							m = staticMeshPool.obtain(attributes, vertexCount, indices.size, level instanceof OverworldLevel);
						}

						// pack the array
						indices.shrink();
						vertices.shrink();

						m.setIndices(indices.toArray());
						m.setVertices(vertices.toArray());

						// reset the vertices
						indices.clear();
						vertices.clear();

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

					synchronized (t_mergeMatrix) {
						Matrix4 matrix = t_mergeMatrix.setToTranslation(e.x, e.z - 0.5f, e.y);
						matrix.scale(drbl.scale, drbl.scale, drbl.scale);
						matrix.mul(t_mergeMatrix_2.setToRotation(Vector3.X, drbl.dir.y * -90f));

						matrix.rotate(Vector3.Y, drbl.rotZ);
						matrix.rotate(Vector3.X, drbl.rotX);
						matrix.rotate(Vector3.Z, drbl.rotY);

						// translate the vertices by the model matrix
						Matrix4.mulVec(matrix.val, verts, 0, drbl.loadedMesh.getNumVertices(), attSize);

						if (ind != null && verts != null) {
							for (int i = 0; i < ind.length; i++) {
								indices.add((short) (ind[i] + indexOffset));
							}
							indexOffset += ind.length;

							for (int i = 0; i < drbl.loadedMesh.getNumVertices() * attSize; i += attSize) {

								vertices.add(verts[i + positionOffset]);
								vertices.add(verts[i + positionOffset + 1]);
								vertices.add(verts[i + positionOffset + 2]);

								if ((e.isSolid || drbl.addCollisionTriangles) && collisionTriangles != null) {
									synchronized (collisionTriangles) {
										collisionTriangles.add(new Vector3(verts[i + positionOffset], verts[i + positionOffset + 1], verts[i + positionOffset + 2]));
									}
								}

								Color c = Color.WHITE;
								Color tempColor = t_staticMeshColor.set(Color.WHITE);

								if (!e.fullbrite) {
									c = level.getLightColorAt(verts[i + positionOffset], verts[i + positionOffset + 2], verts[i + positionOffset + 1], null, tempColor);
								}

								vertices.add(c.toFloatBits());
								vertices.add(verts[i + uvOffset]);
								vertices.add(verts[i + uvOffset + 1]);
								vertexCount++;
							}
						}
					}
				}
			}
		}

		if(vertexCount == 0) return null;

		if(collisionTriangles != null) {
			for (int i = 0; i < collisionTriangles.size(); i += 3) {
				CollisionTriangle triangle = new CollisionTriangle(collisionTriangles.get(i + 2), collisionTriangles.get(i + 1), collisionTriangles.get(i));
				synchronized (triangleSpatialHash) {
					triangleSpatialHash.AddTriangle(triangle);
				}
			}
		}

		VertexAttributes attributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		Mesh m;
		synchronized (staticMeshPool) {
			m = staticMeshPool.obtain(attributes, vertexCount, indices.size, level instanceof OverworldLevel);
		}

		// pack the array
		indices.shrink();
		vertices.shrink();

		m.setIndices(indices.toArray());
		m.setVertices(vertices.toArray());

		created.add(m);
		return created;
	}

	public void renderCollisionBox(Entity e, ShapeRenderer lineRenderer) {
		if(!e.isSolid && !(e instanceof Projectile))
			return;

		if(lineRenderer == null)
			return;

		float zStart = e.z - 0.5f;
		float zEnd = zStart + e.collision.z;

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

	Color pathfindingColorStart = new Color();
	Color pathfindingColorEnd = new Color();
	public void renderPathfinding() {
		ShapeRenderer lineRenderer = collisionLineRenderer;
		if(lineRenderer == null) lineRenderer = new ShapeRenderer();
		lineRenderer.setProjectionMatrix(camera.combined);

		Gdx.gl.glLineWidth(2f);

		lineRenderer.setColor(Color.WHITE);
		lineRenderer.begin(ShapeType.Line);

		for(PathNode node : Game.pathfinding.GetNodes()) {
			if(node != null) {
				Array<PathNode> connections = node.getConnections();
				Array<PathNode> jumps = node.getJumps();

				for(int i = 0; i < connections.size; i++) {
					PathNode c = connections.get(i);

					float colorStart;
					if(node.playerSmell > Pathfinding.MaxTraversal)
						colorStart = 0f;
					else
						colorStart = 1f - ((float)node.playerSmell / Pathfinding.MaxTraversal);

					float colorEnd;
					if(c.playerSmell > Pathfinding.MaxTraversal)
						colorEnd = 0f;
					else
						colorEnd = 1f - ((float)c.playerSmell / Pathfinding.MaxTraversal);

					pathfindingColorStart.set(colorStart, colorStart, colorStart, 1f);
					pathfindingColorEnd.set(colorEnd, colorEnd, colorEnd, 1f);

					lineRenderer.line(node.loc.x, node.loc.z - 0.4f, node.loc.y, c.loc.x, c.loc.z - 0.4f, c.loc.y, pathfindingColorStart, pathfindingColorEnd);
				}

				for(int i = 0; i < jumps.size; i++) {
					PathNode c = jumps.get(i);

					float colorStart;
					if(node.playerSmell > Pathfinding.MaxTraversal)
						colorStart = 0f;
					else
						colorStart = 1f - ((float)node.playerSmell / Pathfinding.MaxTraversal);

					float colorEnd;
					if(c.playerSmell > Pathfinding.MaxTraversal)
						colorEnd = 0f;
					else
						colorEnd = 1f - ((float)c.playerSmell / Pathfinding.MaxTraversal);

					pathfindingColorStart.set(colorStart, colorStart * 0.1f, colorStart * 0.1f, 1f);
					pathfindingColorEnd.set(colorEnd, colorEnd * 0.1f, colorEnd * 0.1f, 1f);

					lineRenderer.line(node.loc.x, node.loc.z - 0.4f, node.loc.y, c.loc.x, c.loc.z - 0.4f, c.loc.y, pathfindingColorStart, pathfindingColorEnd);
				}
			}
		}

		lineRenderer.end();
	}

	public void renderCollisionBoxes(Level level) {

		ShapeRenderer lineRenderer = collisionLineRenderer;
		if(lineRenderer == null) lineRenderer = new ShapeRenderer();
		lineRenderer.setProjectionMatrix(camera.combined);

		Gdx.gl.glLineWidth(2f);

		lineRenderer.setColor(Color.WHITE);
		lineRenderer.begin(ShapeType.Line);

		for(Entity e : level.entities) {
			renderCollisionBox(e, lineRenderer);
		}

		for(Entity e : level.static_entities) {
			renderCollisionBox(e, lineRenderer);
		}

		lineRenderer.end();
	}

	public void renderVisualizer() {
		if(collisionLineRenderer != null) {
			collisionLineRenderer.setProjectionMatrix(camera.combined);

			Gdx.gl.glLineWidth(2f);
			collisionLineRenderer.end();
			collisionLineRenderer.begin(ShapeType.Line);
		}
	}

	public void visualizeCollisionCheck(float x, float y, float z, Vector3 collision) {

		x += 0.5f;
		y += 0.5f;

		float zStart = z - 0.5f;
		float zEnd = zStart + collision.z;

		if(collisionLineRenderer == null) {
			collisionLineRenderer = new ShapeRenderer();
			collisionLineRenderer.setColor(Color.WHITE);
			collisionLineRenderer.setProjectionMatrix(camera.combined);
			collisionLineRenderer.begin(ShapeType.Line);
		}

		collisionLineRenderer.line(x - collision.x, zStart, y + collision.y, x + collision.x, zStart, y + collision.y);
		collisionLineRenderer.line(x - collision.x, zStart, y - collision.y, x + collision.x, zStart, y - collision.y);
		collisionLineRenderer.line(x + collision.x, zStart, y - collision.y, x + collision.x, zStart, y + collision.y);
		collisionLineRenderer.line(x - collision.x, zStart, y - collision.y, x - collision.x, zStart, y + collision.y);

		collisionLineRenderer.line(x - collision.x, zEnd, y + collision.y, x + collision.x, zEnd, y + collision.y);
		collisionLineRenderer.line(x - collision.x, zEnd, y - collision.y, x + collision.x, zEnd, y - collision.y);
		collisionLineRenderer.line(x + collision.x, zEnd, y - collision.y, x + collision.x, zEnd, y + collision.y);
		collisionLineRenderer.line(x - collision.x, zEnd, y - collision.y, x - collision.x, zEnd, y + collision.y);

		collisionLineRenderer.line(x - collision.x, zStart, y + collision.y, x - collision.x, zEnd, y + collision.y);
		collisionLineRenderer.line(x - collision.x, zStart, y - collision.y, x - collision.x, zEnd, y - collision.y);
		collisionLineRenderer.line(x + collision.x, zStart, y + collision.y, x + collision.x, zEnd, y + collision.y);
		collisionLineRenderer.line(x + collision.x, zStart, y - collision.y, x + collision.x, zEnd, y - collision.y);
	}

	public void visualizeTriangle(Triangle t) {
		if(collisionLineRenderer == null) {
			collisionLineRenderer = new ShapeRenderer();
			collisionLineRenderer.setColor(Color.WHITE);
			collisionLineRenderer.setProjectionMatrix(camera.combined);
			collisionLineRenderer.begin(ShapeType.Line);
		}

		collisionLineRenderer.line(t.v1.x, t.v1.y, t.v1.z, t.v2.x, t.v2.y, t.v2.z);
		collisionLineRenderer.line(t.v2.x, t.v2.y, t.v2.z, t.v3.x, t.v3.y, t.v3.z);
		collisionLineRenderer.line(t.v3.x, t.v3.y, t.v3.z, t.v1.x, t.v1.y, t.v1.z);
	}

	public void renderProjectionBoxes(Level level) {

		ShapeRenderer lineRenderer = collisionLineRenderer;
		if(lineRenderer == null) lineRenderer = new ShapeRenderer();
		lineRenderer.setProjectionMatrix(camera.combined);

		Gdx.gl.glLineWidth(2f);

		lineRenderer.setColor(Color.WHITE);
		lineRenderer.begin(ShapeType.Line);

		for(Entity e : level.entities) {
			//if(!e.isSolid) continue;

			if(e instanceof ProjectedDecal) {
				ProjectedDecal decal = (ProjectedDecal)e;
				if(decal.perspective == null) continue;

				for(int i = 0; i < 4; i++) {
					Vector3 startPoint = decal.perspective.frustum.planePoints[i];
					Vector3 endPoint = i != 3 ? decal.perspective.frustum.planePoints[i + 1] : decal.perspective.frustum.planePoints[0];

					lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
				}

				for(int i = 0; i < 4; i++) {
					Vector3 startPoint = decal.perspective.frustum.planePoints[i];
					Vector3 endPoint = decal.perspective.frustum.planePoints[i + 4];

					lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
				}

				for(int i = 4; i < 8; i++) {
					Vector3 startPoint = decal.perspective.frustum.planePoints[i];
					Vector3 endPoint = i != 7 ? decal.perspective.frustum.planePoints[i + 1] : decal.perspective.frustum.planePoints[4];

					lineRenderer.line(startPoint.x, startPoint.y, startPoint.z, endPoint.x, endPoint.y, endPoint.z);
				}
			}
		}

		lineRenderer.end();
	}

	public static DynamicLight getLight() {
		usedLights++;
		DynamicLight l = lightPool.obtain();
		if(l != null) {
			l.color.set(0,0,0);
			l.range = 3.0f;
			usedLightPool.add(l);
		}
		return l;
	}

	private void initLights() {
		clearLights();
	}

	public void clearLights() {

		for(i = 0; i < lightColors.length; i++) {
			lightColors[i] = 0f;
		}

		usedLights = 0;
		// free the light pool
		lightPool.freeAll(usedLightPool);
		usedLightPool.clear();
	}

	public void disposeMeshes() {
		Gdx.app.log("Renderer", "Disposing Tesselator Meshes");
		GlRenderer.staticMeshPool.resetAndDisposeAllMeshes();
		Tesselator.tesselatorMeshPool.resetAndDisposeAllMeshes();
	}

	public void setLevelToRender(Level level) {
		Gdx.app.log("Renderer", "Set level to render");
		loadedLevel = level;
		level.rendererDirty = true;
		level.mapIsDirty = true;
		if(camera != null) camera.far = level.viewDistance;

		fogColor.set(loadedLevel.fogColor);
		fogStart = loadedLevel.fogStart;
		fogEnd = loadedLevel.fogEnd;
		viewDistance = loadedLevel.viewDistance;
		skybox = loadedLevel.skybox;

		handLagRotation = null;
		offhandLagRotation = null;
		cutsceneCamera = null;

		stencilHaloColors.clear();

		// Hack, but try to fix that mouse jump on level switches
		if(Game.instance != null && Game.instance.input != null)
			Game.instance.input.ignoreLastMouseLocation = true;
	}

	private Vector3 t_updateVisVector = new Vector3();
	private Random updateVisRandom = new Random();
	public void updateVisiblityOfEntity(Entity entity) {
		if(entity.drawDistance != Entity.DrawDistance.FAR) {
			if(entity.drawUpdateTimer == null) entity.drawUpdateTimer = (float)updateVisRandom.nextInt(100);
			entity.drawUpdateTimer -= Gdx.graphics.getDeltaTime() * 300;
			if (entity.drawUpdateTimer <= 0) {
				entity.outOfDrawDistance = false;
				t_updateVisVector.set(entity.x, entity.z, entity.y);
				float distance = t_updateVisVector.sub(camera.position).len();
				if(entity.drawDistance == Entity.DrawDistance.MEDIUM && distance >= 18f) {
					entity.outOfDrawDistance = true;
				}
				else if(entity.drawDistance == Entity.DrawDistance.NEAR && distance >= 10f) {
					entity.outOfDrawDistance = true;
				}
				entity.drawUpdateTimer = 100f;
			}
		}
	}

	public DecalBatch makeNewDecalBatch(String shader, boolean transparent) {

		try {
			ShaderInfo shaderInfo = ShaderManager.getShaderManager().getCompiledShader(shader);

			DecalBatch newBatch = new DecalBatch(new SpriteGroupStrategy(camera, game, shaderInfo, !transparent ? 1 : -1));
			if(transparent) {
				transparentSpriteBatches.put(shader, newBatch);
			}
			else {
				opaqueSpriteBatches.put(shader, newBatch);
			}

			return newBatch;
		}
		catch(Exception ex) {
			Gdx.app.error("Delver-makeNewDecalBatch", ex.getMessage());
			return getDecalBatch("sprite", transparent ? Entity.BlendMode.ALPHA : Entity.BlendMode.OPAQUE);
		}
	}

	public DecalBatch getDecalBatch(String shader, Entity.BlendMode blendMode) {

		if(shader == null || shader.equals("")) {
			shader = "sprite";
		}

		boolean transparent = blendMode != Entity.BlendMode.OPAQUE;

		if(renderingForPicking) {
			shader = "picking";
			transparent = false;
		}

		if (transparent) {
			DecalBatch batch = transparentSpriteBatches.get(shader);
			if (batch != null) return batch;
		} else {
			DecalBatch batch = opaqueSpriteBatches.get(shader);
			if (batch != null) return batch;
		}

		return makeNewDecalBatch(shader, transparent);
	}

	public void freeLoadedLevel() {
		loadedLevel = null;
	}

	public static void bindTexture(Texture t) {
		if(t == null || t != boundTexture) {
			t.bind();
			boundTexture = t;
		}
	}

	public static void clearBoundTexture() {
		boundTexture = null;
	}

	public Color getColorFromPool(Color toCopy) {
		Color c = colorPool.obtain();
		usedColorPool.add(c);
		return c.set(toCopy);
	}

	public void freAllColorsFromPool() {
		colorPool.freeAll(usedColorPool);
		usedColorPool.clear();
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

	public static void EnableBlending(boolean enabled) {
		boolean blendingEnabled = Gdx.gl.glIsEnabled(GL20.GL_BLEND);
		if(blendingEnabled != enabled) {
			if(enabled) {
				Gdx.gl.glEnable(GL20.GL_BLEND);
			}
			else {
				Gdx.gl.glDisable(GL20.GL_BLEND);
			}
		}
	}

	public void startFrame() {
		if(canvasFrameBuffer != null && postProcessBatch != null && Options.instance.enablePostProcessing) {
			canvasFrameBuffer.bind();
			canvasFrameBuffer.begin();
		}

		Gdx.gl20.glClearColor(fogColor.r, fogColor.g, fogColor.b, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
		EnableBlending(false);
	}

	public void endFrame() {
		boolean doFxaaPass = Options.instance.fxaaEnabled && fxaaShader != null;
		if(canvasFrameBuffer != null && postProcessBatch != null && (Options.instance.enablePostProcessing)) {
			canvasFrameBuffer.end();

			Texture diffuseTexture = canvasFrameBuffer.getColorBufferTexture();
			postProcessBatch.setProjectionMatrix(camera2D.combined);

			if(Options.instance.postProcessingQuality > 0 && Options.instance.postProcessFilter != null) {
				FrameBuffer drawPostIntoBuffer = doFxaaPass ? fxaaFrameBuffer : null;
				drawPostProcessFilter(diffuseTexture, drawPostIntoBuffer);

				if(drawPostIntoBuffer != null)
					diffuseTexture = drawPostIntoBuffer.getColorBufferTexture();
			}

			// Apply FXAA
			if(doFxaaPass) {
				drawFxaaPass(diffuseTexture);
			}
		}
	}

	public void gaussianBlurGlow(float kernelSize, FrameBuffer frameBuffer) {
		Texture diffuseTexture = canvasFrameBuffer.getColorBufferTexture();
		Texture frameTexture = diffuseTexture;

		kernelSize *= (Options.instance.postProcessingQuality / 3.0f);

		FrameBuffer originalBuffer = frameBuffer;

		for(int i = 0; i < kernelSize; i++) {
			// Blur passes
			frameBuffer = i % 2 != 0 ? originalBuffer : blurFrameBufferPingPong;
			frameBuffer.begin();

			if(i == 0) {
				Gdx.gl20.glClearColor(0, 0, 0, 1);
				Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
			}

			postProcessBatch.setShader(blurShader);
			postProcessBatch.begin();
			blurShader.setUniformf("u_resolution", Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);

			// Decrease the radius over the iterations
			float radius = (kernelSize - i) - 1;
			if(i % 2 == 0) {
				blurShader.setUniformf("u_direction", radius, 0.0f);
			}
			else {
				blurShader.setUniformf("u_direction", 0.0f, radius);
			}

			// Draw using the blur shader
			frameTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			postProcessBatch.draw(frameTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2) - 0.5f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
			postProcessBatch.flush();
			postProcessBatch.end();

			frameBuffer.end();

			// Use this buffer next time
			frameTexture = frameBuffer.getColorBufferTexture();
		}
	}

	public void drawPostProcessFilter(Texture diffuseTexture, FrameBuffer drawIntoOrNull) {
		// Gaussian blur passes
		int[] sizes = {12, 8, 2};
		FrameBuffer[] buffers = {blurFrameBuffer1, blurFrameBuffer2, blurFrameBuffer3};

		for (i = 0; i < sizes.length; i++) {
			// Do the blur
			gaussianBlurGlow(sizes[i], buffers[i]);
		}

		// Grab the filter we want to use
		String postShader = "post_filter_bloom";
		if(Options.instance.postProcessFilter != null) {
			postShader = Options.instance.postProcessFilter;
		}

		ShaderInfo postProcessShader = ShaderManager.getShaderManager().getCompiledShader(postShader);

		ShaderProgram shader = null;
		if(postProcessShader != null) {
			shader = postProcessShader.shader;
		}
		if(shader == null) {
			shader = uiShader;
		}

		postProcessBatch.setShader(shader);
		postProcessBatch.begin();

		// Might need to capture into another buffer
		if(drawIntoOrNull != null) {
			drawIntoOrNull.begin();
		}

		// bind shader attributes
		if(postProcessShader != null) {
			postProcessShader.begin(3);
		}

		// Draw the final image
		blurFrameBuffer3.getColorBufferTexture().bind(3);
		blurFrameBuffer2.getColorBufferTexture().bind(2);
		blurFrameBuffer1.getColorBufferTexture().bind(1);

		diffuseTexture.bind(0);
		diffuseTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		Color eyeAdaptationColor = loadedLevel.getLightColorAt(camera.position.x, camera.position.z, camera.position.y, null, tempColor);
		eyeAdaptationAmount = Interpolation.linear.apply(eyeAdaptationAmount, (eyeAdaptationColor.r + eyeAdaptationColor.g + eyeAdaptationColor.b) / 2.75f, 1.66f * Gdx.graphics.getDeltaTime());

		shader.setUniformf("u_resolution", diffuseTexture.getWidth(), diffuseTexture.getHeight());
		shader.setUniformi("u_texture3", 3);
		shader.setUniformi("u_texture2", 2);
		shader.setUniformi("u_texture1", 1);
		shader.setUniformf("u_eyeAdaptation", eyeAdaptationAmount);
		postProcessBatch.draw(diffuseTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2 ), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
		postProcessBatch.flush();
		postProcessBatch.end();

		if(drawIntoOrNull != null) {
			drawIntoOrNull.end();
		}
	}

	public void drawFxaaPass(Texture frameTexture) {
		postProcessBatch.setShader(fxaaShader);
		postProcessBatch.begin();
		fxaaShader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		postProcessBatch.draw(frameTexture, -(Gdx.graphics.getWidth() / 2), -(Gdx.graphics.getHeight() / 2 ), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
		postProcessBatch.flush();
		postProcessBatch.end();
	}

	public Color getPickColorForEntity(Entity e) {
		return entityPickColor;
	}

	public void resetEntityPickColor(Entity e) {
		if(entitiesForPicking == null) {
			entitiesForPicking = new IntMap<Entity>();
		}

		int index = entitiesForPicking.size + 1;
		int r = (index >> 16) & 0xff;
		int g = (index >> 8) & 0xff;
		int b = (index) & 0xff;

		entityPickColor.set(r / 255f, g / 255f, b / 255f, 1f);
		entitiesForPicking.put(index, e);
	}

	public void setFieldOfViewMod(float newFieldOfView) {
	    fieldOfViewMod = newFieldOfView;
    }
}
