/******************************************************************************
Copyright (c) 2005-2012, Regents of the University of California
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

package org.swordapp.server.action;
import java.util.List;
import java.util.Properties;

import org.cdlib.mrt.s3.service.CloudStoreInf;
import org.cdlib.mrt.core.ServiceStatus;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.StringUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.cdlib.mrt.core.DateState;


import org.cdlib.mrt.inv.utility.DPRFileDB;
import org.cdlib.mrt.s3.service.NodeIO;
import org.cdlib.mrt.tools.SSMConfigResolver;
import org.cdlib.mrt.utility.PropertiesUtil;
import org.cdlib.mrt.utility.TFileLogger;
import org.cdlib.mrt.tools.YamlParser;
import org.cdlib.mrt.utility.LoggerAbs;
import org.json.JSONObject;

/**
 * Base properties for Inv
 * @author  dloy
 */

public class SwordConfig
{
    private static final String NAME = "ReplicationConfig";
    private static final String MESSAGE = NAME + ": ";
    private static final boolean DEBUG = false;
    
    protected JSONObject fileLoggerJSON = null;
    protected JSONObject serviceJSON = null;
    protected JSONObject log4jAppender = null;
    protected JSONObject jdb = null;
    protected JSONObject scanJSON = null;
    protected DPRFileDB db = null;
    //protected FileManager fileManager = null;
    protected LoggerInf logger = null;
    protected boolean shutdown = true;
    protected static NodeIO nodeIO = null;
    protected Properties serviceProperties = null;
    private static class Test{ };
    
