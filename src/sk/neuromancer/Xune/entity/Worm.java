package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;

public class Worm extends Entity {
    private int animation = 0;

    public Worm(float x, float y) {
        super(x, y);
        this.sprite = SpriteSheet.WORM_SHEET.getSprite(animation);
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
    public void render() {
        super.render();
    }

    @Override
    public boolean onClick(float x, float y, Button b) {
        return false;
    }

}
