package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.Buggy;
import sk.neuromancer.Xune.entity.unit.Heli;
import sk.neuromancer.Xune.level.Level;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static sk.neuromancer.Xune.game.Game.TPS;
import static sk.neuromancer.Xune.level.Level.tileCenterX;
import static sk.neuromancer.Xune.level.Level.tileCenterY;

public class Player extends EntityOwner {
    private final List<PlayableEntity> selected = new LinkedList<>();
    private final Map<Class<? extends PlayableEntity>, CommandStrategy> commandStrategies = new HashMap<>();

    public Player(Game g, Level level, Flag flag, int money) {
        super(g, level, flag, money);
        this.addEntity(new Base(7, 2, Orientation.NORTH, this, this.flag));
        this.addEntity(new Refinery(5, 4, Orientation.NORTH, this, this.flag));
        this.addEntity(new Silo(5, 5, Orientation.NORTH, this, this.flag));
        this.addEntity(new Silo(5, 6, Orientation.NORTH, this, this.flag));
        this.addEntity(new Helipad(4, 5, Orientation.NORTH, this, this.flag));
        Factory factory = new Factory(6, 5, Orientation.NORTH, this, this.flag);
        this.addEntity(factory);
        this.addEntity(new Buggy(tileCenterX(6, 8), tileCenterY(6, 8), Orientation.SOUTHEAST, this, this.flag));
        this.addEntity(new Buggy(tileCenterX(8, 4), tileCenterY(8, 4), Orientation.EAST, this, this.flag));
        this.addEntity(new Heli(tileCenterX(7, 7), tileCenterY(7, 7), Orientation.EAST, this, this.flag));

        Buggy buggy = new Buggy(tileCenterX(6, 6), tileCenterY(6, 6), Orientation.EAST, this, this.flag);
        Command produceBuggy = new Command.ProduceCommand(TPS * 5, buggy);
        factory.pushCommand(produceBuggy);

        commandStrategies.put(Heli.class, new CommandStrategy.AirStrategy());
        commandStrategies.put(Buggy.class, new CommandStrategy.GroundStrategy());
    }

    @Override
    public void removeEntity(PlayableEntity e) {
        super.removeEntity(e);
        selected.remove(e);
    }


    @Override
    public void tick(int tickCount) {
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
                    handleLeftClick(levelX, levelY);
                } else {
                    handleDrag(fromLevelX, fromLevelY, levelX, levelY);
                }
            } else {
                handleLeftClick(levelX, levelY);
            }
        }
        if (game.getInput().mouse.isRightReleased()) {
            handleRightClick();
        }
    }

    private void handleRightClick() {
        for (PlayableEntity e : entities) {
            e.unselect();
        }
    }

    private void handleDrag(float fromLevelX, float fromLevelY, float levelX, float levelY) {
        for (PlayableEntity e : entities) {
            if (e.intersects(Math.min(fromLevelX, levelX), Math.min(fromLevelY, levelY),
                    Math.max(fromLevelX, levelX), Math.max(fromLevelY, levelY))) {
                if (e instanceof Building) {
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

    private void handleLeftClick(float levelX, float levelY) {
        Entity other = level.entityAt(levelX, levelY);
        if (other instanceof PlayableEntity playable && entities.contains(playable)) {
            if (selected.contains(playable)) {
                selected.remove(playable);
                playable.unselect();
            } else {
                selected.add(playable);
                playable.select();
            }
            return;
        }

        for (PlayableEntity only : selected) {
            CommandStrategy strategy = commandStrategies.get(only.getClass());
            if (strategy != null) {
                Command command = strategy.createCommand(only, other, level, levelX, levelY);
                if (command != null) {
                    only.pushCommand(command);
                }
            }
        }
    }

    public List<PlayableEntity> getSelected() {
        return selected;
    }
}
