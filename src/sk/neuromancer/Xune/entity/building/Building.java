package sk.neuromancer.Xune.entity.building;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.EntityOwner;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.level.Level.tileCenterX;
import static sk.neuromancer.Xune.level.Level.tileCenterY;

public abstract class Building extends Entity.PlayableEntity {
    public int tileX, tileY;

    public Building(int tileX, int tileY, Orientation orientation, EntityOwner owner, Flag flag, int maxHealth, int baseSpriteId) {
        super(tileCenterX(tileX, tileY), tileCenterY(tileX, tileY), owner, flag, maxHealth);
        this.tileX = tileX;
        this.tileY = tileY;
        this.orientation = orientation;
        int spriteRow = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(baseSpriteId + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight(), true));
    }

    @Override
    public void render() {
        glPushMatrix();
        float screenY = owner.getLevel().getScreenY(y);
        float depth = screenY / Game.DEFAULT_HEIGHT;
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, depth);
        this.sprite.render();
        if (isSelected) {
            SpriteSheet.MISC_SHEET.getSprite(1, 0).render();
        }
        glPopMatrix();
    }

    public abstract boolean[] getPassable();
}
