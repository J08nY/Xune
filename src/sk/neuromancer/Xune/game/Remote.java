package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.level.Level;

public class Remote extends Player {
    public Remote(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
    }

    //Use commandstrategies?

    @Override
    public void tick(int tickCount) {
        //Nothing
    }
}
