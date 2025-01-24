package sk.neuromancer.Xune.game.screens;

import sk.neuromancer.Xune.entity.Flag;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.game.players.Bot;
import sk.neuromancer.Xune.graphics.Renderable;
import sk.neuromancer.Xune.graphics.elements.Sprite;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.graphics.elements.Text;

import static org.lwjgl.opengl.GL11.*;

public class Intro implements Tickable, Renderable {
    private final Game game;
    private float scaleOld, scaleNew, scaleStart;
    private final Sprite oldLogo;
    private final Sprite newLogo;
    private boolean worm;
    private int wormIndex;
    private int wormTicks;

    private final Menu flagMenu;
    private final Menu botMenu;
    private final Menu loadMenu;

    private int column;

    private boolean done;

    public Intro(Game game) {
        this.game = game;
        this.scaleOld = 0.1f;
        this.scaleNew = 0.1f;
        this.oldLogo = SpriteSheet.TITLE.getSprite(0);
        this.newLogo = SpriteSheet.TITLE.getSprite(1);
        this.flagMenu = new Menu(game.getInput(), "Flag", new String[]{"RED", "BLUE", "GREEN"}, 2f);
        this.botMenu = new Menu(game.getInput(), "Bot", new String[]{"Army General", "Buggy Boy", "Heli Master", "Jack of All Trades", "Econ Graduate"}, 2f);
        this.loadMenu = new Menu(game.getInput(), "", new String[]{"Load Game"}, 2f);
        this.flagMenu.activate();
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
                    flagMenu.tick(tickCount);
                    if (input.D.wasPressed() || input.RIGHT.wasPressed()) {
                        flagMenu.deactivate();
                        botMenu.activate();
                        column = 1;
                    }
                } else if (column == 1) {
                    botMenu.tick(tickCount);
                    if (input.A.wasPressed() || input.LEFT.wasPressed()) {
                        botMenu.deactivate();
                        flagMenu.activate();
                        column = 0;
                    }
                    if (input.D.wasPressed() || input.RIGHT.wasPressed()) {
                        botMenu.deactivate();
                        loadMenu.activate();
                        column = 2;
                    }
                } else if (column == 2) {
                    loadMenu.tick(tickCount);
                    if (input.A.wasPressed() || input.LEFT.wasPressed()) {
                        loadMenu.deactivate();
                        botMenu.activate();
                        column = 1;
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
            glTranslatef(game.getWindow().getCenterX() * 0.5f, game.getWindow().getCenterY() * 1.5f, 0);
            flagMenu.render();
            glPopMatrix();

            glPushMatrix();
            glTranslatef(game.getWindow().getCenterX() * 1.0f, game.getWindow().getCenterY() * 1.5f, 0);
            botMenu.render();
            glPopMatrix();

            glPushMatrix();
            glTranslatef(game.getWindow().getCenterX() * 1.5f, game.getWindow().getCenterY() * 1.5f, 0);
            loadMenu.render();
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
        return Flag.values()[flagMenu.getSelected()];
    }

    public Class<? extends Bot> getSelectedBot() {
        return switch (botMenu.getSelected()) {
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

    public boolean shouldPlayNew() {
        return done && (column == 0 || column == 1);
    }

    public boolean shouldLoad() {
        return done && column == 2;
    }
}
