package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class LevelView implements Tickable, Renderable {
    private final Game game;
    private Level level;

    private final int screenWidth, screenHeight;
    private final float screenCenterX, screenCenterY;
    private final float hudTop;

    public float zoom;
    public float xOff;
    public float yOff;

    public static final float ZOOM_SPEED = (float) 3 / TPS;
    public static final float SCROLL_SPEED = (float) 5 / TPS;
    public static final float MOVE_SPEED = (float) TPS / 4.5f;
    public static final float EDGE_MARGIN_X = Tile.TILE_WIDTH * 2;
    public static final float EDGE_MARGIN_Y = Tile.TILE_HEIGHT * 4;

    public LevelView(Game game) {
        this.game = game;

        this.screenWidth = game.getWindow().getWidth();
        this.screenHeight = game.getWindow().getHeight();
        this.screenCenterX = game.getWindow().getCenterX();
        this.screenCenterY = game.getWindow().getCenterY();
        this.hudTop = game.getHud().getHudTop();
    }

    public void setLevel(Level level) {
        this.level = level;
        this.zoom = 5.0f;
        centerOn(level.getWidth() / 2, level.getHeight() / 2);
    }

    @Override
    public void tick(int tickCount) {
        InputHandler input = this.game.getInput();

        if (input.PLUS.isPressed()) {
            zoomIn(ZOOM_SPEED);
        } else if (input.MINUS.isPressed()) {
            zoomOut(ZOOM_SPEED);
        }

        float dy = input.scroller.getDeltaY();
        if (dy > 0) {
            zoomIn(SCROLL_SPEED);
        } else if (dy < 0) {
            zoomOut(SCROLL_SPEED);
        }

        float dx = input.scroller.getDeltaX();
        if (dx > 0) {
            moveLeft();
        } else if (dx < 0) {
            moveRight();
        }

        double mouseX = input.mouse.getX();
        double mouseY = input.mouse.getY();

        if (input.W.isPressed() || mouseY < 10) {
            moveUp();
        } else if (input.A.isPressed() || mouseX < 10) {
            moveLeft();
        } else if (input.S.isPressed() || mouseY > game.getWindow().getHeight() - 10) {
            moveDown();
        } else if (input.D.isPressed() || mouseX > game.getWindow().getWidth() - 10) {
            moveRight();
        }
    }

    public void zoomIn(float speed) {
        this.zoom *= 1 + speed;
    }

    public void zoomOut(float speed) {
        if (true || getLevelY(0) > -EDGE_MARGIN_Y &&
                getLevelX(0) > -EDGE_MARGIN_X &&
                getLevelY(hudTop) < (level.getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2) &&
                getLevelX(screenWidth) < (level.getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2))
            this.zoom *= 1 - speed;
    }

    public void moveUp() {
        if (true || getLevelY(0) > -EDGE_MARGIN_Y) {
            this.yOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveDown() {
        if (true || getLevelY(hudTop) < (level.getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2)) {
            this.yOff -= MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveLeft() {
        if (true || getLevelX(0) > -EDGE_MARGIN_X) {
            this.xOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveRight() {
        if (true || getLevelX(screenWidth) < (level.getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2)) {
            this.xOff -= MOVE_SPEED * (1 / zoom);
        }
    }

    public void centerOn(float levelX, float levelY) {
        this.xOff = screenCenterX - levelX;
        this.yOff = screenCenterY - levelY;
    }

    public float getLevelX(double screenPointX) {
        return (((float) screenPointX - this.screenCenterX) / this.zoom) - this.xOff + this.screenCenterX;
    }

    public float getScreenX(float levelX) {
        return (levelX + this.xOff - this.screenCenterX) * this.zoom + this.screenCenterX;
    }

    public float getLevelY(double screenPointY) {
        return (((float) screenPointY - this.screenCenterY) / this.zoom) - this.yOff + this.screenCenterY;
    }

    public float getScreenY(float levelY) {
        return (levelY + this.yOff - this.screenCenterY) * this.zoom + this.screenCenterY;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    private void applyLevelView() {
        glTranslatef(screenCenterX, screenCenterY, 0);
        glScalef(zoom, zoom, 1f);
        glTranslatef(-screenCenterX, -screenCenterY, 0);
        glTranslatef(xOff, yOff, 0);
    }

    @Override
    public void render() {
        glPushMatrix();
        applyLevelView();
        level.render();
        glPopMatrix();
    }
}
