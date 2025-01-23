package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Moveable;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.Effect;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public abstract class Unit extends PlayableEntity implements Moveable {
    protected static final Map<Class<? extends Unit>, Float> speedMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Float> rangeMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Integer> rateMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Integer> damageMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Float> accuracyMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Integer> shotSoundMap = new HashMap<>();

    private final float speed;
    private final float range;
    private final float r2;
    private final int rate;
    private final int damage;
    private final float accuracy;
    private int ready = 0;
    private boolean immobile;

    public Unit(float x, float y, Orientation orientation, Player owner) {
        super(x, y, owner);
        this.orientation = orientation;
        Class<? extends Unit> klass = getClass();
        this.speed = getSpeed(klass);
        this.range = getRange(klass);
        this.r2 = range * range;
        this.rate = getRate(klass);
        this.damage = getDamage(klass);
        this.accuracy = getAccuracy(klass);
    }

    public Unit(EntityStateProto.UnitState savedState, Player owner) {
        super(savedState.getPlayable(), owner);
        this.ready = savedState.getReady();
        this.immobile = savedState.getImmobile();
        Class<? extends Unit> klass = getClass();
        this.speed = getSpeed(klass);
        this.range = getRange(klass);
        this.r2 = range * range;
        this.rate = getRate(klass);
        this.damage = getDamage(klass);
        this.accuracy = getAccuracy(klass);
    }

    private float angleTo(float toX, float toY) {
        float dx = toX - x;
        float dy = toY - y;
        return (float) Math.atan2(dy, dx);
    }

    public void move(float toX, float toY) {
        float angle = angleTo(toX, toY);
        face(angle);
        if (immobile) {
            return;
        }
        setPosition(x + (float) (speed * Math.cos(angle)), y + (float) (speed * Math.sin(angle)));
    }

    public void face(float toX, float toY) {
        float angle = angleTo(toX, toY);
        face(angle);
    }

    public void face(float angle) {
        float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
        this.orientation = Orientation.fromAzimuth(azimuth);
        updateSprite();
    }

    public boolean inRange(Entity target) {
        float dx = x - target.x;
        float dy = y - target.y;
        return dx * dx + dy * dy <= r2;
    }

    public void attack(Entity target) {
        if (rate == 0) {
            return;
        }
        if (ready % rate == 0 && inRange(target) && rand.nextFloat() < this.accuracy) {
            target.takeDamage(damage);
            owner.getLevel().addEffect(new Effect.Hit(target.x + rand.nextFloat(3) * (rand.nextBoolean() ? 1 : -1), target.y + rand.nextFloat(3) * (rand.nextBoolean() ? 1 : -1)));
            SoundManager.play(getShotSound(getClass()), false, 1.0f);
        }
    }

    public float getRange() {
        return range;
    }

    public float getSpeed() {
        return speed;
    }

    public int getDamage() {
        return damage;
    }

    public int getRate() {
        return rate;
    }

    public void setImmobile(boolean immobile) {
        this.immobile = immobile;
    }

    public boolean isImmobile() {
        return immobile;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        ready++;
        if (rate != 0 && ready % rate == 0) {
            ready = 0;
        }
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, y);
        this.sprite.render();
        if (isSelected) {
            SpriteSheet.MISC_SHEET.getSprite(0, 0).render();
            glPushMatrix();
            glTranslatef(0, sprite.getHeight(), 0);
            glBegin(GL_QUADS);
            glColor3f(0, 1, 0);
            float healthPercentage = (float) health / maxHealth;
            for (int i = 0; i < 12; i++) {
                if (i < healthPercentage * 12) {
                    glColor3f(0, 1, 0);
                } else {
                    glColor3f(1, 0, 0);
                }
                glVertex2f(((float) 25 / 12) * i, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 1);
                glVertex2f(((float) 25 / 12) * i, 1);
            }
            glEnd();
            glColor3f(1, 1, 1);
            glPopMatrix();
        }
        glPopMatrix();
    }

    public EntityStateProto.UnitState serialize() {
        return EntityStateProto.UnitState.newBuilder()
                .setPlayable(toPlayableEntityState())
                .setReady(this.ready)
                .setImmobile(this.immobile)
                .build();
    }

    public void deserialize(EntityStateProto.UnitState state) {
        fromPlayableEntityState(state.getPlayable(), owner);
        if (state.hasReady()) {
            this.ready = state.getReady();
        }
        if (state.hasImmobile()) {
            this.immobile = state.getImmobile();
        }
    }

    protected static void setSpeed(Class<? extends Unit> klass, float speed) {
        speedMap.put(klass, speed);
    }

    public static float getSpeed(Class<? extends Unit> klass) {
        return speedMap.get(klass);
    }

    protected static void setRange(Class<? extends Unit> klass, float range) {
        rangeMap.put(klass, range);
    }

    public static float getRange(Class<? extends Unit> klass) {
        return rangeMap.get(klass);
    }

    protected static void setRate(Class<? extends Unit> klass, int rate) {
        rateMap.put(klass, rate);
    }

    public static int getRate(Class<? extends Unit> klass) {
        return rateMap.get(klass);
    }

    protected static void setDamage(Class<? extends Unit> klass, int damage) {
        damageMap.put(klass, damage);
    }

    public static int getDamage(Class<? extends Unit> klass) {
        return damageMap.get(klass);
    }

    protected static void setAccuracy(Class<? extends Unit> klass, float accuracy) {
        accuracyMap.put(klass, accuracy);
    }

    public static float getAccuracy(Class<? extends Unit> klass) {
        return accuracyMap.get(klass);
    }

    protected static void setShotSound(Class<? extends Unit> klass, int sound) {
        shotSoundMap.put(klass, sound);
    }

    public static int getShotSound(Class<? extends Unit> klass) {
        return shotSoundMap.get(klass);
    }

    protected abstract void updateSprite();

    public abstract Point[] getOccupied();
}
