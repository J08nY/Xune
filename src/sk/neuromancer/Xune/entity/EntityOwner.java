package sk.neuromancer.Xune.entity;

import java.util.ArrayList;
import java.util.List;

import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;


public class EntityOwner implements Tickable, Renderable {
    protected List<Entity> entities = new ArrayList<Entity>();
    protected int money;
    protected Flag flag;

    public EntityOwner() {

    }

    public EntityOwner(Flag flag, int money) {
        this.flag = flag;
        this.money = money;
    }

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public List<Entity> getEntities() {
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
    }

    @Override
    public void tick(int tickCount) {
        for (Entity e : entities) {
            e.tick(tickCount);
        }
    }

}
