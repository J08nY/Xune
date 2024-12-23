package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Factory extends Entity.Building {

    public Factory(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_FACTORY);
    }

    @Override
    public void tick(int tickCount) {

    }
}
