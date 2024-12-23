package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.gfx.SpriteSheet;


public class Heli extends Entity.Unit {
    private boolean wing;

    public static final float SPEED = 1.5f;

    public Heli(float x, float y, Orientation orientation, EntityOwner owner, Flag flag) {
        super(x, y, owner, flag, 100);
        this.orientation = orientation;
        updateSprite();
        this.clickableAreas.add(ClickableCircle.getCentered(x, y, 7, false));
    }

    @Override
    public void tick(int tickCount) {
        if (tickCount % 5 == 0) {
            wing = !wing;
            updateSprite();
        }
        Command current = currentCommand();
        if (current instanceof Command.FlyCommand fly) {
            if (fly.isFinished(x, y)) {
                setPosition(fly.getToX(), fly.getToY());
                this.commands.removeFirst();
            } else {
                move(fly.getToX(), fly.getToY(), SPEED);
            }
        }
    }

    @Override
    protected void updateSprite() {
        int i = orientation.ordinal();
        int animation;
        if (i < 4) {
            animation = i * 2;
        } else {
            animation = (i % 4) * 2 + Entity.SPRITE_ROW_LENGTH;
        }
        this.sprite = SpriteSheet.ENTITY_SHEET.getSprite(Entity.SPRITE_ID_HELI + PlayableEntity.getOffsetonFlag(flag) + (wing ? 1 : 0) + animation);
    }
}
