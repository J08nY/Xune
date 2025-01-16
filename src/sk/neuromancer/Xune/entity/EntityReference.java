package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.level.Level;

public class EntityReference {
    private final long id;
    private final Level level;
    private Entity resolved;

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
        return level.getEntity(id);
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
}
