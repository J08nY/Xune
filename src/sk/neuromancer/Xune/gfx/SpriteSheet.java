package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.entity.Flag;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteSheet {
    public static SpriteSheet ENTITY_SHEET = new SpriteSheet("entities.png", 24, 11);
    public static SpriteSheet TILE_SHEET = new SpriteSheet("tiles.png", 24, 11);
    public static SpriteSheet PASSMAP_SHEET = new SpriteSheet("passmap.png", 5, 5, true);
    public static SpriteSheet SOLIDMAP_SHEET = new SpriteSheet("solidmap.png", 5, 5, true);
    public static SpriteSheet MAP_SHEET = new SpriteSheet("minimap.png", 1, 1, true);
    public static SpriteSheet EFFECTS_SHEET = new SpriteSheet("effects.png", 24, 11);
    public static SpriteSheet MISC_SHEET = new SpriteSheet("misc.png", 24, 11);
    public static SpriteSheet CURSOR_SHEET = new SpriteSheet("cursors.png", 19, 19);
    public static SpriteSheet LOGO = new SpriteSheet("logo.png", 160, 61);
    public static SpriteSheet TITLE = new SpriteSheet("title.png", 464, 88);
    public static SpriteSheet HUD_PANEL = new SpriteSheet("gamepanel.png", 384, 60);
    public static SpriteSheet TEXT_SHEET = new SpriteSheet("text.png", 9, 9);
    public static SpriteSheet WORM_SHEET = new SpriteSheet("worm.png", 24, 11);

    private int width, height;
    private String imageName;
    private int spriteWidth, spriteHeight;
    private boolean keepData;
    private Sprite[] sprites;
    private boolean isInitialized;

    public static final int SPRITE_ID_BASE = 0;
    public static final int SPRITE_ID_FACTORY = 1;
    public static final int SPRITE_ID_BARRACKS = 2;
    public static final int SPRITE_ID_POWERPLANT = 3;
    public static final int SPRITE_ID_REFINERY = 4;
    public static final int SPRITE_ID_SILO = 5;
    public static final int SPRITE_ID_HELIPAD = 6;
    public static final int SPRITE_ID_BUGGY = 7;
    public static final int SPRITE_ID_HELI = 11;
    public static final int SPRITE_ID_HARVESTER = 19;
    public static final int SPRITE_ID_SOLDIER = 23;

    public static final int SPRITE_ROW_LENGTH = 35;

    public static final int SPRITE_OFFSET_RED = 0;
    public static final int SPRITE_OFFSET_GREEN = SPRITE_ROW_LENGTH * 2;
    public static final int SPRITE_OFFSET_BLUE = SPRITE_ROW_LENGTH * 4;

    public SpriteSheet(String imageName, int spriteWidth, int spriteHeight, boolean keepData) {
        this.imageName = imageName;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.keepData = keepData;
    }

    public SpriteSheet(String imageName, int spriteWidth, int spriteHeight) {
        this(imageName, spriteWidth, spriteHeight, false);
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
                sprites[y * width + x] = new Sprite(RGBAData, spriteWidth, spriteHeight, keepData);
            }
        }
        isInitialized = true;
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSpriteWidth() {
        return spriteWidth;
    }

    public int getSpriteHeight() {
        return spriteHeight;
    }

    public void destroySheet() {
        if (!isInitialized)
            return;

        for (Sprite s : sprites) {
            s.destroy();
        }
    }

    public static int flagToOffset(Flag f) {
        return switch (f) {
            case RED -> SPRITE_OFFSET_RED;
            case GREEN -> SPRITE_OFFSET_GREEN;
            case BLUE -> SPRITE_OFFSET_BLUE;
        };
    }

    public static void initSheets() {
        SpriteSheet.ENTITY_SHEET.initSheet();
        SpriteSheet.TILE_SHEET.initSheet();
        SpriteSheet.PASSMAP_SHEET.initSheet();
        SpriteSheet.SOLIDMAP_SHEET.initSheet();
        SpriteSheet.MAP_SHEET.initSheet();
        SpriteSheet.EFFECTS_SHEET.initSheet();
        SpriteSheet.MISC_SHEET.initSheet();
        SpriteSheet.CURSOR_SHEET.initSheet();
        SpriteSheet.LOGO.initSheet();
        SpriteSheet.TITLE.initSheet();
        SpriteSheet.HUD_PANEL.initSheet();
        SpriteSheet.TEXT_SHEET.initSheet();
        SpriteSheet.WORM_SHEET.initSheet();
    }

    public static void destroySheets() {
        SpriteSheet.ENTITY_SHEET.destroySheet();
        SpriteSheet.TILE_SHEET.destroySheet();
        SpriteSheet.PASSMAP_SHEET.destroySheet();
        SpriteSheet.SOLIDMAP_SHEET.destroySheet();
        SpriteSheet.MAP_SHEET.destroySheet();
        SpriteSheet.EFFECTS_SHEET.destroySheet();
        SpriteSheet.MISC_SHEET.destroySheet();
        SpriteSheet.CURSOR_SHEET.destroySheet();
        SpriteSheet.LOGO.destroySheet();
        SpriteSheet.TITLE.destroySheet();
        SpriteSheet.HUD_PANEL.destroySheet();
        SpriteSheet.TEXT_SHEET.destroySheet();
        SpriteSheet.WORM_SHEET.destroySheet();
    }
}
