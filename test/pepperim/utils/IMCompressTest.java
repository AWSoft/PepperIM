package pepperim.utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IMCompressTest {
    
    public IMCompressTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGZip() {
        String str = "This is some longer string to be compressed with the GZip algorithm using the IMCompress class";
        
        assertEquals(null, IMCompress.zip(null));
        assertEquals("", IMCompress.unzip(null));
        assertEquals("", IMCompress.unzip("invalid gzip data".getBytes()));

        byte[] comp = IMCompress.zip(str);
        String back = IMCompress.unzip(comp);
        assertEquals(str, back);
    }
}
