package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Buggy;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Heli;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;

public class Enemy extends EntityOwner {

    public Enemy(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.addEntity(new Heli(Level.tileCenterX(14, 10), Level.tileCenterY(14, 10), Entity.Orientation.WEST, this, this.flag));
        this.addEntity(new Buggy(Level.tileCenterX(13, 6), Level.tileCenterY(13, 6), Entity.Orientation.WEST, this, this.flag));
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        if (!entities.isEmpty()) {
            Entity first = entities.getFirst();
            if (tickCount % 120 == 0) {
                first.setPosition(first.x + 5f, first.y);
            }
        }
    }

}
