package sk.neuromancer.Xune.level;

public class TileReference {
    private final int tileX, tileY;
    private final Level level;
    private Tile resolved;

    public TileReference(int tileX, int tileY, Level level) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.level = level;
    }

    public TileReference(Tile tile) {
        this(tile.getX(), tile.getY(), null);
        this.resolved = tile;
    }

    public int getX() {
        return tileX;
    }

    public int getY() {
        return tileY;
    }

    public Tile resolve(Level level) {
        if (resolved == null && level != null) {
            resolved = level.getTile(tileX, tileY);
        }
        return resolved;
    }

    public Tile resolve() {
        return resolve(level);
    }

    public boolean isResolved() {
        return resolved != null;
    }
}
