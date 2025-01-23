package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Worm;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.game.players.Bot;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.Effect;
import sk.neuromancer.Xune.graphics.LevelView;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.proto.LevelProto;
import sk.neuromancer.Xune.proto.PlayerProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class Level implements Renderable, Tickable {
    private LevelView view;
    private Human human;
    private Map<Long, Player> players;
    private Pathfinder pathfinder;
    private Map<Long, Entity> entities;
    private List<Worm> worms;
    private List<Effect> effects;

    private Tile[][] level;
    private int width, height;
    private List<Tile> spawns;

    public static final String LEVEL_1 = "level1.lvl";

    public Level(String levelName) {
        this.players = new TreeMap<>();
        this.entities = new TreeMap<>();
        this.worms = new LinkedList<>();
        this.effects = new LinkedList<>();
        loadLevel(levelName);
    }

    public Level(Game game, LevelProto.FullLevelState savedState) {
        this.width = savedState.getWidth();
        this.height = savedState.getHeight();
        this.level = new Tile[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int type = savedState.getTiles(x * this.height + y);
                this.level[x][y] = new Tile(type, x, y);
            }
        }
        for (LevelProto.SpiceEntry entry : savedState.getTransient().getSpiceMap().getEntriesList()) {
            BaseProto.Tile tile = entry.getKey();
            this.level[tile.getX()][tile.getY()].setSpice(entry.getValue());
        }
        this.spawns = new LinkedList<>();
        for (BaseProto.Tile spawn : savedState.getSpawnsList()) {
            Tile tile = new Tile(48, spawn.getX(), spawn.getY());
            this.level[tile.getX()][tile.getY()] = tile;
            this.spawns.add(tile);
        }
        this.pathfinder = new Pathfinder(this);
        this.entities = new TreeMap<>();

        this.players = new TreeMap<>();
        for (PlayerProto.PlayerState playerState : savedState.getTransient().getPlayersList()) {
            PlayerProto.PlayerClass playerClass = playerState.getPlayerClass();
            Player player = null;
            switch (playerClass) {
                case HUMAN -> player = new Human(this, playerState);
                case BOT_ARMY_GENERAL -> player = new Bot.ArmyGeneral(this, playerState);
                case BOT_BUGGY_BOY -> player = new Bot.BuggyBoy(this, playerState);
                case BOT_HELI_MASTER -> player = new Bot.HeliMaster(this, playerState);
                case BOT_JACK_OF_ALL_TRADES -> player = new Bot.JackOfAllTrades(this, playerState);
                case BOT_ECON_GRADUATE -> player = new Bot.EconGraduate(this, playerState);
                case REMOTE -> player = null; //TODO
            }
            if (player == null) {
                continue;
            }
            addPlayer(player);
        }

        this.worms = new LinkedList<>();
        for (EntityStateProto.WormState wormState : savedState.getTransient().getWormsList()) {
            Worm worm = new Worm(this, wormState);
            this.worms.add(worm);
            addEntity(worm);
        }
        this.effects = new LinkedList<>();
    }

    @Override
    public void tick(int tickCount) {
        for (Player player : players.values()) {
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
        for (Effect e : effects) {
            e.tick(tickCount);
        }
        pathfinder.tick(tickCount);
        effects.removeIf(Effect::isFinished);
    }

    @Override
    public void render() {
        boolean[][] visible = human == null ? null : human.getVisible();
        boolean[][] discovered = human == null ? null : human.getDiscovered();
        int minX, maxX, minY, maxY;
        if (view != null) {
            float left = view.getLevelX(0);
            float right = view.getLevelX(view.getScreenWidth());
            float top = view.getLevelY(0);
            float bottom = view.getLevelY(view.getScreenHeight());
            minX = levelToTileX(left, top) - 1;
            maxX = levelToTileX(right, bottom) + 1;
            minY = levelToTileY(left, top) - 1;
            maxY = levelToTileY(right, bottom) + 1;
        } else {
            minX = 0;
            maxX = this.width;
            minY = 0;
            maxY = this.height;
        }

        for (int x = 0; x < this.width; x++) {
            if (x < minX || x > maxX) {
                continue;
            }
            for (int y = 0; y < this.height; y++) {
                if (y < minY || y > maxY) {
                    continue;
                }
                if (human == null || discovered[x][y]) {
                    Tile t = this.level[x][y];
                    t.render();
                }
            }
        }
        glPushMatrix();
        glScalef(1, 1, 1 / getHeight());
        for (Player player : players.values()) {
            player.render();
        }
        glPopMatrix();

        for (Worm worm : worms) {
            if (human == null || human.isTileVisible(tileAt(worm))) {
                worm.render();
            }
        }
        for (Effect e : effects) {
            if (human == null || human.isTileVisible(tileAt(e))) {
                e.render();
            }
        }
        if (Config.DEBUG_PATH_GRID_LEVEL || Config.DEBUG_PATH_GRID_SOLID || Config.DEBUG_PATH_GRID_BUILDING || Config.DEBUG_PATH_GRID_ENTITY) {
            pathfinder.render();
        }

        glPushMatrix();
        glTranslatef(0, 0, 0.99f);
        glColor4f(1, 1, 1, 0.5f);
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                if (human == null || (discovered[x][y] && !visible[x][y])) {
                    new Tile(50, x, y).render();
                }
            }
        }
        glColor4f(1, 1, 1, 1);
        for (int x = 0; x < this.width + 1; x++) {
            if (x == 0) {
                new Tile(57, x, this.height).render();
            } else if (x == this.width) {
                new Tile(55, x, -1).render();
            } else {
                new Tile(51, x, -1).render();
                new Tile(52, x, this.height).render();
            }
        }
        for (int y = 0; y < this.height; y++) {
            if (y % 2 == 0)
                new Tile(53, 0, y).render();
            if (y % 2 == 1)
                new Tile(54, this.width - 1, y).render();
        }
        glPopMatrix();
    }

    private void loadLevel(String levelName) {
        try (InputStream stream = getClass().getResourceAsStream("/sk/neuromancer/Xune/lvl/" + levelName)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
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
            this.pathfinder = new Pathfinder(this);

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
                        Worm worm = new Worm(this, point.x, point.y);
                        this.worms.add(worm);
                        addEntity(worm);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long addPlayer(Player player) {
        long id = players.size();
        this.players.put(id, player);
        if (player instanceof Human h) {
            this.human = h;
        }
        return id;
    }

    public Human getHuman() {
        return this.human;
    }

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public Tile spawnOf(Player player) {
        return this.spawns.get((int) player.getId());
    }

    public Pathfinder getPathfinder() {
        return this.pathfinder;
    }

    public void setView(LevelView view) {
        this.view = view;
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(entities.values());
    }

    public void addEntity(Entity e) {
        entities.put(e.getId(), e);
        pathfinder.addEntity(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e.getId());
        pathfinder.removeEntity(e);
    }

    public Entity getEntity(long id) {
        return entities.get(id);
    }

    public Entity entityAt(float levelX, float levelY) {
        for (Entity entity : entities.values()) {
            if (entity.intersects(levelX, levelY)) {
                return entity;
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
        // TODO: This is incorrect somehow. Can build powerplant over refinery or barracks over powerplant AND refinery.
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

    public static float tileToLevelX(int tileX, int tileY) {
        return (tileX + 0.5f * (tileY % 2)) * Tile.TILE_WIDTH;
    }

    public static float tileToCenterLevelX(int tileX, int tileY) {
        return tileToLevelX(tileX, tileY) + Tile.TILE_CENTER_X;
    }

    public static int levelToTileX(float levelX, float levelY) {
        return Math.round(levelToFullTileX(levelX, levelY));
    }

    public static float levelToFullTileX(float levelX, float levelY) {
        int tileY = levelToTileY(levelX, levelY);
        if (tileY % 2 == 0) {
            return (levelX - Tile.TILE_CENTER_X) / Tile.TILE_WIDTH;
        } else {
            return (levelX - 2 * Tile.TILE_CENTER_X) / Tile.TILE_WIDTH;
        }
    }

    public static float tileToLevelY(int tileX, int tileY) {
        return 0.5f * tileY * (Tile.TILE_HEIGHT + 1);
    }

    public static float tileToCenterLevelY(int tileX, int tileY) {
        return tileToLevelY(tileX, tileY) + Tile.TILE_CENTER_Y;
    }

    public static int levelToTileY(float levelX, float levelY) {
        return Math.round(levelToFullTileY(levelX, levelY));
    }

    public static float levelToFullTileY(float levelX, float levelY) {
        return (levelY - Tile.TILE_CENTER_Y) / (0.5f * (Tile.TILE_HEIGHT + 1));
    }

    public boolean isDone() {
        Set<Flag> flags = players.values().stream().filter(player -> !player.isEliminated()).map(Player::getFlag).collect(Collectors.toSet());
        return flags.size() <= 1;
    }

    public LevelProto.LevelState serializeTransient() {
        LevelProto.LevelState.Builder builder = LevelProto.LevelState.newBuilder();
        for (Player player : players.values()) {
            builder.addPlayers(player.serialize());
        }
        for (Worm worm : worms) {
            builder.addWorms(worm.serialize());
        }
        LevelProto.LevelState.SpiceMap.Builder spiceBuilder = LevelProto.LevelState.SpiceMap.newBuilder();
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Tile t = this.level[x][y];
                if (t.isSpicy()) {
                    BaseProto.Tile tile = BaseProto.Tile.newBuilder().setX(x).setY(y).build();
                    spiceBuilder.addEntries(LevelProto.SpiceEntry.newBuilder().setKey(tile).setValue(t.getSpice()));
                }
            }
        }
        builder.setSpiceMap(spiceBuilder.build());
        return builder.build();
    }

    public void deserializeTransient(LevelProto.LevelState state) {
        for (PlayerProto.PlayerState playerState : state.getPlayersList()) {
            long id = playerState.getId();
            Player player = players.get(id);
            player.deserialize(playerState);
        }
        for (LevelProto.SpiceEntry entry : state.getSpiceMap().getEntriesList()) {
            BaseProto.Tile tile = entry.getKey();
            this.level[tile.getX()][tile.getY()].setSpice(entry.getValue());
        }
        for (EntityStateProto.WormState wormState : state.getWormsList()) {
            long id = wormState.getEntity().getId();
            Worm worm = (Worm) getEntity(id);
            worm.deserialize(wormState, this);
        }
    }

    public LevelProto.FullLevelState serializeFull() {
        LevelProto.FullLevelState.Builder builder = LevelProto.FullLevelState.newBuilder();
        builder.setWidth(this.width);
        builder.setHeight(this.height);
        builder.setTransient(serializeTransient());
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Tile t = this.level[x][y];
                builder.addTiles(t.type);
            }
        }
        for (Tile spawn : this.spawns) {
            builder.addSpawns(BaseProto.Tile.newBuilder().setX(spawn.getX()).setY(spawn.getY()));
        }
        return builder.build();
    }

    public void deserializeFull(LevelProto.FullLevelState state) {
        deserializeTransient(state.getTransient());
        this.width = state.getWidth();
        this.height = state.getHeight();
        this.level = new Tile[this.width][this.height];
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int type = state.getTiles(x * this.height + y);
                this.level[x][y] = new Tile(type, x, y);
            }
        }
        this.spawns = new LinkedList<>();
        for (BaseProto.Tile spawn : state.getSpawnsList()) {
            Tile tile = new Tile(48, spawn.getX(), spawn.getY());
            this.level[tile.getX()][tile.getY()] = tile;
            this.spawns.add(tile);
        }
        for (LevelProto.SpiceEntry entry : state.getTransient().getSpiceMap().getEntriesList()) {
            BaseProto.Tile tile = entry.getKey();
            this.level[tile.getX()][tile.getY()].setSpice(entry.getValue());
        }
        this.pathfinder = new Pathfinder(this);
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
