package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;

import static org.lwjgl.opengl.GL11.*;

public class Gameover implements Tickable, Renderable {
    private final Game game;
    private Menu menu;
    private boolean confirmed;

    public Gameover(Game game) {
        this.game = game;
    }

    @Override
    public void tick(int tickCount) {
        if (menu == null) {
            if (game.getLevel().getHuman().isEliminated()) {
                menu = new Menu(game.getInput(), "Game Over", new String[]{"Spectate", "Exit"}, 5f);
            } else {
                menu = new Menu(game.getInput(), "Game Over", new String[]{"Continue", "Exit"}, 5f);
            }
            menu.activate();
            return;
        }
        menu.tick(tickCount);
        if (game.getInput().ENTER.wasPressed()) {
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
        glVertex2i(game.getWindow().getWidth(), 0);
        glVertex2i(game.getWindow().getWidth(), game.getWindow().getHeight());
        glVertex2i(0, game.getWindow().getHeight());
        glColor4f(1, 1, 1, 1);
        glEnd();

        //Options
        // If human is alive, allow them to continue
        // If human is dead, allow them to spectate
        // Always allow them to exit
        glPushMatrix();
        glTranslatef(game.getWindow().getCenterX(), game.getWindow().getCenterY(), 0);
        menu.render();
        glPopMatrix();

        glEnable(GL_DEPTH_TEST);
    }

    public boolean shouldContinue() {
        return confirmed && menu.getSelected() == 0;
    }

    public boolean shouldExit() {
        return confirmed && menu.getSelected() == 1;
    }
}
