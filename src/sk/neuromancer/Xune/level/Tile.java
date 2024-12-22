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
     *        / 0 \
     *       /     \
     *      /7  9  1\
     *     /         \
     *    (6 12 8 10 2)
     *     \         /
     *      \5 11  3/
     *       \     /
     *        \ 4 /
     */
    public static final boolean[] PASS_ALL =
            {true, true, true, true, true, true, true, true, true, true, true, true, true};
    public static final boolean[] PASS_NONE =
            {false, false, false, false, false, false, false, false, false, false, false, false, false};
    public static final boolean[][] PASS_START = {
            PASS_ALL, //0
            PASS_ALL, //1
            PASS_ALL  //2
    };
    public static final boolean[][] PASS_LOW = {
            //0     1      2     3     4     5      6     7     8      9    10     11   12
            {true, false, true, true, true, false, true, true, false, true, true, true, true}, //3
            {true, true, true, false, true, false, true, true, false, true, true, true, true}, //4
            {true, true, true, false, true, true, true, false, false, true, true, true, true}, //5
            {true, false, true, false, true, true, true, true, false, true, true, true, true}, //6
            PASS_ALL,                                                                          //7
            {true, true, true, true, true, false, true, false, false, true, true, true, true}, //8
            {true, true, true, false, true, true, true, false, false, true, true, true, true}, //9
            {true, false, true, true, true, true, true, false, false, true, true, true, true}, //10
            {true, false, true, true, true, false, true, true, false, true, true, true, true}, //11
            {true, true, true, false, true, false, true, true, false, true, true, true, true}, //12
            {true, false, true, true, true, true, true, false, false, true, true, true, true}, //13
            {true, false, true, false, true, true, true, true, false, false, true, true, true}, //14
            {true, true, true, true, true, false, true, false, false, true, true, true, false}, //15
            {true, true, true, true, true, true, true, true, false, false, false, false, false},//16
            PASS_ALL                                                   //17
    };
    public static final boolean[][] PASS_HIGH = {
            //0     1      2     3     4     5      6     7     8      9    10     11   12
            {true, false, true, true, true, false, true, true, false, true, true, true, true}, //18
            {true, true, true, false, true, false, true, true, false, true, true, true, true}, //19
            {true, true, true, false, true, true, true, false, false, true, true, true, true}, //20
            {true, false, true, false, true, true, true, true, false, true, true, false, true}, //21
            PASS_ALL,                                                                          //22
            {true, true, true, true, true, false, true, false, false, true, true, false, true}, //23
            {true, true, true, false, true, true, true, false, false, true, true, false, false},//24
            {true, false, true, true, true, true, true, false, false, true, false, false, false}, //25
            {true, false, true, true, true, false, true, true, false, true, false, false, true}, //26
            {true, true, true, false, true, false, true, true, false, true, true, false, true}, //27
            {true, false, true, true, true, true, true, false, false, true, true, true, true}, //28
            {true, false, true, false, true, true, true, true, false, false, true, true, true}, //29
            {true, true, true, true, true, false, true, false, false, true, true, true, false}, //30
            {true, true, true, true, true, true, true, true, false, false, false, false, false},//31
            PASS_ALL                                                                            //32
    };

    public static final boolean[][] PASS_OPEN = {
            //0     1      2     3     4     5      6     7     8      9    10     11   12
            {true, false, true, true, true, false, true, true, true, true, true, true, true}, //33
            {true, true, true, false, true, false, true, true, true, true, true, true, true}, //34
            {true, true, true, false, true, true, true, false, true, true, true, true, true}, //35
            {true, false, true, false, true, true, true, true, true, true, true, true, true}, //36
            PASS_ALL,                                                                         //37
            {true, true, true, true, true, false, true, false, true, true, true, true, true}, //38
            {true, true, true, false, true, true, true, false, true, true, true, true, true}, //39
            {true, false, false, true, true, true, false, false, true, true, false, true, false}, //40
            {true, false, true, true, true, false, true, true, true, true, true, true, true}, //41
            {true, true, true, false, true, false, true, true, true, true, true, true, true}, //42
            {true, false, true, true, true, true, true, false, true, true, true, true, true}, //43
            {true, false, true, false, true, true, true, true, true, true, true, true, true}, //44
            {true, true, true, true, true, false, true, false, true, true, true, true, true}, //45
            {true, true, true, true, true, true, true, true, false,  true, true, true, true}, //46
            PASS_ALL                                                                          //47
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
        } else if (type < 18) {
            this.pass = PASS_LOW[type - 3];
        } else if (type < 33) {
            this.pass = PASS_HIGH[type - 18];
        } else if (type < 48) {
            this.pass = PASS_OPEN[type - 33];
        } else {
            throw new IllegalStateException("Invalid tile type: " + type);
        }
    }

    public boolean[] getPassable() {
        return this.pass;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(px, py, 0.0f);
        SpriteSheet.TILE_SHEET.getSprite(type).render();
        //SpriteSheet.TILE_SHEET.getSprite(2, 15).render();
        glPopMatrix();
    }
}
