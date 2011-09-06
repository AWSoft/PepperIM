package pepperim.backend.snserver.util;

//TODO types for REGISTER KEY-ID, ...

import java.nio.channels.SocketChannel;

public class ChangeRequest {
    public enum Type {
        REGISTER,
        CHANGEOPS
    }

    public SocketChannel channel;
    public Type type;
    public int ops;

    public ChangeRequest(SocketChannel socket, Type type, int ops) {
            this.channel = socket;
            this.type = type;
            this.ops = ops;
    }
}
