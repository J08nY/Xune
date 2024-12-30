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
    private final float hudScale;

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
        hudScale = width / (float) hudPanel.getWidth();
        hudPanel.setScaleFactor(hudScale);
    }

    @Override
    public void render() {
        float hudTop = height - (hudPanel.getScaledHeight());
        float hudLeft = (hudPanel.getScaledWidth()) * 0.18f;
        //60

        // Render HUD panel
        glPushMatrix();
        glTranslated(0, hudTop, 0);
        // Render power
        glPushMatrix();
        glScalef(hudScale, hudScale, 0);
        glBegin(GL_QUADS);
        glColor3f(0.1f, 0.1f, 0.1f);
        glVertex2i(71, 4);
        glVertex2i(380, 4);
        glVertex2i(380, 7);
        glVertex2i(71, 7);
        int production = game.getLevel().getPlayer().getPowerProduction();
        int consumption = game.getLevel().getPlayer().getPowerConsumption();
        glColor3f(0.1f, 0.6f, 0.1f);
        glVertex2i(71, 4);
        glVertex2i(380 * production / 1000, 4);
        glVertex2i(380 * production / 1000, 7);
        glVertex2i(71, 7);
        if (production < consumption) {
            glColor3f(0.6f, 0.1f, 0.1f);
            glVertex2i(380 * production / 1000, 4);
            glVertex2i(380 * (consumption - 2) / 1000, 4);
            glVertex2i(380 * (consumption - 2) / 1000, 7);
            glVertex2i(380 * production / 1000, 7);
        }
        glColor3f(0.6f, 0.6f, 0.6f);
        glVertex2i(380 * (consumption - 2) / 1000, 4);
        glVertex2i(380 * (consumption + 2) / 1000, 4);
        glVertex2i(380 * (consumption + 2) / 1000, 7);
        glVertex2i(380 * (consumption - 2) / 1000, 7);
        glColor3f(1, 1, 1);
        glEnd();
        glPopMatrix();

        hudPanel.render();
        glPopMatrix();

        // Render HUD text
        glPushMatrix();
        glTranslatef(hudLeft, hudTop, 0);
        glScalef(1.5f, 1.5f, 0);

        Level level = game.getLevel();
        renderText(0, 60, "MONEY: " + level.getPlayer().getMoney());
        String selected = level.getPlayer().getSelected().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(", "));
        renderText(200, 60, selected);
        float levelX = level.getLevelX(mouseX);
        float levelY = level.getLevelY(mouseY);

        renderText(0, 90, "X: " + mouseX);
        renderText(0, 120, "Y: " + mouseY);
        renderText(200, 90, "LEVELX: " + levelX);
        renderText(200, 120, "LEVELY: " + levelY);
        renderText(400, 90, "XOFF: " + level.xOff);
        renderText(400, 120, "YOFF: " + level.yOff);
        renderText(600, 90, "ZOOM: " + level.zoom);
        glPopMatrix();

        // Render Cursor
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
            if (mouseY > height - hudPanel.getScaledHeight()) {
                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(13);
            } else {
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
    }

    private void renderText(float x, float y, String text) {
        new Text(text, x, y, false).render();
    }

    public boolean isMouseOverHud(float mouseY) {
        return mouseY > height - hudPanel.getScaledHeight();
    }
}
