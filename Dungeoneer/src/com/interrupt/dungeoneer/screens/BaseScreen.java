package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Tesselator;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.input.ControllerState.DPAD;
import com.interrupt.dungeoneer.ui.UiSkin;

public class BaseScreen implements Screen {
	public class GamepadEntryListener {
		public void onPress() {}
	}

	public class GamepadEntry {
		public Table table;
		private GamepadEntryListener onSelect = null;
		private GamepadEntryListener onDeselect = null;

		public GamepadEntry(Table table, GamepadEntryListener onSelect, GamepadEntryListener onDeselect) {
			this.table = table;
			this.onSelect = onSelect;
			this.onDeselect = onDeselect;
		}

		public void select() {
			this.onSelect.onPress();
		}

		public void deselect() {
			this.onDeselect.onPress();
		}
	}

	public static SplashScreenInfo splashScreenInfo = new SplashScreenInfo();

	protected String screenName = "BaseScreen";

	protected static int curWidth;
    protected static int curHeight;
    protected boolean running = false;

    protected Stage ui;
    protected Skin skin;
    protected BitmapFont font;

    protected GlRenderer renderer;
	protected GL20 gl;

	protected Texture backgroundTexture = null;
	protected Color backgroundColor = new Color(Color.WHITE);

	protected ControllerState controllerState = null;
	protected Integer gamepadSelectionIndex = null;
	protected Array<GamepadEntry> gamepadEntries = new Array<GamepadEntry>();

	protected float uiScale = 3.75f;

	protected static Level level = null;

    protected String splashLevel = null;
    protected Vector3 splashCameraPosition = new Vector3(7f,-1.25f,11f);
    protected Vector3 splashCameraLookAt = new Vector3(-3f, 3f, 6f);
    protected Vector3 splashCameraLookAtSway = new Vector3();
    protected boolean splashCameraSway = true;

	protected boolean useBackgroundLevel = true;

	protected Viewport viewport;

	protected boolean showMouse = true;

	protected float scale = 1f;

	Vector2 viewportSize = new Vector2(426, 240).scl(3f);

	public boolean tickGamepadManager = true;

