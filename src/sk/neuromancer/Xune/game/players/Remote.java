package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.proto.PlayerProto;

public class Remote extends Player {
    public Remote(Level level, Flag flag, int money) {
        super(level, flag, money);
        setupSpawn();
    }

    public Remote(Level level, PlayerProto.PlayerState playerState) {
        super(level, playerState);
    }
}
