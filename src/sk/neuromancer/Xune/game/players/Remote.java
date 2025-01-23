package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.level.Level;

public class Remote extends Player {
    public Remote(Level level, Flag flag, int money) {
        super(level, flag, money);
    }

    //Use commandstrategies?

    @Override
    public void tick(int tickCount) {
        //Nothing
    }
}
