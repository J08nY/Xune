package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.level.Level;

public abstract class CommandStrategy {
    public abstract Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY);

    public static class GroundStrategy extends CommandStrategy {
        @Override
        public Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY) {
            try {
                if (other != null) {
                    return new Command.MoveAndAttackCommand(entity.x, entity.y, level.getPathfinder(), other);
                } else {
                    return new Command.MoveCommand(entity.x, entity.y, levelX, levelY, level.getPathfinder());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("No path found");
                return null;
            }
        }
    }

    public static class AirStrategy extends CommandStrategy {
        @Override
        public Command createCommand(Entity entity, Entity other, Level level, float levelX, float levelY) {
            if (other != null) {
                return new Command.FlyAndAttackCommand(entity.x, entity.y, other);
            } else {
                return new Command.FlyCommand(entity.x, entity.y, levelX, levelY);
            }
        }
    }
}
