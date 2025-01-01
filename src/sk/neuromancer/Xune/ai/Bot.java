package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.building.Base;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;


public class Bot extends Player {

    public Bot(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        level.setBot(this);
        this.addEntity(new Base(16, 8, Orientation.NORTH, this));
        this.addEntity(new Refinery(16, 9, Orientation.NORTH, this));
        this.addEntity(new Heli(Level.tileToCenterLevelX(14, 10), Level.tileToCenterLevelY(14, 10), Orientation.WEST, this));
        this.addEntity(new Buggy(Level.tileToCenterLevelX(13, 6), Level.tileToCenterLevelY(13, 6), Orientation.WEST, this));
    }
}
