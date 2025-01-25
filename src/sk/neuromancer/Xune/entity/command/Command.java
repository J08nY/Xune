package sk.neuromancer.Xune.entity.command;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.EntityReference;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.TileReference;
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
    protected boolean started;

    public boolean isStarted(Entity entity) {
        return started;
    }

    public void start(Entity entity) {
        started = true;
    }

    public abstract void execute(Entity entity);

    public abstract boolean isFinished(Entity entity);

    public abstract void finish(Entity entity, boolean done);

    public abstract CommandProto.Command serialize();

    public static Command deserialize(CommandProto.Command command, Level level) {
        boolean started = command.getStarted();
        switch (command.getCmdCase()) {
            case FLY -> {
                return new FlyCommand(command.getFly(), started);
            }
            case MOVE -> {
                return new MoveCommand(command.getMove(), started);
            }
            case ATTACK -> {
                return new AttackCommand(command.getAttack(), level, started);
            }
            case MOVEANDATTACK -> {
                return new MoveAndAttackCommand(command.getMoveAndAttack(), level, started);
            }
            case FLYANDATTACK -> {
                return new FlyAndAttackCommand(command.getFlyAndAttack(), level, started);
            }
            case PRODUCE -> {
                return new ProduceCommand(command.getProduce(), level.getPathfinder(), started);
            }
            case COLLECTSPICE -> {
                return new CollectSpiceCommand(command.getCollectSpice(), level, started);
            }
            case DROPOFFSPICE -> {
                return new DropOffSpiceCommand(command.getDropOffSpice(), level, started);
            }
            default -> {
                throw new IllegalArgumentException("Unknown command type.");
            }
        }
    }

    public static class FlyCommand extends Command {
        private final float fromX, fromY, toX, toY;

        public FlyCommand(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public FlyCommand(CommandProto.CommandFly fly, boolean started) {
            this.fromX = fly.getFrom().getX();
            this.fromY = fly.getFrom().getY();
            this.toX = fly.getTo().getX();
            this.toY = fly.getTo().getY();
            this.started = started;
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
        public void execute(Entity entity) {
            if (entity instanceof Unit unit) {
                unit.move(toX, toY);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
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
                    .setStarted(started)
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

        public MoveCommand(CommandProto.CommandMove move, boolean started) {
            this.fromX = move.getFrom().getX();
            this.fromY = move.getFrom().getY();
            this.toX = move.getTo().getX();
            this.toY = move.getTo().getY();
            this.path = new Path(move.getPoints());
            this.next = move.getNext();
            this.started = started;
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
        public void execute(Entity entity) {
            if (entity instanceof Unit unit) {
                unit.move(getNext().getLevelX(), getNext().getLevelY());
                update(unit);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
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
                    .setStarted(started)
                    .build();
        }
    }

    public static class AttackCommand extends Command {
        private final EntityReference target;
        private final boolean keep;

        public AttackCommand(Entity target, boolean keep) {
            this.target = new EntityReference(target);
            this.keep = keep;
        }

        public AttackCommand(Entity target) {
            this(target, true);
        }

        public AttackCommand(CommandProto.CommandAttack attack, Level level, boolean started) {
            this.target = new EntityReference(attack.getTargetId(), level);
            this.keep = attack.getKeep();
            this.started = started;
        }

        public EntityReference getTargetReference() {
            return target;
        }

        public Entity getTarget() {
            return target.resolve();
        }

        @Override
        public boolean isFinished(Entity entity) {
            Entity t = target.resolve();
            if (t == null) {
                // TODO: Maybe raise?
                return false;
            }

            if (keep) {
                return t.health <= 0;
            } else {
                return t.health <= 0 || !entity.inSight(t);
            }
        }

        @Override
        public void execute(Entity entity) {
            if (entity instanceof Unit unit) {
                Entity t = target.resolve();
                if (t == null) {
                    return;
                }
                unit.face(t.x, t.y);
                unit.attack(t);
                unit.setAttacking(true, t);
                t.setUnderAttack(true, unit);
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
            Entity t = target.resolve();
            if (t == null) {
                return;
            }
            entity.setAttacking(false, target);
            t.setUnderAttack(false, entity);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setAttack(CommandProto.CommandAttack.newBuilder()
                            .setTargetId(target.getId())
                            .setKeep(keep)
                            .build())
                    .setStarted(started)
                    .build();
        }
    }

    public static class MoveAndAttackCommand extends Command {
        private MoveCommand move;
        private AttackCommand attack;
        private Pathfinder pathfinder;
        private EntityReference target;
        private float targetX, targetY;
        private int targetMoved;

        public MoveAndAttackCommand(float fromX, float fromY, Pathfinder pathFinder, Entity target) {
            this.move = new MoveCommand(fromX, fromY, target.x, target.y, pathFinder);
            this.attack = new AttackCommand(target);
            this.pathfinder = pathFinder;
            this.target = new EntityReference(target);
            this.targetX = target.x;
            this.targetY = target.y;
        }

        public MoveAndAttackCommand(CommandProto.CommandMoveAndAttack moveAndAttack, Level level, boolean started) {
            this.move = new MoveCommand(moveAndAttack.getMove(), started);
            this.attack = new AttackCommand(moveAndAttack.getAttack(), level, started);
            this.pathfinder = level.getPathfinder();
            this.target = attack.getTargetReference();
            this.targetX = Float.NaN;
            this.targetY = Float.NaN;
            this.started = started;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return attack.isFinished(entity);
        }

        @Override
        public void execute(Entity entity) {
            if (entity instanceof Unit unit) {
                Entity t = target.resolve();
                if (t == null) {
                    return;
                }

                if (unit.inRange(t)) {
                    attack.execute(entity);
                } else {
                    if (t.x != targetX || t.y != targetY) {
                        targetMoved++;
                    }
                    if (targetMoved == Config.TPS) {
                        targetMoved = 0;
                        try {
                            targetX = t.x;
                            targetY = t.y;
                            move = new MoveCommand(entity.x, entity.y, t.x, t.y, pathfinder);
                        } catch (NoPathFound e) {
                            return;
                        }
                    }
                    move.execute(entity);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
            attack.finish(entity, done);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setMoveAndAttack(CommandProto.CommandMoveAndAttack.newBuilder()
                            .setMove(move.serialize().getMove())
                            .setAttack(attack.serialize().getAttack())
                            .build())
                    .setStarted(started)
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
        private EntityReference target;
        private float targetX, targetY;
        private int targetMoved;

        public FlyAndAttackCommand(float fromX, float fromY, Entity target) {
            this.move = new FlyCommand(fromX, fromY, target.x, target.y);
            this.attack = new AttackCommand(target);
            this.target = new EntityReference(target);
            this.targetX = target.x;
            this.targetY = target.y;
        }

        public FlyAndAttackCommand(CommandProto.CommandFlyAndAttack flyAndAttack, Level level, boolean started) {
            this.move = new FlyCommand(flyAndAttack.getMove(), started);
            this.attack = new AttackCommand(flyAndAttack.getAttack(), level, started);
            this.target = attack.getTargetReference();
            this.targetX = Float.NaN;
            this.targetY = Float.NaN;
            this.started = started;
        }

        @Override
        public boolean isFinished(Entity entity) {
            return attack.isFinished(entity);
        }

        @Override
        public void execute(Entity entity) {
            if (entity instanceof Unit unit) {
                Entity t = target.resolve();
                if (t == null) {
                    return;
                }
                if (unit.inRange(t)) {
                    attack.execute(entity);
                } else {
                    if (t.x != targetX || t.y != targetY) {
                        targetMoved++;
                    }
                    if (targetMoved == Config.TPS) {
                        targetMoved = 0;
                        targetX = t.x;
                        targetY = t.y;
                        move = new FlyCommand(unit.x, unit.y, t.x, t.y);
                    }
                    move.execute(entity);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a unit.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
            attack.finish(entity, done);
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setFlyAndAttack(CommandProto.CommandFlyAndAttack.newBuilder()
                            .setMove(move.serialize().getFly())
                            .setAttack(attack.serialize().getAttack())
                            .build())
                    .setStarted(started)
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

        public ProduceCommand(CommandProto.CommandProduce produce, Pathfinder pathfinder, boolean started) {
            this.resultClass = PlayableEntity.fromEntityClass(produce.getResultClass()).asSubclass(Unit.class);
            this.progress = produce.getProgress();
            this.duration = produce.getDuration();
            this.finished = produce.getFinished();
            this.pathfinder = pathfinder;
            this.started = started;
        }

        @Override
        public void start(Entity entity) {
            super.start(entity);
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
        public void execute(Entity entity) {
            if (entity instanceof Building building) {
                if (progress >= duration) {
                    Unit result;
                    try {
                        Constructor<? extends Unit> con = resultClass.getConstructor(float.class, float.class, Orientation.class, Player.class);
                        result = con.newInstance(building.x, building.y + Tile.TILE_CENTER_Y, Orientation.SOUTH, building.getOwner());
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
                    if (building.getOwner() instanceof Human) {
                        SoundManager.play(SoundManager.SOUND_TADA_1, false, 1.0f);
                    }
                    building.getOwner().addEntity(result);
                    finished = true;
                } else {
                    progress += Math.min(building.getOwner().getPowerFactor(), 1.0f);
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
        public void finish(Entity entity, boolean done) {
        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setProduce(CommandProto.CommandProduce.newBuilder()
                            .setProgress(progress)
                            .setDuration(duration)
                            .setResultClass(PlayableEntity.toEntityClass(resultClass))
                            .setFinished(finished)
                            .build())
                    .setStarted(started)
                    .build();
        }
    }

    public static class CollectSpiceCommand extends Command {
        private final MoveCommand move;
        private final TileReference target;

        public CollectSpiceCommand(float fromX, float fromY, Pathfinder pathfinder, Tile target) {
            float targetX = Level.tileToCenterLevelX(target.getX(), target.getY());
            float targetY = Level.tileToCenterLevelY(target.getX(), target.getY());
            this.move = new MoveCommand(fromX, fromY, targetX, targetY, pathfinder);
            this.target = new TileReference(target);
        }

        public CollectSpiceCommand(CommandProto.CommandCollectSpice collectSpice, Level level, boolean started) {
            this.move = new MoveCommand(collectSpice.getMove(), started);
            this.target = new TileReference(collectSpice.getTarget().getX(), collectSpice.getTarget().getY(), level);
            this.started = started;
        }

        public TileReference getTargetReference() {
            return target;
        }

        public Tile getTarget() {
            return target.resolve();
        }

        public boolean collecting(Entity entity) {
            return move.isFinished(entity) && !isFinished(entity);
        }

        @Override
        public void execute(Entity entity) {
            if (entity instanceof Harvester harvester) {
                Tile t = target.resolve();
                if (t == null) {
                    return;
                }

                if (!move.isFinished(entity)) {
                    move.execute(entity);
                } else {
                    harvester.collectSpice(t);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public boolean isFinished(Entity entity) {
            Tile t = target.resolve();
            if (t == null) {
                return false;
            }

            if (entity instanceof Harvester harvester) {
                return harvester.isFull() || t.getSpice() == 0;
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {
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
                    .setStarted(started)
                    .build();
        }
    }

    public static class DropOffSpiceCommand extends Command {
        private final MoveCommand move;
        private final EntityReference target;

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
            this.target = new EntityReference(target);
        }

        public DropOffSpiceCommand(CommandProto.CommandDropOffSpice dropOffSpice, Level level, boolean started) {
            this.move = new MoveCommand(dropOffSpice.getMove(), started);
            this.target = new EntityReference(dropOffSpice.getTargetId(), level);
            this.started = started;
        }

        public boolean dropping(Entity entity) {
            return move.isFinished(entity) && !isFinished(entity);
        }

        @Override
        public void execute(Entity entity) {
            if (entity instanceof Harvester harvester) {
                Entity t = target.resolve();
                if (t == null) {
                    return;
                }

                if (!move.isFinished(entity)) {
                    move.execute(entity);
                } else {
                    harvester.dropOffSpice((Refinery) t);
                }
            } else {
                throw new IllegalArgumentException("Entity must be a harvester.");
            }
        }

        @Override
        public void finish(Entity entity, boolean done) {

        }

        @Override
        public CommandProto.Command serialize() {
            return CommandProto.Command.newBuilder()
                    .setDropOffSpice(CommandProto.CommandDropOffSpice.newBuilder()
                            .setTargetId(target.getId())
                            .setMove(move.serialize().getMove())
                            .build())
                    .setStarted(started)
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

