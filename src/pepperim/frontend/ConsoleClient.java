/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/
package pepperim.frontend;

import pepperim.frontend.RuntimeContactList;
import java.io.*;
import net.sf.json.*;
import java.net.InetSocketAddress;
import java.security.PublicKey;

import pepperim.Main;
import pepperim.frontend.Base;
import pepperim.backend.IMIdentity;
import pepperim.backend.IMContact;
import pepperim.backend.IMForge;
import pepperim.backend.IMStatus;
import pepperim.backend.snserver.util.RawMessage;
import pepperim.backend.snserver.SNServer;
import pepperim.util.IMCrypt;

/**
 * This is the console user interface for the messenger.
 * @author Anton Pirogov <anton dot pirogov at googlemail dot com> *
 */
public class ConsoleClient extends Base {
    private String idfile = null;
    private IMIdentity id = null;
    private IMForge forge = null;
    private IMStatus mystatus = null;
    private int port = 1706;
    private InetSocketAddress idserver_addr = null;

    private RuntimeContactList clist = null;

    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    /**
     * initialize a console client
     * @param port Port to be used for P2P communication
     * @param idserver Address of the IDServer to connect to (if null, client will ask interactively)
     */
    public ConsoleClient(int port, InetSocketAddress idserver) {
      this.port = port;
      this.idserver_addr = idserver;
    }

    /**
     * @return Line from Stdin or empty string (circumventing exceptions)
     */
    private String readline() {
        try {
          return stdIn.readLine();
        } catch (Exception e) { return ""; }
    }

    /**
     * Checks whether the file exists. if it does not, guide user through creation.
     * If it does, try to load. If it fails, ask for password. If it fails again, die.
     * @param filename path to identity file to be used or created
     */
    private void initIdentity(String idfile) {
        String input = "";
        //Ask to create identity if not exists
        if (!(new File(idfile).exists())) {
            System.out.print("No such identity! Create? (y/n): ");
            input = readline();
            if (input.equals("y") || input.equals("Y")) {
                System.out.print("New password: ");
                String pwd = readline();
                if (pwd.equals(""))
                    System.out.println("WARNING: No password set, the identity is unprotected!");
                System.out.println("Generating...");
                id = new IMIdentity();
                System.out.println("Saving...");
                if (!pwd.equals(""))
                  id.setPassword(pwd);
                id.save(idfile);
                System.out.println("Done!");
            } else
                System.exit(1);
        }

        //Load identity file
        try {
          id = new IMIdentity(idfile, null);
        } catch (Exception e) {
            System.out.print("Enter password: ");
            input = readline();
            try {
              id = new IMIdentity(idfile, input);
            } catch (Exception ex) {
                System.out.println("Invalid password!");
                System.exit(1);
            }
        }
    }

    /**
     * ask for the IDServer to use, if none given
     */
    private void askIDServerAddr() {
        String str = null;
        while (str == null || str.equals("")) {
          System.out.print("ID Server address: ");
          str = readline();
        }

        this.idserver_addr = new InetSocketAddress(str, pepperim.idserver.IDServer.DEFAULT_PORT);
    }

    /**
     * announce own IP to idserver, ask for contact IPs
     */
    private void initIPs() {
        server.send_message(idserver_addr, forge.SVR_announce(port));
        try { Thread.sleep(2000); } catch(Exception e) {} //wait (TODO: fix bug in SNserver eating too rapid messages)

        String[] contact_ids = clist.getIDs();
        for (int i=0; i<contact_ids.length; i++) {
            server.send_message(idserver_addr, forge.SVR_getip(contact_ids[i]));
        }
    }


    /**
     * Send own pubkey, request pubkey
     * @param id ID of other client
     */
    private void exchangeKeys(String id) {
        InetSocketAddress cl_addr = clist.getAddress(id);

        if (cl_addr != null) {
            server.send_message(cl_addr, forge.sendPubkey());
            server.send_message(cl_addr, forge.requestPubkey());
        }
    }

    /**
     * announce own status to ID, ask for his.. called on client startup
     * @param id ID of other client
     */
    private void exchangeStati(String id) {
        InetSocketAddress cl_addr = clist.getAddress(id);
        PublicKey key = clist.getPublicKey(id);

        if (cl_addr != null && key != null) {
            server.send_message(cl_addr, forge.packMessage(forge.MSG_setstatus(mystatus), key));
            server.send_message(cl_addr, forge.packMessage(forge.MSG_getstatus(), key));
        }
    }

    /**
     * broadcast new status to whole available contact list
     * @param st New status
     */
    public void broadcastStatus(IMStatus st) {
        String[] contact_ids = clist.getIDs();

        for (int i=0; i<contact_ids.length; i++) {
          InetSocketAddress cl_addr = clist.getAddress(contact_ids[i]);
          PublicKey key = clist.getPublicKey(contact_ids[i]);
          if (cl_addr != null && key != null)
            server.send_message(cl_addr, forge.packMessage(forge.MSG_setstatus(st), key));
        }
    }

