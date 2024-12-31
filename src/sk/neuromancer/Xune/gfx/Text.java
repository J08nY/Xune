package sk.neuromancer.Xune.gfx;

import static org.lwjgl.opengl.GL11.*;

public class Text implements Renderable {
    private final String text;
    private final float x, y;
    private final float scale;
    private final boolean outline;

    public Text(String text, float x, float y, boolean outline, float scale) {
        this.text = text.toUpperCase();
        this.x = x;
        this.y = y;
        this.outline = outline;
        this.scale = scale;
    }

    public Text(String text, boolean outline) {
        this(text, 0, 0, outline, 1);
    }

    public Text(String text) {
        this(text, 0, 0, false, 1);
    }

    public float getWidth() {
        return text.length() * SpriteSheet.TEXT_SHEET.getSpriteWidth() * scale;
    }

    public float getHeight() {
        return SpriteSheet.TEXT_SHEET.getSpriteHeight() * scale;
    }


    @Override
    public void render() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ$ 0123456789.,!?'\"-+=/\\%()<>:;";
        float sw = SpriteSheet.TEXT_SHEET.getSpriteWidth() * scale;
        for (int i = 0; i < text.length(); i++) {
            int spriteId = charset.indexOf(text.charAt(i)) + (outline ? SpriteSheet.TEXT_SHEET.width * 2 : 0);
            glPushMatrix();
            glTranslated(x + sw * i, y, 0);
            glScalef(scale, scale, 0);
            SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
            glPopMatrix();
        }
    }
}
