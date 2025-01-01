package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.Arrays;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Barracks extends Building {

    static {
        setMaxHealth(Barracks.class, 500);
        setCost(Barracks.class, 225);
        setPower(Barracks.class, -30);
        setProduces(Barracks.class, Arrays.asList(Soldier.class));
        registerPrerequisites(Barracks.class, Arrays.asList(new Prerequisite(Powerplant.class)));
    }

    public Barracks(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_BARRACKS);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}