	public BaseScreen() {
		skin = UiSkin.getSkin();
		font = UiSkin.getFont();
		viewport = new FillViewport(viewportSize.x, viewportSize.y);
		recalculateUiScale(scale);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void hide() {
		Gdx.app.log(screenName, "LibGdx Hide");
		running = false;
	}

	@Override
	public void pause() {
		Gdx.app.log(screenName, "LibGdx Pause");
		if(Game.isMobile)
			running = false;
	}

	@Override
	public void render(float delta) {
		if(running) {
			tick(delta);
        	draw(delta);
		}
	}

	@Override
	public void resize(int width, int height) {
		Gdx.app.log(screenName, "LibGdx Resize");

		recalculateUiScale(scale);

		if(viewport != null) {
			viewportSize.set(width, height).scl(uiScale);
			viewport.setWorldSize(viewportSize.x, viewportSize.y);
			viewport.update(width, height ,true);
		}

		if(ui != null) {
			Gdx.input.setInputProcessor(ui);
			ui.setViewport(viewport);
		}

		curWidth = width;
		curHeight = height;
		GameManager.renderer.setSize(width, height);
	}

	@Override
	public void resume() {
		Gdx.app.log(screenName, "LibGdx Resume");
		running = true;
	}

	@Override
	public void show() {
		Gdx.app.log(screenName, "LibGdx Show");

		running = true;

		if(showMouse && Gdx.input.isCursorCatched()) {
			Gdx.input.setCursorCatched(false);
		}

		renderer = GameManager.renderer;
		gl = renderer.getGL();

		controllerState = Game.gamepadManager.controllerState;

		gamepadEntries.clear();

		// non mobile devices get a fancy menu background
		if(!hasBackgroundLevel() && useBackgroundLevel) {
			loadBackgroundLevel(splashLevel);
		}

		if(!hasBackgroundLevel()) {
			if(splashScreenInfo != null && splashScreenInfo.backgroundImage != null) {
				backgroundTexture = Art.loadTexture(splashScreenInfo.backgroundImage);
				if(backgroundTexture != null) {
					backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
				}
			}
		}

		recalculateUiScale(scale);
	}

	protected void draw(float delta) {
		renderer = GameManager.renderer;
		gl = renderer.getGL();

		gl.glClearColor(0, 0, 0, 1);

		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT);
		gl.glDisable(GL20.GL_ALPHA);
		gl.glDisable(GL20.GL_BLEND);
        gl.glEnable(GL20.GL_CULL_FACE);

		GlRenderer.time += delta;

		// draw a background level if one exists
		if(level != null) {
            splashCameraLookAtSway.set(splashCameraLookAt);

            if(splashCameraSway)
                splashCameraLookAtSway.add((float)Math.sin(GlRenderer.time * 0.06f) * 4f, (float)Math.sin(GlRenderer.time * 0.04f) * 0.5f, 0f);

            renderer.loadedLevel = level;
            GlRenderer.clearBoundTexture();
			renderer.updateDynamicLights(renderer.camera);
			renderer.updateShaderAttributes();

			renderer.startFrame();

			if(GlRenderer.skybox != null) {
				GlRenderer.skybox.x = renderer.camera.position.x;
				GlRenderer.skybox.z = renderer.camera.position.y;
				GlRenderer.skybox.y = renderer.camera.position.z;
				GlRenderer.skybox.scale = 4f;
				GlRenderer.skybox.fullbrite = true;
				GlRenderer.skybox.update();

				// draw sky
				Gdx.gl20.glDisable(GL20.GL_CULL_FACE);
				renderer.renderSkybox(GlRenderer.skybox);
				Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
				Gdx.gl20.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			}

			renderer.renderWorld(level);
			renderer.renderEntities(level);
			renderer.renderMeshes();
			renderer.renderStencilPasses();
			renderer.renderTransparentEntities();
			renderer.clearDecals();

			renderer.endFrame();
		}
		else if(backgroundTexture != null) {
            gl.glDisable(GL20.GL_CULL_FACE);
            gl.glDisable(GL20.GL_DEPTH_TEST);
			renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
			renderer.uiBatch.begin();

			float backgroundScale = (float)backgroundTexture.getWidth() / (float)backgroundTexture.getHeight();
			renderer.uiBatch.setColor(backgroundColor);
			renderer.uiBatch.draw(backgroundTexture, -(curHeight * backgroundScale) / 2f, -(curHeight / 2f), curHeight * backgroundScale, curHeight);

			renderer.uiBatch.end();

            gl.glEnable(GL20.GL_DEPTH_TEST);
		}
	}

	float time_since_last_tick = 0f;
	protected void tick_level(float delta) {
		if(level != null) {
			float level_delta = delta * 60f;

			// put a cap on tick rates
			time_since_last_tick += level_delta;
			if(time_since_last_tick < 0.3333333f) {
				return;
			}
			else {
				level_delta = time_since_last_tick;
				time_since_last_tick = 0;
			}

			renderer.clearLights();
			level.tickEntityList(level_delta, level.entities, false);
			level.tickEntityList(level_delta, level.static_entities, false);
			level.tickEntityList(level_delta, level.non_collidable_entities, false);
		}
	}

