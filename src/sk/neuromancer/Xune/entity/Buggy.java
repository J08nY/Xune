package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Buggy extends Entity.Unit {
    public static final float SPEED = 0.5f;

    public Buggy(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 100);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 5, false));
    }

    @Override
    public void tick(int tickCount) {
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand cmd) {
            if (tickCount % 5 == 0) {
                this.health--;
            }
            if (cmd.isFinished(x, y)) {
                setPosition(cmd.getToX(), cmd.getToY());
                this.commands.removeFirst();
            } else {
                move(cmd.getNext().getLevelX(), cmd.getNext().getLevelY(), SPEED);
                cmd.update(x, y);
            }
        }
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
