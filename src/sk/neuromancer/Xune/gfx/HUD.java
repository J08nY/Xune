package sk.neuromancer.Xune.gfx;

import sk.neuromancer.Xune.entity.Command;
import sk.neuromancer.Xune.entity.Entity;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.game.*;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.level.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class HUD implements Tickable, Renderable {
    private final Game game;
    private final float screenWidth;
    private final float screenHeight;

    private Sprite currentCursor;
    private final float cursorScale = 2f;
    private Text tooltip;
    private final Sprite logo;
    private final Sprite hudPanel;
    private final float hudScale;
    private final float hudTop;
    private final float hudLeft;


    private Level level;
    private Human human;
    private List<Button<?>> buttons;

    private double mouseX, mouseY;
    private double fromX, fromY;
    private boolean drag;

    public static final int MAX_POWER = 1000;

    public HUD(Game game) {
        this.game = game;
        this.screenWidth = game.getWindow().getWidth();
        this.screenHeight = game.getWindow().getHeight();
        glfwSetInputMode(game.getWindow().getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // TODO: This does nothing. Why?
        glfwSetCursorPos(game.getWindow().getHandle(), (double) screenWidth / 2, (double) screenHeight / 2);
        this.currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);

        this.logo = SpriteSheet.LOGO.getSprite(0);

        this.hudPanel = SpriteSheet.HUD_PANEL.getSprite(0);
        this.hudScale = screenWidth / (float) hudPanel.getWidth();
        this.hudTop = screenHeight - (hudPanel.getHeight() * hudScale);
        this.hudLeft = 60 * hudScale;
    }

    public void setLevel(Level level) {
        this.level = level;
        this.human = level.getHuman();

        this.buttons = new ArrayList<>(11);
        this.buttons.add(new Button<>(Base.class, human, 2, 1, hudLeft + 400, hudTop + 64, 4));
        this.buttons.add(new Button<>(Powerplant.class, human, 2, 1, hudLeft + 400 + 4 * 25, hudTop + 64, 4));
        this.buttons.add(new Button<>(Barracks.class, human, 2, 1, hudLeft + 400 + 4 * 25 * 2, hudTop + 64, 4));
        this.buttons.add(new Button<>(Factory.class, human, 2, 1, hudLeft + 400 + 4 * 25 * 3, hudTop + 64, 4));
        this.buttons.add(new Button<>(Refinery.class, human, 2, 1, hudLeft + 400 + 4 * 25 * 4, hudTop + 64, 4));
        this.buttons.add(new Button<>(Silo.class, human, 2, 1, hudLeft + 400 + 4 * 25 * 5, hudTop + 64, 4));
        this.buttons.add(new Button<>(Helipad.class, human, 2, 1, hudLeft + 400 + 4 * 25 * 6, hudTop + 64, 4));

        this.buttons.add(new Button<>(Soldier.class, human, 2, 12, hudLeft + 400, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Buggy.class, human, 2, 4, hudLeft + 400 + 4 * 25, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Heli.class, human, 2, 8, hudLeft + 400 + 4 * 25 * 2, hudTop + 64 + 4 * 12, 4));
        this.buttons.add(new Button<>(Harvester.class, human, 2, 4, hudLeft + 400 + 4 * 25 * 3, hudTop + 64 + 4 * 12, 4));
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        renderPanel();
        renderCursor();

        //logo.render();
        glEnable(GL_DEPTH_TEST);
    }

    private void renderCursor() {
        // Render Cursor
        if (drag) {
            glPushMatrix();
            glBegin(GL_QUADS);
            glColor4f(0f, 1f, 0f, 0.2f);
            glVertex3d(fromX, fromY, 1);
            glVertex3d(fromX, mouseY, 1);
            glVertex3d(mouseX, mouseY, 1);
            glVertex3d(mouseX, fromY, 1);
            glEnd();
            glColor4f(1.f, 1.f, 1.f, 1.f);
            glPopMatrix();
        }
        glPushMatrix();
        glTranslated(mouseX - (currentCursor.getWidth() * cursorScale) / 2, mouseY - (currentCursor.getHeight() * cursorScale) / 2, 1);
        glScalef(cursorScale, cursorScale, 1);
        currentCursor.render();
        glPopMatrix();
        if (tooltip != null) {
            tooltip.render();
        }
    }

    private void renderPanelText() {
        // Render HUD text
        glPushMatrix();
        glTranslatef(hudLeft, hudTop, 0);
        glScalef(2f, 2f, 1);

        renderText(20, 40, "MONEY: " + human.getMoney() + "$");
        renderText(20, 60, "POWER: " + human.getPowerProduction() + "/" + human.getPowerConsumption());
        String selected = human.getSelected().stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(", "));
        renderText(20, 110, selected);

//        float levelX = level.getLevelX(mouseX);
//        float levelY = level.getLevelY(mouseY);
//
//        renderText(0, 70, String.format("X:      %.2f", mouseX));
//        renderText(0, 80, String.format("Y:      %.2f", mouseY));
//        renderText(0, 90, String.format("LEVELX: %.2f", levelX));
//        renderText(0, 100, String.format("LEVELY: %.2f", levelY));
//        renderText(0, 110, String.format("XOFF:   %.2f", level.xOff));
//        renderText(0, 120, String.format("YOFF:   %.2f", level.yOff));
//        renderText(200, 120, String.format("ZOOM:   %.2f", level.zoom));
        glPopMatrix();
    }

    private void renderPower() {
        // Render power
        glPushMatrix();
        glTranslated(0, hudTop, 0.99f);
        glScalef(hudScale, hudScale, 1);
        glBegin(GL_QUADS);
        glColor3f(0.1f, 0.1f, 0.1f);
        glVertex2i(71, 4);
        glVertex2i(380, 4);
        glVertex2i(380, 7);
        glVertex2i(71, 7);
        int production = human.getPowerProduction();
        int consumption = human.getPowerConsumption();
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
        renderMinimap();
        renderEntities();
        renderPanelText();
    }

    private void renderMinimap() {
        // Render minimap
        glPushMatrix();
        glTranslatef(0, hudTop, 0.99f);
        glPointSize(1);
        glScalef(hudScale, hudScale, 1);
        glTranslatef(5, 5, 0);
        glScalef((float) 50 / level.getWidthInTiles(), (float) 50 / level.getHeightInTiles(), 1);
        glColor3f(0, 0, 0);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(level.getWidthInTiles(), 0);
        glVertex2f(level.getWidthInTiles(), level.getHeightInTiles());
        glVertex2f(0, level.getHeightInTiles());
        glEnd();
        glBegin(GL_QUADS);
        for (int x = 0; x < level.getWidthInTiles(); x++) {
            for (int y = 0; y < level.getHeightInTiles(); y++) {
                Tile tile = level.getTile(x, y);
                Sprite sprite = SpriteSheet.MAP_SHEET.getSprite(tile.type);
                int[] pixels = sprite.getPixels();
                byte red = (byte) (pixels[0] & 0xff);
                byte green = (byte) (pixels[1] & 0xff);
                byte blue = (byte) (pixels[2] & 0xff);
                byte alpha;
                if (human.isTileVisible(tile)) {
                    alpha = (byte) 255;
                } else if (human.isTileDiscovered(tile)) {
                    alpha = (byte) 128;
                } else {
                    continue;
                }
                glColor4ub(red, green, blue, alpha);
                glVertex2f(x, y);
                glVertex2f(x + 1, y);
                glVertex2f(x + 1, y + 1);
                glVertex2f(x, y + 1);
            }
        }
        glEnd();
        glBegin(GL_QUADS);
        for (Entity entity : level.getEntities()) {
            if (entity instanceof Entity.PlayableEntity playable && human.isTileDiscovered(level.tileAt(entity))) {
                glColor3fv(playable.getOwner().getFlag().getColor());
                float lx = entity.x;
                float ly = entity.y;
                int tx = Level.levelToTileX(lx, ly);
                int ty = Level.levelToTileY(lx, ly);
                glVertex2f(tx, ty);
                glVertex2f(tx + 1, ty);
                glVertex2f(tx + 1, ty + 1);
                glVertex2f(tx, ty + 1);
            }
        }
        glColor3f(1, 1, 1);
        glEnd();
        glPopMatrix();
    }

    private void renderPanelBackground() {
        glPushMatrix();
        glTranslatef(0, hudTop, 0.99f);
        glScalef(hudScale, hudScale, 1);
        hudPanel.render();
        glPopMatrix();
    }

    private void renderEntities() {
        // Render entities
        glPushMatrix();
        glTranslatef(0, 0, 0.99f);
        for (Button<?> button : buttons) {
            button.render();
        }
        glPopMatrix();
    }

    @Override
    public void tick(int tickCount) {
        updateInputs();
        updateCursor();
        updateButtons(tickCount);
    }

    private void updateButtons(int tickCount) {
        for (Button<?> button : buttons) {
            button.tick(tickCount);
        }
    }

    private void updateCursor() {
        boolean hitEdge = false;
        if (mouseX < 10) {
            mouseX = 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(7);
            hitEdge = true;
        } else if (mouseX > screenWidth - 10) {
            mouseX = screenWidth - 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(5);
            hitEdge = true;
        }
        if (mouseY < 10) {
            mouseY = 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(4);
            hitEdge = true;
        } else if (mouseY > screenHeight - 10) {
            mouseY = screenHeight - 10;
            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(6);
            hitEdge = true;
        }
        glfwSetCursorPos(game.getWindow().getHandle(), mouseX, mouseY);

        tooltip = null;
        if (!hitEdge) {
            if (isMouseOverHud((float) mouseY)) {
                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(13);
                for (Button<?> button : buttons) {
                    if (button.intersects((float) mouseX, (float) mouseY)) {
                        tooltip = new Text(button.getKlass().getSimpleName(), (float) mouseX, (float) mouseY + 20, true, 2f);
                    }
                }
            } else {
                Entity entity = level.entityAt(game.getView().getLevelX(mouseX), game.getView().getLevelY(mouseY));
                if (entity != null && human.isTileVisible(level.tileAt(entity))) {
                    tooltip = new Text(entity.getClass().getSimpleName(), (float) mouseX, (float) mouseY + 20, true, 1f);
                }
                if (human.getSelected().isEmpty()) {
                    currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(2);
                } else {
                    if (entity != null && human.isTileVisible(level.tileAt(entity))) {
                        if (entity instanceof Entity.PlayableEntity playable) {
                            if (playable.getOwner() == human) {
                                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(0);
                            } else {
                                currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(1);
                            }
                        } else {
                            currentCursor = SpriteSheet.CURSOR_SHEET.getSprite(1);
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
        new Text(text, x, y, false, 1).render();
    }

    public boolean isMouseOverHud(float mouseY) {
        return mouseY > hudTop;
    }

    public float getHudTop() {
        return hudTop;
    }

    public List<Button<?>> getButtons() {
        return buttons;
    }

    public static class Button<T extends Entity.PlayableEntity> implements Clickable, Tickable, Renderable {
        private final Class<T> klass;
        private final Human human;
        private Sprite sprite;
        private int animation;
        private boolean enabled;
        private Map<Building, Command.ProduceCommand> inProgress = new HashMap<>();
        private int totalInProgress;
        private final int spriteOffset;
        private final int spriteRows, spriteCols;
        private float x;
        private float y;
        private final float width;
        private final float height;
        private final float scale;


        public Button(Class<T> klass, Human human, int spriteRows, int spriteCols, float x, float y, float scale) {
            this.klass = klass;
            this.human = human;
            this.spriteOffset = Entity.PlayableEntity.getBaseSprite(klass) + SpriteSheet.flagToOffset(human.getFlag());
            this.spriteRows = spriteRows;
            this.spriteCols = spriteCols;
            this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(this.spriteOffset);
            this.x = x;
            this.y = y;
            this.width = sprite.getWidth();
            this.height = sprite.getHeight();
            this.scale = scale;
        }

        @Override
        public void tick(int tickCount) {
            enabled = Entity.PlayableEntity.canBeBuilt(klass, human);
            if (Config.ANIMATE_HUD_BUTTONS && tickCount % TPS == 0) {
                animation = (animation + 1) % (spriteCols * spriteRows);
                int row = animation / spriteCols;
                int col = animation % spriteCols;
                sprite = SpriteSheet.ENTITY_SHEET.getSprite(spriteOffset + row * SpriteSheet.SPRITE_ROW_LENGTH + col);
            }
            inProgress = new HashMap<>();
            totalInProgress = 0;
            if (Unit.class.isAssignableFrom(klass)) {
                for (Entity.PlayableEntity entity : human.getEntities()) {
                    if (!(entity instanceof Building)) {
                        continue;
                    }
                    for (Command cmd : entity.getCommands().reversed()) {
                        if (cmd instanceof Command.ProduceCommand produce) {
                            if (produce.getResultClass() == klass) {
                                inProgress.put((Building) entity, produce);
                                totalInProgress++;
                            }
                        }
                    }
                }
            } else if (Building.class.isAssignableFrom(klass)) {
                if (human.getBuildingToBuild() == klass) {
                    totalInProgress = 1;
                }
            }
        }

        @Override
        public void render() {
            glPushMatrix();
            glTranslatef(x, y, 0);
            glScalef(scale, scale, 1);
            if (enabled) {
                glBegin(GL_QUADS);
                glColor4f(1, 1, 1, 0.2f);
                glVertex2f(0, 0);
                glVertex2f(width, 0);
                glVertex2f(width, height);
                glVertex2f(0, height);
                glColor4f(1, 1, 1, 1);
                glEnd();
            }
            sprite.render();
            if (totalInProgress > 0) {
                float textWidth = Text.widthOf(String.valueOf(totalInProgress), 0.4f);
                float textHeight = Text.heightOf(String.valueOf(totalInProgress), 0.4f);
                new Text(String.valueOf(totalInProgress), width - textWidth, height - textHeight, true, 0.4f).render();
                int index = 0;
                if (Unit.class.isAssignableFrom(klass)) {
                    for (Map.Entry<Building, Command.ProduceCommand> producers : inProgress.entrySet()) {
                        Command.ProduceCommand cmd = producers.getValue();
                        float progress = (float) (Game.currentTick() - cmd.getStart()) / cmd.getDuration();
                        if (progress <= 0) {
                            continue;
                        }
                        renderProgress(index, progress);
                        index++;
                    }
                } else if (Building.class.isAssignableFrom(klass)) {
                    renderProgress(0, human.getBuildProgress());
                }
            }
            glPopMatrix();
        }

        private void renderProgress(int index, float progress) {
            glBegin(GL_QUADS);
            glColor4f(0, 1, 0, 0.5f);
            glVertex2f(0, height - 2 - index * 2);
            glVertex2f(width * progress, height - 2 - index * 2);
            glVertex2f(width * progress, height - index * 2);
            glVertex2f(0, height - index * 2);
            glColor4f(1, 1, 1, 1);
            glEnd();
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
