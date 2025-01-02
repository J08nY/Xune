package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EAST_WEST;

public class Factory extends Building {

    static {
        setMaxHealth(Factory.class, 500);
        setCost(Factory.class, 500);
        setPower(Factory.class, -125);
        setSight(Factory.class, 60);
        setProduces(Factory.class, Arrays.asList(Buggy.class, Harvester.class));
        registerPrerequisites(Factory.class, Arrays.asList(new Prerequisite(Refinery.class)));
    }

    public Factory(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_FACTORY);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EAST_WEST;
    }
}
