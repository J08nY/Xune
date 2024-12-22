package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Helipad extends Entity.Building {

    public Helipad(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = orientation;
        int spriteOffset = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HELIPAD + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableTile.getCentered(x, y, this.sprite.getWidth(), this.sprite.getHeight(), Button.LEFT, true));
    }

    @Override
    public void tick(int tickCount) {

    }
}
