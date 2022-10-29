package com.interrupt.dungeoneer.game.pathfinding;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;

public interface PathfindingInterface {
    void tick(float delta);
    void initForLevel(Level level);
    Vector3 getNextPathToTarget(Level level, Entity checking, Entity target);
}
