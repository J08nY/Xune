package sk.neuromancer.Xune.graphics;

import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Config.TPS;

public class LevelView implements Tickable, Renderable {
    private final InputHandler input;
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
    public static final float EDGE_MARGIN_X = Tile.TILE_WIDTH * 4;
    public static final float EDGE_MARGIN_Y = Tile.TILE_HEIGHT * 8;

    public LevelView(InputHandler input, Window window, HUD hud) {
        this.input = input;

        this.screenWidth = window.getWidth();
        this.screenHeight = window.getHeight();
        this.screenCenterX = window.getCenterX();
        this.screenCenterY = window.getCenterY();
        this.hudTop = hud.getHudTop();
    }

    public void setLevel(Level level, boolean reset) {
        this.level = level;
        if (reset) {
            this.zoom = 5.0f;
            centerOn(level.getWidth() / 2, level.getHeight() / 2);
        }
    }

    @Override
    public void tick(int tickCount) {
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
        } else if (input.S.isPressed() || mouseY > screenHeight - 10) {
            moveDown();
        } else if (input.D.isPressed() || mouseX > screenWidth - 10) {
            moveRight();
        }
    }

    public void zoomIn(float speed) {
        this.zoom *= 1 + speed;
    }

    public void zoomOut(float speed) {
        if (getLevelY(0) > -EDGE_MARGIN_Y &&
                getLevelX(0) > -EDGE_MARGIN_X &&
                getLevelY(hudTop) < (level.getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2) &&
                getLevelX(screenWidth) < (level.getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2))
            this.zoom *= 1 - speed;
    }

    public void moveUp() {
        if (getLevelY(0) > -EDGE_MARGIN_Y) {
            this.yOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveDown() {
        if (getLevelY(hudTop) < (level.getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2)) {
            this.yOff -= MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveLeft() {
        if (getLevelX(0) > -EDGE_MARGIN_X) {
            this.xOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveRight() {
        if (getLevelX(screenWidth) < (level.getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2)) {
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
