package sk.neuromancer.Xune.level.paths;

import sk.neuromancer.Xune.gfx.Renderable;

import static org.lwjgl.opengl.GL11.*;

public class Path implements Renderable {
    private final Point[] p;

    public Path(Point[] p) {
        this.p = p;
    }

    public Point[] getPoints() {
        return p;
    }

    public Point getStart() {
        return p[0];
    }

    public Point getEnd() {
        return p[p.length - 1];
    }

    @Override
    public void render() {
        glEnable(GL_POINT_SMOOTH);
        glPointSize(10);
        glBegin(GL_POINTS);
        glColor4f(0, 0, 0, 0.4f);
        for (Point point : p) {
            float x = point.getLevelX();
            float y = point.getLevelY();
            glVertex3f(x, y, 0);
        }
        glColor4f(1, 1, 1, 1);
        glEnd();
        glDisable(GL_POINT_SMOOTH);
    }
}
