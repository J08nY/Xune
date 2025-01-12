package sk.neuromancer.Xune.level.paths;

import static sk.neuromancer.Xune.level.Tile.ALL;
import static sk.neuromancer.Xune.level.Tile.NONE;

public class IntMap {
    private final int width, height;

    private final int[][] val;
    private final boolean[][] set;

    public static final int[] INONE = new int[13];

    public IntMap(int widthInTiles, int heightInTiles) {
        this.width = 5 + (widthInTiles - 1) * 4 + 2;
        this.height = 5 + (heightInTiles - 1) * 2;
        this.val = new int[height][width];
        this.set = new boolean[height][width];
    }

    private void fillTile(int[][] map, int col, int row, int[] value) {
        int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
        int baseY = row * 2;

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
        // Fill base on value
        map[baseY][baseX + 2] = value[0];
        map[baseY + 1][baseX + 3] = value[1];
        map[baseY + 2][baseX + 4] = value[2];
        map[baseY + 3][baseX + 3] = value[3];
        map[baseY + 4][baseX + 2] = value[4];
        map[baseY + 3][baseX + 1] = value[5];
        map[baseY + 2][baseX] = value[6];
        map[baseY + 1][baseX + 1] = value[7];
        map[baseY + 2][baseX + 2] = value[8];
        map[baseY + 1][baseX + 2] = value[9];
        map[baseY + 2][baseX + 3] = value[10];
        map[baseY + 3][baseX + 2] = value[11];
        map[baseY + 2][baseX + 1] = value[12];
    }

    private void fillTile(boolean[][] map, int col, int row, boolean[] value) {
        int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
        int baseY = row * 2;

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
        // Fill base on value
        map[baseY][baseX + 2] = value[0];
        map[baseY + 1][baseX + 3] = value[1];
        map[baseY + 2][baseX + 4] = value[2];
        map[baseY + 3][baseX + 3] = value[3];
        map[baseY + 4][baseX + 2] = value[4];
        map[baseY + 3][baseX + 1] = value[5];
        map[baseY + 2][baseX] = value[6];
        map[baseY + 1][baseX + 1] = value[7];
        map[baseY + 2][baseX + 2] = value[8];
        map[baseY + 1][baseX + 2] = value[9];
        map[baseY + 2][baseX + 3] = value[10];
        map[baseY + 3][baseX + 2] = value[11];
        map[baseY + 2][baseX + 1] = value[12];
    }

    public int[] getTile(int[][] map, int col, int row) {
        int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
        int baseY = row * 2;
        return new int[]{
                map[baseY][baseX + 2],
                map[baseY + 1][baseX + 3],
                map[baseY + 2][baseX + 4],
                map[baseY + 3][baseX + 3],
                map[baseY + 4][baseX + 2],
                map[baseY + 3][baseX + 1],
                map[baseY + 2][baseX],
                map[baseY + 1][baseX + 1],
                map[baseY + 2][baseX + 2],
                map[baseY + 1][baseX + 2],
                map[baseY + 2][baseX + 3],
                map[baseY + 3][baseX + 2],
                map[baseY + 2][baseX + 1]
        };
    }

    public boolean[] getTile(boolean[][] map, int col, int row) {
        int baseX = col * 4 + (row % 2 == 0 ? 0 : 2);
        int baseY = row * 2;
        return new boolean[]{
                map[baseY][baseX + 2],
                map[baseY + 1][baseX + 3],
                map[baseY + 2][baseX + 4],
                map[baseY + 3][baseX + 3],
                map[baseY + 4][baseX + 2],
                map[baseY + 3][baseX + 1],
                map[baseY + 2][baseX],
                map[baseY + 1][baseX + 1],
                map[baseY + 2][baseX + 2],
                map[baseY + 1][baseX + 2],
                map[baseY + 2][baseX + 3],
                map[baseY + 3][baseX + 2],
                map[baseY + 2][baseX + 1]
        };
    }

    public void setTile(int col, int row, int[] value) {
        int[] currentTile = getTile(val, col, row);
        boolean[] setTile = getTile(set, col, row);
        int[] corrected = value.clone();
        for (int i = 0; i < value.length; i++) {
            if (setTile[i]) {
                corrected[i] &= currentTile[i];
            }
        }
        fillTile(val, col, row, corrected);
        fillTile(set, col, row, ALL);
    }

    public void resetTile(int col, int row) {
        fillTile(val, col, row, INONE);
        fillTile(set, col, row, NONE);
    }

    public int get(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) {
            return 0x00;
        }
        return val[row][col];
    }

    public int get(Point p) {
        return get(p.x, p.y);
    }

    public void set(Point p) {
        set(p.x, p.y);
    }

    public void reset(Point p) {
        reset(p.x, p.y);
    }

    public void set(int col, int row) {
        val[row][col] = 0xff;
        set[row][col] = true;
    }

    public void reset(int col, int row) {
        val[row][col] = 0x00;
        set[row][col] = false;
    }

    public void setAll() {
        for (int i = 0; i < val.length; i++) {
            for (int j = 0; j < val[i].length; j++) {
                val[i][j] = 0xff;
                set[i][j] = true;
            }
        }
    }

    public void resetAll() {
        for (int i = 0; i < val.length; i++) {
            for (int j = 0; j < val[i].length; j++) {
                val[i][j] = 0x00;
                set[i][j] = false;
            }
        }
    }

    public int[][] getValMap() {
        return val;
    }

    public boolean[][] getSetMap() {
        return set;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
