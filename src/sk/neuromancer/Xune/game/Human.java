package sk.neuromancer.Xune.game;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.Entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.*;
import sk.neuromancer.Xune.entity.unit.*;
import sk.neuromancer.Xune.gfx.HUD;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.sfx.SoundManager;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public class Human extends Player {
    private final List<PlayableEntity> selected = new LinkedList<>();

    public Human(Game g, Level level, Flag flag, int money) {
        super(g, level, flag, money);
        level.setHuman(this);
        this.addEntity(new Base(7, 2, Orientation.NORTH, this));
        this.addEntity(new Refinery(5, 6, Orientation.NORTH, this));
        this.addEntity(new Silo(5, 5, Orientation.NORTH, this));
        this.addEntity(new Helipad(4, 5, Orientation.NORTH, this));
        this.addEntity(new Powerplant(6, 4, Orientation.NORTH, this));
        this.addEntity(new Barracks(6, 3, Orientation.NORTH, this));
        this.addEntity(new Powerplant(5, 4, Orientation.NORTH, this));
        this.addEntity(new Factory(6, 5, Orientation.NORTH, this));
        this.addEntity(new Buggy(tileToCenterLevelX(6, 8), tileToCenterLevelY(6, 8), Orientation.SOUTHEAST, this));
        this.addEntity(new Buggy(tileToCenterLevelX(8, 4), tileToCenterLevelY(8, 4), Orientation.EAST, this));
        this.addEntity(new Heli(tileToCenterLevelX(7, 7), tileToCenterLevelY(7, 7), Orientation.EAST, this));
        this.addEntity(new Harvester(tileToCenterLevelX(11, 8), tileToCenterLevelY(11, 8), Orientation.SOUTHEAST, this));
        this.addEntity(new Soldier(tileToCenterLevelX(10, 7), tileToCenterLevelY(10, 7), Orientation.SOUTHEAST, this));

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

        if (game.getHud().isMouseOverHud(mouseY)) {
            if (game.getInput().mouse.isLeftReleased()) {
                handleHUDClick(mouseX, mouseY);
            }
            return;
        }

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
            selected.remove(e);
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

        if (other != null && !isTileVisible(level.tileAt(other))) {
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

    private void handleHUDClick(float mouseX, float mouseY) {
        for (HUD.Button<?> button : game.getHud().getButtons()) {
            if (button.intersects(mouseX, mouseY)) {
                Class<? extends PlayableEntity> klass = button.getKlass();
                if (Building.class.isAssignableFrom(klass)) {
                    System.out.println("Build " + klass.getSimpleName());
                } else if (Unit.class.isAssignableFrom(klass)) {
                    if (PlayableEntity.canBeBuilt(klass, this)) {
                        List<Building> producers = entities.stream().filter(e -> e instanceof Building building && building.getProduces().contains(klass)).map(e -> (Building) e).sorted(Comparator.comparingInt(building -> building.getCommands().size())).toList();
                        if (producers.isEmpty()) {
                            System.out.println("No producers for " + klass.getSimpleName());
                            return;
                        } else {
                            Building building = producers.getFirst();
                            takeMoney(PlayableEntity.getCost(klass));
                            building.sendCommand(new Command.ProduceCommand(PlayableEntity.getBuildTime(klass), (Class<? extends Unit>) klass, level.getPathfinder()));
                            SoundManager.play(SoundManager.SOUND_BLIP_1, false, 0.5f);
                            return;
                        }
                    } else {
                        System.out.println("Cannot build " + klass.getSimpleName());
                    }
                }
            }
        }
    }

    public List<PlayableEntity> getSelected() {
        return selected;
    }
}
