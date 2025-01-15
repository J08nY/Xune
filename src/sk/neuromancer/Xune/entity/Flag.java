package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.net.proto.BaseProto;

public enum Flag {
    RED, BLUE, GREEN;

    private static final float[] RED_COLOR = new float[]{1, 0, 0};
    private static final float[] BLUE_COLOR = new float[]{0, 0, 1};
    private static final float[] GREEN_COLOR = new float[]{0, 1, 0};

    public float[] getColor() {
        return switch (this) {
            case RED -> RED_COLOR;
            case BLUE -> BLUE_COLOR;
            case GREEN -> GREEN_COLOR;
        };
    }

    public BaseProto.Flag serialize() {
        return switch (this) {
            case RED -> BaseProto.Flag.RED;
            case BLUE -> BaseProto.Flag.BLUE;
            case GREEN -> BaseProto.Flag.GREEN;
        };
    }
}
