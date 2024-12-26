package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public abstract class Entity implements Renderable, Tickable, Clickable {
    protected Sprite sprite;
    public float x, y;
    public int health;
    protected int maxHealth;
    protected Orientation orientation;
    protected List<Clickable> clickableAreas = new ArrayList<>();

    public Entity(float x, float y, int maxHealth) {
        this.x = x;
        this.y = y;
        this.health = maxHealth;
        this.maxHealth = maxHealth;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        for (Clickable area : clickableAreas) {
            area.setPosition(x, y);
        }
    }

    @Override
    public boolean intersects(float x, float y) {
        for (Clickable area : clickableAreas) {
            if (area.intersects(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean intersects(float fromX, float fromY, float toX, float toY) {
        for (Clickable area : clickableAreas) {
            if (area.intersects(fromX, fromY, toX, toY)) {
                return true;
            }
        }
        return false;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
        }
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
        this.sprite.render();
        glPopMatrix();
    }

    public static abstract class PlayableEntity extends Entity {
        protected EntityOwner owner;
        protected List<Command> commands;
        protected Flag flag;
        protected boolean isSelected;

        public PlayableEntity(float x, float y, EntityOwner owner, Flag flag, int maxHealth) {
            super(x, y, maxHealth);
            this.owner = owner;
            this.commands = new LinkedList<>();
            this.flag = flag;
        }

        public void select() {
            this.isSelected = true;
        }

        public void unselect() {
            this.isSelected = false;
        }

        @Override
        public void tick(int tickCount) {
            if (!commands.isEmpty()) {
                Command current = commands.getFirst();
                current.execute(this, tickCount);
                if (current.isFinished(this)) {
                    System.out.println("Command finished" + current + " " + this);
                    commands.removeFirst();
                    current.finalize(this);
                }
            }
        }

        @Override
        public void render() {
            glPushMatrix();
            float screenY = owner.getLevel().getScreenY(y);
            float depth = screenY / Game.DEFAULT_HEIGHT;
            glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
            this.sprite.render();
            glPopMatrix();
            if (isSelected) {
                glPushMatrix();
                glTranslatef(x, y, 0);
                for (Clickable area : clickableAreas) {
                    if (area instanceof Renderable) {
                        ((Renderable) area).render();
                    }
                }
                glPopMatrix();
            }
        }

        public EntityOwner getOwner() {
            return owner;
        }

        public Command currentCommand() {
            return this.commands.isEmpty() ? null : this.commands.getFirst();
        }

        public void sendCommand(Command c) {
            this.commands.add(c);
        }

        public void pushCommand(Command c) {
            this.commands.clear();
            this.commands.add(c);
        }
    }

}
