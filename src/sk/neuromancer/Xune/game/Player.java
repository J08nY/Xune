package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Entity.Flag;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.level.Level;

import java.util.LinkedList;
import java.util.List;

import static sk.neuromancer.Xune.level.Level.tileCenterX;
import static sk.neuromancer.Xune.level.Level.tileCenterY;

public class Player extends EntityOwner {
    private List<PlayableEntity> selected = new LinkedList<>();

    public Player(Game g, Level level, Flag flag, int money) {
        super(g, level, flag, money);
        this.addEntity(new Base(1, 4, Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(2, 4, Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Silo(2, 3, Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Helipad(3, 3, Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Factory(2, 5, Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Buggy(tileCenterX(1, 7), tileCenterY(1, 7), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Buggy(tileCenterX(2, 7), tileCenterY(2, 7), Entity.Orientation.NORTH, this, this.flag));
        this.addEntity(new Heli(tileCenterX(7, 7), tileCenterY(7, 7), Entity.Orientation.EAST, this, this.flag));
    }

    @Override
    public void removeEntity(PlayableEntity e) {
        super.removeEntity(e);
        selected.remove(e);
    }


    @Override
    public void tick(int tickCount) {
        handleDead();
        handleInput();
        super.tick(tickCount);
    }

    private void handleInput() {
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
    }

    private void handleDrag(float fromLevelX, float fromLevelY, float levelX, float levelY) {
        for (PlayableEntity e : entities) {
            if (e.intersects(Math.min(fromLevelX, levelX), Math.min(fromLevelY, levelY),
                    Math.max(fromLevelX, levelX), Math.max(fromLevelY, levelY))) {
                if (e instanceof Entity.Building) {
                    continue;
                }
                selected.add(e);
                e.select();
            } else {
                selected.remove(e);
                e.unselect();
            }
        }
    }

    private void handleClick(float levelX, float levelY) {
        PlayableEntity only = selected.size() == 1 ? selected.getFirst() : null;
        for (PlayableEntity e : entities) {
            if (e.intersects(levelX, levelY)) {
                selected.add(e);
                e.select();
            } else {
                selected.remove(e);
                e.unselect();
            }
        }

        if (selected.isEmpty() && only != null) {
            if (only instanceof Heli) {
                Command fly = new Command.FlyCommand(only.x, only.y, levelX, levelY);
                only.pushCommand(fly);
            } else if (only instanceof Buggy) {
                try {
                    Command move = new Command.MoveCommand(only.x, only.y, levelX, levelY, level.getPathfinder());
                    only.pushCommand(move);
                } catch (IllegalArgumentException e) {
                    System.out.println("No path found");
                }
            }
        }
    }

    public List<PlayableEntity> getSelected() {
        return selected;
    }
}
