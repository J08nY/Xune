package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Point;

public class Soldier extends Unit {
    private int step;

    public Soldier(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 20, 0.2f, 50f, 20f, 15, 2);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 3, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 15 == 0) {
            step = (step + 1) % 4;
            updateSprite();
        }
        super.tick(tickCount);
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
        int i = orientation.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 3;
        } else {
            animation = (i % 4) * 3 + SpriteSheet.SPRITE_ROW_LENGTH;
        }
        int variant = 0;
        switch (step) {
            case 0 -> variant = 1;
            case 1, 3 -> variant = 0;
            case 2 -> variant = 2;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_SOLDRIED + SpriteSheet.flagToOffset(flag) + variant + animation);
    }

    @Override
    public Point[] getOccupied() {
        return new Point[0];
    }
}
