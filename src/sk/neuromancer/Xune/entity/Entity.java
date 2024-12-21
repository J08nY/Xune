package sk.neuromancer.Xune.entity;

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
        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

        public Orientation cw() {
            return switch (this) {
                case NORTH -> NORTHEAST;
                case NORTHEAST -> EAST;
                case EAST -> SOUTHEAST;
                case SOUTHEAST -> SOUTH;
                case SOUTH -> SOUTHWEST;
                case SOUTHWEST -> WEST;
                case WEST -> NORTHWEST;
                case NORTHWEST -> NORTH;
            };
        }

        public Orientation ccw() {
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

        public static Orientation fromAngle(float radians) {
            double degrees = Math.toDegrees(radians);
            if (degrees < 0) {
                degrees += 360;
            }
            degrees %= 360;
            int index = (int) Math.round(degrees / 45) % 8;
            return switch (index) {
                case 0 -> NORTH;
                case 1 -> NORTHEAST;
                case 2 -> EAST;
                case 3 -> SOUTHEAST;
                case 4 -> SOUTH;
                case 5 -> SOUTHWEST;
                case 6 -> WEST;
                case 7 -> NORTHWEST;
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }
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
        protected List<Command> commands;
        protected Flag flag;
        protected boolean isSelected;

        public PlayableEntity(float x, float y, EntityOwner owner, Flag flag) {
            super(x, y);
            this.owner = owner;
            this.commands = new LinkedList<>();
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

        public Command currentCommand() {
            return this.commands.isEmpty() ? null : this.commands.getFirst();
        }

        public void sendCommand(Command c) {
            this.commands.add(c);
        }
    }

    public static abstract class Building extends PlayableEntity {
        public Building(float x, float y, EntityOwner owner, Flag flag) {
            super(x, y, owner, flag);
        }
    }

    public static abstract class Unit extends PlayableEntity {
        public Unit(float x, float y, EntityOwner owner, Flag flag) {
            super(x, y, owner, flag);
        }
    }
}
