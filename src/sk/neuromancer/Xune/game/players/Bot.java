package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.Command;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;

import java.util.*;
import java.util.stream.Collectors;

import static sk.neuromancer.Xune.game.Game.TPS;


public class Bot extends Player {
    private final Tile spawn;
    protected List<Class<? extends Building>> buildingPlan = new LinkedList<>();
    protected List<Class<? extends Unit>> unitPlan = new LinkedList<>();
    protected int soldierPriority = 0;
    protected int buggyPriority = 0;
    protected int heliPriority = 0;

    protected int buildInterval = 15;
    protected int produceInterval = 2;
    protected int attackInterval = 3;
    protected int attackGroupSize = 5;

    private Random rand = new Random();
    private int buildings;
    private int units;
    private int harvesters;

    Bot(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.spawn = setupSpawn();
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        buildings = entities.stream().filter(e -> e instanceof Building).mapToInt(e -> 1).sum();
        units = entities.stream().filter(e -> e instanceof Unit).mapToInt(e -> 1).sum();
        harvesters = entities.stream().filter(e -> e instanceof Harvester).mapToInt(e -> 1).sum();

        checkSpiceCollection();
        if (tickCount % (TPS * produceInterval) == 0) {
            produce();
        }
        if (tickCount % (TPS * buildInterval) == 0) {
            build();
        }
        if (isBuildDone()) {
            placeBuild();
        }
        if (tickCount % (TPS * attackInterval) == 0 && units >= attackGroupSize) {
            attack();
        }
        defend();
        planUnitBuild();
        planBuildingBuild();
        if (tickCount % TPS == 0) {
            log("Bot: " + getClass().getSimpleName() + "." + flag + " Money: " + money + " Buildings: " + buildings + " Units: " + units + " Harvesters: " + harvesters);
            log("\tUnitPlan " + unitPlan.stream().map(Class::getSimpleName).toList());
            log("\tBuildingPlan " + buildingPlan.stream().map(Class::getSimpleName).toList());
        }
    }

    private void checkSpiceCollection() {
        if (harvesters == 0) {
            unitPlan.removeIf(klass -> klass == Harvester.class);
            unitPlan.addFirst(Harvester.class);
            produce();
        }
        if (entities.stream().noneMatch(e -> e instanceof Refinery)) {
            buildingPlan.removeIf(klass -> klass == Refinery.class);
            buildingPlan.addFirst(Refinery.class);
            build();
        }
    }

    private void planBuildingBuild() {
        if (buildingPlan.isEmpty()) {
            // Check that we have all
            if (entities.stream().noneMatch(e -> e instanceof Powerplant)) {
                buildingPlan.add(Powerplant.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Barracks)) {
                buildingPlan.add(Barracks.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Factory)) {
                buildingPlan.add(Factory.class);
            } else if (entities.stream().noneMatch(e -> e instanceof Helipad)) {
                buildingPlan.add(Helipad.class);
            }
        }
    }

    private void planUnitBuild() {
        if (unitPlan.size() < 5) {
            int r = rand.nextInt(100);
            if (r < soldierPriority) {
                unitPlan.add(Soldier.class);
            } else if (r < soldierPriority + buggyPriority) {
                unitPlan.add(Buggy.class);
            } else if (r < soldierPriority + buggyPriority + heliPriority) {
                unitPlan.add(Heli.class);
            } else {
                unitPlan.add(Harvester.class);
            }
        }
    }

    private void defend() {
        List<Entity.PlayableEntity> underAttack = entities.stream().filter(e -> (e instanceof Building || e instanceof Harvester) && e.isUnderAttack()).toList();
        if (!underAttack.isEmpty()) {
            List<Unit> freeUnits = entities.stream().filter(e -> e instanceof Unit unit && !(e instanceof Harvester) && !unit.hasCommands()).map(e -> (Unit) e).toList();
            log("Defending with " + freeUnits.size() + " units.");
            for (int i = 0; i < freeUnits.size(); i++) {
                Entity.PlayableEntity attacked = underAttack.get(i % underAttack.size());
                Unit unit = freeUnits.get(i);
                try {
                    unit.sendCommand(new Command.MoveAndAttackCommand(unit.x, unit.y, level.getPathfinder(), attacked.getAttacker()));
                } catch (NoPathFound ignored) {
                }
            }
        }
    }

