package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Heli extends PlayableEntity {
    private Orientation heading;
    private boolean wing;

    public Heli(float x, float y, Orientation heading, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.heading = heading;
        updateSprite();
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableCircle.getCentered(x, y, 11, 5, 7, Button.LEFT, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 5 == 0) {
            wing = !wing;
            updateSprite();
        }
    }

    private void updateSprite() {
        int i = heading.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 2;
        } else {
            animation = (i % 4) * 2 + Entity.SPRITE_ROW_LENGTH;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HELI + PlayableEntity.getOffsetonFlag(flag) + (wing ? 1 : 0) + animation);
    }

}
