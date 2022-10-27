package com.interrupt.dungeoneer.game.pathfinding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.PathNode;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.partitioning.SpatialHash;
import com.interrupt.dungeoneer.tiles.Tile;

public class Pathfinding {

    public Pathfinding() { }

    private static final float StepHeight = 0.35f;
    private static final float FallHeight = 1.5f;

    Vector2 t_calcVec1 = new Vector2();
    Vector2 t_calcVec2 = new Vector2();

    public class FoundTile {
        public Tile tile;
        public float floorHeight;
        public float lowestHeight;

        public void set(Tile tile, float highest, float lowest) {
            this.tile = tile;
            this.floorHeight = highest;
            this.lowestHeight = lowest;
        }
    }

    FoundTile t_foundTile = new FoundTile();
    public FoundTile getHighestTile(Level level, float levelX, float levelY, Entity checking) {
        boolean first = true;

        t_foundTile.set(null, 0f, Float.MAX_VALUE);

        for(int x = -1; x <= 1; x += 2) {
            for(int y = -1; y <= 1; y += 2) {
                Tile t = level.getTile((int)(levelX + (x * checking.collision.x * 0.5f)), (int)(levelY + (y * checking.collision.y * 0.5f)));
                if(!t.blockMotion) {
                    float floorHeight = t.getFloorHeight(levelX + (x * checking.collision.x * 0.1f), levelY + (y * checking.collision.y * 0.1f));
                    if(floorHeight > t_foundTile.floorHeight || first) {
                        t_foundTile.tile = t;
                        t_foundTile.floorHeight = floorHeight;
                        first = false;
                    }
                    t_foundTile.lowestHeight = Math.min(floorHeight, t_foundTile.lowestHeight);
                }
            }
        }

        return t_foundTile;
    }

    public boolean TryTurn(Level level, Monster m, float angle) {
        Vector3 monsterDir = m.getDirection();
        Vector2 nextDir = t_calcVec1.set(monsterDir.x, monsterDir.y).scl(0.5f);
        nextDir.rotateDeg(angle);

        Vector2 nextPos = t_calcVec2.set(m.x, m.y).add(nextDir);
        return CanMoveTo(level, nextPos.x, nextPos.y, m);
    }

    float getAngleTowards(Vector3 direction, Entity checking, Player player) {
        // Check if we can do this turn! Don't just flip 180
        Vector2 nextDir = t_vec2Calc1.set(direction.x, direction.y).scl(-1f);
        Vector2 testCheckDir = t_vec2Calc2.set(checking.x - player.x, checking.y - player.y);

        float checkAngle = testCheckDir.angleDeg(nextDir);
        if(checkAngle > 180)
            checkAngle -= 360;

        return checkAngle;
    }

