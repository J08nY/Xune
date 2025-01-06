package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.paths.Path;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class Worm extends Entity implements Moveable {
    private final Level level;
    private int animation = 0;
    private int dir = 1;
    private Queue<Path> plan = new LinkedList<>();
    private Path current;
    private int nextPoint;
    private float speed = 0.3f;
    private State state;
    private Position position;

    private Unit target;
    private float scale = 1f;

    private enum State {
        WANDERING,
        HUNTING,
        EATING;
    }

    private enum Position {
        ABOVE, BELOW;
    }

    static {
        setMaxHealth(Worm.class, 1000);
        setSight(Worm.class, 30);
        setDeathSound(Worm.class, SoundManager.SOUND_WORM_DEATH);
    }

    public Worm(Level level, float x, float y) {
        super(x, y);
        this.state = State.WANDERING;
        this.position = Position.BELOW;
        this.level = level;
        this.orientation = Orientation.SOUTH;
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(animation);
        this.clickableAreas.add(ClickableCircle.getCentered(this.x, this.y, 6, false));
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() * scale / 2, y - (float) sprite.getHeight() * scale / 2, 0);
        glScalef(scale, scale, 1);
        this.sprite.render();
        glPopMatrix();
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void tick(int tickCount) {
        switch (state) {
            case WANDERING -> wandering(tickCount);
            case HUNTING -> hunting(tickCount);
            case EATING -> eating(tickCount);
        }
        speed = ((float) health / maxHealth) * 0.3f;
        updateSprite();
    }

    private void eating(int tickCount) {
        target.setImmobile(true);
        scale = 2f;
        position = Position.ABOVE;
        if (tickCount % 25 == 0) {
            float angleFrom = orientation.opposite().toAzimuthRadians();
            float dx = -(float) Math.sin(angleFrom);
            float dy = (float) Math.cos(angleFrom);
            setPosition(target.x + dx * (animation - 3), target.y + dy * (animation - 3));

            if (animation == 0) {
                SoundManager.play(SoundManager.SOUND_WORM_KILL, false, 1.0f);
            }

            animation = (animation + 1) % 8;

            if (animation == 3) {
                target.takeDamage(target.health);
            }

            if (animation == 7) {
                state = State.WANDERING;
            }
        }
    }

    private void hunting(int tickCount) {
        scale = 1f;
        position = Position.BELOW;
        if (tickCount % 10 == 0) {
            animation = (animation + dir) % 5;
            if (animation == 4) {
                dir = -1;
            } else if (animation == 0) {
                dir = 1;
            }
        }

        Point targetGrid = new Point(Pathfinder.levelXToGrid(target.x), Pathfinder.levelYToGrid(target.y));
        Point currentEnd = current.getEnd();
        if (!targetGrid.equals(currentEnd)) {
            Point src = new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y));
            Path next = level.getPathfinder().find(src, targetGrid, true);
            if (next != null) {
                current = next;
                nextPoint = 0;
            } else {
                state = State.WANDERING;
                target = null;
                current = null;
                nextPoint = 0;
                return;
            }
        }

        walkCurrent();

        if (current == null) {
            state = State.EATING;
            animation = 0;
        }
    }

    private void wandering(int tickCount) {
        scale = 1f;
        position = Position.BELOW;
        if (tickCount % 10 == 0) {
            animation = (animation + dir) % 5;
            if (animation == 4) {
                dir = -1;
            } else if (animation == 0) {
                dir = 1;
            }
        }

        if (tickCount % (TPS * 15) == 0) {
            Iterator<Entity> closeBy = level.findClosestEntity(x, y, e -> e instanceof Unit && !(e instanceof Heli) && isEnemyOf(e));
            if (closeBy.hasNext()) {
                Entity close = closeBy.next();
                Point src = new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y));
                Point dest = new Point(Pathfinder.levelXToGrid(close.x), Pathfinder.levelYToGrid(close.y));
                Path next = level.getPathfinder().find(src, dest, true);
                if (next != null) {
                    plan.clear();
                    current = next;
                    nextPoint = 0;
                    target = (Unit) close;
                    state = State.HUNTING;
                    return;
                }
            }
        }

        int limit = 10;
        while (plan.isEmpty()) {
            int targetX = rand.nextInt(level.getWidthInTiles());
            int targetY = rand.nextInt(level.getHeightInTiles());
            float targetLevelX = Level.tileToCenterLevelX(targetX, targetY);
            float targetLevelY = Level.tileToCenterLevelY(targetX, targetY);
            int targetGridX = Pathfinder.levelXToGrid(targetLevelX);
            int targetGridY = Pathfinder.levelYToGrid(targetLevelY);
            Point src;
            if (current != null) {
                src = current.getEnd();
            } else {
                src = new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y));
            }
            Point dest = new Point(targetGridX, targetGridY);
            Path next = level.getPathfinder().find(src, dest, true);
            if (next != null) {
                plan.add(next);
            }
            limit--;
            if (limit <= 0) {
                return;
            }
        }

        if (current == null) {
            current = plan.poll();
            nextPoint = 0;
        }

        walkCurrent();
    }

    private void walkCurrent() {
        Point next = current.getPoints()[nextPoint];
        if (nextPoint == 0) {
            Point nextNext = current.getPoints()[nextPoint + 1];
            int pathAngle = next.angleToNeighbor(nextNext);
            float dx = next.getLevelX() - x;
            float dy = next.getLevelY() - y;
            float angle = (float) Math.atan2(dy, dx);
            float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
            float azimuthDeg = (float) Math.toDegrees(azimuth);
            if (Math.abs(azimuthDeg - pathAngle) >= 90) {
                nextPoint++;
                next = current.getPoints()[nextPoint];
            }
        }
        move(next.getLevelX(), next.getLevelY());
        if (Math.abs(x - next.getLevelX()) <= speed && Math.abs(y - next.getLevelY()) <= speed) {
            nextPoint++;
            if (nextPoint >= current.getPoints().length) {
                current = null;
            }
        }
    }

    public void move(float toX, float toY) {
        float dx = toX - x;
        float dy = toY - y;
        float angle = (float) Math.atan2(dy, dx);
        float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
        this.orientation = Orientation.fromAzimuth(azimuth);
        setPosition(x + (float) (speed * Math.cos(angle)), y + (float) (speed * Math.sin(angle)));
    }

    public float getSpeed() {
        return speed;
    }

    private void updateSprite() {
        int width = SpriteSheet.WORM_SHEET.getWidth();
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(orientation.ordinal() * width + (position == Position.BELOW ? width * 8 : 0) + animation);
    }

    @Override
    public boolean intersects(float fromX, float fromY, float toX, float toY) {
        return false;
    }

    @Override
    public boolean isEnemyOf(Entity other) {
        return !(other instanceof Worm);
    }

}
