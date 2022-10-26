package com.interrupt.dungeoneer.game.pathfinding;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.PathNode;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.WorldChunk;
import com.interrupt.dungeoneer.partitioning.SpatialHash;
import com.interrupt.dungeoneer.tiles.Tile;

public class PathNodesBuilder {
    public float scale = 1;
    public float offset = 0.25f;

    public int width = 0;
    public int height = 0;

    private PathNode[] nodes = null;
    private boolean[] failed_collision  = null;

    private Array<PathNode> allNodes = new Array<PathNode>();
    public SpatialHash pathfindingSpatialHash = new SpatialHash(2);

    private static final float StepHeight = 0.35f;
    private static final float FallHeight = 3f;

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

    Array<Entity> t_entityCheckerArray = new Array<>();
    protected boolean collidesWorldOrEntities(Level level, float x, float y, float z, Vector3 collision, Entity checking) {
        boolean isFreeSoFar = level.isFree(x, y, z, collision, checking.stepHeight, checking.floating, null);
        if(!isFreeSoFar)
            return false;

        float widthX = checking.collision.x;
        float widthY = checking.collision.y;

        // The level entity collision checks are not thread safe by default.
        Array<Entity> foundEntities = level.staticSpatialhash.getEntitiesAt(x,y,checking.collision.x,t_entityCheckerArray);
        for(int i = 0; i < foundEntities.size; i++)
        {
            Entity e = foundEntities.get(i);
            if(e.isSolid && e != checking && e.isActive)
            {
                if(e.isDynamic && checking.collidesWith == Entity.CollidesWith.staticOnly) continue;
                else if(e.collidesWith == Entity.CollidesWith.staticOnly && checking.isDynamic) continue;

                // simple AABB test
                if(x > e.x - e.collision.x - widthX) {
                    if(x < e.x + e.collision.x + widthX) {
                        if(y > e.y - e.collision.y - widthY) {
                            if(y < e.y + e.collision.y + widthY) {
                                if(z > e.z - height && z < e.z + e.collision.z) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public PathNode addNodeIfFree(Level level, int x, int y, float levelX, float levelY, Entity checking, Vector3 collision, int pass) {
        FoundTile at = getHighestTile(level, levelX, levelY, checking);
        Tile t = at.tile;

        if(t == null)
            return null;

        if(t.blockMotion)
            return null;

        if(!t.hasRoomFor(collision.z))
            return null;

        at.floorHeight += 0.5f;

        if(t.data != null) {
            // Don't make paths in pits
            if(t.data.hurts > 0)
                return null;

            // Raise floor height for water
            if(t.data.isWater)
                at.floorHeight += 0.4f;
        }

        boolean isFree = collidesWorldOrEntities(level, levelX, levelY, at.floorHeight, collision, checking);
        if(isFree) {
            PathNode existing = GetNode(x, y);

            if(pass > 0 && adjacentNodeFailedCollision(x, y)) {
                existing = null;
            }

            if(existing == null) {
                PathNode newNode = new PathNode(new Vector3(levelX, levelY, at.floorHeight));
                nodes[x + y * width] = newNode;
                return newNode;
            }
        }
        else if(pass == 0) {
            failed_collision[x + y * width] = true;
        }

        return null;
    }

    public boolean adjacentNodeFailedCollision(int x, int y) {
        boolean anyFailedCollision = false;

        for(int tx = x - 1; tx <= x + 1; tx++) {
            for(int ty = y - 1; ty <= y + 1; ty++) {
                anyFailedCollision |= NodeFailedCollisionCheck(tx, ty);
            }
        }

        return anyFailedCollision;
    }

    private PathNode AddNode(PathNode n) {
        allNodes.add(n);
        pathfindingSpatialHash.AddEntity(n);
        return n;
    }

    private PathNode GetNode(int x, int y) {
        if(x < 0 || y < 0)
            return null;
        if(x >= width || y >= height)
            return null;

        return nodes[x + y * width];
    }

    private void reduce() {
        for(int i = 0; i < nodes.length; i++) {
            PathNode n = nodes[i];
            if(n == null)
                continue;

            for(int ii = 0; ii < nodes.length; ii++) {
                PathNode other = nodes[ii];
                if(other == null)
                    continue;

                if(n != other && n.loc.equals(other.loc)) {
                    // merge!
                    nodes[ii] = n;
                }
            }
        }
    }

    public boolean NodeFailedCollisionCheck(int x, int y) {
        if(x < 0 || y < 0)
            return false;
        if(x >= width || y >= height)
            return false;

        return failed_collision[x + y * width];
    }

    private void addNodesForStaticEntities(Level level, Entity checking, WorldChunk chunk) {

        Vector3 nodeOffset = new Vector3();
        Vector3 distCheck = new Vector3();
        Array<Entity> staticEntities = chunk.GetStaticEntities();

        // Add nodes on top of bridges
        for(int i = 0; i < staticEntities.size; i++) {
            Entity e = staticEntities.get(i);
            if(e.collision.x >= 0.4f || e.collision.y >= 0.4f) {
                boolean isFree = collidesWorldOrEntities(level, e.x, e.y, e.z + e.collision.z, checking.collision, checking);
                if(isFree) {
                    PathNode n = new PathNode(new Vector3(e.x, e.y, e.z + e.collision.z));

                    Array<Entity> nearby = pathfindingSpatialHash.getEntitiesAt(e.x, e.y, 2f);
                    for (int ii = 0; ii < nearby.size; ii++) {
                        PathNode a = (PathNode)nearby.get(ii);
                        nodeOffset.set(0f, 0f, 0f);

                        if(Math.abs(a.loc.x - n.loc.x) >= e.collision.x) {
                            nodeOffset.x = n.loc.x < a.loc.x ? -e.collision.x : e.collision.x;
                        }
                        if(Math.abs(a.loc.y - n.loc.y) >= e.collision.y) {
                            nodeOffset.y = n.loc.y < a.loc.y ? -e.collision.y : e.collision.y;
                        }

                        float zLen = Math.abs(a.loc.z - n.loc.z + nodeOffset.z);
                        float distance = distCheck.set(a.loc).add(nodeOffset).sub(n.loc).len();

                        if(distance <= 0.6f && distance > 0.1f) {
                            if (zLen < 0.3f) {
                                n.addConnectionIfPossible(a, StepHeight, FallHeight);
                                if (a.addConnectionIfPossible(n, StepHeight, FallHeight)) {
                                    // probably can't jump down from here anymore
                                    a.getJumps().clear();
                                }
                            }
                        }
                    }

                    AddNode(n);
                }
            }
        }
    }

    public static Runnable GetPathNodeBuilderRunnable(WorldChunk chunk) {
        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                if(Game.instance == null)
                    return;

                Player p = Game.instance.player;
                if(p == null)
                    return;

                PathNodesBuilder builder = new PathNodesBuilder();
                builder.buildNodesForChunk(chunk);
            }
        };

        return runnable;
    }

    public void buildNodesForChunk(WorldChunk chunk) {
        scale = 2f;
        offset = 0.5f;
        Game.drawDebugBoxes = true;

        Level level = Game.GetLevel();

        int chunkX = chunk.getX();
        int chunkY = chunk.getY();
        int chunkWidth = chunk.getWidth();
        int chunkHeight = chunk.getHeight();

        // Check if this area has any empty tiles at all
        boolean foundEmptyTile = false;
        for(int x = chunk.getX(); x < chunkX + chunkWidth && !foundEmptyTile; x++) {
            for(int y = chunk.getY(); y < chunkY + chunkHeight && !foundEmptyTile; y++) {
                Tile t = level.getTileOrNull(x, y);
                if(t != null && !t.blockMotion)
                    foundEmptyTile = true;
            }
        }

        if(!foundEmptyTile)
            return;

        width = chunk.getWidth() * (int) scale;
        height = chunk.getHeight() * (int) scale;

        // arrays for building
        nodes = new PathNode[width * height];
        failed_collision = new boolean[width * height];

        // final arrays
        allNodes.clear();
        pathfindingSpatialHash.Clear();

        Vector3 checkCollision = new Vector3(0.26f, 0.26f, 0.65f);
        Vector3 checkCollisionLarge = new Vector3(0.499f, 0.499f, 0.65f);

        Entity checking = new Entity();
        checking.collidesWith = Entity.CollidesWith.staticOnly;
        checking.stepHeight = 0.3f;
        checking.isSolid = true;
        checking.collision.set(checkCollision);
        checking.floating = false;

        // Start coarse grained
        for (int lx = 0; lx < chunkWidth; lx++) {
            for (int ly = 0; ly < chunkHeight; ly++) {
                float locX = lx + 0.5f + chunkX;
                float locY = ly + 0.5f + chunkY;

                int px = (int)((lx + 0.5f) * scale - offset);
                int py = (int)((ly + 0.5f) * scale - offset);

                failed_collision[px + py * width] = false;

                addNodeIfFree(level, px, py, locX, locY, checking, checkCollisionLarge, 0);
                for (int x = px; x < px + scale; x++) {
                    for (int y = py; y < py + scale; y++) {
                        if(x > 0 && y > 0 && x < width && y < height) {
                            nodes[x + y * width] = nodes[px + py * width];
                            failed_collision[x + y * width] = failed_collision[px + py * width];
                        }
                    }
                }
            }
        }

        // Now go smaller
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                float levelX = (x / scale + offset) + chunkX;
                float levelY = (y / scale + offset) + chunkY;
                addNodeIfFree(level, x, y, levelX, levelY, checking, checking.collision, 1);
            }
        }

        reduce();

        // add connections
        for(int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                PathNode cur = GetNode(x, y);

                if(cur != null) {
                    cur = AddNode(cur);

                    PathNode east = GetNode(x + 1, y);
                    PathNode west = GetNode(x - 1, y);
                    PathNode north = GetNode(x, y - 1);
                    PathNode south = GetNode(x, y + 1);

                    if(east != null && east != cur)
                        cur.addConnectionIfPossible(east, StepHeight, FallHeight);

                    if(west != null && west != cur)
                        cur.addConnectionIfPossible(west, StepHeight, FallHeight);

                    if(north != null && north != cur)
                        cur.addConnectionIfPossible(north, StepHeight, FallHeight);

                    if(south != null && south != cur)
                        cur.addConnectionIfPossible(south, StepHeight, FallHeight);
                }
            }
        }

        // add bridges
        addNodesForStaticEntities(level, checking, chunk);

        // Add all of our path nodes to the global path node tree
        Game.pathfinding.AddNodes(allNodes);

        // Fix up the edges!
        for(int x = 0; x < chunkWidth; x++) {
            Game.pathfinding.LinkNodesAt(level,chunkX + x, chunkY, 0);
            Game.pathfinding.LinkNodesAt(level,chunkX + x, chunkY + chunkHeight, 0);
        }
        for(int y = 0; y < chunkHeight; y++) {
            Game.pathfinding.LinkNodesAt(level, chunkX, chunkY + y, 0);
            Game.pathfinding.LinkNodesAt(level, chunkX + chunkWidth, chunkY + y, 0);
        }

        failed_collision = null;
    }
}
