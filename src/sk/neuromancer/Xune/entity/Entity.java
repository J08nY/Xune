package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.game.Clickable;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.Sprite;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.CommandProto;
import sk.neuromancer.Xune.proto.EntityStateProto;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public abstract class Entity implements Renderable, Tickable, Clickable {
    protected static final Map<Class<? extends Entity>, Integer> healthMap = new HashMap<>();
    protected static final Map<Class<? extends Entity>, Integer> sightMap = new HashMap<>();
    protected static final Map<Class<? extends Entity>, Integer> deathSoundMap = new HashMap<>();
    protected final Random rand = new Random();

    protected long id;
    public float x, y;
    public int health;
    protected final int maxHealth;
    protected final int sight;
    protected final int s2;
    protected Orientation orientation;
    protected Sprite sprite;
    protected final List<Clickable> clickableAreas = new ArrayList<>();

    protected boolean attacking;
    protected Entity attackTarget;
    protected boolean underAttack;
    protected Entity attacker;

    public Entity(float x, float y) {
        this.id = rand.nextLong();
        this.x = x;
        this.y = y;
        this.health = getMaxHealth(getClass());
        this.maxHealth = getMaxHealth(getClass());
        this.sight = getSight(getClass());
        this.s2 = sight * sight;
    }

    public Entity(EntityStateProto.EntityState savedState) {
        this.id = savedState.getId();
        this.x = savedState.getPosition().getX();
        this.y = savedState.getPosition().getY();
        this.orientation = Orientation.deserialize(savedState.getOrientation());

        if (savedState.hasAttackingState()) {
            EntityStateProto.EntityState.AttackingState attackingState = savedState.getAttackingState();
            this.attacking = attackingState.getAttacking();
            //this.attackTarget = Game.getEntity(attackingState.getTargetId());
        }
        if (savedState.hasAttackedState()) {
            EntityStateProto.EntityState.AttackedState attackedState = savedState.getAttackedState();
            this.underAttack = attackedState.getUnderAttack();
            //this.attacker = Game.getEntity(attackedState.getAttackerId());
        }
        this.health = savedState.getHealth();
        this.maxHealth = getMaxHealth(getClass());
        this.sight = getSight(getClass());
        this.s2 = sight * sight;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        for (Clickable area : clickableAreas) {
            area.setPosition(x, y);
        }
    }

    @Override
    public boolean intersects(float x, float y) {
        for (Clickable area : clickableAreas) {
            if (area.intersects(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean intersects(float fromX, float fromY, float toX, float toY) {
        for (Clickable area : clickableAreas) {
            if (area.intersects(fromX, fromY, toX, toY)) {
                return true;
            }
        }
        return false;
    }

    public void heal(int health) {
        this.health += health;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void setAttacking(boolean attacking, Entity target) {
        this.attacking = attacking;
        this.attackTarget = target;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public Entity getAttackTarget() {
        return attackTarget;
    }

    public void setUnderAttack(boolean underAttack, Entity attacker) {
        this.underAttack = underAttack;
        this.attacker = attacker;
    }

    public boolean isUnderAttack() {
        return underAttack;
    }

    public Entity getAttacker() {
        return attacker;
    }

    public long getId() {
        return id;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
        this.sprite.render();
        glPopMatrix();
    }

    public static BaseProto.EntityClass toEntityClass(Class<? extends Entity> klass) {
        if (klass == null) {
            return BaseProto.EntityClass.NULL;
        }
        return switch (klass.getSimpleName()) {
            case "Worm" -> BaseProto.EntityClass.WORM;
            case "Base" -> BaseProto.EntityClass.BASE;
            case "Barracks" -> BaseProto.EntityClass.BARRACKS;
            case "Factory" -> BaseProto.EntityClass.FACTORY;
            case "Helipad" -> BaseProto.EntityClass.HELIPAD;
            case "Powerplant" -> BaseProto.EntityClass.POWERPLANT;
            case "Refinery" -> BaseProto.EntityClass.REFINERY;
            case "Silo" -> BaseProto.EntityClass.SILO;
            case "Buggy" -> BaseProto.EntityClass.BUGGY;
            case "Harvester" -> BaseProto.EntityClass.HARVESTER;
            case "Heli" -> BaseProto.EntityClass.HELI;
            case "Soldier" -> BaseProto.EntityClass.SOLDIER;
            default -> BaseProto.EntityClass.UNRECOGNIZED;
        };
    }

    public static Class<? extends Entity> fromEntityClass(BaseProto.EntityClass klass) {
        return switch (klass) {
            case WORM -> Worm.class;
            case BASE -> Base.class;
            case BARRACKS -> Barracks.class;
            case FACTORY -> Factory.class;
            case HELIPAD -> Helipad.class;
            case POWERPLANT -> Powerplant.class;
            case REFINERY -> Refinery.class;
            case SILO -> Silo.class;
            case BUGGY -> Buggy.class;
            case HARVESTER -> Harvester.class;
            case HELI -> Heli.class;
            case SOLDIER -> Soldier.class;
            case UNRECOGNIZED, NULL -> null;
        };
    }

    protected EntityStateProto.EntityState toEntityState() {
        return EntityStateProto.EntityState.newBuilder()
                .setId(this.getId())
                .setKlass(toEntityClass(this.getClass()))
                .setPosition(BaseProto.Position.newBuilder().setX(this.x).setY(this.y).build())
                .setOrientation(BaseProto.Orientation.forNumber(this.orientation.ordinal()))
                .setHealth(this.health)
                .setAttackingState(EntityStateProto.EntityState.AttackingState.newBuilder()
                        .setTargetId(this.attackTarget != null ? this.attackTarget.getId() : 0)
                        .setAttacking(this.attacking)
                        .build())
                .setAttackedState(EntityStateProto.EntityState.AttackedState.newBuilder()
                        .setAttackerId(this.attacker != null ? this.attacker.getId() : 0)
                        .setUnderAttack(this.underAttack)
                        .build()).build();
    }

    protected static void setMaxHealth(Class<? extends Entity> klass, int health) {
        healthMap.put(klass, health);
    }

    public static int getMaxHealth(Class<? extends Entity> klass) {
        return healthMap.get(klass);
    }

    protected static void setDeathSound(Class<? extends Entity> klass, int sound) {
        deathSoundMap.put(klass, sound);
    }

    public static int getDeathSound(Class<? extends Entity> klass) {
        return deathSoundMap.get(klass);
    }

    protected static void setSight(Class<? extends Entity> klass, int sight) {
        sightMap.put(klass, sight);
    }

    public int getSight(Class<? extends Entity> klass) {
        return sightMap.get(klass);
    }

    public boolean inSight(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy <= s2;
    }

    public boolean inSight(Entity target) {
        return inSight(target.x, target.y);
    }

    public abstract boolean isEnemyOf(Entity other);

    protected float computeDepth(Game game) {
        return game.getView().getScreenY(y) / game.getWindow().getHeight();
    }

    public static void initClasses() {
        String[] classes = new String[]{
                "sk.neuromancer.Xune.entity.building.Base",
                "sk.neuromancer.Xune.entity.building.Barracks",
                "sk.neuromancer.Xune.entity.building.Factory",
                "sk.neuromancer.Xune.entity.building.Helipad",
                "sk.neuromancer.Xune.entity.building.Powerplant",
                "sk.neuromancer.Xune.entity.building.Refinery",
                "sk.neuromancer.Xune.entity.building.Silo",
                "sk.neuromancer.Xune.entity.unit.Buggy",
                "sk.neuromancer.Xune.entity.unit.Harvester",
                "sk.neuromancer.Xune.entity.unit.Heli",
                "sk.neuromancer.Xune.entity.unit.Soldier",
        };
        try {
            for (String klass : classes) {
                Class.forName(klass);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static abstract class PlayableEntity extends Entity {
        protected static final Map<Class<? extends PlayableEntity>, List<Prerequisite>> prerequisitesMap = new HashMap<>();
        protected static final Map<Class<? extends PlayableEntity>, Integer> costMap = new HashMap<>();
        protected static final Map<Class<? extends PlayableEntity>, Integer> buildTimeMap = new HashMap<>();
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
            super(savedState.getEntity());
            this.owner = owner;
            this.flag = Flag.deserialize(savedState.getFlag());
            this.commands = new LinkedList<>();
            for (CommandProto.Command commandState : savedState.getCommandsList()) {
                switch (commandState.getCmdCase()) {
                    case FLY -> {
                        Command.FlyCommand fly = new Command.FlyCommand(commandState.getFly());
                        this.commands.add(fly);
                    }
                    case MOVE -> {
                        Command.MoveCommand move = new Command.MoveCommand(commandState.getMove());
                        this.commands.add(move);
                    }
                    case ATTACK -> {
                        Command.AttackCommand attack = new Command.AttackCommand(commandState.getAttack());
                        this.commands.add(attack);
                    }
                    case MOVEANDATTACK -> {
                        //TODO this pathfinder thing is nasty.
                        Command.MoveAndAttackCommand moveAndAttack = new Command.MoveAndAttackCommand(commandState.getMoveAndAttack(), owner.getLevel().getPathfinder());
                        this.commands.add(moveAndAttack);
                    }
                    case FLYANDATTACK -> {
                        Command.FlyAndAttackCommand flyAndAttack = new Command.FlyAndAttackCommand(commandState.getFlyAndAttack());
                        this.commands.add(flyAndAttack);
                    }
                    case PRODUCE -> {
                        Command.ProduceCommand produce = new Command.ProduceCommand(commandState.getProduce(), owner.getLevel().getPathfinder());
                        this.commands.add(produce);
                    }
                    case COLLECTSPICE -> {
                        Command.CollectSpiceCommand collectSpice = new Command.CollectSpiceCommand(commandState.getCollectSpice(), owner.getLevel());
                        this.commands.add(collectSpice);
                    }
                    case DROPOFFSPICE -> {
                        Command.DropOffSpiceCommand dropOffSpice = new Command.DropOffSpiceCommand(commandState.getDropOffSpice());
                        this.commands.add(dropOffSpice);
                    }
                }
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
            float depth = computeDepth(owner.getGame());
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

        protected static void setCost(Class<? extends PlayableEntity> klass, int cost) {
            costMap.put(klass, cost);
        }

        public static int getCost(Class<? extends PlayableEntity> klass) {
            return costMap.get(klass);
        }

        protected static void setBuildTime(Class<? extends PlayableEntity> klass, int buildTime) {
            buildTimeMap.put(klass, buildTime);
        }

        public static int getBuildTime(Class<? extends PlayableEntity> klass) {
            return buildTimeMap.get(klass);
        }

        protected static void registerPrerequisites(Class<? extends PlayableEntity> klass, List<Prerequisite> prerequisites) {
            prerequisitesMap.put(klass, prerequisites);
        }

        public static boolean canBeBuilt(Class<? extends PlayableEntity> klass, Player owner) {
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

}
