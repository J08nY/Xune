package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.NoPathFound;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.PlayerProto;

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

    protected boolean buildsBarracks = true;
    protected boolean buildsFactory = true;
    protected boolean buildsHelipad = true;
    protected boolean buildsPowerplant = true;

    private Random rand = new Random();
    private int buildings;
    private int units;
    private int harvesters;

    Bot(Game game, Level level, Flag flag, int money) {
        super(game, level, flag, money);
        this.spawn = setupSpawn();
    }

    @SuppressWarnings("unchecked")
    Bot(Game game, Level level, PlayerProto.PlayerState savedState) {
        super(game, level, savedState);
        this.spawn = new Tile(48, savedState.getBot().getSpawn().getX(), savedState.getBot().getSpawn().getY());
        this.buildingPlan = savedState.getBot().getBuildingPlanList().stream().map(Entity::fromEntityClass).map(e -> (Class<? extends Building>) e).collect(Collectors.toCollection(LinkedList::new));
        this.unitPlan = savedState.getBot().getUnitPlanList().stream().map(Entity::fromEntityClass).map(e -> (Class<? extends Unit>) e).collect(Collectors.toCollection(LinkedList::new));
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
            if (buildsPowerplant && entities.stream().noneMatch(e -> e instanceof Powerplant)) {
                buildingPlan.add(Powerplant.class);
            } else if (buildsBarracks && entities.stream().noneMatch(e -> e instanceof Barracks)) {
                buildingPlan.add(Barracks.class);
            } else if (buildsFactory && entities.stream().noneMatch(e -> e instanceof Factory)) {
                buildingPlan.add(Factory.class);
            } else if (buildsHelipad && entities.stream().noneMatch(e -> e instanceof Helipad)) {
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
        List<PlayableEntity> underAttack = entities.stream().filter(e -> (e instanceof Building || e instanceof Harvester) && e.isUnderAttack()).toList();
        if (!underAttack.isEmpty()) {
            List<Unit> freeUnits = entities.stream().filter(e -> e instanceof Unit unit && !(e instanceof Harvester) && !unit.hasCommands()).map(e -> (Unit) e).toList();
            log("Defending with " + freeUnits.size() + " units.");
            for (int i = 0; i < freeUnits.size(); i++) {
                PlayableEntity attacked = underAttack.get(i % underAttack.size());
                Set<EntityReference> attackers = attacked.getAttackers();
                Unit unit = freeUnits.get(i);
                try {
                    unit.sendCommand(new Command.MoveAndAttackCommand(unit.x, unit.y, level.getPathfinder(), attackers.stream().findFirst().get().resolve(level)));
                } catch (NoPathFound ignored) {
                }
            }
        }
    }

    private void attack() {
        Iterator<Entity> others = level.findClosestEntity(spawn.getLevelX(), spawn.getLevelY(), e -> e instanceof PlayableEntity other && other.getOwner() != this);
        PlayableEntity target = null;
        int r = rand.nextInt(3);
        try {
            for (int i = 0; i < r; i++) {
                target = (PlayableEntity) others.next();
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
            if (PlayableEntity.canBeBuilt(klass, this)) {
                List<Building> producers = entities.stream().filter(e -> e instanceof Building building && building.getProduces().contains(klass)).map(e -> (Building) e).sorted(Comparator.comparingInt(building -> building.getCommands().size())).toList();
                if (!producers.isEmpty()) {
                    log("Producing " + klass.getSimpleName());
                    Building building = producers.getFirst();
                    takeMoney(PlayableEntity.getCost(klass));
                    building.sendCommand(new Command.ProduceCommand(PlayableEntity.getBuildTime(klass), klass, level.getPathfinder()));
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
        if (PlayableEntity.canBeBuilt(building, this) && !isBuilding()) {
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

    public PlayerProto.PlayerState serialize() {
        PlayerProto.PlayerState.Builder state = super.serialize().toBuilder();
        PlayerProto.BotState.Builder bot = PlayerProto.BotState.newBuilder()
                .setSpawn(BaseProto.Tile.newBuilder().setX(spawn.getX()).setY(spawn.getY()).build())
                .addAllBuildingPlan(buildingPlan.stream().map(Entity::toEntityClass).toList())
                .addAllUnitPlan(unitPlan.stream().map(Entity::toEntityClass).toList());
        state.setBot(bot);
        return state.build();
    }

    public static class ArmyGeneral extends Bot {
        public ArmyGeneral(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Barracks.class);
            buildingPlan.add(Powerplant.class);
            buildingPlan.add(Barracks.class);

            unitPlan.add(Soldier.class);
            unitPlan.add(Soldier.class);

            buildsFactory = false;
            buildsHelipad = false;

            soldierPriority = 90;
            buggyPriority = 0;
            heliPriority = 0;
        }

        public ArmyGeneral(Game game, Level level, PlayerProto.PlayerState savedState) {
            super(game, level, savedState);
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

            buildsBarracks = false;
            buildsHelipad = false;

            soldierPriority = 0;
            buggyPriority = 90;
            heliPriority = 0;
        }

        public BuggyBoy(Game game, Level level, PlayerProto.PlayerState playerState) {
            super(game, level, playerState);
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

            buildsBarracks = false;
            buildsFactory = false;

            soldierPriority = 0;
            buggyPriority = 0;
            heliPriority = 90;
        }

        public HeliMaster(Game game, Level level, PlayerProto.PlayerState playerState) {
            super(game, level, playerState);
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

        public JackOfAllTrades(Game game, Level level, PlayerProto.PlayerState playerState) {
            super(game, level, playerState);
        }
    }

    public static class EconGraduate extends Bot {
        public EconGraduate(Game game, Level level, Flag flag, int money) {
            super(game, level, flag, money);
            buildingPlan.add(Factory.class);
            buildingPlan.add(Barracks.class);
            buildingPlan.add(Powerplant.class);

            unitPlan.add(Buggy.class);
            unitPlan.add(Harvester.class);
            unitPlan.add(Soldier.class);

            buildsHelipad = false;

            soldierPriority = 30;
            buggyPriority = 30;
            heliPriority = 10;
        }

        public EconGraduate(Game game, Level level, PlayerProto.PlayerState playerState) {
            super(game, level, playerState);
        }
    }
}
