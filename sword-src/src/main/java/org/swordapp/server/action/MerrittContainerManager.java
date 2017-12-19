package org.swordapp.server.action;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.abdera.i18n.iri.IRI;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;

public class MerrittContainerManager
    extends AbstractMerrittAction
{
    
    protected static final String NAME = "MerrittContainerManager";
    protected static final String MESSAGE = NAME + ": ";
    
    public MerrittContainerManager()
        throws SwordError, SwordServerException, SwordAuthException
    {
        super();
    }
    
    public DepositReceipt addMetadataAndResources(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {    
        
        if (deposit.getInputStream() != null) {
            System.out.println("***addMetadataAndResources" 
                    + " - InputStream exists"
            );
        } else {
            System.out.println("***addMetadataAndResources" 
                    + " - InputStream does NOT exists"
            );
        }
        
        String packaging = deposit.getPackaging();
        String md5 = deposit.getMd5();
        String mnemonic = getMnemonicFromUri(editIRI);
        String localID = getIdFromUri(editIRI);;
        InputStream containerStream = deposit.getInputStream();
        System.out.println("***CollectionDepositManagerImpl"
            + " - collectionURI:" + editIRI
            + " - mnemonic:" + mnemonic
            + " - localID:" + localID
            + " - Deposit.filename:" + deposit.getFilename()
            + " - Deposit.mimeType:" + deposit.getMimeType()
            + " - Deposit.slug:" + deposit.getSlug()
            + " - Deposit.md5:" + deposit.getMd5()
            + " - Deposit.packaging:" + deposit.getPackaging()
        );
        
        IngestHandler ingestHandler = process(
            auth,
            packaging, 
            md5,
            mnemonic,
            localID,
            localID,
            containerStream);
        if (!ingestHandler.isCompleted()) {
            throw new SwordError("ingest fails:" + ingestHandler.getIngestResponse());
        }
        String ark = ingestHandler.getArk();
        DepositReceipt receipt = new DepositReceipt();
        IRI location  = new IRI(getLink() + localID);
        receipt.setLocation(location);
        receipt.setEmpty(true);
        return receipt;
    }
    
    public DepositReceipt replaceMetadataAndResources(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {    
        if (deposit.getFile() != null) {
            try {
                System.out.println("***replaceMetadataAndResources" 
                        + " - file exists:" + deposit.getFile().getCanonicalPath()
                );
            } catch (Exception ex) {
                System.out.println("Exception" + ex);
            }
        } else {
            System.out.println("***replaceMetadataAndResources" 
                    + " - file does not exist"
            );
        }
        
        String packaging = deposit.getPackaging();
        String md5 = deposit.getMd5();
        String mnemonic = getMnemonicFromUri(editIRI);
        String localID = getIdFromUri(editIRI);
        File containerFile = deposit.getFile();
        System.out.println("***replaceMetadataAndResources"
            + " - collectionURI:" + editIRI
            + " - mnemonic:" + mnemonic
            + " - localID:" + localID
            + " - Deposit.filename:" + deposit.getFilename()
            + " - Deposit.mimeType:" + deposit.getMimeType()
            + " - Deposit.slug:" + deposit.getSlug()
            + " - Deposit.md5:" + deposit.getMd5()
            + " - Deposit.packaging:" + deposit.getPackaging()
        );
        
        IngestHandler ingestHandler = process(
            auth,
            packaging, 
            md5,
            mnemonic,
            localID,
            localID,
            containerFile);
        if (!ingestHandler.isCompleted()) {
            throw new SwordError("ingest fails:" + ingestHandler.getIngestResponse());
        }
        String ark = ingestHandler.getArk();
        DepositReceipt receipt = new DepositReceipt();
        IRI location  = new IRI(getLink() + localID);
        receipt.setLocation(location);
        receipt.setEmpty(true);
        return receipt;
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
            AuthCredentials auth,
            String packaging, 
            String md5,
            String mnemonic,
            String localID,
            String comment,
            File containerFile)
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
            containerFile);
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
        boolean complete = false;
        try {
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
            
        }
    }
    
    public IngestHandler process(
            String user,
            String authCode,
            String packaging, 
            String md5,
            String profile,
            String localID,
            String comment,
            File containerFile)
        throws SwordError, SwordServerException, SwordAuthException
    {
        
        boolean complete = false;
        try {
            String urlS = this.getServiceProperties().getProperty("ingestUrlUpdate");
            if (StringUtil.isAllBlank(urlS)) {
                throw new SwordError("ingest URL not defined in info");
            }
            URL url = new URL(urlS);
            IngestHandler handler = new IngestHandler(url, profile, user, authCode, retainTargetUrl, logger);
            complete = handler.process(containerFile, packaging, md5, localID, comment);
            
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
