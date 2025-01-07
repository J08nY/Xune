package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.EAST_WEST;

public class Powerplant extends Building {

    static {
        setMaxHealth(Powerplant.class, 500);
        setCost(Powerplant.class, 225);
        setPower(Powerplant.class, 200);
        setSight(Powerplant.class, 60);
        setBuildTime(Powerplant.class, TPS * 8);
        setDeathSound(Powerplant.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Powerplant.class, SpriteSheet.SPRITE_ID_POWERPLANT);
    }

    public Powerplant(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    @Override
    public boolean[] getPassable() {
        return EAST_WEST;
    }
}