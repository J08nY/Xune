package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Config.TPS;
import static sk.neuromancer.Xune.level.Tile.CORNERS;

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
        setPassable(Barracks.class, CORNERS);
        registerPrerequisites(Barracks.class, List.of(new Prerequisite(Powerplant.class)));
    }

    public Barracks(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    public Barracks(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.getPlayable().getEntity().getKlass() != BaseProto.EntityClass.BARRACKS) {
            throw new IllegalArgumentException("Invalid entity class");
        }
    }
}