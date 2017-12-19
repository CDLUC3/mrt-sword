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
import org.cdlib.mrt.inv.service.ObjectModList;
import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.security.Base64;
import org.cdlib.mrt.security.CertsLDAP;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.FixityTests;
import static org.cdlib.mrt.utility.HTTPUtil.getFTPInputStream;
import static org.cdlib.mrt.utility.HTTPUtil.getHttpResponse;
import static org.cdlib.mrt.utility.HTTPUtil.isFTP;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.utility.URLEncoder;

public class AbstractHandlerAction
{
    
    protected static final String NAME = "MerrittResourceManager";
    protected static final String MESSAGE = NAME + ": ";
    
    private String filename;
    
    protected LoggerInf logger = null;
    
    public AbstractHandlerAction(LoggerInf logger)
        throws SwordError, SwordServerException, SwordAuthException
    {
        this.logger = logger;
    }
  
    
    public File saveFileValidate(InputStream containerStream, String packaging, String md5)
        throws SwordError, SwordServerException, SwordAuthException
    {
        File file = null;
        try {
            if (packaging.contains(UriRegistry.PACKAGE_BINARY)) {
                file = FileUtil.getTempFile("download.", ".txt");
            } else {
                file = FileUtil.getTempFile("download.", ".zip");
            }
            FileUtil.stream2File(containerStream, file);
            
            FixityTests fixity = new FixityTests(file, "md5", logger);
            FixityTests.FixityResult result = fixity.validateSizeChecksum(md5, "md5", file.length());
            if (!result.checksumMatch) {
                String msg = "The received MD5 checksum for the deposited file did not match the checksum sent by the deposit client"
                        
                       + " - in md5:" + md5
                       +  " - calculated md5:" + fixity.getChecksum()
                       +  " - in length:" + file.length();
                throw new SwordError(UriRegistry.ERROR_CHECKSUM_MISMATCH, msg);
            } else {
                System.out.println("MD5 match:" + md5);
            }
            return file;
            
        } catch (Exception ex) {
            System.out.println("Exception:" + ex);
            ex.printStackTrace();
            throw new SwordError("Exception:" + ex);
        }
        
    }
}
