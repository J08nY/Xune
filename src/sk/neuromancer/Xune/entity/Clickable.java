package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Window;

import static org.lwjgl.opengl.GL11.*;

public interface Clickable {

    public enum Button {
        LEFT, RIGHT;
    }

    public boolean onClick(float x, float y, Button b);

    public void setPosition(float x, float y);//mozno move?


    public class ClickableBox implements Clickable {
        private float fromX, fromY;
        private float toX, toY;
        private Button button;
        private boolean isStatic;

        private ClickableBox() {
        }

        private ClickableBox(float fromX, float fromY, float toX, float toY, Button button, boolean isStatic) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.button = button;
            this.isStatic = isStatic;
        }

        @Override
        public boolean onClick(float x, float y, Button b) {
            if (b != this.button)
                return false;
            if (x > fromX && x < toX && y > fromY && y < toY) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setPosition(float x, float y) {
            if (isStatic)
                return;
            float width = toX - fromX;
            float height = toY - fromY;
            fromX = x;
            fromY = y;
            toX = x + width;
            toY = y + height;
        }

        public static ClickableBox getFromCoordinates(float fromX, float fromY, float toX, float toY, Button button, boolean isStatic) {
            return new ClickableBox(fromX, fromY, toX, toY, button, isStatic);
        }

        public static ClickableBox getFromDimensions(float x, float y, float width, float height, Button button, boolean isStatic) {
            return new ClickableBox(x, y, x + width, y + height, button, isStatic);
        }

        public static ClickableBox getCentered(float x, float y, float width, float height, Button button, boolean isStatic) {
            float halfWidth = width / 2;
            float halfHeight = height / 2;
            return new ClickableBox(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight, button, isStatic);
        }

    }

    public class ClickableCircle implements Clickable, Renderable {
        private float x, y;
        private float offsetX, offsetY;
        private float radius;
        private Button button;
        private boolean isStatic;

        private ClickableCircle() {
        }

        private ClickableCircle(float x, float y, float offsetX, float offsetY, float radius, Button button, boolean isStatic) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.button = button;
            this.isStatic = isStatic;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        public boolean onClick(float x, float y, Button b) {
            if (b != this.button)
                return false;
            float dx = (this.x + this.offsetX) - x;
            float dy = (this.y + this.offsetY) - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            System.out.println(dist);
            return dist <= radius;
        }

        @Override
        public void setPosition(float x, float y) {
            if (isStatic)
                return;
            this.x = x;
            this.y = y;
        }

        public static ClickableCircle getCentered(float x, float y, float offsetX, float offsetY, float radius, Button button, boolean isStatic) {
            return new ClickableCircle(x, y, offsetX, offsetY, radius, button, isStatic);
        }

        public static ClickableCircle getFromDimensions(float x, float y, float width, float height, Button button, boolean isStatic) {
            if (width != height)
                return null;
            float centerX = x + (width / 2);
            float centerY = y + (height / 2);
            return new ClickableCircle(centerX, centerY, 0, 0, width / 2, button, isStatic);
        }

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(offsetX, offsetY, 0);
            float[] vertices = new float[30];
            for (int i = 0; i < 15; i++) {
                float angle = (360 / 15) * i;
                vertices[2 * i] = (float) (Math.cos(angle) * radius);//x;
                vertices[2 * i + 1] = (float) (Math.sin(angle) * radius);//y;
            }
            glColor3f(1f, 0f, 0f);
            Window.renderPoints(vertices);
            glColor3f(1f, 1f, 1f);
            glPopMatrix();;
        }

    }

    public class ClickableTile implements Clickable, Renderable {
        private float x, y;
        private float width, height;
        private float k, l;
        private float kOffsetTop, kOffsetBottom;
        private float lOffsetTop, lOffsetBottom;
        private float halfWidth, halfHeight;
        private Button button;
        private boolean isStatic;

        private ClickableTile() {
        }

        private ClickableTile(float x, float y, float width, float height, Button button, boolean isStatic) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            float halfWidth = width / 2;
            float halfHeight = height / 2;
            this.halfWidth = halfWidth;
            this.halfHeight = halfHeight;
            this.k = (-halfHeight) / (halfWidth);
            this.l = (halfHeight) / (halfWidth);
            this.kOffsetTop = y - k * (x + halfWidth);
            this.kOffsetBottom = y + height - k * (x + halfWidth);
            this.lOffsetTop = (y - l * (x + halfWidth));
            this.lOffsetBottom = -(y + 5 * height - l * (x - halfWidth));
            this.button = button;
            this.isStatic = isStatic;
        }

        @Override
        public boolean onClick(float x, float y, Button b) {
            if (b != button)
                return false;
            if (y < k * x + kOffsetTop)
                return false;
            if (y > k * x + kOffsetBottom)
                return false;
            if (y < l * x + lOffsetTop)
                return false;
            if (y > l * x + lOffsetBottom)
                return false;
            return true;
        }

        public static ClickableTile getFromCoordinates(float fromX, float fromY, float toX, float toY, Button button, boolean isStatic) {
            return new ClickableTile(fromX, fromY, toX - fromX, toY - fromY, button, isStatic);
        }

        public static ClickableTile getFromDimensions(float x, float y, float width, float height, Button button, boolean isStatic) {
            return new ClickableTile(x, y, width, height, button, isStatic);
        }

        @Override
        public void render() {
            //TODO: Fix
            float[] vertices = new float[60];
            for (int i = 0; i < 30; i++) {
                vertices[2 * i] = 2 * i;
                vertices[2 * i + 1] = k * (2 * i) + kOffsetTop;
            }
            float[] verticesTwo = new float[60];
            for (int i = 0; i < 30; i++) {
                verticesTwo[2 * i] = 2 * i;
                verticesTwo[2 * i + 1] = k * (2 * i) + kOffsetBottom;
            }
            float[] verticesThree = new float[60];
            for (int i = 0; i < 30; i++) {
                verticesThree[2 * i] = 2 * i;
                verticesThree[2 * i + 1] = l * (2 * i) + lOffsetTop;
            }
            float[] verticesFour = new float[60];
            for (int i = 0; i < 30; i++) {
                verticesFour[2 * i] = 2 * i;
                verticesFour[2 * i + 1] = l * (2 * i) + lOffsetBottom;
            }
            //float[] finishedVertices = new float[120];
            //System.arraycopy(vertices, 0, finishedVertices, 0, 30);
            //System.arraycopy(verticesTwo, 0, finishedVertices, 30, 30);
            //System.arraycopy(verticesThree, 0, finishedVertices, 60, 30);
            //System.arraycopy(verticesFour, 0, finishedVertices, 90, 30);
            glColor3f(1f, 0f, 0f);//koffsettop
            Window.renderPoints(vertices);
            glColor3f(0f, 1f, 0f);//koffsetbottom
            Window.renderPoints(verticesTwo);
            glColor3f(0f, 0f, 1f);//loffsetbottom
            Window.renderPoints(verticesThree);
            glColor3f(1f, 1f, 1f);//loffsettop
            Window.renderPoints(verticesFour);
        }

        @Override
        public void setPosition(float x, float y) {
            if (isStatic)
                return;
            this.x = x;
            this.y = y;
            this.kOffsetTop = y - k * (x + halfWidth);
            this.kOffsetBottom = y + height - k * (x + halfWidth);
            this.lOffsetTop = (y - l * (x + halfWidth));
            this.lOffsetBottom = -(y + 2 * height - l * (x + halfWidth));
        }

    }

}
