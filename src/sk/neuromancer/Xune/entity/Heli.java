package sk.neuromancer.Xune.entity;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import sk.neuromancer.Xune.entity.Clickable.ClickableCircle;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Heli extends PlayableEntity {
    private int baseSpriteId = Entity.SPRITE_ID_HELI;
    private int animation;
    private int heading;
    private boolean wing;

    public Heli(float x, float y, EntityOwner owner, Flag flag) {//TODO prepisat Heli classu
        super(x, y, owner, flag);
        this.animation = baseSpriteId + PlayableEntity.getOffsetonFlag(flag);
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(this.animation);//modra heli..
        this.heading = 3;
        this.clickableAreas.add(ClickableCircle.getCentered(x + 80, y + 80, 20, Button.LEFT, false));
        //this.clickableAreas.add(ClickableCircle.getFromDimensions(x, y, this.sprite.getWidth()*this.sprite.getScaleFactor(), this.sprite.getWidth()*this.sprite.getScaleFactor(), Button.LEFT, false));
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x, y, 0);
        ((ClickableCircle) this.clickableAreas.get(0)).render();
        SpriteSheet.ENTITY_SHEET.getSprite(animation).render();
        glPopMatrix();
    }

    @Override
    public void tick(int tickCount) {
        if (heading < 4) {
            animation = baseSpriteId + heading * 2;
        } else {
            animation = baseSpriteId + 8 + heading * 2;
        }
        animation += wing ? 1 : 0;

        if (tickCount % 5 == 0) {
            wing = !wing;
            setPosition(x + 3, y + 2);
        }

        if (tickCount % 60 == 0) {
            heading += 1;
            heading %= 8;
        }
    }
}
