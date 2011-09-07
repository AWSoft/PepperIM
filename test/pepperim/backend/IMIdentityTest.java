/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import java.io.File;
import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Anton Pirogov
 */
public class IMIdentityTest {
    
    public IMIdentityTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testIdentity() {
        IMIdentity id = new IMIdentity();
        IMContactList cts = id.getContactList();

        IMIdentity id2 = new IMIdentity();
        String id2n = id2.getID();
        cts.add(id2n,id2.getPublicKey());
        cts.get(id2n).setAlias("Chunky Bacon");
        cts.get(id2n).setAuthorizes(true);
        cts.get(id2n).setAuthorized(true);
        String contacts = cts.toString();

        //check ID name , extension data, saving  w/ or wo/ password
        
        id.setExtensionData("testext",new JSONObject().element("some","thing"));
        JSONObject extdat = new JSONObject().element("foo","bar");
        id.setExtensionData("testext",extdat); //overwriting
        assertEquals(null, id.getExtensionData("qux")); //no data saved there

        String idname = id.getID();
        assertEquals(8, idname.length());

        assertTrue(id.save());
        new File(idname + ".id").delete(); //clean up
        

        assertTrue(id.save("some.id"));

        int exceptions = 0;

        try { new IMIdentity("some.id", "wrongpw"); } catch (Exception e) { exceptions++; } //should fail
        
        try {
        id = new IMIdentity("some.id",null);
        id = new IMIdentity("some.id","");
        } catch (Exception e) { exceptions++; }

        assertEquals(1, exceptions);

        id.setPassword("deinemum");
        assertTrue(id.save("some.id"));

        try { new IMIdentity("some.id", "wrongpw"); } catch (Exception e) { exceptions++; } //should fail
        try { new IMIdentity("some.id", null); } catch (Exception e) { exceptions++; } //should fail

        try { id = new IMIdentity("some.id","deinemum"); } catch (Exception e) {exceptions++; }

        assertEquals(3,exceptions);

        id.removePassword();
        assertTrue(id.save("some.id"));
        try {id = new IMIdentity("some.id",null);} catch (Exception e) { exceptions++; }

        assertEquals(3,exceptions);

        //check contact list & data persistance
        assertEquals(contacts, id.getContactList().toString());
        assertEquals(extdat.toString(), id.getExtensionData("testext").toString());

        new File("some.id").delete(); //clean up
    }
}
