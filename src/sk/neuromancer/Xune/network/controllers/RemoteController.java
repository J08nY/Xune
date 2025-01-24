package sk.neuromancer.Xune.network.controllers;

import com.github.quantranuk.protobuf.nio.ProtoSocketChannel;
import sk.neuromancer.Xune.entity.PlayableEntity;
import sk.neuromancer.Xune.entity.building.Building;
import sk.neuromancer.Xune.entity.command.Command;
import sk.neuromancer.Xune.entity.unit.Unit;
import sk.neuromancer.Xune.proto.BaseProto;
import sk.neuromancer.Xune.proto.MessageProto;

public class RemoteController implements Controller {
    private final ProtoSocketChannel channel;

    public RemoteController(ProtoSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void produceUnit(Class<? extends Unit> klass, Building producer) {
        channel.sendMessage(MessageProto.Action.newBuilder()
                .setEntityProduce(MessageProto.EntityProduceAction.newBuilder()
                        .setKlass(PlayableEntity.toEntityClass(klass))
                        .setProducerId(producer.getId())
                        .build())
                .build());
    }

    @Override
    public void produceBuilding(Class<? extends Building> klass) {
        channel.sendMessage(MessageProto.Action.newBuilder()
                .setBuildingProduce(MessageProto.BuildingProduceAction.newBuilder()
                        .setKlass(PlayableEntity.toEntityClass(klass))
                        .build())
                .build());
    }

    @Override
    public void placeBuilding(Building building) {
        channel.sendMessage(MessageProto.Action.newBuilder()
                .setBuildingPlace(MessageProto.BuildingPlaceAction.newBuilder()
                        .setKlass(PlayableEntity.toEntityClass(building.getClass()))
                        .setPosition(BaseProto.Tile.newBuilder()
                                .setX(building.tileX)
                                .setY(building.tileY)
                                .build())
                        .build())
                .build());
    }

    @Override
    public void sendCommand(Unit unit, Command command) {
        channel.sendMessage(MessageProto.Action.newBuilder()
                .setSendCommand(MessageProto.SendCommandAction.newBuilder()
                        .setEntityId(unit.getId())
                        .setCommand(command.serialize())
                        .build())
                .build());
    }

    @Override
    public void pushCommand(Unit unit, Command command) {
        channel.sendMessage(MessageProto.Action.newBuilder()
                .setPushCommand(MessageProto.PushCommandAction.newBuilder()
                        .setEntityId(unit.getId())
                        .setCommand(command.serialize())
                        .build())
                .build());
    }
}
