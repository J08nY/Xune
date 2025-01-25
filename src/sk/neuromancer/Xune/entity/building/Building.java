package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.Config;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.input.Clickable;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.EntityStateProto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public abstract class Building extends PlayableEntity {
    protected static final Map<Class<? extends Building>, Integer> powerMap = new HashMap<>();
    protected static final Map<Class<? extends Building>, List<Class<? extends Unit>>> producesMap = new HashMap<>();
    protected static final Map<Class<? extends Building>, boolean[]> passableMap = new HashMap<>();

    public int tileX, tileY;

    public Building(int tileX, int tileY, Orientation orientation, Player owner) {
        super(tileToCenterLevelX(tileX, tileY), tileToCenterLevelY(tileX, tileY), owner);
        this.tileX = tileX;
        this.tileY = tileY;
        this.orientation = orientation;
        int spriteRow = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(owner.getFlag()) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight()));
    }

    public Building(EntityStateProto.BuildingState savedState, Player owner) {
        super(savedState.getPlayable(), owner);
        this.tileX = savedState.getTilePosition().getX();
        this.tileY = savedState.getTilePosition().getY();
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(owner.getFlag()) + (orientation.ordinal() % 2 == 0 ? 1 : 0) * SpriteSheet.SPRITE_ROW_LENGTH);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight()));
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(this.x, this.y, this.y);
        if (Config.DEBUG_CLICKABLE) {
            for (Clickable clickable : clickableAreas) {
                if (clickable instanceof Renderable r) {
                    r.render();
                }
            }
        }
        glTranslatef(- (float) sprite.getWidth() / 2, - (float) sprite.getHeight() / 2, 0);
        this.sprite.render();
        if (isSelected) {
            SpriteSheet.MISC_SHEET.getSprite(1, 0).render();
            glPushMatrix();
            glTranslatef(0, sprite.getHeight(), 0);
            glBegin(GL_QUADS);
            glColor3f(0, 1, 0);
            float healthPercentage = (float) health / maxHealth;
            for (int i = 0; i < 12; i++) {
                if (i < healthPercentage * 12) {
                    glColor3f(0, 1, 0);
                } else {
                    glColor3f(1, 0, 0);
                }
                glVertex2f(((float) 25 / 12) * i, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 1);
                glVertex2f(((float) 25 / 12) * i, 1);
            }
            glEnd();
            glColor3f(1, 1, 1);
            glPopMatrix();
        }
        glPopMatrix();
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.tileX = Level.levelToTileX(x, y);
        this.tileY = Level.levelToTileY(x, y);
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "." + flag + " at (" + x + "," + y + "), [" + tileX + "," + tileY + "].";
    }

    public EntityStateProto.BuildingState serialize() {
        return EntityStateProto.BuildingState.newBuilder()
                .setPlayable(this.toPlayableEntityState())
                .setTilePosition(BaseProto.Tile.newBuilder().setX(tileX).setY(tileY).build()).build();
    }

    public void deserialize(EntityStateProto.BuildingState state) {
        fromPlayableEntityState(state.getPlayable(), owner);
        if (state.hasTilePosition()) {
            this.tileX = state.getTilePosition().getX();
            this.tileY = state.getTilePosition().getY();
        }
    }

    public int getPower() {
        return powerMap.getOrDefault(this.getClass(), 0);
    }

    public List<Class<? extends Unit>> getProduces() {
        return producesMap.getOrDefault(this.getClass(), List.of());
    }

    public boolean[] getPassable() {
        return passableMap.getOrDefault(this.getClass(), new boolean[0]);
    }

    public static int getPower(Class<? extends Building> klass) {
        return powerMap.getOrDefault(klass, 0);
    }

    public static List<Class<? extends Unit>> getProduces(Class<? extends Building> klass) {
        return producesMap.getOrDefault(klass, List.of());
    }

    public static boolean[] getPassable(Class<? extends Building> klass) {
        return passableMap.getOrDefault(klass, new boolean[0]);
    }

    protected static void setPower(Class<? extends Building> klass, int cost) {
        powerMap.put(klass, cost);
    }

    protected static void setProduces(Class<? extends Building> klass, List<Class<? extends Unit>> units) {
        producesMap.put(klass, units);
    }

    protected static void setPassable(Class<? extends Building> klass, boolean[] passable) {
        passableMap.put(klass, passable);
    }
}
