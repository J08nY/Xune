package sk.neuromancer.Xune.entity;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Heli extends PlayableEntity {
    private int baseSpriteId = Entity.SPRITE_ID_HELI;
    private int animation;
    private int heading;
    private boolean wing;

    public Heli(float x, float y, int heading, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag);
        this.animation = baseSpriteId + PlayableEntity.getOffsetonFlag(flag);
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(this.animation);
        this.heading = heading;
        if (owner instanceof Player)
            this.clickableAreas.add(ClickableCircle.getCentered(x, y, 11, 5, 7, Button.LEFT, false));
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x, y, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(animation).render();
        if (isSelected)
            ((ClickableCircle) this.clickableAreas.get(0)).render();
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
        }

        if (tickCount % 60 == 0) {
            heading += 1;
            heading %= 8;
        }
    }

}
