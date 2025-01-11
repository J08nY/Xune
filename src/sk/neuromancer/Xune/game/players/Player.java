package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Base;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.building.Powerplant;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Player implements Tickable, Renderable {
    protected final Game game;
    protected final Level level;
    private final boolean[][] visible;
    private final boolean[][] discovered;

    protected List<Entity.PlayableEntity> entities = new ArrayList<>();
    protected List<Entity.PlayableEntity> toAdd = new LinkedList<>();
    protected List<Entity.PlayableEntity> toRemove = new LinkedList<>();

    protected int money;
    protected int powerProduction;
    protected int powerConsumption;
    protected final Flag flag;
    protected final Map<Class<? extends Entity.PlayableEntity>, CommandStrategy> commandStrategies = new HashMap<>();

    protected Class<? extends Building> buildingToBuild;
    protected float buildProgress;
    protected int buildDuration;

    public Player(Game game, Level level, Flag flag, int money) {
        this.game = game;
        this.level = level;
        this.flag = flag;
        this.money = money;
        level.addPlayer(this);

        this.visible = new boolean[level.getWidthInTiles()][level.getHeightInTiles()];
        this.discovered = new boolean[level.getWidthInTiles()][level.getHeightInTiles()];

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
        return entities.isEmpty();
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
}
