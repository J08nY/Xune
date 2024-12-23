package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.LinkedList;
import java.util.List;


public class EntityOwner implements Tickable, Renderable {
    protected final Game game;
    protected final Level level;
    protected List<Entity.PlayableEntity> entities = new LinkedList<>();
    protected List<Effect> effects = new LinkedList<>();
    protected int money;
    protected Flag flag;

    public EntityOwner(Game game, Level level, Flag flag, int money) {
        this.game = game;
        this.level = level;
        this.flag = flag;
        this.money = money;
    }

    public void addEntity(Entity.PlayableEntity e) {
        entities.add(e);
        //TODO: Hook this and let Level know about the entity, it can then avoid handling buildings during a tick
    }

    public void removeEntity(Entity.PlayableEntity e) {
        entities.remove(e);
    }

    public List<Entity.PlayableEntity> getEntities() {
        return entities;
    }

    public Flag getFlag() {
        return flag;
    }

    public int getMoney() {
        return money;
    }

    @Override
    public void render() {
        for (Entity e : entities) {
            e.render();
        }
        for (Effect e : effects) {
            e.render();
        }
    }

    @Override
    public void tick(int tickCount) {
        for (Entity e : entities) {
            e.tick(tickCount);
        }
        for (Effect e : effects) {
            e.tick(tickCount);
        }
    }

    protected void handleDead() {
        List<Entity.PlayableEntity> toRemove = new LinkedList<>();
        for (Entity.PlayableEntity e : entities) {
            if (e.health == 0) {
                toRemove.add(e);
                this.effects.add(new Effect.Explosion(e.x, e.y));
                game.getSound().play(SoundManager.SOUND_EXPLOSION_1, false, 1.0f);
            }
        }
        toRemove.forEach(this::removeEntity);
        effects.removeIf(Effect::isFinished);
    }
}
