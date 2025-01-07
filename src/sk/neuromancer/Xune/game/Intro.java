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
    private boolean worm;
    private int wormIndex;
    private int wormTicks;
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
                if (!worm) {
                    worm = true;
                    wormTicks = tickCount;
                }
            }
            if (worm) {
                wormTicks++;
            }
            if (worm && wormTicks % 20 == 0) {
                if (wormIndex < SpriteSheet.WORM_SHEET.getWidth()) {
                    wormIndex++;
                } else {
                    done = true;
                }
            }
        }
        scaleStart = 0.9f + (float) Math.sin(Math.toRadians(tickCount * 1.7f)) * 0.2f;
    }

    @Override
    public void render() {
        if (scaleNew >= 3.0f) {
            glPushMatrix();
            float width = Text.widthOf("Click to start", scaleStart + 3);
            glTranslatef(game.getWindow().getCenterX(), game.getWindow().getCenterY() + newLogo.getHeight() * 2f, 0);
            new Text("Click to start", -(width / 2), 0, true, scaleStart + 3).render();

            if (worm) {
                glPushMatrix();
                glScalef(50, 50, 1);
                glTranslatef((float) -(SpriteSheet.WORM_SHEET.getSpriteWidth() / 2) - 7 + wormIndex, (float) -SpriteSheet.WORM_SHEET.getSpriteHeight() / 2, 0);
                glBegin(GL_QUADS);
                glColor3f(0, 0, 0);
                glVertex2f(0, 0);
                glVertex2f(SpriteSheet.WORM_SHEET.getSpriteWidth() * ((float) wormIndex / SpriteSheet.WORM_SHEET.getWidth()), 0);
                glVertex2f(SpriteSheet.WORM_SHEET.getSpriteWidth() * ((float) wormIndex / SpriteSheet.WORM_SHEET.getWidth()), SpriteSheet.WORM_SHEET.getSpriteHeight());
                glVertex2f(0, SpriteSheet.WORM_SHEET.getSpriteHeight());
                glColor3f(1, 1, 1);
                glEnd();
                if (wormIndex < SpriteSheet.WORM_SHEET.getWidth())
                    SpriteSheet.WORM_SHEET.getSprite(wormIndex, 2).render();
                glPopMatrix();
            }
            glPopMatrix();

//            glPushMatrix();
//            glTranslatef(game.getWindow().getCenterX() / 2, game.getWindow().getCenterY() * 1.5f, 0);
//            float height = Text.heightOf("X", 2);
//            new Text("RED", 0, 0, true, 2).render();
//            new Text("BLUE", 0, height, true, 2).render();
//            new Text("GREEN", 0, 2 * height, true, 2).render();
//            glPopMatrix();
        }

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
            glPopMatrix();
        }

    }

    public boolean isDone() {
        return done;
    }
}
