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
    private final float hudTop;
    private final float hudLeft;

    private double mouseX, mouseY;
    private double fromX, fromY;
    private boolean drag;


    public static final int MAX_POWER = 1000;

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
        hudTop = height - (hudPanel.getScaledHeight());
        hudLeft = 60 * hudScale;
    }


    @Override
    public void render() {
        renderPanel();
        renderCursor();

        logo.render();
    }

    private void renderCursor() {
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
    }

    private void renderPanelText() {
        // Render HUD text
        glPushMatrix();
        glTranslatef(hudLeft, hudTop, 0);
        glScalef(2f, 2f, 0);

        Level level = game.getLevel();
        renderText(0, 30, "MONEY: " + level.getPlayer().getMoney());
        String selected = level.getPlayer().getSelected().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(", "));
        renderText(200, 110, selected);

        float levelX = level.getLevelX(mouseX);
        float levelY = level.getLevelY(mouseY);

        renderText(0, 70, String.format("X:      %.2f", mouseX));
        renderText(0, 80, String.format("Y:      %.2f", mouseY));
        renderText(0, 90, String.format("LEVELX: %.2f", levelX));
        renderText(0, 100, String.format("LEVELY: %.2f", levelY));
        renderText(0, 110, String.format("XOFF:   %.2f", level.xOff));
        renderText(0, 120, String.format("YOFF:   %.2f", level.yOff));
        renderText(200, 120, String.format("ZOOM:   %.2f", level.zoom));
        glPopMatrix();
    }

    private void renderPower() {
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
        glVertex2i(380 * production / MAX_POWER, 4);
        glVertex2i(380 * production / MAX_POWER, 7);
        glVertex2i(71, 7);
        if (production < consumption) {
            glColor3f(0.6f, 0.1f, 0.1f);
            glVertex2i(380 * production / MAX_POWER, 4);
            glVertex2i(380 * (consumption - 2) / MAX_POWER, 4);
            glVertex2i(380 * (consumption - 2) / MAX_POWER, 7);
            glVertex2i(380 * production / MAX_POWER, 7);
        }
        glColor3f(0.6f, 0.6f, 0.6f);
        glVertex2i(380 * (consumption - 2) / MAX_POWER, 4);
        glVertex2i(380 * (consumption + 2) / MAX_POWER, 4);
        glVertex2i(380 * (consumption + 2) / MAX_POWER, 7);
        glVertex2i(380 * (consumption - 2) / MAX_POWER, 7);
        glColor3f(1, 1, 1);
        glEnd();
        glPopMatrix();
    }

    private void renderPanel() {
        glPushMatrix();
        glTranslated(0, hudTop, 0);
        renderPower();
        hudPanel.render();
        renderEntities();
        glPopMatrix();

        renderPanelText();
    }

    private void renderEntities() {
        // Render entities
        glPushMatrix();
        glTranslatef(hudLeft, 0, 0);
        glScalef(4f, 4f, 0);
        glTranslatef(100, 16, 0);
        int playerOffset = SpriteSheet.flagToOffset(game.getLevel().getPlayer().getFlag());
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_BASE + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_POWERPLANT + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_BARRACKS + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_FACTORY + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_REFINERY + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_SILO + playerOffset).render();
        glTranslatef(24, 0, 0);
        SpriteSheet.ENTITY_SHEET.getSprite(SpriteSheet.SPRITE_ID_HELIPAD + playerOffset).render();
        glPopMatrix();
    }

    @Override
    public void tick(int tickCount) {
        updateInputs();
        updateCursor();
    }

    private void updateCursor() {
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

    private void updateInputs() {
        InputHandler.Mouse mouse = game.getInput().mouse;
        mouseX = mouse.getX();
        mouseY = mouse.getY();
        drag = mouse.wasLeftDrag();
        fromX = mouse.getLastLeftX();
        fromY = mouse.getLastLeftY();
    }

    private void renderText(float x, float y, String text) {
        new Text(text, x, y, false).render();
    }

    public boolean isMouseOverHud(float mouseY) {
        return mouseY > height - hudPanel.getScaledHeight();
    }
}
