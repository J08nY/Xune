package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public abstract class Entity implements Renderable, Tickable, Clickable {
    protected Sprite sprite;
    public float x, y;
    public int health;
    protected Orientation orientation;
    protected boolean isStatic;

    public static final int SPRITE_ID_BASE = 0;
    public static final int SPRITE_ID_FACTORY = 1;
    public static final int SPRITE_ID_REFINERY = 2;
    public static final int SPRITE_ID_SILO = 3;
    public static final int SPRITE_ID_HELIPAD = 4;
    public static final int SPRITE_ID_HARVESTER = 5;
    public static final int SPRITE_ID_HELI = 9;

    public static final int SPRITE_ROW_LENGTH = 17;

    public static final int SPRITE_OFFSET_RED = 0;
    public static final int SPRITE_OFFSET_GREEN = SPRITE_ROW_LENGTH * 2;
    public static final int SPRITE_OFFSET_BLUE = SPRITE_ROW_LENGTH * 4;


    public enum Flag {
        RED, BLUE, GREEN
    }

    public enum Orientation {
        //0		1		 2		3		4		5		6		7
        NORTH, NORTHWEST, WEST, SOUTHWEST, SOUTH, SOUTHEAST, EAST, NORTHEAST;

        public Orientation cw() {
            return switch (this) {
                case NORTH -> NORTHWEST;
                case NORTHWEST -> WEST;
                case WEST -> SOUTHWEST;
                case SOUTHWEST -> SOUTH;
                case SOUTH -> SOUTHEAST;
                case SOUTHEAST -> EAST;
                case EAST -> NORTHEAST;
                case NORTHEAST -> NORTH;
            };
        }

        public Orientation ccw() {
            return switch (this) {
                case NORTH -> NORTHEAST;
                case NORTHWEST -> NORTH;
                case WEST -> NORTHWEST;
                case SOUTHWEST -> WEST;
                case SOUTH -> SOUTHWEST;
                case SOUTHEAST -> SOUTH;
                case EAST -> SOUTHEAST;
                case NORTHEAST -> EAST;
            };
        }
    }

    public Entity() {

    }

    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x, y, 0);
        this.sprite.render();
        glPopMatrix();
    }

    public static abstract class ClickableEntity extends Entity implements Clickable {
        protected List<Clickable> clickableAreas = new ArrayList<Clickable>();

        public ClickableEntity(float x, float y) {
            super(x, y);
        }

        @Override
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
            for (Clickable area : clickableAreas) {
                area.setPosition(x, y);
            }
        }

        @Override
        public boolean intersects(float x, float y, Button b) {
            for (Clickable area : clickableAreas) {
                if (area.intersects(x, y, b)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY, Button b) {
            for (Clickable area : clickableAreas) {
                if (area.intersects(fromX, fromY, toX, toY, b)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static abstract class PlayableEntity extends ClickableEntity {
        protected EntityOwner owner;
        protected Flag flag;
        protected boolean isSelected;

        public PlayableEntity(float x, float y, EntityOwner owner, Flag flag) {
            super(x, y);
            this.owner = owner;
            this.flag = flag;
        }

        public static int getOffsetonFlag(Flag f) {
            return switch (f) {
                case RED -> SPRITE_OFFSET_RED;
                case GREEN -> SPRITE_OFFSET_GREEN;
                case BLUE -> SPRITE_OFFSET_BLUE;
            };
        }

        public void select() {
            this.isSelected = true;
        }

        public void unselect() {
            this.isSelected = false;
        }

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(x, y, 0);
            this.sprite.render();
            if (isSelected) {
                for (Clickable area : clickableAreas) {
                    if (area instanceof Renderable) {
                        ((Renderable) area).render();
                    }
                }
            }
            glPopMatrix();
        }

    }
}
