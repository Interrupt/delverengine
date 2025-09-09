package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.*;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.helpers.Message;
import com.interrupt.managers.MessageManager;

public class MessageOverlay extends WindowOverlay {
	
	public float timer = 0;
	public float drawTimer = 0;
	public Color textColor = Color.WHITE;

    protected boolean finishedShowingText = false;
    protected int showingCharacters = -1;

    public Action afterAction = null;

	protected Table mainTable = null;

	float lastHeight = 0f;
    public String triggerOnClose;

    protected boolean animateText = true;

    public MessageOverlay(Array<String> message, NinePatchDrawable background, Color textColor) {
		if(background != null) this.background = background;
		if(textColor != null) this.textColor = textColor;
		messages.addAll(message);
	}
	
	public MessageOverlay(String messageFile, Player player, NinePatchDrawable background, Color textColor) {
		if(background != null) this.background = background;
		if(textColor != null) this.textColor = textColor;
		try {
			Message loaded = MessageManager.getMessage(messageFile);
			
			int views = player.getMessageViews(messageFile);
			
			if(loaded.repeats)
				views %= loaded.messages.size;
			else if(views > loaded.messages.size - 1)
				views = loaded.messages.size - 1;
			
			messages.addAll(loaded.messages.get(Math.round(views)));
			
			player.setMessageViews(messageFile, views + 1);
		}
		catch(Exception ex) {
			messages.add("error loading " + messageFile);
		}
	}
	
	protected final Array<String> messages = new Array<String>();
	protected int index = 0;

	Label messageText = null;
	CharSequence thisText = "";

	Image continueImage = new Image(new TextureRegion(Art.loadTexture("ui/continue-icon.png")));
    GlyphLayout glyphLayout = null;

	public GlyphLayout getTextGlyphs() {
	    if(glyphLayout == null) {
            glyphLayout = new GlyphLayout();
            glyphLayout.setText(UiSkin.getFont(), thisText, textColor, 200, 0, true);
        }
        return glyphLayout;
    }

    public void resetGlyphLayout() {
	    glyphLayout = null;
    }

	public void onDoneAnimatingText() {

	}

	@Override
	public void tick(float delta) {

		// Keyboard next
		if(Gdx.input.isKeyJustPressed(Keys.SPACE) || Gdx.input.isKeyJustPressed(Keys.ENTER))
		{
			next();
		}

		// Gamepad next
		if(controllerState.buttonEvents.size > 0) {
			next();
		}

		Game.instance.input.tick();

		boolean shouldContinue = Game.instance.input.isActionRequested(Actions.Action.ATTACK) ||
				Game.instance.input.isActionRequested(Actions.Action.USE);

		if(shouldContinue) {
			next();
		}

		// Tick message
		timer += delta;

		if(animateText) {
			String text = "";
			int maxCharacters = (int) (timer * 40f);

			if (finishedShowingText) maxCharacters = thisText.length();

			if (maxCharacters != showingCharacters) {
				GlyphLayout glyphLayout = getTextGlyphs();

				int charCount = 0;
				for (GlyphLayout.GlyphRun run : glyphLayout.runs) {
					if (!text.equals("")) text += "\n";
					for (BitmapFont.Glyph g : run.glyphs) {
						if (charCount < maxCharacters) {
							text += g.toString();
							charCount++;
						}
					}
				}

				messageText.setText(text);

				Cell c = mainTable.getCell(messageText);
				if (c != null) {
					float lerp = Interpolation.circleOut.apply(lastHeight, glyphLayout.height, Math.min(timer * 2.25f, 1.0f)) + 4;
					c.height((int) lerp);
				}
			}

			if (!finishedShowingText) {
				finishedShowingText = ((int) (timer * 40f) - 5) >= thisText.length();

				if(finishedShowingText) onDoneAnimatingText();
			}
		}

        // animate!
        if(finishedShowingText) {
            drawTimer += delta;
            float imageY = 0;
            if (Math.sin(drawTimer * 10f) > 0f) imageY = -1;
            if (continueImage.getY() != imageY) continueImage.setY(imageY);
        }
		
		super.tick(delta);
	}

	@Override
	public void onShow() {
		// Don't play the default window open sound
		playSoundOnOpen = false;

		super.onShow();
		
		InputListener listener = new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				return false;
			}
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
			
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				next();
			}
		};
		
		ui.addListener(listener);
		
		Audio.playSound("/ui/ui_dialogue_open.mp3", 0.35f);

		tick(0);
	}
	
	@Override
	public void draw(float delta) {
		super.draw(delta);
	}

	public void next() {
	    if(!finishedShowingText) {
            finishedShowingText = true;
            return;
        }

        final Overlay thisOverlay = this;
        if(index + 1 < messages.size) {
            index = index + 1;
			makeMessage();
            Audio.playSound("/ui/ui_dialogue_next.mp3", 0.35f);
        }
        else {
            Audio.playSound("/ui/ui_dialogue_close.mp3", 0.35f);
			if(afterAction != null) afterAction.act(0f);
            OverlayManager.instance.remove(thisOverlay);
			Game.instance.input.clear();

			if(triggerOnClose != null) {
				Game.instance.level.trigger(Game.instance.player, triggerOnClose, "message");
			}
        }

        resetGlyphLayout();
        finishedShowingText = false;
    }

    public void makeMessage() {
		if(mainTable == null) {
			mainTable = new Table();
		}
		else {

			Cell c = mainTable.getCell(messageText);
			if(c != null) {
				lastHeight = c.getMinHeight();
			}

			mainTable.clear();
		}

		timer = 0;
		drawTimer = 0;

		String message = messages.get(index);

		if(message.startsWith("[image:") && message.endsWith("]")) {
			String imageFile = message.substring(7);
			imageFile = imageFile.substring(0, imageFile.length() - 1);

			thisText = " ";
			finishedShowingText = true;

			Texture t = Art.loadTexture(imageFile);
			if(t != null) {
				Image i = new Image(t);

				float scale = 200f / t.getWidth();
				i.setWidth(t.getWidth() * scale);
				i.setHeight(t.getHeight() * scale);

				mainTable.add(i).width(200).height(t.getHeight() * scale);
				mainTable.row();
			}

			continueImage.setColor(textColor);
			mainTable.add(continueImage).align(Align.right);

			animateText = false;

			return;
		}

		messageText = new Label(message, skin.get(LabelStyle.class));
		messageText.setColor(textColor);
		messageText.setWrap(true);
		messageText.setAlignment(Align.top | Align.left);

		thisText = messageText.getText().toString();

		mainTable.add(messageText).minWidth(200);
		mainTable.row();

		continueImage.setColor(textColor);
		mainTable.add(continueImage).align(Align.right);

		animateText = true;
	}

	@Override
	public Table makeContent() {
		makeMessage();
	    return mainTable;
	}
}
