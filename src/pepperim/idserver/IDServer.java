/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.idserver;

import pepperim.util.IMCrypt;
import java.util.concurrent.ConcurrentHashMap;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Date;
import java.util.Enumeration;
import java.security.PublicKey;
import java.io.*;
import java.net.*;

/**
 * This is the server which lets clients associate their messenger IDs with their IPs
 * and responds to IP requests from the clients. The host of the server should make
 * sure to have a static IP or domain name.
 * @author Anton Pirogov
 */
public class IDServer implements Runnable {

  public static String GET    = "GET";
  public static String HELLO  = "HELLO";

  public static String INVALID= "INVALID_MSG_FORMAT";
  public static String FUCKOFF= "FUCKOFF";
  public static String UNKNOWN="UNKNOWN";
  public static String OK     ="OK";

  private int port = 1993;

  private int regLifespan = 720; //minutes a registration lasts
  private int cleanupInterval = 30;  //minutes between cleanups

  // Hashmap: id -> [ip, port, timestamp]
  private ConcurrentHashMap<String,String[]> ips = null;

  /**
   * Create an ID server.
   */
  public IDServer() {
      ips = new ConcurrentHashMap<String,String[]>();
  }

  /**
   * Start ID Server loop
   */
  public void run() {
    //schedule cleanup routine to be run regularly
    Timer cleaner = new Timer();
    cleaner.schedule( new TimerTask() {
            @Override
            public void run() {
                Enumeration<String> keys = ips.keys();
                while(keys.hasMoreElements()) {
                    String id = keys.nextElement();

                    //check entry age
                    String[] d = ips.get(id);
                    long time = new Date().getTime();
                    long stamp = Long.parseLong(d[2]);
                    long age = time - stamp;

                    if (age > regLifespan*1000*60)
                        ips.remove(id);
                }

                System.out.println("Online users: "+ips.size());
            }
        }, cleanupInterval*1000*60, cleanupInterval*1000*60);

    //start loop
    try{
      ServerSocket listener = new ServerSocket(port);
      Socket sock;

      while(true){
        sock = listener.accept();
        clientThread client = new clientThread(sock, ips);
        Thread t = new Thread(client);
        t.start();
      }
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

}

/**
 * This is the class representing a connection thread
 */
class clientThread implements Runnable {

    private Socket conn;
    private ConcurrentHashMap<String,String[]> ips;

    clientThread(Socket conn, ConcurrentHashMap<String,String[]> ips) {
        this.conn = conn;
        this.ips = ips;
    }

    private String getIPforID(String id) {
        if (ips.containsKey(id))
            return ips.get(id)[0]+" "+ips.get(id)[1];
        else
            return IDServer.UNKNOWN;
    }

    /**
     * Validates the sent data. On success, puts mapping ID->IP.
     * Returns response to be transmitted back.
     * @param ip remote IP of socket
     * @param port   transmitted listening port
     * @param b64pub transmitted public key as Base64
     * @param b64sig transmitted signature as Base64
     */
    private String associateID(String ip, String port, String b64pub, String b64sig) {
        try {
          PublicKey pub = IMCrypt.decodePublicKey(b64pub);
          if (pub == null) {
              System.out.println("Invalid public key");
              return IDServer.FUCKOFF;
          }


          String data = IMCrypt.RSA_Dec(b64sig, pub);
          if (data == null) {
              System.out.println("Could not verify");
              return IDServer.FUCKOFF;
          }

          String id = pepperim.backend.IMIdentity.IDfromPubkey(pub);

          if (!data.equals(id)) {
              System.out.println("Public key and ID do not match");
              return IDServer.FUCKOFF;
          }

          //everything is fine -> associate
          String[] entry = new String[3];
          entry[0] = ip;
          entry[1] = port;
          entry[2] = String.valueOf(new Date().getTime());
          ips.put(id, entry);

          System.out.println("Online users: "+ips.size());

          return IDServer.OK;

        } catch (Exception e) {
          e.printStackTrace();
          return IDServer.FUCKOFF;
        }
    }

    public void run() {
      try {
        String response = "";
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        PrintStream out = new PrintStream(conn.getOutputStream());

        // read request string
        String request = in.readLine();
        String[] tokens = request.split("\\s+");

        // IP request
        if (tokens[0].equals(IDServer.GET)) {
            if (tokens.length == 2) {
                response = getIPforID(tokens[1]);
            } else {
                response = IDServer.INVALID;
            }
        }
        // IP+ID association
        else if (tokens[0].equals(IDServer.HELLO)) {
            if (tokens.length == 4) {
                String ip = conn.getInetAddress().getHostAddress();
                response = associateID(ip, tokens[1], tokens[2], tokens[3]);
            } else {
                response = IDServer.INVALID;
            }
        }
        // bullshit
        else {
            response = IDServer.INVALID;
        }

        // send response
        out.println(response);

        // Done
        conn.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
}
