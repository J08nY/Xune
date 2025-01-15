package sk.neuromancer.Xune.entity;

import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.game.players.Player;

public class Prerequisite {
    private final Class<? extends Building> requiredBuilding;

    public Prerequisite(Class<? extends Building> requiredBuilding) {
        this.requiredBuilding = requiredBuilding;
    }

    public boolean isMet(Player owner) {
        for (PlayableEntity entity : owner.getEntities()) {
            if (requiredBuilding.isInstance(entity)) {
                return true;
            }
        }
        return false;
    }
}