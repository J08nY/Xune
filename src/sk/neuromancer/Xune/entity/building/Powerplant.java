package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Powerplant extends Building {

    static {
        setMaxHealth(Powerplant.class, 500);
        setCost(Powerplant.class, 225);
        setPower(Powerplant.class, 200);
    }

    public Powerplant(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_POWERPLANT);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}