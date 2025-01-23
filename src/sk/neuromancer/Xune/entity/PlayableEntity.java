package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Clickable;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.proto.CommandProto;
import sk.neuromancer.Xune.proto.EntityStateProto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public abstract class PlayableEntity extends Entity {
    protected static final Map<Class<? extends sk.neuromancer.Xune.entity.PlayableEntity>, List<Prerequisite>> prerequisitesMap = new HashMap<>();
    protected static final Map<Class<? extends sk.neuromancer.Xune.entity.PlayableEntity>, Integer> costMap = new HashMap<>();
    protected static final Map<Class<? extends sk.neuromancer.Xune.entity.PlayableEntity>, Integer> buildTimeMap = new HashMap<>();
    protected static final Map<Class<? extends Entity>, Integer> baseSpriteMap = new HashMap<>();
    protected Player owner;
    protected List<Command> commands;
    protected Flag flag;
    protected boolean isSelected;

    public PlayableEntity(float x, float y, Player owner) {
        super(x, y);
        this.owner = owner;
        this.commands = new LinkedList<>();
        this.flag = owner.getFlag();
    }

    public PlayableEntity(EntityStateProto.PlayableEntityState savedState, Player owner) {
        super(savedState.getEntity(), owner.getLevel());
        this.owner = owner;
        this.flag = Flag.deserialize(savedState.getFlag());
        this.commands = new LinkedList<>();
        for (CommandProto.Command commandState : savedState.getCommandsList()) {
            Command command = Command.deserialize(commandState, owner.getLevel());
            this.commands.add(command);
        }
    }

    protected static void setBaseSprite(Class<? extends Entity> klass, int sprite) {
        baseSpriteMap.put(klass, sprite);
    }

    public static int getBaseSprite(Class<? extends Entity> klass) {
        return baseSpriteMap.get(klass);
    }

    public void select() {
        this.isSelected = true;
    }

    public void unselect() {
        this.isSelected = false;
    }

    @Override
    public void tick(int tickCount) {
        if (!commands.isEmpty()) {
            Command current = commands.getFirst();
            if (!current.isStarted(this)) {
                current.start(this, tickCount);
            }
            current.execute(this, tickCount);
            if (current.isFinished(this)) {
                commands.removeFirst();
                current.finish(this, tickCount, true);
            }
        }
    }

    @Override
    public void render() {
        glPushMatrix();
        float depth = computeDepth();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
        this.sprite.render();
        glPopMatrix();
        if (isSelected) {
            glPushMatrix();
            glTranslatef(x, y, 0);
            for (Clickable area : clickableAreas) {
                if (area instanceof Renderable renderable) {
                    renderable.render();
                }
            }
            glPopMatrix();
        }
    }

    protected EntityStateProto.PlayableEntityState toPlayableEntityState() {
        EntityStateProto.PlayableEntityState.Builder builder = EntityStateProto.PlayableEntityState.newBuilder();

        // Convert EntityState
        EntityStateProto.EntityState entityState = toEntityState();
        builder.setEntity(entityState);

        // Set other PlayableEntityState fields
        builder.setFlag(this.flag.serialize());
        builder.setOwnerId(this.owner.getId());

        // Convert commands
        for (Command command : this.commands) {
            builder.addCommands(command.serialize());
        }

        return builder.build();
    }

    protected void fromPlayableEntityState(EntityStateProto.PlayableEntityState state, Player owner) {
        Level level = owner.getLevel();

        // Convert EntityState
        fromEntityState(state.getEntity(), level);

        // Set other PlayableEntityState fields
        this.flag = Flag.deserialize(state.getFlag());
        this.owner = owner;

        // Convert commands
        // TODO: Handle command diffs.
        this.commands = new LinkedList<>();
        for (CommandProto.Command commandState : state.getCommandsList()) {
            Command command = Command.deserialize(commandState, level);
            this.commands.add(command);
        }
    }

    @Override
    public boolean isEnemyOf(Entity other) {
        if (other instanceof Worm) {
            return true;
        }
        if (other instanceof Road) {
            return false;
        }
        return ((PlayableEntity) other).owner != this.owner;
    }

    public abstract boolean isStatic();

    public Player getOwner() {
        return owner;
    }

    public boolean hasCommands() {
        return !this.commands.isEmpty();
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Command currentCommand() {
        return this.commands.isEmpty() ? null : this.commands.getFirst();
    }

    public void sendCommand(Command c) {
        this.commands.add(c);
    }

    public void pushCommand(Command c) {
        this.commands.forEach(cmd -> cmd.finish(this, Game.currentTick(), false));
        this.commands.clear();
        this.commands.add(c);
    }

    protected static void setCost(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass, int cost) {
        costMap.put(klass, cost);
    }

    public static int getCost(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass) {
        return costMap.get(klass);
    }

    protected static void setBuildTime(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass, int buildTime) {
        buildTimeMap.put(klass, buildTime);
    }

    public static int getBuildTime(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass) {
        return buildTimeMap.get(klass);
    }

    protected static void registerPrerequisites(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass, List<Prerequisite> prerequisites) {
        prerequisitesMap.put(klass, prerequisites);
    }

    public static boolean canBeBuilt(Class<? extends sk.neuromancer.Xune.entity.PlayableEntity> klass, Player owner) {
        int cost = costMap.get(klass);
        if (cost > owner.getMoney()) {
            return false;
        }
        List<Prerequisite> prerequisites = prerequisitesMap.get(klass);
        if (prerequisites == null) {
            return true;
        }
        for (Prerequisite prerequisite : prerequisites) {
            if (!prerequisite.isMet(owner)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "." + flag + " at (" + x + "," + y + ").";
    }
}
