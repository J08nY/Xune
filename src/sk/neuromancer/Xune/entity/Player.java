package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.*;

import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public class Player implements Tickable, Renderable {
    protected final Game game;
    protected final Level level;
    private final boolean[][] visible;
    private final boolean[][] discovered;

    protected List<Entity.PlayableEntity> entities = new LinkedList<>();
    protected List<Entity.PlayableEntity> toAdd = new LinkedList<>();
    protected List<Entity.PlayableEntity> toRemove = new LinkedList<>();

    protected int money;
    protected final Flag flag;
    protected final Map<Class<? extends Entity.PlayableEntity>, CommandStrategy> commandStrategies = new HashMap<>();

    public Player(Game game, Level level, Flag flag, int money) {
        this.game = game;
        this.level = level;
        this.flag = flag;
        this.money = money;

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
    }

    public void removeEntity(Entity.PlayableEntity e) {
        toRemove.add(e);
        level.removeEntity(e);
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

    public int getPowerConsumption() {
        return Math.abs(entities.stream().filter(e -> e instanceof Building).mapToInt(e -> ((Building) e).getPower()).filter(i -> i < 0).sum());
    }

    public int getPowerProduction() {
        return entities.stream().filter(e -> e instanceof Building).mapToInt(e -> ((Building) e).getPower()).filter(i -> i > 0).sum();
    }

    @Override
    public void render() {
        entities.stream().filter(e -> level.getHuman().isTileDiscovered(level.tileAt(e))).sorted(Comparator.comparingDouble(e -> e.y)).forEach(Entity::render);
    }

    @Override
    public void tick(int tickCount) {
        entities.addAll(toAdd);
        toAdd.clear();
        for (Entity e : entities) {
            e.tick(tickCount);
        }
        handleDead();
        handleUnitBehavior();
        entities.removeAll(toRemove);
        toRemove.clear();
        updateVisibility();
    }

    protected void handleDead() {
        for (Entity.PlayableEntity e : entities) {
            if (e.health <= 0) {
                if (!e.commands.isEmpty()) {
                    e.commands.forEach(c -> c.finish(e, Game.currentTick(), false));
                }
                removeEntity(e);
                level.addEffect(new Effect.Explosion(e.x, e.y));
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
        for (int x = 0; x < level.getWidthInTiles(); x++) {
            for (int y = 0; y < this.level.getHeightInTiles(); y++) {
                visible[x][y] = false;
            }
        }
        for (Entity.PlayableEntity e : entities) {
            updateVisibility(e);
        }
    }

    private void updateVisibility(Entity.PlayableEntity entity) {
        for (int x = 0; x < level.getWidthInTiles(); x++) {
            for (int y = 0; y < this.level.getHeightInTiles(); y++) {
                float cx = tileToCenterLevelX(x, y);
                float cy = tileToCenterLevelY(x, y);
                if (entity instanceof Unit unit) {
                    if (unit.inSight(cx, cy)) {
                        visible[x][y] = true;
                        discovered[x][y] = true;
                    }
                } else if (entity instanceof Building building) {
                    if (building.inSight(cx, cy)) {
                        visible[x][y] = true;
                        discovered[x][y] = true;
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
        return discovered[column][row];
    }
}
