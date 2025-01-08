package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.entity.Road;
import sk.neuromancer.Xune.entity.Worm;
import sk.neuromancer.Xune.game.*;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class Level implements Renderable, Tickable {
    private final Game game;

    private Human human;
    private List<Player> players;
    private Pathfinder pathfinder;
    private List<Worm> worms;
    private List<Road> roads;
    private List<Effect> effects;

    private Tile[][] level;
    private int width, height;
    private List<Tile> spawns;

    private final int screenWidth, screenHeight;
    private final float screenCenterX, screenCenterY;

    public float zoom;
    public float xOff;
    public float yOff;

    public static final String LEVEL_1 = "level1.lvl";
    public static final float ZOOM_SPEED = (float) 3 / TPS;
    public static final float SCROLL_SPEED = (float) 5 / TPS;
    public static final float MOVE_SPEED = (float) TPS / 4.5f;
    public static final float EDGE_MARGIN_X = Tile.TILE_WIDTH * 2;
    public static final float EDGE_MARGIN_Y_TOP = Tile.TILE_HEIGHT * 4;
    public static final float EDGE_MARGIN_Y_BOTTOM = Tile.TILE_HEIGHT * 10;


    public Level(Game game, String levelName) {
        this.game = game;
        this.players = new ArrayList<>();
        this.worms = new LinkedList<>();
        this.roads = new LinkedList<>();
        this.effects = new LinkedList<>();

        this.screenWidth = game.getWindow().getWidth();
        this.screenHeight = game.getWindow().getHeight();
        this.screenCenterX = game.getWindow().getCenterX();
        this.screenCenterY = game.getWindow().getCenterY();
        loadLevel(levelName);
    }

    @Override
    public void tick(int tickCount) {
        InputHandler input = this.game.getInput();

        if (input.PLUS.isPressed()) {
            zoomIn(ZOOM_SPEED);
        } else if (input.MINUS.isPressed()) {
            zoomOut(ZOOM_SPEED);
        }

        float dy = input.scroller.getDeltaY();
        if (dy > 0) {
            zoomIn(SCROLL_SPEED);
        } else if (dy < 0) {
            zoomOut(SCROLL_SPEED);
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

        for (Player player : players) {
            player.tick(tickCount);
        }
        for (Worm worm : worms) {
            worm.tick(tickCount);
        }
        List<Worm> toRemove = worms.stream().filter(Worm::isDead).toList();
        for (Worm dead : toRemove) {
            SoundManager.play(Entity.getDeathSound(dead.getClass()), false, 1.0f);
            worms.remove(dead);
        }
        for (Road road : roads) {
            road.tick(tickCount);
        }
        for (Effect e : effects) {
            e.tick(tickCount);
        }
        pathfinder.tick(tickCount);
        effects.removeIf(Effect::isFinished);
    }


    public void zoomIn(float speed) {
        this.zoom *= 1 + speed;
    }

    public void zoomOut(float speed) {
        if (getLevelY(0) > -EDGE_MARGIN_Y_TOP && getLevelX(0) > -EDGE_MARGIN_X && getLevelY(game.getWindow().getHeight()) < (getHeight() + EDGE_MARGIN_X + (float) Tile.TILE_HEIGHT / 2) && getLevelX(game.getWindow().getWidth()) < (getWidth() + EDGE_MARGIN_Y_BOTTOM + (float) Tile.TILE_WIDTH / 2))
            this.zoom *= 1 - speed;
    }

    public void moveUp() {
        if (getLevelY(0) > -EDGE_MARGIN_Y_TOP) {
            this.yOff += MOVE_SPEED * (1 / zoom);
        }
    }

    public void moveDown() {
        if (getLevelY(game.getWindow().getHeight()) < (getHeight() + EDGE_MARGIN_Y_BOTTOM + (float) Tile.TILE_HEIGHT / 2)) {
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
        glScalef(zoom, zoom, 1f);
        glTranslatef(-centerX, -centerY, 0);
        glTranslatef(xOff, yOff, 0);

        boolean[][] visible = human.getVisible();
        boolean[][] discovered = human.getDiscovered();
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                if (discovered[x][y]) {
                    Tile t = this.level[x][y];
                    float screenX = getScreenX(t.getLevelX());
                    float screenY = getScreenY(t.getLevelY());
                    if (screenX + Tile.TILE_WIDTH * zoom < 0 || screenX > screenWidth ||
                            screenY + Tile.TILE_HEIGHT * zoom < 0 || screenY > screenHeight) {
                        continue;
                    }
                    t.render();
                }
            }
        }
        for (Road road : roads) {
            road.render();
        }
        for (Player player : players) {
            player.render();
        }
        for (Worm worm : worms) {
            if (human.isTileVisible(tileAt(worm))) {
                worm.render();
            }
        }
        for (Effect e : effects) {
            if (human.isTileVisible(tileAt(e))) {
                e.render();
            }
        }
        if (Config.DEBUG_PATH_GRID_LEVEL || Config.DEBUG_PATH_GRID_SOLID || Config.DEBUG_PATH_GRID_BUILDING || Config.DEBUG_PATH_GRID_ENTITY) {
            pathfinder.render();
        }

        glPushMatrix();
        glTranslatef(0, 0, 0.99f);
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                if (discovered[x][y] && !visible[x][y]) {
                    glColor4f(1, 1, 1, 0.5f);
                    new Tile(50, x, y).render();
                    glColor4f(1, 1, 1, 1);
                }
            }
        }
        glPopMatrix();
        glPopMatrix();
    }

    private void loadLevel(String levelName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/sk/neuromancer/Xune/lvl/" + levelName)));
            String line = br.readLine();
            String[] dimensions = line.split("x");
            this.width = Integer.parseInt(dimensions[0]);
            this.height = Integer.parseInt(dimensions[1]);
            line = br.readLine();

            List<String> lines = new LinkedList<>();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            br.close();

            this.level = new Tile[this.width][this.height];
            this.spawns = new LinkedList<>();

            int i = 0;
            for (; i < this.height; i++) {
                String[] row = lines.get(i).split(",");
                for (int j = 0; j < this.width; j++) {
                    String entry = row[j].strip();
                    this.level[j][i] = new Tile(Byte.parseByte(entry), j, i);
                }
            }
            if (i < lines.size() - 1) {
                for (; i < lines.size(); i++) {
                    String entry = lines.get(i).strip();
                    if (entry.startsWith("spawn")) {
                        String[] parts = entry.substring(6).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        Tile spawn = new Tile(48, x, y);
                        this.level[x][y] = spawn;
                        this.spawns.add(spawn);
                    } else if (entry.startsWith("worm")) {
                        String[] parts = entry.substring(5).split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        LevelPoint point = new TilePoint(x, y).toLevelPoint();
                        this.worms.add(new Worm(this, point.x, point.y));
                    }
                }
            }

            this.pathfinder = new Pathfinder(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.zoom = 5.0f;
        this.xOff = screenCenterX - (screenCenterX / this.zoom);
        this.yOff = screenCenterY - (screenCenterY / this.zoom);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        if (player instanceof Human h) {
            this.human = h;
        }
    }

    public Human getHuman() {
        return this.human;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public Tile spawnOf(Player player) {
        return this.spawns.get(this.players.indexOf(player));
    }

    public Pathfinder getPathfinder() {
        return this.pathfinder;
    }

    public List<Entity> getEntities() {
        int size = worms.size() + human.getEntities().size();
        for (Player player : players) {
            size += player.getEntities().size();
        }
        List<Entity> entities = new ArrayList<>(size);
        entities.addAll(worms);
        for (Player player : players) {
            entities.addAll(player.getEntities());
        }
        return entities;
    }

    public void addEntity(Entity e) {
        pathfinder.addEntity(e);
    }

    public void removeEntity(Entity e) {
        pathfinder.removeEntity(e);
    }

    public Entity entityAt(float levelX, float levelY) {
        for (Worm worm : worms) {
            if (worm.intersects(levelX, levelY)) {
                return worm;
            }
        }
        for (Player player : players) {
            for (Entity.PlayableEntity entity : player.getEntities()) {
                if (entity.intersects(levelX, levelY)) {
                    return entity;
                }
            }
        }
        return null;
    }

    public void addEffect(Effect effect) {
        this.effects.add(effect);
    }

    public Tile[][] getTiles() {
        return this.level;
    }

    public Tile getTile(int column, int row) {
        return this.level[column][row];
    }

    public Tile tileAt(Effect effect) {
        return tileAt(effect.getX(), effect.getY());
    }

    public Tile tileAt(Entity entity) {
        return tileAt(entity.x, entity.y);
    }

    public Tile tileAt(float levelX, float levelY) {
        int tileX = levelToTileX(levelX, levelY);
        int tileY = levelToTileY(levelX, levelY);
        if (tileX >= 0 && tileX < this.width && tileY >= 0 && tileY < this.height) {
            return this.level[tileX][tileY];
        }
        return null;
    }

    public boolean isTileClear(Tile tile) {
        return pathfinder.isTileClear(tile.getX(), tile.getY());
    }

    public boolean isTileClear(int tileX, int tileY) {
        return pathfinder.isTileClear(tileX, tileY);
    }

    public boolean isTileBuildable(Tile tile) {
        return pathfinder.isTileBuildable(tile.getX(), tile.getY());
    }

    public boolean isTileBuildable(int tileX, int tileY) {
        return pathfinder.isTileBuildable(tileX, tileY);
    }

    public boolean isTileBuildable(int tileX, int tileY, boolean[] footprint) {
        return pathfinder.isTileBuildable(tileX, tileY, footprint);
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
        return (((float) screenPointX - this.screenCenterX) / this.zoom) - this.xOff + this.screenCenterX;
    }

    public float getScreenX(float levelX) {
        return (levelX + this.xOff - this.screenCenterX) * this.zoom + this.screenCenterX;
    }

    public float getLevelY(double screenPointY) {
        return (((float) screenPointY - this.screenCenterY) / this.zoom) - this.yOff + this.screenCenterY;
    }

    public float getScreenY(float levelY) {
        return (levelY + this.yOff - this.screenCenterY) * this.zoom + this.screenCenterY;
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
            Tile result = null;
            while (!queue.isEmpty()) {
                Tile currentTile = queue.poll();
                if (condition.test(currentTile)) {
                    result = currentTile;
                }

                for (Tile neighbor : level.getNeighbors(currentTile)) {
                    if (!visited[neighbor.getY()][neighbor.getX()]) {
                        queue.add(neighbor);
                        visited[neighbor.getY()][neighbor.getX()] = true;
                    }
                }
                if (result != null) {
                    return result;
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

            List<Entity> entities = level.getEntities();
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

    public record TilePoint(int x, int y) {
        public LevelPoint toLevelPoint() {
            return new LevelPoint(tileToLevelX(x, y), tileToLevelY(x, y));
        }

        public LevelPoint toCenterLevelPoint() {
            return new LevelPoint(tileToCenterLevelX(x, y), tileToCenterLevelY(x, y));
        }
    }

    public record LevelPoint(float x, float y) {
        public TilePoint toTilePoint() {
            return new TilePoint(levelToTileX(x, y), levelToTileY(x, y));
        }
    }
}
