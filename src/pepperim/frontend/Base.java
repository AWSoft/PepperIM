/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.frontend;

import pepperim.backend.snserver.SNServer;
import pepperim.backend.snserver.util.RawMessage;

/**
 * Class handling the server and message processing.
 * Has to be implemented by a user interface.
 * Start with start().
 * @author Felix Wiemuth
 */
public abstract class Base extends Thread {
    //Server
    protected SNServer server;

    /**
     * Loop listening and processing messages (to be run in a thread)
     */
    public void processMessages() {
        RawMessage raw;

        while(true) {
            while(true) {
                raw = server.getMessage();

                if (raw == null) {
                    break; //go to sleep again
                }
                else {
                    switch(raw.getType()) {
                        case CLIENT_MSG:
                            process_clientMsg(raw.getMsg());
                            break;
                        default:
                            process_systemMsg(raw);
                            break;
                    }
                }
            }
            try { Thread.sleep(50); } catch (Exception e) {}
        }
    }

    public abstract void process_clientMsg(String msg);

    public abstract void process_systemMsg(RawMessage sys);

    /**
     * Display an incoming client message via the user interface to the user.
     * @param msg Message to be displayed
    */
    public abstract void displayMsg(String msg);

    @Override
    public void run() {
      server.start();
    }
}
