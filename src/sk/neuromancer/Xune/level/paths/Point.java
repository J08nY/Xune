package sk.neuromancer.Xune.level.paths;

import java.util.Objects;

public class Point {
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

    public Point add(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public boolean isNextTo(Point q) {
        return Math.abs(x - q.x) <= 1 && Math.abs(y - q.y) <= 1;
    }

    public double distance(Point b) {
        return Math.hypot(x - b.x, y - b.y);
    }

    public double heuristic(Point b) {
        return Math.max(Math.abs(x - b.x), Math.abs(y - b.y));
    }

    public Point[] getNeighbors() {
        if ((x + y) % 2 == 0) {
            return new Point[]{
                    new Point(x - 1, y - 1),
                    new Point(x - 1, y),
                    new Point(x - 1, y + 1),
                    new Point(x, y - 1),
                    new Point(x, y + 1),
                    new Point(x + 1, y - 1),
                    new Point(x + 1, y),
                    new Point(x + 1, y + 1),
            };
        } else {
            return new Point[]{
                    new Point(x - 1, y),
                    new Point(x, y - 1),
                    new Point(x, y + 1),
                    new Point(x + 1, y),
            };
        }
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }


}
