package com.interrupt.dungeoneer.game.pathfinding;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.PathNode;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.partitioning.SpatialHash;
import com.interrupt.dungeoneer.tiles.Tile;

public class Pathfinding {

    public Pathfinding() { }

    public static final int MaxTraversal = 16;
    public static final float PathUpdateTime = 35f;

    private IntMap<PathNode> allNodes = new IntMap<>();

    //private Array<PathNode> allNodes = new Array<PathNode>();
    public SpatialHash pathfindingSpatialHash = new SpatialHash(1);

    private float timeSinceLastPathUpdate = 1000f;

    public final Integer PathfindingLock = 0;
    private Vector3 lastPathfindingUpdateLoc = new Vector3();

    public final Integer GlobalPathfindingLock = 1;

    public PathNode AddNode(PathNode n) {
        synchronized (GlobalPathfindingLock) {
            allNodes.put(n.loc.hashCode(), n);
            pathfindingSpatialHash.AddEntity(n);
            return n;
        }
    }

    public void AddNodes(Array<PathNode> nodes) {
        synchronized (GlobalPathfindingLock) {
            for(int i = 0; i < nodes.size; i++) {
                PathNode n = nodes.get(i);
                allNodes.put(n.loc.hashCode(), n);
                pathfindingSpatialHash.AddEntity(n);
            }
        }
    }

    public void LinkNodesAt(float levelX, float levelY, float levelZ) {
        synchronized (GlobalPathfindingLock) {
            Array<Entity> nearby = pathfindingSpatialHash.getEntitiesAt(levelX, levelY, 1f);
            for(int i = 0; i < nearby.size; i++) {
                PathNode node1 = (PathNode)nearby.get(i);
                for(int ii = 0; ii < nearby.size; ii++) {
                    PathNode node2 = (PathNode)nearby.get(ii);
                    if(node1 == node2)
                        continue;

                    boolean hasMatchingX = node1.loc.x == node2.loc.x;
                    boolean hasMatchingY = node1.loc.y == node2.loc.y;

                    // Only link these nodes if they are on the grid
                    if(!hasMatchingX && !hasMatchingY)
                        continue;

                    // Only link when close enough
                    Vector3 distCalc = new Vector3(node1.loc);
                    float len = distCalc.sub(node2.loc).len();
                    if(len > 1.5f)
                        continue;

                    node1.addConnectionIfPossible(node2, 0.3f, 0.35f);
                    node2.addConnectionIfPossible(node1, 0.3f, 0.35f);
                }
            }
        }
    }

    Vector3 lengthChecker = new Vector3();
    public PathNode GetNodeAt(float levelX, float levelY, float levelZ) {
        synchronized (GlobalPathfindingLock) {
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

                for(PathNode n : allNodes.values()) {
                    n.reset();
                }

                PathNode pnode = GetNodeAt(player.x, player.y, player.z);
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
        synchronized (GlobalPathfindingLock) {
            return allNodes.values().toArray();
        }
    }

    public void Clear() {
        synchronized (GlobalPathfindingLock) {
            allNodes = new IntMap<>();
            pathfindingSpatialHash = new SpatialHash(1);
        }
    }
}
