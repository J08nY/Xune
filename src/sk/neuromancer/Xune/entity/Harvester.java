package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Harvester extends PlayableEntity {
    private float spice;

    public Harvester(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = orientation;
        int spriteOffset = orientation.ordinal();
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HARVESTER + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableCircle.getCentered(x, y, 13, 5, 5, Button.LEFT, false));
    }

    @Override
    public void tick(int tickCount) {

    }

}
