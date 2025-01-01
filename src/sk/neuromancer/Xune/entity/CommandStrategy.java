package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static sk.neuromancer.Xune.game.Game.TPS;

public abstract class CommandStrategy {

    public abstract Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY);

    public abstract Command defaultBehavior(Entity entity, Level level);

    public static class GroundAttackStrategy extends CommandStrategy {
        @Override
        public Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY) {
            try {
                if (other != null) {
                    return new Command.MoveAndAttackCommand(entity.x, entity.y, level.getPathfinder(), other);
                } else {
                    return new Command.MoveCommand(entity.x, entity.y, levelX, levelY, level.getPathfinder());
                }
            } catch (NoPathFound e) {
                return null;
            }
        }

        @Override
        public Command defaultBehavior(Entity entity, Level level) {
            if (entity instanceof Unit unit) {
                // If there is an enemy in range: attack
                for (Entity enemyEntity : level.getBot().entities) {
                    if (unit.inRange(enemyEntity)) {
                        return new Command.AttackCommand(enemyEntity);
                    }
                }
                // If there is an enemy attacking us: respond
            }
            return null;
        }
    }

    public static class AirAttackStrategy extends CommandStrategy {
        @Override
        public Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY) {
            if (other != null) {
                return new Command.FlyAndAttackCommand(entity.x, entity.y, other);
            } else {
                return new Command.FlyCommand(entity.x, entity.y, levelX, levelY);
            }
        }

        @Override
        public Command defaultBehavior(Entity entity, Level level) {
            // Example default behavior: hover around the current position
            /*
            float hoverX = entity.x + (float) (Math.random() * 100 - 50);
            float hoverY = entity.y + (float) (Math.random() * 100 - 50);
            return new Command.FlyCommand(entity.x, entity.y, hoverX, hoverY)
             */
            return null;
        }
    }

    public static class SpiceCollectStrategy extends CommandStrategy {
        Map<Harvester, Integer> harvesters = new HashMap<>();

        @Override
        public Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY) {
            try {
                if (other instanceof Refinery refinery) {
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
        public Command defaultBehavior(Entity entity, Level level) {
            if (entity instanceof Harvester harvester) {
                Player owner = harvester.owner;
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
                    for (Iterator<Entity> it = level.findClosestEntity(entity.x, entity.y, e -> e instanceof Entity.PlayableEntity playable && playable.owner == harvester.owner && e instanceof Refinery); it.hasNext(); ) {
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
                harvesters.put(harvester, TPS);
                return null;
            } else {
                throw new IllegalArgumentException("Entity is not a harvester");
            }
        }
    }
}
