package sk.neuromancer.Xune.gfx;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long handle;

    private final int width;
    private final int height;

    public Window(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        glfwInit();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        long monitor = glfwGetPrimaryMonitor();
        handle = glfwCreateWindow(width, height, "Xune", NULL, NULL);

        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        glfwSetWindowPos(
                handle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );
        glfwMakeContextCurrent(handle);
        //glfwSwapInterval(1);

        GL.createCapabilities();

        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);

        //glEnable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0f, 0f, 0f, 1.0f);
    }

    public void show() {
        glfwShowWindow(handle);
    }

    public long getHandle() {
        return handle;
    }

    public void quit() {
        glfwDestroyWindow(handle);
    }

    public static void renderPoints(float[] vertexArray) {
        if (vertexArray.length % 2 != 0)
            return;
        glPushMatrix();
        //glColor3f(0f,1f,0f);
        glPointSize(10.0f);
        glBegin(GL_POINTS);
        for (int i = 0; i < vertexArray.length / 2; i++) {
            glVertex2f(vertexArray[2 * i], vertexArray[2 * i + 1]);
        }
        glEnd();
        glPopMatrix();
        glColor3f(1f, 1f, 1f);
    }
}
