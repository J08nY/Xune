package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Barracks extends Building {

    static {
        setCost(Barracks.class, 225);
        setPower(Barracks.class, -30);
        registerPrerequisites(Barracks.class, Arrays.asList(new Prerequisite(Powerplant.class)));
    }

    public Barracks(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_BARRACKS);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}