package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Factory extends Building {

    static {
        setCost(Factory.class, 500);
        registerPrerequisites(Factory.class, Arrays.asList(new Prerequisite(Refinery.class)));
    }

    public Factory(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, -125, SpriteSheet.SPRITE_ID_FACTORY);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}
