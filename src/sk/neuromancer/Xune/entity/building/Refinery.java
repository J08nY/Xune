package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.CORNERS;

public class Refinery extends Building {

    static {
        setMaxHealth(Refinery.class, 500);
        setCost(Refinery.class, 1500);
        setPower(Refinery.class, -75);
        setSight(Refinery.class, 60);
        setBuildTime(Refinery.class, TPS * 10);
        setDeathSound(Refinery.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Refinery.class, SpriteSheet.SPRITE_ID_REFINERY);
        setPassable(Refinery.class, CORNERS);
        registerPrerequisites(Refinery.class, List.of(new Prerequisite(Powerplant.class)));
    }


    public Refinery(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

}
