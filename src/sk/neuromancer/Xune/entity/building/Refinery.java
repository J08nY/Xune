package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Refinery extends Building {

    static {
        setCost(Refinery.class, 1500);
        registerPrerequisites(Refinery.class, Arrays.asList(new Prerequisite(Powerplant.class)));
    }


    public Refinery(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, -75, SpriteSheet.SPRITE_ID_REFINERY);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}
