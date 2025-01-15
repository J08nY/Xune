package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.graphics.Renderable;

import static org.lwjgl.opengl.GL11.*;

public class Pause implements Tickable, Renderable {
    private final Game game;
    private final Menu menu;
    private boolean confirmed;

    public Pause(Game game) {
        this.game = game;
        this.menu = new Menu(game.getInput(), "Pause", new String[]{"Continue", "Save", "Exit"}, 5f);
        this.menu.activate();
    }

    @Override
    public void tick(int tickCount) {
        menu.tick(tickCount);
        if (game.getInput().ENTER.wasPressed()) {
            confirmed = true;
        }
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        glBegin(GL_QUADS);
        glColor4f(0.2f, 0.2f, 0.2f, 0.5f);
        glVertex2i(0, 0);
        glVertex2i(game.getWindow().getWidth(), 0);
        glVertex2i(game.getWindow().getWidth(), game.getWindow().getHeight());
        glVertex2i(0, game.getWindow().getHeight());
        glColor4f(1, 1, 1, 1);
        glEnd();

        glPushMatrix();
        glTranslatef(game.getWindow().getCenterX(), game.getWindow().getCenterY(), 0);
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

    public boolean shouldExit() {
        return confirmed && menu.getSelected() == 2;
    }

    public void reset() {
        confirmed = false;
        menu.setSelected(0);
    }
}
