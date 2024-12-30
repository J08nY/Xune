package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.entity.Clickable;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.Game;
import sk.neuromancer.Xune.game.InputHandler;
import sk.neuromancer.Xune.game.Player;
import sk.neuromancer.Xune.game.Tickable;
import sk.neuromancer.Xune.level.Level;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class HUD implements Tickable, Renderable {
    private final Game game;
    private final Level level;
    private final Player player;
    private final float width;
    private final float height;

    private Sprite currentCursor;
    private final float cursorScale = 2f;
    private final Sprite logo;
    private final Sprite hudPanel;
    private final float hudScale;
    private final float hudTop;
    private final float hudLeft;

    private final List<Button<?>> buttons;

    private double mouseX, mouseY;
    private double fromX, fromY;
    private boolean drag;

    public static final int MAX_POWER = 1000;

    public HUD(Game game) {
        this.game = game;
        this.level = game.getLevel();
        this.player = level.getPlayer();
        this.width = game.getWindow().getWidth();
        this.height = game.getWindow().getHeight();
        glfwSetInputMode(game.getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // TODO: This does nothing. Why?
        glfwSetCursorPos(game.getWindow().getHandle(), (double) width / 2, (double) height / 2);
        this.currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);

        this.logo = SpriteSheet.LOGO.getSprite(0);

        this.hudPanel = SpriteSheet.HUD_PANEL.getSprite(0);
        this.hudScale = width / (float) hudPanel.getWidth();
        this.hudTop = height - (hudPanel.getHeight() * hudScale);
        this.hudLeft = 60 * hudScale;

        this.buttons = new LinkedList<>();
        int playerOffset = SpriteSheet.flagToOffset(player.getFlag());
        this.buttons.add(new Button<>(Base.class, SpriteSheet.SPRITE_ID_BASE + playerOffset, 1, hudLeft + 400, hudTop + 64, 4));
        this.buttons.add(new Button<>(Powerplant.class, SpriteSheet.SPRITE_ID_POWERPLANT + playerOffset, 1, hudLeft + 400 + 4 * 25, hudTop + 64, 4));
        this.buttons.add(new Button<>(Barracks.class, SpriteSheet.SPRITE_ID_BARRACKS + playerOffset, 1, hudLeft + 400 + 4 * 25 * 2, hudTop + 64, 4));
        this.buttons.add(new Button<>(Factory.class, SpriteSheet.SPRITE_ID_FACTORY + playerOffset, 1, hudLeft + 400 + 4 * 25 * 3, hudTop + 64, 4));
        this.buttons.add(new Button<>(Refinery.class, SpriteSheet.SPRITE_ID_REFINERY + playerOffset, 1, hudLeft + 400 + 4 * 25 * 4, hudTop + 64, 4));
        this.buttons.add(new Button<>(Silo.class, SpriteSheet.SPRITE_ID_SILO + playerOffset, 1, hudLeft + 400 + 4 * 25 * 5, hudTop + 64, 4));
        this.buttons.add(new Button<>(Base.class, SpriteSheet.SPRITE_ID_HELIPAD + playerOffset, 1, hudLeft + 400 + 4 * 25 * 6, hudTop + 64, 4));

        this.buttons.add(new Button<>(Soldier.class, SpriteSheet.SPRITE_ID_SOLDIER + playerOffset, 1, hudLeft + 400, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Buggy.class, SpriteSheet.SPRITE_ID_BUGGY + playerOffset, 1, hudLeft + 400 + 4 * 25, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Heli.class, SpriteSheet.SPRITE_ID_HELI + playerOffset, 1, hudLeft + 400 + 4 * 25 * 2, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Harvester.class, SpriteSheet.SPRITE_ID_HARVESTER + playerOffset, 1, hudLeft + 400 + 4 * 25 * 3, hudTop + 64 + 4 * 12, 4));
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
        glTranslated(mouseX - (currentCursor.getWidth() * cursorScale) / 2, mouseY - (currentCursor.getHeight() * cursorScale) / 2, 0);
        glScalef(cursorScale, cursorScale, 0);
        currentCursor.render();
        glPopMatrix();
    }

    private void renderPanelText() {
        // Render HUD text
        glPushMatrix();
        glTranslatef(hudLeft, hudTop, 0);
        glScalef(2f, 2f, 0);

        renderText(0, 30, "MONEY: " + player.getMoney());
        String selected = player.getSelected().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(", "));
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
        glTranslated(0, hudTop, 0);
        glScalef(hudScale, hudScale, 0);
        glBegin(GL_QUADS);
        glColor3f(0.1f, 0.1f, 0.1f);
        glVertex2i(71, 4);
        glVertex2i(380, 4);
        glVertex2i(380, 7);
        glVertex2i(71, 7);
        int production = player.getPowerProduction();
        int consumption = player.getPowerConsumption();
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
        renderPower();
        renderPanelBackground();
        renderEntities();
        renderPanelText();
    }

    private void renderPanelBackground() {
        glPushMatrix();
        glTranslatef(0, hudTop, 0);
        glScalef(hudScale, hudScale, 0);
        hudPanel.render();
        glPopMatrix();
    }

    private void renderEntities() {
        // Render entities
        glPushMatrix();
        for (Button<?> button : buttons) {
            button.render();
        }
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
            if (mouseY > height - (hudPanel.getHeight() * hudScale)) {
                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(13);
            } else {
                if (player.getSelected().isEmpty()) {
                    currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);
                } else {
                    Entity entity = level.entityAt(level.getLevelX(mouseX), level.getLevelY(mouseY));
                    if (entity != null && level.isTileVisible(level.tileAt(entity))) {
                        if (entity instanceof Entity.PlayableEntity playable) {
                            if (playable.getOwner() == player) {
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
        return mouseY > height - (hudPanel.getHeight() * hudScale);
    }

    public List<Button<?>> getButtons() {
        return buttons;
    }

    public static class Button<T extends Entity.PlayableEntity> implements Clickable, Tickable, Renderable {
        private final Class<T> klass;
        private Sprite sprite;
        private final int spriteOffset;
        private final int numSprites;
        private float x;
        private float y;
        private final float width;
        private final float height;
        private final float scale;

        public Button(Class<T> klass, int spriteOffset, int numSprites, float x, float y, float scale) {
            this.klass = klass;
            this.spriteOffset = spriteOffset;
            this.numSprites = numSprites;
            this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(spriteOffset);
            this.x = x;
            this.y = y;
            this.width = sprite.getWidth();
            this.height = sprite.getHeight();
            this.scale = scale;
        }

        @Override
        public void tick(int tickCount) {

        }

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(x, y, 0);
            glScalef(scale, scale, 0);
            glBegin(GL_QUADS);
            glColor4f(1, 1, 1, 0.2f);
            glVertex2f(0, 0);
            glVertex2f(width, 0);
            glVertex2f(width, height);
            glVertex2f(0, height);
            glColor4f(1, 1, 1, 1);
            glEnd();
            sprite.render();
            glPopMatrix();
        }

        @Override
        public boolean intersects(float x, float y) {
            return x >= this.x && x <= this.x + width * scale && y >= this.y && y <= this.y + height * scale;
        }

        @Override
        public boolean intersects(float fromX, float fromY, float toX, float toY) {
            return false;
        }

        @Override
        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Class<T> getKlass() {
            return klass;
        }
    }
}
