package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Level.tileCenterX;
import static sk.neuromancer.Xune.level.Level.tileCenterY;

public abstract class Entity implements Renderable, Tickable, Clickable {
    protected Sprite sprite;
    public float x, y;
    public int health;
    protected int maxHealth;
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

    public Entity(float x, float y, int maxHealth) {
        this.x = x;
        this.y = y;
        this.health = maxHealth;
        this.maxHealth = maxHealth;
    }


    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
        this.sprite.render();
        glPopMatrix();
    }

    public static abstract class ClickableEntity extends Entity implements Clickable {
        protected List<Clickable> clickableAreas = new ArrayList<>();

        public ClickableEntity(float x, float y, int maxHealth) {
            super(x, y, maxHealth);
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
    }

    public static abstract class PlayableEntity extends ClickableEntity {
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
            glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
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

    public static abstract class Building extends PlayableEntity {
        public int tileX, tileY;

        public Building(int tileX, int tileY, EntityOwner owner, Flag flag, int maxHealth) {
            super(tileCenterX(tileX, tileY), tileCenterY(tileX, tileY), owner, flag, maxHealth);
            this.tileX = tileX;
            this.tileY = tileY;
        }

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
            this.sprite.render();
            if (isSelected) {
                SpriteSheet.MISC_SHEET.getSprite(1, 0).render();
            }
            glPopMatrix();
        }
    }

    public static abstract class Unit extends PlayableEntity {
        public Unit(float x, float y, EntityOwner owner, Flag flag, int maxHealth) {
            super(x, y, owner, flag, maxHealth);
        }

        protected void move(float toX, float toY, float speed) {
            float dx = toX - x;
            float dy = toY - y;
            float angle = (float) Math.atan2(dy, dx);
            float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
            this.orientation = Orientation.fromAngle(azimuth);
            updateSprite();
            setPosition(x + (float) (speed * Math.cos(angle)), y + (float) (speed * Math.sin(angle)));
        }

        protected abstract void updateSprite();

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0);
            this.sprite.render();
            if (isSelected) {
                SpriteSheet.MISC_SHEET.getSprite(0, 0).render();
                glPushMatrix();
                glTranslatef(0, sprite.getHeight(), 0);
                glBegin(GL_QUADS);
                glColor3f(0, 1, 0);
                float healthPercentage = (float) health / maxHealth;
                for (int i = 0; i < 12; i++) {
                    if (i < healthPercentage * 12) {
                        glColor3f(0, 1, 0);
                    } else {
                        glColor3f(1, 0, 0);
                    }
                    glVertex2f(((float) 25 / 12) * i, 0);
                    glVertex2f(((float) 25 / 12) * i + 1, 0);
                    glVertex2f(((float) 25 / 12) * i + 1, 1);
                    glVertex2f(((float) 25 / 12) * i, 1);
                }
                glEnd();
                glColor3f(1, 1, 1);
                glPopMatrix();
            }
            glPopMatrix();
        }
    }
}
