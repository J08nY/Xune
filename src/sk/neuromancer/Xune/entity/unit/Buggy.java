package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.Clickable;
import sk.neuromancer.Xune.entity.Command;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Buggy extends Unit {
    public Buggy(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 100, 0.5f);
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
}
