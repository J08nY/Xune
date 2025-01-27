package sk.neuromancer.Xune.game;

import org.lwjgl.system.Library;
import picocli.CommandLine;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.game.players.Bot;
import sk.neuromancer.Xune.game.players.Human;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.game.screens.Gameover;
import sk.neuromancer.Xune.game.screens.Intro;
import sk.neuromancer.Xune.game.screens.Pause;
import sk.neuromancer.Xune.graphics.HUD;
import sk.neuromancer.Xune.graphics.LevelView;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.network.controllers.LocalController;
import sk.neuromancer.Xune.proto.LevelProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@CommandLine.Command(name = "Xune", mixinStandardHelpOptions = true, version = "1.0", description = "Xune 2025.")
public class Game implements Runnable {

    private boolean keepRunning = false;
    private static int tickCount = 0;
    private GameState state;

    private InputHandler input;
    private SoundManager sound;
    private Window window;

    private Intro intro;
    private Pause pause;
    private Gameover gameover;

    private Level level;
    private LevelView view;
    private Human human;
    private HUD hud;

    @CommandLine.Option(names = {"-f", "--fullscreen"}, description = "Run in fullscreen mode.")
    private boolean fullscreen;

    public enum GameState {
        INTRO, PLAYING, PAUSED, GAMEOVER
    }

    public void run() {
        keepRunning = true;
        state = GameState.INTRO;
        init();
        loop();
        quit();
    }

    private void init() {
        if (fullscreen) {
            window = new Window();
        } else {
            window = new Window(Config.DEFAULT_WIDTH, Config.DEFAULT_HEIGHT);
        }

        SpriteSheet.initSheets();
        Entity.initClasses();

        input = new InputHandler(window);
        sound = new SoundManager();

        intro = new Intro(this);
        pause = new Pause(this);
        gameover = new Gameover(this);

        SoundManager.play(SoundManager.TRACK_DUNESHIFTER, true, 0.5f);
        window.show();
    }

    private void loop() {
        long lastRender = System.nanoTime();
        double unprocessed = 0;
        double nsPerTick = 1000000000.0 / Config.TPS;
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
                view.render();
                hud.render();
                gameover.render();
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
                    if (intro.shouldPlayNew()) {
                        play();
                    } else if (intro.shouldLoad()) {
                        load();
                        cont();
                    }
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
                if (level.isDone() && !gameover.shouldContinue()) {
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
                if (pause.shouldSave()) {
                    save();
                    cont();
                }
                if (pause.shouldLoad()) {
                    load();
                    cont();
                }
                if (pause.shouldExit()) {
                    stop();
                }
                break;
            case GAMEOVER:
                gameover.tick(tickCount);
                input.tick(tickCount);
                sound.tick(tickCount);
                if (gameover.shouldContinue()) {
                    cont();
                }
                if (gameover.shouldExit()) {
                    stop();
                }
                if (input.ESC.isPressed()) {
                    stop();
                }
                break;
        }

        if (glfwWindowShouldClose(window.getHandle()))
            stop();
    }

    private void save() {
        try (FileOutputStream fos = new FileOutputStream("save.xune");
        ) {
            //GZIPOutputStream gos = new GZIPOutputStream(fos)
            level.serializeFull().writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        try (FileInputStream fis = new FileInputStream("save.xune");
        ) {
            //GZIPInputStream gis = new GZIPInputStream(fis)
            LevelProto.FullLevelState state = LevelProto.FullLevelState.parseFrom(fis);
            if (hud == null) {
                hud = new HUD(input, window);
            }
            if (view == null) {
                view = new LevelView(input, window, hud);
            }
            level = new Level(state);
            level.setView(view);
            human = level.getHuman();
            human.setHud(hud);
            human.setView(view);
            human.setInput(input);
            for (Player p : level.getPlayers()) {
                p.setController(new LocalController(level, p));
            }

            hud.setLevel(level);
            hud.setView(view);
            view.setLevel(level, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void play() {
        state = GameState.PLAYING;

        Flag humanFlag = intro.getSelectedFlag();
        Flag[] flags = Flag.values();
        Flag botFlag = humanFlag;
        Random r = new Random();
        while (botFlag == humanFlag) {
            botFlag = flags[r.nextInt(flags.length)];
        }

        level = new Level(Level.LEVEL_1);
        human = new Human(level, humanFlag, 1000, 0);
        human.setController(new LocalController(level, human));
        Class<? extends Bot> botClass = intro.getSelectedBot();
        try {
            Bot bot = botClass.getDeclaredConstructor(Level.class, Flag.class, int.class, long.class).newInstance(level, botFlag, 1000, 1);
            bot.setController(new LocalController(level, bot));
        } catch (Exception ignored) {
        }

        hud = new HUD(input, window);
        hud.setLevel(level);
        view = new LevelView(input, window, hud);
        hud.setView(view);
        view.setLevel(level, true);
        level.setView(view);

        human.setHud(hud);
        human.setView(view);
        human.setInput(input);
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
        int exitCode = new CommandLine(new Game()).execute(args);
        System.exit(exitCode);
    }


}
