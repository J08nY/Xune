package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import static sk.neuromancer.Xune.game.Config.TPS;
import static sk.neuromancer.Xune.level.Tile.EAST_WEST;

public class Base extends Building {

    static {
        setMaxHealth(Base.class, 1000);
        setCost(Base.class, 2000);
        setPower(Base.class, 20);
        setSight(Base.class, 60);
        setBuildTime(Base.class, TPS * 15);
        setBaseSprite(Base.class, SpriteSheet.SPRITE_ID_BASE);
        setDeathSound(Base.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setPassable(Base.class, EAST_WEST);
    }

    public Base(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    public Base(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.getPlayable().getEntity().getKlass() != BaseProto.EntityClass.BASE) {
            throw new IllegalArgumentException("Invalid entity class");
        }
    }

}
