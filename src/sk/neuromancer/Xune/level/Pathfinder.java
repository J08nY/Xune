package sk.neuromancer.Xune.level;

import java.util.*;

import static sk.neuromancer.Xune.level.Tile.TILE_HEIGHT;
import static sk.neuromancer.Xune.level.Tile.TILE_WIDTH;

public class Pathfinder {
    private final Level l;
    private boolean[][] pass;

    public Pathfinder(Level l) {
        this.l = l;
        this.pass = new boolean[5 + (l.getHeightInTiles() - 1) * 2][5 + (l.getWidthInTiles() - 1) * 4 + 2];

        for (int row = 0; row < l.getHeightInTiles(); row++) {
            for (int col = 0; col < l.getWidthInTiles(); col++) {
                Tile tile = l.getTile(row, col);
                boolean[] passable = tile.getPassable();
                int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
                int baseY = row * 2;

                /*
                 *      / 0 \
                 *     /7   1\
                 *    (6  8  2)
                 *     \5   3/
                 *      \ 4 /
                 */
                // Fill base on passable
                pass[baseY][baseX + 2] = passable[0];
                pass[baseY + 1][baseX + 3] = passable[1];
                pass[baseY + 2][baseX + 4] = passable[2];
                pass[baseY + 3][baseX + 3] = passable[3];
                pass[baseY + 4][baseX + 2] = passable[4];
                pass[baseY + 3][baseX + 1] = passable[5];
                pass[baseY + 2][baseX] = passable[6];
                pass[baseY + 1][baseX + 1] = passable[7];
                pass[baseY + 2][baseX + 2] = passable[8];

                //Fill remaining
                pass[baseY + 1][baseX + 2] = true;
                pass[baseY + 2][baseX + 3] = true;
                pass[baseY + 3][baseX + 2] = true;
                pass[baseY + 2][baseX + 1] = true;
            }
        }
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
                //printPass(path);
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
        for (int i = 0; i < pass.length; i++) {
            for (int j = 0; j < pass[i].length; j++) {
                char v = pass[i][j] ? '#' : '-';
                if (path != null) {
                    for (Point p : path.p) {
                        if (p.x == j && p.y == i) {
                            v = 'O';
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
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
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
        if (p.y < 0 || p.y >= pass.length || p.x < 0 || p.x >= pass[0].length) {
            return false;
        }
        return pass[p.y][p.x];
    }

    public static int levelXToGrid(float x) {
        return Math.round(x / ((float) TILE_WIDTH / 4));
    }

    public static float gridXToLevel(int x) {
        return x * ((float) TILE_WIDTH / 4);
    }

    public static int levelYToGrid(float y) {
        return Math.round(y / ((float) TILE_HEIGHT / 4));
    }

    public static float gridYToLevel(int y) {
        return y * ((float) TILE_HEIGHT / 4);
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

    public static class Path {
        private final Point[] p;

        public Path(Point[] p) {
            this.p = p;
        }

        public Point[] getPoints() {
            return p;
        }
    }

}
