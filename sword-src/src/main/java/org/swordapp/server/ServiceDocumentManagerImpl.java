package org.swordapp.server;
import java.util.List;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.action.MerrittServiceDocument;

public class ServiceDocumentManagerImpl
    implements ServiceDocumentManager
{
 
    protected MerrittServiceDocument msd = null;
    
    public ServiceDocumentManagerImpl()
            throws SwordError, SwordServerException, SwordAuthException
    {    
        msd = new MerrittServiceDocument();
    }
    
    @Override
    public ServiceDocument getServiceDocument(String sdUri, AuthCredentials auth, SwordConfiguration config) 
            throws SwordError, SwordServerException, SwordAuthException
    {
        ServiceDocument serviceDocument = new ServiceDocument();
        SwordWorkspace swordWorkspace = new SwordWorkspace();
        swordWorkspace.setTitle("Merritt");
        serviceDocument.setMaxUploadSize(msd.getMaxUploadSize());
        serviceDocument.addWorkspace(swordWorkspace);
        
        String onBehalfOf = auth.getOnBehalfOf();
        String userName = auth.getUsername();
        String pw = auth.getPassword();
        if (StringUtil.isAllBlank(onBehalfOf)) {
            return serviceDocument;
        }
        System.out.println("getServiceDocument:" 
                + " - onBehalfOf=" + onBehalfOf
                + " - userName=" + userName
                + " - pw=" + pw
        );
        List<InvCollection> listCollections = msd.getCollections(onBehalfOf, pw);
        for (InvCollection collection : listCollections) {
            SwordCollection swordCollection = new SwordCollection();
            addCollectionContent(swordCollection, collection);
            swordWorkspace.addCollection(swordCollection);
        }
        return serviceDocument;
    }
    
    protected void addCollectionContent(SwordCollection swordCollection, InvCollection collection)
            throws SwordError, SwordServerException, SwordAuthException
    {
        swordCollection.setHref("http://merritt.cdlib.org/sword/v2/" +  collection.getMnemonic());
        swordCollection.setTitle(collection.getName());
        swordCollection.setAccept("*/*");
        swordCollection.addMultipartAccepts("*/*");
        swordCollection.setMediation(true);
        swordCollection.addAcceptPackaging("http://purl.org/net/sword/package/SimpleZip");
    }
}