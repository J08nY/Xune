package sk.neuromancer.Xune.game.players;

import sk.neuromancer.Xune.entity.*;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.command.CommandStrategy;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.input.InputHandler;
import sk.neuromancer.Xune.graphics.HUD;
import sk.neuromancer.Xune.graphics.LevelView;
import sk.neuromancer.Xune.level.Level;
import sk.neuromancer.Xune.proto.PlayerProto;
import sk.neuromancer.Xune.sound.SoundManager;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glColor4f;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelX;
import static sk.neuromancer.Xune.level.Level.tileToCenterLevelY;

public class Human extends Player {
    private final List<PlayableEntity> selected = new LinkedList<>();
    private Building buildingToPlace;
    private boolean canPlace;

    private InputHandler input;
    private LevelView view;
    private HUD hud;

    public Human(Level level, Flag flag, int money) {
        super(level, flag, money);
        setupSpawn();
    }

    public Human(Level level, PlayerProto.PlayerState playerState) {
        super(level, playerState);
        // TODO: Handle this somewhere else
        //game.getView().xOff = playerState.getHuman().getXOffset();
        //game.getView().yOff = playerState.getHuman().getYOffset();
        //game.getView().zoom = playerState.getHuman().getZoom();
    }

    @Override
    public void removeEntity(PlayableEntity e) {
        super.removeEntity(e);
        selected.remove(e);
    }

    @Override
    public void tick(int tickCount) {
        handleInput(tickCount);
        super.tick(tickCount);
    }

    private void handleInput(int tickCount) {
        //TODO: How is Human input called when the player is not getting ticked?
        InputHandler.Mouse mouse = input.mouse;
        float mouseX = (float) mouse.getX();
        float mouseY = (float) mouse.getY();
        float fromX = (float) mouse.getLastLeftX();
        float fromY = (float) mouse.getLastLeftY();

        if (hud.isMouseOverHud(mouseY)) {
            if (mouse.isLeftReleased()) {
                handleHUDClick(mouseX, mouseY);
            }
            return;
        }

        float levelX = view.getLevelX(mouseX);
        float levelY = view.getLevelY(mouseY);
        float fromLevelX = view.getLevelX(fromX);
        float fromLevelY = view.getLevelY(fromY);

        if (buildingToPlace != null) {
            int tileX = Level.levelToTileX(levelX, levelY);
            int tileY = Level.levelToTileY(levelX, levelY);
            float levelCenterX = tileToCenterLevelX(tileX, tileY);
            float levelCenterY = tileToCenterLevelY(tileX, tileY);
            buildingToPlace.setPosition(levelCenterX, levelCenterY);
            canPlace = isTileDiscovered(tileX, tileY) && level.isTileBuildable(tileX, tileY, buildingToPlace.getPassable());
        }

        if (mouse.isLeftReleased()) {
            if (mouse.wasLeftDrag()) {
                if (Math.abs(fromX - mouseX) < 5 && Math.abs(fromY - mouseY) < 5) {
                    handleLeftClick(levelX, levelY, tickCount);
                } else {
                    handleDrag(fromLevelX, fromLevelY, levelX, levelY);
                }
            } else {
                handleLeftClick(levelX, levelY, tickCount);
            }
        }
        if (mouse.isRightReleased()) {
            handleRightClick();
        }
    }

