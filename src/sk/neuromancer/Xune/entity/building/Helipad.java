package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Helipad extends Building {

    public Helipad(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_HELIPAD);
    }
}
