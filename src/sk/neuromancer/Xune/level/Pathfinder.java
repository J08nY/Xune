package sk.neuromancer.Xune.level;


import sk.neuromancer.Xune.game.Tickable;

public class Pathfinder implements Tickable {
    private final Level l;

    public Pathfinder(Level l) {
        this.l = l;
    }

    public Path find(Point src, Point dest) {
        return null;

    }

    @Override
    public void tick(int tickCount) {

    }

    public static class Point {
        public int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Path {
        private final Point[] p;

        public Path(Point[] p) {
            this.p = p;
        }
    }

}
