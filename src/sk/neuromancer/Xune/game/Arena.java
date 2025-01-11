package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.entity.unit.Unit;
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
        int games = 10;
        int oneWins = 0;
        int otherWins = 0;
        for (int i = 0; i < games; i++) {
            Level level = new Level(null, Level.LEVEL_1);
            Bot one = new Bot.JackOfAllTrades(null, level, Flag.BLUE, 1000);
            Bot other = new Bot.ArmyGeneral(null, level, Flag.RED, 1000);
            int tickCount = 0;
            System.out.println("Starting game");
            while (!level.isDone()) {
                level.tick(tickCount);
                tickCount++;
                if (tickCount % 2000 == 0) {
                    System.out.println("Tick " + tickCount);
                    System.out.println("One: " + one.getMoney() + " army:" + one.getEntities().stream().filter(e -> e instanceof Unit).map(e -> Entity.PlayableEntity.getCost(e.getClass())).reduce(0, Integer::sum));
                    System.out.println("Other: " + other.getMoney() + " army:" + other.getEntities().stream().filter(e -> e instanceof Unit).map(e -> Entity.PlayableEntity.getCost(e.getClass())).reduce(0, Integer::sum));
                    //System.out.println("One: " + one.getEntities().size() + one.getEntities().stream().map(e -> e.getClass().getSimpleName()).reduce("", (a, b) -> a + " " + b));
                    //System.out.println("Other: " + other.getEntities().size() + other.getEntities().stream().map(e -> e.getClass().getSimpleName()).reduce("", (a, b) -> a + " " + b));
                }
                if (tickCount >= 100000) {
                    System.out.println("Game took too long, draw");
                    break;
                }
            }
            Duration duration = Duration.ofSeconds(tickCount / 60);
            System.out.println("Game over " + tickCount + " ticks -> " + duration.toString().replace("PT", "").toLowerCase());
            System.out.println(one.getClass().getSimpleName() + " is " + (one.isEliminated() ? "eliminated": "alive"));
            System.out.println(other.getClass().getSimpleName() + " is " + (other.isEliminated() ? "eliminated": "alive"));
            if (one.isEliminated() && !other.isEliminated()) {
                otherWins++;
            } else if (!one.isEliminated() && other.isEliminated()) {
                oneWins++;
            }
        }
        System.out.println("One wins: " + oneWins + " Other wins: " + otherWins);
    }

    public static void main(String[] args) {
        Arena arena = new Arena();
        arena.start();
    }
}
