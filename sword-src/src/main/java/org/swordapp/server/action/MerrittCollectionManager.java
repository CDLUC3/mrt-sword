package org.swordapp.server.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.security.Base64;
import org.cdlib.mrt.security.CertsLDAP;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.FixityTests;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.URLEncoder;

public class MerrittCollectionManager
    extends AbstractMerrittAction
{
    
    protected static final String NAME = "MerrittResourceManager";
    protected static final String MESSAGE = NAME + ": ";
    
    public MerrittCollectionManager()
        throws SwordError, SwordServerException, SwordAuthException
    {
        super();
    }
    
    public IngestHandler process(
            AuthCredentials auth,
            String packaging, 
            String md5,
            String mnemonic,
            String localID,
            String comment,
            InputStream containerStream)
        throws SwordError, SwordServerException, SwordAuthException
    {
        String profile = getProfileAuthenticated(auth, mnemonic);
        return process(
            auth.getOnBehalfOf(),
            auth.getPassword(),
            packaging, 
            md5,
            profile,
            localID,
            comment,
            containerStream);
    }
    
    public IngestHandler process(
            String user,
            String authCode,
            String packaging, 
            String md5,
            String profile,
            String localID,
            String comment,
            InputStream containerStream)
        throws SwordError, SwordServerException, SwordAuthException
    {
        File containerFile = null;
        boolean complete = false;
        try {
            System.out.println(PropertiesUtil.dumpProperties("***MerrittCollectionManager", this.getServiceProperties()));
            String urlS = this.getServiceProperties().getProperty("ingestUrlUpdate");
            if (StringUtil.isAllBlank(urlS)) {
                throw new SwordError("ingest URL not defined in info");
            }
            URL url = new URL(urlS);
            IngestHandler handler = new IngestHandler(url, profile, user, authCode, retainTargetUrl, logger);
            complete = handler.process(containerStream, packaging, md5, localID, comment);
            
            String ingestResponse = handler.getIngestResponse();
            System.out.println("IngestHandler:\n"
                    + " - complete=" + complete + "\n"
                    + " - ingestResponse=" + ingestResponse + "\n"
            );
            return handler;
            
        } catch (SwordError se) {
            throw se;
            
        } catch (SwordServerException se) {
            throw se;
            
        } catch (SwordAuthException se) {
            throw se;
            
        } catch (Exception ex) {
            throw new SwordServerException("MerrittCollectionManager:" + ex);
            
        } finally {
            if (containerFile != null) {
                try {
                    containerFile.delete();
                } catch (Exception ex) { }
            }
        }
    }
    
    public void log(String msg, int lvl)
    {
        if (logger == null) return;
        logger.logMessage(msg, lvl);
    }
}
