package sk.neuromancer.Xune.entity;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Base extends PlayableEntity {

    public Base(float x, float y, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.orientation = Orientation.NORTH;
        int spriteOffset = this.orientation.ordinal() % 2 == 0 ? 1 : 0;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_BASE + PlayableEntity.getOffsetonFlag(flag) + spriteOffset * Entity.SPRITE_ROW_LENGTH);
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableTile.getFromDimensions(x, y, this.sprite.getWidth(), this.sprite.getHeight(), Button.LEFT, true));
    }

    @Override
    public void render() {
        glPushMatrix();
        //((ClickableTile) this.clickableAreas.get(0)).render();
        glTranslatef(x, y, 0);
        this.sprite.render();
        if (isSelected)
            SpriteSheet.TILE_SHEET.getSprite(17).render();
        glPopMatrix();
    }

    @Override
    public void tick(int tickCount) {

    }

    @Override
    public boolean onClick(float x, float y, Button b) {
        boolean clickedOn = super.onClick(x, y, b);
        if (clickedOn) {
            this.isSelected = !this.isSelected;
            System.out.println("clickedOnBase");
        }
        return clickedOn;
    }


}
