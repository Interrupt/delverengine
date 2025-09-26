package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

public class Ladder extends Model {
    public Ladder() { meshFile = "meshes/plane.obj"; textureFile = "textures/wood-ladder.png"; collision.set(0.3f, 0.04f, 1f); isSolid = true; }

    private transient Vector3 climbArea = new Vector3();

    @Override
    public void init(Level level, Level.Source source) {
        climbArea = new Vector3(collision);
        climbArea.add(0.1f, 0.1f, 0f);
    }

    @Override
    public void tick(Level level, float delta)
    {
        Array<Entity> touching = level.getEntitiesColliding(x, y, z, climbArea, this);
        for(Entity e : touching) {
            if(e instanceof Player) {
                ((Player)e).isOnLadder = true;
            }
        }
    }
}
