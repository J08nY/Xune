package sk.neuromancer.Xune.gfx;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.opengl.GL11.*;

import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Sprite.ScalableSprite;

public class HUD implements Tickable, Renderable {
    private Game game;

    private ScalableSprite currentCursor;
    private ScalableSprite logo;
    private ScalableSprite hudPanel;

    private double mouseX, mouseY;
    private double fromX, fromY;
    private boolean drag;

    public HUD(Game game) {
        this.game = game;
        glfwSetInputMode(game.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(3);
        currentCursor.setScaleFactor(2f);

        logo = SpriteSheet.LOGO.getSprite(0);
        logo.setScaleFactor(1f);

        hudPanel = SpriteSheet.HUD_PANEL.getSprite(0);
        hudPanel.setScaleFactor(Game.WIDTH / (float) hudPanel.getWidth());
        fromX = fromY = 0;
    }

    @Override
    public void render() {
        if (drag) {
            glPushMatrix();
            glBegin(GL_QUADS);
            glColor4f(0f, 1f, 0f, 0.2f);
            glVertex3d(fromX, fromY, 0);
            glVertex3d(fromX, mouseY, 0);
            glVertex3d(mouseX, mouseY, 0);
            glVertex3d(mouseX, fromY, 0);
            glEnd();
            glColor4f(1.f, 1.f, 1.f, 1.f);
            glPopMatrix();
        }

        glPushMatrix();
        glTranslated(0, Game.HEIGHT - (hudPanel.getHeight() * hudPanel.getScaleFactor()), 0);
        hudPanel.render();
        glPopMatrix();

        glPushMatrix();
        float hudTop = Game.HEIGHT - (hudPanel.getHeight() * hudPanel.getScaleFactor());
        float hudLeft = (hudPanel.getWidth() * hudPanel.getScaleFactor()) * 0.18f;
        glTranslatef(hudLeft, hudTop, 0);

        renderText(0, 60, "MONEY: " + game.getLevel().getPlayer().money);
        float levelX = game.getLevel().getLevelX(mouseX);
        float levelY = game.getLevel().getLevelY(mouseY);

        renderText(0, 90, "X: " + mouseX);
        renderText(0, 120, "Y: " + mouseY);
        renderText(200, 90, "LEVELX: " + levelX);
        renderText(200, 120, "LEVELY: " + levelY);
        renderText(400, 90, "XOFF: " + game.getLevel().xOff);
        renderText(400, 120, "YOFF: " + game.getLevel().yOff);
        renderText(600, 90, "ZOOM: " + game.getLevel().zoom);

        glPopMatrix();

        glPushMatrix();
        glTranslated(mouseX - (currentCursor.getWidth() * currentCursor.getScaleFactor()) / 2, mouseY - (currentCursor.getHeight() * currentCursor.getScaleFactor()) / 2, 0);
        currentCursor.render();
        glPopMatrix();

        logo.render();
    }

    @Override
    public void tick(int tickCount) {
        mouseX = game.getInput().mouse.getX();
        mouseY = game.getInput().mouse.getY();

        if (mouseX < -20) {
            mouseX = -20;

        } else if (mouseX > Game.WIDTH + 20) {
            mouseX = Game.WIDTH + 20;
        }
        if (mouseY < -20) {
            mouseY = -20;
        } else if (mouseY > Game.HEIGHT + 20) {
            mouseY = Game.HEIGHT + 20;
        }
        glfwSetCursorPos(game.getWindow(), mouseX, mouseY);

        if (game.getInput().mouse.isLeftDrag()) {
            fromX = game.getInput().mouse.getLastLeftX();
            fromY = game.getInput().mouse.getLastLeftY();
            drag = true;
        } else {
            drag = false;
        }
    }

    private void renderText(float x, float y, String text) {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ  0123456789.,!?'\"-+=/\\%()<>:;";
        float textScaleFactor = SpriteSheet.TEXT_SHEET.getSprite(0).getScaleFactor();
        float scaledSpriteWidth = SpriteSheet.TEXT_SHEET.getSprite(0).getWidth() * textScaleFactor;
        text = text.toUpperCase();
        for (int i = 0; i < text.length(); i++) {
            int spriteId = charset.indexOf(text.charAt(i));
            glPushMatrix();
            glTranslated(x + scaledSpriteWidth * i, y, 0);
            SpriteSheet.TEXT_SHEET.getSprite(spriteId).render();
            glPopMatrix();
        }
    }

}
