package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.PathNode;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.partitioning.SpatialHash;
import com.interrupt.dungeoneer.tiles.Tile;

public class Pathfinding {

    public Pathfinding() { }

    public static final int MaxTraversal = 16;
    public static final float PathUpdateTime = 35f;

    private static final float StepHeight = 0.35f;
    private static final float FallHeight = 3f;

    public class FoundTile {
        public Tile tile;
        public float floorHeight;

        public void set(Tile tile, float floorHeight) {
            this.tile = tile;
            this.floorHeight = floorHeight;
        }
    }

    public float scale = 1;
    public float offset = 0.25f;

    public int width = 0;
    public int height = 0;

    private PathNode[] nodes = null;
    private boolean[] failed_collision  = null;

    private Array<PathNode> allNodes = new Array<PathNode>();
    private SpatialHash pathfindingSpatialHash = new SpatialHash(2);

    private float timeSinceLastPathUpdate = 1000f;

    private final Integer PathfindingLock = 0;
    private Vector3 lastPathfindingUpdateLoc = new Vector3();

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

    public PathNode addNodeIfFree(Level level, int x, int y, float levelX, float levelY, Entity checking, Vector3 collision, int pass) {
        FoundTile at = getHighestTile(level, levelX, levelY, checking);
        Tile t = at.tile;

        if(t == null)
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

        if(!t.blockMotion) {
            boolean isFree = level.collidesWorldOrEntities(levelX, levelY, at.floorHeight, collision, checking);
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

    public void InitForLevel(Level level) {
        scale = 2f;
        offset = 0.5f;

        width = level.width * (int) scale;
        height = level.height * (int) scale;

        timeSinceLastPathUpdate = PathUpdateTime + 1;
        lastPathfindingUpdateLoc.set(Game.rand.nextFloat() * 100000f, Game.rand.nextFloat() * 100000f, Game.rand.nextFloat() * 100000f);

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
        for (int lx = 0; lx < level.width; lx++) {
            for (int ly = 0; ly < level.height; ly++) {
                float locX = lx + 0.5f;
                float locY = ly + 0.5f;

                int px = (int)(locX * scale - offset);
                int py = (int)(locY * scale - offset);

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
                float levelX = x / scale + offset;
                float levelY = y / scale + offset;
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
        addNodesForStaticEntities(level, checking);

        failed_collision = null;
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

    private void addNodesForStaticEntities(Level level, Entity checking) {

        Vector3 nodeOffset = new Vector3();
        Vector3 distCheck = new Vector3();

        // Add nodes on top of bridges
        for(int i = 0; i < level.static_entities.size; i++) {

            Entity e = level.static_entities.get(i);
            if(e.collision.x >= 0.4f || e.collision.y >= 0.4f) {

                boolean isFree = level.collidesWorldOrEntities(e.x, e.y, e.z + e.collision.z, checking.collision, checking);
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

    public boolean NodeFailedCollisionCheck(int x, int y) {
        if(x < 0 || y < 0)
            return false;
        if(x >= width || y >= height)
            return false;

        return failed_collision[x + y * width];
    }

    Vector3 lengthChecker = new Vector3();
    public PathNode GetNodeAt(float levelX, float levelY, float levelZ) {
        synchronized (this) {
            try {
                Array<Entity> nearby = pathfindingSpatialHash.getEntitiesAt(levelX, levelY, 1f);

                PathNode picked = null;
                float distance = 0f;

                // Find the closest node
                for (int i = 0; i < nearby.size; i++) {
                    PathNode c = (PathNode) nearby.get(i);

                    if (c == null)
                        continue;

                    float d = lengthChecker.set(levelX, levelY, levelZ).sub(c.loc.x, c.loc.y, c.loc.z).len();

                    if (picked == null || d < distance) {
                        distance = d;
                        picked = c;
                    }
                }

                return picked;
            }
            catch(Exception ex) {
                Gdx.app.log("Pathing: GetNode", ex.getMessage());
                return null;
            }
        }
    }

    private Vector3 t_updatePathing = new Vector3();
    public void updatePlayerPathing(Player player) {
        synchronized (PathfindingLock) {
            try {

                float distanceSinceLast = t_updatePathing.set(lastPathfindingUpdateLoc).sub(player.x, player.y, player.z).len();
                if(distanceSinceLast <= 0.2f) {
                    // player hasn't moved that far, can probably skip this
                    return;
                }

                timeSinceLastPathUpdate = 0f;
                PathNode pnode = GetNodeAt(player.x, player.y, player.z);

                for (int i = 0; i < allNodes.size; i++) {
                    PathNode n = allNodes.get(i);
                    if (n != null) {
                        n.reset();
                    }
                }

                if (pnode != null) {
                    pnode.walk(0, null);
                }

                //Gdx.app.log("Pathing", "Updated pathing");
                lastPathfindingUpdateLoc.set(player.x, player.y, player.z);

            } catch (Exception ex) {
                Gdx.app.log("Pathing: updatePlayerPathing", ex.getMessage());
            }
        }
    }

    Runnable t_runnable = null;
    public Runnable GetRunnable() {
        if(t_runnable != null)
            return t_runnable;

        t_runnable = new Runnable() {
            @Override
            public void run() {
                if(Game.instance == null)
                    return;

                Player p = Game.instance.player;
                if(p == null)
                    return;

                updatePlayerPathing(p);
            }
        };

        return t_runnable;
    }

    public void tick(float delta) {
        timeSinceLastPathUpdate += delta;
        if(timeSinceLastPathUpdate > PathUpdateTime) {
            timeSinceLastPathUpdate = 0f;
            Game.threadPool.submit(Game.pathfinding.GetRunnable());
        }
    }

    public Array<PathNode> GetNodes() {
        return allNodes;
    }
}
