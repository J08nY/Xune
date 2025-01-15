package sk.neuromancer.Xune.graphics;

public class Ease {

    public static double linear(double x) {
        return x;
    }

    public static double easeInSine(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }

    public static double easeOutSine(double x) {
        return Math.sin((x * Math.PI) / 2);
    }

    public static double easeInOutSine(double x) {
        return (1 - Math.cos(Math.PI * x)) / 2;
    }
}
