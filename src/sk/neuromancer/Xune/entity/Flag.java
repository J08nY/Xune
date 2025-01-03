package sk.neuromancer.Xune.entity;

public enum Flag {
    RED, BLUE, GREEN;

    public float[] getColor() {
        return switch (this) {
            case RED -> new float[]{1, 0, 0};
            case BLUE -> new float[]{0, 0, 1};
            case GREEN -> new float[]{0, 1, 0};
        };
    }
}
