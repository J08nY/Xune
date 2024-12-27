package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import static org.lwjgl.opengl.GL11.*;

public abstract class Unit extends Entity.PlayableEntity {
    private float speed;
    private float range;
    private int rate;
    private int damage;
    private int ready = 0;

    public Unit(float x, float y, Orientation orientation, EntityOwner owner, Flag flag,
                int maxHealth,
                float speed,
                float range,
                int rate,
                int damage) {
        super(x, y, owner, flag, maxHealth);
        this.orientation = orientation;
        this.speed = speed;
        this.range = range;
        this.rate = rate;
        this.damage = damage;
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
        if (ready % rate == 0 && inRange(target)) {
            target.takeDamage(damage);
            owner.getGame().getSound().play(SoundManager.SOUND_SHOT_1, false, 1.0f);
        }
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        ready++;
        if (rate != 0 && ready % rate == 0) {
            ready = 0;
        }
    }

    protected abstract void updateSprite();

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
    }

    public abstract Point[] getOccupied();
}
