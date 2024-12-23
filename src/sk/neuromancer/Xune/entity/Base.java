package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Base extends Entity.Building {

    public Base(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag, 1000);
        this.orientation = orientation;
        int spriteOffset = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_BASE + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight(), true));
    }

    @Override
    public void tick(int tickCount) {

    }
}
