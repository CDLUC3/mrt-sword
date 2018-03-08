package org.swordapp.server.action;

import java.io.File;
import java.io.InputStream;
import org.swordapp.server.*;
import org.cdlib.mrt.utility.FileUtil;
import org.cdlib.mrt.utility.FixityTests;
import org.cdlib.mrt.utility.LoggerInf;

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
