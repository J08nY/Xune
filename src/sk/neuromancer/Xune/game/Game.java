package sk.neuromancer.Xune.game;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.gfx.HUD;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.gfx.Window;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

public class Game implements Renderable {

    private boolean keepRunning = false;
    private int tickCount = 0;

    private InputHandler input;
    private Window window;
    private Level level;
    private Player player;
    private Enemy enemy;

    private HUD hud;

    private SoundManager sound;

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    public static final int TPS = 60;

    public void start() {
        keepRunning = true;
        init();
        run();
        quit();
    }

    private void init() {
        window = new Window(WIDTH, HEIGHT);

        SpriteSheet.initSheets();

        input = new InputHandler(this);

        level = new Level(this);
        player = new Player(this, level, Flag.RED, 0);
        enemy = new Enemy(this, level, Flag.BLUE, 0);

        hud = new HUD(this);

        sound = new SoundManager(this);

        level.loadLevel(Level.LEVEL_1);
        level.setPlayer(player);
        level.setEnemy(enemy);

        window.show();
    }

    public void run() {
        long lastTime = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / TPS;
        int frames = 0;
        int ticks = 0;
        long lastTimer1 = System.currentTimeMillis();

        while (keepRunning) {
            long now = System.nanoTime();
            unprocessed += (now - lastTime) / nsPerTick;
            lastTime = now;
            boolean shouldRender = true;
            while (unprocessed >= 1) {
                ticks++;
                tick();
                unprocessed -= 1;
                shouldRender = true;
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (shouldRender) {
                frames++;
                render();
            }
            if (System.currentTimeMillis() - lastTimer1 > 1000) {
                lastTimer1 += 1000;
                System.out.println(ticks + " ticks, " + frames + " fps");
                frames = 0;
                ticks = 0;
            }
        }
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        level.render();
        hud.render();

        glfwSwapBuffers(window.getHandle());
    }

    public void tick() {
        tickCount++;
        glfwPollEvents();

        level.tick(tickCount);
        hud.tick(tickCount);
        input.tick(tickCount);
        sound.tick(tickCount);

        if (input.ESC.isPressed() || (glfwWindowShouldClose(window.getHandle())) == GL_TRUE)
            stop();
    }

    public void stop() {
        keepRunning = false;
    }

    private void quit() {
        SpriteSheet.destroySheets();

        input.quit();
        sound.quit();
        window.quit();
        glfwTerminate();
    }

    public long getWindow() {
        return window.getHandle();
    }

    public InputHandler getInput() {
        return input;
    }

    public Level getLevel() {
        return level;
    }

    public SoundManager getSound() {
        return sound;
    }

    public static void main(String[] args) {
        Game g = new Game();
        g.start();
    }


}