    private void attack() {
        Iterator<Entity> others = level.findClosestEntity(spawn.getLevelX(), spawn.getLevelY(), e -> e instanceof Entity.PlayableEntity other && other.getOwner() != this);
        Entity.PlayableEntity target = null;
        int r = rand.nextInt(3);
        try {
            for (int i = 0; i < r; i++) {
                target = (Entity.PlayableEntity) others.next();
            }
        } catch (NoSuchElementException ignored) {
        }

        if (target != null) {
            Entity finalTarget = target;
            List<Unit> attackers = entities.stream()
                    .filter(e -> e instanceof Unit unit && !(e instanceof Harvester) && unit.getCommands().isEmpty())
                    .map(e -> (Unit) e)
                    .sorted(Comparator.comparingDouble(e -> Math.abs(e.x - finalTarget.x) + Math.abs(e.y - finalTarget.y))).collect(Collectors.toCollection(ArrayList::new));
            if (!attackers.isEmpty()) {
                int total = attackers.size();
                int participating = rand.nextInt(total);
                log("Attacking with " + participating + " units. Target: " + target);
                int successful = 0;
                for (int i = 0; i < participating; i++) {
                    if (attackers.isEmpty()) {
                        log("Successfully attacked with: " + successful);
                        break;
                    }
                    Unit attacker = attackers.removeFirst();
                    if (attacker instanceof Heli) {
                        attacker.sendCommand(new Command.FlyAndAttackCommand(attacker.x, attacker.y, target));
                        successful++;
                    } else {
                        if (attacker.inRange(target) && target.isStatic()) {
                            attacker.sendCommand(new Command.AttackCommand(target));
                            successful++;
                        } else {
                            try {
                                attacker.sendCommand(new Command.MoveAndAttackCommand(attacker.x, attacker.y, level.getPathfinder(), target));
                                successful++;
                            } catch (NoPathFound ignored) {
                                i--;
                            }
                        }
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
                    log("Producing " + klass.getSimpleName());
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
            log("Building " + building);
            startBuild(building);
            buildingPlan.removeFirst();
        }
    }

    private void placeBuild() {
        Iterator<Tile> close = level.findClosestTile(spawn, tile -> level.isTileBuildable(tile.getX(), tile.getY(), Building.getPassable(buildingToBuild)));
        int pos = rand.nextInt(3 + buildings);
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

    private void log(String message) {
        if (Config.LOG_BOT_ACTIONS) {
            System.out.println(message);
        }
    }

    public static class ArmyGeneral extends Bot {
        public ArmyGeneral(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Barracks.class);
            buildingPlan.add(Powerplant.class);
            buildingPlan.add(Barracks.class);

            unitPlan.add(Soldier.class);
            unitPlan.add(Soldier.class);

            soldierPriority = 95;
            buggyPriority = 0;
            heliPriority = 0;
        }
    }

    public static class BuggyBoy extends Bot {
        public BuggyBoy(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Factory.class);
            buildingPlan.add(Powerplant.class);
            buildingPlan.add(Factory.class);

            unitPlan.add(Buggy.class);
            unitPlan.add(Buggy.class);

            soldierPriority = 0;
            buggyPriority = 95;
            heliPriority = 0;
        }
    }

    public static class HeliMaster extends Bot {
        public HeliMaster(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Helipad.class);
            buildingPlan.add(Powerplant.class);
            buildingPlan.add(Helipad.class);

            unitPlan.add(Heli.class);
            unitPlan.add(Heli.class);

            soldierPriority = 0;
            buggyPriority = 0;
            heliPriority = 95;
        }
    }

    public static class JackOfAllTrades extends Bot {
        public JackOfAllTrades(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Powerplant.class);
            buildingPlan.add(Barracks.class);
            buildingPlan.add(Factory.class);
            buildingPlan.add(Helipad.class);

            unitPlan.add(Soldier.class);
            unitPlan.add(Buggy.class);
            unitPlan.add(Heli.class);

            soldierPriority = 50;
            buggyPriority = 30;
            heliPriority = 10;
        }
    }
}
