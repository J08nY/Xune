package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.level.Pathfinder;

import java.util.Arrays;

public abstract class Command {

    public abstract void execute(Entity entity, int tickCount);

    public abstract boolean isFinished(Entity entity);

    public static class FlyCommand extends Command {
        private final float fromX, fromY, toX, toY;

        public FlyCommand(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public boolean isFinished(Entity entity) {
            return Math.abs(entity.x - toX) <= 1.5 && Math.abs(entity.y - toY) <= 1.5;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Entity.Unit unit) {
                if (isFinished(unit)) {
                    unit.setPosition(toX, toY);
                } else {
                    unit.move(toX, toY);
                }
            }
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

        public Pathfinder.Point getNext() {
            return path.getPoints()[next];
        }

        public Pathfinder.Path getPath() {
            return path;
        }

        public Pathfinder.Path getNextPath() {
            Pathfinder.Point[] points = path.getPoints();
            return new Pathfinder.Path(Arrays.copyOfRange(points, next, points.length));
        }

        private void update(float x, float y) {
            if (this.next == path.getPoints().length - 1) {
                return;
            }
            Pathfinder.Point next = path.getPoints()[this.next];
            if (Math.abs(x - next.getLevelX()) <= 1.5 && Math.abs(y - next.getLevelY()) <= 1.5) {
                this.next++;
            }
        }

        public boolean isFinished(Entity entity) {
            return Math.abs(entity.x - toX) <= 1.5 && Math.abs(entity.y - toY) <= 1.5;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Entity.Unit unit) {
                if (isFinished(unit)) {
                    unit.setPosition(toX, toY);
                } else {
                    unit.move(getNext().getLevelX(), getNext().getLevelY());
                    update(unit.x, unit.y);
                }
            }
        }
    }

    public static class AttackCommand extends Command {
        private final Entity target;
        private final float range;
        private final int rate;

        public AttackCommand(Entity target, float range, int rate) {
            this.target = target;
            this.range = range;
            this.rate = rate;
        }

        public boolean inRange(float x, float y) {
            float dx = x - target.x;
            float dy = y - target.y;
            return dx * dx + dy * dy <= range * range;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return target.health == 0;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (inRange(entity.x, entity.y) && tickCount % rate == 0) {
                target.health -= 10;
            } else {
                //just wait
            }
        }
    }
}

