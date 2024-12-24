package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.gfx.SpriteSheet;


public class Heli extends Unit {
    private boolean wing;

    public Heli(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 100, 1.5f);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 7, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 5 == 0) {
            wing = !wing;
            updateSprite();
        }
        super.tick(tickCount);
    }

    @Override
    protected void updateSprite() {
        int i = orientation.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 2;
        } else {
            animation = (i % 4) * 2 + SpriteSheet.SPRITE_ROW_LENGTH;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_HELI + SpriteSheet.flagToOffset(flag) + (wing ? 1 : 0) + animation);
    }
}
