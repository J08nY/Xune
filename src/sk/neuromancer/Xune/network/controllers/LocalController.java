package sk.neuromancer.Xune.network.controllers;

import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.game.players.Player;
import sk.neuromancer.Xune.level.Level;

public class LocalController implements Controller {
    private final Level level;
    private final Player player;
    private int tickCount = 0;

    public LocalController(Level level, Player player) {
        this.level = level;
        this.player = player;
    }

    @Override
    public void produceUnit(Class<? extends Unit> klass, Building producer) {
        player.takeMoney(PlayableEntity.getCost(klass));
        producer.sendCommand(new Command.ProduceCommand(PlayableEntity.getBuildTime(klass), klass.asSubclass(Unit.class), level.getPathfinder()));
    }

    @Override
    public void produceBuilding(Class<? extends Building> klass) {
        player.startBuild(klass);
    }

    @Override
    public void placeBuilding(Building building) {
        player.finishBuild(building);
    }

    @Override
    public void pushCommand(Unit unit, Command command) {
        unit.pushCommand(command);
    }

    @Override
    public void sendCommand(Unit unit, Command command) {
        unit.sendCommand(command);
    }

    @Override
    public void tick(int tickCount) {
        this.tickCount = tickCount;
    }
}
