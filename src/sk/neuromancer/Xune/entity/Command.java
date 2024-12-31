package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;
import sk.neuromancer.Xune.level.paths.Path;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class Command {

    public abstract void execute(Entity entity, int tickCount);

    public abstract void finalize(Entity entity);

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
                unit.move(toX, toY);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finalize(Entity entity) {
            if (entity instanceof Unit unit) {
                unit.setPosition(toX, toY);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }
    }

    public static class MoveCommand extends Command {
        private final float fromX, fromY, toX, toY;
        private final Path path;
        private int next;

        public MoveCommand(float fromX, float fromY, float toX, float toY, Pathfinder pathFinder) {
            int startX = Pathfinder.levelXToGrid(fromX);
            int startY = Pathfinder.levelYToGrid(fromY);
            int stopX = Pathfinder.levelXToGrid(toX);
            int stopY = Pathfinder.levelYToGrid(toY);
            Point start = new Point(startX, startY);
            Point stop = new Point(stopX, stopY);
            this.path = pathFinder.find(start, stop);
            if (this.path == null) {
                throw new NoPathFound("No path found");
            }
            stop = path.getPoints()[path.getPoints().length - 1];

            this.fromX = start.getLevelX();
            this.fromY = start.getLevelY();
            this.toX = stop.getLevelX();
            this.toY = stop.getLevelY();
            this.next = 0;
        }

        public Point getNext() {
            return path.getPoints()[next];
        }

        public Path getPath() {
            return path;
        }

        public Path getNextPath() {
            Point[] points = path.getPoints();
            return new Path(Arrays.copyOfRange(points, next, points.length));
        }

        private void update(float x, float y) {
            if (this.next == path.getPoints().length - 1) {
                return;
            }
            Point next = path.getPoints()[this.next];
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
                unit.move(getNext().getLevelX(), getNext().getLevelY());
                update(unit.x, unit.y);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finalize(Entity entity) {
            if (entity instanceof Unit unit) {
                unit.setPosition(toX, toY);
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

        @Override
        public void finalize(Entity entity) {
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
                        try {
                            move = new MoveCommand(entity.x, entity.y, target.x, target.y, pathfinder);
                            targetX = target.x;
                            targetY = target.y;
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                    }
                    move.execute(entity, tickCount);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finalize(Entity entity) {
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

        @Override
        public void finalize(Entity entity) {
        }
    }

    public static class ProduceCommand extends Command {
        private final Class<? extends Unit> resultClass;
        private int start;
        private boolean started;
        private boolean finished;
        private final int duration;

        public ProduceCommand(int duration, Class<? extends Unit> resultClass) {
            this.duration = duration;
            this.resultClass = resultClass;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Building building) {
                if (!started) {
                    start = tickCount;
                    started = true;
                }
                if (tickCount - start >= duration) {
                    Unit result;
                    try {
                        Constructor<? extends Unit> con = resultClass.getConstructor(float.class, float.class, Orientation.class, EntityOwner.class, Flag.class);
                        result = con.newInstance(building.x, building.y + Tile.TILE_CENTER_Y, building.orientation, building.owner, building.flag);
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                             IllegalAccessException ex) {
                        System.out.println("Failed to create unit." + ex);
                        finished = true;
                        return;
                    }
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

        @Override
        public void finalize(Entity entity) {
        }
    }

    public static class CollectSpiceCommand extends Command {
        private final MoveCommand move;
        private final Tile target;

        public CollectSpiceCommand(float fromX, float fromY, Pathfinder pathfinder, Tile target) {
            float targetX = Level.tileToCenterLevelX(target.getX(), target.getY());
            float targetY = Level.tileToCenterLevelY(target.getX(), target.getY());
            this.move = new MoveCommand(fromX, fromY, targetX, targetY, pathfinder);
            this.target = target;
        }

        public Tile getTarget() {
            return target;
        }

        public boolean collecting(Entity entity) {
            return move.isFinished(entity) && !isFinished(entity);
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Harvester harvester) {
                if (!move.isFinished(entity)) {
                    move.execute(entity, tickCount);
                } else {
                    harvester.collectSpice(target);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public boolean isFinished(Entity entity) {
            if (entity instanceof Harvester harvester) {
                return harvester.isFull() || target.getSpice() == 0;
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public void finalize(Entity entity) {
        }
    }

    public static class DropOffSpiceCommand extends Command {
        private final MoveCommand move;
        private final Refinery target;

        public DropOffSpiceCommand(float fromX, float fromY, Pathfinder pathfinder, Refinery target) {
            float[][] offsets = new float[][]{
                    {0, Tile.TILE_CENTER_Y},
                    {Tile.TILE_CENTER_X, 0},
                    {0, -Tile.TILE_CENTER_Y},
                    {-Tile.TILE_CENTER_X, 0}
            };
            MoveCommand m = null;
            for (float[] offset : offsets) {
                try {
                    m = new MoveCommand(fromX, fromY, target.x + offset[0], target.y + offset[1], pathfinder);
                    break;
                } catch (NoPathFound ignore) {

                }
            }
            if (m == null) {
                throw new NoPathFound("No path to Refinery found.");
            }
            this.move = m;
            this.target = target;
        }

        public boolean dropping(Entity entity) {
            return move.isFinished(entity) && !isFinished(entity);
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Harvester harvester) {
                if (!move.isFinished(entity)) {
                    move.execute(entity, tickCount);
                } else {
                    harvester.dropOffSpice(target);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public void finalize(Entity entity) {

        }

        @Override
        public boolean isFinished(Entity entity) {
            if (entity instanceof Harvester harvester) {
                return harvester.getSpice() == 0;
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }
    }
}

