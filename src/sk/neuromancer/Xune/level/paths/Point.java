package sk.neuromancer.Xune.level.paths;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.net.proto.BaseProto;

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
    }

    public int angleToNeighbor(Point b) {
        if (!isNextTo(b)) {
            throw new IllegalArgumentException("Points are not neighbors.");
        }
        /*
         *        315             0               45
         *      (-1,-1)         (0,-1)          (1,-1)
         *            \           |            /
         *                  \     |      /
         *  270 (-1, 0) -------- this --------- (1, 0)  90
         *                  /     |      \
         *            /           |            \
         *      (-1, 1)         (0, 1)          (1, 1)
         *        225            180             135
         */
        int dx = b.x - x;
        int dy = b.y - y;
        return switch (dx) {
            case -1 -> switch (dy) {
                case -1 -> 315;
                case 0 -> 270;
                case 1 -> 225;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            case 0 -> switch (dy) {
                case -1 -> 0;
                case 1 -> 180;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            case 1 -> switch (dy) {
                case -1 -> 45;
                case 0 -> 90;
                case 1 -> 135;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            default -> throw new IllegalArgumentException("Invalid dx.");
        };
    }

    public Orientation orientationToNeighbor(Point b) {
        if (!isNextTo(b)) {
            throw new IllegalArgumentException("Points are not neighbors.");
        }

        int dx = x - b.x;
        int dy = y - b.y;
        return switch (dx) {
            case -1 -> switch (dy) {
                case -1 -> Orientation.NORTHWEST;
                case 0 -> Orientation.WEST;
                case 1 -> Orientation.SOUTHWEST;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            case 0 -> switch (dy) {
                case -1 -> Orientation.NORTH;
                case 1 -> Orientation.SOUTH;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            case 1 -> switch (dy) {
                case -1 -> Orientation.NORTHEAST;
                case 0 -> Orientation.EAST;
                case 1 -> Orientation.SOUTHEAST;
                default -> throw new IllegalArgumentException("Invalid dy.");
            };
            default -> throw new IllegalArgumentException("Invalid dx.");
        };
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    public BaseProto.Point serialize() {
        return BaseProto.Point.newBuilder().setX(x).setY(y).build();
    }
}
