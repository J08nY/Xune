package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Factory;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;

import java.util.Arrays;

import static sk.neuromancer.Xune.game.Game.TPS;

public class Buggy extends Unit {

    static {
        setMaxHealth(Buggy.class, 100);
        setCost(Buggy.class, 100);
        setSight(Buggy.class, 50);
        setBuildTime(Buggy.class, TPS * 5);
        registerPrerequisites(Buggy.class, Arrays.asList(new Prerequisite(Factory.class)));
    }

    public Buggy(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, 0.5f, 20f, 30, 10);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 5, false));
    }

    @Override
    public void render() {
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand move) {
            move.getNextPath().render();
        }
        super.render();
    }

    @Override
    protected void updateSprite() {
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_BUGGY + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH + spriteOffset);
    }

    public Point[] getOccupied() {
        return new Point[]{new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y))};
    }
}
