package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Text;

import static org.lwjgl.opengl.GL11.*;

public class Pause implements Tickable, Renderable {
    private final Game game;
    private int selected = 0;
    private boolean confirmed;

    public Pause(Game game) {
        this.game = game;
    }

    @Override
    public void tick(int tickCount) {
        InputHandler input = game.getInput();
        if ((input.W.wasPressed() || input.UP.wasPressed()) && selected > 0) {
            selected = (selected - 1) % 2;
        }
        if ((input.S.wasPressed() || input.DOWN.wasPressed()) && selected < 1) {
            selected = (selected + 1) % 2;
        }
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
        glVertex2i(game.getWindow().getWidth(), 0);
        glVertex2i(game.getWindow().getWidth(), game.getWindow().getHeight());
        glVertex2i(0, game.getWindow().getHeight());
        glColor4f(1, 1,1, 1);
        glEnd();

        glTranslatef(game.getWindow().getCenterX(), game.getWindow().getCenterY(), 0);
        Text contText = new Text((selected == 0 ? "> ": "") + "Continue", true, 5f);
        Text exitText = new Text((selected == 1 ? "> ": "") + "Exit", true, 5f);
        contText.render();
        glTranslatef(0, contText.getHeight(), 0);
        exitText.render();

        glEnable(GL_DEPTH_TEST);
    }

    public boolean shouldContinue() {
        return confirmed && selected == 0;
    }

    public boolean shouldExit() {
        return confirmed && selected == 1;
    }

    public void reset() {
        confirmed = false;
        selected = 0;
    }
}
