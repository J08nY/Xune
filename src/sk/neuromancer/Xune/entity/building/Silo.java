package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.CORNERS;

public class Silo extends Building {

    static {
        setMaxHealth(Silo.class, 500);
        setCost(Silo.class, 120);
        setPower(Silo.class, -15);
        setSight(Silo.class, 60);
        setBuildTime(Silo.class, TPS * 8);
        setDeathSound(Silo.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Silo.class, SpriteSheet.SPRITE_ID_SILO);
        setPassable(Silo.class, CORNERS);
        registerPrerequisites(Silo.class, List.of(new Prerequisite(Powerplant.class)));
    }

    public Silo(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }
}
