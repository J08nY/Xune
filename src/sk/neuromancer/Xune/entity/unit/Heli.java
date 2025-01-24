package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.building.Helipad;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Config.TPS;


public class Heli extends Unit {
    private boolean wing;

    static {
        setMaxHealth(Heli.class, 100);
        setCost(Heli.class, 1000);
        setSight(Heli.class, 100);
        setSpeed(Heli.class, 1.2f);
        setRange(Heli.class, 30f);
        setRate(Heli.class, 10);
        setDamage(Heli.class, 5);
        setAccuracy(Heli.class, 0.9f);
        setBuildTime(Heli.class, TPS * 6);
        setDeathSound(Heli.class, SoundManager.SOUND_EXPLOSION_3);
        setBaseSprite(Heli.class, SpriteSheet.SPRITE_ID_HELI);
        setShotSound(Heli.class, SoundManager.SOUND_SHOT_1);
        registerPrerequisites(Heli.class, List.of(new Prerequisite(Helipad.class)));
    }

    public Heli(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 7, false));
    }

    public Heli(EntityStateProto.UnitState savedState, Player owner) {
        super(savedState, owner);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 7, false));
    }

    @Override
    public void render() {
        Command current = currentCommand();
        if (current instanceof Command.FlyCommand fly) {
            glDisable(GL_DEPTH_TEST);
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
            glEnable(GL_DEPTH_TEST);
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
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(flag) + (wing ? 1 : 0) + animation);
    }

    public Point[] getOccupied() {
        return new Point[]{};
    }
}
