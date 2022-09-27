package org.swordapp.server.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;
import org.cdlib.mrt.security.Base64;
import org.cdlib.mrt.utility.LoggerAbs;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

public class IngestHandler
    extends AbstractHandlerAction
{
    
    protected static final String NAME = "IngestHandler";
    protected static final String MESSAGE = NAME + ": ";
    
    private String filename;
    private String mimeType;
    private String slug = null;
    private String md5 = null;
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
    protected boolean retainTargetUrl = false;
    
    public IngestHandler(URL url, String profile, String user, String authCode, 
            boolean retainTargetUrl, 
            LoggerInf logger)
        throws SwordError, SwordServerException, SwordAuthException
    {
        super(logger);
        this.user = user;
        this.authCode = authCode;
        this.url = url;
        this.profile = profile;
        this.retainTargetUrl = retainTargetUrl;
        System.out.println("***IngestHandler:"
                + " - user=" + user
                + " - url=" + url
                + " - profile=" + profile
                + " - retainTargetUrl=" + retainTargetUrl
        );
    }
    
    public boolean process(InputStream containerStream, String packaging, String md5, String localID, String comment)
        throws SwordError, SwordServerException, SwordAuthException
    {
        HttpResponse response = null;
        File ingestPackage = null;
        try {
            if (containerStream == null) {
                throw new SwordError("Container file not supplied");
            }
            ingestPackage = saveFileValidate(containerStream, packaging, md5);
            String type  = "container-batch-manifest";
            sendIngestMultipart(user, authCode,
                ingestPackage,
                packaging,
                profile,
                url,
                localID,
                comment);
            setIngestResponse();
            setArk();
            return completed;
            
        } catch (SwordError sx) {
            throw sx;
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw new SwordError("Ingest handling failure url='" + url + "' Exception:" + tex.toString());
            
        } finally {
            if (ingestPackage != null) {
                try {
                    ingestPackage.delete();
                } catch (Exception ex) { }
            }
        }
    }
    
    public boolean process(File containerFile, String packaging, String md5, String localID, String comment)
        throws SwordError, SwordServerException, SwordAuthException
    {
        HttpResponse response = null;
        File ingestPackage = null;
        try {
            if (containerFile == null) {
                throw new SwordError("Container file not supplied");
            }
            ingestPackage = containerFile;
            System.out.println("***ingestPackage"
                    + " - path:" + ingestPackage.getCanonicalPath()
                    + " - length:" + ingestPackage.length()
            );
            String type  = "container-batch-manifest";
            sendIngestMultipart(user, authCode,
                ingestPackage,
                packaging,
                profile,
                url,
                localID,
                comment);
            setIngestResponse();
            setArk();
            return completed;
            
        } catch (SwordError sx) {
            throw sx;
            
        } catch (TException tex) {
            throw new SwordError("Ingest handling failure url='" + url + "' Exception:" + tex.toString());
            
        } catch (Exception ex) {
            throw new SwordError("Ingest handling failure url='" + url + "' Exception:" + ex.toString());
            
        }finally {
            if (ingestPackage != null) {
                try {
                    if (false) ingestPackage.delete();
                } catch (Exception ex) { }
            }
        }
    }
  
    /**
     * 
     * @param userInfo
     * @param authCode
     * @param ingestID
     * @param url
     * @param ingestPackage
     * @param profile
     * @param localID
     * @param submitter
     * @param type
     * @return
     * @throws TException 
     */
    public void sendIngestMultipart(
            String userInfo,
            String authCode,
            File ingestPackage,
            String packaging,
            String profile,
            URL url,
            String localID,
            String comment)
        throws TException
    {
        try {
            /*
            curl --verbose --insecure --user "dloy:xxx" \
        -F "localIdentifier=doi:10.20200/hij1000106" \
        -F "synchronousMode=true" \
        -F "file=@test.zip" \
        -F "type=container" \
        -F "submitter=dloy/production ingest api test" \
        -F "note=Dash Test" \
        -F "profile=dash_cdl_content" \
        -F "responseForm=xml" \
http://uc3-mrt-wrk1-dev.cdlib.org:33121/submit-object
            */System.out.println("****Ingest request - "
                    + " - ingestPackage.getAbsolutePath()=" + ingestPackage.getAbsolutePath()
            );
            String fileType = "container";
            if (packaging.endsWith(UriRegistry.PACKAGE_BINARY)) {
                fileType = "object-manifest";
            }
            
            String submitter = userInfo;
            if (!StringUtil.isAllBlank(comment)) {
                submitter += "/" + comment;
            }
            
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(24 * 3600 * 1000).build();
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            HttpPost httppost = new HttpPost(url.toString());

            test("profile", profile);
            test("submitter", submitter);
            test("localIdentifier", localID);
            if (ingestPackage == null) {
                throw new TException.INVALID_OR_MISSING_PARM("ingestPackage required but not supplied");
            }
            System.out.println("****Ingest request - \n"
                    + " - profile=" + profile + "\n"
                    + " - packaging=" + packaging + "\n"
                    + " - type=" + fileType + "\n"
                    + " - submitter=" + submitter + "\n"
                    + " - retainTargetURL=" + retainTargetUrl + "\n"
            );
            MultipartEntityBuilder builder = MultipartEntityBuilder
                .create()
                .addTextBody("profile", profile)
                .addTextBody("type", fileType)
                .addTextBody("submitter", submitter)
                .addTextBody("localIdentifier", localID)
                .addTextBody("synchronousMode", "true")
                .addTextBody("retainTargetURL", "" + retainTargetUrl)
                .addTextBody("responseForm", "xml")
                .addBinaryBody("package", ingestPackage);
            HttpEntity entity = builder.build();
            
            httppost.setEntity(entity);
            httppost.addHeader("accept", "text/xml; q=1");
            
            if (StringUtil.isNotEmpty(userInfo) && StringUtil.isNotEmpty(authCode)) {
                String pass = userInfo + ":" + authCode;
                String auth64 = Base64.encodeBytes(pass.getBytes("utf-8"));
                httppost.addHeader("Authorization", "Basic " + auth64);
                System.out.println("Authorization: " + "Basic " + auth64);
            }
            response = httpClient.execute(httppost);

        } catch (Exception ex) {
            log(MESSAGE + "Exception:" + ex, 3);
            log(MESSAGE + "Trace:" + StringUtil.stackTrace(ex), 10);
            throw new TException.GENERAL_EXCEPTION(ex);

        }
    }
    
    protected String test(String name, String value) 
        throws TException
    {
        if (StringUtil.isAllBlank(value)) {
            throw new TException.INVALID_OR_MISSING_PARM("Param " + name 
                    + " required for ingest but not supplied");
        }
        return value;
    }
    
    public void setIngestResponse()
        throws TException
    {
        try {
	    int responseCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null && (responseCode >= 200 && responseCode < 300)) {
                InputStream inStream = entity.getContent();
                ingestResponse = StringUtil.streamToString(inStream, "utf-8");
                if (ingestResponse.contains("<job:jobStatus>COMPLETED</job:jobStatus>"))  {
                    completed = true;
                }
                return;
            }
            if (responseCode == 404) {
                throw new TException.REQUESTED_ITEM_NOT_FOUND(
                    "HTTPUTIL: getObject- Error during HttpClient processing"
                    + " - responseCode:" + responseCode
                    );
            }
            throw new TException.ACCEPTED(
                    "HTTPUTIL: getObject- Error during HttpClient processing"
                    + " - responseCode:" + responseCode
                    );

        } catch( TException tex ) {
            //System.out.println("trace:" + StringUtil.stackTrace(tex));
            throw tex;

        } catch( Exception ex ) {
            System.out.println("trace:" + StringUtil.stackTrace(ex));
            throw new TException.GENERAL_EXCEPTION("HTTPUTIL: getObject- Exception:" + ex);
        }
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

    public String getIngestResponse() {
        return ingestResponse;
    }
    
    public void setArk()
    {
        if (StringUtil.isAllBlank(ingestResponse)) {
            setArk(null);
            return;
        }
        String START = "<job:primaryID>";
        int pos = ingestResponse.indexOf(START);
        if (pos < 0)  {
            setArk(null);
            return;
        }
        String rest = ingestResponse.substring(pos + START.length());
        String END = "</job:primaryID>";
        pos = rest.indexOf(END);
        ark = rest.substring(0,pos);
    }
    
    public void setArk(String ark)
    {
        this.ark = ark;
    }

    public String getArk() {
        return ark;
    }

    public String getProfile() {
        return profile;
    }

    /**
     * Main method
     */
    public static void main(String args[])
    {
        /*
curl --verbose --insecure --user "dloy:xxx" \
        -F "localIdentifier=doi:10.20200/hij1000106" \
        -F "synchronousMode=true" \
        -F "file=@test.zip" \
        -F "type=file" \
        -F "submitter=dloy/production ingest api test" \
        -F "note=Dash Test" \
        -F "profile=dash_cdl_content" \
        -F "responseForm=xml" \
http://uc3-mrt-wrk1-dev.cdlib.org:33121/submit-object
                */
 
        String user = "ucb_dash_submitter";
        String authCode ="8L75av8z";
        String urlS = "http://ingest01-aws-dev.cdlib.org:33121/submit-object";
        //String filePath = "/apps/replic/tomcat-28080/webapps/test/sword/manifest/manifest.txt";
        String filePath = "/apps/replic/tomcat-28080/webapps/test/sword/manifest/man-min.txt";
        //String md5 = "a65a60c1285b98d8f56918c4e3a6cd31"; //manifest.txt
        String md5 = "e3f5602dede28f0082ea0e7adea1482b"; //man-min.txt
        String profile = "dash_ucb_content";
        String localID = "doi:10.20200/embdev01";
        String comment = null;
        LoggerInf logger;
        try {
            URL url = new URL(urlS);
            File in = new File(filePath);
            logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
            InputStream inStream = new FileInputStream(in);
            IngestHandler handler = new IngestHandler(url, profile, user, authCode, false, logger);
            String packaging = UriRegistry.PACKAGE_BINARY;
            //String packaging = UriRegistry.PACKAGE_SIMPLE_ZIP;
            boolean complete = handler.process(inStream, packaging, md5, localID, comment);
            String ingestResponse = handler.getIngestResponse();
            String ark = handler.getArk();
            System.out.println("IngestHandler:\n"
                    + " - complet=" + complete + "\n"
                    + " - ark=" + ark + "\n"
                    + " - ingestResponse=" + ingestResponse + "\n"
            );

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }

    /**
     * Main method
     */
    public static void main_zip(String args[])
    {
        /*
curl --verbose --insecure --user "dloy:xxx" \
        -F "localIdentifier=doi:10.20200/hij1000106" \
        -F "synchronousMode=true" \
        -F "file=@test.zip" \
        -F "type=file" \
        -F "submitter=dloy/production ingest api test" \
        -F "note=Dash Test" \
        -F "profile=dash_cdl_content" \
        -F "responseForm=xml" \
http://uc3-mrt-wrk1-dev.cdlib.org:33121/submit-object
                */
 
        String user = "ucb_dash_submitter";
        String authCode ="8L75av8z";
        String urlS = "http://ingest01-aws-dev.cdlib.org:33121/submit-object";
        String filePath = "/apps/replic/test/sword/manifest-170411/noneemb.zip";
        String md5 = "74ee2b310864a44b9aeedb77ebd7d219";
        String profile = "dash_ucb_content";
        String localID = "doi:10.20200/embdev01";
        String comment = null;
        LoggerInf logger;
        try {
            URL url = new URL(urlS);
            File in = new File(filePath);
            logger = LoggerAbs.getTFileLogger("testFormatter", 10, 10);
            InputStream inStream = new FileInputStream(in);
            IngestHandler handler = new IngestHandler(url, profile, user, authCode, false, logger);
            //String packaging = UriRegistry.PACKAGE_BINARY;
            String packaging = UriRegistry.PACKAGE_SIMPLE_ZIP;
            boolean complete = handler.process(inStream, packaging, md5, localID, comment);
            String ingestResponse = handler.getIngestResponse();
            String ark = handler.getArk();
            System.out.println("IngestHandler:\n"
                    + " - complet=" + complete + "\n"
                    + " - ark=" + ark + "\n"
                    + " - ingestResponse=" + ingestResponse + "\n"
            );

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }
}
