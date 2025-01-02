package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Helipad;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;


public class Heli extends Unit {
    private boolean wing;

    static {
        setMaxHealth(Heli.class, 100);
        setCost(Heli.class, 1000);
        setSight(Heli.class, 100);
        setBuildTime(Heli.class, TPS * 6);
        setDeathSound(Heli.class, SoundManager.SOUND_EXPLOSION_3);
        registerPrerequisites(Heli.class, List.of(new Prerequisite(Helipad.class)));
    }

    public Heli(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner,  1.2f, 30f, 10, 5);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 7, false));
    }

    @Override
    public void render() {
        Command current = currentCommand();
        if (current instanceof Command.FlyCommand fly) {
            glEnable(GL_POINT_SMOOTH);
            glPointSize(10);
            glBegin(GL_POINTS);
            glColor4f(0, 0, 0, 0.4f);
            float dx = fly.getToX() - fly.getFromX();
            float dy = fly.getToY() - fly.getFromY();
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            for (float i = 0; i < d; i += 6) {
                float cx = fly.getFromX() + dx * i / d;
                float cy = fly.getFromY() + dy * i / d;
                if (Math.abs(cx - fly.getToX()) < Math.abs(x - fly.getToX()) &&
                        Math.abs(cy - fly.getToY()) < Math.abs(y - fly.getToY())) {
                    glVertex2f(cx, cy);
                }
            }
            glColor4f(1, 1, 1, 1);
            glEnd();
            glDisable(GL_POINT_SMOOTH);
        }
        super.render();
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 5 == 0) {
            wing = !wing;
            updateSprite();
        }
        super.tick(tickCount);
    }

    @Override
    protected void updateSprite() {
        int i = orientation.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 2;
        } else {
            animation = (i % 4) * 2 + SpriteSheet.SPRITE_ROW_LENGTH;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_HELI + SpriteSheet.flagToOffset(flag) + (wing ? 1 : 0) + animation);
    }

    public Point[] getOccupied() {
        return new Point[]{};
    }
}
