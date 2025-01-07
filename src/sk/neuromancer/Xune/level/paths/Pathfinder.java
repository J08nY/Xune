package sk.neuromancer.Xune.level.paths;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Tile.TILE_HEIGHT;
import static sk.neuromancer.Xune.level.Tile.TILE_WIDTH;

public class Pathfinder implements Tickable, Renderable {
    private final Level l;
    private final BoolMap levelMap;
    private final BoolMap solidMap;
    private final BoolMap buildingMap;
    private final BoolMap entityMap;
    private final List<Entity> entities;
    private final List<Path> paths;

    public Pathfinder(Level l) {
        this.l = l;
        this.levelMap = new BoolMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.solidMap = new BoolMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.buildingMap = new BoolMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.entityMap = new BoolMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.entities = new LinkedList<>();
        this.paths = new LinkedList<>();

        fillLevelMap(l);
    }

    private void fillLevelMap(Level l) {
        for (int row = 0; row < l.getHeightInTiles(); row++) {
            for (int col = 0; col < l.getWidthInTiles(); col++) {
                Tile tile = l.getTile(col, row);
                levelMap.setTile(col, row, tile.getPassable());
                solidMap.setTile(col, row, tile.getSolid());
            }
        }
    }

    private boolean validDestination(Point dest, List<Point> ends, Walkability w) {
        if (!isPassable(dest, w)) {
            return false;
        }
        for (Point end : ends) {
            if (end.equals(dest) || end.isNextTo(dest)) {
                return false;
            }
        }
        return true;
    }

    private Point findValidDestination(Point original, int bound, Walkability w) {
        List<Point> ends = paths.stream().map(Path::getEnd).toList();
        int x = original.x;
        int y = original.y;
        int dx = 0;
        int dy = -1;
        int segmentLength = 1;
        int segmentPassed = 0;
        int segmentCount = 0;

        for (int i = 0; i < bound; i++) {
            Point pt = new Point(x, y);
            if (validDestination(pt, ends, w)) {
                return pt;
            }

            if (segmentPassed == segmentLength) {
                segmentPassed = 0;
                int temp = dx;
                dx = -dy;
                dy = temp;
                segmentCount++;
                if (segmentCount % 2 == 0) {
                    segmentLength++;
                }
            }

            x += dx;
            y += dy;
            segmentPassed++;
        }
        return null;
    }

