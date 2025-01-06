package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Barracks;
import sk.neuromancer.Xune.gfx.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Game.TPS;

public class Soldier extends Unit {
    private int step;

    static {
        setMaxHealth(Soldier.class, 50);
        setCost(Soldier.class, 50);
        setSight(Soldier.class, 30);
        setSpeed(Soldier.class, 0.2f);
        setRange(Soldier.class, 20f);
        setRate(Soldier.class, 15);
        setDamage(Soldier.class, 2);
        setAccuracy(Soldier.class, 0.7f);
        setBuildTime(Soldier.class, TPS * 2);
        setDeathSound(Soldier.class, SoundManager.SOUND_WILHELM);
        setShotSound(Soldier.class, SoundManager.SOUND_SHOT_1);
        setBaseSprite(Soldier.class, SpriteSheet.SPRITE_ID_SOLDIER);
        registerPrerequisites(Soldier.class, List.of(new Prerequisite(Barracks.class)));
    }

    public Soldier(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 3, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 15 == 0) {
            step = (step + 1) % 4;
            updateSprite();
        }
        super.tick(tickCount);
    }

    @Override
    public void render() {
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand move) {
            glDisable(GL_DEPTH_TEST);
            move.getNextPath().render();
            glEnable(GL_DEPTH_TEST);
        }
        super.render();
    }

    @Override
    protected void updateSprite() {
        int i = orientation.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 3;
        } else {
            animation = (i % 4) * 3 + SpriteSheet.SPRITE_ROW_LENGTH;
        }
        int variant = 0;
        switch (step) {
            case 0 -> variant = 1;
            case 1, 3 -> variant = 0;
            case 2 -> variant = 2;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(flag) + variant + animation);
    }

    @Override
    public Point[] getOccupied() {
        return new Point[]{new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y))};
    }
}
