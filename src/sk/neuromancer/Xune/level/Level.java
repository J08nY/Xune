package sk.neuromancer.Xune.level;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;

public class Level implements Renderable, Tickable {
    private Game game;

    private Pathfinder pathfinder;

    private Player player;
    private Enemy enemy;

    private Tile[][] level;
    private int width, height;

    private float zoom = 1.0f;

    public float xOff = 0.0f;
    public float yOff = 0.0f;

    public static final String LEVEL_1 = "newlevel.lvl";

    public Level(Game game) {
        this.game = game;
    }

    @Override
    public void tick(int tickCount) {

        if (this.game.getInput().PLUS.isPressed()) {
            this.zoom *= 1.02f;
        } else if (this.game.getInput().MINUS.isPressed()) {
            this.zoom *= 0.98f;
        }

        float dy = this.game.getInput().scroller.getDeltaY();
        if (dy > 0) {
            this.zoom *= 1.02f;
        } else if (dy < 0) {
            this.zoom *= 0.98f;
        }

        if (this.game.getInput().W.isPressed()) {
            this.yOff += 5 * this.zoom;
        } else if (this.game.getInput().A.isPressed()) {
            this.xOff += 5 * this.zoom;
        } else if (this.game.getInput().S.isPressed()) {
            this.yOff -= 5 * this.zoom;
        } else if (this.game.getInput().D.isPressed()) {
            this.xOff -= 5 * this.zoom;
        }


        player.tick(tickCount);
        enemy.tick(tickCount);
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(xOff, yOff, 0);
        glScalef(zoom, zoom, 0f);

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
            BufferedReader br = new BufferedReader(new FileReader("res/lvl/" + levelName));
            String line = br.readLine();
            this.width = Integer.parseInt(line.split("x")[0]);
            this.height = Integer.parseInt(line.split("x")[1]);
            line = br.readLine();

            StringBuilder sb = new StringBuilder();
            while (line != null) {
                sb.append(line);
                sb.append("=");
                line = br.readLine();
            }
            br.close();

            String[] lvl = sb.toString().split("=");
            this.level = new Tile[this.width][this.height];

            for (int i = 0; i < lvl.length; i++) {
                String[] row = lvl[i].split(",");
                for (int j = 0; j < row.length; j++) {
                    this.level[j][i] = new Tile(Byte.parseByte(row[j]), j, i);
                }
            }
            //this.pathfinder = new Pathfinder(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    /**
     * @param levelPointX
     * @return Point sized Screen X coordinate
     */
    public int getScreenX(float levelPointX) {
        return Math.round(levelPointX * this.zoom + this.xOff);
    }

    /**
     * @param levelPointY
     * @return Point sized Screen Y coordinate
     */
    public int getScreenY(float levelPointY) {
        return Math.round(levelPointY * this.zoom + this.yOff);
    }

    /**
     * @param screenPointX
     * @return Point sized X Level coordinate
     */
    public float getLevelX(double screenPointX) {
        return ((float) screenPointX - this.xOff) / this.zoom;
    }

    /**
     * @param screenPointY
     * @return Point sized Y Level coordinate
     */
    public float getLevelY(double screenPointY) {
        return ((float) screenPointY - this.yOff) / this.zoom;
    }
}
