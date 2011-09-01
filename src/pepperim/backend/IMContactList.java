package pepperim.backend;

import pepperim.backend.IMContact;

import java.util.ArrayList;
import java.security.PublicKey;

import net.sf.json.*;

/**
 * Class representing a identity's contact list. You should not create own instances of this. Use IMIdentity.getContactList()
 * @author      Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMContactList {

    private ArrayList<IMContact> contacts = null;

    /**
     * Initializes an empty contact list
     */
    public IMContactList() {
        contacts = new ArrayList<IMContact>();
    }

    /**
     * restores a contact list from the JSONArray saved in the identity file.
     * You should not use this in your code (basically a routine for IMIdentity)
     * @param cts JSONArray containing the contacts
     */
    public IMContactList(JSONArray cts) {
        contacts = new ArrayList<IMContact>();
        for(int i=0; i<cts.size(); i++) {
            contacts.add(new IMContact(cts.getJSONObject(i)));
        }
    }

    /**
     * @return JSONArray of command list
     */
    public JSONArray toJSONArray() {
        JSONArray json = new JSONArray();
        for(int i=0; i<contacts.size(); i++) {
            json.element(contacts.get(i).toJSONObject());
        }
        return json;
    }

    /**
     * Shortcut for toJSONArray().toString()
     * @return stringified contact list
     */
    public String toString() {
        return toJSONArray().toString();
    }


    /**
     * Add a new contact list entry
     * @param id Messenger ID to be added
     * @return true on success, false on failure (e.g. already exists)
     */
    public boolean add(String id, PublicKey key) {
        for(int i=0; i<contacts.size(); i++) {
            if (contacts.get(i).getID().equals(id))
                return false;
        }

        contacts.add(new IMContact(id, key));
        return true;
    }

    /**
     * Remove a contact list entry
     * @param id Messenger ID of contact to be removed
     * @return true on success, false on failure (e.g. no such contact in list)
     */
    public boolean remove(String id) {
        for (int i=0; i<contacts.size(); i++) {
            if (contacts.get(i).getID().equals(id)) {
                contacts.remove(i);
                return true;

            }
        }
        return false;
    }

    /**
     * Get a reference to a contact object (which is used to manipulate contacts)
     * @param id Messenger ID of the contact
     * @return IMContact object reference (or null if not found)
     */
    public IMContact get(String id) {
        for (int i=0; i<contacts.size(); i++) {
            if (contacts.get(i).getID().equals(id)) {
                return contacts.get(i);
            }
        }
        return null;
    }

    /**
     * Get a list of all stored contact IDs
     * @return The array with the IDs
     */
    public String[] getIDs() {
        ArrayList<String> ids = new ArrayList<String>();
        for(int i=0; i<contacts.size(); i++) {
            ids.add(contacts.get(i).getID());
        }

        return ids.toArray(new String[ids.size()]);
    }

}
