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

    /*
     *      / 0 \
     *     /7   1\
     *    (6  8  2)
     *     \5   3/
     *      \ 4 /
     */
    public static final boolean[] PASS_ALL = {true, true, true, true, true, true, true, true, true};
    public static final boolean[] PASS_NONE = {false, false, false, false, false, false, false, false, false};
    public static final boolean[][] PASS_START = {
            PASS_ALL, //0
            PASS_ALL, //1
            PASS_ALL  //2
    };
    public static final boolean[][] PASS_BASE = {
            //0     1      2     3     4     5      6     7     8
            {true, false, true, true, true, false, true, true, false}, //3,  18
            {true, true, true, false, true, false, true, true, false}, //4,  19
            {true, true, true, false, true, true, true, false, false}, //5,  20
            {true, false, true, false, true, true, true, true, false}, //6,  21
            PASS_ALL,                                                  //7,  22
            {true, true, true, true, true, false, true, false, false}, //8,  23
            {true, true, true, false, true, true, true, false, false}, //9,  24
            {true, false, true, true, true, true, true, false, false}, //10, 25
            {true, false, true, true, true, false, true, true, false}, //11, 26
            {true, true, true, false, true, false, true, true, false}, //12, 27
            {true, false, true, true, true, true, true, false, false}, //13, 28
            {true, false, true, false, true, true, true, true, false}, //14, 29
            {true, true, true, true, true, false, true, false, false}, //15, 30
            {true, true, true, true, true, true, true, true, false},   //16, 31
            PASS_ALL                                                   //17, 32
    };

    public static final boolean[][] PASS_OPEN = {
            //0     1      2     3     4     5      6     7     8
            {true, false, true, true, true, false, true, true, true}, //33
            {true, true, true, false, true, false, true, true, true}, //34
            {true, true, true, false, true, true, true, false, true}, //35
            {true, false, true, false, true, true, true, true, true}, //36
            PASS_ALL,                                                 //37
            {true, true, true, true, true, false, true, false, true}, //38
            {true, true, true, false, true, true, true, false, true}, //39
            {true, true, false, true, true, true, false, true, true}, //40
            {true, false, true, true, true, false, true, true, true}, //41
            {true, true, true, false, true, false, true, true, true}, //42
            {true, false, true, true, true, true, true, false, true}, //43
            {true, false, true, false, true, true, true, true, true}, //44
            {true, true, true, true, true, false, true, false, true}, //45
            {true, true, true, true, true, true, true, true, false}, //46
            PASS_ALL                                                  //47
    };

    public static final int TILE_WIDTH = 24;
    public static final int TILE_HEIGHT = 11;
    public static final float TILE_CENTER_X = (float) TILE_WIDTH / 2;
    public static final float TILE_CENTER_Y = (float) TILE_HEIGHT / 2;

    public Tile(int type, int x, int y) {
        this.x = x;
        this.y = y;
        this.px = Level.tileX(x, y);
        this.py = Level.tileY(x, y);

        this.type = type;
        if (type < 3) {
            this.pass = PASS_START[type];
        } else if (type < 33) {
            this.pass = PASS_BASE[(type - 3) % PASS_BASE.length];
        } else if (type < 48) {
            this.pass = PASS_OPEN[(type - 33) % PASS_OPEN.length];
        } else {
            throw new IllegalStateException("Invalid tile type: " + type);
        }
    }

    public boolean[] getPassable() {
        return this.pass;
    }

    public boolean isPassable(int point) {
        return pass[point];
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(px, py, 0.0f);
        SpriteSheet.TILE_SHEET.getSprite(type).render();
        SpriteSheet.TILE_SHEET.getSprite(2, 15).render();
        glPopMatrix();
    }
}
