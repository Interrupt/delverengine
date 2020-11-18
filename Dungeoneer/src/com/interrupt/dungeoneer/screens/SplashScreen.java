package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.ShaderManager;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

import java.util.Random;

public class SplashScreen extends BaseScreen {

    private float tickCount = 30;
    private Interpolation lerp = Interpolation.swing;
    private float fadeStartTick = 0;
    private float fadeEndTick = 200;
    
    private boolean isFadingOut = false;
    
    private String splashText = "";
    
    private Color whiteColor = new Color(Color.WHITE);
    private Color greyColor = new Color(Color.DARK_GRAY);
    private Color blackColor = new Color(Color.BLACK);
    private Color fadeColor = new Color();

    private String[] splashTexts = { StringManager.get("screens.SplashScreen.pressKeyToStart") };
    
    protected Texture logoTexture = null;

	ShaderProgram logoShader = null;

	private float time = -1.5f;
	private float nextFlashTime = 15;

	Random r = new Random();
	
	public SplashScreen() {
		screenName = "SplashScreen";

		try {
			splashScreenInfo = JsonUtil.fromJson(SplashScreenInfo.class, Game.findInternalFileInMods("data/splash.dat"));
		}
		catch(Exception ex) {
			Gdx.app.error("Delver", ex.getMessage());
		}

		splashLevel = splashScreenInfo.backgroundLevel;
		
		Game.init();
		Options.loadOptions();
		
		// load the entity templates
		EntityManager entityManager = Game.getModManager().loadEntityManager(Game.gameData.entityDataFiles);
		EntityManager.setSingleton(entityManager);

		try {
			logoShader = ShaderManager.getShaderManager().loadShader("", "logo.vert", "logo.frag");
		}
		catch(Exception ex) {
			Gdx.app.error("Delver", ex.getMessage());
		}
	}
	
	@Override
	public void show() {
		super.show();

		splashText = splashTexts[r.nextInt(splashTexts.length)];

		if(splashScreenInfo.logoImage != null) {
			logoTexture = Art.loadTexture(splashScreenInfo.logoImage);

			if(splashScreenInfo.logoFilter) {
				logoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
			}
		}

		fadeStartTick = 0;
		fadeEndTick = 200;
		isFadingOut = false;
		tickCount = 30;
		
		Options.loadOptions();

		if(splashScreenInfo.music != null)
			Audio.playMusic(splashScreenInfo.music, true);

        ui = new Stage(viewport);
        ui.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                goToNextScreen();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                goToNextScreen();
                return super.keyTyped(event, character);
            }
        });

        Gdx.input.setInputProcessor(ui);
	}
	
	@Override
	public void draw(float delta) {
		
		renderer = GameManager.renderer;
		gl = renderer.getGL();
		
		float lTime = Math.min((tickCount - fadeStartTick) / (fadeEndTick - fadeStartTick), 1);
		float fade = 0;
		if(lTime > 0)
			fade = lerp.apply(lTime);
		
		if(!isFadingOut)
			backgroundColor.set(Color.WHITE).mul(fade);
		else
			backgroundColor.set(Color.WHITE);
		
		super.draw(delta);
		
		whiteColor.set(Color.WHITE).mul(fade);
		greyColor.set(Color.DARK_GRAY).mul(fade, fade, fade, fade * 0.75f);
        blackColor.set(Color.BLACK).mul(fade, fade, fade, fade * 0.75f);
		
		renderer.uiBatch.enableBlending();

		float uiScale = Game.getDynamicUiScale();
		
		if(logoTexture != null) {
			ShaderProgram defaultShader = renderer.uiBatch.getShader();

			if(logoShader != null) {
				renderer.uiBatch.setShader(logoShader);
			}

			renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
			renderer.uiBatch.begin();

			if(logoShader != null) {
				logoShader.setUniformf("u_time", time);
			}
			
			float logoSize = uiScale * 650;

			float backgroundScale = logoTexture.getWidth() / logoTexture.getHeight();
			backgroundScale *= fade;

			renderer.uiBatch.setColor(whiteColor);
			renderer.uiBatch.draw(logoTexture, -(logoSize * backgroundScale) / 2f, -(logoSize / 2f) + (logoSize * 0.15f), logoSize * backgroundScale, logoSize * fade);
			renderer.uiBatch.end();

			renderer.uiBatch.setShader(defaultShader);
		}
		
		// draw the welcome text
		float fontSize = uiScale * 140;
		
		renderer.uiBatch.setProjectionMatrix(renderer.camera2D.combined);
		renderer.uiBatch.begin();
		
		float yPos = (int)((1 * -fontSize * 3.25f) / 2 + fontSize * 3.25f);

        if(!isFadingOut) {
            renderer.drawCenteredText(splashText, -yPos - fontSize * 0.5f, fontSize * 0.25f, whiteColor, greyColor);
        }

        renderer.drawCenteredText("2018 Priority Interrupt Games", -yPos - fontSize * 1.7f, fontSize * 0.15f, greyColor, blackColor);
		
		renderer.uiBatch.end();

		if(!isFadingOut && 1f - whiteColor.r > 0f)
			renderer.drawFlashOverlay(fadeColor.set(0f, 0f, 0f, 1f - whiteColor.r));
	}

    public void goToNextScreen() {
        if((tickCount-fadeStartTick) >= fadeEndTick && !isFadingOut)
        {
            isFadingOut = true;
            fadeEndTick = tickCount;
            fadeStartTick = tickCount + 60;
        }
    }
	
	@Override
	public void tick(float delta) {
		
		tickCount += delta * 80f;
		time += delta * 2f;

		if(time > nextFlashTime) {
			time = 0f;
			nextFlashTime = r.nextInt(10) + 10;
		}

        if(controllerState.buttonEvents.size>0) goToNextScreen();

        if(isFadingOut) {
            float fade = lerp.apply(Math.min((tickCount - fadeStartTick) / (fadeEndTick - fadeStartTick), 1));
            if (fade <= 0) {
                GameApplication.SetScreen(new MainMenuScreen());
            }
        }

        if(level != null) {
        	level.fogStart = splashScreenInfo.fogStart;
			level.fogEnd = splashScreenInfo.fogEnd;
		}

        // tick needs to be down here to not clear gamepad events
        super.tick(delta);
    }

}
