/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim;

/**
 * Entry point into the application
 * @author Anton Pirogov
 */
public class Main {

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
        //parse options which are valid for all frontends
        int port = 1706; //default port
        if (argsHaveFlag(args,"--port"))
            port = Integer.parseInt(getParam(args,"--port"));

        //Start as ID Server
        if (argsHaveFlag(args,"--idserver"))
            new pepperim.idserver.IDServer().run();

        //Start as IM Console Client
        if (argsHaveFlag(args,"--cui"))
            new pepperim.frontend.ConsoleClient(port).main(args);
    }
}
