package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.Arrays;
import java.util.List;

import static sk.neuromancer.Xune.level.Tile.PASS_EAST_WEST;

public class Helipad extends Building {

    static {
        setMaxHealth(Helipad.class, 500);
        setCost(Helipad.class, 500);
        setPower(Helipad.class, -100);
        setSight(Helipad.class, 60);
        setDeathSound(Helipad.class, SoundManager.SOUND_LONG_EXPLOSION_1);
        setProduces(Helipad.class, List.of(Heli.class));
        registerPrerequisites(Helipad.class, List.of(new Prerequisite(Refinery.class)));
    }

    public Helipad(int x, int y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner, SpriteSheet.SPRITE_ID_HELIPAD);
    }

    @Override
    public boolean[] getPassable() {
        return PASS_EAST_WEST;
    }
}
