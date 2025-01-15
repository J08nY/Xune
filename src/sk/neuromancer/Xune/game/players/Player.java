package sk.neuromancer.Xune.game.players;

import com.google.protobuf.ByteString;
import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Base;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Powerplant;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Effect;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.proto.PlayerProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.*;

public class Player implements Tickable, Renderable {
    protected final Game game;
    protected final Level level;
    private final boolean[][] visible;
    private final boolean[][] discovered;

    protected List<Entity.PlayableEntity> entities = new ArrayList<>();
    protected List<Entity.PlayableEntity> toAdd = new LinkedList<>();
    protected List<Entity.PlayableEntity> toRemove = new LinkedList<>();

    protected long id;
    protected int money;
    protected int powerProduction;
    protected int powerConsumption;
    protected final Flag flag;
    protected final Map<Class<? extends Entity.PlayableEntity>, CommandStrategy> commandStrategies = new HashMap<>();

    protected Class<? extends Building> buildingToBuild;
    protected float buildProgress;
    protected int buildDuration;

    protected Player(Game game, Level level, Flag flag, int money) {
        this.game = game;
        this.level = level;
        this.flag = flag;
        this.money = money;
        this.id = level.addPlayer(this);

        this.visible = new boolean[level.getWidthInTiles()][level.getHeightInTiles()];
        this.discovered = new boolean[level.getWidthInTiles()][level.getHeightInTiles()];

        commandStrategies.put(Heli.class, new CommandStrategy.AirAttackStrategy());
        commandStrategies.put(Buggy.class, new CommandStrategy.GroundAttackStrategy());
        commandStrategies.put(Harvester.class, new CommandStrategy.SpiceCollectStrategy());
        commandStrategies.put(Soldier.class, new CommandStrategy.GroundAttackStrategy());
    }

