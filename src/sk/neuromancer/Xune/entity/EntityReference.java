package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.level.Level;

public class EntityReference {
    private final long id;
    private final Level level;
    private Entity resolved = null;

    public EntityReference(long id, Level level) {
        this.id = id;
        this.level = level;
    }

    public EntityReference(Entity entity) {
        this(entity.getId(), null);
        this.resolved = entity;
    }

    public EntityReference(long id) {
        this(id, null);
    }

    public long getId() {
        return id;
    }

    public Entity resolve(Level level) {
        resolved = level.getEntity(id);
        return resolved;
    }

    public Entity resolve() {
        if (resolved == null && level != null) {
            resolved = level.getEntity(id);
        }
        return resolved;
    }

    public boolean isResolved() {
        return resolved != null;
    }

    public boolean isResolvable() {
        return level != null || resolved != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityReference) {
            return ((EntityReference) obj).id == id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
