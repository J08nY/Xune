package sk.neuromancer.Xune.network.controllers;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.unit.Unit;

public interface Controller {

    void produceUnit(Class<? extends Unit> klass, Building producer);

    void produceBuilding(Class<? extends Building> klass);

    void placeBuilding(Building building);

    void pushCommand(Unit unit, Command command);

    void sendCommand(Unit unit, Command command);
}
