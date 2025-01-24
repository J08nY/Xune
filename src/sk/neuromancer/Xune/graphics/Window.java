package sk.neuromancer.Xune.graphics;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long handle;

    private boolean fullscreen;
    private boolean vsync;
    private int width = 0;
    private int height = 0;

    public Window(int width, int height, boolean vsync) {
        this.width = width;
        this.height = height;
        init(false, vsync);
    }

    public Window(int width, int height) {
        this(width, height, true);
    }

    public Window(boolean vsync) {
        init(true, vsync);
    }

    public Window() {
        init(true, true);
    }

    private void init(boolean fullscreen, boolean vsync) {
        this.fullscreen = fullscreen;
        this.vsync = vsync;
        glfwInit();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_FALSE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        glfwWindowHint(GLFW_DEPTH_BITS, 24);

        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);
        if (fullscreen) {
            this.width = vidmode.width();
            this.height = vidmode.height();
            System.out.println("Fullscreen: " + width + "x" + height);
            handle = glfwCreateWindow(width, height, "Xune", monitor, NULL);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
            handle = glfwCreateWindow(width, height, "Xune", NULL, NULL);
            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }
        glfwMakeContextCurrent(handle);
        if (vsync) {
            glfwSwapInterval(1);
        } else {
            glfwSwapInterval(0);
        }

        GL.createCapabilities();

        System.out.println("OpenGL: " + glGetString(GL_VERSION));

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

    public void hide() {
        glfwHideWindow(handle);
    }

    public long getHandle() {
        return handle;
    }

    public int getWidth() {
        return width;
    }

    public float getCenterX() {
        return width / 2.0f;
    }

    public int getHeight() {
        return height;
    }

    public float getCenterY() {
        return height / 2.0f;
    }

    public void quit() {
        glfwDestroyWindow(handle);
    }

    public static int getMaxTextureUnits() {
        int[] maxTextureUnits = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maxTextureUnits);
        return maxTextureUnits[0];
    }
}
