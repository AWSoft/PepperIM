/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/
package pepperim.frontend;

import java.io.*;

import pepperim.Main;
import pepperim.frontend.Base;
import pepperim.backend.IMIdentity;
import pepperim.backend.IMForge;

/**
 * This is the console user interface for the messenger.
 * @author Anton Pirogov
 */
public class ConsoleClient extends Base {
    private IMIdentity id = null;
    private IMForge forge = null;
    private int port = 1706;

    private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

    /**
     * initialize a console client
     * @param port Port to be used for P2P communication
     */
    public ConsoleClient(int port) {
      this.port = port;
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
                System.out.print("Set password: ");
                String pwd = readline();
                System.out.println("Generating...");
                id = new IMIdentity();
                System.out.println("Saving...");
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
     * Main function of the ConsoleClient user interface
     * @param args args string array forwarded from real main function.
     */
    public void main(String[] args) {
        String idfile = Main.getParam(args, "--id");
        if (idfile == null) {
            System.out.println("No filename of/for identity given!");
            System.exit(1);
        }

        initIdentity(idfile);
        this.forge = new IMForge(id);
        this.start(); //Run server + Message handlers

        //Run interaction loop
        String input = "";
        while (true){
            System.out.print("> ");
            input = readline();
            if (input.equals("q")) {
                //Init shutdown, stop server, send offline etc
                System.exit(0); // Clean exit
            }

            System.out.println("You typed: " + input);
        }
    }

    /**
     * Launches the server and message processing
     * when started in a seperate thread.
     */
    @Override
    public void run() {
      initServer(port);
      server.start();
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
}
