package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.game.Tickable;

import java.util.function.DoubleUnaryOperator;

import static org.lwjgl.opengl.GL11.*;

public class Effect implements Tickable, Renderable {
    private int type;
    private int length;
    private int step = 0;
    private int duration;
    private DoubleUnaryOperator ease;
    private float x, y;
    private Sprite sprite;

    public static int EXPLOSION = 0;
    public static int SPARKLE = 8;
    public static int FIRE = 16;
    public static int HIT = 24;

    Effect(float x, float y, int type, int length, int duration, DoubleUnaryOperator ease) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.length = length;
        this.duration = duration;
        this.ease = ease;
        this.sprite = SpriteSheet.EFFECTS_SHEET.getSprite(type + step);
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(x - (float) sprite.getWidth() / 2, y - (float) sprite.getHeight() / 2, 0.99f);
        this.sprite.render();
        glPopMatrix();
    }

    @Override
    public void tick(int tickCount) {
        step++;
        if (isFinished()) {
            return;
        }
        float p = (float) step / duration;
        int t = Math.round((float) ease.applyAsDouble(p) * length);
        sprite = SpriteSheet.EFFECTS_SHEET.getSprite(type + t);
    }

    public boolean isFinished() {
        return step == duration;
    }

    public static class Explosion extends Effect {
        public Explosion(float x, float y) {
            super(x, y, EXPLOSION, 8, 100, Ease::easeOutSine);
        }
    }

    public static class Sparkle extends Effect {
        public Sparkle(float x, float y) {
            super(x, y, SPARKLE, 4, 130, Ease::linear);
        }
    }

    public static class Fire extends Effect {
        public Fire(float x, float y) {
            super(x, y - 10, FIRE, 8, 1000, Ease::linear);
        }
    }

    public static class Hit extends Effect {
        public Hit(float x, float y) {
            super(x, y, HIT, 3, 20, Ease::linear);
        }
    }
}
