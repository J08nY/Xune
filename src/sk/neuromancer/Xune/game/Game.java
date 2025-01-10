package sk.neuromancer.Xune.game;

import org.lwjgl.system.Library;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.gfx.*;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    private boolean keepRunning = false;
    private static int tickCount = 0;
    private GameState state;

    private InputHandler input;
    private SoundManager sound;
    private Window window;

    private Intro intro;
    private Pause pause;

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
        pause = new Pause(this);

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

    private void run() {
        long lastRender = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / TPS;
        int frames = 0;
        int ticks = 0;
        long lastSecond = System.currentTimeMillis();

        while (keepRunning) {
            long now = System.nanoTime();
            unprocessed += (now - lastRender) / nsPerTick;
            lastRender = now;
            while (unprocessed >= 1) {
                ticks++;
                tick();
                unprocessed -= 1;
            }
            frames++;
            render();
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSecond > 1000) {
                lastSecond = currentTime;
                System.out.println(ticks + " ticks, " + frames + " fps");
                frames = 0;
                ticks = 0;
            }
        }
    }

    private void render() {
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
                view.render();
                hud.render();
                pause.render();
                break;
            case GAMEOVER:
                break;
        }

        glfwSwapBuffers(window.getHandle());
    }

    private void tick() {
        tickCount++;
        glfwPollEvents();

        switch (state) {
            case INTRO:
                intro.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                if (intro.isDone()) {
                    play();
                }
                if (input.ESC.isPressed()) {
                    stop();
                }
                break;
            case PLAYING:
                view.tick(tickCount);
                level.tick(tickCount);
                hud.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                if (input.ESC.isPressed()) {
                    pause();
                }
                if (level.isDone()) {
                    end();
                }
                break;
            case PAUSED:
                pause.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                if (pause.shouldContinue()) {
                    cont();
                }
                if (pause.shouldExit()) {
                    stop();
                }
                break;
            case GAMEOVER:
                if (input.ESC.isPressed()) {
                    stop();
                }
                break;
        }

        if (glfwWindowShouldClose(window.getHandle()))
            stop();
    }

    private void play() {
        state = GameState.PLAYING;
    }

    private void pause() {
        state = GameState.PAUSED;
    }

    private void cont() {
        state = GameState.PLAYING;
        pause.reset();
    }

    private void end() {
        state = GameState.GAMEOVER;
    }

    private void stop() {
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
