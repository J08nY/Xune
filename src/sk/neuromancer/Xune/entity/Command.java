package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;
import sk.neuromancer.Xune.level.paths.Path;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.CommandProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public abstract class Command {
    private boolean started;

    public boolean isStarted(Entity entity) {
        return started;
    }

    public void start(Entity entity, int tickCount) {
        started = true;
    }

    public abstract void execute(Entity entity, int tickCount);

    public abstract boolean isFinished(Entity entity);

    public abstract void finish(Entity entity, int tickCount, boolean done);

    public abstract CommandProto.Command serialize();

    public static class FlyCommand extends Command {
        private final float fromX, fromY, toX, toY;

        public FlyCommand(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public FlyCommand(CommandProto.CommandFly fly) {
            this.fromX = fly.getFrom().getX();
            this.fromY = fly.getFrom().getY();
            this.toX = fly.getTo().getX();
            this.toY = fly.getTo().getY();
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
        public boolean isFinished(Entity entity) {
            if (entity instanceof Unit unit) {
                float speed = unit.getSpeed();
                return Math.abs(unit.x - toX) <= speed && Math.abs(unit.y - toY) <= speed;
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void start(Entity entity, int tickCount) {
            super.start(entity, tickCount);
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
        public void finish(Entity entity, int tickCount, boolean done) {
            if (entity instanceof Unit unit) {
                if (done)
                    unit.setPosition(toX, toY);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public CommandProto.Command serialize() {
            BaseProto.Position from = BaseProto.Position.newBuilder()
                    .setX(fromX)
                    .setY(fromY)
                    .build();

            BaseProto.Position to = BaseProto.Position.newBuilder()
                    .setX(toX)
                    .setY(toY)
                    .build();

            return CommandProto.Command.newBuilder()
                    .setFly(CommandProto.CommandFly.newBuilder()
                            .setFrom(from)
                            .setTo(to)
                            .build())
                    .build();
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
            this.path = pathFinder.find(start, stop, Pathfinder.Walkability.UNIT);
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

        public MoveCommand(CommandProto.CommandMove move) {
            this.fromX = move.getFrom().getX();
            this.fromY = move.getFrom().getY();
            this.toX = move.getTo().getX();
            this.toY = move.getTo().getY();
            this.path = new Path(move.getPoints());
            this.next = move.getNext();
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

        private void update(Unit unit) {
            if (this.next == path.getPoints().length - 1) {
                return;
            }
            Point next = path.getPoints()[this.next];
            if (this.next == 0) {
                Point nextNext = path.getPoints()[this.next + 1];
                int pathAngle = next.angleToNeighbor(nextNext);
                float dx = next.getLevelX() - unit.x;
                float dy = next.getLevelY() - unit.y;
                float angle = (float) Math.atan2(dy, dx);
                float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
                float azimuthDeg = (float) Math.toDegrees(azimuth);
                if (Math.abs(azimuthDeg - pathAngle) >= 90) {
                    this.next++;
                    next = path.getPoints()[this.next];
                }
            }
            float speed = unit.getSpeed();
            if (Math.abs(unit.x - next.getLevelX()) <= speed && Math.abs(unit.y - next.getLevelY()) <= speed) {
                this.next++;
            }
        }

        @Override
        public boolean isFinished(Entity entity) {
            if (entity instanceof Unit unit) {
                float speed = unit.getSpeed();
                return Math.abs(unit.x - toX) <= speed && Math.abs(unit.y - toY) <= speed;
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                unit.move(getNext().getLevelX(), getNext().getLevelY());
                update(unit);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, int tickCount, boolean done) {
            if (entity instanceof Unit unit) {
                if (done)
                    unit.setPosition(toX, toY);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public CommandProto.Command serialize() {
            BaseProto.Position from = BaseProto.Position.newBuilder()
                    .setX(fromX)
                    .setY(fromY)
                    .build();

            BaseProto.Position to = BaseProto.Position.newBuilder()
                    .setX(toX)
                    .setY(toY)
                    .build();

            return CommandProto.Command.newBuilder()
                    .setMove(CommandProto.CommandMove.newBuilder()
                            .setFrom(from)
                            .setTo(to)
                            .setPoints(path.serialize())
                            .setNext(next)
                            .build())
                    .build();
        }
    }

    public static class AttackCommand extends Command {
        private final Entity target;
        private final boolean keep;

        public AttackCommand(Entity target, boolean keep) {
            this.target = target;
            this.keep = keep;
        }

        public AttackCommand(Entity target) {
            this(target, true);
        }

        public AttackCommand(CommandProto.CommandAttack attack) {
            //TODO:
            // this.target = Entity.getEntity(attack.getTargetId());
            this.target = null;
            this.keep = attack.getKeep();
        }

        public Entity getTarget() {
            return target;
        }

        @Override
        public boolean isFinished(Entity entity) {
            if (keep) {
                return target.health <= 0;
            } else {
                return target.health <= 0 || !entity.inSight(target);
            }
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Unit unit) {
                unit.face(target.x, target.y);
                unit.attack(target);
                unit.setAttacking(true, target);
                target.setUnderAttack(true, unit);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, int tickCount, boolean done) {
            entity.setAttacking(false, null);
            target.setUnderAttack(false, null);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setAttack(CommandProto.CommandAttack.newBuilder()
                            .setTargetId(target.getId())
                            .setKeep(keep)
                            .build())
                    .build();
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

        public MoveAndAttackCommand(CommandProto.CommandMoveAndAttack moveAndAttack, Pathfinder pathfinder) {
            this.move = new MoveCommand(moveAndAttack.getMove());
            this.attack = new AttackCommand(moveAndAttack.getAttack());
            this.pathfinder = pathfinder;
            this.target = attack.getTarget();
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
                        } catch (NoPathFound e) {
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
        public void finish(Entity entity, int tickCount, boolean done) {
            attack.finish(entity, tickCount, done);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setMoveAndAttack(CommandProto.CommandMoveAndAttack.newBuilder()
                            .setMove(move.serialize().getMove())
                            .setAttack(attack.serialize().getAttack())
                            .build())
                    .build();
        }

        public AttackCommand getAttackCommand() {
            return attack;
        }

        public MoveCommand getMoveCommand() {
            return move;
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

        public FlyAndAttackCommand(CommandProto.CommandFlyAndAttack flyAndAttack) {
            this.move = new FlyCommand(flyAndAttack.getMove());
            this.attack = new AttackCommand(flyAndAttack.getAttack());
            this.target = attack.getTarget();
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
        public void finish(Entity entity, int tickCount, boolean done) {
            attack.finish(entity, tickCount, done);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setFlyAndAttack(CommandProto.CommandFlyAndAttack.newBuilder()
                            .setMove(move.serialize().getFly())
                            .setAttack(attack.serialize().getAttack())
                            .build())
                    .build();
        }

        public AttackCommand getAttackCommand() {
            return attack;
        }

        public FlyCommand getFlyCommand() {
            return move;
        }
    }

    public static class ProduceCommand extends Command {
        private final Class<? extends Unit> resultClass;
        private final Pathfinder pathfinder;
        private float progress;
        private boolean finished;
        private final int duration;

        public ProduceCommand(int duration, Class<? extends Unit> resultClass, Pathfinder pathFinder) {
            this.duration = duration;
            this.resultClass = resultClass;
            this.pathfinder = pathFinder;
        }

        public ProduceCommand(CommandProto.CommandProduce produce, Pathfinder pathfinder) {
            this.duration = produce.getDuration();
            this.resultClass = PlayableEntity.fromEntityClass(produce.getResultClass()).asSubclass(Unit.class);
            this.progress = produce.getProgress();
            this.finished = false;
            this.pathfinder = pathfinder;
        }

        @Override
        public void start(Entity entity, int tickCount) {
            super.start(entity, tickCount);
            progress = 0;
        }

        public Class<? extends Unit> getResultClass() {
            return resultClass;
        }

        public int getDuration() {
            return duration;
        }

        public float getProgress() {
            return progress / duration;
        }

        @Override
        public void execute(Entity entity, int tickCount) {
            if (entity instanceof Building building) {
                if (progress >= duration) {
                    Unit result;
                    try {
                        Constructor<? extends Unit> con = resultClass.getConstructor(float.class, float.class, Orientation.class, Player.class);
                        result = con.newInstance(building.x, building.y + Tile.TILE_CENTER_Y, building.orientation, building.owner);
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                             IllegalAccessException ex) {
                        System.out.println("Failed to create unit." + ex);
                        finished = true;
                        return;
                    }
                    if (resultClass != Heli.class) {
                        try {
                            result.pushCommand(new MoveCommand(result.x, result.y, result.x, result.y, pathfinder));
                        } catch (NoPathFound ignored) {

                        }
                    }
                    if (building.owner instanceof Human) {
                        SoundManager.play(SoundManager.SOUND_TADA_1, false, 1.0f);
                    }
                    building.owner.addEntity(result);
                    finished = true;
                } else {
                    progress += Math.min(building.owner.getPowerFactor(), 1.0f);
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
        public void finish(Entity entity, int tickCount, boolean done) {
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setProduce(CommandProto.CommandProduce.newBuilder()
                            .setProgress(progress)
                            .setDuration(duration)
                            .setResultClass(PlayableEntity.toEntityClass(resultClass))
                            .build())
                    .build();
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

        public CollectSpiceCommand(CommandProto.CommandCollectSpice collectSpice, Level level) {
            this.move = new MoveCommand(collectSpice.getMove());
            this.target = level.getTile(collectSpice.getTarget().getX(), collectSpice.getTarget().getY());
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
        public void finish(Entity entity, int tickCount, boolean done) {
        }

        @Override
        public CommandProto.Command serialize() {
            BaseProto.Tile tile = BaseProto.Tile.newBuilder()
                    .setX(target.getX())
                    .setY(target.getY())
                    .build();

            return CommandProto.Command.newBuilder()
                    .setCollectSpice(CommandProto.CommandCollectSpice.newBuilder()
                            .setTarget(tile)
                            .setMove(move.serialize().getMove())
                            .build())
                    .build();
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

        public DropOffSpiceCommand(CommandProto.CommandDropOffSpice dropOffSpice) {
            this.move = new MoveCommand(dropOffSpice.getMove());
            // TODO: Fix this
            //      this.target = (Refinery) Entity.getEntity(dropOffSpice.getTargetId());
            this.target = null;
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
        public void finish(Entity entity, int tickCount, boolean done) {

        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setDropOffSpice(CommandProto.CommandDropOffSpice.newBuilder()
                            .setTargetId(target.getId())
                            .setMove(move.serialize().getMove())
                            .build())
                    .build();
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

