package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.paths.Path;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.LinkedList;
import java.util.Queue;

public class Worm extends Entity {
    private final Level level;
    private int animation = 0;
    private Queue<Path> plan = new LinkedList<>();
    private Path current;
    private int nextPoint;
    private float speed = 0.5f;

    static {
        setMaxHealth(Worm.class, 1000);
        setSight(Worm.class, 30);
        setDeathSound(Worm.class, SoundManager.SOUND_WORM_DEATH);
    }

    public Worm(Level level, float x, float y) {
        super(x, y);
        this.level = level;
        this.orientation = Orientation.SOUTH;
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(animation);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight(), true));
    }

    @Override
    public void tick(int tickCount) {
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
            Path next = level.getPathfinder().find(src, dest);
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

        Point next = current.getPoints()[nextPoint];
        move(next.getLevelX(), next.getLevelY());
        if (Math.abs(x - next.getLevelX()) <= speed && Math.abs(y - next.getLevelY()) <= speed) {
            nextPoint++;
            if (nextPoint >= current.getPoints().length) {
                current = null;
            }
        }

        if (tickCount % 10 == 0) {
            animation = (animation + 1) % 3;
        }
        updateSprite();
    }

    public void move(float toX, float toY) {
        float dx = toX - x;
        float dy = toY - y;
        float angle = (float) Math.atan2(dy, dx);
        float azimuth = (float) ((angle < 0 ? angle + 2 * (float) Math.PI : angle) + (Math.PI / 2));
        this.orientation = Orientation.fromAngle(azimuth);
        setPosition(x + (float) (speed * Math.cos(angle)), y + (float) (speed * Math.sin(angle)));
    }

    private void updateSprite() {
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(orientation.ordinal() * SpriteSheet.WORM_SHEET.getWidth() + animation);
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
