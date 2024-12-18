package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Heli;
import sk.neuromancer.Xune.entity.Worm;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;

public class Enemy extends EntityOwner {

    public Enemy(Game game, Level level, Flag flag, int money) {
        super(flag, money);
        this.addEntity(new Worm(24 * 4.5f, 11 * 2.5f));
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }

}
