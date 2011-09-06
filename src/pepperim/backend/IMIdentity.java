/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import pepperim.utils.*;
import pepperim.backend.IMContactList;
import java.io.*;
import java.util.ArrayList;
import java.security.PrivateKey;
import java.security.PublicKey;
import net.sf.json.*;

/**
 * Class representing an individual account.
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMIdentity {

    private String id = null;
    private String strpub = null;
    private String strpriv = null;
    private JSONObject extdata = null;
    private IMContactList clist = null;

    //not directly saved in json
    private String pass=null; 
    private PrivateKey priv = null;
    private PublicKey pub = null;
    //..........

    /**
     * @return JSON form of the IMIdentity object, used for saving
     */
    private JSONObject toJSONObject() {
        ArrayList<String> keysarr = new ArrayList<String>();
        keysarr.add(strpub);
        keysarr.add(strpriv);
        JSONObject ret = new JSONObject().element("id",id).element("keypair",keysarr)
                               .element("contacts",clist.toJSONArray()).element("extdata",extdata);
        System.out.println(ret.toString());
        return ret;
    }

    //create a new identity
    /**
     * A new identity containing an RSA keypair, ID and an empty contact list
     */
    public IMIdentity() {
        //Initialize new identity (RSA keys)
        String[] keypair = IMCrypt.RSA_genKeypair();
        this.strpub = keypair[0];
        this.strpriv = keypair[1];
        this.id = IDfromPubkey(strpub);
        clist = new IMContactList();
        this.extdata = new JSONObject();

        //"derivatives"
        this.pub = IMCrypt.decodePublicKey(strpub);
        this.priv = IMCrypt.decodePrivateKey(strpriv);
    }

    /**
     * Identity from the given file
     * @param filename Filename of the identity file
     * @param password Password to be used to load the file (null or empty string, if the file is unencrypted)
     */
    public IMIdentity(String filename, String password) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        byte[] buffer = new byte[65536];
        int size=0;

        DataInputStream in = new DataInputStream(new FileInputStream(filename));

        while ((size = in.read(buffer)) != -1)
            data.write(buffer,0,size);
        in.close();

        String dat = IMCompress.unzip(data.toByteArray());

        if (password != null && !password.equals("")) {
            this.pass = IMCrypt.SHA512(password).substring(0,32);
            dat = IMCrypt.AES_Dec(dat,pass);
            if (dat.equals(""))
                throw new Exception("Invalid Identity password!");
        }
        
        try {
          JSONObject iddata = (JSONObject) JSONSerializer.toJSON( dat );

          id = iddata.getString("id");
          JSONArray keys = iddata.getJSONArray("keypair");
          this.strpub = keys.getString(0);
          this.strpriv = keys.getString(1);
          this.clist = new IMContactList(iddata.getJSONArray("contacts"));
          this.extdata = iddata.getJSONObject("extdata");

          //"derivatives"
          this.pub = IMCrypt.decodePublicKey(strpub);
          this.priv = IMCrypt.decodePrivateKey(strpriv);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception("Identity file is password protected!");
        }
    }

    /**
     * Save the identity with the default filename ("[Messenger ID].id")
     */
    public boolean save() {
        return save(id+".id");
    }

    /**
     * Save the identity to a file
     * @param filename filename for the identity
     * @return true on success, false on failure
     */
    public boolean save(String filename) {
        String data = toJSONObject().toString();
        try {
          
          if (this.pass != null) {
              data = IMCrypt.AES_Enc(data, pass);
              if (data.equals(""))
                  return false;
          }

          byte[] dat = IMCompress.zip(data);
          DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
          out.write(dat,0,dat.length);
          out.close();
          return true;
        }
        
        catch (IOException e) {
          e.printStackTrace();
          return false;
        }
    }

    /**
     * Calculates the Messenger ID for an identity from its base64-encoded public key.
     * @param b64pubkey Base64-encoded public key
     * @return Messenger ID
     */
    private static String IDfromPubkey(String b64pubkey) {
        String hash = IMCrypt.SHA512(b64pubkey);
        return hash.substring(0,8);
    }

   /**
     * Calculates the Messenger ID for an identity from its public key.
     * @param pub Public key
     * @return Messenger ID
     */
    public static String IDfromPubkey(PublicKey pub) {
        return IDfromPubkey(IMCrypt.B64_Enc(pub.getEncoded()));
    }
    

    //-----------------------------------------------

    public PrivateKey getPrivateKey() {
        return this.priv;
    }
    public PublicKey getPublicKey() {
        return this.pub;
    }
    /**
     * @return Base64-encoded public key
     */
    public String getEncodedPublicKey() {
        return this.strpub;
    }

    public String getID() {
        return this.id;
    }

    public IMContactList getContactList() {
        return this.clist;
    }

    /**
     * Set a password for the identity (will be encrypted on the next save)
     * @param pw Password to be used
     */
    public void setPassword(String pw) {
        this.pass = IMCrypt.SHA512(pw).substring(0,32);
    }

    /**
     * Remove the password from the identity file (will be saved unencrypted next time)
     */
    public void removePassword() {
        this.pass = null;
    }

    /**
     * @return the stored JSON object stored by a plugin
     */
    public JSONObject getExtensionData(String extname) {
        return extdata.optJSONObject(extname);
    }

    /**
     * Store a JSON object for an extension within the identity.
     * Overwrites, if there is already data saved.
     * @param extname Extension name for which to save data
     * @param obj JSON object containing extension/plugin data
     */
    public void setExtensionData(String extname, JSONObject obj) {
        extdata.element(extname, obj);
    }
}
