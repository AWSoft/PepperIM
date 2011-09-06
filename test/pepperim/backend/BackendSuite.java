package pepperim.backend;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BackendSuite extends TestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Backend Test Suite");
        
        suite.addTestSuite(pepperim.backend.IMContactListTest.class);
        suite.addTestSuite(pepperim.backend.IMForgeTest.class);
        suite.addTestSuite(pepperim.backend.IMIdentityTest.class);
        suite.addTestSuite(pepperim.backend.IMStatusTest.class);
        
        return suite;
    }    
}
