/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/
package pepperim.frontend;

import pepperim.backend.IMContactList;
import pepperim.backend.IMContact;
import pepperim.backend.IMIdentity;

import java.net.InetSocketAddress;
import pepperim.backend.IMStatus;

import java.security.PublicKey;
import java.util.HashMap;

/**
 * Class which wraps the Identity contact list and stores additional
 * runtime data like current status and IP (which is not saved)
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com> *
 */
class RuntimeContactList {

    private IMContactList list = null;

    private HashMap<String, IMStatus> stati = null;
    private HashMap<String, InetSocketAddress> addrs = null;
    private HashMap<String, PublicKey> pkeys = null;

    public RuntimeContactList(IMIdentity id) {
      this.list = id.getContactList();
      stati = new HashMap<String, IMStatus>();
      addrs = new HashMap<String, InetSocketAddress>();
      pkeys = new HashMap<String, PublicKey>();
    }

    /**
     * Wraps IMContactList.add(...)
     */
    public boolean add(String id) {
        stati.put(id, new IMStatus(IMStatus.Status.OFFLINE,"")); //assume offline as default

        return list.add(id);
    }

    /**
     * Wraps IMContactList.remove(...)
     */
    public boolean remove(String id) {
        //remove runtime data too, if present
        addrs.remove(id);
        stati.remove(id);
        pkeys.remove(id);

        return list.remove(id);
    }

    /**
     * Wraps IMContactList.get(...)
     */
    public IMContact get(String id) {
        return list.get(id);
    }

    /**
     * Wraps IMContactList.getIDs()
     */
    public String[] getIDs() {
        return list.getIDs();
    }

    public boolean updateAddress(String id, InetSocketAddress addr) {
        if (get(id) == null)
            return false; //Not in contact list

        addrs.put(id, addr);
        return true;
    }

    public InetSocketAddress getAddress(String id) {
        return addrs.get(id);
    }

    public boolean updatePublicKey(String id, PublicKey k) {
        if (get(id) == null)
            return false; //Not in contact list

        pkeys.put(id, k);
        return true;
    }

    public PublicKey getPublicKey(String id) {
        return pkeys.get(id);
    }

    public boolean updateStatus(String id, IMStatus status) {
        if (get(id) == null)
            return false; //Not in contact list

        stati.put(id, status);
        return true;
    }

    public IMStatus getStatus(String id) {
        IMStatus st = stati.get(id);
        if (st==null) {
            st = new IMStatus(IMStatus.Status.OFFLINE,"");
            stati.put(id,st);
        }

        return st;
    }

}
