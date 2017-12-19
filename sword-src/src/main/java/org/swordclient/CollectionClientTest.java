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
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.TFrame;
import org.cdlib.mrt.security.Base64;

import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.content.FileBody;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.methods.HttpPost;
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
import org.apache.http.entity.InputStreamEntity;
/**
 *
 * @author dloy
 * ReplaceClient replace/edit/PUT SWORD content
 */
public class CollectionClientTest 
{
    protected URL url = null;
    protected String authCode = null;
    protected String submitter = null;
    protected String doi = null;
 
    /**
     * constructor
     * @param profile user profile
     * @param submitter name
     * @param authCode authorization code for submitter
     * @param url server URL
     */
    public CollectionClientTest(
            String doi,
            String submitter,
            String authCode,
            URL url
    ) 
    {
        this.submitter = submitter;
        this.authCode = authCode;
        this.url = url;
        this.doi = doi;
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
            File zipPackage = new File("/apps/replic/test/sword/big/big.zip");
            String md5 = "47523cc2298712563541a02efa3c986b";
            //File zipPackage = new File("/apps/replic/test/sword/big/reallybig.zip");
            //String md5 = "60715376077338a50cfa76a1c5a6dadc";
            String submitter = "ucb_dash_submitter";
            //URL url = new URL("http://sword-aws-dev.cdlib.org:39001/mrtsword/edit/dash_ucb/doi%3A10.20200%2Fhij1000106");
            URL url = new URL("http://uc3-mrtreplic2-dev.cdlib.org:28080/mrtsword/collection/dash_ucb");
            String authCode = "8L75av8z";
            String doi = "doi:10.20200/bigxxxxx3";
            /*
            String submitter = "test_dash_user";
            String localID = "doi:10.20200/xxxxxxxxx1";
            String md5 = "099265e9be557f0d6e9b8f3019e3e7bc";
            URL url = new URL("http://sword-aws.cdlib.org:39001/mrtsword");
            String profile = "merritt_demo";
            String authCode = "47XpCtdu";
            */
            CollectionClientTest client = new CollectionClientTest(doi, submitter, authCode, url);
            client.collection(zipPackage,  md5);

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
    public void collection(File zipPackage,  String md5) throws Exception 
    {
            
        System.setProperty("org.apache.commons.logging.Log","org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        //System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(6 * 3600 * 1000).build();
            HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
            String urlS = url.toString();
            System.out.println("URL:" + urlS);
            HttpPost httppost = new HttpPost(urlS);
            
            
            if (StringUtil.isNotEmpty(submitter) && StringUtil.isNotEmpty(authCode)) {
                String pass = submitter + ":" + authCode;
                String auth64 = Base64.encodeBytes(pass.getBytes("utf-8"));
                httppost.addHeader("Authorization", "Basic " + auth64);
                System.out.println("Authorization: " + "Basic " + auth64);
            }
            Header onBehalfOf=new BasicHeader("On-Behalf-Of", submitter);
            httppost.addHeader(onBehalfOf);
            
            Header packaging = new BasicHeader("Packaging", "http://purl.org/net/sword/package/SimpleZip");
            httppost.addHeader(packaging);
            
            Header slug=new BasicHeader("Slug", doi);
            httppost.addHeader(slug);
            
            Header contentDisposition=new BasicHeader("Content-Disposition", "attachment; filename=big.zip");
            httppost.addHeader(contentDisposition);
            
            Header contentMd5=new BasicHeader("Content-MD5", md5);
            httppost.addHeader(contentMd5);
            
            InputStreamEntity ise = new InputStreamEntity(new FileInputStream(zipPackage), zipPackage.length());
            httppost.setEntity(ise);
            System.out.println("EditCall properties:"
                    + " - zipPackage:" + zipPackage.length()
                    + " - filename:" + zipPackage.getCanonicalPath()
            );
            HttpResponse response = httpClient.execute(httppost);
            StatusLine status = response.getStatusLine();
            System.out.println("Status:" + status.getStatusCode());
            HeaderIterator iter = response.headerIterator();
            while (iter.hasNext()) {
                Header header = iter.nextHeader();
                System.out.println("header:" + header.getName() + ":" +
                    header.getValue());
            }
            HttpEntity outEntity = response.getEntity();
            if (outEntity != null) {
                String out = StringUtil.streamToString(outEntity.getContent(), "utf-8");
                System.out.println("response" + out);
            }
            
    }

}
