package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Worm;
import sk.neuromancer.Xune.game.*;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.paths.Pathfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class Level implements Renderable, Tickable {
    private final Game game;

    private Player player;
    private Enemy enemy;
    private Pathfinder pathfinder;
    private List<Worm> worms;

    private Tile[][] level;
    private int width, height;

    public float zoom;

    public float xOff;
    public float yOff;

    public static final String LEVEL_1 = "newlevel.lvl";
    public static final float ZOOM_SPEED = 0.02f;
    public static final float SCROLL_SPEED = 0.04f;
    public static final float MOVE_SPEED = 5f;
    public static final float EDGE_MARGIN_X = Tile.TILE_WIDTH * 2;
    public static final float EDGE_MARGIN_Y = Tile.TILE_HEIGHT * 4;


    public Level(Game game) {
        this.game = game;
        this.worms = new LinkedList<>();
        worms.add(new Worm(tileToCenterLevelX(0, 0), tileToCenterLevelY(0, 0)));
    }

    @Override
    public void tick(int tickCount) {
        InputHandler input = this.game.getInput();

        if (input.PLUS.isPressed()) {
            zoomIn();
        } else if (input.MINUS.isPressed()) {
            zoomOut();
        }

        float dy = input.scroller.getDeltaY();
        if (dy > 0) {
            zoomIn();
        } else if (dy < 0) {
            zoomOut();
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
        } else if (input.S.isPressed() || mouseY > game.getWindow().getHeight() - 10) {
            moveDown();
        } else if (input.D.isPressed() || mouseX > game.getWindow().getWidth() - 10) {
            moveRight();
        }

        player.tick(tickCount);
        enemy.tick(tickCount);
        for (Worm worm : worms) {
            worm.tick(tickCount);
        }
        pathfinder.tick(tickCount);
    }

    public void zoomIn() {
        this.zoom *= 1 + ZOOM_SPEED;
    }

    public void zoomOut() {
        if (getLevelY(0) > -EDGE_MARGIN_Y && getLevelX(0) > -EDGE_MARGIN_X && getLevelY(game.getWindow().getHeight()) < (getHeight() + EDGE_MARGIN_X + (float) Tile.TILE_HEIGHT / 2) && getLevelX(game.getWindow().getWidth()) < (getWidth() + EDGE_MARGIN_Y + (float) Tile.TILE_WIDTH / 2))
            this.zoom *= 1 - SCROLL_SPEED;
    }

    public void moveUp() {
        if (getLevelY(0) > -EDGE_MARGIN_Y) {
            this.yOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveDown() {
        if (getLevelY(game.getWindow().getHeight()) < (getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2)) {
            this.yOff -= MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveLeft() {
        if (getLevelX(0) > -EDGE_MARGIN_X) {
            this.xOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveRight() {
        if (getLevelX(game.getWindow().getWidth()) < (getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2)) {
            this.xOff -= MOVE_SPEED * (1 / zoom);
        }
    }

    @Override
    public void render() {
        glPushMatrix();
        float centerX = (float) game.getWindow().getWidth() / 2;
        float centerY = (float) game.getWindow().getHeight() / 2;
        glTranslatef(centerX, centerY, 0);
        glScalef(zoom, zoom, 0f);
        glTranslatef(-centerX, -centerY, 0);
        glTranslatef(xOff, yOff, 0);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Tile t = this.level[x][y];
                t.render();
            }
        }
        enemy.render();
        player.render();
        for (Worm worm : worms) {
            worm.render();
        }
        if (Config.DEBUG_PATH_GRID) {
            pathfinder.render();
        }

        glPopMatrix();
    }

    public void loadLevel(String levelName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/sk/neuromancer/Xune/lvl/" + levelName)));
            String line = br.readLine();
            this.width = Integer.parseInt(line.split("x")[0]);
            this.height = Integer.parseInt(line.split("x")[1]);
            line = br.readLine();

            List<String> lines = new LinkedList<>();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            br.close();

            this.level = new Tile[this.width][this.height];

            for (int i = 0; i < lines.size(); i++) {
                String[] row = lines.get(i).split(",");
                for (int j = 0; j < row.length; j++) {
                    this.level[j][i] = new Tile(Byte.parseByte(row[j]), j, i);
                }
            }
            this.pathfinder = new Pathfinder(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.zoom = 4.0f;
        this.xOff = Game.CENTER_X - Game.CENTER_X / this.zoom;
        this.yOff = Game.CENTER_Y - Game.CENTER_Y / this.zoom;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Enemy getEnemy() {
        return this.enemy;
    }

    public Pathfinder getPathfinder() {
        return this.pathfinder;
    }

    public void addEntity(Entity.PlayableEntity e) {
        pathfinder.addEntity(e);
    }

    public void removeEntity(Entity.PlayableEntity e) {
        pathfinder.removeEntity(e);
    }

    public Entity entityAt(float levelX, float levelY) {
        for (Worm worm : worms) {
            if (worm.intersects(levelX, levelY)) {
                return worm;
            }
        }
        for (Entity.PlayableEntity entity : player.getEntities()) {
            if (entity.intersects(levelX, levelY)) {
                return entity;
            }
        }
        for (Entity.PlayableEntity entity : enemy.getEntities()) {
            if (entity.intersects(levelX, levelY)) {
                return entity;
            }
        }
        return null;
    }

    public Tile[][] getTiles() {
        return this.level;
    }

    public Tile getTile(int row, int column) {
        return this.level[column][row];
    }

    public Tile tileAt(float levelX, float levelY) {
        int tileX = levelToTileX(levelX, levelY);
        int tileY = levelToTileY(levelX, levelY);
        if (tileX >= 0 && tileX < this.width && tileY >= 0 && tileY < this.height) {
            return this.level[tileX][tileY];
        }
        return null;
    }

    public Tile[] getNeighbors(Tile currentTile) {
        int x = currentTile.getX();
        int y = currentTile.getY();
        int[][] moves;
        if (y % 2 == 0) {
            moves = new int[][]{
                    {-1, 0},
                    {0, -1},
                    {1, 0},
                    {0, 1},
                    {0, -2},
                    {0, 2},
                    {-1, 1}, // only if y%2 = 0
                    {-1, -1}, // only if y%2 = 0
            };
        } else {
            moves = new int[][]{
                    {-1, 0},
                    {0, -1},
                    {1, 0},
                    {0, 1},
                    {0, -2},
                    {0, 2},
                    {1, -1}, //only if y%2 = 1
                    {1, 1}  //only if y%2 = 1
            };
        }

        List<Tile> neighbors = new LinkedList<>();
        for (int[] move : moves) {
            int newX = x + move[0];
            int newY = y + move[1];
            if (newX >= 0 && newX < this.width && newY >= 0 && newY < this.height) {
                neighbors.add(this.level[newX][newY]);
            }
        }
        return neighbors.toArray(new Tile[0]);
    }

    public Iterator<Tile> findClosestTile(Tile startTile, Predicate<Tile> condition) {
        return new TileFinder(this, startTile, condition);
    }

    public Iterator<Entity> findClosestEntity(float levelX, float levelY, Predicate<Entity> condition) {
        return new EntityFinder(this, levelX, levelY, condition);
    }

    public float getWidth() {
        return this.width * Tile.TILE_WIDTH;
    }

    public float getHeight() {
        return (float) (this.height * Tile.TILE_HEIGHT) / 2;
    }

    public int getWidthInTiles() {
        return this.width;
    }

    public int getHeightInTiles() {
        return this.height;
    }

    public float getLevelX(double screenPointX) {
        return (((float) screenPointX - Game.CENTER_X) / this.zoom) - this.xOff + Game.CENTER_X;
    }

    public float getScreenX(float levelX) {
        return (levelX + this.xOff - Game.CENTER_X) * this.zoom + Game.CENTER_X;
    }

    public float getLevelY(double screenPointY) {
        return (((float) screenPointY - Game.CENTER_Y) / this.zoom) - this.yOff + Game.CENTER_Y;
    }

    public float getScreenY(float levelY) {
        return (levelY + this.yOff - Game.CENTER_Y) * this.zoom + Game.CENTER_Y;
    }

    public static float tileToLevelX(int tileX, int tileY) {
        return (tileX + 0.5f * (tileY % 2)) * Tile.TILE_WIDTH;
    }

    public static float tileToCenterLevelX(int tileX, int tileY) {
        return tileToLevelX(tileX, tileY) + Tile.TILE_CENTER_X;
    }

    public static int levelToTileX(float levelX, float levelY) {
        int tileY = levelToTileY(levelX, levelY);
        if (tileY % 2 == 0) {
            return Math.round((levelX - Tile.TILE_CENTER_X) / Tile.TILE_WIDTH);
        } else {
            return Math.round((levelX - 2 * Tile.TILE_CENTER_X) / Tile.TILE_WIDTH);
        }
    }

    public static float tileToLevelY(int tileX, int tileY) {
        return 0.5f * tileY * (Tile.TILE_HEIGHT + 1);
    }

    public static float tileToCenterLevelY(int tileX, int tileY) {
        return tileToLevelY(tileX, tileY) + Tile.TILE_CENTER_Y;
    }

    public static int levelToTileY(float levelX, float levelY) {
        return Math.round((levelY - Tile.TILE_CENTER_Y) / (0.5f * (Tile.TILE_HEIGHT + 1)));
    }

    public static class TileFinder implements Iterator<Tile> {
        private final Level level;
        private final boolean[][] visited;
        private final Queue<Tile> queue;
        private final Predicate<Tile> condition;
        private Tile next;

        public TileFinder(Level level, Tile startTile, Predicate<Tile> condition) {
            this.level = level;
            this.visited = new boolean[level.height][level.width];
            this.queue = new LinkedList<>();
            this.queue.add(startTile);
            this.visited[startTile.getY()][startTile.getX()] = true;
            this.condition = condition;
            this.next = findOne();
        }


        private Tile findOne() {
            while (!queue.isEmpty()) {
                Tile currentTile = queue.poll();
                if (condition.test(currentTile)) {
                    return currentTile;
                }

                for (Tile neighbor : level.getNeighbors(currentTile)) {
                    if (!visited[neighbor.getY()][neighbor.getX()]) {
                        queue.add(neighbor);
                        visited[neighbor.getY()][neighbor.getX()] = true;
                    }
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Tile next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Tile result = next;
            next = findOne();
            return result;
        }
    }

    public static class EntityFinder implements Iterator<Entity> {
        private final float startX, startY;
        private final Predicate<Entity> condition;
        private final List<Entity> filtered;

        public EntityFinder(Level level, float startX, float startY, Predicate<Entity> condition) {
            this.startX = startX;
            this.startY = startY;
            this.condition = condition;

            //First merge entity lists from level
            List<Entity> entities = new LinkedList<>();
            entities.addAll(level.worms);
            entities.addAll(level.player.getEntities());
            entities.addAll(level.enemy.getEntities());
            filtered = entities.stream().filter(condition).sorted(Comparator.comparingDouble(e -> Math.hypot(e.x - startX, e.y - startY))).collect(Collectors.toList());
        }

        @Override
        public boolean hasNext() {
            return !filtered.isEmpty();
        }

        @Override
        public Entity next() {
            return filtered.removeFirst();
        }
    }
}