    public static SwordConfig useYaml()
        throws TException
    {
        try {

            JSONObject swordInfoJSON = getYamlJson();
            SwordConfig swordConfig = new SwordConfig(swordInfoJSON);
            
            return swordConfig;
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
        
    }
    
    protected SwordConfig(JSONObject swordInfoJSON) 
        throws TException
    {
        try {
            System.out.println("***getYamlJson:\n" + swordInfoJSON.toString(3));
            
            serviceJSON = swordInfoJSON.getJSONObject("service");
            setServiceProp(serviceJSON);
            System.out.println(PropertiesUtil.dumpProperties("SwordConfig.serviceProperties", serviceProperties));
            log4jAppender = swordInfoJSON.getJSONObject("log4jAppender");
            jdb = swordInfoJSON.getJSONObject("db");
            
            JSONObject fileLogger = swordInfoJSON.getJSONObject("fileLogger");
            logger = setLogger(fileLogger);
            db = startDB();
            
        } catch (TException tex) {
            tex.printStackTrace();
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected static JSONObject getYamlJson()
       throws TException
    {
        try {
            String propName = "resources/yaml/swordConfig.yml";
            System.out.println("propName:" + propName);
            Test test=new Test();
            InputStream propStream =  test.getClass().getClassLoader().
                    getResourceAsStream(propName);
            String swordYaml = StringUtil.streamToString(propStream, "utf8");
            System.out.println("swordYaml:\n" + swordYaml);
            String invInfoConfig = getYamlInfo();
            System.out.println("\n\n***table:\n" + invInfoConfig);
            String rootPath = System.getenv("SSM_ROOT_PATH");
            System.out.append("\n\n***root:\n" + rootPath + "\n");
            SSMConfigResolver ssmResolver = new SSMConfigResolver();
            YamlParser yamlParser = new YamlParser(ssmResolver);
            System.out.println("\n\n***SwordYaml:\n" + swordYaml);
            LinkedHashMap<String, Object> map = yamlParser.parseString(swordYaml);
            LinkedHashMap<String, Object> lmap = (LinkedHashMap<String, Object>)map.get(invInfoConfig);
            if (lmap == null) {
                throw new TException.INVALID_CONFIGURATION(MESSAGE + "Unable to locate configuration");
            }
            //System.out.println("lmap not null");
            yamlParser.loadConfigMap(lmap);

            yamlParser.resolveValues();
            return yamlParser.getJson();
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
        }
    }
    
    protected static String getYamlInfo()
       throws TException
    { 
        String swordInfoConfig = System.getenv("which-sword-info");
        if (swordInfoConfig == null) {
            swordInfoConfig = System.getenv("MERRITT_SWORD_INFO");
        }
        if (swordInfoConfig == null) {
            swordInfoConfig = "sword-info";
        }
        return swordInfoConfig;
    }
    
    public void setServiceProp(JSONObject serviceJSON)
        throws TException
    {
        serviceProperties = new Properties();
        try {
            addServiceProp("maxUploadSize");           
            addServiceProp("jssecacert");
            addServiceProp("ldapHost");           
            addServiceProp("link");
            addServiceProp("editmedia");           
            addServiceProp("edit");
            addServiceProp("ingestUrlAdd");           
            addServiceProp("ingestUrlUpdate");          
            addServiceProp("retainTargetUrl");
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    protected void addServiceProp(String key)
        throws TException
    {
        if (serviceProperties == null) {
            throw new TException.INVALID_CONFIGURATION("serviceProperties not set");
        }
        String value = null;
        try {
            value = serviceJSON.getString(key);
        } catch (Exception ex) {
            throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "Required service property '" + key + "' not set");
        }
        serviceProperties.setProperty(key, value);
    }
    
    public DPRFileDB startDB()
       throws TException
    {
        return startDB(logger);
    }
    
    public DPRFileDB getDB()
       throws TException
    {
        return db;
    }
    
    public void shutdownDB()
       throws TException
    {
        if (db == null) {
            return;
        } else {
            try {
                db.shutDown();
            } catch (Exception ex) { }
            db = null;
        }
    }
    
    public DPRFileDB startDB(LoggerInf logger)
       throws TException
    {
        try {
            /*
              "db": {
                "user": "invrw",
                "password": "ixxx",
                "url": "uc3db-inv-stg.cdlib.org",
                "name": "inv",
                "encoding": "OPTIONAL",
                "maxConnections": "OPTIONAL"
                }
            jdbc:mysql://uc3db-inv-stg.cdlib.org:3306/inv?characterEncoding=UTF-8&characterSetResults=UTF-8
            
    public DPRFileDB(LoggerInf logger,
            String dburl,
            String dbuser,
            String dbpass)
        throws TException
    {
        this.logger = logger;
        this.dburl = dburl;
        this.dbuser = dbuser;
        this.dbpass = dbpass;
        setPool();
    }
            */
            
            String  password = jdb.getString("password");
            String  user = jdb.getString("user");
            
            String server = jdb.getString("host");
            String encoding = jdb.getString("encoding");
            if (encoding.equals("OPTIONAL")) {
                encoding = "";
            } else {
                encoding = "?" + encoding;
            }
            String name = jdb.getString("name");
            String url = "jdbc:mysql://" + server + ":3306/" + name + encoding;
            System.out.println("url:" + url);
            db = new DPRFileDB(logger, url, user, password);
            return db;
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    /**
     * set local logger to node/log/...
     * @param path String path to node
     * @return Node logger
     * @throws Exception process exception
     */
    protected LoggerInf setLogger(JSONObject fileLogger)
        throws Exception
    {
        String qualifier = fileLogger.getString("qualifier");
        String path = fileLogger.getString("path");
        String name = fileLogger.getString("name");
        Properties logprop = new Properties();
        logprop.setProperty("fileLogger.message.maximumLevel", "" + fileLogger.getInt("messageMaximumLevel"));
        logprop.setProperty("fileLogger.error.maximumLevel", "" + fileLogger.getInt("messageMaximumError"));
        logprop.setProperty("fileLogger.name", fileLogger.getString("name"));
        logprop.setProperty("fileLogger.trace", "" + fileLogger.getInt("trace"));
        logprop.setProperty("fileLogger.qualifier", fileLogger.getString("qualifier"));
        if (StringUtil.isEmpty(path)) {
            throw new TException.INVALID_OR_MISSING_PARM(
                    MESSAGE + "setCANLog: path not supplied");
        }

        File canFile = new File(path);
        File log = new File(canFile, "logs");
        if (!log.exists()) log.mkdir();
        String logPath = log.getCanonicalPath() + '/';
        
        if (DEBUG) System.out.println(PropertiesUtil.dumpProperties("LOG", logprop)
            + "\npath:" + path
            + "\nlogpath:" + logPath
        );
        LoggerInf logger = LoggerAbs.getTFileLogger(name, log.getCanonicalPath() + '/', logprop);
        return logger;
    }

    public LoggerInf getLogger() {
        return logger;
    }

    public JSONObject getServiceJSON() {
        return serviceJSON;
    }

    public JSONObject getScanJSON() {
        return scanJSON;
    }
    
    public void setLogger(LoggerInf logger) {
        this.logger = logger;
    }

    public JSONObject getJdb() {
        return jdb;
    }

    public void setJdb(JSONObject jdb) {
        this.jdb = jdb;
    }

    public static NodeIO getNodeIO() {
        return nodeIO;
    }

    public void setNodeIO(NodeIO nodeIO) {
        this.nodeIO = nodeIO;
    }

    public Properties getServiceProperties() {
        return serviceProperties;
    }
    
    public static void main(String[] argv) {
    	
    	try {
            
            LoggerInf logger = new TFileLogger("test", 50, 50);
            SwordConfig swordConfig = SwordConfig.useYaml();
            if (false) return;
            ServiceStatus serviceStatus = null;
            DPRFileDB db = swordConfig.startDB();
            if (db == null) serviceStatus = ServiceStatus.shutdown;
            else  serviceStatus = ServiceStatus.running;
            
            System.out.println("setDB dbStatus:" + serviceStatus);
            if (serviceStatus == ServiceStatus.running) {
                db.shutDown();
            }
            Properties serviceProp = swordConfig.getServiceProperties();
            System.out.println(PropertiesUtil.dumpProperties("ServiceProp", serviceProp));
        } catch (Exception ex) {
                // TODO Auto-generated catch block
                System.out.println("Exception:" + ex);
                ex.printStackTrace();
        }
    }
    
    
}
