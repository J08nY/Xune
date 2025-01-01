package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public abstract class Building extends Entity.PlayableEntity {
    protected static final Map<Class<? extends Building>, Integer> powerMap = new HashMap<>();
    protected static final Map<Class<? extends Building>, List<Class<? extends Unit>>> producesMap = new HashMap<>();
    public int tileX, tileY;

    public Building(int tileX, int tileY, Orientation orientation, Player owner, int baseSpriteId) {
        super(tileToCenterLevelX(tileX, tileY), tileToCenterLevelY(tileX, tileY), owner);
        this.tileX = tileX;
        this.tileY = tileY;
        this.orientation = orientation;
        int spriteRow = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(baseSpriteId + SpriteSheet.flagToOffset(owner.getFlag()) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight(), true));
    }

    public boolean inSight(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy <= 60 * 60;
    }

    @Override
    public void render() {
        glPushMatrix();
        float screenY = owner.getLevel().getScreenY(y);
        float depth = screenY / Game.DEFAULT_HEIGHT;
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
        this.sprite.render();
        if (isSelected) {
            SpriteSheet.MISC_SHEET.getSprite(1, 0).render();
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

    public int getPower() {
        return powerMap.getOrDefault(this.getClass(), 0);
    }

    public List<Class<? extends Unit>> getProduces() {
        return producesMap.getOrDefault(this.getClass(), null);
    }

    protected static void setPower(Class<? extends Building> klass, int cost) {
        powerMap.put(klass, cost);
    }

    protected static void setProduces(Class<? extends Building> klass, List<Class<? extends Unit>> units) {
        producesMap.put(klass, units);
    }

    public abstract boolean[] getPassable();
}
