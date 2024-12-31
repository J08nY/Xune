package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Helipad extends Building {

    static {
        setCost(Helipad.class, 500);
        setPower(Helipad.class, -100);
        setProduces(Helipad.class, Arrays.asList(Heli.class));
        registerPrerequisites(Helipad.class, Arrays.asList(new Prerequisite(Refinery.class)));
    }

    public Helipad(int x, int y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, orientation, owner, flag, 500, SpriteSheet.SPRITE_ID_HELIPAD);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}