	protected void tick(float delta) {
		// tick the background level, if one is loaded
		tick_level(delta);

		// Translate keyboard directions into gamepad directions
		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			Game.gamepadManager.controllerState.dpadEvents.add(DPAD.UP);
		}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			Game.gamepadManager.controllerState.dpadEvents.add(DPAD.DOWN);
		}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
			Game.gamepadManager.controllerState.dpadEvents.add(DPAD.LEFT);
		}
		else if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
			Game.gamepadManager.controllerState.dpadEvents.add(DPAD.RIGHT);
		}

		// Navigate through the Gamepad Entries
		if(Game.gamepadManager.controllerState.dpadEvents.size > 0 && gamepadEntries.size > 0) {
			DPAD direction = Game.gamepadManager.controllerState.dpadEvents.get(0);

			if(direction == DPAD.DOWN) {
				if (gamepadSelectionIndex == null) {
					gamepadSelectionIndex = 0;
				}
				else {
					gamepadSelectionIndex = (gamepadSelectionIndex + 1 + gamepadEntries.size) % gamepadEntries.size;
				}
			}
			else if(direction == DPAD.UP) {
				if (gamepadSelectionIndex == null) {
					gamepadSelectionIndex = 0;
				}
				else {
					gamepadSelectionIndex = (gamepadSelectionIndex - 1 + gamepadEntries.size) % gamepadEntries.size;
				}
			}

			// HACK: Fix this Future Joshua
			if(gamepadSelectionIndex != null && gamepadSelectionIndex < gamepadEntries.size) {
				Table button = gamepadEntries.get(gamepadSelectionIndex).table;
				if (button.isVisible() && !(button instanceof TextButton)) {
					for (EventListener listener : button.getListeners()) {
						if (listener instanceof ClickListener) {
							((ClickListener) listener).clicked(new InputEvent(), button.getX(), button.getY());
						}
					}
				}
			}

			// Set text button styles
			for(GamepadEntry entry : gamepadEntries) {
				if(entry.table instanceof TextButton) {
					GamepadEntry selectedEntry = null;
					if(gamepadSelectionIndex != null) selectedEntry = gamepadEntries.get(gamepadSelectionIndex);

					if(selectedEntry == entry) {
						((TextButton)entry.table).setStyle(UiSkin.getSkin().get("gamepad-selected", TextButton.TextButtonStyle.class));
					}
					else {
						((TextButton)entry.table).setStyle(UiSkin.getSkin().get("default", TextButton.TextButtonStyle.class));
					}
				}
			}
		}

		if(gamepadSelectionIndex != null && gamepadEntries.size > 0){
			if (Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.SELECT, true) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
				GamepadEntry entry = this.gamepadEntries.get(gamepadSelectionIndex);
				entry.select();
			}
			else if (Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.CANCEL, true)) {
				GamepadEntry entry = this.gamepadEntries.get(gamepadSelectionIndex);
				entry.deselect();
			}
		}

		if(tickGamepadManager) {
			Game.gamepadManager.menuMode = true;
			Game.gamepadManager.tick(delta);
		}

		SteamApi.api.runCallbacks();
	}

	// loads a background level
	public static void loadBackgroundLevel(String filename) {

        if(filename == null) return;

        if(GameManager.renderer.needToInit)
			GameManager.renderer.initTextures();

		level = new Level(17,17);
		level.loadForSplash(filename);

		// Temple fog settings
		level.fogStart = 2f;
		level.fogEnd = 12f;

		level.init(Source.LEVEL_START);

		level.isDirty = true;
		level.rendererDirty = true;

		GameManager.renderer.setLevelToRender(level);
	}

	// unload any background level
	public static void freeBackgroundLevel() {
		if(hasBackgroundLevel()) {
			level.rendererDirty = true;
			level.entities.clear();
			level.non_collidable_entities.clear();
			level.static_entities.clear();
			level = null;
			GameManager.renderer.freeLoadedLevel();
		}
	}

	public void recalculateUiScale() {
		// shrink scale if we start to clip outside the default bounds
		float scaleX = Gdx.graphics.getWidth() / 1800f;
		float scaleY = Gdx.graphics.getHeight() / 1020f;
		uiScale = Math.max(scaleX, scaleY);

		Gdx.app.log("Delver", Gdx.graphics.getWidth() + "");

		float min = Math.min(scaleX, scaleY);
		min = (int)(min * 25f);
		min = (min / 25f);
		uiScale /= min;

		uiScale *= 0.3f;
	}

	public void recalculateUiScale(float scaling) {
		// shrink scale if we start to clip outside the default bounds
		float scaleX = Gdx.graphics.getWidth() / 1800f;
		float scaleY = Gdx.graphics.getHeight() / 1020f;
		uiScale = Math.max(scaleX, scaleY);

		Gdx.app.log("Delver", Gdx.graphics.getWidth() + "");

		float min = Math.min(scaleX, scaleY);
		min = (int)(min * 25f);
		min = (min / 25f);
		uiScale /= min;

		uiScale *= 0.3f;
		uiScale /= scaling;
	}

	public static boolean hasBackgroundLevel() {
		return level != null;
	}

	public static Viewport getViewport(float width, float height) {
		return new FillViewport(width, height);
	}
}
