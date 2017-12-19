package org.swordapp.server;

import java.io.InputStream;
import java.util.Date;
import org.swordapp.server.action.MerrittCollectionManager;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Text;
import org.swordapp.server.action.IngestHandler;
import org.cdlib.mrt.utility.DateUtil;
import org.cdlib.mrt.utility.URLEncoder;


public class CollectionDepositManagerImpl
    implements CollectionDepositManager
{
    private final String PREFIX;
    private final String EDITMEDIA;
    private final String EDIT;
    
    private final MerrittCollectionManager mcm;
    
    public CollectionDepositManagerImpl()
            throws SwordError, SwordServerException, SwordAuthException
    {
        mcm = new MerrittCollectionManager();
        PREFIX = mcm.getLink();
        EDITMEDIA = mcm.getEditMedia();
        EDIT = mcm.getEdit();
    }
    
    public DepositReceipt createNew(String collectionURI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
            throws SwordError, SwordServerException, SwordAuthException
    {
 
        if (deposit.getFile() != null) {
            try {
                System.out.println("***CollectionDepositManagerImpl" 
                        + " - file exists:" + deposit.getFile().getCanonicalPath()
                );
            } catch (Exception ex) {
                System.out.println("Exception" + ex);
            }
        } else {
            System.out.println("***CollectionDepositManagerImpl" 
                    + " - file does not exist"
            );
        }
        if (deposit.getInputStream() != null) {
            System.out.println("***CollectionDepositManagerImpl" 
                    + " - InputStream exists"
            );
        } else {
            System.out.println("***CollectionDepositManagerImpl" 
                    + " - InputStream does NOT exists"
            );
        }
        
        String userName = auth.getUsername();
        String onBehalfOf = auth.getOnBehalfOf();
        String authCode = auth.getPassword();
        String packaging = deposit.getPackaging();
        String md5 = deposit.getMd5();
        String mnemonic = mcm.getIdFromUri(collectionURI);
        String localID = deposit.getSlug();
        InputStream containerStream = deposit.getInputStream();
        System.out.println("***CollectionDepositManagerImpl"
            + " - collectionURI:" + collectionURI
            + " - mnemonic:" + mnemonic
            + " - Deposit.filename:" + deposit.getFilename()
            + " - Deposit.mimeType:" + deposit.getMimeType()
            + " - Deposit.slug:" + deposit.getSlug()
            + " - Deposit.md5:" + deposit.getMd5()
            + " - Deposit.packaging:" + deposit.getPackaging()
        );
        
        IngestHandler ingestHandler = mcm.process(
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
        DepositReceipt receipt = setResult(onBehalfOf, mnemonic, localID, ark);
        return receipt;
    }
            
    
    protected DepositReceipt setResult(String user, String mnemonic, String localID, String ark)
           throws SwordError, SwordServerException, SwordAuthException
    {
        System.out.println("setResult:"
                + " - user:" + user
                + " - localID:" + localID
                + " - ark:" + ark
        );
        String n2tnet = "http://n2t.net/";
        DepositReceipt receipt = new DepositReceipt();
        Entry entry = receipt.getWrappedEntry();
        entry.addAuthor(user);
        entry.setTitle(localID, Text.Type.TEXT);
        IRI id = new IRI(PREFIX + ark);
        entry.setId(n2tnet+ ark);
        
        receipt.setSplashUri(n2tnet + ark);
        
        IRI edit = null;
        try {
            edit = new IRI(EDIT +  mnemonic + '/' + URLEncoder.encode(localID, "utf-8"));
        } catch (Exception ex) {
            throw new SwordError("Encode exception:" + ex + " - ark:"+ ark);
        }
        receipt.setEditIRI(edit);
        
        IRI editMedia = null;
        try {
            editMedia = new IRI(EDITMEDIA +  URLEncoder.encode(ark, "utf-8"));
        } catch (Exception ex) {
            throw new SwordError("Encode exception:" + ex + " - ark:"+ ark);
        }
        receipt.setEditMediaIRI(editMedia);
        
        Date currentDate = DateUtil.getCurrentDate();
        //receipt.setLastModified(currentDate);
        entry.setUpdated(currentDate);
        
        IRI location  = new IRI(PREFIX + localID);
        receipt.setLocation(location);
        return receipt;
    }
            
    
    protected DepositReceipt setResultOriginal(String user, String localID, String ark)
           throws SwordError, SwordServerException, SwordAuthException
    {
        DepositReceipt receipt = new DepositReceipt();
        Entry entry = receipt.getWrappedEntry();
        entry.setId(localID);
        IRI id = new IRI(PREFIX + localID);
        receipt.setEditIRI(id);
        receipt.setEditMediaIRI(id);
        receipt.setSwordEditIRI(id);
        IRI location  = new IRI(PREFIX + ark);
        receipt.setLocation(location);
        entry.addAuthor(user);
        
        return receipt;
    }
    
}
