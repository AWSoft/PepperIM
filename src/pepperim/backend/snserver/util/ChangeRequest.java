/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend.snserver.util;

import java.nio.channels.SocketChannel;

/**
 * Class ChangeRequest
 * @author Felix Wiemuth
 */
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
