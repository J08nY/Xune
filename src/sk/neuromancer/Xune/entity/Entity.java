package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.game.Clickable;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.Sprite;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.proto.BaseProto;
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
    protected EntityReference attackTarget;
    protected boolean underAttack;
    protected Set<EntityReference> attackers;

    public Entity(float x, float y) {
        this.id = rand.nextLong();
        this.x = x;
        this.y = y;
        this.health = getMaxHealth(getClass());
        this.maxHealth = getMaxHealth(getClass());
        this.sight = getSight(getClass());
        this.s2 = sight * sight;
        this.attackers = new HashSet<>();
    }

    public Entity(EntityStateProto.EntityState savedState, Level level) {
        this.id = savedState.getId();
        this.x = savedState.getPosition().getX();
        this.y = savedState.getPosition().getY();
        this.orientation = Orientation.deserialize(savedState.getOrientation());

        if (savedState.hasAttackingState()) {
            EntityStateProto.EntityState.AttackingState attackingState = savedState.getAttackingState();
            this.attacking = attackingState.getAttacking();
            this.attackTarget = new EntityReference(attackingState.getTargetId(), level);
        }
        if (savedState.hasAttackedState()) {
            EntityStateProto.EntityState.AttackedState attackedState = savedState.getAttackedState();
            this.underAttack = attackedState.getUnderAttack();
            this.attackers = new HashSet<>();
            this.attackers.addAll(attackedState.getAttackerIdsList().stream().map(id -> new EntityReference(id, level)).toList());
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

    public void setAttacking(boolean attacking, EntityReference target) {
        this.attacking = attacking;
        this.attackTarget = target;
    }

    public void setAttacking(boolean attacking, Entity target) {
        setAttacking(attacking, new EntityReference(target));
    }

    public boolean isAttacking() {
        return attacking;
    }

    public EntityReference getAttackTarget() {
        return attackTarget;
    }

    public void setUnderAttack(boolean underAttack, EntityReference attacker) {
        if (underAttack) {
            this.attackers.add(attacker);
        } else {
            this.attackers.remove(attacker);
        }
        this.underAttack = !attackers.isEmpty();
    }

    public void setUnderAttack(boolean underAttack, Entity attacker) {
        setUnderAttack(underAttack, new EntityReference(attacker));
    }

    public boolean isUnderAttack() {
        return underAttack;
    }

    public Set<EntityReference> getAttackers() {
        return Collections.unmodifiableSet(attackers);
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
                        .addAllAttackerIds(this.attackers.stream().map(EntityReference::getId).toList())
                        .setUnderAttack(this.underAttack)
                        .build()).build();
    }

    protected void fromEntityState(EntityStateProto.EntityState savedState, Level level) {
        //this.id = savedState.getId();
        if (savedState.hasPosition()) {
            this.x = savedState.getPosition().getX();
            this.y = savedState.getPosition().getY();
            for (Clickable area : clickableAreas) {
                area.setPosition(x, y);
            }
        }
        if (savedState.hasOrientation()) {
            this.orientation = Orientation.deserialize(savedState.getOrientation());
        }

        if (savedState.hasAttackingState()) {
            EntityStateProto.EntityState.AttackingState attackingState = savedState.getAttackingState();
            this.attacking = attackingState.getAttacking();
            this.attackTarget = new EntityReference(attackingState.getTargetId(), level);
        }
        if (savedState.hasAttackedState()) {
            EntityStateProto.EntityState.AttackedState attackedState = savedState.getAttackedState();
            this.underAttack = attackedState.getUnderAttack();
            this.attackers = new HashSet<>();
            this.attackers.addAll(attackedState.getAttackerIdsList().stream().map(id -> new EntityReference(id, level)).toList());
        }
        if (savedState.hasHealth()) {
            this.health = savedState.getHealth();
        }
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

}
