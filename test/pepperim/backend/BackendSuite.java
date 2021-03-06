/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import pepperim.backend.snserver.SnserverSuite;

/**
 *
 * @author Anton Pirogov
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({IMContactListTest.class, IMIdentityTest.class, IMContactTest.class, SnserverSuite.class, IMStatusTest.class, IMForgeTest.class})
public class BackendSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}
