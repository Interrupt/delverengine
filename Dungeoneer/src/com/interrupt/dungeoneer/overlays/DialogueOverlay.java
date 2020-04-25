package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bladecoder.ink.runtime.Choice;
import com.bladecoder.ink.runtime.Story;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;

public class DialogueOverlay extends MessageOverlay {

    public DialogueOverlay(String inkFile, NinePatchDrawable background, Color textColor) {
        super(new Array<String>(), background, textColor);

        inkStoryFile = inkFile;
    }

    public String inkStoryFile;

    protected String currentLine;

    // Current Ink story
    protected Story story;

    protected Table choicesTable;

    @Override
    public void tick(float delta) {
        super.tick(delta);
    }

    @Override
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

        String message = currentLine;

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

        messageText = new Label(message, skin.get(Label.LabelStyle.class));
        messageText.setColor(textColor);
        messageText.setWrap(true);
        messageText.setAlignment(Align.top | Align.left);

        thisText = messageText.getText().toString();

        mainTable.add(messageText).minWidth(200);
        mainTable.row();

        continueImage.setColor(textColor);

        animateText = true;

        // Also add the choice buttons, if any!
        if(story.getCurrentChoices().size() > 0) {
            // Add a spacer between the choices and the text
            mainTable.row().padTop(2f);

            // Create or clear the choices table
            if(choicesTable == null)
                choicesTable = new Table();
            else
                choicesTable.clear();

            choicesTable.setVisible(false);

            int choiceIndex = 0;
            for (Choice c : story.getCurrentChoices()) {
                String choiceText = c.getText();

                final Label choiceLabel = new Label(choiceText, skin.get(Label.LabelStyle.class));
                choiceLabel.setColor(Color.YELLOW);
                choiceLabel.setWrap(true);
                choiceLabel.setAlignment(Align.top | Align.left);

                final int internalChoiceIndex = choiceIndex;
                choiceLabel.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        try {
                            story.chooseChoiceIndex(internalChoiceIndex);
                            finishedShowingText = false;
                            next();
                        } catch (Exception ex) {
                            Gdx.app.log("DialogueOverlay", ex.getMessage());
                            next();
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        choiceLabel.setColor(Color.WHITE);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        choiceLabel.setColor(Color.YELLOW);
                    }
                });

                choicesTable.add(choiceLabel).minWidth(200);
                choicesTable.row();

                choiceIndex++;
            }

            mainTable.add(choicesTable);
            mainTable.row();
        } else {
            // Just add the continue icon
            mainTable.add(continueImage).align(Align.right);
        }
    }

    @Override
    public void onDoneAnimatingText() {
        // Show the choices, if any.
        if(choicesTable != null) {
            choicesTable.setVisible(true);
        }
    }

    public void bindExternalFunctions(Story story) {
        try {
            // Test out binding some functions
            story.bindExternalFunction("killPlayer", new Story.ExternalFunction() {
                @Override
                public Object call(Object[] args) throws Exception {
                    Gdx.app.log("Ink Function", "killPlayer");
                    Game.instance.player.hp = 0;
                    return null;
                }
            });

            story.bindExternalFunction("showGameOverScreen", new Story.ExternalFunction() {
                @Override
                public Object call(Object[] args) throws Exception {
                    Gdx.app.log("Ink Function", "showGameOverScreen");

                    Boolean wonGame = false;
                    if(args.length > 0) {
                        wonGame = (Integer)args[0] == 1;
                    }

                    GameApplication.ShowGameOverScreen(wonGame);
                    return wonGame;
                }
            });

            story.bindExternalFunction("fireTrigger", new Story.ExternalFunction() {
                @Override
                public Object call(Object[] args) throws Exception {
                    Gdx.app.log("Ink Function", "fireTrigger");

                    String triggerName = (String)args[0];
                    Game.GetLevel().trigger(Game.instance.player, triggerName, "");

                    return null;
                }
            });
        } catch(Exception ex) {
            Gdx.app.log("DialogueOverlayFunctions", ex.getMessage());
        }
    }

    @Override
    public void onShow() {
        try {
            FileHandle file = Game.findInternalFileInMods("data/" + inkStoryFile);
            if(!file.exists()) {
                Gdx.app.log("DialogueOverlay", "File " + inkStoryFile + " does not exist!");
                close();
            }

            String fileJson = file.readString();

            // Fixup some characters for Ink, will explode otherwise (https://github.com/bladecoder/blade-ink/issues/8)
            String BOM = new String("\uFEFF".getBytes("UTF-8"));
            fileJson = fileJson.replace(BOM, "");

            story = new Story(fileJson);
            bindExternalFunctions(story);
            processStory();
        } catch(Exception ex) {
            Gdx.app.log("DialogueOverlay", ex.getMessage());
        }

        super.onShow();
    }

    @Override
    public void next() {
        if(!finishedShowingText) {
            finishedShowingText = true;
            onDoneAnimatingText();
            return;
        }

        if(story.getCurrentChoices().size() > 0) {
            return;
        }

        if(!story.canContinue()) {
            this.close();
            return;
        }

        processStory();

        // Update the window
        makeMessage();
        resetGlyphLayout();

        Audio.playSound("/ui/ui_dialogue_next.mp3", 0.35f);
        finishedShowingText = false;
    }

    public void processStory() {
        try {
            // Get the next bit of the story
            currentLine = story.Continue();
        } catch(Exception ex) {
            Gdx.app.log("DialogueOverlay", ex.getMessage());
        }
    }

    protected void close() {
        Audio.playSound("/ui/ui_dialogue_close.mp3", 0.35f);
        if(afterAction != null) afterAction.act(0f);
        OverlayManager.instance.remove(this);
        Game.instance.input.clear();

        if(triggerOnClose != null) {
            Game.instance.level.trigger(Game.instance.player, triggerOnClose, "message");
        }
    }

    @Override
    public void draw(float delta) {
        super.draw(delta);
    }

    @Override
    public Table makeContent() {
        makeMessage();
        return mainTable;
    }
}
