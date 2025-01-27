package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.elements.Sprite;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.graphics.elements.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class Tile implements Renderable {
    public int type;
    private final int x;
    private final int y;
    private final float levelX;
    private final float levelY;
    private final float centerX;
    private final float centerY;

    private final int[] pass;
    private final boolean[] solid;
    private int spice;

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
    public static final boolean[] ALL =
            {true, true, true, true, true, true, true, true, true, true, true, true, true};
    public static final boolean[] NONE =
            {false, false, false, false, false, false, false, false, false, false, false, false, false};
    public static final boolean[] EAST_WEST =
            {false, false, true, false, false, false, true, false, false, false, false, false, false};
    public static final boolean[] CORNERS =
            {true, false, true, false, true, false, true, false, false, false, false, false, false};
    public static final int TILE_WIDTH = 24;
    public static final int TILE_HEIGHT = 11;
    public static final float TILE_CENTER_X = (float) TILE_WIDTH / 2;
    public static final float TILE_CENTER_Y = (float) TILE_HEIGHT / 2;

    public static final Map<Integer, Integer> COLOR_TO_PASS = Map.of(
            /*
             * #99e550
             *          * * *
             *          *   *
             *          * * *
             */
            0x99e550, Orientation.getBits(Orientation.NORTH, Orientation.NORTHEAST, Orientation.EAST, Orientation.SOUTHEAST, Orientation.SOUTH, Orientation.SOUTHWEST, Orientation.WEST, Orientation.NORTHWEST),
            /*
             * #ac3232
             *          o o o
             *          o   o
             *          o o o
             */
            0xac3232, 0,
            /*
             * #d77bba
             *          * * *
             *          *
             *          *
             */
            0xd77bba, Orientation.getBits(Orientation.NORTH, Orientation.NORTHEAST, Orientation.SOUTHWEST, Orientation.WEST, Orientation.NORTHWEST),
            /*
             * #76428a
             *              *
             *              *
             *          * * *
             */
            0x76428a, Orientation.getBits(Orientation.NORTHEAST, Orientation.EAST, Orientation.SOUTHEAST, Orientation.SOUTH, Orientation.SOUTHWEST),
            /*
             * #d95763
             *          * * *
             *              *
             *              *
             */
            0xd95763, Orientation.getBits(Orientation.NORTHWEST, Orientation.NORTH, Orientation.NORTHEAST, Orientation.EAST, Orientation.SOUTHEAST),
            /*
             * #8f974a
             *          *
             *          *
             *          * * *
             */
            0x8f974a, Orientation.getBits(Orientation.SOUTHEAST, Orientation.SOUTH, Orientation.SOUTHWEST, Orientation.WEST, Orientation.NORTHWEST),
            /*
             * #5b6ee1
             *
             *
             *          * * *
             */
            0x5b6ee1, Orientation.getBits(Orientation.SOUTH, Orientation.SOUTHEAST, Orientation.SOUTHWEST),
            /*
             * #5fcde4
             *              *
             *              *
             *              *
             */
            0x5fcde4, Orientation.getBits(Orientation.EAST, Orientation.SOUTHEAST, Orientation.NORTHEAST),
            /*
             * #306082
             *          *
             *          *
             *          *
             */
            0x306082, Orientation.getBits(Orientation.WEST, Orientation.NORTHWEST, Orientation.SOUTHWEST),
            /*
             * #fbf236
             *          * * *
             *
             *
             */
            0xfbf236, Orientation.getBits(Orientation.NORTH, Orientation.NORTHEAST, Orientation.NORTHWEST)
    );
    public static final Map<Integer, Integer> PASS_TO_COLOR;

    public static final Map<Integer, Boolean> COLOR_TO_SOLID = Map.of(
            0x99e550, false,
            0xac3232, true,
            0, false
    );
    public static final Map<Boolean, Integer> SOLID_TO_COLOR;

    public static final Map<Integer, int[]> passMap;
    public static final Map<Integer, boolean[]> solidMap;

    static {
        passMap = new HashMap<>();
        solidMap = new HashMap<>();
        for (int type = 0; type < 59; type++) {
            Sprite passSprite = SpriteSheet.PASSMAP_SHEET.getSprite(type);
            int[] pass = new int[13];

            pass[0] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(2, 0, false), 0);
            pass[1] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(3, 1, false), 0);
            pass[2] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(4, 2, false), 0);
            pass[3] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(3, 3, false), 0);
            pass[4] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(2, 4, false), 0);
            pass[5] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(1, 3, false), 0);
            pass[6] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(0, 2, false), 0);
            pass[7] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(1, 1, false), 0);
            pass[8] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(2, 2, false), 0);
            pass[9] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(2, 1, false), 0);
            pass[10] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(3, 2, false), 0);
            pass[11] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(2, 3, false), 0);
            pass[12] = COLOR_TO_PASS.getOrDefault(passSprite.getPixelAt(1, 2, false), 0);
            passMap.put(type, pass);

            Sprite solidSprite = SpriteSheet.SOLIDMAP_SHEET.getSprite(type);
            boolean[] solid = new boolean[13];
            solid[0] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(2, 0, false));
            solid[1] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(3, 1, false));
            solid[2] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(4, 2, false));
            solid[3] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(3, 3, false));
            solid[4] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(2, 4, false));
            solid[5] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(1, 3, false));
            solid[6] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(0, 2, false));
            solid[7] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(1, 1, false));
            solid[8] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(2, 2, false));
            solid[9] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(2, 1, false));
            solid[10] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(3, 2, false));
            solid[11] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(2, 3, false));
            solid[12] = COLOR_TO_SOLID.get(solidSprite.getPixelAt(1, 2, false));
            solidMap.put(type, solid);
        }

        PASS_TO_COLOR = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : COLOR_TO_PASS.entrySet()) {
            PASS_TO_COLOR.put(entry.getValue(), entry.getKey());
        }

        SOLID_TO_COLOR = new HashMap<>();
        for (Map.Entry<Integer, Boolean> entry : COLOR_TO_SOLID.entrySet()) {
            SOLID_TO_COLOR.put(entry.getValue(), entry.getKey());
        }
    }


    public Tile(int type, int x, int y) {
        this.x = x;
        this.y = y;
        this.levelX = Level.tileToLevelX(x, y);
        this.levelY = Level.tileToLevelY(x, y);
        this.centerX = levelX + TILE_CENTER_X;
        this.centerY = levelY + TILE_CENTER_Y;

        this.type = type;
        this.pass = passMap.get(type);
        this.solid = solidMap.get(type);
        if (isSpicy()) {
            this.spice = 1000;
        }
    }


    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public float getLevelX() {
        return this.levelX;
    }

    public float getLevelY() {
        return this.levelY;
    }

    public float getCenterX() {
        return this.centerX;
    }

    public float getCenterY() {
        return this.centerY;
    }

    public boolean isSpicy() {
        return this.type == 1 || this.type == 2;
    }

    public int getSpice() {
        return this.spice;
    }

    public void setSpice(int amount) {
        this.spice = amount;
    }

    public void takeSpice(int amount) {
        this.spice -= amount;
    }

    public int[] getPassable() {
        return this.pass;
    }

    public boolean[] getSolid() {
        return this.solid;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(levelX, levelY, 0);
        if (isSpicy() && spice == 0) {
            if (type == 1) {
                SpriteSheet.TILE_SHEET.getSprite(0).render();
            } else if (type == 2) {
                SpriteSheet.TILE_SHEET.getSprite(7).render();
            }
        } else {
            SpriteSheet.TILE_SHEET.getSprite(type).render();
        }
        if (Config.DEBUG_TILES) {
            SpriteSheet.TILE_SHEET.getSprite(2, 15).render();
        }
        if (Config.DEBUG_TILE_GRID) {
            String s = (x + 1) + "," + (y + 1);
            glTranslatef(TILE_CENTER_X - s.length(), TILE_CENTER_Y - 1.5f, 0);
            glScalef(0.2f, 0.2f, 1);
            new Text(s, true).render();
        }
        glPopMatrix();
    }

    @Override
    public String toString() {
        return "Tile{" +
                "type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", px=" + levelX +
                ", py=" + levelY +
                '}';
    }
}
