package pepperim.backend;

import junit.framework.*;

public class IMStatusTest extends TestCase {
    public void testIMStatus() {
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, null).toString()).getMessage());
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "").toString()).getMessage());
        assertEquals("Hello, world", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "Hello, world").toString()).getMessage());
    }
}
