package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.CORNERS;

public class Powerplant extends Building {

    static {
        setMaxHealth(Powerplant.class, 500);
        setCost(Powerplant.class, 225);
        setPower(Powerplant.class, 200);
        setSight(Powerplant.class, 60);
        setBuildTime(Powerplant.class, TPS * 8);
        setDeathSound(Powerplant.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Powerplant.class, SpriteSheet.SPRITE_ID_POWERPLANT);
        setPassable(Powerplant.class, CORNERS);
    }

    public Powerplant(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    public Powerplant(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.getPlayable().getEntity().getKlass() != BaseProto.EntityClass.POWERPLANT) {
            throw new IllegalArgumentException("Invalid entity class");
        }
    }
}