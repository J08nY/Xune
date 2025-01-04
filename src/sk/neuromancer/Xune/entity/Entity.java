package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public abstract class Entity implements Renderable, Tickable, Clickable {
    protected static final Map<Class<? extends Entity>, Integer> healthMap = new HashMap<>();
    protected static final Map<Class<? extends Entity>, Integer> sightMap = new HashMap<>();
    protected static final Map<Class<? extends Entity>, Integer> deathSoundMap = new HashMap<>();

    protected Sprite sprite;
    public float x, y;
    public int health;
    protected int maxHealth;
    protected int sight;
    protected Orientation orientation;
    protected List<Clickable> clickableAreas = new ArrayList<>();

    protected boolean attacking;
    protected Entity attackTarget;
    protected boolean underAttack;
    protected Entity attacker;

    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
        this.health = getMaxHealth(getClass());
        this.maxHealth = getMaxHealth(getClass());
        this.sight = getSight(getClass());
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

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
        this.sprite.render();
        glPopMatrix();
    }

    protected static void setMaxHealth(Class<? extends Entity> klass, int health) {
        healthMap.put(klass, health);
    }

    public static int getMaxHealth(Class<? extends Entity> klass) {
        return healthMap.get(klass);
    }

    protected static void setDeathSound(Class<? extends PlayableEntity> klass, int sound) {
        deathSoundMap.put(klass, sound);
    }

    public static int getDeathSound(Class<? extends PlayableEntity> klass) {
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
        return dx * dx + dy * dy <= sight * sight;
    }

    public boolean inSight(Entity target) {
        return inSight(target.x, target.y);
    }

    public abstract boolean isEnemyOf(Entity other);


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
            float screenY = owner.getLevel().getScreenY(y);
            float depth = screenY / Game.DEFAULT_HEIGHT;
            glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
            this.sprite.render();
            glPopMatrix();
            if (isSelected) {
                glPushMatrix();
                glTranslatef(x, y, 0);
                for (Clickable area : clickableAreas) {
                    if (area instanceof Renderable) {
                        ((Renderable) area).render();
                    }
                }
                glPopMatrix();
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