    private void handleRightClick() {
        if (buildingToPlace != null) {
            buildingToPlace = null;
            return;
        }
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

    private void handleLeftClick(float levelX, float levelY, int tickCount) {
        // Handle building placement first
        if (buildingToPlace != null) {
            if (canPlace) {
                finishBuild(buildingToPlace);
                buildingToPlace = null;
                SoundManager.play(SoundManager.SOUND_TADA_1, false, 0.5f);
            }
            return;
        }

        // Handle selection changes
        Entity other = level.entityAt(levelX, levelY);
        boolean allUnits = !selected.isEmpty() && selected.stream().allMatch(e -> e instanceof Unit);
        boolean allBuildings = !selected.isEmpty() && selected.stream().allMatch(e -> e instanceof Building);

        if (other instanceof PlayableEntity playable && entities.contains(playable)) {
            // We clicked on something ours.

            if (selected.contains(playable)) {
                // We clicked on something selected, deselect it.
                selected.remove(playable);
                playable.unselect();
                return;
            } else {
                // We clicked on something not selected.
                if ((allUnits && playable instanceof Building) || (allBuildings && playable instanceof Unit)) {
                    // Do not change selection, instead continue to the commands
                } else {
                    selected.add(playable);
                    playable.select();
                    return;
                }
            }
        }

        if (other != null && !isTileVisible(level.tileAt(levelX, levelY))) {
            other = null;
        }

        for (PlayableEntity only : selected) {
            CommandStrategy strategy = commandStrategies.get(only.getClass());
            if (strategy != null) {
                Command command = strategy.onClick(only, other, level, levelX, levelY);
                if (command != null) {
                    only.pushCommand(command);
                }
            }
        }
    }

    private void handleHUDClick(float mouseX, float mouseY) {
        for (HUD.Button<?> button : hud.getButtons()) {
            if (button.intersects(mouseX, mouseY)) {
                Class<? extends PlayableEntity> klass = button.getKlass();
                if (Building.class.isAssignableFrom(klass)) {
                    if (buildingToBuild == null) {
                        if (PlayableEntity.canBeBuilt(klass, this)) {
                            // Building produce.
                            startBuild(klass);
                        }
                    } else {
                        if (buildingToBuild == klass && isBuildDone()) {
                            // Building place.
                            float levelX = view.getLevelX(mouseX);
                            float levelY = view.getLevelY(mouseY);
                            int tileX = Level.levelToTileX(levelX, levelY);
                            int tileY = Level.levelToTileY(levelX, levelY);
                            buildingToPlace = getBuildResult(tileX, tileY);
                        }
                    }
                } else if (Unit.class.isAssignableFrom(klass)) {
                    if (PlayableEntity.canBeBuilt(klass, this)) {
                        List<Building> producers = entities.stream().filter(e -> e instanceof Building building && building.getProduces().contains(klass)).map(e -> (Building) e).sorted(Comparator.comparingInt(building -> building.getCommands().size())).toList();
                        if (!producers.isEmpty()) {
                            // Unit produce.
                            Building building = producers.getFirst();
                            takeMoney(PlayableEntity.getCost(klass));
                            building.sendCommand(new Command.ProduceCommand(PlayableEntity.getBuildTime(klass), klass.asSubclass(Unit.class), level.getPathfinder()));
                            SoundManager.play(SoundManager.SOUND_BLIP_1, false, 0.5f);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        super.render();
        if (buildingToPlace != null) {
            if (canPlace) {
                glColor4f(1, 1, 1, 1);
            } else {
                glColor4f(1, 0, 0, 0.5f);
            }
            buildingToPlace.render();
            glColor4f(1, 1, 1, 1);
        }
    }

    public Class<? extends Building> getBuildingToBuild() {
        return buildingToBuild;
    }

    public List<PlayableEntity> getSelected() {
        return selected;
    }

    public PlayerProto.PlayerState serialize() {
        PlayerProto.PlayerState.Builder state = super.serialize().toBuilder();
        PlayerProto.HumanState.Builder human = PlayerProto.HumanState.newBuilder()
                .setXOffset(view.xOff)
                .setYOffset(view.yOff)
                .setZoom(view.zoom);
        state.setHuman(human);
        return state.build();
    }

    public void setInput(InputHandler input) {
        this.input = input;
    }

    public void setView(LevelView view) {
        this.view = view;
    }

    public void setHud(HUD hud) {
        this.hud = hud;
    }
}
