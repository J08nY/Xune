package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.unit.Unit;
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

        public float getFromX() {
            return fromX;
        }

        public float getFromY() {
            return fromY;
        }

        public float getToX() {
            return toX;
        }

        public float getToY() {
            return toY;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                if (isFinished(unit)) {
                    unit.setPosition(toX, toY);
                } else {
                    unit.move(toX, toY);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
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
            if (entity instanceof Unit unit) {
                if (isFinished(unit)) {
                    unit.setPosition(toX, toY);
                } else {
                    unit.move(getNext().getLevelX(), getNext().getLevelY());
                    update(unit.x, unit.y);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }
    }

    public static class AttackCommand extends Command {
        private final Entity target;

        public AttackCommand(Entity target) {
            this.target = target;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return target.health == 0;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                unit.face(target.x, target.y);
                unit.attack(target);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }
    }

    public static class MoveAndAttackCommand extends Command {
        private MoveCommand move;
        private AttackCommand attack;
        private Pathfinder pathfinder;
        private Entity target;
        private float targetX, targetY;

        public MoveAndAttackCommand(float fromX, float fromY, Pathfinder pathFinder, Entity target) {
            this.move = new MoveCommand(fromX, fromY, target.x, target.y, pathFinder);
            this.attack = new AttackCommand(target);
            this.pathfinder = pathFinder;
            this.target = target;
            this.targetX = target.x;
            this.targetY = target.y;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return attack.isFinished(entity);
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                if (unit.inRange(target)) {
                    attack.execute(entity, tickCount);
                } else {
                    if ((target.x != targetX || target.y != targetY) && tickCount % 30 == 0) {
                        // Target moved, update
                        targetX = target.x;
                        targetY = target.y;
                        move = new MoveCommand(entity.x, entity.y, target.x, target.y, pathfinder);
                    }
                    move.execute(entity, tickCount);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }
    }

    public static class FlyAndAttackCommand extends Command {
        private FlyCommand move;
        private AttackCommand attack;
        private Entity target;
        private float targetX, targetY;

        public FlyAndAttackCommand(float fromX, float fromY, Entity target) {
            this.move = new FlyCommand(fromX, fromY, target.x, target.y);
            this.attack = new AttackCommand(target);
            this.target = target;
            this.targetX = target.x;
            this.targetY = target.y;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return attack.isFinished(entity);
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                if (unit.inRange(target)) {
                    attack.execute(entity, tickCount);
                } else {
                    if ((target.x != targetX || target.y != targetY) && tickCount % 30 == 0) {
                        // Target moved, update
                        targetX = target.x;
                        targetY = target.y;
                        move = new FlyCommand(unit.x, unit.y, target.x, target.y);
                    }
                    move.execute(entity, tickCount);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }
    }

    public static class ProduceCommand extends Command {
        private int start;
        private boolean started;
        private boolean finished;
        private final int duration;

        private final Entity.PlayableEntity result;

        public ProduceCommand(int duration, Entity.PlayableEntity result) {
            this.duration = duration;
            this.result = result;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Building building) {
                if (!started) {
                    start = tickCount;
                    started = true;
                }
                if (tickCount - start >= duration) {
                    building.owner.addEntity(result);
                    finished = true;
                }
            } else {
                throw new IllegalArgumentException("Entity must be a building.");
            }
        }

        @Override
        public boolean isFinished(Entity entity) {
            return finished;
        }
    }
}