    /**
     * Main function of the ConsoleClient user interface
     * @param args args string array forwarded from real main function.
     */
    public void main(String[] args) {
        this.idfile = Main.getParam(args, "--id");
        if (idfile == null) {
            System.out.println("No filename of/for identity given!");
            System.exit(1);
        }

        if (idserver_addr == null)
            askIDServerAddr();

        initIdentity(idfile);
        this.forge = new IMForge(id);
        this.mystatus = new IMStatus(IMStatus.Status.ONLINE,"Hello guys!");
        this.clist = new RuntimeContactList(this.id);
        this.server = new SNServer(port); //run server
        this.start(); //run message processing thread

        server.init_connect(idserver_addr); //connect to IDServer

        //Run interaction loop
        String input = "";
        while (true){
            System.out.print(id.getID()+"> ");
            input = readline();
            String[] token = input.split("\\s+");

            if (token[0].equals(""))
                continue;

            if (token[0].equals("q") || token[0].equals("quit")) {
                broadcastStatus(new IMStatus(IMStatus.Status.OFFLINE,""));
                server.send_message(idserver_addr, forge.SVR_bye());
                try { Thread.sleep(1000); } catch(Exception e) {} //wait a sec for messages to transmit (TODO: replace with blocking server.shutdown() )

                id.save(idfile);

                //TODO: Shutdown threads/SNServer cleanly

                System.exit(0); // clean exit
            }

            if (token[0].equals("add")) {
                if (token.length != 2)
                    System.out.println("usage: add <id> -> add ID to contact list");
                else {
                    String id = token[1];

                    clist.add(id);
                    server.send_message(idserver_addr, forge.SVR_getip(id));
                }
            } else if (token[0].equals("remove")) {
                if (token.length != 2)
                    System.out.println("usage: remove <id> -> remove ID from contact list");
                else {
                    String id = token[1];

                    clist.remove(id);
                }
            } else if (token[0].equals("alias")) {
                if (token.length != 3)
                    System.out.println("usage: alias <id> <name> -> set alias for contact");
                else {
                    IMContact c = clist.get(token[1]);
                    if (c != null)
                        c.setAlias(token[2]);
                    else
                        System.out.println("Not in contact list!");
                }
            } else if (token[0].equals("list")) {
              String[] ids = clist.getIDs();
              System.out.println(ids.length+" contacts:");
              for (int i=0; i<ids.length; i++) {
                  String l = "";
                  IMContact c = clist.get(ids[i]);
                  IMStatus s = clist.getStatus(ids[i]);
                  String alias = c.getAlias();
                  l = ids[i];
                  if (alias != null)
                    l += "(" + c.getAlias() + ")";
                  l += " : "+s.getStatus().toString();
                  if (!s.getMessage().equals(""))
                    l += " -> "+s.getMessage();
                  System.out.println(l);
              }
            } else {
                System.out.println("no such command!");
            }

        }
    }

    /**
     * Launches the server and message processing
     * when started in a seperate thread.
     */
    @Override
    public void run() {
      processMessages();
    }

    /**
     * Display an incoming client message via the user interface to the user.
     * @param msg Message to be displayed
    */
    @Override
    public void displayMsg(String msg) {
        System.out.println(msg);
    }

    public void process_clientMsg(String msg) {
        Main.log("MSG: "+msg); //DEBUG

        try {
          JSONObject obj = (JSONObject)JSONSerializer.toJSON(msg);

          String type = obj.getString("type");
          String from = obj.getString("from");

          if (type.equals("KEY")) { //got public key -> store
              PublicKey pub = IMCrypt.decodePublicKey(obj.getString("data"));

              clist.updatePublicKey(from,pub);
              exchangeStati(from);
          } else if (type.equals("KEYPLZ")) {
              InetSocketAddress adr = clist.getAddress(from);
              if (adr!=null)
                  server.send_message(adr, forge.sendPubkey());
              else
                  server.send_message(idserver_addr, forge.SVR_getip(from));
          }


        } catch (JSONException e) {
          //not JSON -> must be a server message
          String[] token = msg.split(" ");
          if (token[0].equals("ADDRESS")) { //got an IP address for a contect -> save it and exchange keys
              String id = token[1];
              String ip = token[2];
              int prt   = Integer.parseInt(token[3]);
              InetSocketAddress adr = new InetSocketAddress(ip, prt);

              clist.updateAddress(id, adr);
              exchangeKeys(id);
          }

        }

    }

    public void process_systemMsg(RawMessage sys) {
        Main.log(sys.getType().toString()+" "+sys.getMsg()); //DEBUG

        String[] addr = sys.getMsg().split(":");
        InetSocketAddress ad = new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));

        if (ad.equals(idserver_addr)) {
          Main.log("Connection to IDServer established!");
          initIPs();
        }
    }
}

