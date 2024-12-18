package sk.neuromancer.Xune.level;


import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glScalef;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.entity.Base;
import sk.neuromancer.Xune.entity.Harvester;
import sk.neuromancer.Xune.entity.Heli;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;
import sk.neuromancer.Xune.gfx.SpriteSheet;

public class OldLevel implements Renderable, Tickable {
    private Game game;
    private Player player;
    private Enemy enemy;

    private Tile[] level;
    private int width, height;

    public static final String LEVEL_1 = "level1.lvl";

    private float zoomFactor = 3f;
    private float xOffset = 0, yOffset = 0;

    public OldLevel(Game game) {
        this.game = game;
    }

    @Override
    public void tick(int tickCount) {
        float deltaScroll = game.getInput().scroller.getDeltaY();
        zoomFactor += deltaScroll * 0.08f;

        float widthMax = ((float) Game.WIDTH /
                (width * SpriteSheet.TILE_SHEET.getSpriteWidth()));
        float heightMax = ((float) Game.HEIGHT /
                (height * SpriteSheet.TILE_SHEET.getSpriteHeight()));
        float zoomFactorMax = Math.max(widthMax, heightMax);
        if (zoomFactor < zoomFactorMax) {
            zoomFactor = zoomFactorMax;
        }

        if (game.getInput().W.isPressed() || game.getInput().mouse.getY() < 15) {
            yOffset += 5 + 5 * zoomFactor;
        }
        if (game.getInput().A.isPressed() || game.getInput().mouse.getX() < 15) {
            xOffset += 5 + 5 * zoomFactor;
        }
        if (game.getInput().S.isPressed() || game.getInput().mouse.getY() > Game.HEIGHT - 15) {
            yOffset -= 5 + 5 * zoomFactor;
        }
        if (game.getInput().D.isPressed() || game.getInput().mouse.getX() > Game.WIDTH - 15) {
            xOffset -= 5 + 5 * zoomFactor;
        }

        if (-xOffset < 0) {
            xOffset = 0;
        }
        if (-yOffset < 0) {
            yOffset = 0;
        }

        player.tick(tickCount);
        enemy.tick(tickCount);
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(xOffset, yOffset, 0);
        glScalef(zoomFactor, zoomFactor, 0f);
        int startX = Math.max(0, (int) (toLevelX(0) / SpriteSheet.TILE_SHEET.getSpriteWidth()));
        int startY = Math.max(0, (int) (toLevelY(0) / SpriteSheet.TILE_SHEET.getSpriteHeight()));
        int endX = Math.min(width, 2 + (int) (toLevelX(Game.WIDTH) / SpriteSheet.TILE_SHEET.getSpriteWidth()));
        int endY = Math.min(height, 2 * (int) (toLevelY(Game.HEIGHT) / SpriteSheet.TILE_SHEET.getSpriteHeight()));

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                float xx = x - 0.5f;
                float yy = y * 0.545f - 0.5f;
                if (y % 2 != 0) {
                    xx += 0.5;
                }
                Sprite toDraw = SpriteSheet.TILE_SHEET.getSprite(level[y * width + x].type);
                glPushMatrix();
                glTranslatef(xx * toDraw.getWidth(), yy * toDraw.getHeight(), 0);
                toDraw.render();
                SpriteSheet.TILE_SHEET.getSprite(17).render(); // GRID
                glPopMatrix();
            }
        }

        player.render();
        enemy.render();
        glPopMatrix();
    }

    public void renderMini(int width, int height) {
        glPushMatrix();
        float xScale = width / ((float) this.width * SpriteSheet.TILE_SHEET.getSpriteWidth());
        float yScale = height / ((float) this.height * SpriteSheet.TILE_SHEET.getSpriteHeight());
        glScalef(xScale, yScale, 0);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                float xx = x - 0.5f;
                float yy = y * 0.545f - 0.5f;
                if (y % 2 != 0) {
                    xx += 0.5;
                }
                Sprite toDraw = SpriteSheet.TILE_SHEET.getSprite(level[y * this.width + x].type);
                glPushMatrix();
                glTranslatef(xx * toDraw.getWidth(), yy * toDraw.getHeight(), 0);
                toDraw.render();
                glPopMatrix();
            }
        }
        glPopMatrix();
    }

    public double toLevelX(double windowX) {
        //najprv ked je zoomFactor 1, to je vlastne toco chceme.. suradnice v pixeloch pri zoomFactor = 1 a scaleFactor = DEFAULT
        return (windowX - xOffset) / zoomFactor;
    }

    public double toLevelY(double windowY) {
        return (windowY - yOffset) / zoomFactor;
    }

    public void setPlayer(Player player) {
        this.player = player;
        player.addEntity(new Base(24 * 3.5f, 11 * 0.5f, player, player.getFlag()));
        player.addEntity(new Harvester(24 * 6, 0, player, player.getFlag()));
        player.addEntity(new Heli(0, 0, player, player.getFlag()));
        player.addEntity(new Heli(50, 0, player, player.getFlag()));
        player.addEntity(new Heli(0, 50, player, player.getFlag()));
    }

    public Player getPlayer() {
        return player;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
        enemy.addEntity(new Heli(100, 0, enemy, enemy.getFlag()));
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public Tile[] getTiles() {
        return level;
    }

    public void loadLevel(String levelName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("res/lvl/" + levelName));
            String line = br.readLine();
            this.width = line.length() / 2;
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            String[] lvl = sb.toString().split(",");

            this.level = new Tile[lvl.length];

            for (int i = 0; i < lvl.length; i++) {
                this.level[i] = new Tile(Byte.valueOf(lvl[i]));
            }
            this.height = this.level.length / this.width;

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
