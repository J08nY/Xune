package sk.neuromancer.Xune.level;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Level implements Renderable, Tickable {
    private final Game game;

    private Pathfinder pathfinder;

    private Player player;
    private Enemy enemy;

    private Tile[][] level;
    private int width, height;

    public float zoom;

    public float xOff;
    public float yOff;

    public static final String LEVEL_1 = "newlevel.lvl";
    public static final float ZOOM_SPEED = 0.02f;
    public static final float SCROLL_SPEED = 0.04f;
    public static final float MOVE_SPEED = 5f;
    public static final float EDGE_MARGIN_X = Tile.TILE_WIDTH * 2;
    public static final float EDGE_MARGIN_Y = Tile.TILE_HEIGHT * 4;

    public Level(Game game) {
        this.game = game;
    }

    @Override
    public void tick(int tickCount) {

        if (this.game.getInput().PLUS.isPressed()) {
            this.zoom *= 1 + ZOOM_SPEED;
        } else if (this.game.getInput().MINUS.isPressed()) {
            if (getLevelY(0) > -EDGE_MARGIN_Y && getLevelX(0) > -EDGE_MARGIN_X && getLevelY(Game.HEIGHT) < (getHeight() + EDGE_MARGIN_X + (float) Tile.TILE_HEIGHT / 2) && getLevelX(Game.WIDTH) < (getWidth() + EDGE_MARGIN_Y + (float) Tile.TILE_WIDTH / 2))
                this.zoom *= 1 - ZOOM_SPEED;
        }

        float dy = this.game.getInput().scroller.getDeltaY();
        if (dy > 0) {
            this.zoom *= 1 + SCROLL_SPEED;
        } else if (dy < 0) {
            if (getLevelY(0) > -EDGE_MARGIN_Y && getLevelX(0) > -EDGE_MARGIN_X && getLevelY(Game.HEIGHT) < (getHeight() + EDGE_MARGIN_X + (float) Tile.TILE_HEIGHT / 2) && getLevelX(Game.WIDTH) < (getWidth() + EDGE_MARGIN_Y + (float) Tile.TILE_WIDTH / 2))
                this.zoom *= 1 - SCROLL_SPEED;
        }

        if (this.game.getInput().W.isPressed() && getLevelY(0) > -EDGE_MARGIN_Y) {
            this.yOff += MOVE_SPEED * (1 / zoom);
        } else if (this.game.getInput().A.isPressed() && getLevelX(0) > -EDGE_MARGIN_X) {
            this.xOff += MOVE_SPEED * (1 / zoom);
        } else if (this.game.getInput().S.isPressed() && getLevelY(Game.HEIGHT) < (getHeight() + EDGE_MARGIN_Y + (float) Tile.TILE_HEIGHT / 2)) {
            this.yOff -= MOVE_SPEED * (1 / zoom);
        } else if (this.game.getInput().D.isPressed() && getLevelX(Game.WIDTH) < (getWidth() + EDGE_MARGIN_X + (float) Tile.TILE_WIDTH / 2)) {
            this.xOff -= MOVE_SPEED * (1 / zoom);
        }

        player.tick(tickCount);
        enemy.tick(tickCount);
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef((float) Game.WIDTH / 2, (float) Game.HEIGHT / 2, 0);
        glScalef(zoom, zoom, 0f);
        glTranslatef(-(float) Game.WIDTH / 2, -(float) Game.HEIGHT / 2, 0);
        glTranslatef(xOff, yOff, 0);

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                Tile t = this.level[x][y];
                t.render();
            }
        }
        enemy.render();
        player.render();

        glPopMatrix();
    }

    public void loadLevel(String levelName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/sk/neuromancer/Xune/lvl/" + levelName)));
            String line = br.readLine();
            this.width = Integer.parseInt(line.split("x")[0]);
            this.height = Integer.parseInt(line.split("x")[1]);
            line = br.readLine();

            List<String> lines = new LinkedList<>();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
            br.close();

            this.level = new Tile[this.width][this.height];

            for (int i = 0; i < lines.size(); i++) {
                String[] row = lines.get(i).split(",");
                for (int j = 0; j < row.length; j++) {
                    this.level[j][i] = new Tile(Byte.parseByte(row[j]), j, i);
                }
            }
            //this.pathfinder = new Pathfinder(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.zoom = 4.0f;
        this.xOff = Game.CENTER_X - getWidth() / 2;
        this.yOff = Game.CENTER_Y - getHeight() / 2;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEnemy(Enemy enemy) {
        this.enemy = enemy;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Enemy getEnemy() {
        return this.enemy;
    }

    public Tile[][] getTiles() {
        return this.level;
    }

    public float getWidth() {
        return this.width * Tile.TILE_WIDTH;
    }

    public float getHeight() {
        return (float) (this.height * Tile.TILE_HEIGHT) / 2;
    }

    public int getWidthInTiles() {
        return this.width;
    }

    public int getHeightInTiles() {
        return this.height;
    }

    public float getLevelX(double screenPointX) {
        return (((float) screenPointX - Game.CENTER_X) / this.zoom) - this.xOff + Game.CENTER_X;
    }

    public float getLevelY(double screenPointY) {
        return (((float) screenPointY - Game.CENTER_Y) / this.zoom) - this.yOff + Game.CENTER_Y;
    }
}
