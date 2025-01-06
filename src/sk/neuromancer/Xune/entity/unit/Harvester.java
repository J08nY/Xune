package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Command;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class Harvester extends Unit {
    private int spice;

    static {
        setMaxHealth(Harvester.class, 300);
        setCost(Harvester.class, 1200);
        setSight(Harvester.class, 40);
        setSpeed(Harvester.class, 0.1f);
        setRange(Harvester.class, 0);
        setRate(Harvester.class, 0);
        setDamage(Harvester.class, 0);
        setAccuracy(Harvester.class, 0);
        setBuildTime(Harvester.class, TPS * 10);
        setDeathSound(Harvester.class, SoundManager.SOUND_EXPLOSION_2);
        setShotSound(Harvester.class, SoundManager.SOUND_NONE);
        setBaseSprite(Harvester.class, SpriteSheet.SPRITE_ID_HARVESTER);
        registerPrerequisites(Harvester.class, List.of(new Prerequisite(Refinery.class)));
    }

    public Harvester(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 6, false));
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }

    @Override
    public void render() {
        Command current = currentCommand();
        float sx = 0;
        float sy = 0;
        if (current instanceof Command.MoveCommand move) {
            glDisable(GL_DEPTH_TEST);
            move.getNextPath().render();
            glEnable(GL_DEPTH_TEST);
        } else if (current instanceof Command.CollectSpiceCommand collect) {
            Tile target = collect.getTarget();
            if (collect.collecting(this)) {
                sx = rand.nextFloat();
                sy = rand.nextFloat();
            } else {
                glPushMatrix();
                glTranslatef(target.getLevelX(), target.getLevelY(), 0);
                SpriteSheet.TILE_SHEET.getSprite(17).render();
                glPopMatrix();
            }
        }
        glPushMatrix();
        glTranslatef(sx, sy, 0);
        super.render();
        glPopMatrix();
    }

    @Override
    protected void updateSprite() {
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH + spriteOffset);
    }

    public boolean collectSpice(Tile target) {
        if (target.getSpice() > 0 && !isFull()) {
            target.takeSpice(1);
            this.spice++;
            return true;
        }
        return false;
    }

    public int getSpice() {
        return spice;
    }

    public boolean isFull() {
        return this.spice == 500;
    }

    public void dropOffSpice(Refinery target) {
        if (this.spice > 0) {
            target.getOwner().addMoney(1);
            this.spice--;
        }
    }

    public Point[] getOccupied() {
        return new Point[]{new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y))};
    }

}
