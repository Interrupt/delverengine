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

    public class FoundTile {
        public Tile tile;
        public float floorHeight;

        public void set(Tile tile, float floorHeight) {
            this.tile = tile;
            this.floorHeight = floorHeight;
        }
    }

    FoundTile t_foundTile = new FoundTile();
    public FoundTile getHighestTile(Level level, float levelX, float levelY, Entity checking) {
        boolean first = true;

        t_foundTile.set(null, 0f);

        for(int x = -1; x <= 1; x += 2) {
            for(int y = -1; y <= 1; y += 2) {
                Tile t = level.getTile((int)(levelX + (x * checking.collision.x)), (int)(levelY + (y * checking.collision.y)));
                if(!t.blockMotion) {
                    float floorHeight = t.getFloorHeight(levelX + (x * checking.collision.x * 0.1f), levelY + (y * checking.collision.y * 0.1f));
                    if(floorHeight > t_foundTile.floorHeight || first) {
                        t_foundTile.set(t, floorHeight);
                        first = false;
                    }
                }
            }
        }

        return t_foundTile;
    }

    // Entity can ask for where to move next
    public PathNode GetNextPathLocation(Level level, Entity checking) {
        if(checking == null)
            return null;

        Player player = Game.instance.player;
        if(player == null)
            return null;

        Monster m = (Monster)checking;
        if(m == null)
            return null;

        // First, try to move towards the player
        Vector3 posCalc = new Vector3(checking.x + checking.xa, checking.y + checking.ya, checking.z + checking.za);
        Vector3 directionCalcFar = new Vector3(player.x, player.y, player.z).sub(posCalc).nor();
        Vector3 directionCalcNear = new Vector3(player.x, player.y, player.z).sub(posCalc).nor().scl(0.5f);

        // Check if we can actually walk towards this
        PathNode towardsPlayer = new PathNode();
        towardsPlayer.loc.set(posCalc.add(directionCalcNear));
        if(CanMoveTo(level, towardsPlayer.loc.x, towardsPlayer.loc.y, checking)) {
            towardsPlayer.loc.set(posCalc.add(directionCalcFar));
            if(CanMoveTo(level, towardsPlayer.loc.x, towardsPlayer.loc.y, checking))
                return towardsPlayer;
        }

        // Can't move towards the player, start random walking
        Vector3 dir = new Vector3(m.lastPathDirection);
        Vector2 monsterDir = new Vector2(dir.x, dir.y);

        Array<PathNode> availablePathNodes = new Array<>();
        for(int x = -1; x < 2; x++) {
            for(int y = -1; y < 2; y++) {
                // Don't add the origin point
                if(x == 0 && y== 0)
                    continue;

                float checkX = (int)(posCalc.x + x) + 0.5f;
                float checkY = (int)(posCalc.y + y) + 0.5f;

                if(!CanMoveTo(level, checkX, checkY, checking))
                    continue;

                PathNode p = new PathNode(new Vector3(checkX, checkY, checking.z));
                availablePathNodes.add(p);
            }
        }

        if(availablePathNodes.size == 0)
            return null;

        if(availablePathNodes.size == 1)
            return availablePathNodes.get(0);

        // More than one, try to pick a good one
        Array<PathNode> validPathNodes = new Array<>();
        for(int i = 0; i < availablePathNodes.size; i++) {
            PathNode toCheck = availablePathNodes.get(i);
            Vector2 newDir = new Vector2(toCheck.loc.x, toCheck.loc.y).sub(checking.x, checking.y).nor();
            float angle = monsterDir.angleDeg(newDir);

            // Don't turn around if able
            if(Math.abs(angle) < 160)
                validPathNodes.add(toCheck);
        }

        if(validPathNodes.size == 0) {
            // No path nodes that we can turn towards? Use a random one from the list of all instead
            return availablePathNodes.get(Game.rand.nextInt(availablePathNodes.size));
        }

        if(validPathNodes.size == 1)
            return validPathNodes.get(0);

        return validPathNodes.get(Game.rand.nextInt(validPathNodes.size));
    }

    public boolean CanMoveTo(Level level, float x, float y, Entity checking) {
        FoundTile at = getHighestTile(level, x, y, checking);
        Tile t = at.tile;

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

        float heightDifference = Math.abs(checking.z - at.floorHeight);
        if(heightDifference > StepHeight)
            return false;

        if(at.floorHeight < checking.z && heightDifference > FallHeight)
            return false;

        return true;
    }

    public void InitForLevel(Level level) {
        // add bridges
        //addNodesForStaticEntities(level, checking);
    }

    public void tick(float delta) {

    }
}
