// Command.java
package sk.neuromancer.Xune.entity;

public abstract class Command {

    public static class MoveCommand extends Command {
        private final float fromX, fromY, toX, toY;

        public MoveCommand(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        public float getToX() {
            return toX;
        }

        public float getToY() {
            return toY;
        }

        public boolean isFinished(float x, float y) {
            return Math.abs(x - toX) <= 1.5 && Math.abs(y - toY) <= 1.5;
        }
    }
}

