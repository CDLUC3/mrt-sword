package org.swordapp.server.action;

import java.io.File;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.mime.content.FileBody;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;
import org.cdlib.mrt.security.Base64;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;

public class EditCall
    extends AbstractHandlerAction
{
    
    protected static final String NAME = "IngestHandler";
    protected static final String MESSAGE = NAME + ": ";
    
    private String mimeType;
    private String slug = null;
    private String packaging;
    private boolean inProgress = false;
    private boolean metadataRelevant = true;
    private File file = null;
    private final String user;
    private final String authCode;
    private HttpResponse response = null;
    private String ingestResponse = null;
    private final URL url;
    private boolean completed = false;
    private String ark = null;
    private String profile = null;
    
    public EditCall(URL url, String profile, String user, String authCode, LoggerInf logger)
        throws SwordError, SwordServerException, SwordAuthException
    {
        super(logger);
        this.user = user;
        this.authCode = authCode;
        this.url = url;
        this.profile = profile;
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {
 
        String user = "ucb_dash_submitter";
        String submitter = "ucb_dash_submitter";
        String authCode ="8L75av8z";
        String md5 = "5778d534953f91919772ccc5390bfb31";
        String profile = "dash_ucb";
        String localID = "doi:10.20200/hij1000106";
        String comment = null;
        LoggerInf logger;
        try {
            URL url = new URL("http://uc3-mrtsword-dev.cdlib.org:39001/mrtsword/edit");
            File zipPackage = new File("/replic/test/sword/test.zip");
            logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
            EditCall editCall = new EditCall(url,profile, user, authCode, logger);
            editCall.editReplaceAdd(zipPackage, submitter, localID, md5);
            
            System.out.println("IngestHandler:\n"
                    + " - user=" + user + "\n"
                    + " - profile=" + profile + "\n"
            );

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }
    
    public void editReplaceAdd(File zipPackage, String submitter, String localID, String md5) throws Exception 
    {
            
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(6 * 3600 * 1000).build();
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            String urlS = url.toString() + "/" + profile;
            HttpPut httpput = new HttpPut(urlS);
            
            
            if (StringUtil.isNotEmpty(submitter) && StringUtil.isNotEmpty(authCode)) {
                String pass = submitter + ":" + authCode;
                String auth64 = Base64.encodeBytes(pass.getBytes("utf-8"));
                httpput.addHeader("Authorization", "Basic " + auth64);
                System.out.println("Authorization: " + "Basic " + auth64);
            }
            Header onBehalfOf=new BasicHeader("On-Behalf-Of", submitter);
            httpput.addHeader(onBehalfOf);
            Header localIDH=new BasicHeader("Slug", localID);
            httpput.addHeader(localIDH);
            
            ContentType contentType = ContentType.create("application/zip");
            
            String filename = zipPackage.getCanonicalPath();
            FileBody fileBody = new FileBody(zipPackage, contentType, filename);
            System.out.println("EditCall properties:"
                    + " - zipPackage:" + zipPackage.length()
                    + " - contentType:" + contentType
                    + " - filename:" + filename
            );
            FormBodyPartBuilder formBuilder = FormBodyPartBuilder
                    .create()
                    .addField("Packaging", "http://purl.org/net/sword/package/SimpleZip")
                    .addField("Content-MD5", md5)
                    .addField("MIME-Version", "1.0")
                    .setBody(fileBody)
                    .setName("payload");
            FormBodyPart part = formBuilder.build();
            
                    
            MultipartEntityBuilder builder = MultipartEntityBuilder
                .create()
                .setMimeSubtype("related; "
                    + "type=\"application/atom+xml\"")
                .addPart(part);
            HttpEntity entity = builder.build();
            httpput.setEntity(entity);
            response = httpClient.execute(httpput);
    }

    public void log(String msg, int lvl)
    {
        if (logger == null) return;
        logger.logMessage(msg, lvl);
    }

    public HttpResponse getResponse() {
        return response;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getProfile() {
        return profile;
    }
}
