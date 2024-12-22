package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.gfx.Sprite.ScalableSprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteSheet {
    public static SpriteSheet ENTITY_SHEET = new SpriteSheet("entities.png", 24, 11);
    public static SpriteSheet TILE_SHEET = new SpriteSheet("tiles.png", 24, 11);
    public static SpriteSheet EFFECTS_SHEET = new SpriteSheet("effects.png", 24, 11);
    public static SpriteSheet PASS_SHEET = new SpriteSheet("passmap.png", 10, 10);
    public static ScalableSpriteSheet CURSOR_SHEET = new ScalableSpriteSheet("cursors.png", 19, 19);
    public static ScalableSpriteSheet LOGO = new ScalableSpriteSheet("logo.png", 160, 61);
    public static ScalableSpriteSheet HUD_PANEL = new ScalableSpriteSheet("gamepanel.png", 384, 60);
    public static ScalableSpriteSheet TEXT_SHEET = new ScalableSpriteSheet("text.png", 8, 8);
    public static SpriteSheet WORM_SHEET = new SpriteSheet("worm.png", 24, 11);

    protected int width, height; // Spritoch, nie pixeloch..

    protected String imageName;
    protected int spriteWidth, spriteHeight;

    protected Sprite[] sprites;

    protected boolean isInitiated = false;

    public SpriteSheet(String imageName, int spriteWidth, int spriteHeight) {
        this.imageName = imageName;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }

    public void initSheet() {
        BufferedImage img = null;
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/sk/neuromancer/Xune/img/" + imageName));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        width = img.getWidth() / spriteWidth;
        height = img.getHeight() / spriteHeight;

        sprites = new Sprite[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] RGBAData = null;
                RGBAData = img.getData().getPixels(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight, RGBAData);
                sprites[y * width + x] = new Sprite(RGBAData, spriteWidth, spriteHeight);
            }
        }
        isInitiated = true;
    }

    public int numSprites() {
        return sprites.length;
    }

    public Sprite getSprite(int index) {
        return sprites[index];
    }

    public Sprite getSprite(int x, int y) {
        return getSprite(y * width + x);
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public void destroySheet() {
        if (!isInitiated)
            return;

        for (Sprite s : sprites) {
            s.destroy();
        }
    }

    public static void initSheets() {
        SpriteSheet.ENTITY_SHEET.initSheet();
        SpriteSheet.TILE_SHEET.initSheet();
        SpriteSheet.EFFECTS_SHEET.initSheet();
        SpriteSheet.PASS_SHEET.initSheet();
        SpriteSheet.CURSOR_SHEET.initSheet();
        SpriteSheet.LOGO.initSheet();
        SpriteSheet.HUD_PANEL.initSheet();
        SpriteSheet.TEXT_SHEET.initSheet();
        SpriteSheet.WORM_SHEET.initSheet();
    }

    public static void destroySheets() {
        SpriteSheet.ENTITY_SHEET.destroySheet();
        SpriteSheet.TILE_SHEET.destroySheet();
        SpriteSheet.EFFECTS_SHEET.destroySheet();
        SpriteSheet.PASS_SHEET.destroySheet();
        SpriteSheet.CURSOR_SHEET.destroySheet();
        SpriteSheet.LOGO.destroySheet();
        SpriteSheet.HUD_PANEL.destroySheet();
        SpriteSheet.TEXT_SHEET.destroySheet();
        SpriteSheet.WORM_SHEET.destroySheet();
    }

    public static class ScalableSpriteSheet extends SpriteSheet {

        public ScalableSpriteSheet(String imageName, int spriteWidth, int spriteHeight) {
            super(imageName, spriteWidth, spriteHeight);
        }

        @Override
        public void initSheet() {
            BufferedImage img = null;
            try {
                img = ImageIO.read(getClass().getResourceAsStream("/sk/neuromancer/Xune/img/" + imageName));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            width = img.getWidth() / spriteWidth;
            height = img.getHeight() / spriteHeight;

            sprites = new ScalableSprite[width * height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int[] RGBAData = null;
                    RGBAData = img.getData().getPixels(x * spriteWidth, y * spriteHeight, spriteWidth, spriteHeight, RGBAData);
                    sprites[y * width + x] = new ScalableSprite(RGBAData, spriteWidth, spriteHeight);
                }
            }
            isInitiated = true;
        }

        @Override
        public ScalableSprite getSprite(int index) {
            return (ScalableSprite) sprites[index];
        }

        @Override
        public ScalableSprite getSprite(int x, int y) {
            return getSprite(y * width + x);
        }

        public void setScaleFactor(float scaleFactor) {
            for (Sprite s : sprites) {
                ScalableSprite ss = (ScalableSprite) s;
                ss.setScaleFactor(scaleFactor);
            }
        }

    }

}
