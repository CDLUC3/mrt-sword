/******************************************************************************
Copyright (c) 2005-2016, Regents of the University of California
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
 *
- Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
- Neither the name of the University of California nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
*******************************************************************************/
package org.swordclient;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.security.Base64;

import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.content.FileBody;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.HeaderIterator;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.HttpResponse;
/**
 *
 * @author dloy
 * ReplaceClient replace/edit/PUT SWORD content
 */
public class ReplaceClientUrlDevBig 
{
    protected URL url = null;
    protected String authCode = null;
    protected String submitter = null;
 
    /**
     * constructor
     * @param profile user profile
     * @param submitter name
     * @param authCode authorization code for submitter
     * @param url server URL
     */
    public ReplaceClientUrlDevBig(
            String submitter,
            String authCode,
            URL url
    ) 
    {
        this.submitter = submitter;
        this.authCode = authCode;
        this.url = url;
    }
    
    /**
     * Main method
     */
    public static void main(String args[])
    {

        TFrame tFrame = null;
        try {
            
            //File zipPackage = new File("/apps/replic/test/sword/replace.zip");
            //String md5 = "099265e9be557f0d6e9b8f3019e3e7bc";
            //File zipPackage = new File("/apps/replic/test/sword/collection/114_archive.zip");
            //String md5 = "f1858c50222e24d7c7137a80098037ed";
            
            
            File zipPackage = new File("/apps/replic/test/sword/big/big.zip");
            String md5 = "47523cc2298712563541a02efa3c986b";
            String submitter = "ucb_dash_submitter";
            
//URL url = new URL("http://sword-aws-dev.cdlib.org:39001/mrtsword/edit/dash_ucb/doi%3A10.20200%2Fhij1000106");
            //URL url = new URL("http://sword-aws-dev.cdlib.org:39001/mrtsword/edit/dash_ucb/doi%3A10.20200%2Fxxxxxxxx02");
            //URL url = new URL("http://uc3-mrtreplic2-dev.cdlib.org:28080/mrtsword/edit/dash_ucb/doi%3A10.20200%2Fxxxxxxxxx11");
 
            
            URL url = new URL("http://uc3-mrtsword-dev.cdlib.org:39001/mrtsword/edit/dash_ucb/doi%3A10.20200%2Fhijxxx0004");
            String authCode = "8L75av8z";
            
            /*
            String submitter = "test_dash_user";
            String localID = "doi:10.20200/xxxxxxxxx1";
            String md5 = "099265e9be557f0d6e9b8f3019e3e7bc";
            URL url = new URL("http://sword-aws.cdlib.org:39001/mrtsword");
            String profile = "merritt_demo";
            String authCode = "47XpCtdu";
            */
            ReplaceClientUrlDevBig client = new ReplaceClientUrlDevBig(submitter, authCode, url);
            client.editReplaceAdd(zipPackage,  md5);

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        } 
    }
    
    /**
     * do SWORD edit
     * @param zipPackage container with updates
     * @param localID doi local ID
     * @param md5 md5 of container
     * @throws Exception 
     */
    public void editReplaceAdd(File zipPackage,  String md5) throws Exception 
    {
            
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(6 * 3600 * 1000).build();
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            String urlS = url.toString();
            System.out.println("URL:" + urlS);
            HttpPut httpput = new HttpPut(urlS);
            
            
            if (StringUtil.isNotEmpty(submitter) && StringUtil.isNotEmpty(authCode)) {
                String pass = submitter + ":" + authCode;
                String auth64 = Base64.encodeBytes(pass.getBytes("utf-8"));
                httpput.addHeader("Authorization", "Basic " + auth64);
                System.out.println("Authorization: " + "Basic " + auth64);
            }
            Header onBehalfOf=new BasicHeader("On-Behalf-Of", submitter);
            httpput.addHeader(onBehalfOf);
            
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
                    .setName("payload")
                    .addField("Packaging", "http://purl.org/net/sword/package/SimpleZip")
                    .addField("Content-MD5", md5)
                    //.addField("Content-Type", "application/x-download")
                    .addField("MIME-Version", "1.0");
            formBuilder.addField("Content-Disposition", 
                    //"form-data; filename=\"" + zipPackage.getCanonicalPath() + "\"");
                    //"form-data; name=\"payload\"; filename=\"" + zipPackage.getCanonicalPath() + "\""); 
                    "attachment; name=\"payload\"; filename=\"" + zipPackage.getCanonicalPath() + "\"");
                    //"attachment; filename=\"" + zipPackage.getCanonicalPath() + "\"");
                    //"form-data; name=\"xxxx\"; filename=\"" + zipPackage.getCanonicalPath() + "\""); 
                    
            formBuilder.setBody(fileBody);
                    //.setName("payload");
                    /*
            FormBodyPartBuilder formBuilder = FormBodyPartBuilder
                    .create()
                    .addField("Packaging", "http://purl.org/net/sword/package/SimpleZip")
                    .addField("Content-MD5", md5)
                    .addField("MIME-Version", "1.0")
                    .setBody(fileBody)
                    .setName("payload");
                            */
            FormBodyPart part = formBuilder.build();
            
                    
            MultipartEntityBuilder builder = MultipartEntityBuilder
                .create()
                .setMimeSubtype("related; "
                    + "type=\"application/atom+xml\"")
                .addPart(part);
            HttpEntity entity = builder.build();
            httpput.setEntity(entity);
            HttpResponse response = httpClient.execute(httpput);
            StatusLine status = response.getStatusLine();
            System.out.println("Status:" + status.getStatusCode());
            HeaderIterator iter = response.headerIterator();
            while (iter.hasNext()) {
                Header header = iter.nextHeader();
                System.out.println("header:" + header.getName() + ":" +
                    header.getValue());
            }
    }

}
