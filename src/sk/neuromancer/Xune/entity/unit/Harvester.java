package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;

import java.util.Arrays;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class Harvester extends Unit {
    private int spice;
    private final Random rand = new Random();

    static {
        setMaxHealth(Harvester.class, 300);
        setCost(Harvester.class, 1200);
        setSight(Harvester.class, 40);
        setBuildTime(Harvester.class, TPS * 10);
        registerPrerequisites(Harvester.class, Arrays.asList(new Prerequisite(Refinery.class)));
    }

    public Harvester(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, 0.1f, 0, 0, 0);
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
            move.getNextPath().render();
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
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_HARVESTER + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH + spriteOffset);
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
