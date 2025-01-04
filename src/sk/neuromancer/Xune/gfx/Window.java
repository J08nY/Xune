package sk.neuromancer.Xune.gfx;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long handle;

    private boolean fullscreen = false;
    private int width = 0;
    private int height = 0;

    public Window(int width, int height) {
        this.width = width;
        this.height = height;
        init(false);
    }

    public Window() {
        init(true);
    }

    private void init(boolean fullscreen) {
        this.fullscreen = fullscreen;
        glfwInit();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (fullscreen) {
            this.width = vidmode.width();
            this.height = vidmode.height();
            handle = glfwCreateWindow(width, height, "Xune", monitor, NULL);
        } else {
            handle = glfwCreateWindow(width, height, "Xune", NULL, NULL);
            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }
        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glViewport(0, 0, width, height);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.1f);

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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void quit() {
        glfwDestroyWindow(handle);
    }
}
