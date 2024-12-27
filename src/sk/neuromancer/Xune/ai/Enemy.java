package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.building.Base;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;

public class Enemy extends EntityOwner {

    public Enemy(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.addEntity(new Base(16, 8, Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(16, 9, Orientation.NORTH, this, this.flag));
        this.addEntity(new Heli(Level.tileCenterX(14, 10), Level.tileCenterY(14, 10), Orientation.WEST, this, this.flag));
        this.addEntity(new Buggy(Level.tileCenterX(13, 6), Level.tileCenterY(13, 6), Orientation.WEST, this, this.flag));
    }
}
