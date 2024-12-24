package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Tile.*;

public class Pathfinder implements Tickable, Renderable {
    private final Level l;
    private final boolean[][] levelMap;
    private boolean[][] buildingMap;
    private Set<Building> buildings = new HashSet<>();

    public Pathfinder(Level l) {
        this.l = l;
        this.levelMap = new boolean[5 + (l.getHeightInTiles() - 1) * 2][5 + (l.getWidthInTiles() - 1) * 4 + 2];
        this.buildingMap = new boolean[5 + (l.getHeightInTiles() - 1) * 2][5 + (l.getWidthInTiles() - 1) * 4 + 2];

        fillLevelMap(l);
    }

    private void fillLevelMap(Level l) {
        for (int row = 0; row < l.getHeightInTiles(); row++) {
            for (int col = 0; col < l.getWidthInTiles(); col++) {
                Tile tile = l.getTile(row, col);
                boolean[] passable = tile.getPassable();
                fillTile(levelMap, col, row, passable);
            }
        }
    }

    private void fillTile(boolean[][] map, int col, int row, boolean[] passable) {
        int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
        int baseY = row * 2;

        /*
         *        / 0 \
         *       /     \
         *      /7  9  1\
         *     /         \
         *    (6 12 8 10 2)
         *     \         /
         *      \5 11  3/
         *       \     /
         *        \ 4 /
         */
        // Fill base on passable
        map[baseY][baseX + 2] = passable[0];
        map[baseY + 1][baseX + 3] = passable[1];
        map[baseY + 2][baseX + 4] = passable[2];
        map[baseY + 3][baseX + 3] = passable[3];
        map[baseY + 4][baseX + 2] = passable[4];
        map[baseY + 3][baseX + 1] = passable[5];
        map[baseY + 2][baseX] = passable[6];
        map[baseY + 1][baseX + 1] = passable[7];
        map[baseY + 2][baseX + 2] = passable[8];
        map[baseY + 1][baseX + 2] = passable[9];
        map[baseY + 2][baseX + 3] = passable[10];
        map[baseY + 3][baseX + 2] = passable[11];
        map[baseY + 2][baseX + 1] = passable[12];
    }

    public Path find(Point src, Point dest) {
        if (!isPassable(src) || !isPassable(dest)) {
            return null;
        }
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(src, null, 0, heuristic(src, dest));
        openSet.add(startNode);
        allNodes.put(src, startNode);

        long searched = 0;
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            searched++;

            if (current.point.equals(dest)) {
                Path path = reconstructPath(current);
                printPass(path);
                System.out.println("Searched: " + searched);
                System.out.println("Path length: " + path.p.length);
                return path;
            }

            for (Point neighbor : getNeighbors(current.point)) {
                if (!isPassable(neighbor)) continue;

                double tentativeG = current.g + distance(current.point, neighbor);
                Node neighborNode = allNodes.getOrDefault(neighbor, new Node(neighbor));

                if (tentativeG < neighborNode.g) {
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + heuristic(neighbor, dest);
                    neighborNode.cameFrom = current;
                    openSet.add(neighborNode);
                    allNodes.put(neighbor, neighborNode);
                }
            }
        }

        return null; // No path found
    }

    private void printPass(Path path) {
        for (int i = 0; i < levelMap.length; i++) {
            for (int j = 0; j < levelMap[i].length; j++) {
                char v = buildingMap[i][j] ? '░' : (levelMap[i][j] ? '#' : '-');
                if (path != null) {
                    for (Point p : path.p) {
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

    private double heuristic(Point a, Point b) {
        return Math.max(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    private double distance(Point a, Point b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private Point[] getNeighbors(Point p) {
        if ((p.x + p.y) % 2 == 0) {
            return new Point[]{
                    new Point(p.x - 1, p.y - 1),
                    new Point(p.x - 1, p.y),
                    new Point(p.x - 1, p.y + 1),
                    new Point(p.x, p.y - 1),
                    new Point(p.x, p.y + 1),
                    new Point(p.x + 1, p.y - 1),
                    new Point(p.x + 1, p.y),
                    new Point(p.x + 1, p.y + 1),
            };
        } else {
            return new Point[]{
                    new Point(p.x - 1, p.y),
                    new Point(p.x, p.y - 1),
                    new Point(p.x, p.y + 1),
                    new Point(p.x + 1, p.y),
            };
        }
    }

    private boolean isPassable(Point p) {
        if (p.y < 0 || p.y >= levelMap.length || p.x < 0 || p.x >= levelMap[0].length) {
            return false;
        }
        return levelMap[p.y][p.x] && !buildingMap[p.y][p.x];
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
    public void render() {
        glPointSize(5);
        glBegin(GL_POINTS);
        glColor4f(1, 1, 1, 0.9f);
        for (int i = 0; i < levelMap.length; i++) {
            for (int j = 0; j < levelMap[i].length; j++) {
                if (levelMap[i][j] && !buildingMap[i][j]) {
                    glVertex3f(gridXToLevel(j), gridYToLevel(i), 0);
                }
            }
        }
        glColor4f(1, 1, 1, 1);
        glEnd();
    }

    @Override
    public void tick(int tickCount) {
        Set<Building> notFound = new HashSet<>(buildings);
        for (Entity.PlayableEntity e : l.getPlayer().getEntities()) {
            if (e instanceof Building building) {
                if (buildings.contains(building)) {
                    notFound.remove(building);
                    continue;
                }
                fillTile(buildingMap, building.tileX, building.tileY, PASS_ALL);
                buildings.add(building);
            }
        }
        for (Building b : notFound) {
            fillTile(buildingMap, b.tileX, b.tileY, PASS_NONE);
        }
        buildings.removeAll(notFound);
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

    public static class Point {
        public int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point point)) return false;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        public float getLevelX() {
            return Pathfinder.gridXToLevel(x);
        }

        public float getLevelY() {
            return Pathfinder.gridYToLevel(y);
        }

        @Override
        public String toString() {
            return "Point{" + "x=" + x + ", y=" + y + '}';
        }
    }

    public static class Path implements Renderable {
        private final Point[] p;

        public Path(Point[] p) {
            this.p = p;
        }

        public Point[] getPoints() {
            return p;
        }

        @Override
        public void render() {
            for (Point point : p) {
                float x = point.getLevelX();
                float y = point.getLevelY();

                glEnable(GL_POINT_SMOOTH);
                glPointSize(10);
                glBegin(GL_POINTS);
                glColor4f(0, 0, 0, 0.4f);
                glVertex3f(x, y, 0);
                glColor4f(1, 1, 1, 1);
                glEnd();
                glDisable(GL_POINT_SMOOTH);
            }
        }
    }

}
