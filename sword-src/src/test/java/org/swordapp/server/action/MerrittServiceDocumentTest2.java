/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.swordapp.server.action;
import org.cdlib.mrt.core.*;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TFileLogger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.security.CertsLDAP;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.HTTPUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.StringUtil;

import org.w3c.dom.Document;
/**
 *
 * @author dloy
 */
public class MerrittServiceDocumentTest2 {

    protected static final String NAME = "LDAPAuthenticationTest";
    protected static final String MESSAGE = NAME + ": ";
    protected final static String NL = System.getProperty("line.separator");

    //protected final static String HOSTS = "badger.cdlib.org:1636";
    //protected final static String HOSTS = "dp01.cdlib.org:1636";
    //protected final static String HOSTS = "coot.ucop.edu:1636";
    //protected final static String HOSTS = "ferret.cdlib.org:1636";
    //protected final static String HOSTS = "dp08.cdlib.org:1636";
    //protected final static String HOSTS = "merritt.cdlib.org:1636";
    //protected final static String HOSTS = "uc3-mrt-wrk1-stg.cdlib.org:1636";
    protected final static String HOSTS = "uc3-ldap.cdlib.org:1636";
    protected final static String HOSTSXX = "ldaps://uc3-ldap.cdlib.org:1636";
    public MerrittServiceDocumentTest2() {
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
    public void Dummy()
        throws TException
    {
        assertTrue(true);
    }

    @Test
    public void Test()
        throws TException
    {
        File outFile = null;
        try {
            System.out.println("MerrittServiceDocument2 test entered");
            MerrittServiceDocument msd = new MerrittServiceDocument();
            String submitter = "test_dash_user";
            String pw = "47XpCtdu";
                    
            List<InvCollection> list = msd.getCollections(submitter, pw);
            dumpList(list);
            assertTrue(true);

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            assertFalse("TestIT exception:" + ex, true);
            
        }
    }
    
    public void dumpList(List<InvCollection> list) 
    {
        if ((list == null) || (list.size() == 0)) {
            System.out.println("***List empty");
            return;
        } else {
            System.out.println("***List contains items:" + list.size());
            for (InvCollection item : list) {
                System.out.println(item.dump("item"));
            }
        }
    }

}