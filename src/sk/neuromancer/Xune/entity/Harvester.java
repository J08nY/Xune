package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Harvester extends Entity.Unit {
    private float spice;

    public static final float SPEED = 0.5f;

    public Harvester(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = orientation;
        updateSprite();
        if (owner instanceof Player)
            this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 5, Button.LEFT, false));
    }

    @Override
    public void tick(int tickCount) {
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand move) {
            if (move.isFinished(x, y)) {
                setPosition(move.getToX(), move.getToY());
                this.commands.removeFirst();
            } else {
                move(move.getNext().getLevelX(), move.getNext().getLevelY(), SPEED);
                move.update(x, y);
            }
        }
    }

    @Override
    public void render() {
        super.render();
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand move) {
            //move.getPath().render();
        }
    }

    @Override
    protected void updateSprite() {
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HARVESTER + PlayableEntity.getOffsetonFlag(flag) + spriteRow * Entity.SPRITE_ROW_LENGTH + spriteOffset);
    }
}
