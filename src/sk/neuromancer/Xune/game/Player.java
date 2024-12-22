package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.gfx.Effect;
import sk.neuromancer.Xune.level.Level;

import java.util.LinkedList;
import java.util.List;

import static sk.neuromancer.Xune.level.Level.tileX;
import static sk.neuromancer.Xune.level.Level.tileY;

public class Player extends EntityOwner {
    private final Game game;
    private final Level level;
    public int money;

    private List<PlayableEntity> selected = new LinkedList<>();

    public Player(Game g, Level level, Flag flag, int money) {
        super(flag, money);
        this.game = g;
        this.level = level;
        this.addEntity(new Base(tileX(1, 4), tileY(1, 4), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(tileX(2, 4), tileY(2, 4), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Silo(tileX(2, 3), tileY(2, 3), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Helipad(tileX(3, 3), tileY(3, 3), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Factory(tileX(2, 5), tileY(2, 5), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Harvester(tileX(1, 7), tileY(1, 7), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Heli(tileX(7, 7), tileY(7, 7), Entity.Orientation.EAST, this, this.flag));
        this.effects.add(new Effect.Explosion());
    }

    @Override
    public void tick(int tickCount) {
        InputHandler.Mouse mouse = game.getInput().mouse;
        float mouseX = (float) mouse.getX();
        float mouseY = (float) mouse.getY();
        float fromX = (float) mouse.getLastLeftX();
        float fromY = (float) mouse.getLastLeftY();

        float levelX = level.getLevelX(mouseX);
        float levelY = level.getLevelY(mouseY);
        float fromLevelX = level.getLevelX(fromX);
        float fromLevelY = level.getLevelY(fromY);

        if (game.getInput().mouse.isLeftReleased()) {
            if (game.getInput().mouse.wasLeftDrag()) {
                if (Math.abs(fromX - mouseX) < 5 && Math.abs(fromY - mouseY) < 5) {
                    handleClick(levelX, levelY);
                } else {
                    handleDrag(fromLevelX, fromLevelY, levelX, levelY);
                }
            } else {
                handleClick(levelX, levelY);
            }
        }
        if (game.getInput().mouse.isRightReleased()) {
            for (PlayableEntity e : entities) {
                e.unselect();
            }
        }

        super.tick(tickCount);
    }

    private void handleDrag(float fromLevelX, float fromLevelY, float levelX, float levelY) {
        for (PlayableEntity e : entities) {
            if (e.intersects(Math.min(fromLevelX, levelX), Math.min(fromLevelY, levelY),
                    Math.max(fromLevelX, levelX), Math.max(fromLevelY, levelY), Clickable.Button.LEFT)) {
                selected.add(e);
                e.select();
            } else {
                selected.remove(e);
                e.unselect();
            }
        }
    }

    private void handleClick(float levelX, float levelY) {
        PlayableEntity only = selected.isEmpty() ? null : selected.getFirst();
        for (PlayableEntity e : entities) {
            if (e.intersects(levelX, levelY, Clickable.Button.LEFT)) {
                selected.add(e);
                e.select();
            } else {
                selected.remove(e);
                e.unselect();
            }
        }

        if (selected.isEmpty() && only != null) {
            if (only instanceof Entity.Unit) {
                Command move = new Command.MoveCommand(only.x, only.y, levelX, levelY);
                only.sendCommand(move);
            }
        }
    }


}
