package org.swordapp.test;

import org.swordapp.server.servlets.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;
import org.swordapp.server.AtomStatement;
import org.apache.abdera.model.Feed;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.security.CertsLDAP;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.FixityTests;
import org.cdlib.mrt.utility.LinkedHashList;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.URLEncoder;
import org.swordapp.server.action.SwordConfig;

public class LDAPTest
{
    
    protected static final String NAME = "AbstractMerrittManager";
    protected static final String MESSAGE = NAME + ": ";
    
    protected static SwordConfig swordConfig = null;
    protected Properties serviceProperties;
    //protected final Properties setupProp;
    protected long maxUploadSize = -1;
    protected File certFile = null;
    protected String ldapHost = null;
    protected String link = null;
    protected String editmedia = null;
    protected String edit = null;
    protected boolean retainTargetUrl = false;
    protected CertsLDAP ldap = null;
    protected DPRFileDB db = null;
    protected File swordService = null;
    protected LoggerInf logger = null;
    
    
    public LDAPTest()
        throws SwordError, SwordServerException, SwordAuthException
    {
        try {
            /*
            String propertyList[] = {
                "resources/Mysql.properties",
                "resources/Sword.properties",
                "resources/SwordLogger.properties"
            };
            TFrame tFrame = new TFrame(propertyList, "ServiceDocumentManager");
            setupProp = tFrame.getProperties();
            */
            if (swordConfig == null) {
                swordConfig = SwordConfig.useYaml();
            }
            setConstruct();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SwordError(MESSAGE + ex);
            
        }
    }
    
    protected void setConstruct()
        throws SwordError
    {
        try {
            serviceProperties = swordConfig.getServiceProperties();

            String cert = serviceProperties.getProperty("jssecacert");
            if (StringUtil.isAllBlank(cert)) {
                throw new SwordError(MESSAGE + "jssecacert not supplied");
            }
            certFile = new File(cert);

            ldapHost = serviceProperties.getProperty("ldapHost");
            if (StringUtil.isAllBlank(ldapHost)) {
                throw new SwordError(MESSAGE + "ldapHost not supplied");
            }
            
            try {
                //System.out.println("AbstractMerrittAction - ldap - host:" + ldapHost);
                ldap = new CertsLDAP(certFile, ldapHost);
            } catch (Exception ex) {
                throw new SwordError(MESSAGE + "CertsLDAP exception:" + ex);
            }
        } catch (SwordError se) {
            throw se; 
            
        } catch (Exception ex) {
            throw new SwordError("Initialize Exception:" + ex);
        }
    }
    
    
    public static void main(String[] argv) {
    	
    	try {
            
            LoggerInf logger = new TFileLogger("test", 50, 50);
            //testCert("uc3-ldap-stg.cdlib.org:1636", "/apps/replic1/docker/sword/jssecacert");
            //testCert("uc3-ldap-stg.cdlib.org:1636", "/apps/replic/docker/sword/jssecacert");
            //testCert("uc3-ldap-stg.cdlib.org:1636", "/apps/replic/apps/sword39001/jssecacert");
            LDAPTest ldapTest = new LDAPTest();
            
            
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        }
    }
    public static void testCert(String ldapHost, String cert)
        throws SwordError
    {
        try {
            File certFile = new File(cert);

            if (StringUtil.isAllBlank(ldapHost)) {
                throw new SwordError(MESSAGE + "ldapHost not supplied");
            }
            
            try {
                //System.out.println("AbstractMerrittAction - ldap - host:" + ldapHost);
                CertsLDAP ldap = new CertsLDAP(certFile, ldapHost);
            } catch (Exception ex) {
                throw new SwordError(MESSAGE + "CertsLDAP exception:" + ex);
            }
        } catch (SwordError se) {
            throw se; 
            
        } catch (Exception ex) {
            throw new SwordError("Initialize Exception:" + ex);
        }
    }
    
    
}
