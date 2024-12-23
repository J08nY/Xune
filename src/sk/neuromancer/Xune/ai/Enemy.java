package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Heli;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;

public class Enemy extends EntityOwner {

    public Enemy(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.addEntity(new Heli(Level.tileX(14, 10), Level.tileY(14, 10), Entity.Orientation.WEST, this, this.flag));
    }

    @Override
    public void tick(int tickCount) {
        handleDead();
        super.tick(tickCount);
    }

}
