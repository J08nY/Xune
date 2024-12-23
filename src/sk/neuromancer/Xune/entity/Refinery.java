package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Refinery extends Entity.Building {

    public Refinery(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_REFINERY);
    }

    @Override
    public void tick(int tickCount) {

    }
}
