package sk.neuromancer.Xune.ai;

import sk.neuromancer.Xune.entity.Command;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Player;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import java.util.*;

import static sk.neuromancer.Xune.game.Game.TPS;


public class Bot extends Player {
    private final Tile spawn;
    private List<Class<? extends Building>> buildingPlan = new LinkedList<>();
    private List<Class<? extends Unit>> unitPlan = new LinkedList<>();
    private Random rand = new Random();
    private int buildings;
    private int units;
    private int harvesters;

    public Bot(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.spawn = setupSpawn();
        buildingPlan.add(Barracks.class);
        buildingPlan.add(Powerplant.class);
        buildingPlan.add(Factory.class);
        buildingPlan.add(Helipad.class);

        unitPlan.add(Soldier.class);
        unitPlan.add(Soldier.class);
        unitPlan.add(Buggy.class);
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        buildings = entities.stream().filter(e -> e instanceof Building).mapToInt(e -> 1).sum();
        units = entities.stream().filter(e -> e instanceof Unit).mapToInt(e -> 1).sum();
        harvesters = entities.stream().filter(e -> e instanceof Harvester).mapToInt(e -> 1).sum();

        if (harvesters == 0) {
            // Bump up!
            if (unitPlan.contains(Harvester.class)) {
                unitPlan.removeIf(klass -> klass == Harvester.class);
            }
            unitPlan.addFirst(Harvester.class);
            produce();
        }
        if (tickCount % (TPS * 10) == 0) {
            build();
        }
        if (tickCount % (TPS * 2) == 0) {
            produce();
            if (units > 5) {
                attack();
            }
        }
        if (isBuildDone()) {
            placeBuild();
        }
        if (unitPlan.size() < 5) {
            int r = rand.nextInt(100);
            if (r < 50) {
                unitPlan.add(Soldier.class);
            } else if (r < 80) {
                unitPlan.add(Buggy.class);
            } else if (r < 95) {
                unitPlan.add(Heli.class);
            } else {
                unitPlan.add(Harvester.class);
            }
        }
        if (buildingPlan.isEmpty()) {
            // Check that we have all
            if (entities.stream().noneMatch(e -> e instanceof Powerplant)) {
                buildingPlan.add(Powerplant.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Refinery)) {
                buildingPlan.add(Refinery.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Barracks)) {
                buildingPlan.add(Barracks.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Factory)) {
                buildingPlan.add(Factory.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Helipad)) {
                buildingPlan.add(Helipad.class);
            }
        }
    }

    private void attack() {
        Iterator<Entity> others = level.findClosestEntity(spawn.getLevelX(), spawn.getLevelY(), e -> e instanceof Entity.PlayableEntity other && other.getOwner() != this);
        Entity target = null;
        int r = rand.nextInt(3);
        try {
            for (int i = 0; i < r; i++) {
                target = others.next();
            }
        } catch (NoSuchElementException ignored) {
        }

        if (target != null) {
            List<Unit> attackers = entities.stream().filter(e -> e instanceof Unit unit && !(e instanceof Harvester) && unit.getCommands().isEmpty()).map(e -> (Unit) e).toList();
            if (!attackers.isEmpty()) {
                int total = attackers.size();
                int participating = rand.nextInt(total);
                for (int i = 0; i < participating; i++) {
                    Unit attacker = attackers.get(i);
                    if (attacker instanceof Heli) {
                        attacker.sendCommand(new Command.FlyAndAttackCommand(attacker.x, attacker.y, target));
                    } else {
                        attacker.sendCommand(new Command.MoveAndAttackCommand(attacker.x, attacker.y, level.getPathfinder(), target));
                    }
                }
            }
        }
    }

    private void produce() {
        int r = rand.nextInt(4);
        for (int i = 0; i < r; i++) {
            if (unitPlan.isEmpty()) {
                return;
            }
            Class<? extends Unit> klass = unitPlan.getFirst();
            if (Entity.PlayableEntity.canBeBuilt(klass, this)) {
                List<Building> producers = entities.stream().filter(e -> e instanceof Building building && building.getProduces().contains(klass)).map(e -> (Building) e).sorted(Comparator.comparingInt(building -> building.getCommands().size())).toList();
                if (!producers.isEmpty()) {
                    Building building = producers.getFirst();
                    takeMoney(Entity.PlayableEntity.getCost(klass));
                    building.sendCommand(new Command.ProduceCommand(Entity.PlayableEntity.getBuildTime(klass), klass, level.getPathfinder()));
                    unitPlan.removeFirst();
                }
            }
        }
    }

    private void build() {
        if (buildingPlan.isEmpty()) {
            return;
        }
        Class<? extends Building> building = buildingPlan.getFirst();
        if (Entity.PlayableEntity.canBeBuilt(building, this) && !isBuilding()) {
            startBuild(building);
            buildingPlan.removeFirst();
        }
    }

    private void placeBuild() {
        Iterator<Tile> close = level.findClosestTile(spawn, level::isTileBuildable);
        int pos = rand.nextInt(5 + buildings);
        Tile last = null;
        try {
            for (int i = 0; i < pos; i++) {
                last = close.next();
            }
        } catch (NoSuchElementException ignored) {
        }

        if (last != null) {
            Building result = getBuildResult(last.getX(), last.getY());
            finishBuild(result);
        }
    }
}
