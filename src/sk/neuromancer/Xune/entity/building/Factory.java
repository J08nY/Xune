package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.EAST_WEST;

public class Factory extends Building {

    static {
        setMaxHealth(Factory.class, 500);
        setCost(Factory.class, 500);
        setPower(Factory.class, -125);
        setSight(Factory.class, 60);
        setBuildTime(Factory.class, TPS * 12);
        setDeathSound(Factory.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Factory.class, SpriteSheet.SPRITE_ID_FACTORY);
        setPassable(Factory.class, EAST_WEST);
        setProduces(Factory.class, List.of(Buggy.class, Harvester.class));
        registerPrerequisites(Factory.class, List.of(new Prerequisite(Refinery.class)));
    }

    public Factory(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

    public Factory(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.getPlayable().getEntity().getKlass() != BaseProto.EntityClass.FACTORY) {
            throw new IllegalArgumentException("Invalid entity class");
        }
    }

}
