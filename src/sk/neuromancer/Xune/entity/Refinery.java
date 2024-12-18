package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

import static org.lwjgl.opengl.GL11.*;

public class Refinery extends PlayableEntity {

    public Refinery(float x, float y, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = Orientation.SOUTHEAST;
        int spriteOffset = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_REFINERY + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableTile.getFromDimensions(x, y, this.sprite.getWidth(), this.sprite.getHeight(), Button.LEFT, true));
    }

    @Override
    public void render() {
        glPushMatrix();
        //((ClickableTile) this.clickableAreas.get(0)).render();
        glTranslatef(x, y, 0);
        this.sprite.render();
        glPopMatrix();
    }


    @Override
    public void tick(int tickCount) {

    }

}
