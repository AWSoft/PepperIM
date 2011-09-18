/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim;

import java.io.*;

import pepperim.idserver.IDServer;
import pepperim.frontend.ConsoleClient;
import java.net.InetSocketAddress;

/**
 * Entry point into the application
 * @author Anton Pirogov
 */
public class Main {
    public static int DEFAULT_PORT = 1706;

    public static PrintWriter logfile = null;
    static {
        try {
          logfile = new PrintWriter(new FileOutputStream(new File("debug.log")));
        } catch (Exception e) {}
    }

    /**
     * write a message to debug log file
     * @param msg Log message
     */
    public static void log(String msg) {
      try {
        logfile.println(msg);
        logfile.flush();
      } catch (Exception e) {}
    }

    /**
     * @param args from main function
     * @param flag flag to be checked
     * @return flag state
     */
    public static boolean argsHaveFlag(String[] args, String flag) {
        for (int i=0; i<args.length; i++)
            if (args[i].equals(flag))
                return true;
        return false;
    }

    /**
     * @param args from main function
     * @param param param to be retrieved
     * @return value or null
     */
    public static String getParam(String[] args, String param) {
        for (int i=0; i<(args.length-1); i++)
            if (args[i].equals(param))
                return args[i+1];
        return null;
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        //parse options which are valid for all frontends
        if (argsHaveFlag(args,"--port"))
            port = Integer.parseInt(getParam(args,"--port"));

        InetSocketAddress server_addr = null;
        String server_host = getParam(args, "--server");
        if (server_host != null)
            server_addr = new InetSocketAddress(server_host,IDServer.DEFAULT_PORT);

        //Start as ID Server
        if (argsHaveFlag(args,"--idserver"))
            new IDServer().run();

        //Start as IM Console Client
        if (argsHaveFlag(args,"--cui"))
            new ConsoleClient(port, server_addr).main(args);
    }
}
