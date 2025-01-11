package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.game.players.Bot;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.gfx.Window;
import sk.neuromancer.Xune.level.Level;

import java.time.Duration;

public class Arena {
    private Window window;

    public Arena() {
        this.window = new Window();
        SpriteSheet.initSheets();
        Entity.initClasses();
    }

    public void start() {
        Level level = new Level(null, Level.LEVEL_1);
        Bot one = new Bot.JackOfAllTrades(null, level, Flag.BLUE, 1000);
        Bot other = new Bot.BuggyBoy(null, level, Flag.RED, 1000);
        int tickCount = 0;
        System.out.println("Starting game loop");
        while (!level.isDone()) {
            level.tick(tickCount);
            tickCount++;
        }
        Duration duration = Duration.ofSeconds(tickCount / 60);
        System.out.println("Game over " + tickCount + " ticks -> " + duration.toString().replace("PT", "").toLowerCase());
        System.out.println("One is " + (one.isEliminated() ? "eliminated": "alive"));
        System.out.println("Other is " + (other.isEliminated() ? "eliminated": "alive"));
    }

    public static void main(String[] args) {
        Arena arena = new Arena();
        arena.start();
    }
}
