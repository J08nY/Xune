package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Worm extends Entity {
    private int animation = 0;

    public Worm(float x, float y) {
        super(x, y, 1000);
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(animation);
        this.clickableAreas.add(ClickableTile.getCentered(this.x, this.y, this.sprite.getWidth(), this.sprite.getHeight(), true));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 20 == 0) {
            animation++;
        }
        if (animation >= SpriteSheet.WORM_SHEET.numSprites()) {
            animation = 0;
        }
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(animation);
    }

    @Override
    public boolean intersects(float fromX, float fromY, float toX, float toY) {
        return false;
    }

}
