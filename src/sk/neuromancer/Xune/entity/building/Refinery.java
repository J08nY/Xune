package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Refinery extends Building {

    static {
        setMaxHealth(Refinery.class, 500);
        setCost(Refinery.class, 1500);
        setPower(Refinery.class, -75);
        registerPrerequisites(Refinery.class, Arrays.asList(new Prerequisite(Powerplant.class)));
    }


    public Refinery(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_REFINERY);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}
