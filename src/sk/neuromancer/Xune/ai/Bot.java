package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.building.Barracks;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Factory;
import sk.neuromancer.Xune.entity.building.Powerplant;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import java.util.*;

import static sk.neuromancer.Xune.game.Game.TPS;


public class Bot extends Player {
    private final Tile spawn;
    private List<Class<? extends Building>> buildingPlan = new LinkedList<>();
    private Random rand = new Random();

    public Bot(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.spawn = setupSpawn();
        buildingPlan.add(Barracks.class);
        buildingPlan.add(Powerplant.class);
        buildingPlan.add(Factory.class);
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        if (tickCount % (TPS * 20) == 0) {
            build();
        }
        if (isBuildDone()) {
            placeBuild();
        }
    }

    private void build() {
        if (buildingPlan.isEmpty()) {
            return;
        }
        Class<? extends Building> building = buildingPlan.getFirst();
        if (Entity.PlayableEntity.canBeBuilt(building, this)) {
            startBuild(building);
            buildingPlan.removeFirst();
        }
    }

    private void placeBuild() {
        Iterator<Tile> close = level.findClosestTile(spawn, level::isTileClear);
        int pos = rand.nextInt(10);
        Tile last = null;
        try {
            for (int i = 0; i < pos; i++) {
                last = close.next();
            }
        } catch (NoSuchElementException ignored) {}

        if (last != null) {
            Building result = getBuildResult(last.getX(), last.getY());
            finishBuild(result);
        }
    }
}
