package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.gfx.Window;

import static org.lwjgl.opengl.GL11.*;

public interface Clickable {

    enum Button {
        LEFT, RIGHT
    }

    boolean intersects(float x, float y, Button b);

    boolean intersects(float fromX, float fromY, float toX, float toY, Button b);

    void setPosition(float x, float y);

    class ClickableBox implements Clickable {
        private float fromX, fromY;
        private float toX, toY;
        private Button button;
        private boolean isStatic;

        private ClickableBox(float fromX, float fromY, float toX, float toY, Button button, boolean isStatic) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
            this.button = button;
            this.isStatic = isStatic;
        }

        @Override
        public boolean intersects(float x, float y, Button b) {
            if (b != this.button)
                return false;
            return x > fromX && x < toX && y > fromY && y < toY;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY, Button b) {
            if (b != this.button)
                return false;
            boolean xOverlap = this.fromX < toX && this.toX < fromX;
            boolean yOverlap = this.fromY < toY && this.toY < fromY;
            return xOverlap && yOverlap;
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

    class ClickableCircle implements Clickable, Renderable {
        private float x, y;
        private float offsetX, offsetY;
        private float radius;
        private Button button;
        private boolean isStatic;

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
        public boolean intersects(float x, float y, Button b) {
            if (b != this.button)
                return false;
            float dx = (this.x + this.offsetX) - x;
            float dy = (this.y + this.offsetY) - y;
            // TODO: Avoid sqrt, instead square the radius.
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            return dist <= radius;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY, Button b) {
            if (b != this.button)
                return false;
            float cx = x < fromX ? fromX : (x > toX ? toX : x);
            float cy = y < fromY ? fromY : (y > toY ? toY : y);
            float dx = x - cx;
            float dy = y - cy;
            // TODO: Avoid sqrt, instead square the radius.
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
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
            float[] vertices = new float[60];
            for (int i = 0; i < 30; i++) {
                float angle = (float) Math.toRadians(((float) 360 / 30) * i);
                vertices[2 * i] = (float) (Math.cos(angle) * radius);//x;
                vertices[2 * i + 1] = (float) (Math.sin(angle) * radius);//y;
            }
            glColor3f(1f, 0f, 0f);
            Window.renderPoints(vertices);
            glColor3f(1f, 1f, 1f);
            glPopMatrix();
        }

    }

    class ClickableTile implements Clickable, Renderable {
        private float x, y;
        private float width, height;
        private float k, l;
        private float kOffsetTop, kOffsetBottom;
        private float lOffsetTop, lOffsetBottom;
        private float halfWidth, halfHeight;
        private Button button;
        private boolean isStatic;

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
            this.lOffsetBottom = y - l * (x - halfWidth);
            this.button = button;
            this.isStatic = isStatic;
        }

        @Override
        public boolean intersects(float x, float y, Button b) {
            if (b != button)
                return false;
            if (y < k * x + kOffsetTop)
                return false;
            if (y > k * x + kOffsetBottom)
                return false;
            if (y < l * x + lOffsetTop)
                return false;
            return !(y > l * x + lOffsetBottom);
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY, Button b) {
            if (b != button)
                return false;
            // Intersects iff: at least one of the lines intersects the box or the box is fully inside the lines.

            return false;
        }

        public static ClickableTile getFromCoordinates(float fromX, float fromY, float toX, float toY, Button button, boolean isStatic) {
            return new ClickableTile(fromX, fromY, toX - fromX, toY - fromY, button, isStatic);
        }

        public static ClickableTile getFromDimensions(float x, float y, float width, float height, Button button, boolean isStatic) {
            return new ClickableTile(x, y, width, height, button, isStatic);
        }

        @Override
        public void render() {
            SpriteSheet.TILE_SHEET.getSprite(17).render();
            /*
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
            glColor3f(1f, 0f, 0f);//koffsettop
            Window.renderPoints(vertices);
            glColor3f(0f, 1f, 0f);//koffsetbottom
            Window.renderPoints(verticesTwo);
            glColor3f(0f, 0f, 1f);//loffsetbottom
            Window.renderPoints(verticesThree);
            glColor3f(1f, 1f, 1f);//loffsettop
            Window.renderPoints(verticesFour);*/
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
            this.lOffsetBottom = y - l * (x - halfWidth);
        }

    }

}
