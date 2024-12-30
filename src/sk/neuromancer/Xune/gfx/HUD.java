package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.InputHandler;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.gfx.Sprite.ScalableSprite;
import sk.neuromancer.Xune.level.Level;

import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class HUD implements Tickable, Renderable {
    private final Game game;
    private final float width;
    private final float height;

    private ScalableSprite currentCursor;
    private final ScalableSprite logo;
    private final ScalableSprite hudPanel;

    private double mouseX, mouseY;
    private double fromX, fromY;
    private boolean drag;

    public HUD(Game game) {
        this.game = game;
        this.width = game.getWindow().getWidth();
        this.height = game.getWindow().getHeight();
        glfwSetInputMode(game.getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        // TODO: This does nothing. Why?
        glfwSetCursorPos(game.getWindow().getHandle(), (double) width / 2, (double) height / 2);
        currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);

        logo = SpriteSheet.LOGO.getSprite(0);
        logo.setScaleFactor(1);

        hudPanel = SpriteSheet.HUD_PANEL.getSprite(0);
        hudPanel.setScaleFactor(width / (float) hudPanel.getWidth());
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
        float hudTop = height - (hudPanel.getHeight() * hudPanel.getScaleFactor());
        float hudLeft = (hudPanel.getWidth() * hudPanel.getScaleFactor()) * 0.18f;

        // Render HUD panel
        glPushMatrix();
        glTranslated(0, hudTop, 0);
        hudPanel.render();
        glPopMatrix();

        // Render HUD text
        glPushMatrix();
        glTranslatef(hudLeft, hudTop, 0);
        glScalef(1.5f, 1.5f, 0);

        renderText(0, 60, "MONEY: " + game.getLevel().getPlayer().getMoney());
        String selected = game.getLevel().getPlayer().getSelected().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(", "));
        renderText(200, 60, selected);
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

        // Render Cursor
        glPushMatrix();
        glTranslated(mouseX - (currentCursor.getWidth() * currentCursor.getScaleFactor()) / 2, mouseY - (currentCursor.getHeight() * currentCursor.getScaleFactor()) / 2, 0);
        currentCursor.render();
        glPopMatrix();

        logo.render();
    }

    @Override
    public void tick(int tickCount) {
        InputHandler.Mouse mouse = game.getInput().mouse;
        mouseX = mouse.getX();
        mouseY = mouse.getY();

        boolean hitEdge = false;

        if (mouseX < 10) {
            mouseX = 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(7);
            hitEdge = true;
        } else if (mouseX > width - 10) {
            mouseX = width - 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(5);
            hitEdge = true;
        }
        if (mouseY < 10) {
            mouseY = 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(4);
            hitEdge = true;
        } else if (mouseY > height - 10) {
            mouseY = height - 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(6);
            hitEdge = true;
        }
        glfwSetCursorPos(game.getWindow().getHandle(), mouseX, mouseY);

        drag = mouse.wasLeftDrag();
        fromX = mouse.getLastLeftX();
        fromY = mouse.getLastLeftY();

        if (!hitEdge) {
            Level level = game.getLevel();
            if (level.getPlayer().getSelected().isEmpty()) {
                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);
            } else {
                Entity entity = level.entityAt(level.getLevelX(mouseX), level.getLevelY(mouseY));
                if (entity != null && level.isTileVisible(level.tileAt(entity))) {
                    if (entity instanceof Entity.PlayableEntity playable) {
                        if (playable.getOwner() == level.getPlayer()) {
                            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(0);
                        } else {
                            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(1);
                        }
                    }
                } else {
                    currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(3);
                }
            }
        }
    }

    private void renderText(float x, float y, String text) {
        new Text(text, x, y, false).render();
    }

}
