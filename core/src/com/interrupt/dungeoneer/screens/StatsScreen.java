package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class StatsScreen extends BaseScreen {

    protected int statNumber = 0;

    public StatsScreen() { }

    Color statLabelColor = new Color(0.6f, 0, 0, 1);
	
	public void makeStats(Integer progress, boolean doFade) {
		ui.clear();
		
		// display some stats
		Table mainTable = new Table();
		mainTable.setFillParent(true);

		int goldThisRun = Game.instance.player.gold - Game.instance.progression.goldAtStartOfRun;
		
		Table innerTable = new Table();
		if(progress > 0) makeStat(innerTable, StringManager.get("screens.GameOverScreen.playtimeStatLabel"), Game.instance.player.getPlaytime(), doFade);
		if(progress > 1) makeStat(innerTable, StringManager.get("screens.GameOverScreen.goldStatLabel"), goldThisRun, doFade);
		if(progress > 2) makeStat(innerTable, StringManager.get("screens.GameOverScreen.killsStatLabel"), Game.instance.player.history.monstersKilled, doFade);
		if(progress > 3) makeStat(innerTable, StringManager.get("screens.GameOverScreen.damageStatLabel"), Game.instance.player.history.damageTaken, doFade);
		if(progress > 4) makeStat(innerTable, StringManager.get("screens.GameOverScreen.potionsStatLabel"), Game.instance.player.history.potionsDrank, doFade);
		if(progress > 5) makeStat(innerTable, StringManager.get("screens.GameOverScreen.foodStatLabel"), Game.instance.player.history.foodEaten, doFade);
		if(progress > 6) makeStat(innerTable, StringManager.get("screens.GameOverScreen.scrollsStatLabel"), Game.instance.player.history.scrollsUsed, doFade);
		if(progress > 7) makeStat(innerTable, StringManager.get("screens.GameOverScreen.trapsStatLabel"), Game.instance.player.history.trapsActivated, doFade);
        if(progress > 8) makeStat(innerTable, StringManager.get("screens.GameOverScreen.secretsStatLabel"), Game.instance.player.history.secretsFound, doFade);
		
		mainTable.add(innerTable);
		ui.addActor(mainTable);
        ui.act(0.0001f);
	}

    public void makeStat(Table table, String text, Integer value, boolean doFade) {
		if(value == null)
			return;
		makeStat(table, text, value.toString(), doFade);
    }

	public void makeStat(Table table, String text, String value, boolean doFade) {
		if(value == null)
			return;

		Label name = new Label(text, skin.get(LabelStyle.class));
		name.setColor(statLabelColor);
		name.setFontScale(1f);

		Label val = new Label(value, skin.get(LabelStyle.class));
		val.setColor(1, 1, 1, 1);
		val.setFontScale(1f);

		table.add(name).align(Align.left).padBottom(6);
		table.add(val).align(Align.left).padBottom(6);
		table.row();

		float delay = 0.35f;
		float delayTime = statNumber * delay;

		if(doFade) {
			name.addAction(Actions.sequence(Actions.fadeOut(0.00001f), Actions.delay(delayTime), Actions.fadeIn(0.2f)));
			val.addAction(Actions.sequence(Actions.fadeOut(0.00001f), Actions.delay(delayTime + delay * 0.3f), Actions.fadeIn(0.2f)));
		}

		statNumber++;
	}
}
