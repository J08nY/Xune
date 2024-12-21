package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static org.lwjgl.opengl.GL11.*;

public class Tile implements Renderable {
    public int type;
    private final int x;    //GRID coordinates
    private final int y;

    private final float px;    //POINT coordinates
    private final float py;
    private final boolean[] pass;

    public static final boolean[] PASS_ALL = {true, true, true, true, true, true, true, true, true};
    public static final boolean[] PASS_NONE = {false, false, false, false, false, false, false, false, false};
    public static final boolean[][] PASS = {
            {true, false, true, true, true, false, true, true, false},
            {true, true, true, false, true, false, true, true, false},
            {true, true, true, false, true, true, true, false, false},
            {true, false, true, false, true, true, true, true, false},
            PASS_ALL,
            {true, true, true, true, true, false, true, false, false},
            {true, true, true, false, true, true, true, false, false},
            {true, false, true, true, true, true, true, false, false},
            {true, false, true, true, true, false, true, true, false},
            {true, true, true, false, true, false, true, true, false},
            {true, false, true, true, true, true, true, false, false},
            {true, false, true, false, true, true, true, true, false},
            {true, true, true, true, true, false, true, false, false},
            {true, true, true, true, true, true, true, true, false}};

    public static final int TILE_WIDTH = 24;
    public static final int TILE_HEIGHT = 11;
    public static final float TILE_CENTER_X = (float) TILE_WIDTH / 2;
    public static final float TILE_CENTER_Y = (float) TILE_HEIGHT / 2;

    public Tile(int type, int x, int y) {
        this.x = x;
        this.y = y;
        this.px = tileX(x, y);
        this.py = tileY(x, y);

        this.type = type;
        if (type < 3) {
            this.pass = PASS_ALL;
        } else {
            int i = (type - 3) % 15;
            this.pass = PASS[i];
        }
    }

    public boolean[] getPassable() {
        return this.pass;
    }

    public boolean isPassable(int point) {
        return pass[point];
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getLevelX() {
        return px;
    }

    public float getCenterX() {
        return px + TILE_CENTER_X;
    }

    public float getLevelY() {
        return py;
    }

    public float getCenterY() {
        return py + TILE_CENTER_Y;
    }

    public static float tileX(float x, float y) {
        return (x + 0.5f * (y % 2)) * Tile.TILE_WIDTH;
    }

    public static float tileY(float x, float y) {
        return 0.5f * y * Tile.TILE_HEIGHT;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(px, py, 0.0f);
        SpriteSheet.TILE_SHEET.getSprite(type).render();
        glPopMatrix();
    }
}
