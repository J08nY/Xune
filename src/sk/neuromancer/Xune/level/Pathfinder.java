package sk.neuromancer.Xune.level;


import sk.neuromancer.Xune.game.Tickable;

public class Pathfinder implements Tickable {
    private final Level l;
    private boolean[][] grid;
    private int gridWidth;
    private int gridHeight;

    public Pathfinder(Level l) {
        this.l = l;
        this.setupGrid();
    }

    public Path find(Point src, Point dest) {
        return null;

    }

    @Override
    public void tick(int tickCount) {

    }

    private void setupNewGrid() {
        Tile[][] tiles = this.l.getTiles();

        for (int x = 0; x < this.l.getWidthInTiles(); x++) {

            for (int y = 0; y < this.l.getHeightInTiles(); y++) {
                Tile t = tiles[x][y];
                boolean[] points = t.getPassable();//9
				
				
				/*
				int[] passMap = new int[13];
				passMap[0] = passMap[2] = passMap[10] = passMap[12] = -1; // netreba mi
				passMap[1] = points[0] ? 1 : 0;
				passMap[4] = points[1] ? 1 : 0;
				passMap[7] = points[2] ? 1 : 0;
				passMap[9] = points[3] ? 1 : 0;
				passMap[11] = points[4] ? 1 : 0;
				passMap[8] = points[5] ? 1 : 0;
				passMap[5] = points[6] ? 1 : 0;
				passMap[3] = points[7] ? 1 : 0;
				passMap[6] = points[8] ? 1 : 0;
			*/
            }
        }
    }


    private void setupGrid() {
        Tile[][] tiles = this.l.getTiles();
        //System.out.println(tiles.length);//26
        //System.out.println(tiles[0].length);//70

        this.gridWidth = (this.l.getWidthInTiles() + 1) * 2; //maxWIdth.... -1 je neparna sirka..
        this.gridHeight = this.l.getHeightInTiles() + 2;//maxHeight... -1 je zase neparna sirka..
        int[][] tempGrid = new int[this.gridWidth][this.gridHeight];
        for (int i = 0; i < this.gridWidth; i++) {
            for (int j = 0; j < this.gridHeight; j++) {
                tempGrid[i][j] = -1;
            }
        }


        for (int x = 0; x < this.l.getWidthInTiles(); x++) {
            for (int y = 0; y < this.l.getHeightInTiles(); y++) {
                /* INITIATE GRID FU!! */

                Tile t = tiles[x][y];
                boolean[] points = t.getPassable();//9
                int[] passMap = new int[13];
                passMap[0] = passMap[2] = passMap[10] = passMap[12] = -1; // netreba mi
                passMap[1] = points[0] ? 1 : 0;
                passMap[4] = points[1] ? 1 : 0;
                passMap[7] = points[2] ? 1 : 0;
                passMap[9] = points[3] ? 1 : 0;
                passMap[11] = points[4] ? 1 : 0;
                passMap[8] = points[5] ? 1 : 0;
                passMap[5] = points[6] ? 1 : 0;
                passMap[3] = points[7] ? 1 : 0;
                passMap[6] = points[8] ? 1 : 0;

                for (int i = 0; i < passMap.length; i++) {
                    //potrebujem grid X a grid Y pre kazde i
                    if (i % 5 <= 3) {
                        int gX = i % 5;
                        int gY = i / 5;
                        if (tempGrid[x * 3 + gX][y * 5 + gY] == -1) {
                            tempGrid[x * 3 + gX][y * 5 + gY] = passMap[i];
                        } else if (tempGrid[x * 3 + gX][y * 5 + gY] == 1) {
                            if (passMap[i] != -1) {
                                tempGrid[x * 3 + gX][y * 5 + gY] = passMap[i];
                            }
                        }
                    } else {
                        //gX = ;
                        //gY =
                    }

                }

				/*
				for(int i=0;i<5;i++){
					if(i % 2 == 0){
						//3

						if(tempGrid[x*3][y*5+i] == -1){
							tempGrid[x*3][y*5+i] = passMap[i*3];
						}else if(tempGrid[x*3][y*5+i] == 1){
							if(passMap[i*3] != -1){
								tempGrid[x*3][y*5+i] = passMap[i*3];
							}
						}

						if(tempGrid[x*3 + 1][y*5+i] == -1){
							tempGrid[x*3 + 1][y*5+i] = passMap[i*3 + 1];
						}else if(tempGrid[x*3 + 1][y*5+i] == 1){
							if(passMap[i*3 + 1] != -1){
								tempGrid[x*3 + 1][y*5+i] = passMap[i*3 + 1];
							}
						}

						if(tempGrid[x*3 + 2][y*5+i] == -1){
							tempGrid[x*3 + 2][y*5+i] = passMap[i*3 + 2];
						}else if(tempGrid[x*3 + 2][y*5+i] == 1){
							if(passMap[i*3 + 2] != -1){
								tempGrid[x*3 + 2][y*5+i] = passMap[i*3 + 2];
							}
						}

					}else{
						//2
						if(tempGrid[x*2][y*5+i] == -1){
							tempGrid[x*2][y*5+i] = passMap[i*2];
						}else if(tempGrid[x*2][y*5+i] == 1){
							if(passMap[i*2] != -1){
								tempGrid[x*2][y*5+i] = passMap[i*2];
							}
						}

						if(tempGrid[x*2 + 1][y*5+i] == -1){
							tempGrid[x*2 + 1][y*5+i] = passMap[i*2 + 1];
						}else if(tempGrid[x*2 + 1][y*5+i] == 1){
							if(passMap[i*2 + 1] != -1){
								tempGrid[x*2 + 1][y*5+i] = passMap[i*2 + 1];
							}
						}
					}

				}*/

                /* FATALITY!!!! */
            }
        }

    }

    public static class Point {
        public int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Path {
        private final Point[] p;

        public Path(Point[] p) {
            this.p = p;
        }
    }

}
