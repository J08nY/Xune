package sk.neuromancer.Xune.game;

import org.lwjgl.system.Library;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.gfx.*;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game implements Renderable {

    private boolean keepRunning = false;
    private static int tickCount = 0;
    private GameState state;

    private InputHandler input;
    private SoundManager sound;
    private Window window;

    private Intro intro;

    private Level level;
    private LevelView view;
    private Human human;
    private Bot bot;
    private HUD hud;

    public static final int DEFAULT_WIDTH = 1920;
    public static final int DEFAULT_HEIGHT = 1080;

    public static final int TPS = 60;

    public enum GameState {
        INTRO, PLAYING, PAUSED, GAMEOVER
    }

    public void start() {
        keepRunning = true;
        state = GameState.PLAYING;
        init();
        run();
        quit();
    }

    private void init() {
        window = new Window(); //DEFAULT_WIDTH, DEFAULT_HEIGHT

        SpriteSheet.initSheets();
        Entity.initClasses();

        input = new InputHandler(this);
        sound = new SoundManager(this);


        intro = new Intro(this);

        level = new Level(this, Level.LEVEL_1);
        human = new Human(this, level, Flag.BLUE, 1000);
        bot = new Bot.JackOfAllTrades(this, level, Flag.RED, 1000);
        hud = new HUD(this);
        hud.setLevel(level);
        view = new LevelView(this);
        view.setLevel(level);

        SoundManager.play(SoundManager.TRACK_DUNESHIFTER, true, 0.5f);
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
                lastTimer1 = currentTime;
                System.out.println(ticks + " ticks, " + frames + " fps");
                frames = 0;
                ticks = 0;
            }
        }
    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        switch (state) {
            case INTRO:
                intro.render();
                break;
            case PLAYING:
                view.render();
                hud.render();
                break;
            case PAUSED:
                break;
            case GAMEOVER:
                break;
        }

        glfwSwapBuffers(window.getHandle());
    }

    public void tick() {
        tickCount++;
        glfwPollEvents();

        switch (state) {
            case INTRO:
                intro.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                if (intro.isDone()) {
                    state = GameState.PLAYING;
                }
                break;
            case PLAYING:
                view.tick(tickCount);
                level.tick(tickCount);
                hud.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                break;
            case PAUSED:
                break;
            case GAMEOVER:
                break;
        }

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

    public HUD getHud() {
        return hud;
    }

    public LevelView getView() {
        return view;
    }

    public GameState getState() {
        return state;
    }

    public static int currentTick() {
        return tickCount;
    }

    public static void main(String[] args) {
        Library.initialize();
        Game g = new Game();
        g.start();
    }


}
