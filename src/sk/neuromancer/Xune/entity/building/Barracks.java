package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.PASS_CORNERS;

public class Barracks extends Building {

    static {
        setMaxHealth(Barracks.class, 500);
        setCost(Barracks.class, 225);
        setPower(Barracks.class, -30);
        setProduces(Barracks.class, List.of(Soldier.class));
        setSight(Barracks.class, 60);
        setBuildTime(Barracks.class, TPS * 12);
        setDeathSound(Barracks.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Barracks.class, SpriteSheet.SPRITE_ID_BARRACKS);
        registerPrerequisites(Barracks.class, List.of(new Prerequisite(Powerplant.class)));
    }

    public Barracks(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_CORNERS;
    }
}