package sk.neuromancer.Xune.entity.command;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.EntityReference;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static sk.neuromancer.Xune.game.Config.TPS;

public abstract class CommandStrategy {

    public abstract Command onClick(PlayableEntity entity, Entity clicked, Level level, float levelX, float levelY);

    public abstract Command defaultBehavior(PlayableEntity entity, Level level);

    public static class GroundAttackStrategy extends CommandStrategy {
        @Override
        public Command onClick(PlayableEntity entity, Entity clicked, Level level, float levelX, float levelY) {
            try {
                if (clicked != null) {
                    if (clicked instanceof PlayableEntity playable && playable.getOwner() == entity.getOwner()) {
                        return new Command.MoveCommand(entity.x, entity.y, levelX, levelY, level.getPathfinder());
                    } else {
                        return new Command.MoveAndAttackCommand(entity.x, entity.y, level.getPathfinder(), clicked);
                    }
                } else {
                    return new Command.MoveCommand(entity.x, entity.y, levelX, levelY, level.getPathfinder());
                }
            } catch (NoPathFound e) {
                return null;
            }
        }

        @Override
        public Command defaultBehavior(PlayableEntity entity, Level level) {
            if (entity instanceof Unit unit) {
                try {
                    // If there is an enemy attacking us: respond
                    if (unit.isUnderAttack()) {
                        EntityReference attacker = unit.getAttackers().stream().min(Comparator.comparingDouble(e -> Math.hypot(e.resolve(level).x - unit.x, e.resolve(level).y - unit.y))).orElse(null);
                        return new Command.MoveAndAttackCommand(unit.x, unit.y, level.getPathfinder(), attacker.resolve(level));
                    }
                    // If there is an enemy in range: attack
                    for (Entity other : level.getEntities()) {
                        if (unit.isEnemyOf(other) && unit.inRange(other)) {
                            return new Command.AttackCommand(other, false);
                        }
                    }
                } catch (NoPathFound e) {
                    return null;
                }
            }
            return null;
        }
    }

    public static class AirAttackStrategy extends CommandStrategy {
        @Override
        public Command onClick(PlayableEntity entity, Entity clicked, Level level, float levelX, float levelY) {
            if (clicked != null) {
                if (clicked instanceof PlayableEntity playable && playable.getOwner() == entity.getOwner()) {
                    return new Command.FlyCommand(entity.x, entity.y, levelX, levelY);
                } else {
                    return new Command.FlyAndAttackCommand(entity.x, entity.y, clicked);
                }
            } else {
                return new Command.FlyCommand(entity.x, entity.y, levelX, levelY);
            }
        }

        @Override
        public Command defaultBehavior(PlayableEntity entity, Level level) {
            if (entity instanceof Unit unit) {
                // If there is an enemy attacking us: respond
                if (unit.isUnderAttack()) {
                    EntityReference attacker = unit.getAttackers().stream().min(Comparator.comparingDouble(e -> Math.hypot(e.resolve(level).x - unit.x, e.resolve(level).y - unit.y))).orElse(null);
                    return new Command.FlyAndAttackCommand(unit.x, unit.y, attacker.resolve(level));
                }
                // If there is an enemy in range: attack
                for (Entity other : level.getEntities()) {
                    if (unit.isEnemyOf(other) && unit.inRange(other)) {
                        return new Command.AttackCommand(other, false);
                    }
                }
            }
            return null;
        }
    }

    public static class SpiceCollectStrategy extends CommandStrategy {
        Map<Harvester, Integer> harvesters = new HashMap<>();

        @Override
        public Command onClick(PlayableEntity entity, Entity clicked, Level level, float levelX, float levelY) {
            try {
                if (clicked instanceof Refinery refinery && refinery.getOwner() == entity.getOwner()) {
                    return new Command.DropOffSpiceCommand(entity.x, entity.y, level.getPathfinder(), refinery);
                }

                int tileX = Level.levelToTileX(levelX, levelY);
                int tileY = Level.levelToTileY(levelX, levelY);
                Tile target = level.getTile(tileX, tileY);
                if (target.isSpicy() && target.getSpice() > 0) {
                    return new Command.CollectSpiceCommand(entity.x, entity.y, level.getPathfinder(), target);
                } else {
                    return new Command.MoveCommand(entity.x, entity.y, levelX, levelY, level.getPathfinder());
                }
            } catch (NoPathFound e) {
                return null;
            }
        }

        @Override
        public Command defaultBehavior(PlayableEntity entity, Level level) {
            if (entity instanceof Harvester harvester) {
                Player owner = harvester.getOwner();
                if (harvesters.containsKey(harvester)) {
                    int timeout = harvesters.get(harvester);
                    if (timeout > 0) {
                        harvesters.put(harvester, timeout - 1);
                        return null;
                    } else {
                        harvesters.remove(harvester);
                    }
                }

                if (harvester.isFull()) {
                    for (Iterator<Entity> it = level.findClosestEntity(entity.x, entity.y, e -> e instanceof PlayableEntity playable && playable.getOwner() == harvester.getOwner() && e instanceof Refinery); it.hasNext(); ) {
                        Refinery refinery = (Refinery) it.next();
                        try {
                            return new Command.DropOffSpiceCommand(entity.x, entity.y, level.getPathfinder(), refinery);
                        } catch (NoPathFound ignored) {
                        }
                    }
                } else {
                    Tile harvesterTile = level.tileAt(harvester.x, harvester.y);
                    for (Iterator<Tile> it = level.findClosestTile(harvesterTile, tile -> owner.isTileDiscovered(tile) && tile.isSpicy() && tile.getSpice() > 0); it.hasNext(); ) {
                        Tile tile = it.next();
                        try {
                            return new Command.CollectSpiceCommand(entity.x, entity.y, level.getPathfinder(), tile);
                        } catch (NoPathFound ignored) {
                        }
                    }
                }
                // If there is no spice to collect or refinery to dropoff to, wait for a bit
                harvesters.put(harvester, TPS);
                return null;
            } else {
                throw new IllegalArgumentException("Entity is not a harvester");
            }
        }
    }
}
