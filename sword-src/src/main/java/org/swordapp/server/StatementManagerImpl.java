package org.swordapp.server;

import java.util.Map;

public class StatementManagerImpl
    implements StatementManager
{
    public Statement getStatement(String iri, Map<String, String> accept, AuthCredentials auth, SwordConfiguration config)
        throws SwordServerException, SwordError, SwordAuthException
    {
        System.out.println("StatmentManagerImpl called");
        try {
            throw new Exception();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
