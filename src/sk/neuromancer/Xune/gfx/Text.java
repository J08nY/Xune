package sk.neuromancer.Xune.gfx;

import static org.lwjgl.opengl.GL11.*;

public class Text implements Renderable {
    private String text;
    private float x, y;

    public Text(String text, float x, float y) {
        this.text = text.toUpperCase();
        this.x = x;
        this.y = y;
    }

    public Text(String text) {
        this(text, 0, 0);
    }


    @Override
    public void render() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ  0123456789.,!?'\"-+=/\\%()<>:;";
        float textScaleFactor = SpriteSheet.TEXT_SHEET.getSprite(0).getScaleFactor();
        float scaledSpriteWidth = SpriteSheet.TEXT_SHEET.getSprite(0).getWidth() * textScaleFactor;
        for (int i = 0; i < text.length(); i++) {
            int spriteId = charset.indexOf(text.charAt(i));
            glPushMatrix();
            glTranslated(x + scaledSpriteWidth * i, y, 0);
            SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
            glPopMatrix();
        }
    }
}
