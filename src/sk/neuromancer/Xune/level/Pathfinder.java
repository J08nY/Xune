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
    private final PassMap levelMap;
    private final PassMap buildingMap;
    private final List<Path> paths;

    public Pathfinder(Level l) {
        this.l = l;
        this.levelMap = new PassMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.buildingMap = new PassMap(l.getWidthInTiles(), l.getHeightInTiles());
        this.paths = new LinkedList<>();

        fillLevelMap(l);
    }

    private void fillLevelMap(Level l) {
        for (int row = 0; row < l.getHeightInTiles(); row++) {
            for (int col = 0; col < l.getWidthInTiles(); col++) {
                Tile tile = l.getTile(row, col);
                boolean[] passable = tile.getPassable();
                levelMap.setTile(col, row, passable);
            }
        }
    }

    public Path find(Point src, Point dest) {
        if (!isPassable(src)) {
            return null;
        }
        if (!isPassable(dest)) {
            //XXX: This changes the path and thus the command gets confused before finishing up.
            for (Point alt : getNeighbors(dest)) {
                if (isPassable(alt)) {
                    dest = alt;
                    break;
                }
            }
        }
        //TODO: Keep track of paths that are already calculated
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
                paths.add(path);
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
        boolean[][] levelMap = this.levelMap.getPassMap();
        boolean[][] buildingMap = this.buildingMap.getPassMap();
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
        return levelMap.isPassable(p) && !buildingMap.isPassable(p);
    }

    public void addEntity(Entity.PlayableEntity e) {
        if (e instanceof Building building) {
            buildingMap.setTile(building.tileX, building.tileY, negate(building.getPassable()));
        }
    }

    private boolean[] negate(boolean[] passEdges) {
        boolean[] negated = new boolean[passEdges.length];
        for (int i = 0; i < passEdges.length; i++) {
            negated[i] = !passEdges[i];
        }
        return negated;
    }

    public void removeEntity(Entity.PlayableEntity e) {
        if (e instanceof Building building) {
            buildingMap.resetTile(building.tileX, building.tileY);
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
    }

    @Override
    public void render() {
        glPointSize(5);
        glBegin(GL_POINTS);

        boolean[][] levelMap = this.levelMap.getPassMap();
        boolean[][] buildingMap = this.buildingMap.getPassMap();
        for (int i = 0; i < levelMap.length; i++) {
            for (int j = 0; j < levelMap[i].length; j++) {
                if (levelMap[i][j] && !buildingMap[i][j]) {
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
    }


    private static class PassMap {
        private final boolean[][] pass;
        private final boolean[][] set;

        public PassMap(int widthInTiles, int heightInTiles) {
            this.pass = new boolean[5 + (heightInTiles - 1) * 2][5 + (widthInTiles - 1) * 4 + 2];
            this.set = new boolean[5 + (heightInTiles - 1) * 2][5 + (widthInTiles - 1) * 4 + 2];
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

        private boolean[] getTile(boolean[][] map, int col, int row) {
            int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
            int baseY = row * 2;
            return new boolean[]{
                    map[baseY][baseX + 2],
                    map[baseY + 1][baseX + 3],
                    map[baseY + 2][baseX + 4],
                    map[baseY + 3][baseX + 3],
                    map[baseY + 4][baseX + 2],
                    map[baseY + 3][baseX + 1],
                    map[baseY + 2][baseX],
                    map[baseY + 1][baseX + 1],
                    map[baseY + 2][baseX + 2],
                    map[baseY + 1][baseX + 2],
                    map[baseY + 2][baseX + 3],
                    map[baseY + 3][baseX + 2],
                    map[baseY + 2][baseX + 1]
            };
        }

        public void setTile(int col, int row, boolean[] passable) {
            boolean[] currentTile = getTile(pass, col, row);
            boolean[] setTile = getTile(set, col, row);
            boolean[] corrected = passable.clone();
            for (int i = 0; i < passable.length; i++) {
                if (setTile[i]) {
                    corrected[i] &= currentTile[i];
                }
            }
            fillTile(pass, col, row, corrected);
            fillTile(set, col, row, PASS_ALL);
        }

        public void resetTile(int col, int row) {
            fillTile(pass, col, row, PASS_NONE);
            fillTile(set, col, row, PASS_NONE);
        }

        public boolean[][] getPassMap() {
            return pass;
        }

        public boolean[][] getSetMap() {
            return set;
        }

        private boolean isPassable(Point p) {
            if (p.y < 0 || p.y >= pass.length || p.x < 0 || p.x >= pass[0].length) {
                return false;
            }
            return pass[p.y][p.x];
        }
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
            glEnable(GL_POINT_SMOOTH);
            glPointSize(10);
            glBegin(GL_POINTS);
            glColor4f(0, 0, 0, 0.4f);
            for (Point point : p) {
                float x = point.getLevelX();
                float y = point.getLevelY();
                glVertex3f(x, y, 0);
            }
            glColor4f(1, 1, 1, 1);
            glEnd();
            glDisable(GL_POINT_SMOOTH);
        }
    }

}
