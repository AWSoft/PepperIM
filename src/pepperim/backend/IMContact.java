/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import pepperim.backend.IMContact;
import pepperim.util.IMCrypt;
import java.security.PublicKey;
import net.sf.json.*;

/**
 * Class representing a contact. Do not create own instances of this class. Use the apropriate methods of the IMContactList instead.
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com>
 */
public class IMContact {


    private String id = null;
    private String alias = "";

    private boolean locauth = false;
    private boolean remauth = false;

    /**
     * Create a new contact from scratch
     */
    public IMContact(String id) {
        this.id = id;
    }

    /**
     * Load a contact from its json representation
     * @param json Contact in JSON format
     */
    public IMContact(JSONObject json) {
        id = json.getString("id");
        alias = json.getString("alias");
        locauth = json.getBoolean("lauth");
        remauth = json.getBoolean("rauth");
    }

    /**
     * @param al New alias for the contact (set to null or empty string to remove alias)
     */
    public void setAlias(String al) {
        if (al == null || al.equals(""))
            alias = "";
        else
            this.alias = al;
    }

    /**
     * Update local authorization state for this contact (should be done in conjunction with auth messages)
     * @param state true if you want to receive messages from this contact, false if you don't
     */
    public void setAuthorized(boolean state) {
        locauth = state;
    }

    /**
     * Update remote authorization state for this contact (should be done in conjunction with auth messages)
     * @param state true if you can send messages to this contact (e.g. got MSG_authacc), false if you can't (e.g. got MSG_authdeny)
     */
    public void setAuthorizes(boolean state) {
        remauth = state;
    }

    /**
     * @return The alias of the contact
     */
    public String getAlias() {
        if (alias != null && !alias.equals(""))
          return alias;
        else
          return null; //no or empty alias
    }

    /**
     * @return Local authorization state
     */
    public boolean getAuthorized() {
        return locauth;
    }

    /**
     * @return Remote authorization state
     */
    public boolean getAuthorizes() {
        return remauth;
    }

    /**
     * @return ID of this contact
     */
    public String getID() {
        return id;
    }

    /**
     * @return JSON encoded string of contact
     */
    public JSONObject toJSONObject() {
        return new JSONObject().element("id", id).element("alias", alias).element("lauth", locauth).element("rauth", remauth);
    }

}
