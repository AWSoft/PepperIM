/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Anton Pirogov
 */
public class IMStatusTest {
    
    public IMStatusTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testIMStatus() {
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, null).toString()).getMessage());
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "").toString()).getMessage());
        assertEquals("Hello, world", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "Hello, world").toString()).getMessage());
    }
}
