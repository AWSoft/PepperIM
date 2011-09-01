package pepperim.test;

import junit.framework.*;

import pepperim.backend.*;
import java.security.*;
import java.io.File;
import net.sf.json.*;

/**
 * Testing IMIdentity, IMStatus, IMContactList, IMContact and IMForge
 */
public class BackendTest extends TestCase {

    public BackendTest(String name) {
        super(name);
    }

    //check IMIdentity + counts exceptions and checks regularly whether the number of catched is correct
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

    public void testStatus() {
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, null).toString()).getMessage());
        assertEquals("", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "").toString()).getMessage());
        assertEquals("Hello, world", new IMStatus(new IMStatus(IMStatus.Status.ONLINE, "Hello, world").toString()).getMessage());
    }

    public void testIMForge() {
        //IMForge test
        IMIdentity id_a = new IMIdentity();
        IMIdentity id_b = new IMIdentity();

        IMForge f_a = new IMForge(id_a);
        IMForge f_b = new IMForge(id_b);

        System.out.println("Generated test IDs: "+id_a.getID()+","+id_b.getID());

        System.out.println("Server messages:");
        System.out.println(f_a.SVR_announce());
        System.out.println(f_b.SVR_getip(id_a.getID()));

        System.out.println("\nKey exchange:");
        System.out.println(f_a.requestPubkey());
        System.out.println(f_b.sendPubkey());

        System.out.println("\nMessage acknowledgement:");
        System.out.println(f_b.ackMsg("some msgid"));

        System.out.println("\nMessage types (unwrapped):");
        System.out.println(f_a.MSG_getstatus());
        System.out.println(f_a.MSG_setstatus(new IMStatus(IMStatus.Status.ONLINE, "I'm here!")));
        System.out.println(f_a.MSG_imtext("instant message"));
        System.out.println(f_a.MSG_noauth());
        System.out.println(f_a.MSG_authreq("message text"));
        System.out.println(f_a.MSG_authdeny("message text"));
        System.out.println(f_a.MSG_deauth("message text"));
        System.out.println(f_a.MSG_authacc());

        JSONObject extmsg = new JSONObject().element("pic","SMILEY");
        System.out.println(f_a.MSG_ext("smilies",1,extmsg));
        System.out.println(f_a.MSG_extfail("smilies"));

        System.out.println("\nFull instant message exchange (with wrapping+unwrapping):");

        JSONObject immsg = f_a.MSG_imtext("hello, world!");
        String msg = f_a.packMessage(immsg, id_b.getPublicKey());
        System.out.println(msg);

        JSONObject ret = f_b.unpackMessage(msg, id_b.getPublicKey());
        assertEquals(null, ret);

        ret = f_b.unpackMessage(msg, id_a.getPublicKey());
        assertEquals("hello, world!", ret.getString("data"));
        System.out.println(ret.toString());

        //TODO: add some cases where unpack should fail because of some obvious manipulation / attack
    }

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
