package sk.neuromancer.Xune.game;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler implements Tickable {

    private final GLFWKeyCallback keyCallback;
    private final GLFWCursorPosCallback cursorCallback;
    private final GLFWMouseButtonCallback mouseCallback;
    private final GLFWScrollCallback scrollCallback;

    public class Key {
        private boolean isPressed = false;

        public void toggle(boolean isPressed) {
            this.isPressed = isPressed;
        }

        public boolean isPressed() {
            return this.isPressed;
        }
    }

    public class Mouse {
        private double x, y;
        private boolean pressLeft, releaseLeft, pressRight, releaseRight;
        private boolean leftMB = false, rightMB = false;
        private double leftX, leftY;
        private double rightX, rightY;

        public void setPosition(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void pressLeft() {
            this.leftMB = true;
            this.leftX = x;
            this.leftY = y;
            this.pressLeft = true;
        }

        public void pressRight() {
            this.rightMB = true;
            this.rightX = x;
            this.rightY = y;
            this.pressRight = true;
        }

        public void releaseLeft() {
            this.leftMB = false;
            this.releaseLeft = true;
        }

        public void releaseRight() {
            this.rightMB = false;
            this.releaseRight = true;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getLastLeftX() {
            return leftX;
        }

        public double getLastLeftY() {
            return leftY;
        }

        public double getLastRightX() {
            return rightX;
        }

        public double getLastRightY() {
            return rightY;
        }

        public boolean isLeftDown() {
            return this.leftMB;
        }

        public boolean isLeftPressed() {
            return this.pressLeft;
        }

        public boolean isLeftReleased() {
            return this.releaseLeft;
        }

        public boolean isLeftDrag() {
            return this.leftMB && (this.leftX != this.x || this.leftY != this.y);
        }

        public boolean isRightDown() {
            return this.rightMB;
        }

        public boolean isRightPressed() {
            return this.pressRight;
        }

        public boolean isRightReleased() {
            return this.releaseRight;
        }

        public boolean isRightDrag() {
            return this.rightMB && (this.rightX != this.x || this.rightY != this.y);
        }

        public void resetPress() {
            this.pressLeft = false;
            this.pressRight = false;
            this.releaseLeft = false;
            this.releaseRight = false;
        }
    }

    public class Scroller {
        private float x, y;
        private float deltaX, deltaY;

        public void scroll(double xOff, double yOff) {
            deltaX = (float) xOff;
            deltaY = (float) yOff;
            x += (float) xOff;
            y += (float) yOff;
        }

        public float getXoffset() {
            return x;
        }

        public float getYoffset() {
            return y;
        }

        public float getDeltaX() {
            return deltaX;
        }

        public float getDeltaY() {
            return deltaY;
        }

        public void resetDelta() {
            deltaX = 0;
            deltaY = 0;
        }
    }

    public Key W = new Key();
    public Key A = new Key();
    public Key S = new Key();
    public Key D = new Key();
    public Key ESC = new Key();

    public Key PLUS = new Key();
    public Key MINUS = new Key();

    public Mouse mouse = new Mouse();

    public Scroller scroller = new Scroller();

    public InputHandler(Game g) {
        long window = g.getWindow().getHandle();
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                boolean isPress = action != GLFW_RELEASE;
                switch (key) {
                    case GLFW_KEY_W:
                        W.toggle(isPress);
                        break;
                    case GLFW_KEY_A:
                        A.toggle(isPress);
                        break;
                    case GLFW_KEY_S:
                        S.toggle(isPress);
                        break;
                    case GLFW_KEY_D:
                        D.toggle(isPress);
                        break;
                    case GLFW_KEY_ESCAPE:
                        ESC.toggle(isPress);
                        break;
                    case GLFW_KEY_KP_ADD:
                        PLUS.toggle(isPress);
                        break;
                    case GLFW_KEY_KP_SUBTRACT:
                        MINUS.toggle(isPress);
                        break;

                }
            }
        });
        glfwSetCursorPosCallback(window, cursorCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                mouse.setPosition(xpos, ypos);
            }
        });
        glfwSetMouseButtonCallback(window, mouseCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    switch (button) {
                        case GLFW_MOUSE_BUTTON_LEFT -> mouse.pressLeft();
                        case GLFW_MOUSE_BUTTON_RIGHT -> mouse.pressRight();
                    }
                } else {
                    switch (button) {
                        case GLFW_MOUSE_BUTTON_LEFT -> mouse.releaseLeft();
                        case GLFW_MOUSE_BUTTON_RIGHT -> mouse.releaseRight();
                    }
                }
            }
        });
        glfwSetScrollCallback(window, scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                scroller.scroll(xoffset, yoffset);
            }
        });
    }

    @Override
    public void tick(int tickCount) {
        //This needs to be the last call during a tick.
        mouse.resetPress();
        scroller.resetDelta();
    }

    public void quit() {
        keyCallback.free();
        mouseCallback.free();
        cursorCallback.free();
        scrollCallback.free();
    }


}