    protected Player(Game game, Level level, PlayerProto.PlayerState savedState) {
        this.game = game;
        this.level = level;
        this.flag = Flag.deserialize(savedState.getFlag());
        this.money = savedState.getMoney();
        this.id = savedState.getId();
        this.powerProduction = savedState.getPowerProduction();
        this.powerConsumption = savedState.getPowerConsumption();
        if (savedState.getBuildingKlass() != BaseProto.EntityClass.NULL) {
            this.buildingToBuild = Entity.fromEntityClass(savedState.getBuildingKlass()).asSubclass(Building.class);
            this.buildDuration = savedState.getBuildDuration();
            this.buildProgress = savedState.getBuildProgress();
        }
        for (PlayerProto.PlayerEntity entity : savedState.getEntitiesList()) {
            if (entity.hasUnit()) {
                Class<? extends Unit> klass = Entity.fromEntityClass(entity.getUnit().getPlayable().getEntity().getKlass()).asSubclass(Unit.class);
                try {
                    Unit u = klass.getConstructor(EntityStateProto.UnitState.class, Player.class).newInstance(entity.getUnit(), this);
                    entities.add(u);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else if (entity.hasBuilding()) {
                Class<? extends Building> klass = Entity.fromEntityClass(entity.getBuilding().getPlayable().getEntity().getKlass()).asSubclass(Building.class);
                try {
                    Building b = klass.getConstructor(EntityStateProto.BuildingState.class, Player.class).newInstance(entity.getBuilding(), this);
                    entities.add(b);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        int w = level.getWidthInTiles();
        int h = level.getHeightInTiles();
        this.visible = deserializeVisibility(savedState.getVisible().toByteArray(), w, h);
        this.discovered = deserializeVisibility(savedState.getDiscovered().toByteArray(), w, h);

        commandStrategies.put(Heli.class, new CommandStrategy.AirAttackStrategy());
        commandStrategies.put(Buggy.class, new CommandStrategy.GroundAttackStrategy());
        commandStrategies.put(Harvester.class, new CommandStrategy.SpiceCollectStrategy());
        commandStrategies.put(Soldier.class, new CommandStrategy.GroundAttackStrategy());
    }

    public void addEntity(Entity.PlayableEntity e) {
        toAdd.add(e);
        level.addEntity(e);
        if (e instanceof Building b) {
            int power = b.getPower();
            if (power > 0) {
                powerProduction += power;
            } else {
                powerConsumption += power;
            }
        }
    }

    public void removeEntity(Entity.PlayableEntity e) {
        toRemove.add(e);
        level.removeEntity(e);
        if (e instanceof Building b) {
            int power = b.getPower();
            if (power > 0) {
                powerProduction -= power;
            } else {
                powerConsumption -= power;
            }
        }
    }

    public List<Entity.PlayableEntity> getEntities() {
        return entities;
    }

    public Game getGame() {
        return game;
    }

    public Level getLevel() {
        return level;
    }

    public Flag getFlag() {
        return flag;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int money) {
        this.money += money;
    }

    public void takeMoney(int money) {
        this.money -= money;
    }

    public boolean isEliminated() {
        return entities.isEmpty() && toAdd.isEmpty();
    }

    protected Tile setupSpawn() {
        Tile spawn = level.spawnOf(this);
        addEntity(new Base(spawn.getX(), spawn.getY(), Orientation.NORTH, this));
        Iterator<Tile> closest = level.findClosestTile(spawn, level::isTileBuildable);
        Tile powerplantTile = closest.next();
        Tile refineryTile = closest.next();
        Tile harvesterTile = closest.next();
        addEntity(new Powerplant(powerplantTile.getX(), powerplantTile.getY(), Orientation.NORTH, this));
        addEntity(new Refinery(refineryTile.getX(), refineryTile.getY(), Orientation.NORTH, this));
        addEntity(new Harvester(Level.tileToCenterLevelX(harvesterTile.getX(), harvesterTile.getY()), Level.tileToCenterLevelY(harvesterTile.getX(), harvesterTile.getY()), Orientation.NORTH, this));
        return spawn;
    }

    public int getPowerConsumption() {
        return Math.abs(powerConsumption);
    }

    public int getPowerProduction() {
        return powerProduction;
    }

    public float getPowerFactor() {
        return (float) getPowerProduction() / getPowerConsumption();
    }

    protected void startBuild(Class<? extends Entity.PlayableEntity> klass) {
        takeMoney(Entity.PlayableEntity.getCost(klass));
        buildingToBuild = klass.asSubclass(Building.class);
        buildProgress = 0;
        buildDuration = Entity.PlayableEntity.getBuildTime(klass);
    }

    public boolean isBuilding() {
        return buildingToBuild != null;
    }

    public boolean isBuildDone() {
        return buildDuration != 0 && buildProgress >= buildDuration;
    }

    public float getBuildProgress() {
        if (!isBuilding()) {
            return 0.0f;
        }
        return Math.min(buildProgress / buildDuration, 1.0f);
    }

    protected Building getBuildResult(int tileX, int tileY) {
        try {
            return buildingToBuild.getConstructor(int.class, int.class, Orientation.class, Player.class).newInstance(tileX, tileY, Orientation.NORTH, this);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            return null;
        }
    }

    protected void finishBuild(Building building) {
        addEntity(building);
        buildProgress = 0;
        buildDuration = 0;
        buildingToBuild = null;
    }

    private boolean renderFilter(Entity e) {
        Tile at = level.tileAt(e);
        Human human = level.getHuman();
        if (e instanceof Building) {
            return human.isTileDiscovered(at);
        } else {
            return human.isTileVisible(at);
        }
    }

    @Override
    public void render() {
        entities.stream().filter(this::renderFilter).sorted(Comparator.comparingDouble(e -> e.y)).forEach(Entity::render);
    }

    @Override
    public void tick(int tickCount) {
        entities.addAll(toAdd);
        toAdd.clear();
        for (Entity e : entities) {
            e.tick(tickCount);
        }
        handleBuild();
        handleDead();
        handleUnitBehavior();
        entities.removeAll(toRemove);
        toRemove.clear();
        updateVisibility();
    }

    private void handleBuild() {
        if (isBuilding()) {
            buildProgress += Math.min(getPowerFactor(), 1.0f);
        }
    }

    private void handleDead() {
        for (Entity.PlayableEntity e : entities) {
            if (e.health <= 0) {
                if (e.hasCommands()) {
                    e.getCommands().forEach(c -> c.finish(e, Game.currentTick(), false));
                }
                removeEntity(e);
                if (!(e instanceof Soldier)) {
                    level.addEffect(new Effect.Explosion(e.x, e.y));
                }
                if (e instanceof Building) {
                    level.addEffect(new Effect.Sparkle(e.x, e.y));
                }
                SoundManager.play(Entity.getDeathSound(e.getClass()), false, 1.0f);
            }
        }
    }

    private void handleUnitBehavior() {
        for (Entity.PlayableEntity entity : entities) {
            if (!entity.hasCommands() && !toRemove.contains(entity)) {
                CommandStrategy strategy = commandStrategies.get(entity.getClass());
                if (strategy != null) {
                    Command defaultCommand = strategy.defaultBehavior(entity, level);
                    if (defaultCommand != null) {
                        entity.pushCommand(defaultCommand);
                    }
                }
            }
        }
    }

    private void updateVisibility() {
        int w = level.getWidthInTiles();
        int h = level.getHeightInTiles();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                visible[x][y] = false;
            }
        }
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Tile t = level.getTile(x, y);
                float cx = t.getCenterX();
                float cy = t.getCenterY();
                for (Entity.PlayableEntity e : entities) {
                    if (e.inSight(cx, cy)) {
                        visible[x][y] = true;
                        discovered[x][y] = true;
                        break;
                    }
                }
            }
        }
    }

    public boolean[][] getVisible() {
        return visible;
    }

    public boolean isTileVisible(Tile tile) {
        if (tile == null) {
            return true;
        }
        return visible[tile.getX()][tile.getY()];
    }

    public boolean isTileVisible(int column, int row) {
        if (column < 0 || column >= level.getWidthInTiles() || row < 0 || row >= level.getHeightInTiles()) {
            return false;
        }
        return visible[column][row];
    }

    public boolean[][] getDiscovered() {
        return discovered;
    }

    public boolean isTileDiscovered(Tile tile) {
        if (tile == null) {
            return true;
        }
        return discovered[tile.getX()][tile.getY()];
    }

    public boolean isTileDiscovered(int column, int row) {
        if (column < 0 || column >= level.getWidthInTiles() || row < 0 || row >= level.getHeightInTiles()) {
            return false;
        }
        return discovered[column][row];
    }

    public long getId() {
        return id;
    }

    private byte[] serializeVisibility(boolean[][] array) {
        int rows = array.length;
        int cols = array[0].length;
        int byteLength = (rows * cols + 7) / 8;
        ByteBuffer buffer = ByteBuffer.allocate(byteLength);
        byte currentByte = 0;
        int bitIndex = 0;

        for (boolean[] row : array) {
            for (boolean b : row) {
                if (b) {
                    currentByte |= (byte) (1 << (7 - bitIndex));
                }
                bitIndex++;
                if (bitIndex == 8) {
                    buffer.put(currentByte);
                    currentByte = 0;
                    bitIndex = 0;
                }
            }
        }

        // Store the last byte if there are remaining bits
        if (bitIndex > 0) {
            buffer.put(currentByte);
        }

        return buffer.array();
    }

    private boolean[][] deserializeVisibility(byte[] data, int width, int height) {
        boolean[][] array = new boolean[width][height];
        int byteIndex = 0;
        int bitIndex = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ((data[byteIndex] & (1 << (7 - bitIndex))) != 0) {
                    array[x][y] = true;
                }
                bitIndex++;
                if (bitIndex == 8) {
                    byteIndex++;
                    bitIndex = 0;
                }
            }
        }

