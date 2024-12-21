package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Heli;
import sk.neuromancer.Xune.entity.Worm;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;

import static sk.neuromancer.Xune.level.Tile.*;

public class Enemy extends EntityOwner {

    public Enemy(Game game, Level level, Flag flag, int money) {
        super(flag, money);
        //this.addEntity(new Worm(TILE_WIDTH * 4.5f, TILE_HEIGHT * 2.5f));
        this.addEntity(new Heli(tileX(14, 10), tileY(14, 10), Entity.Orientation.WEST, this, this.flag));
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
    }

}
