package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.SpriteSheet;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Tile.EAST_WEST;

public class Helipad extends Building {

    static {
        setMaxHealth(Helipad.class, 500);
        setCost(Helipad.class, 500);
        setPower(Helipad.class, -100);
        setSight(Helipad.class, 60);
        setBuildTime(Helipad.class, TPS * 10);
        setDeathSound(Helipad.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setBaseSprite(Helipad.class, SpriteSheet.SPRITE_ID_HELIPAD);
        setPassable(Helipad.class, EAST_WEST);
        setProduces(Helipad.class, List.of(Heli.class));
        registerPrerequisites(Helipad.class, List.of(new Prerequisite(Refinery.class)));
    }

    public Helipad(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
    }

}
