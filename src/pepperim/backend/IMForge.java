/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import pepperim.util.*;
import pepperim.backend.IMIdentity;
import pepperim.backend.IMStatus;
import java.util.Date;
import java.util.ArrayList;
import net.sf.json.*;
import java.security.PublicKey;
import java.security.PrivateKey;

/**
 * Class providing methods to pack/unpack ready-to-send packages
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMForge {

    IMIdentity imid = null;

    /**
     * Create a new IMForge instance
     * @param id Identity to be used (for encryption, etc.)
     */
    public IMForge(IMIdentity id) {
        this.imid = id;
    }

    /**
     * @return message to associate a messenger ID with an IP (to be sent to the ID server) ("" on failure)
     */
    public String SVR_announce() {
        String signedid = IMCrypt.RSA_Enc(imid.getID(), imid.getPrivateKey());
        if (signedid.equals(""))
            return ""; //something went wrong Oo
        String pub = imid.getEncodedPublicKey();
        return "HELLO "+pub+" "+signedid+"\n";
    }

    /**
     * @param id Messenger ID
     * @return IP request for this ID (to be sent to the messenger server)
     */
    public String SVR_getip(String id) {
        return "GET "+id+"\n";
    }

    /* Crypto-layer (+IM message wrapper) */

    /**
     * @return Public key request
     */
    public String requestPubkey() {
        JSONObject json = new JSONObject();
        json.element("from",imid.getID());
        json.element("type","KEYPLZ");
        return json.toString();
    }

    /**
     * @return Public key response
     */
    public String sendPubkey() {
        JSONObject json = new JSONObject();
        json.element("from",imid.getID());
        json.element("type","KEY");
        json.element("data",imid.getEncodedPublicKey());
        return json.toString();
    }

    /**
     * @return Message acknowledgement (for delivery verification)
     */
    public String ackMsg(String msgid) {
        JSONObject json = new JSONObject();
        json.element("from",imid.getID());
        json.element("type","ACK");
        json.element("data",msgid);
        return json.toString();
    }

    /**
     * Wraps a message with AES and RSA
     * @param jsondata An object containing an arbitrary valid message (use the MSG_* methods to generate)
     * @param dest key of the destination
     * @return packed/encrypted message (ready to send) on success, "" on failure
     */
    public String packMessage(JSONObject jsondata, PublicKey dest) {
        String key = IMCrypt.AES_genKey();
        
        String enckey = IMCrypt.RSA_Enc(key, dest);
        if (enckey.equals(""))
            return "";

        String encdata = IMCrypt.AES_Enc(jsondata.toString(), key);
        if (encdata.equals(""))
            return "";

        String signature = IMCrypt.RSA_Enc(IMCrypt.SHA512(encdata), imid.getPrivateKey());
        if (signature.equals(""))
            return "";

        ArrayList<String> arr = new ArrayList<String>();
        arr.add(enckey);
        arr.add(encdata);
        JSONArray enc = (JSONArray) JSONSerializer.toJSON(arr);

        JSONObject json = new JSONObject();
        json.element("from",imid.getID());
        json.element("type","ENCMSG");
        json.element("data",enc);
        json.element("sign",signature);
        return json.toString();
    }

    /**
     * @param msgdata The packed message
     * @param src public key of the message source (for verification)
     * @return The actual message on success, null on failure
     */
    public JSONObject unpackMessage(String msgdata, PublicKey src) {
        JSONObject dat = (JSONObject) JSONSerializer.toJSON(msgdata);

        try {
          String type = dat.getString("type");
          if (!type.equals("ENCMSG"))
            return null;

          String from = dat.getString("from");

          if (!from.equals(IMIdentity.IDfromPubkey(src)))
            return null;

          String sig = dat.getString("sign");
          JSONArray enc = dat.getJSONArray("data");
          String enckey = enc.getString(0);
          String encdata = enc.getString(1);

          String key = IMCrypt.RSA_Dec(enckey, imid.getPrivateKey());
          String hash = IMCrypt.RSA_Dec(sig, src);
          if (key.equals(""))
            return null;
          if (hash.equals(""))
            return null;
          if (!IMCrypt.SHA512(encdata).equals(hash))
            return null;

          String data = IMCrypt.AES_Dec(encdata, key);
          if (data.equals(""))
            return null;
          
          JSONObject unpacked = (JSONObject) JSONSerializer.toJSON(data);
          return unpacked;
        }

        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /* IM-Messages */
    
    /**
     * Regular instant message
     * @param data message plain text
     * @return instant message object
     */
    public JSONObject MSG_imtext(String data) {
        return newIMMessage("imtext").element("data",data);
    }

    /**
     * @return Message to be sent as response to a message from an unauthorized ID
     */
    public JSONObject MSG_noauth() {
        return newIMMessage("noauth");
    }

    /* status */

    /**
     * @return Status request message
     */
    public JSONObject MSG_getstatus() {
        return newIMMessage("getstatus");
    }

    /**
     * @param st New status to be propagated
     * @return Status broadcast message
     */
    public JSONObject MSG_setstatus(IMStatus st) {
        return newIMMessage("setstatus").element("data",st.toString());
    }

    /* authorization */
    
    /**
     * @param msg message (e.g. reason for authorization request)
     * @return Authorization reqest message
     */
    public JSONObject MSG_authreq(String msg) {
        return newIMMessage("authreq").element("data",msg);
    }

    /**
     * @return Authorization accept message
     */
    public JSONObject MSG_authacc() {
        return newIMMessage("authacc");
    }

    /**
     * @param msg message (e.g. reson for authorization denial)
     * @return Authorization denial message
     */
    public JSONObject MSG_authdeny(String msg) {
        return newIMMessage("authdeny").element("data",msg);
    }

    /**
     * @param msg message (e.g. reason for deauthorization)
     * @return Deauthorization message
     */
    public JSONObject MSG_deauth(String msg) {
        return newIMMessage("deauth").element("data",msg);
    }

    /* extension messages */

    /**
     * @param extname Name of the protocol extension
     * @param version Version of the protocol extension
     * @param data Arbitrary JSON data (as required by the extension)
     * @return correctly packed (but not encrypted) extension message
     */
    public JSONObject MSG_ext(String extname, int version, JSONObject data) {
        JSONObject extmeta = new JSONObject()
                                 .element("extname",extname)
                                 .element("version",version)
                                 .element("data",data);
        return newIMMessage("ext").element("data",extmeta);
    }

    /**
     * @param extname Name of the problematic / missing protocol extension
     * @return Extension failure message (e.g. no such extension or too old version)
     */
    public JSONObject MSG_extfail(String extname) {
        return newIMMessage("extfail").element("data",extname);
    }

    /*---------------------------------*/
 
    /**
     * @return Unique message ID consisting of the ID, a timestamp and random data.
     */
    private String genMsgID() {
        String id = imid.getID();
        id += "-";
        id += String.valueOf(new Date().getTime());
        id += "-";
        id += IMCrypt.randHex(4);
        return id;
    }

    /**
     * @return Initializes a new message object with some presets
     */
    private JSONObject newIMMessage(String type) {
        JSONObject o = new JSONObject();
        o.element("from", imid.getID());
        o.element("msgid", genMsgID());
        o.element("type",type);
        return o;
    }
}
