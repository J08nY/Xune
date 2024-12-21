package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.game.Tickable;

import java.util.function.DoubleUnaryOperator;

public class Effect implements Tickable, Renderable {
    private int type;
    private int length;
    private int step = 0;
    private int duration;
    private DoubleUnaryOperator ease;
    private float x, y;
    private Sprite sprite;

    public static int EXPLOSION = 0;


    Effect(int type, int length, int duration, DoubleUnaryOperator ease) {
        this.type = type;
        this.length = length;
        this.duration = duration;
        this.ease = ease;
        this.sprite = SpriteSheet.EFFECTS_SHEET.getSprite(type + step);
    }

    @Override
    public void render() {
        this.sprite.render();
    }

    @Override
    public void tick(int tickCount) {
        step++;
        if (step > duration) {
            step = 0;
        }
        float p = (float) step / duration;
        int t = Math.round((float) ease.applyAsDouble(p) * length);
        sprite = SpriteSheet.EFFECTS_SHEET.getSprite(type + t);
    }

    public static class Explosion extends Effect {
        public Explosion() {
            super(EXPLOSION, 8, 200, Ease::easeOutSine);
        }
    }
}
