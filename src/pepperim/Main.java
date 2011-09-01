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
    private static boolean argsHaveFlag(String[] args, String flag) {
        for (int i=0; i<args.length; i++)
            if (args[i].equals(flag))
                return true;
        return false;
    }

    public static void main(String[] args) {
        //Start as ID Server
        if (argsHaveFlag(args,"--idserver"))
            new pepperim.idserver.IDServer().run();

        //Start as IM Client
        //TODO
    }
}
