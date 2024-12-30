package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Harvester;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Soldier;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.*;


public class EntityOwner implements Tickable, Renderable {
    protected final Game game;
    protected final Level level;
    protected List<Entity.PlayableEntity> entities = new LinkedList<>();
    protected List<Entity.PlayableEntity> toAdd = new LinkedList<>();
    protected List<Entity.PlayableEntity> toRemove = new LinkedList<>();
    protected List<Effect> effects = new LinkedList<>();
    protected int money;
    protected Flag flag;
    protected final Map<Class<? extends Entity.PlayableEntity>, CommandStrategy> commandStrategies = new HashMap<>();

    public EntityOwner(Game game, Level level, Flag flag, int money) {
        this.game = game;
        this.level = level;
        this.flag = flag;
        this.money = money;

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

    @Override
    public void render() {
        entities.stream().filter(e -> (e instanceof Building) || level.isTileVisible(level.tileAt(e))).sorted(Comparator.comparingDouble(e -> e.y)).forEach(Entity::render);
        //for (Entity e : entities) {
        //    e.render();
        //}
        for (Effect e : effects) {
            e.render();
        }
    }

    @Override
    public void tick(int tickCount) {
        entities.addAll(toAdd);
        toAdd.clear();
        for (Entity e : entities) {
            e.tick(tickCount);
        }
        for (Effect e : effects) {
            e.tick(tickCount);
        }
        handleDead();
        handleUnitBehavior();
        entities.removeAll(toRemove);
        toRemove.clear();
    }

    protected void handleDead() {
        for (Entity.PlayableEntity e : entities) {
            if (e.health == 0) {
                removeEntity(e);
                this.effects.add(new Effect.Explosion(e.x, e.y));
                if (e instanceof Building) {
                    this.effects.add(new Effect.Sparkle(e.x, e.y));
                }
                game.getSound().play(SoundManager.SOUND_EXPLOSION_1, false, 1.0f);
            }
        }
        effects.removeIf(Effect::isFinished);
    }

    private void handleUnitBehavior() {
        for (Entity.PlayableEntity entity : entities) {
            if (!entity.hasCommands()) {
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
}
