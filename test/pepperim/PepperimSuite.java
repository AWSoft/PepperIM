/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Felix Wiemuth
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({pepperim.backend.BackendSuite.class, pepperim.idserver.IdserverSuite.class, pepperim.util.UtilsSuite.class, pepperim.base.BaseSuite.class, pepperim.MainTest.class})
public class PepperimSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}
