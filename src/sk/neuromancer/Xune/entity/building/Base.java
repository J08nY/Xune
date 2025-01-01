package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static sk.neuromancer.Xune.level.Tile.PASS_EDGES;

public class Base extends Building {

    static {
        setCost(Base.class, 2000);
        setPower(Base.class, 20);
    }

    public Base(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, 1000, SpriteSheet.SPRITE_ID_BASE);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EDGES;
    }
}
