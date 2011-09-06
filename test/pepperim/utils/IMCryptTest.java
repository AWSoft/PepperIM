package pepperim.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class IMCryptTest {
    
    public IMCryptTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testHexBinHex() {
        byte [] testdata = { 0,0, 1, 126, 64, 5, 0 };
        String hex = IMCrypt.binToHex(testdata);
        byte [] back = IMCrypt.hexToBin(hex);
        String hex2 = IMCrypt.binToHex(back);
        assertEquals(hex, hex2);
    }
    
    @Test
    public void testSHA512() {
        String hash = IMCrypt.SHA512("penis\n");
        assertEquals(hash,"de42f972783cde6246a1deb0698605420be100a03f1c6ffb2560a9ac4166c0e02346db3ed0bbe263b230461364561681529c2bb7f9ecdd646e08e09d97abf066");
    }
    
    @Test
    public void testB64() {
        byte[] data="This is just some test data".getBytes();
        String enc = IMCrypt.B64_Enc(data);
        byte[] dec = IMCrypt.B64_Dec(enc);

        assertEquals(data.length, dec.length);
        for(int i=0; i<data.length; i++)
          assertEquals(data[i], dec[i]);
    }
    
    @Test
    public void testAES() {
        String str = "This is some text to test the AES encryption";

        String p = IMCrypt.AES_genKey();
        assertEquals(p.length(), 32);

        String s = IMCrypt.AES_Enc("This is some text to test the AES encryption", p);
        String d = IMCrypt.AES_Dec(s, p);

        assertEquals(str,d);

        //test obvious fail cases
        String fail1 = IMCrypt.AES_Enc(s, "invalid key");
        String fail2 = IMCrypt.AES_Dec(d, IMCrypt.AES_genKey()); //wrong key
        String fail3 = IMCrypt.AES_Dec("invalid data", p);

        assertEquals("",fail1);
        assertEquals("",fail2);
        assertEquals("",fail3);
    }
    
    @Test
    public void testRSA() {
        String str = "This is some text to test the RSA encryption";

        //key generation
        String[] keypair = IMCrypt.RSA_genKeypair();
        
        assertEquals(null, IMCrypt.decodePublicKey(keypair[1]));
        assertEquals(null, IMCrypt.decodePrivateKey(keypair[0]));

        PublicKey pubk = IMCrypt.decodePublicKey(keypair[0]);
        PrivateKey privk = IMCrypt.decodePrivateKey(keypair[1]);

        //RSA encryption
        String rsaenc = IMCrypt.RSA_Enc(str,pubk);
        String rsadec = IMCrypt.RSA_Dec(rsaenc, privk);
        assertEquals(str, rsadec);

        //test obvious fail cases
        String fail1 = IMCrypt.RSA_Dec("dfosdfsidg",privk);
        String fail2 = IMCrypt.RSA_Dec(rsaenc,pubk);
        assertEquals("", fail1);
        assertEquals("", fail2);

        //custom signing - encrypt with private, decrypt with public
        rsaenc = IMCrypt.RSA_Enc(str, privk);
        rsadec = IMCrypt.RSA_Dec(rsaenc, pubk);

        assertEquals(str, rsadec);

        // standard signing
        String sig = IMCrypt.RSA_Sign(str, privk);
        assertTrue( IMCrypt.RSA_Verify(str, sig, pubk) );
        assertFalse( IMCrypt.RSA_Verify("changed data", sig, pubk) );
    }
}
