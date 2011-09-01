package pepperim.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import java.util.Random;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * A class providing all cryptography-relevant functions.
 * @author      Anton Pirogov <anton dot pirogov at googlemail dot com> 
 */
public class IMCrypt {
    static final String HEXES = "0123456789abcdef";

    /**
     * Returns random hexadecimal digits. (for AES encryption please use {@link #AES_genKey()})
     * @param num Number of digits
     * @return the hexadecimal string
     */
    public static String randHex(int num) {
        Random prng = new Random();
        String ret = "";
        for(int i=0; i<num; i++) {
            int ind = prng.nextInt(16);
            ret += HEXES.substring(ind,ind+1);
        }
        return ret;
    }

    /**
     * @param data data to be encoded
     * @return encoded data string
     */
    public static String B64_Enc(byte[] data) {
        return new String(new Base64().encodeBase64(data));
    }
    
    /**
     * @param data data string to be decoded
     * @return decoded data
     */
    public static byte[] B64_Dec(String data) {
        return new Base64().decodeBase64(data);
    }

    /**
     * @param s hexadecimal string
     * @return binary data
     */
    public static byte[] hexToBin(String s) {
      byte[] b = new byte[s.length() / 2];
      for (int i = 0; i < b.length; i++){
        int v = Integer.parseInt(s.substring(i*2, (i*2)+2), 16);
        b[i] = (byte)v;
      }
      return b;
    }

    /**
     * @param raw binary data
     * @return hexadecimal string
     */
    public static String binToHex( byte [] raw ) {
        if ( raw == null ) {
            return null;
        }
        final StringBuffer hex = new StringBuffer( 2 * raw.length );
        for ( final byte b : raw ) {
          hex.append(HEXES.charAt((b & 0xF0) >> 4))
             .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    
    /**
     * @param text String to be hashed
     * @return SHA512-hash
     */
    public static String SHA512(String text)
    {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-512");
            byte[] hash = new byte[40];
            md.update(text.getBytes("UTF-8"), 0, text.length());
            hash = md.digest();
            return binToHex(hash);
        }
        
        catch (NoSuchAlgorithmException ex)
        {
            System.out.println(ex.getMessage());
            return "";
        }
        catch (UnsupportedEncodingException ex)
        {   
            System.out.println(ex.getMessage());
            return "";
        }
    }
    


    /**
     * Generates a random string to be used as AES encryption key
     * @return random AES encryption key
     */
    public static String AES_genKey() {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
              
            kgen.init(128);
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
 
            return binToHex(raw);
        
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * @param data data to be encrypted
     * @param key key to be used
     * @return Base64-encoded encrypted data
     */
    public static String AES_Enc(String data, String key) {
        try {
        SecretKeySpec skeySpec = new SecretKeySpec(hexToBin(key), "AES");
 
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return B64_Enc(cipher.doFinal(data.getBytes()));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }
    
    /**
     * @param data data to be decrypted (Base64-encoded)
     * @param key key to be used
     * @return decrypted data
     */
    public static String AES_Dec(String data, String key) {
        try {
        SecretKeySpec skeySpec = new SecretKeySpec(hexToBin(key), "AES");
 
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return new String(cipher.doFinal(B64_Dec(data)));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * Generates a new 2048 bit RSA keypair.
     * @return String array containing: [Base64-encoded public key, Base64-encoded private key]
     */
    public static String[] RSA_genKeypair()
    {
        try {
            KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = new SecureRandom();
            pairgen.initialize(2048, random);
            KeyPair keyPair = pairgen.generateKeyPair();
            String[] keypair = new String[2];
            keypair[0] = B64_Enc(keyPair.getPublic().getEncoded());
            keypair[1] = B64_Enc(keyPair.getPrivate().getEncoded());
            return keypair;
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * @param b64str Base64-encoded private key
     * @return PrivateKey object
     */
    public static PrivateKey decodePrivateKey(String b64str) {
        try {
            byte[] keydata = B64_Dec(b64str);
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(keydata);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pk = kf.generatePrivate(ks);
            return pk;
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * @param b64str Base64-encoded public key
     * @return PublicKey object
     */
    public static PublicKey decodePublicKey(String b64str) {
        try {
            byte[] keydata = B64_Dec(b64str);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(keydata);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pk = kf.generatePublic(ks);
            return pk;
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * RSA encryption. You can use a PrivateKey here, if you need a custom signing algorithm (otherwise you can use {@link #RSA_Sign(String,PrivateKey)})
     * @param data Data to be encrypted (should not be more than some dozen bytes!)
     * @param key Encryption key (PublicKey or PrivateKey)
     * @return Base64-encoded encrypted data
     */
    public static String RSA_Enc(String data, Key key) {
        try {
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.ENCRYPT_MODE, key);
        return B64_Enc(c.doFinal(data.getBytes()));
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * @param data Base64-encoded encrypted data
     * @param key Decryption key (normally PrivateKey, but can also be PublicKey if you do custom signing)
     * @return decrypted data
     */
    public static String RSA_Dec(String data, Key key) {
        try {
        Cipher c = Cipher.getInstance("RSA");
        c.init(Cipher.DECRYPT_MODE, key);
        return new String(c.doFinal(B64_Dec(data)));
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * Regular RSA signing (using SHA1-hash)
     * @param data Data to be signed
     * @param key Key to be used for the signature
     * @return Base64-encoded RSA signature
     */
    public static String RSA_Sign(String data, PrivateKey key) {
        try {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(key);
        signer.update(data.getBytes());
        byte[] signature = signer.sign();
        return B64_Enc(signature);
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    /**
     * @param data Data to be verified
     * @param b64sig Base64-encoded RSA signature
     * @param key Public key of the signature source
     * @return true on success, false if the verification fails
     */
    public static boolean RSA_Verify(String data, String b64sig, PublicKey key) {
        try {
        Signature verifier = Signature.getInstance("SHA1withRSA");
        verifier.initVerify(key);
        verifier.update(data.getBytes());
        return verifier.verify(B64_Dec(b64sig));
        } catch (GeneralSecurityException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
