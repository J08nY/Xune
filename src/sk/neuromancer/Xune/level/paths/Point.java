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
