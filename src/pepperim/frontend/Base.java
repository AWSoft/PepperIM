/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.frontend;

import pepperim.backend.snserver.SNServer;
import pepperim.backend.snserver.util.RawMessage;
import pepperim.util.SleepControl;
import pepperim.util.SleepControl.SleepControlSettingsException;

/**
 * Class handling the main application flow.
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
        int[] sleepTime = {100, 1000, 5000};
        int[] increaseTime = {0, 10000, 120000};
        SleepControl sleepControl = null;
        try {
            sleepControl = new SleepControl(sleepTime, increaseTime);
        }
        catch (SleepControlSettingsException e) {
            //--- changed code to avoid exception ---
        }

        RawMessage raw;

        while(true) {
            while(true) {
                raw = server.getMessage();

                //no new message -> update sleepMode
                if (raw == null) {
                    sleepControl.count();
                    break; //go to sleep again
                }
                else {
                    sleepControl.reset();
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
            try { sleepControl.sleep(); } catch (InterruptedException e) {}
        }
    }

    public void process_clientMsg(String msg) {

    }

    public void process_systemMsg(RawMessage raw) {

    }

    /**
     * Display an incoming client message via the user interface to the user.
     * @param msg Message to be displayed
    */
    public abstract void displayMsg(String msg);

    protected void initServer(int port) {
      server = new SNServer(port);
    }

    @Override
    public void run() {
      server.start();
    }
}
