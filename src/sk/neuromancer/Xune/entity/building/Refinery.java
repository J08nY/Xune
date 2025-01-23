package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Config.TPS;
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

    public Refinery(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.getPlayable().getEntity().getKlass() != BaseProto.EntityClass.REFINERY) {
            throw new IllegalArgumentException("Invalid entity class");
        }
    }
}
