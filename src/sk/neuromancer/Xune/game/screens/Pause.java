package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.input.InputHandler;

import static org.lwjgl.opengl.GL11.*;

public class Pause implements Tickable, Renderable {
    private final Window window;
    private final InputHandler input;
    private final Menu menu;
    private boolean confirmed;

    public Pause(Window window, InputHandler input) {
        this.window = window;
        this.input = input;
        this.menu = new Menu(input, "Pause", new String[]{"Continue", "Save", "Load", "Exit"}, 5f);
        this.menu.activate();
    }

    @Override
    public void tick(int tickCount) {
        menu.tick(tickCount);
        if (input.ENTER.wasPressed()) {
            confirmed = true;
        }
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_QUADS);
        glColor4f(0.2f, 0.2f, 0.2f, 0.5f);
        glVertex2i(0, 0);
        glVertex2i(window.getWidth(), 0);
        glVertex2i(window.getWidth(), window.getHeight());
        glVertex2i(0, window.getHeight());
        glColor4f(1, 1, 1, 1);
        glEnd();

        glPushMatrix();
        glTranslatef(window.getCenterX(), window.getCenterY(), 0);
        menu.render();
        glPopMatrix();

        glEnable(GL_DEPTH_TEST);
    }

    public boolean shouldContinue() {
        return confirmed && menu.getSelected() == 0;
    }

    public boolean shouldSave() {
        return confirmed && menu.getSelected() == 1;
    }

    public boolean shouldLoad() {
        return confirmed && menu.getSelected() == 2;
    }

    public boolean shouldExit() {
        return confirmed && menu.getSelected() == 3;
    }

    public void reset() {
        confirmed = false;
        menu.setSelected(0);
    }
}
