package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Harvester extends Entity.Unit {
    private float spice;

    public Harvester(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = orientation;
        updateSprite();
        if (owner instanceof Player)
            this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 12, 5, 5, Button.LEFT, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 60 == 0) {
            this.orientation = this.orientation.cw();
            updateSprite();
        }
    }

    private void updateSprite() {
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HARVESTER + PlayableEntity.getOffsetonFlag(flag) + spriteRow * Entity.SPRITE_ROW_LENGTH + spriteOffset);
    }
}
