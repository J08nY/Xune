package sk.neuromancer.Xune.entity.unit;

import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.building.Factory;
import sk.neuromancer.Xune.input.Clickable;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Config.TPS;

public class Buggy extends Unit {

    static {
        setMaxHealth(Buggy.class, 100);
        setCost(Buggy.class, 100);
        setSight(Buggy.class, 50);
        setSpeed(Buggy.class, 0.5f);
        setRange(Buggy.class, 20f);
        setRate(Buggy.class, 30);
        setDamage(Buggy.class, 10);
        setAccuracy(Buggy.class, 0.6f);
        setBuildTime(Buggy.class, TPS * 5);
        setDeathSound(Buggy.class, SoundManager.SOUND_EXPLOSION_1);
        setShotSound(Buggy.class, SoundManager.SOUND_SHOT_2);
        setBaseSprite(Buggy.class, SpriteSheet.SPRITE_ID_BUGGY);
        registerPrerequisites(Buggy.class, List.of(new Prerequisite(Factory.class)));
    }

    public Buggy(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 5));
    }

    public Buggy(EntityStateProto.UnitState savedState, Player owner) {
        super(savedState, owner);
        updateSprite();
        this.clickableAreas.add(Clickable.ClickableCircle.getCentered(x, y, 5));
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
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH + spriteOffset);
    }

    public Point[] getOccupied() {
        return new Point[]{new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y))};
    }
}
