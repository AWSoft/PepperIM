package pepperim.backend;

import junit.framework.*;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONArray;

public class IMContactListTest extends TestCase {
    public void testIMContactList() {
        IMIdentity testid = new IMIdentity();
        IMIdentity testid2 = new IMIdentity();
        String id = testid.getID();

        IMContactList clist = new IMContactList();

        assertTrue(clist.add(testid.getID(), testid.getPublicKey()));
        assertFalse(clist.add(testid.getID(), testid.getPublicKey())); // no multiple entries for the same ID
        assertEquals(null, clist.get("x")); //not created yet

        assertTrue( IMContact.class == clist.get(testid.getID()).getClass() ); //should return a contact 

        //check contact manipulation methods
        clist.get(id).setAuthorizes(true);
        clist.get(id).setAuthorized(true);
        assertTrue(clist.get(id).getAuthorizes());
        assertTrue(clist.get(id).getAuthorized());
        clist.get(id).setAlias("SomeIdiot");
        assertEquals(clist.get(id).getAlias(), "SomeIdiot");
        clist.get(id).setAlias("");    // empty string = no alias = set to null
        assertEquals(null, clist.get(id).getAlias());
        assertEquals(clist.get(id).getPublicKey(), testid.getPublicKey());
        
        //check removal and ID retrieval
        assertTrue(clist.add(testid2.getID(), testid2.getPublicKey()));
        assertFalse(clist.remove("x")); //no such element
        assertEquals(2, clist.getIDs().length); //namely, a and b
        assertTrue(clist.remove(testid2.getID()));
        assertEquals(1, clist.getIDs().length); //namely, a and b

        //check reversibility
        String jsoncts = clist.toString();
        assertEquals(jsoncts, new IMContactList((JSONArray)JSONSerializer.toJSON(jsoncts)).toString());
    }
}
