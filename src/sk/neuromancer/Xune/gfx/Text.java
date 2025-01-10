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

    public Text(String text, boolean outline, float scale) {
        this(text, 0, 0, outline, scale);
    }

    public Text(String text, boolean outline) {
        this(text, 0, 0, outline, 1);
    }

    public Text(String text) {
        this(text, 0, 0, false, 1);
    }

    public float getWidth() {
        return widthOf(text, scale);
    }

    public float getHeight() {
        return heightOf(text, scale);
    }

    public static float widthOf(String text, float scale) {
        return text.length() * SpriteSheet.TEXT_SHEET.getSpriteWidth() * scale;
    }

    public static float heightOf(String text, float scale) {
        return SpriteSheet.TEXT_SHEET.getSpriteHeight() * scale;
    }

    @Override
    public void render() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ$& 0123456789.,!?'\"-+=/\\%()<>:;";
        int sw = SpriteSheet.TEXT_SHEET.getSpriteWidth();
        int sh = SpriteSheet.TEXT_SHEET.getSpriteHeight();
        glPushMatrix();
        glTranslatef(x, y, 0);
        glScalef(scale, scale, 0);
        int accum = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                glTranslatef(-accum, sh * 1.2f, 0);
                accum = 0;
                continue;
            }
            int spriteId = charset.indexOf(text.charAt(i)) + (outline ? SpriteSheet.TEXT_SHEET.getWidth() * 2 : 0);
            SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
            glTranslatef(sw, 0, 0);
            accum += sw;
        }
        glPopMatrix();
    }
}
