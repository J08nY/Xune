package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Silo extends Building {

    public Silo(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_SILO);
    }

    @Override
    public boolean[] getPassable() {
        return new boolean[]{false, false, true, false, false, false, true, false, false, false, false, false, false};
    }
}
