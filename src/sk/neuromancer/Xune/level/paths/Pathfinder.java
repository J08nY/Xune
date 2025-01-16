package sk.neuromancer.Xune.level.paths;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import java.util.*;
import java.util.function.BiFunction;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Tile.*;

public class Pathfinder implements Tickable, Renderable {
    private final Level l;
    // LevelMap contains the passable tiles as an or of the Orientations
    private final IntMap levelMap;
    // SoligMap is true if the point is solid
    private final BoolMap solidMap;
    // BuildingMap is true if the point is occupied by a building
    private final BoolMap buildingMap;
    // EntityMap is true if the point is occupied by an entity
    private final BoolMap entityMap;
    //TODO: Remove this and only keep it in the level.
    private final List<Entity> entities;
    private final List<Path> paths;

    public Pathfinder(Level l) {
        this.l = l;
        this.levelMap = new IntMap(l.getWidthInTiles(), l.getHeightInTiles());
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
                if (!isPassableFrom(current.point, neighbor, w)) continue;

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
        int[][] levelMap = this.levelMap.getValMap();
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
                } else if (levelMap[i][j] == 0) {
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
        return (!w.requiresPassable || levelMap.get(p) != 0) &&
                (!w.requiresNoBuilding || !buildingMap.isTrue(p)) &&
                (!w.requiresNoEntity || !entityMap.isTrue(p)) &&
                (!w.requiresNotSolid || !solidMap.isTrue(p));
    }

    public boolean isPassableFrom(Point from, Point to, Walkability w) {
        Orientation toOrienatation = to.orientationToNeighbor(from);
        Orientation fromOrientation = from.orientationToNeighbor(to);
        //System.out.println("From: " + from + " To: " + to + " FromO: " + fromOrientation + " ToO: " + toOrienatation);
        // Check levelMap at "from" for the orientation
        if (w.requiresPassable && (levelMap.get(from) & toOrienatation.getBit()) == 0) {
            return false;
        }
        // Check levelMap at "to" for the orientation
        if (w.requiresPassable && (levelMap.get(to) & fromOrientation.getBit()) == 0) {
            return false;
        }
        return (!w.requiresNoBuilding || !buildingMap.isTrue(to)) &&
                (!w.requiresNoEntity || !entityMap.isTrue(to)) &&
                (!w.requiresNotSolid || !solidMap.isTrue(to));
    }

    public boolean isTileClear(int tileX, int tileY) {
        return !buildingMap.isTilePartiallyTrue(tileX, tileY) &&
                !entityMap.isTilePartiallyTrue(tileX, tileY);
    }

    public boolean isTileClear(int tileX, int tileY, boolean[] footprint) {
        return !buildingMap.isTileTrue(tileX, tileY, negate(footprint)) &&
                !entityMap.isTileTrue(tileX, tileY, negate(footprint));
    }

    public boolean isTileBuildable(int tileX, int tileY) {
        return isTileClear(tileX, tileY) && solidMap.isTileAllTrue(tileX, tileY);
    }

    public boolean isTileBuildable(int tileX, int tileY, boolean[] footprint) {
        return isTileClear(tileX, tileY, footprint) && solidMap.isTileTrue(tileX, tileY, footprint);
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
        int w, h;
        BiFunction<Integer, Integer, Integer> colorMap;
        if (Config.DEBUG_PATH_GRID_LEVEL) {
            w = levelMap.getWidth();
            h = levelMap.getHeight();
            colorMap = (i, j) -> PASS_TO_COLOR.get(levelMap.get(i, j));
        } else if (Config.DEBUG_PATH_GRID_BUILDING) {
            w = buildingMap.getWidth();
            h = buildingMap.getHeight();
            colorMap = (i, j) -> buildingMap.isTrue(i, j) ? 0xff0000 : 0x00ff00;
        } else if (Config.DEBUG_PATH_GRID_ENTITY) {
            w = entityMap.getWidth();
            h = entityMap.getHeight();
            colorMap = (i, j) -> entityMap.isTrue(i, j) ? 0xff0000 : 0x00ff00;
        } else if (Config.DEBUG_PATH_GRID_SOLID) {
            w = solidMap.getWidth();
            h = solidMap.getHeight();
            colorMap = (i, j) -> SOLID_TO_COLOR.get(solidMap.isTrue(i, j));
        } else {
            return;
        }
        glPointSize(5);
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_POINTS);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = colorMap.apply(i, j);
                glColor3ub((byte) ((color >> 16) & 0xff), (byte) ((color >> 8) & 0xff), (byte) (color & 0xff));
                glVertex3f(gridXToLevel(i), gridYToLevel(j), 0);
            }
        }
        glColor4f(1, 1, 1, 1);
        glEnd();
        if (Config.DEBUG_PATH_GRID_LEVEL) {
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    float x = gridXToLevel(i);
                    float y = gridYToLevel(j);

                }
            }
        }
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