        return array;
    }

    public PlayerProto.PlayerState serialize() {
        PlayerProto.PlayerState.Builder builder = PlayerProto.PlayerState.newBuilder()
                .setId(id)
                .setMoney(money)
                .setPlayerClass(toPlayerClass(getClass()))
                .setPowerProduction(powerProduction)
                .setPowerConsumption(powerConsumption)
                .setFlag(flag.serialize())
                .setBuildingKlass(Entity.PlayableEntity.toEntityClass(buildingToBuild))
                .setBuildProgress(buildProgress)
                .setBuildDuration(buildDuration)
                .setVisible(ByteString.copyFrom(serializeVisibility(visible)))
                .setDiscovered(ByteString.copyFrom(serializeVisibility(discovered)));

        for (Entity.PlayableEntity e : entities) {
            if (e instanceof Unit unit) {
                builder.addEntities(PlayerProto.PlayerEntity.newBuilder().setUnit(unit.serialize()).build());
            } else if (e instanceof Building building) {
                builder.addEntities(PlayerProto.PlayerEntity.newBuilder().setBuilding(building.serialize()).build());
            }
        }
        return builder.build();
    }

    public static PlayerProto.PlayerClass toPlayerClass(Class<? extends Player> klass) {
        if (klass == Human.class) {
            return PlayerProto.PlayerClass.HUMAN;
        } else if (klass == Bot.ArmyGeneral.class) {
            return PlayerProto.PlayerClass.BOT_ARMY_GENERAL;
        } else if (klass == Bot.BuggyBoy.class) {
            return PlayerProto.PlayerClass.BOT_BUGGY_BOY;
        } else if (klass == Bot.HeliMaster.class) {
            return PlayerProto.PlayerClass.BOT_HELI_MASTER;
        } else if (klass == Bot.JackOfAllTrades.class) {
            return PlayerProto.PlayerClass.BOT_JACK_OF_ALL_TRADES;
        } else if (klass == Bot.EconGraduate.class) {
            return PlayerProto.PlayerClass.BOT_ECON_GRADUATE;
        } else if (klass == Remote.class) {
            return PlayerProto.PlayerClass.REMOTE;
        } else {
            throw new IllegalArgumentException("Unknown player class: " + klass);
        }
    }
}
