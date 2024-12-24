package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static org.lwjgl.opengl.GL11.*;

public abstract class Unit extends Entity.PlayableEntity {
    private float speed;

    public Unit(float x, float y, Orientation orientation, EntityOwner owner, Flag flag, int maxHealth, float speed) {
        super(x, y, owner, flag, maxHealth);
        this.orientation = orientation;
        this.speed = speed;
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

    protected abstract void updateSprite();

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
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
}
