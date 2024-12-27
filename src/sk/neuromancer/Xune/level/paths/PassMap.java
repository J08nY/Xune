package sk.neuromancer.Xune.level.paths;

import static sk.neuromancer.Xune.level.Tile.PASS_ALL;
import static sk.neuromancer.Xune.level.Tile.PASS_NONE;

public class PassMap {
    private final boolean[][] pass;
    private final boolean[][] set;

    public PassMap(int widthInTiles, int heightInTiles) {
        this.pass = new boolean[5 + (heightInTiles - 1) * 2][5 + (widthInTiles - 1) * 4 + 2];
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
        boolean[] currentTile = getTile(pass, col, row);
        boolean[] setTile = getTile(set, col, row);
        boolean[] corrected = passable.clone();
        for (int i = 0; i < passable.length; i++) {
            if (setTile[i]) {
                corrected[i] &= currentTile[i];
            }
        }
        fillTile(pass, col, row, corrected);
        fillTile(set, col, row, PASS_ALL);
    }

    public void resetTile(int col, int row) {
        fillTile(pass, col, row, PASS_NONE);
        fillTile(set, col, row, PASS_NONE);
    }

    public void set(Point p) {
        set(p.x, p.y);
    }

    public void reset(Point p) {
        reset(p.x, p.y);
    }

    public void set(int col, int row) {
        pass[row][col] = true;
        set[row][col] = true;
    }

    public void reset(int col, int row) {
        pass[row][col] = false;
        set[row][col] = false;
    }

    public void setAll() {
        for (int i = 0; i < pass.length; i++) {
            for (int j = 0; j < pass[i].length; j++) {
                pass[i][j] = true;
                set[i][j] = true;
            }
        }
    }

    public void resetAll() {
        for (int i = 0; i < pass.length; i++) {
            for (int j = 0; j < pass[i].length; j++) {
                pass[i][j] = false;
                set[i][j] = false;
            }
        }
    }

    public boolean[][] getPassMap() {
        return pass;
    }

    public boolean[][] getSetMap() {
        return set;
    }

    public boolean isPassable(Point p) {
        if (p.y < 0 || p.y >= pass.length || p.x < 0 || p.x >= pass[0].length) {
            return false;
        }
        return pass[p.y][p.x];
    }
}
