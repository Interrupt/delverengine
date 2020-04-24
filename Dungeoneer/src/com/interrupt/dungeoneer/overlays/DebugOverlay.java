package com.interrupt.dungeoneer.overlays;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.*;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.ItemManager;

public class DebugOverlay extends WindowOverlay {

	final Player player;
	protected TextButton doneBtn;
	protected final Color selectedValue = new Color(0.6f, 1f, 0.6f, 1f);
	protected final Color unselectedValue = new Color(0.6f, 0.6f, 0.6f, 1f);

	public DebugOverlay(Player player) {
		this.player = player;
	}

	@Override
	public void onShow() {
		super.onShow();
	}

	@Override
	public void onHide() {
		super.onHide();
	}

	protected void addItem(Table table, final String text, final HashMap<String, Array<Monster>> value) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeLayout(makeContentFromMonsters(text, value));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addItems(Table table, final String text, final HashMap<String, Array<Item>> value) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeLayout(makeContentFromItems(text, value));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addMonsters(Table table, final String text, final Array<Monster> value) {

		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeLayout(makeContent(text, value));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addLevelUpItem(Table table, final String text) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				Game.instance.player.level++;
				OverlayManager.instance.push(new LevelUpOverlay(Game.instance.player));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addGoDownItem(Table table, final String text) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				Game.instance.level.down.changeLevel(Game.instance.level);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

		table.add(name).align(Align.left);

		table.row();
	}

