package sk.neuromancer.Xune.network;

import com.github.quantranuk.protobuf.nio.serializer.IdSerializer;
import sk.neuromancer.Xune.proto.MessageProto;

import java.util.List;

public class Utils {

    public static IdSerializer getIdSerializer() {
        return IdSerializer.create(List.of(MessageProto.State.class, MessageProto.Action.class, MessageProto.Note.class));
    }
}
