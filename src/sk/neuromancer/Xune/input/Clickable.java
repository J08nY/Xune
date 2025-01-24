package sk.neuromancer.Xune.input;

import sk.neuromancer.Xune.graphics.Renderable;

import static org.lwjgl.opengl.GL11.*;

public interface Clickable {

    boolean intersects(float x, float y);

    boolean intersects(float fromX, float fromY, float toX, float toY);

    void setPosition(float x, float y);

    class ClickableBox implements Clickable, Renderable {
        private float fromX, fromY;
        private float toX, toY;

        private ClickableBox(float fromX, float fromY, float toX, float toY) {
            this.fromX = fromX;
            this.fromY = fromY;
            this.toX = toX;
            this.toY = toY;
        }

        @Override
        public boolean intersects(float x, float y) {
            return x > fromX && x < toX && y > fromY && y < toY;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY) {
            boolean xOverlap = this.fromX < toX && this.toX < fromX;
            boolean yOverlap = this.fromY < toY && this.toY < fromY;
            return xOverlap && yOverlap;
        }

        @Override
        public void setPosition(float x, float y) {
            float width = toX - fromX;
            float height = toY - fromY;
            fromX = x;
            fromY = y;
            toX = x + width;
            toY = y + height;
        }

        public static ClickableBox getFromCoordinates(float fromX, float fromY, float toX, float toY) {
            return new ClickableBox(fromX, fromY, toX, toY);
        }

        public static ClickableBox getFromDimensions(float x, float y, float width, float height) {
            return new ClickableBox(x, y, x + width, y + height);
        }

        public static ClickableBox getCentered(float x, float y, float width, float height) {
            float halfWidth = width / 2;
            float halfHeight = height / 2;
            return new ClickableBox(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight);
        }

        @Override
        public void render() {
            glColor3f(1f, 0f, 0f);
            glBegin(GL_LINE_LOOP);
            glVertex2f(fromX, fromY);
            glVertex2f(toX, fromY);
            glVertex2f(toX, toY);
            glVertex2f(fromX, toY);
            glEnd();
            glColor3f(1f, 1f, 1f);
        }
    }

    class ClickableCircle implements Clickable, Renderable {
        private float x, y;
        private float radius;

        private ClickableCircle(float x, float y, float radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        @Override
        public boolean intersects(float x, float y) {
            float dx = this.x - x;
            float dy = this.y - y;
            float dist = dx * dx + dy * dy;
            return dist <= radius * radius;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY) {
            float cx = x < fromX ? fromX : (x > toX ? toX : x);
            float cy = y < fromY ? fromY : (y > toY ? toY : y);
            float dx = x - cx;
            float dy = y - cy;
            float dist = dx * dx + dy * dy;
            return dist <= radius * radius;
        }

        @Override
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public static ClickableCircle getCentered(float x, float y, float radius) {
            return new ClickableCircle(x, y, radius);
        }

        public static ClickableCircle getFromDimensions(float x, float y, float width, float height) {
            if (width != height)
                return null;
            float centerX = x + (width / 2);
            float centerY = y + (height / 2);
            return new ClickableCircle(centerX, centerY, width / 2);
        }

        @Override
        public void render() {
            glColor3f(1f, 0f, 0f);
            glLineWidth(4.0f);
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
            glBegin(GL_LINE_LOOP);
            int edges = 15;
            for (int i = 0; i < edges; i++) {
                double angle = Math.toRadians(((double) 360 / edges) * i);
                glVertex2d(Math.cos(angle) * radius, Math.sin(angle) * radius);
            }
            glEnd();
            glDisable(GL_LINE_SMOOTH);
            glColor3f(1f, 1f, 1f);
        }
    }

    class ClickableTile implements Clickable, Renderable {
        private float x, y;
        private float width, height;
        private float k, l;
        private float kOffsetTop, kOffsetBottom;
        private float lOffsetTop, lOffsetBottom;
        private float halfWidth, halfHeight;

        private ClickableTile(float x, float y, float width, float height) {
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
        }

        @Override
        public boolean intersects(float x, float y) {
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

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY) {
            //line 0 -> (x, y + halfHeight) to (x + halfWidth, y)
            if (liangBarsky(x, y + halfHeight, x + halfWidth, y, fromX, fromY, toX, toY))
                return true;
            //line 1 -> (x + halfWidth, y) to (x + width, y + halfHeight)
            if (liangBarsky(x + halfWidth, y, x + width, y + halfHeight, fromX, fromY, toX, toY))
                return true;
            //line 2 -> (x + width, y + halfHeight) to (x + halfWidth, y + height)
            if (liangBarsky(x + width, y + halfHeight, x + halfWidth, y + height, fromX, fromY, toX, toY))
                return true;
            //line 3 -> (x + halfWidth, y + height) to (x, y + halfHeight)
            if (liangBarsky(x + halfWidth, y + height, x, y + halfHeight, fromX, fromY, toX, toY))
                return true;
            return false;
        }

        private boolean liangBarsky(float lineFromX, float lineFromY, float lineToX, float lineToY,
                                    float boxFromX, float boxFromY, float boxToX, float boxToY) {
            float dx = lineToX - lineFromX;
            float dy = lineToY - lineFromY;
            float[] p = {-dx, dx, -dy, dy};
            float[] q = {lineFromX - boxFromX, boxToX - lineFromX, lineFromY - boxFromY, boxToY - lineFromY};
            float u1 = 0;
            float u2 = 1;
            for (int i = 0; i < 4; i++) {
                if (p[i] == 0) {
                    if (q[i] < 0) {
                        return false;
                    }
                } else {
                    float r = q[i] / p[i];
                    if (p[i] < 0) {
                        u1 = Math.max(u1, r);
                    } else {
                        u2 = Math.min(u2, r);
                    }
                }
            }
            if (u1 > u2) {
                return false;
            }
            return true;
        }

        public static ClickableTile getFromCoordinates(float fromX, float fromY, float toX, float toY) {
            return new ClickableTile(fromX, fromY, toX - fromX, toY - fromY);
        }

        public static ClickableTile getFromDimensions(float x, float y, float width, float height) {
            return new ClickableTile(x, y, width, height);
        }

        public static ClickableTile getCentered(float x, float y, float width, float height) {
            float halfWidth = width / 2;
            float halfHeight = height / 2;
            return new ClickableTile(x - halfWidth, y - halfHeight, width, height);
        }

        @Override
        public void render() {
            glColor3f(1f, 0f, 0f);
            glPushMatrix();
            glLineWidth(4.0f);
            glTranslatef(-halfWidth, -halfHeight, 0);
            glBegin(GL_LINE_LOOP);
            glVertex2f(0, halfHeight);
            glVertex2f(halfWidth, 0);
            glVertex2f(width, halfHeight);
            glVertex2f(halfWidth, height);
            glEnd();
            glPopMatrix();
            glColor3f(1f, 1f, 1f);
        }

        @Override
        public void setPosition(float x, float y) {
            System.out.println("Setting clickable position to " + x + ", " + y);
            x -= halfWidth;
            y -= halfHeight;
            this.x = x;
            this.y = y;
            this.kOffsetTop = y - k * (x + halfWidth);
            this.kOffsetBottom = y + height - k * (x + halfWidth);
            this.lOffsetTop = (y - l * (x + halfWidth));
            this.lOffsetBottom = y - l * (x - halfWidth);
        }

    }

}