    protected void addFlightItem(Table table, final String text) {
        final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

        final Overlay thisOverlay = this;

        name.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                OverlayManager.instance.remove(thisOverlay);
                Game.instance.player.floating = !Game.instance.player.floating;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                name.setStyle(skin.get("inputover", LabelStyle.class));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                name.setStyle(skin.get("input", LabelStyle.class));
            }
        });

        buttonOrder.add(name);

        table.add(name).align(Align.left);

        table.row();
    }

    protected void addNoClipItem(Table table, final String text) {
        final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

        final Overlay thisOverlay = this;

        name.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                OverlayManager.instance.remove(thisOverlay);
                Game.instance.player.isSolid = !Game.instance.player.isSolid;
                Game.instance.player.floating = !Game.instance.player.isSolid;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                name.setStyle(skin.get("inputover", LabelStyle.class));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                name.setStyle(skin.get("input", LabelStyle.class));
            }
        });

        buttonOrder.add(name);

        table.add(name).align(Align.left);

        table.row();
    }

	protected void addGodModeOption(Table table, final String text) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				Game.instance.player.godMode = !Game.instance.player.godMode;
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

		table.add(name).align(Align.left);

		table.row();
	}

	protected void addNoTargetOption(Table table, final String text) {
		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				Game.instance.player.invisible = !Game.instance.player.invisible;
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

		table.add(name).align(Align.left);

		table.row();
	}

    protected void addSuicideItem(Table table) {
        final Label name = new Label("DIE", skin.get("input", LabelStyle.class));

        final Overlay thisOverlay = this;

        name.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                OverlayManager.instance.remove(thisOverlay);
                Game.instance.player.die();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                name.setStyle(skin.get("inputover", LabelStyle.class));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                name.setStyle(skin.get("input", LabelStyle.class));
            }
        });

        buttonOrder.add(name);

        table.add(name).align(Align.left);

        table.row();
    }

	protected void addRefreshItem(Table table) {
		final Label name = new Label("REFRESH", skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				Art.refresh();
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

		table.add(name).align(Align.left);

		table.row();
	}

	protected void addItems(Table table, final String category, final String text, final Array<Item> items) {

		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				makeLayout(makeContentFromItems(category != "" ? category + "/" + text : text, items));
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addItem(Table table, final String text, Item item) {

		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		final Item value = item;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {

			    Item i = ItemManager.Copy(value.getClass(),value);
			    if(i != null) ItemManager.setItemLevel(Game.instance.player.level, i);

				Game.instance.player.dropItem(i, Game.instance.level, 0.2f);
				OverlayManager.instance.remove(thisOverlay);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	protected void addItem(Table table, final String text, Monster monster) {

		final Label name = new Label(text.toUpperCase(), skin.get("input", LabelStyle.class));

		final Overlay thisOverlay = this;

		final Monster value = monster;

		name.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Monster copy = Game.instance.monsterManager.Copy(value.getClass(),value);
				Player p = Game.instance.player;

				float projx = (0 * (float)Math.cos(p.rot) + 1 * (float)Math.sin(p.rot)) * 1;
				float projy = (1 * (float)Math.cos(p.rot) - 0 * (float)Math.sin(p.rot)) * 1;

				copy.isActive = true;
				copy.x = (p.x + 0.5f + projx * 2);
				copy.y = (p.y + 0.5f + projy * 2);
				copy.z = p.z + 0.35f;
				copy.xa = projx * (0.3f);
				copy.ya = projy * (0.3f);
				copy.za = 0.01f;
				//copy.hostile = false;
				copy.Init(Game.instance.level, player.level);

				Game.instance.level.entities.add(copy);

				OverlayManager.instance.remove(thisOverlay);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				name.setStyle(skin.get("inputover", LabelStyle.class));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				name.setStyle(skin.get("input", LabelStyle.class));
			}
		});

		buttonOrder.add(name);

    	table.add(name).align(Align.left);

		table.row();
	}

	@Override
	public Table makeContent() {

		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label("DEBUG!", skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    // wands
	    Array<Item> wands = new Array<Item>();
	    wands.addAll(Game.instance.itemManager.wands);

	    // food
	    Array<Item> food = new Array<Item>();
	    food.addAll(Game.instance.itemManager.food);

	    // scrolls
	    Array<Item> scrolls = new Array<Item>();
	    scrolls.addAll(Game.instance.itemManager.scrolls);

	    // potions
	    Array<Item> potions = new Array<Item>();
		potions.addAll(Game.instance.player.shuffledPotions);

        // uniques
        Array<Item> uniques = new Array<Item>();
        if(Game.instance.itemManager.unique != null) {
            uniques.addAll(Game.instance.itemManager.unique);
        }

	    // armor
	    HashMap<String, Array<Item>> armors = new HashMap<String, Array<Item>>();
	    for(java.util.Map.Entry<String, Array<Armor>> entry : Game.instance.itemManager.armor.entrySet()) {
	    	Array<Item> items = new Array<Item>();
	    	items.addAll(entry.getValue());
	    	armors.put(entry.getKey(), items);
	    }

	    // melee
	    HashMap<String, Array<Item>> melee = new HashMap<String, Array<Item>>();
	    for(java.util.Map.Entry<String, Array<Sword>> entry : Game.instance.itemManager.melee.entrySet()) {
	    	Array<Item> items = new Array<Item>();
	    	items.addAll(entry.getValue());
	    	melee.put(entry.getKey(), items);
	    }

	    // ranged
	    HashMap<String, Array<Item>> ranged = new HashMap<String, Array<Item>>();
	    for(java.util.Map.Entry<String, Array<Item>> entry : Game.instance.itemManager.ranged.entrySet()) {
	    	Array<Item> items = new Array<Item>();
	    	items.addAll(entry.getValue());
	    	ranged.put(entry.getKey(), items);
	    }

	    // junk
	    Array<Item> junk = new Array<Item>();
	    junk.addAll(Game.instance.itemManager.junk);

	    addItem(contentTable, "MONSTERS", Game.instance.monsterManager.monsters);
	    addItems(contentTable, "", "WANDS", wands);
	    addItems(contentTable, "ARMOR", armors);
	    addItems(contentTable, "MELEE", melee);
	    addItems(contentTable, "RANGED", ranged);
	    addItems(contentTable, "", "FOOD", food);
	    addItems(contentTable, "", "SCROLLS", scrolls);
	    addItems(contentTable, "", "POTIONS", potions);
        addItems(contentTable, "", "UNIQUES", uniques);
	    addItems(contentTable, "", "JUNK", junk);
	    addItem(contentTable, "ORB", new QuestItem());
		addItem(contentTable, "Gold", new Gold(200));

	    addLevelUpItem(contentTable, "LEVEL UP!");
        //addGoDownItem(contentTable, "DOWN");
        addFlightItem(contentTable, "TOGGLE FLIGHT");
        addNoClipItem(contentTable, "TOGGLE NOCLIP");
		addGodModeOption(contentTable, "TOGGLE GODMODE");
		addNoTargetOption(contentTable, "TOGGLE NOTARGET");
		addRefreshItem(contentTable);
        addSuicideItem(contentTable);

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}

	protected Table makeContentFromMonsters(String titleText, HashMap<String, Array<Monster>> objects) {

		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label(titleText, skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    for(java.util.Map.Entry<String, Array<Monster>> entry : objects.entrySet()) {
    		addMonsters(contentTable, entry.getKey(), entry.getValue());
	    }

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}

	protected Table makeContentFromItems(String titleText, HashMap<String, Array<Item>> objects) {

		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label(titleText, skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    for(java.util.Map.Entry<String, Array<Item>> entry : objects.entrySet()) {
    		addItems(contentTable, titleText, entry.getKey(), entry.getValue());
	    }

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}

	protected Table makeContent(Entity[] objects) {

		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label("DEBUG!", skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    for(Entity entry : objects) {
	    	if(entry instanceof Item) {
	    		addItem(contentTable, ((Item)entry).GetName(), (Item)entry);
	    	}
	    	else if(entry instanceof Monster) {
	    		addItem(contentTable, ((Monster)entry).name, (Monster)entry);
	    	}
	    }

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}

	protected Table makeContentFromItems(String titleText, Array<Item> objects) {
		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label(titleText, skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    for(Item entry : objects) {
    		addItem(contentTable, ((Item)entry).GetName(), (Item)entry);
	    }

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}

	protected Table makeContent(String titleText, Array<Monster> objects) {

		buttonOrder.clear();

		final Overlay thisOverlay = this;

		doneBtn = new TextButton("DONE", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);

		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();
	    Label title = new Label(titleText, skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();

	    for(Monster entry : objects) {
    		addItem(contentTable, entry.name, entry);
	    }

	    contentTable.add(doneBtn).padTop(4).align(Align.center).colspan(2);

	    buttonOrder.add(doneBtn);

	    return contentTable;
	}
}
