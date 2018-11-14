package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class Stairs extends Entity {
	public enum StairDirection { up, down };
	
	@EditorProperty
	public StairDirection direction;
	
	public Stairs() { artType = ArtType.sprite; }
	
	@EditorProperty
	public float exitRotation = 0;
	
	private transient boolean didInit = false;

	public Material tileMaterial = new Material("t1", (byte)8);

	public Stairs(StairDirection direction) {
		tex = 7;
		isDynamic = false;
		this.direction = direction;
		artType = ArtType.sprite;
		isSolid = false;
		collision.set(0.3f,0.3f,0.2f);
	}
	
	public Stairs(float x, float y, int tex, StairDirection direction) {
		super(x, y, tex, false);
		this.direction = direction;
		artType = ArtType.sprite;
		isSolid = false;
		collision.set(0.3f,0.3f,0.2f);
	}
	
	@Override
	public void tick(Level level, float delta)
	{
		// stairs don't do much
		if(!didInit) {
			color = new Color(level.GetLightmapAt(x, y, z));
			color.a = 1.0f;

			didInit = true;
		}
	}
	
	public void encroached(Player player)
	{
		if(player.ignoreStairs || Game.messageTimer > 1) return;
		
		if(Game.isMobile)
			Game.ShowMessage(MessageFormat.format(StringManager.get("entities.Stairs.mobileUseText"), direction.toString().toUpperCase()), 0.5f, 1f);
	}
	
	public void use(Player player, float projx, float projy)
	{	
		float pxdir = player.x - x;
		float pydir = player.y - y;
		float playerdist = GlRenderer.FastSqrt(pxdir * pxdir + pydir * pydir);
		
		if(playerdist > 1.1) return;
		
		changeLevel(Game.GetLevel());
		
		Game.ShowMessage("", 1);
	}

	public String getUseText() {
		return StringManager.get("entities.Stairs.direction." + this.direction.toString().toUpperCase());
	}

	public void changeLevel(Level level)
	{
		GameManager.getGame().changeLevel(this);
	}
	
	@Override
	public void rotate90() {
		if(direction == StairDirection.up)
			exitRotation -= 90;
		else
			exitRotation += 90;
	}
	
	@Override
	public void init(Level level, Source source) {
		super.init(level, source);
		
		if(direction == StairDirection.down) level.down = this;
		else if(direction == StairDirection.up) level.up = this;
	}
	
	@Override
	public void updateLight(Level level) {
		color = level.getLightColorAt(x, y, z + 0.08f, null, new Color());
	}
	
	@Override
	public void onTrigger(Entity instigator, String value) {
		changeLevel(Game.GetLevel());
	}
}
