package org.swordapp.server.action;

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

public class AbstractMerrittAction
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
    
    
    public AbstractMerrittAction()
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
            db = swordConfig.getDB();
            serviceProperties = swordConfig.getServiceProperties();
            logger = swordConfig.getLogger();
            
            
            String maxUploadSizeS = serviceProperties.getProperty("maxUploadSize");
            if (StringUtil.isAllBlank(maxUploadSizeS)) {
                throw new SwordError(MESSAGE + "maxUploadSize not supplied");
            }
            try {
                maxUploadSize = Long.parseLong(maxUploadSizeS);
            } catch (Exception ex) {
                throw new SwordError(MESSAGE + "maxUploadSize invalid:" + maxUploadSizeS);
            }

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
            
            link = serviceProperties.getProperty("link");
            if (StringUtil.isAllBlank(link)) {
                throw new SwordError(MESSAGE + "link not supplied");
            }
            
            editmedia = serviceProperties.getProperty("editmedia");
            if (StringUtil.isAllBlank(editmedia)) {
                throw new SwordError(MESSAGE + "editmedia not supplied");
            }
            
            edit = serviceProperties.getProperty("edit");
            if (StringUtil.isAllBlank(edit)) {
                throw new SwordError(MESSAGE + "edit not supplied");
            }
            
            String retainTargetUrlS = serviceProperties.getProperty("retainTargetUrl");
            if (retainTargetUrlS != null) {
                retainTargetUrl = StringUtil.argIsTrue(retainTargetUrlS);
            }
        } catch (SwordError se) {
            throw se; 
            
        } catch (Exception ex) {
            throw new SwordError("Initialize Exception:" + ex);
        }
    }
    
    public DepositReceipt notAllowed(String method)
        throws SwordError, SwordServerException, SwordAuthException
    {
            String msg = "Method not allowed:" + method;
            System.out.println("AbstractMerrittAction: not Allowed:" + msg);
            throw new SwordError(UriRegistry.ERROR_METHOD_NOT_ALLOWED, msg);
    }
    
    public String getProfileAuthenticated(AuthCredentials auth, String mnemonic)
        throws SwordError, SwordServerException, SwordAuthException
    {
        String userName = auth.getUsername();
        String onBehalfOf = auth.getOnBehalfOf();
        String pwd = auth.getPassword();
        try {
            
            if (StringUtil.isAllBlank(onBehalfOf)) {
                String msg = "Missing On-Behalf-Of header in request";
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST, msg);
            }
            
            String profile = ldap.getProfile(onBehalfOf, pwd, mnemonic);
            if (profile == null) {
                String msg = "User not authorized for collection " + mnemonic;
                throw new SwordError(UriRegistry.ERROR_TARGET_OWNER_UNKNOWN, msg);
            }
            return profile;
            
        } catch (TException.USER_NOT_AUTHENTICATED authex) {
            throw new SwordAuthException(onBehalfOf + " not authenticated");
            
        } catch (SwordError se) {
            throw se;
            
        } catch (Exception ex) {
            throw new SwordError("isAuthenticated exception:" + ex);
        }
    }
    
    public LinkedHashList<String,String> isAuthenticated(String userId, String password)
        throws SwordError, SwordServerException, SwordAuthException
    {
        try {
            CertsLDAP cl = new CertsLDAP(certFile, ldapHost);
            LinkedHashList<String,String> authList = cl.find(
                    userId,
                    password);
            if (authList == null) {
                throw new SwordAuthException(userId + " not authenticated");
            }
            return authList;
            
        } catch (SwordAuthException sae) {
            throw sae;
            
        } catch (Exception ex) {
            throw new SwordError("isAuthenticated exception:" + ex);
        }
    }
    
    public boolean isAuthorized(String userId, String password, String profile)
        throws SwordError, SwordServerException, SwordAuthException
    {
        try {
            CertsLDAP cl = new CertsLDAP(certFile, ldapHost);
            boolean authorized = cl.isAuthorized(userId, password, profile, userId);
            return authorized;
            
        } catch (Exception ex) {
            throw new SwordError("isAuthenticated exception:" + ex);
        }
    }
    
    public Feed getFeed(String workspaceTitle, String atomTitle, String feedUri)
    {
        Abdera abdera = new Abdera();
        Feed feed = abdera.newFeed();
        feed.setId(feedUri);
        feed.addLink(feedUri, "self");
        feed.setTitle("Merritt");
        Entry entry = feed.addEntry();
        entry.setTitle(atomTitle);
        return feed;
    }
    
    public static String getMnemonicFromUri(String uri) 
           throws SwordError, SwordServerException, SwordAuthException
    {
        int lastPos = uri.lastIndexOf("/");
        if (lastPos < 0) {
            throw new SwordError("localid not provided in URL:" + uri);
        }
        String noId = uri.substring(0,lastPos);
        
        lastPos = noId.lastIndexOf("/");
        if (lastPos < 0) {
            throw new SwordError("mnemonic not provided in URL:" + uri);
        }
        String mnemonic = noId.substring(lastPos + 1);
        return mnemonic;
    }
    
    public static String getIdFromUri(String uri) 
           throws SwordError, SwordServerException, SwordAuthException
    {
        int lastPos = uri.lastIndexOf("/");
        if (lastPos < 0) {
            throw new SwordError("localid not provided in URL:" + uri);
        }
        String id = uri.substring(lastPos + 1);
        try {
            id = java.net.URLDecoder.decode(id, "utf-8");
        } catch (Exception ex) {
            throw new SwordError("Encode exception:" + ex + " - id:"+ id);
        }
        return id;
    }
    
    
    
    public String authorizedProfile(String userId, String password, String mnemonic)
        throws SwordError, SwordServerException, SwordAuthException
    {
        try {
            String profile = ldap.getProfile(userId, password, mnemonic);
            return profile;
            
        } catch (TException.USER_NOT_AUTHENTICATED aex) {
            throw new SwordAuthException("User not authenticated:" + userId);
            
        } catch (Exception ex) {
            throw new SwordError("isAuthenticated exception:" + ex);
        }
    }
    
    
    public File saveFileValidate(InputStream stream, String md5)
        throws SwordError, SwordServerException, SwordAuthException
    {
        try {
            File file = FileUtil.getTempFile("download", "txt");
            FileUtil.stream2File(stream, file);
            
            FixityTests fixity = new FixityTests(file, "md5", logger);
            FixityTests.FixityResult result = fixity.validateSizeChecksum("md5", md5, file.length());
            if (!result.checksumMatch) {
                String msg = "Precondition failed - container does not match md5"
                       + " - in md5:" + md5
                       +  " - calculated md5:" + fixity.getChecksum();
                throw new SwordError(UriRegistry.ERROR_CHECKSUM_MISMATCH, msg);
            }
            return file;
            
        } catch (Exception ex) {
            throw new SwordError("Exception:" + ex);
        }
        
    }

    

    public Properties getServiceProperties() {
        return serviceProperties;
    }

    public static SwordConfig getSwordConfig() {
        return swordConfig;
    }

    public long getMaxUploadSize() {
        return maxUploadSize;
    }

    public DPRFileDB getDb() {
        return db;
    }

    public String getLink() {
        return link;
    }

    public String getEdit() {
        return edit;
    }

    public String getEditMedia() {
        return editmedia;
    }

    public LoggerInf getLogger() {
        return logger;
    }
    
    
}
