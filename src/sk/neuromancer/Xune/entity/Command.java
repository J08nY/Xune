package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.level.Pathfinder;

public abstract class Command {

    public static class FlyCommand extends Command {
        private final float fromX, fromY, toX, toY;

        public FlyCommand(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public float getToX() {
            return toX;
        }

        public float getToY() {
            return toY;
        }

        public boolean isFinished(float x, float y) {
            return Math.abs(x - toX) <= 1.5 && Math.abs(y - toY) <= 1.5;
        }
    }

    public static class MoveCommand extends Command {
        private final float fromX, fromY, toX, toY;
        private final Pathfinder.Path path;
        private int next;

        public MoveCommand(float fromX, float fromY, float toX, float toY, Pathfinder pathFinder) {
            int startX = Pathfinder.levelXToGrid(fromX);
            int startY = Pathfinder.levelYToGrid(fromY);
            int stopX = Pathfinder.levelXToGrid(toX);
            int stopY = Pathfinder.levelYToGrid(toY);
            Pathfinder.Point start = new Pathfinder.Point(startX, startY);
            Pathfinder.Point stop = new Pathfinder.Point(stopX, stopY);
            this.path = pathFinder.find(start, stop);
            if (this.path == null) {
                throw new IllegalArgumentException("No path found");
            }

            this.fromX = start.getLevelX();
            this.fromY = start.getLevelY();
            this.toX = stop.getLevelX();
            this.toY = stop.getLevelY();
            this.next = 0;
        }

        public float getToX() {
            return toX;
        }

        public float getToY() {
            return toY;
        }

        public Pathfinder.Point getNext() {
            return path.getPoints()[next];
        }

        public Pathfinder.Path getPath() {
            return path;
        }

        public void update(float x, float y) {
            if (this.next == path.getPoints().length - 1) {
                return;
            }
            Pathfinder.Point next = path.getPoints()[this.next];
            if (Math.abs(x - next.getLevelX()) <= 1.5 && Math.abs(y - next.getLevelY()) <= 1.5) {
                this.next++;
            }
        }

        public boolean isFinished(float x, float y) {
            return Math.abs(x - toX) <= 1.5 && Math.abs(y - toY) <= 1.5;
        }


    }
}

