package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public abstract class Unit extends Entity.PlayableEntity {
    protected static final Map<Class<? extends Unit>, Float> speedMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Float> rangeMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Integer> rateMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Integer> damageMap = new HashMap<>();
    protected static final Map<Class<? extends Unit>, Float> accuracyMap = new HashMap<>();

    private float speed;
    private float range;
    private int rate;
    private int damage;
    private float accuracy;
    private int ready = 0;
    protected final Random rand = new Random();

    public Unit(float x, float y, Orientation orientation, Player owner) {
        super(x, y, owner);
        this.orientation = orientation;
        Class<? extends Unit> klass = getClass();
        this.speed = getSpeed(klass);
        this.range = getRange(klass);
        this.rate = getRate(klass);
        this.damage = getDamage(klass);
        this.accuracy = getAccuracy(klass);
    }

    public void move(float toX, float toY) {
        float dx = toX - x;
        float dy = toY - y;
        float angle = (float) Math.atan2(dy, dx);
        face(toX, toY);
        setPosition(x + (float) (speed * Math.cos(angle)), y + (float) (speed * Math.sin(angle)));
    }

    public void face(float toX, float toY) {
        float dx = toX - x;
        float dy = toY - y;
        float angle = (float) Math.atan2(dy, dx);
        float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
        this.orientation = Orientation.fromAngle(azimuth);
        updateSprite();
    }

    public boolean inRange(Entity target) {
        float dx = x - target.x;
        float dy = y - target.y;
        return dx * dx + dy * dy <= range * range;
    }

    public void attack(Entity target) {
        if (rate == 0) {
            return;
        }
        if (ready % rate == 0 && inRange(target) && rand.nextFloat() < this.accuracy) {
            target.takeDamage(damage);
            owner.getLevel().addEffect(new Effect.Hit(target.x + rand.nextFloat(3) * (rand.nextBoolean() ? 1 : -1), target.y + rand.nextFloat(3) * (rand.nextBoolean() ? 1 : -1)));
            SoundManager.play(SoundManager.SOUND_SHOT_1, false, 1.0f);
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
        float screenY = owner.getLevel().getScreenY(y);
        float depth = screenY / Game.DEFAULT_HEIGHT;
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
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
        if (Config.DEBUG_ATTACK) {
            if (attackTarget != null) {
                glLineWidth(5);
                glColor4f(1, 0, 0, 0.5f);
                glBegin(GL_LINES);
                glVertex3f(x, y, 0);
                glVertex3f(attackTarget.x, attackTarget.y, 0);
                glEnd();
                glColor4f(1, 1, 1, 1);
            }
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

    protected abstract void updateSprite();

    public abstract Point[] getOccupied();
}
