package org.swordapp.server.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import org.cdlib.mrt.utility.StringUtil;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.LoggerInf;
import org.cdlib.mrt.utility.TException;

public class EditHandler
{
    
    protected static final String NAME = "EditHandler";
    protected static final String MESSAGE = NAME + ": ";
    protected static final boolean DEBUG = true;
    
    private HttpServletRequest request = null;
    private String boundry = null;
    private String md5 = null;
    private HashMap<String, String> headers = new HashMap<>();
    private File requestFile = null;
    private File zipFile = null;
    private long zipOffset = 0;
    private long zipLen = 0;
    private boolean tempFiles = false;
    
    public EditHandler(HttpServletRequest request)
        throws TException
    {
        try {
            this.request = request;
            requestFile = FileUtil.getTempFile("httpin", ".txt");
            saveFile(this.request, requestFile);
            zipFile = FileUtil.getTempFile("zip", ".zip");
            tempFiles = true;
            
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public EditHandler(File requestFile, File zipFile)
        throws TException
    {
        try {
            this.requestFile = requestFile;
            this.zipFile = zipFile;
            tempFiles = false;
        
        } catch (Exception ex) {
            throw new TException(ex);
        }
        
    }
        /**
     * Main method
     */
    public static void main(String args[])
    {
 
        File saveFile = new File("/apps/replic/test/sword/mime/mime.out");
        File outZip= new File("/apps/replic/test/sword/mime/out.zip");
        LoggerInf logger;
        try {
            EditHandler editHandler = new EditHandler(saveFile, outZip);
            editHandler.parse();
            

        } catch(Exception e) {
                e.printStackTrace();
                System.out.println(
                    "Main: Encountered exception:" + e);
                System.out.println(
                        StringUtil.stackTrace(e));
        }
    }
    

    public static long saveFile(HttpServletRequest request, File saveFile)
        throws TException
    {
        try {
            
            InputStream inStream = request.getInputStream();
            FileUtil.stream2File(inStream, saveFile);
            return saveFile.length();
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public void  parse()
        throws TException
    {
        RandomAccessFile raf = null;
        try {
            long size = requestFile.length();
            //Construct BufferedReader from InputStreamReader
            raf = new RandomAccessFile(requestFile,"r" );
            
            boundry = raf.readLine();
            if (boundry == null) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "no file content");
            }
            if (!boundry.substring(0,2).equals("--")) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "boundry not found");
            }
            if (DEBUG) System.out.println("boundry:" + boundry);
            if (DEBUG) System.out.println("length=" + requestFile.length());
            String line = null;
            while ((line = raf.readLine()) != null) {
                if (line.length() == 0) break;
                addHeader(line);
            }
            zipOffset = raf.getFilePointer();
            if (DEBUG) System.out.println("zipOffset=" + zipOffset);
            
            long seekPos = size - 2000;
            if (seekPos > boundry.length()) {
                raf.seek(seekPos);
            }
            boolean boundaryFound = false;
            while ((line = raf.readLine()) != null) {
                if (line.startsWith(boundry)) {
                    if (DEBUG) System.out.println("boundary found:" + raf.getFilePointer()
                            + " - line=" + line
                            + " - linelength=" + line.length()
                    );
                    boundaryFound = true;
                    break;
                }
            }
            if (!boundaryFound) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "final boundry not found:" + boundry
                );
            }
            long end = raf.getFilePointer() - line.length() - 4;
            raf.seek(end);
            line = raf.readLine();
            zipLen = end - zipOffset;
            if (DEBUG) System.out.println("line=" + line
                            + " - end=" + end
                            + " - zipOffset=" + zipOffset
                            + " - zipLen=" + zipLen
            );
            md5 = getZip(requestFile, zipFile, zipOffset, zipLen);
            System.out.println("Final:"
                            + " - md5=" + md5
                            + " - zipLen=" + zipLen
                            + " - bldlen=" + zipFile.length()
            );
            if (tempFiles) {
                if (DEBUG) System.out.println("delete request:" + requestFile.getAbsolutePath());
                requestFile.delete();
            }
            
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception ex) { }
            }
        }
    }
    
    protected void addHeader(String line) 
        throws TException
    {
        try {
            String parts[] = line.split("\\:\\s+");
            if (parts.length != 2) {
                throw new TException.INVALID_OR_MISSING_PARM(MESSAGE + "parts header not valid:" + line);
            }
            headers.put(parts[0], parts[1]);
            if (DEBUG) System.out.println("header:" + parts[0] + "+:+"+ parts[1]);
        
        } catch (TException tex) {
            throw tex;
            
        } catch (Exception ex) {
            throw new TException(ex);
        }
    }
    
    public static String getZip(File inFile, File outFile, long start, long len)
        throws TException
    {
        RandomAccessFile raf = null;
        FileOutputStream fop = null;
        try {

            // if file doesnt exists, then create it
            if (!outFile.exists()) {
                    outFile.createNewFile();
            }
            fop = new FileOutputStream(outFile);
            raf = new RandomAccessFile(inFile,"r" );
            raf.seek(start);
            byte[] bytes = new byte[100000];
            long readLen = 0;
            long remaining = len;
            MessageDigest algorithm = 
                MessageDigest.getInstance("md5");
            while (true) {
                readLen = raf.read(bytes);
                if (readLen == -1) break;
                if (remaining < readLen) {
                    int lastRead = (int)remaining;
                    fop.write(bytes, 0, lastRead);
                    algorithm.update(bytes, 0, lastRead);
                    if (DEBUG) System.out.println("lastRead:" + lastRead);
                    break;
                }
                remaining -= readLen;
                fop.write(bytes, 0, (int)readLen);
                algorithm.update(bytes, 0, (int)readLen);
            }
            fop.close();
            String retMd5 = getMd5(algorithm);
            return retMd5;
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception ex) { }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (Exception ex) { }
            }
        }
        
    }
    
    public static String getMd5(MessageDigest algorithm)
        throws TException
    {
        try {
            byte[] digest = algorithm.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<digest.length;i++) {
                String val = Integer.toHexString(0xFF & digest[i]);
                if (val.length() == 1) val = "0" + val;
                hexString.append(val);
            }
            String checksum = hexString.toString();
            return checksum;
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new TException(ex);
            
        }
    }

    public String getBoundry() {
        return boundry;
    }

    public String getMd5() {
        return md5;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public File getZipFile() {
        return zipFile;
    }
    
    public String getHeader(String key)
    {
        return headers.get(key);
    }
}
