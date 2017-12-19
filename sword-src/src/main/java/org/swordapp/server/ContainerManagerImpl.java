package org.swordapp.server;

import org.swordapp.server.*;
import java.util.Map;
import org.swordapp.server.action.MerrittContainerManager;

public class ContainerManagerImpl
    implements ContainerManager
{
    private final MerrittContainerManager merrittContainerManager;
    
    public ContainerManagerImpl()
        throws SwordError, SwordServerException, SwordAuthException
    {
        merrittContainerManager = new MerrittContainerManager();
    }
    
    public DepositReceipt getEntry(String editIRI, Map<String, String> accept, AuthCredentials auth, SwordConfiguration config)
            throws SwordServerException, SwordError, SwordAuthException
    {
        return merrittContainerManager.notAllowed("getEntry");
    }

    public DepositReceipt replaceMetadata(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        return merrittContainerManager.notAllowed("replaceMetadata");
    }

    public DepositReceipt replaceMetadataAndMediaResource(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        System.out.println("replaceMetadataAndMediaResource called");
        return merrittContainerManager.replaceMetadataAndResources(editIRI, deposit, auth, config);
    }

    public DepositReceipt addMetadataAndResources(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        System.out.println("addMetadataAndResources called");
        return merrittContainerManager.addMetadataAndResources(editIRI, deposit, auth, config);
    }

    public DepositReceipt addMetadata(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        return merrittContainerManager.notAllowed("addMetadata");
    }

    public DepositReceipt addResources(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        return merrittContainerManager.notAllowed("addResources");
    }

    public void deleteContainer(String editIRI, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        merrittContainerManager.notAllowed("deleteContainer");
    }

    public DepositReceipt useHeaders(String editIRI, Deposit deposit, AuthCredentials auth, SwordConfiguration config)
        throws SwordError, SwordServerException, SwordAuthException
    {
        return merrittContainerManager.notAllowed("getEntry");
    }

    public boolean isStatementRequest(String editIRI, Map<String, String> accept, AuthCredentials auth, SwordConfiguration config)
		throws SwordError, SwordServerException, SwordAuthException
    {
        return false;
    }
}
