package sk.neuromancer.Xune.network.controllers;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.unit.Unit;

public class CombinedController implements Controller {

    private final Controller[] controllers;

    public CombinedController(Controller... controllers) {
        this.controllers = controllers;
    }


    @Override
    public void produceUnit(Class<? extends Unit> klass, Building producer) {
        for (Controller controller : controllers) {
            controller.produceUnit(klass, producer);
        }
    }

    @Override
    public void produceBuilding(Class<? extends Building> klass) {
        for (Controller controller : controllers) {
            controller.produceBuilding(klass);
        }
    }

    @Override
    public void placeBuilding(Building building) {
        for (Controller controller : controllers) {
            controller.placeBuilding(building);
        }
    }

    @Override
    public void pushCommand(Unit unit, Command command) {
        for (Controller controller : controllers) {
            controller.pushCommand(unit, command);
        }
    }

    @Override
    public void sendCommand(Unit unit, Command command) {
        for (Controller controller : controllers) {
            controller.sendCommand(unit, command);
        }
    }
}
