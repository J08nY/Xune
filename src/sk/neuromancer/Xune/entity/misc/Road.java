package sk.neuromancer.Xune.entity.misc;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.graphics.SpriteSheet;

import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public class Road extends Entity {
    public int tileX, tileY;
    private Variant variant;

    public enum Variant {
        TILE, CROSS, I_NE_SW, I_NW_SE, T_NE, T_SE, T_SW, T_NW, V_N, V_S, V_E, V_W;
    }

    public Road(int tileX, int tileY, Variant variant) {
        super(tileToCenterLevelX(tileX, tileY), tileToCenterLevelY(tileX, tileY));
        this.tileX = tileX;
        this.tileY = tileY;
        this.variant = variant;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ROW_LENGTH * 6 + variant.ordinal());
    }

    @Override
    public void tick(int tickCount) {

    }

    @Override
    public boolean isEnemyOf(Entity other) {
        return false;
    }

}
