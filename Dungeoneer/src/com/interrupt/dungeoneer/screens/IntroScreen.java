package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;

import java.util.Random;

public class IntroScreen extends BaseScreen {

    Table fullTable;

    public IntroScreen() {
        useBackgroundLevel = true;
        backgroundColor.set(0, 0, 0, 1f);

        ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);
        fullTable.align(Align.center);

        ui.addActor(fullTable);
        Gdx.input.setInputProcessor(ui);
    }

    @Override
    public void show() {
        splashLevel = SplashScreen.splashScreenInfo.backgroundLevel;
        super.show();

        // Show the PI logo
        Texture logoTextureOne = Art.loadTexture("splash/logo-1.png");
        logoTextureOne.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Texture logoTextureTwo = Art.loadTexture("splash/logo-2.png");
        logoTextureTwo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image logoOne = new Image(logoTextureOne);
        Image logoTwo = new Image(logoTextureTwo);

        float width = 1024 * 0.13f;
        float height = 128 * 0.13f;

        logoOne.addAction(
                Actions.sequence(
                        Actions.moveBy(-3, 0, 2f),
                        Actions.moveBy(3, 0, 2f, Interpolation.circleOut)
                )
        );

        logoTwo.addAction(
                Actions.sequence(
                        Actions.moveBy(3, 0, 2f),
                        Actions.moveBy(-3, 0, 2f, Interpolation.circleOut)
                )
        );

        fullTable.add(logoOne).size(width, height).align(Align.center).fill(true, true);
        fullTable.row();
        fullTable.add(logoTwo).size(width, height).align(Align.center).fill(true, true);

        fullTable.addAction(
                Actions.sequence(
                        Actions.fadeOut(0.0001f),
                        Actions.delay(2f),
                        Actions.fadeIn(1f),
                        Actions.delay(1.5f),
                        Actions.fadeOut(1f),
                        Actions.delay(0.2f),
                        new Action() {
                            @Override
                            public boolean act(float delta) {
                                GameApplication.SetScreen(new SplashScreen());
                                return true;
                            }
                        }
                ));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(GL20.GL_DEPTH_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT);
        ui.draw();
    }

    @Override
    public void tick(float delta) {
        ui.act(delta);
    }
}
