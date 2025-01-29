package sk.neuromancer.Xune.entity.unit;

import com.google.protobuf.ByteString;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.Orientation;
import sk.neuromancer.Xune.entity.Prerequisite;
import sk.neuromancer.Xune.entity.building.Factory;
import sk.neuromancer.Xune.entity.building.Refinery;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.graphics.elements.SpriteSheet;
import sk.neuromancer.Xune.level.Tile;
import sk.neuromancer.Xune.level.paths.Pathfinder;
import sk.neuromancer.Xune.level.paths.Point;
import sk.neuromancer.Xune.proto.EntityStateProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static sk.neuromancer.Xune.game.Config.TPS;

public class Harvester extends Unit {
    private int spice;

    private float offX, offY;

    static {
        setMaxHealth(Harvester.class, 300);
        setCost(Harvester.class, 1200);
        setSight(Harvester.class, 40);
        setSpeed(Harvester.class, 0.1f);
        setRange(Harvester.class, 0);
        setRate(Harvester.class, 0);
        setDamage(Harvester.class, 0);
        setAccuracy(Harvester.class, 0);
        setBuildTime(Harvester.class, TPS * 10);
        setDeathSound(Harvester.class, SoundManager.SOUND_EXPLOSION_2);
        setShotSound(Harvester.class, SoundManager.SOUND_NONE);
        setBaseSprite(Harvester.class, SpriteSheet.SPRITE_ID_HARVESTER);
        registerPrerequisites(Harvester.class, List.of(new Prerequisite(Factory.class)));
    }

    public Harvester(float x, float y, Orientation orientation, Player owner) {
        super(x, y, orientation, owner);
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 6));
    }

    public Harvester(EntityStateProto.UnitState savedState, Player owner) {
        super(savedState, owner);
        if (savedState.hasExtra()) {
            byte[] extra = savedState.getExtra().toByteArray();
            this.spice = (extra[0] << 24) | ((extra[1] & 0xFF) << 16) | ((extra[2] & 0xFF) << 8) | (extra[3] & 0xFF);
        } else {
            this.spice = 0;    
        }
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 6));
    }

    @Override
    public void tick(int tickCount) {
        super.tick(tickCount);
        Command current = currentCommand();
        if (current instanceof Command.CollectSpiceCommand collect && collect.collecting(this)) {
            offX = owner.getRandom().nextFloat();
            offY = owner.getRandom().nextFloat();
        } else {
            offX = 0;
            offY = 0;
        }
    }

    @Override
    public void render() {
        Command current = currentCommand();
        if (current instanceof Command.MoveCommand move) {
            glDisable(GL_DEPTH_TEST);
            move.getNextPath().render();
            glEnable(GL_DEPTH_TEST);
        } else if (current instanceof Command.CollectSpiceCommand collect) {
            Tile target = collect.getTarget();
            if (!collect.collecting(this)) {
                glPushMatrix();
                glTranslatef(target.getLevelX(), target.getLevelY(), 0);
                SpriteSheet.TILE_SHEET.getSprite(17).render();
                glPopMatrix();
            }
        }
        glPushMatrix();
        glTranslatef(offX, offY, 0);
        super.render();
        if (isSelected) {
            glPushMatrix();
            glTranslatef(x - (float) sprite.getWidth() / 2, (y - (float) sprite.getHeight() / 2) - 1, 0);
            glBegin(GL_QUADS);
            glColor3f(0.5f, 0.5f, 0.5f);
            float spicePercentage = (float) spice / 500;
            for (int i = 0; i < 12; i++) {
                if (i >= spicePercentage * 12) {
                    continue;
                }
                glVertex2f(((float) 25 / 12) * i, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 0);
                glVertex2f(((float) 25 / 12) * i + 1, 1);
                glVertex2f(((float) 25 / 12) * i, 1);
            }
            glEnd();
            glColor3f(1, 1, 1);
            glPopMatrix();
        }
        glPopMatrix();
    }

    @Override
    protected void updateSprite() {
        int spriteRow = orientation.ordinal() / 4;
        int spriteOffset = orientation.ordinal() % 4;
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(getBaseSprite(getClass()) + SpriteSheet.flagToOffset(flag) + spriteRow * SpriteSheet.SPRITE_ROW_LENGTH + spriteOffset);
    }

    @Override
    public EntityStateProto.UnitState serialize() {
        byte[] extra = new byte[4];
        extra[0] = (byte) (spice >> 24);
        extra[1] = (byte) (spice >> 16);
        extra[2] = (byte) (spice >> 8);
        extra[3] = (byte) spice;
        return super.serialize().toBuilder().setExtra(ByteString.copyFrom(extra)).build();
    }

    @Override
    public void deserialize(EntityStateProto.UnitState state) {
        super.deserialize(state);
        if (state.hasExtra()) {
            byte[] extra = state.getExtra().toByteArray();
            this.spice = (extra[0] << 24) | ((extra[1] & 0xFF) << 16) | ((extra[2] & 0xFF) << 8) | (extra[3] & 0xFF);
        } else {
            this.spice = 0;
        }
    }

    public boolean collectSpice(Tile target) {
        if (target.getSpice() > 0 && !isFull()) {
            target.takeSpice(1);
            this.spice++;
            return true;
        }
        return false;
    }

    public int getSpice() {
        return spice;
    }

    public boolean isFull() {
        return this.spice == 500;
    }

    public void dropOffSpice(Refinery target) {
        if (this.spice > 0) {
            target.getOwner().addMoney(1);
            this.spice--;
        }
    }

    public Point[] getOccupied() {
        return new Point[]{new Point(Pathfinder.levelXToGrid(x), Pathfinder.levelYToGrid(y))};
    }

}
