package sk.neuromancer.Xune.level;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Tile implements Renderable {
    public int type;
    private int x;    //GRID coordinates
    private int y;

    private float px;    //POINT coordinates
    private float py;
    private boolean[] pass;

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
