/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend.snserver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Felix Wiemuth
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({pepperim.backend.snserver.util.UtilSuite.class, pepperim.backend.snserver.SNServerTest.class})
public class SnserverSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}
