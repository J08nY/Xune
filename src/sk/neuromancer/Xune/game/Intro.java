package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.gfx.Renderable;
import sk.neuromancer.Xune.gfx.Sprite;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.gfx.Text;

import static org.lwjgl.opengl.GL11.*;

public class Intro implements Tickable, Renderable {
    private Game game;
    private float scaleOld, scaleNew, scaleStart;
    private final Sprite oldLogo;
    private final Sprite newLogo;
    private boolean done;

    public Intro(Game game) {
        this.game = game;
        this.scaleOld = 0.1f;
        this.scaleNew = 0.1f;
        this.oldLogo = SpriteSheet.TITLE.getSprite(0);
        this.newLogo = SpriteSheet.TITLE.getSprite(1);
    }

    @Override
    public void tick(int tickCount) {
        if (scaleOld < 3.0f) {
            scaleOld += 0.01f;
        } else if (scaleNew < 3.0f) {
            scaleNew += 0.01f;
        } else {
            if (game.getInput().mouse.isLeftReleased() || game.getInput().mouse.isRightReleased()) {
                done = true;
            }
        }
        scaleStart = 0.9f + (float) Math.sin(Math.toRadians(tickCount * 1.5f)) * 0.1f;
    }

    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(game.getWindow().getCenterX() - (oldLogo.getWidth() * scaleOld) / 2, game.getWindow().getCenterY() - (oldLogo.getHeight() * 2 * scaleOld) / 2, 0);
        glScalef(scaleOld, scaleOld, 1);
        oldLogo.render();
        glPopMatrix();

        if (scaleOld >= 3.0f) {
            glPushMatrix();
            glTranslatef(game.getWindow().getCenterX() - (newLogo.getWidth() * scaleNew) / 2, game.getWindow().getCenterY() - (newLogo.getHeight() * scaleNew) / 2, 0);
            glScalef(scaleNew, scaleNew, 1);
            newLogo.render();
            if (scaleNew >= 3.0f) {
                float width = Text.widthOf("Click to start", scaleStart);
                new Text("Click to start", (newLogo.getWidth() - width) / 2, newLogo.getHeight(), true, scaleStart).render();
            }
            glPopMatrix();
        }
    }

    public boolean isDone() {
        return done;
    }
}
