package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Base extends Entity.Building {

    public Base(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 1000, SpriteSheet.SPRITE_ID_BASE);
    }
}
