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
public class AbstractMerrittManagerTest {

    protected static final String NAME = "LDAPAuthenticationTest";
    protected static final String MESSAGE = NAME + ": ";
    protected final static String NL = System.getProperty("line.separator");

    //protected final static String HOSTS = "badger.cdlib.org:1636";
    //protected final static String HOSTS = "dp01.cdlib.org:1636";
    //protected final static String HOSTS = "coot.ucop.edu:1636";
    //protected final static String HOSTS = "ferret.cdlib.org:1636";
    //protected final static String HOSTS = "dp08.cdlib.org:1636";
    //protected final static String HOSTS = "merritt.cdlib.org:1636";
    protected final static String HOSTS = "uc3-mrt-wrk1-stg.cdlib.org:1636";
    protected final static String HOSTSXX = "ldaps://uc3-mrt-wrk1-stg.cdlib.org:1636";
    public AbstractMerrittManagerTest() {
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

    //@Test
    public void Test()
        throws TException
    {
        File outFile = null;
        try {
            AbstractMerrittAction amm = new AbstractMerrittAction();
            assertTrue(true);

        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            assertFalse("TestIT exception:" + ex, true);
            
        }
    }

}