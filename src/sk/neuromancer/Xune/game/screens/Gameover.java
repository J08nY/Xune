package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.Window;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.level.Level;

import static org.lwjgl.opengl.GL11.*;

public class Gameover implements Tickable, Renderable {
    private final Window window;
    private final InputHandler input;
    private Level level;
    private Menu menu;
    private boolean confirmed;

    public Gameover(Window window, InputHandler input) {
        this.window = window;
        this.input = input;
    }

    @Override
    public void tick(int tickCount) {
        if (menu == null) {
            if (level.getHuman().isEliminated()) {
                menu = new Menu(input, "Game Over", new String[]{"Spectate", "Exit"}, 5f);
            } else {
                menu = new Menu(input, "Game Over", new String[]{"Continue", "Exit"}, 5f);
            }
            menu.activate();
            return;
        }
        menu.tick(tickCount);
        if (input.ENTER.wasPressed()) {
            confirmed = true;
        }
    }

    @Override
    public void render() {
        if (confirmed)
            return;

        glDisable(GL_DEPTH_TEST);
        glBegin(GL_QUADS);
        glColor4f(0.2f, 0.2f, 0.2f, 0.5f);
        glVertex2i(0, 0);
        glVertex2i(window.getWidth(), 0);
        glVertex2i(window.getWidth(), window.getHeight());
        glVertex2i(0, window.getHeight());
        glColor4f(1, 1, 1, 1);
        glEnd();

        //Options
        // If human is alive, allow them to continue
        // If human is dead, allow them to spectate
        // Always allow them to exit
        glPushMatrix();
        glTranslatef(window.getCenterX(), window.getCenterY(), 0);
        if (menu != null)
            menu.render();
        glPopMatrix();

        glEnable(GL_DEPTH_TEST);
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public boolean shouldContinue() {
        return confirmed && menu.getSelected() == 0;
    }

    public boolean shouldExit() {
        return confirmed && menu.getSelected() == 1;
    }
}
