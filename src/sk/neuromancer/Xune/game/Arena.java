package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.game.players.Bot;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.level.Level;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

public class Arena {

    public Arena() {
        SpriteSheet.initSheets();
        Entity.initClasses();
    }

    private int[] runGames(Class<? extends Bot> oneClass, Class<? extends Bot> otherClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        int games = 10;
        int oneWins = 0;
        int otherWins = 0;
        int totalTicks = 0;
        for (int i = 0; i < games; i++) {
            Level level = new Level(Level.LEVEL_1);
            Bot one = oneClass.getConstructor(Level.class, Flag.class, int.class, long.class).newInstance(level, Flag.BLUE, 1000, 0);
            Bot other = otherClass.getConstructor(Level.class, Flag.class, int.class, long.class).newInstance(level, Flag.RED, 1000, 1);
            int tickCount = 0;
            System.out.println("Starting game");
            while (!level.isDone()) {
                level.tick(tickCount);
                tickCount++;
                if (tickCount % 2000 == 0) {
                    System.out.print(".");
                    //System.out.println("Tick " + tickCount);
                    //System.out.println("One: " + one.getMoney() + " army:" + one.getEntities().stream().filter(e -> e instanceof Unit).map(e -> Entity.PlayableEntity.getCost(e.getClass())).reduce(0, Integer::sum));
                    //System.out.println("Other: " + other.getMoney() + " army:" + other.getEntities().stream().filter(e -> e instanceof Unit).map(e -> Entity.PlayableEntity.getCost(e.getClass())).reduce(0, Integer::sum));
                    //System.out.println("One: " + one.getEntities().size() + one.getEntities().stream().map(e -> e.getClass().getSimpleName()).reduce("", (a, b) -> a + " " + b));
                    //System.out.println("Other: " + other.getEntities().size() + other.getEntities().stream().map(e -> e.getClass().getSimpleName()).reduce("", (a, b) -> a + " " + b));
                }
                if (tickCount >= 100000) {
                    System.out.println();
                    System.out.println("Game took too long, draw");
                    break;
                }
            }
            System.out.println();
            Duration duration = Duration.ofSeconds(tickCount / 60);
            System.out.println("Game over " + tickCount + " ticks -> " + duration.toString().replace("PT", "").toLowerCase());
            System.out.println(one.getClass().getSimpleName() + " is " + (one.isEliminated() ? "eliminated" : "alive"));
            System.out.println(other.getClass().getSimpleName() + " is " + (other.isEliminated() ? "eliminated" : "alive"));
            totalTicks += tickCount;
            if (one.isEliminated() && !other.isEliminated()) {
                otherWins++;
            } else if (!one.isEliminated() && other.isEliminated()) {
                oneWins++;
            }
            System.out.println("One wins: " + oneWins + " Other wins: " + otherWins);
            System.out.println();
        }

        System.out.println("One wins: " + oneWins + " Other wins: " + otherWins);
        System.out.println("Average ticks: " + totalTicks / games);
        return new int[]{oneWins, otherWins};
    }

    @SuppressWarnings("unchecked")
    public void start() {
        Class<? extends Bot>[] classes = new Class[]{Bot.ArmyGeneral.class, Bot.BuggyBoy.class, Bot.HeliMaster.class, Bot.JackOfAllTrades.class, Bot.EconGraduate.class};
        int[][] results = new int[classes.length][classes.length];
        for (int i = 0; i < classes.length; i++) {
            Class<? extends Bot> oneClass = classes[i];
            for (int j = 0; j < classes.length; j++) {
                Class<? extends Bot> otherClass = classes[j];
                try {
                    int[] wins = runGames(oneClass, otherClass);
                    results[i][j] += wins[0];
                    results[j][i] += wins[1];
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < classes.length; i++) {
            Class<? extends Bot> oneClass = classes[i];
            for (int j = 0; j < classes.length; j++) {
                Class<? extends Bot> otherClass = classes[j];
                System.out.println(oneClass.getSimpleName() + " vs " + otherClass.getSimpleName() + " -> " + results[i][j] + " : " + results[j][i]);
            }
        }
    }

    public static void main(String[] args) {
        Arena arena = new Arena();
        arena.start();
    }
}
