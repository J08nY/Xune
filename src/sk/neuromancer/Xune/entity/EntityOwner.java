package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;

import java.util.LinkedList;
import java.util.List;


public class EntityOwner implements Tickable, Renderable {
    protected List<Entity.PlayableEntity> entities = new LinkedList<>();
    protected List<Effect> effects = new LinkedList<>();
    protected int money;
    protected Flag flag;

    public EntityOwner(Flag flag, int money) {
        this.flag = flag;
        this.money = money;
    }

    public void addEntity(Entity.PlayableEntity e) {
        entities.add(e);
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

}
