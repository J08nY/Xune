package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.net.proto.BaseProto;

public enum Orientation {
    //0		1		 2		3		4		5		6		7
    NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST;

    public Orientation cw() {
        return switch (this) {
            case NORTH -> NORTHEAST;
            case NORTHEAST -> EAST;
            case EAST -> SOUTHEAST;
            case SOUTHEAST -> SOUTH;
            case SOUTH -> SOUTHWEST;
            case SOUTHWEST -> WEST;
            case WEST -> NORTHWEST;
            case NORTHWEST -> NORTH;
        };
    }

    public Orientation ccw() {
        return switch (this) {
            case NORTH -> NORTHWEST;
            case NORTHWEST -> WEST;
            case WEST -> SOUTHWEST;
            case SOUTHWEST -> SOUTH;
            case SOUTH -> SOUTHEAST;
            case SOUTHEAST -> EAST;
            case EAST -> NORTHEAST;
            case NORTHEAST -> NORTH;
        };
    }

    public Orientation opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTHEAST -> SOUTHWEST;
            case EAST -> WEST;
            case SOUTHEAST -> NORTHWEST;
            case SOUTH -> NORTH;
            case SOUTHWEST -> NORTHEAST;
            case WEST -> EAST;
            case NORTHWEST -> SOUTHEAST;
        };
    }

    public int getBit() {
        return switch (this) {
            case NORTH -> 0b1;
            case NORTHEAST -> 0b10;
            case EAST -> 0b100;
            case SOUTHEAST -> 0b1000;
            case SOUTH -> 0b10000;
            case SOUTHWEST -> 0b100000;
            case WEST -> 0b1000000;
            case NORTHWEST -> 0b10000000;
        };
    }

    public static int getBits(Orientation... orientations) {
        int bits = 0;
        for (Orientation orientation : orientations) {
            bits |= orientation.getBit();
        }
        return bits;
    }

    public static Orientation fromAzimuth(float radians) {
        double degrees = Math.toDegrees(radians);
        if (degrees < 0) {
            degrees += 360;
        }
        degrees %= 360;
        int index = (int) Math.round(degrees / 45) % 8;
        return switch (index) {
            case 0 -> NORTH;
            case 1 -> NORTHEAST;
            case 2 -> EAST;
            case 3 -> SOUTHEAST;
            case 4 -> SOUTH;
            case 5 -> SOUTHWEST;
            case 6 -> WEST;
            case 7 -> NORTHWEST;
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };
    }

    public int toAzimuthDegrees() {
        return switch (this) {
            case NORTH -> 0;
            case NORTHEAST -> 45;
            case EAST -> 90;
            case SOUTHEAST -> 135;
            case SOUTH -> 180;
            case SOUTHWEST -> 225;
            case WEST -> 270;
            case NORTHWEST -> 315;
        };
    }

    public float toAzimuthRadians() {
        return (float) Math.toRadians(toAzimuthDegrees());
    }

    public BaseProto.Orientation serialize() {
        return switch (this) {
            case NORTH -> BaseProto.Orientation.NORTH;
            case NORTHEAST -> BaseProto.Orientation.NORTHEAST;
            case EAST -> BaseProto.Orientation.EAST;
            case SOUTHEAST -> BaseProto.Orientation.SOUTHEAST;
            case SOUTH -> BaseProto.Orientation.SOUTH;
            case SOUTHWEST -> BaseProto.Orientation.SOUTHWEST;
            case WEST -> BaseProto.Orientation.WEST;
            case NORTHWEST -> BaseProto.Orientation.NORTHWEST;
        };
    }
}
