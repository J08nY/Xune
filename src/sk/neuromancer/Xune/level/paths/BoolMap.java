package sk.neuromancer.Xune.level.paths;

import static sk.neuromancer.Xune.level.Tile.ALL;
import static sk.neuromancer.Xune.level.Tile.NONE;

public class BoolMap {
    private final boolean[][] val;
    private final boolean[][] set;

    public BoolMap(int widthInTiles, int heightInTiles) {
        this.val = new boolean[5 + (heightInTiles - 1) * 2][5 + (widthInTiles - 1) * 4 + 2];
        this.set = new boolean[5 + (heightInTiles - 1) * 2][5 + (widthInTiles - 1) * 4 + 2];
    }

    private void fillTile(boolean[][] map, int col, int row, boolean[] passable) {
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
        // Fill base on passable
        map[baseY][baseX + 2] = passable[0];
        map[baseY + 1][baseX + 3] = passable[1];
        map[baseY + 2][baseX + 4] = passable[2];
        map[baseY + 3][baseX + 3] = passable[3];
        map[baseY + 4][baseX + 2] = passable[4];
        map[baseY + 3][baseX + 1] = passable[5];
        map[baseY + 2][baseX] = passable[6];
        map[baseY + 1][baseX + 1] = passable[7];
        map[baseY + 2][baseX + 2] = passable[8];
        map[baseY + 1][baseX + 2] = passable[9];
        map[baseY + 2][baseX + 3] = passable[10];
        map[baseY + 3][baseX + 2] = passable[11];
        map[baseY + 2][baseX + 1] = passable[12];
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

    public void setTile(int col, int row, boolean[] passable) {
        boolean[] currentTile = getTile(val, col, row);
        boolean[] setTile = getTile(set, col, row);
        boolean[] corrected = passable.clone();
        for (int i = 0; i < passable.length; i++) {
            if (setTile[i]) {
                corrected[i] &= currentTile[i];
            }
        }
        fillTile(val, col, row, corrected);
        fillTile(set, col, row, ALL);
    }

    public void resetTile(int col, int row) {
        fillTile(val, col, row, NONE);
        fillTile(set, col, row, NONE);
    }

    public void set(Point p) {
        set(p.x, p.y);
    }

    public void reset(Point p) {
        reset(p.x, p.y);
    }

    public void set(int col, int row) {
        val[row][col] = true;
        set[row][col] = true;
    }

    public void reset(int col, int row) {
        val[row][col] = false;
        set[row][col] = false;
    }

    public void setAll() {
        for (int i = 0; i < val.length; i++) {
            for (int j = 0; j < val[i].length; j++) {
                val[i][j] = true;
                set[i][j] = true;
            }
        }
    }

    public void resetAll() {
        for (int i = 0; i < val.length; i++) {
            for (int j = 0; j < val[i].length; j++) {
                val[i][j] = false;
                set[i][j] = false;
            }
        }
    }

    public boolean[][] getValMap() {
        return val;
    }

    public boolean[][] getSetMap() {
        return set;
    }

    public boolean isTrue(Point p) {
        if (p.y < 0 || p.y >= val.length || p.x < 0 || p.x >= val[0].length) {
            return false;
        }
        return val[p.y][p.x];
    }

    public boolean isTileTrue(int tileX, int tileY, boolean[] mask) {
        boolean[] passArray = getTile(val, tileX, tileY);
        for (int i = 0; i < passArray.length; i++) {
            if (mask[i] && !passArray[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean isTileAllTrue(int tileX, int tileY) {
        boolean[] passArray = getTile(val, tileX, tileY);
        for (boolean b : passArray) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean isTilePartiallyTrue(int tileX, int tileY) {
        boolean[] passArray = getTile(val, tileX, tileY);
        for (boolean b : passArray) {
            if (b) {
                return true;
            }
        }
        return false;
    }
}
