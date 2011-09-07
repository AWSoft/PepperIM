/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend;

import net.sf.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Anton Pirogov
 */
public class IMForgeTest {
    
    public IMForgeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testIMForge() {
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
}