    // Entity can ask for where to move next
    Entity t_fakeChecker = new Entity();
    Vector3 t_vec3Calc1 = new Vector3();
    Vector3 t_vec3Calc2 = new Vector3();
    Vector3 t_vec3Calc3 = new Vector3();
    Vector2 t_vec2Calc1 = new Vector2();
    Vector2 t_vec2Calc2 = new Vector2();
    PathNode towardsPlayer = new PathNode();
    public PathNode GetNextPathLocation(Level level, Entity checking) {
        if(checking == null)
            return null;

        Player player = Game.instance.player;
        if(player == null)
            return null;

        Monster m = (Monster)checking;
        if(m == null)
            return null;

        Vector3 monsterDir = m.getDirection();

        // First, try to move towards the player
        Vector3 posCalc = t_vec3Calc1.set(checking.x, checking.y, checking.z);
        Vector3 directionCalcNear = t_vec3Calc2.set(player.x, player.y, player.z).sub(posCalc).nor().scl(0.5f);
        Vector3 nearCheckPos = t_vec3Calc3.set(posCalc).add(directionCalcNear);

        t_fakeChecker.collision.set(checking.collision);
        t_fakeChecker.collidesWith = Entity.CollidesWith.staticOnly;
        t_fakeChecker.stepHeight = 0.3f;
        t_fakeChecker.isSolid = true;
        t_fakeChecker.x = checking.x;
        t_fakeChecker.y = checking.y;
        t_fakeChecker.z = checking.z;

        // Check if we can actually walk towards the player
        towardsPlayer.loc.set(nearCheckPos);

        boolean couldMoveTowardsPlayer = false;

        if(CanMoveTo(level, towardsPlayer.loc.x, towardsPlayer.loc.y, t_fakeChecker)) {
            couldMoveTowardsPlayer = true;
        }

        float angleToTarget = getAngleTowards(monsterDir, checking, player);

        if(couldMoveTowardsPlayer) {
            // If this is the first tick being alerted, use this direction always
            if(!m.wasAlerted)
                return towardsPlayer;

            // TODO: Removing this check effectively gives the monster a turning radius.
            // Maybe we want that for some monsters?
            if(Math.abs(angleToTarget) <= 110)
                return towardsPlayer;
        }

        // Try turning until we get free space
        boolean turnDirection = angleToTarget > 0;
        for(int angle = 15; angle <= 270; angle += 30) {
            float finalAngle = turnDirection ? angle : -angle;
            boolean canTurn = TryTurn(level, m, finalAngle);
            if(!canTurn) {
                // Now try the other direction
                finalAngle *= -1;
                canTurn = TryTurn(level, m, finalAngle);
            }

            if(canTurn) {
                Vector2 nextDir = t_vec2Calc1.set(monsterDir.x, monsterDir.y).scl(0.5f);
                nextDir.rotateDeg(finalAngle);

                Vector2 nextPos = t_vec2Calc2.set(checking.x, checking.y).add(nextDir);
                towardsPlayer.loc.set(nextPos.x, nextPos.y, 0);
                return towardsPlayer;
            }
        }

        return null;
    }

    public boolean CanMoveTo(Level level, float x, float y, Entity checking) {
        FoundTile at = getHighestTile(level, x, y, checking);
        Tile t = at.tile;

        float stepDistance = Math.abs(at.lowestHeight - checking.z + 0.5f);
        if(at.lowestHeight < checking.z + 0.5f && stepDistance > StepHeight) {
            // Don't step off of edges!
            // TODO: Check if the player is close enough and below. If so, step down!
            // Unless there is a static entity to step on!
            if(!isBridgeAt(level, x, y, checking))
                return false;
        }

        if(t == null)
            return false;

        if(!t.hasRoomFor(checking.collision.z))
            return false;

        at.floorHeight += 0.5f;

        if(t.data != null) {
            // Don't make paths in pits
            if(t.data.hurts > 0)
                return false;

            // Raise floor height for water
            if(t.data.isWater)
                at.floorHeight += 0.4f;
        }

        if(t.blockMotion)
            return false;

        boolean isFree = level.collidesWorldOrEntities(x, y, at.floorHeight, checking.collision, checking);
        if(!isFree)
            return false;

        float heightDifference = at.floorHeight - checking.z;
        if(heightDifference > StepHeight)
            return false;

        return true;
    }

    public boolean isBridgeAt(Level level, float x, float y, Entity checking) {
        Array<Entity> bridges = level.staticSpatialhash.getEntitiesAt(x, y, checking.collision.x);
        for(int i = 0; i < bridges.size; i++) {
            Entity e = bridges.get(i);
            if(Math.abs(x - e.x) > e.collision.x || Math.abs(y - e.y) > e.collision.y)
                continue;

            if(e.collision.x < 0.4f && e.collision.y < 0.4f)
                continue;

            if(Math.abs(e.z + e.collision.z - checking.z) < StepHeight)
                return true;
        }

        return false;
    }

    public void InitForLevel(Level level) {
        // add bridges
        //addNodesForStaticEntities(level, checking);
    }

    public void tick(float delta) {

    }
}
