package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.ai.Enemy;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.gfx.HUD;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.gfx.Window;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

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

    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1080;
    public static final int CENTER_X = DEFAULT_WIDTH / 2;
    public static final int CENTER_Y = DEFAULT_HEIGHT / 2;

    public static final int TPS = 120;

    public void start() {
        keepRunning = true;
        init();
        run();
        quit();
    }

    private void init() {
        window = new Window(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        SpriteSheet.initSheets();

        input = new InputHandler(this);

        level = new Level(this);
        level.loadLevel(Level.LEVEL_1);
        player = new Player(this, level, Flag.RED, 100);
        level.setPlayer(player);
        enemy = new Enemy(this, level, Flag.BLUE, 100);
        level.setEnemy(enemy);

        hud = new HUD(this);

        sound = new SoundManager(this);
        sound.play(SoundManager.TRACK_DUNESHIFTER, true, 0.5f);

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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (shouldRender) {
                frames++;
                render();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimer1 > 1000) {
                lastTimer1 += currentTime;
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

        if (input.ESC.isPressed() || glfwWindowShouldClose(window.getHandle()))
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

    public Window getWindow() {
        return window;
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
