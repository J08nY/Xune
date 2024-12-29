package sk.neuromancer.Xune.gfx;

import static org.lwjgl.opengl.GL11.*;

public class Text implements Renderable {
    private final String text;
    private final float x, y;
    private final boolean outline;

    public Text(String text, float x, float y, boolean outline) {
        this.text = text.toUpperCase();
        this.x = x;
        this.y = y;
        this.outline = outline;
    }

    public Text(String text, boolean outline) {
        this(text, 0, 0, outline);
    }

    public Text(String text) {
        this(text, 0, 0, false);
    }


    @Override
    public void render() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ  0123456789.,!?'\"-+=/\\%()<>:;";
        float textScaleFactor = SpriteSheet.TEXT_SHEET.getSprite(0).getScaleFactor();
        float scaledSpriteWidth = SpriteSheet.TEXT_SHEET.getSprite(0).getWidth() * textScaleFactor;
        for (int i = 0; i < text.length(); i++) {
            int spriteId = charset.indexOf(text.charAt(i)) + (outline ? SpriteSheet.TEXT_SHEET.width * 2 : 0);
            glPushMatrix();
            glTranslated(x + scaledSpriteWidth * i, y, 0);
            SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
            glPopMatrix();
        }
    }
}
