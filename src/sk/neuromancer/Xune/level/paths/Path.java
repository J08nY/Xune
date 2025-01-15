package sk.neuromancer.Xune.level.paths;

import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.proto.BaseProto;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

public class Path implements Renderable {
    private final Point[] p;

    public Path(Point[] p) {
        this.p = p;
    }

    public Path(BaseProto.Path savedPath) {
        this.p = new Point[savedPath.getPointsCount()];
        for (int i = 0; i < savedPath.getPointsCount(); i++) {
            this.p[i] = new Point(savedPath.getPoints(i));
        }
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

    @Override
    public String toString() {
        return "Path{" +
                "p=" + Arrays.toString(p) +
                '}';
    }

    public BaseProto.Path serialize() {
        BaseProto.Path.Builder builder = BaseProto.Path.newBuilder();
        for (Point point : p) {
            builder.addPoints(point.serialize());
        }
        return builder.build();
    }
}