    public Path find(Point src, Point dest, Walkability w) {
        if (w.allowRedirect) {
            dest = findValidDestination(dest, 100, w);
        } else {
            List<Point> ends = paths.stream().map(Path::getEnd).toList();
            if (!validDestination(dest, ends, w)) {
                return null;
            }
        }
        if (Config.DEBUG_PATHS) {
            System.out.println("Source: " + src);
            System.out.println("Destination: " + dest);
        }
        if (dest == null) {
            return null;
        }
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(src, null, 0, src.heuristic(dest));
        openSet.add(startNode);
        allNodes.put(src, startNode);

        long searched = 0;
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            searched++;

            if (current.point.equals(dest)) {
                Path path = reconstructPath(current);
                if (Config.DEBUG_PATH_PRINT) {
                    printPass(path);
                }
                if (Config.DEBUG_PATHS) {
                    System.out.println("Searched: " + searched);
                    System.out.println("Path length: " + path.getPoints().length);
                }
                paths.add(path);
                return path;
            }

            for (Point neighbor : current.point.getNeighbors()) {
                if (!isPassable(neighbor, w)) continue;

                double tentativeG = current.g + current.point.distance(neighbor);
                Node neighborNode = allNodes.getOrDefault(neighbor, new Node(neighbor));

                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + neighbor.heuristic(dest);
                    neighborNode.cameFrom = current;
                    openSet.add(neighborNode);
                    allNodes.put(neighbor, neighborNode);
                }
            }
        }
        if (Config.DEBUG_PATHS) {
            System.out.println("Searched: " + searched);
        }
        return null; // No path found
    }

    private void printPass(Path path) {
        boolean[][] levelMap = this.levelMap.getValMap();
        boolean[][] buildingMap = this.buildingMap.getValMap();
        boolean[][] entityMap = this.entityMap.getValMap();
        boolean[][] solidMap = this.solidMap.getValMap();
        for (int i = 0; i < levelMap.length; i++) {
            for (int j = 0; j < levelMap[i].length; j++) {
                char v = ' ';
                if (buildingMap[i][j]) {
                    v = '░';
                } else if (entityMap[i][j]) {
                    v = 'E';
                } else if (!levelMap[i][j]) {
                    v = '#';
                } else if (solidMap[i][j]){
                    v = '*';
                } else {
                    v = '-';
                }
                if (path != null) {
                    for (Point p : path.getPoints()) {
                        if (p.x == j && p.y == i) {
                            v = '█';
                            break;
                        }
                    }
                }
                System.out.print(v);
            }
            System.out.println();
        }
    }

    private Path reconstructPath(Node node) {
        List<Point> path = new ArrayList<>();
        while (node != null) {
            path.add(node.point);
            node = node.cameFrom;
        }
        Collections.reverse(path);
        return new Path(path.toArray(new Point[0]));
    }

    public boolean isPassable(Point p, Walkability w) {
        return (!w.requiresPassable || levelMap.isTrue(p)) &&
                (!w.requiresNoBuilding || !buildingMap.isTrue(p)) &&
                (!w.requiresNoEntity || !entityMap.isTrue(p)) &&
                (!w.requiresNotSolid || !solidMap.isTrue(p));
    }

    public boolean isTileClear(int tileX, int tileY) {
        return levelMap.isTileAllTrue(tileX, tileY) &&
                !buildingMap.isTilePartiallyTrue(tileX, tileY) &&
                !entityMap.isTilePartiallyTrue(tileX, tileY);
    }

    public boolean isTileClear(int tileX, int tileY, boolean[] footprint) {
        return levelMap.isTileTrue(tileX, tileY, footprint) &&
                !buildingMap.isTileTrue(tileX, tileY, footprint) &&
                !entityMap.isTileTrue(tileX, tileY, footprint);
    }

    public boolean isTileBuildable(int tileX, int tileY) {
        return isTileClear(tileX, tileY) && solidMap.isTileAllTrue(tileX, tileY);
    }

    public boolean isTileBuildable(int tileX, int tileY, boolean[] footprint) {
        return isTileClear(tileX, tileY) && solidMap.isTileTrue(tileX, tileY, footprint);
    }

    public void addEntity(Entity e) {
        if (e instanceof Building building) {
            buildingMap.setTile(building.tileX, building.tileY, negate(building.getPassable()));
        } else if (e instanceof Unit unit) {
            entities.add(unit);
            for (Point point : unit.getOccupied()) {
                entityMap.set(point);
            }
        }
    }

    private boolean[] negate(boolean[] passEdges) {
        boolean[] negated = new boolean[passEdges.length];
        for (int i = 0; i < passEdges.length; i++) {
            negated[i] = !passEdges[i];
        }
        return negated;
    }

    public void removeEntity(Entity e) {
        if (e instanceof Building building) {
            buildingMap.resetTile(building.tileX, building.tileY);
        } else if (e instanceof Unit unit) {
            entities.remove(unit);
            for (Point point : unit.getOccupied()) {
                entityMap.reset(point);
            }
        }
    }

    public static int levelXToGrid(float x) {
        return Math.round(x / ((float) TILE_WIDTH / 4));
    }

    public static float gridXToLevel(int x) {
        return x * ((float) TILE_WIDTH / 4);
    }

    public static int levelYToGrid(float y) {
        return Math.round(((4 * y + 2) / (TILE_HEIGHT + 1)));
    }

    public static float gridYToLevel(int y) {
        return y * ((float) (TILE_HEIGHT + 1) / 4) - 0.5f;
    }

    @Override
    public void tick(int tickCount) {
        paths.clear();
        entityMap.resetAll();
        for (Entity e : entities) {
            if (e instanceof Unit unit) {
                for (Point point : unit.getOccupied()) {
                    entityMap.set(point);
                }
            }
        }
    }

    @Override
    public void render() {
        boolean[][] map;
        if (Config.DEBUG_PATH_GRID_LEVEL) {
            map = levelMap.getValMap();
        } else if (Config.DEBUG_PATH_GRID_BUILDING) {
            map = buildingMap.getValMap();
        } else if (Config.DEBUG_PATH_GRID_ENTITY) {
            map = entityMap.getValMap();
        } else if (Config.DEBUG_PATH_GRID_SOLID) {
            map = solidMap.getValMap();
        } else {
            return;
        }
        glPointSize(5);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_POINTS);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j]) {
                    if ((i + j) % 2 == 0) {
                        glColor4f(1, 0, 0, 0.9f);
                    } else {
                        glColor4f(0, 0, 1, 0.9f);
                    }
                } else {
                    glColor4f(0, 0, 0, 0.3f);
                }
                glVertex3f(gridXToLevel(j), gridYToLevel(i), 0);
            }
        }
        glColor4f(1, 1, 1, 1);
        glEnd();
        glEnable(GL_DEPTH_TEST);
    }

    private static class Node {
        Point point;
        Node cameFrom;
        double g;
        double f;

        Node(Point point) {
            this.point = point;
            this.g = Double.MAX_VALUE;
            this.f = Double.MAX_VALUE;
        }

        Node(Point point, Node cameFrom, double g, double f) {
            this.point = point;
            this.cameFrom = cameFrom;
            this.g = g;
            this.f = f;
        }
    }

    public record Walkability(boolean requiresPassable, boolean requiresNotSolid, boolean requiresNoBuilding,
                              boolean requiresNoEntity, boolean allowRedirect) {
        public static final Walkability WORM = new Walkability(true, true, true, false, false);
        public static final Walkability UNIT = new Walkability(true, false, true, true, true);
    }
}
