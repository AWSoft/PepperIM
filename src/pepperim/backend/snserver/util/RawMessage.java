/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend.snserver.util;

/**
 * Message type the server uses to transmit both system messages and
 * client messages to the main application
 * @author Felix Wiemuth
 */
public class RawMessage {
    public enum Type {
        /** Incoming raw message from a client */
        CLIENT_MSG,
        /** Client successfully established connection */
        CLIENT_CONNECTED,
        /** Client (with address in message) had a disconnect */
        CLIENT_DC,
    }
    private Type type;
    private String msg;

    public RawMessage(Type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    /**
     * @return Type of the Message
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The Message
     */
    public String getMsg() {
        return msg;
    }

}
