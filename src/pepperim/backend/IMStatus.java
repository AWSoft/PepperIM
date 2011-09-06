/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

/**
 * Class representing the online state of a client
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMStatus {
    public enum Status {
        OFFLINE,ONLINE,AFK,AWAY,BUSY;
    }

    private Status st=Status.OFFLINE;
    private String msg="";

    /**
     * Create a new status from scratch
     * @param st Status value
     * @param msg Status message
     */
    public IMStatus(Status st, String msg) {
        this.st = st;
        this.msg = msg;
    }

    /**
     * Parse a status from its String representation.
     * @param ststr Status-String
     */
    public IMStatus(String ststr) {
        String[] states = { "OFFLINE","ONLINE","AFK","AWAY","BUSY" };
        st = Status.valueOf(states[Integer.parseInt(ststr.substring(0,1))]);
        if (ststr.length()>1)
          msg = ststr.substring(1,ststr.length());
    }

    /**
     * @return String representation (for network transmission)
     */
    public String toString() {
        String ret = "";
        ret += String.valueOf(st.ordinal());
        if (msg != null)
            ret += msg;
        return ret;
    }

    /**
     * @return Status value
     */
    public Status getStatus() {
        return st;
    }

    /**
     * @return Status message
     */
    public String getMessage() {
        if (msg == null)
            return "";
        return msg;
    }
}
