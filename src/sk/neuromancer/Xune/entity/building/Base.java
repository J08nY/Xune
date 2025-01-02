package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import static sk.neuromancer.Xune.level.Tile.PASS_EAST_WEST;

public class Base extends Building {

    static {
        setMaxHealth(Base.class, 1000);
        setCost(Base.class, 2000);
        setPower(Base.class, 20);
        setSight(Base.class, 60);
        setDeathSound(Base.class, SoundManager.SOUND_LONG_EXPLOSION_1);
    }

    public Base(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_BASE);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EAST_WEST;
    }
}
