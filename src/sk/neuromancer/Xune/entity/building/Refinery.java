package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;

public class Refinery extends Building {

    static {
        setMaxHealth(Refinery.class, 500);
        setCost(Refinery.class, 1500);
        setPower(Refinery.class, -75);
        setSight(Refinery.class, 60);
        setBuildTime(Refinery.class, TPS * 10);
        setDeathSound(Refinery.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Refinery.class, SpriteSheet.SPRITE_ID_REFINERY);
        setPassable(Refinery.class, new boolean[]{true, true, true, false, false, false, false, false, false, false, false, false, false});
        registerPrerequisites(Refinery.class, List.of(new Prerequisite(Powerplant.class)));
    }


    public Refinery(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

}
