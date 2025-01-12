package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.game.players.Bot;
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
    private int selectedColor;
    private int selectedBot;
    private int column;
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
        InputHandler input = game.getInput();
        if (scaleOld < 3.0f) {
            scaleOld += 0.01f;
            if (input.mouse.isLeftReleased() || input.mouse.isRightReleased() || input.ENTER.wasPressed()) {
                scaleOld = 3.0f;
            }
        } else if (scaleNew < 3.0f) {
            scaleNew += 0.01f;
            if (input.mouse.isLeftReleased() || input.mouse.isRightReleased() || input.ENTER.wasPressed()) {
                scaleNew = 3.0f;
            }
        } else {
            if (worm) {
                wormTicks++;
                if (wormTicks % 20 == 0) {
                    if (wormIndex < SpriteSheet.WORM_SHEET.getWidth()) {
                        wormIndex++;
                    } else {
                        done = true;
                    }
                }
            } else {
                if (input.mouse.isLeftReleased() || input.mouse.isRightReleased() || input.ENTER.wasPressed()) {
                    worm = true;
                    wormTicks = tickCount;
                }
                if (column == 0) {
                    if ((input.W.wasPressed() || input.UP.wasPressed()) && selectedColor > 0) {
                        selectedColor = (selectedColor - 1) % 3;
                    }
                    if ((input.S.wasPressed() || input.DOWN.wasPressed()) && selectedColor < 2) {
                        selectedColor = (selectedColor + 1) % 3;
                    }
                    if (input.D.wasPressed() || input.RIGHT.wasPressed()) {
                        column = 1;
                    }
                } else if (column == 1) {
                    if ((input.W.wasPressed() || input.UP.wasPressed()) && selectedBot > 0) {
                        selectedBot = (selectedBot - 1) % 5;
                    }
                    if ((input.S.wasPressed() || input.DOWN.wasPressed()) && selectedBot < 4) {
                        selectedBot = (selectedBot + 1) % 5;
                    }
                    if (input.A.wasPressed() || input.LEFT.wasPressed()) {
                        column = 0;
                    }
                }
            }
            scaleStart = 0.9f + (float) Math.sin(Math.toRadians(tickCount * 1.7f)) * 0.2f;
        }
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

            glPushMatrix();
            glTranslatef(game.getWindow().getCenterX() / 2, game.getWindow().getCenterY() * 1.5f, 0);
            float height = Text.heightOf("X", 2);
            float w = Text.widthOf(">", 2);
            Text selector = new Text(">", -w, selectedColor * height, true, 2);
            glColor3fv(getSelectedFlag().getColor());
            selector.render();
            glColor3f(1, 1, 1);
            new Text("Flag", 0, -height * 3, true, 2).render();
            new Text("RED", 0, 0, true, 2).render();
            new Text("BLUE", 0, height, true, 2).render();
            new Text("GREEN", 0, 2 * height, true, 2).render();
            if (column == 0) {
                new Text("-----", 0, -height, true, 2).render();
                new Text("-----", 0, 3 * height, true, 2).render();
            }
            glPopMatrix();

            glPushMatrix();
            glTranslatef(game.getWindow().getCenterX() * 0.75f, game.getWindow().getCenterY() * 1.5f, 0);
            Text selectorBot = new Text(">", -w, selectedBot * height, true, 2);
            selectorBot.render();
            new Text("Bot", 0, -height * 3, true, 2).render();
            new Text("Army General", 0, 0, true, 2).render();
            new Text("Buggy Boy", 0, height, true, 2).render();
            new Text("Heli Master", 0, 2 * height, true, 2).render();
            new Text("Jack of All Trades", 0, 3 * height, true, 2).render();
            new Text("Econ Graduate", 0, 4 * height, true, 2).render();
            if (column == 1) {
                new Text("------------------", 0, -height, true, 2).render();
                new Text("------------------", 0, 5 * height, true, 2).render();
            }
            glPopMatrix();
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

    public Flag getSelectedFlag() {
        return Flag.values()[selectedColor];
    }

    public Class<? extends Bot> getSelectedBot() {
        return switch (selectedBot) {
            case 0 -> Bot.ArmyGeneral.class;
            case 1 -> Bot.BuggyBoy.class;
            case 2 -> Bot.HeliMaster.class;
            case 3 -> Bot.JackOfAllTrades.class;
            case 4 -> Bot.EconGraduate.class;
            default -> null;
        };
    }

    public boolean isDone() {
        return done;
    }
}
