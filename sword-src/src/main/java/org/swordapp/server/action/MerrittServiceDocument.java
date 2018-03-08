package org.swordapp.server.action;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.AuthenticationException;
import org.cdlib.mrt.utility.StringUtil;
import org.swordapp.server.*;
import org.cdlib.mrt.inv.content.InvCollection;
import org.cdlib.mrt.inv.utility.InvDBUtil;
import org.cdlib.mrt.security.CertsLDAP;
import org.cdlib.mrt.utility.TException;
import org.cdlib.mrt.utility.TFrame;

public class MerrittServiceDocument
    extends AbstractMerrittAction
{
    
    protected static final String NAME = "MerrittServiceDocument";
    protected static final String MESSAGE = NAME + ": ";
    
    private SwordWorkspace swordWorkspace = null;
    
    public MerrittServiceDocument()
        throws SwordError, SwordServerException, SwordAuthException
    {
        super();
    }
    
    public List<InvCollection> getCollections(String user, String pwd)
        throws SwordAuthException, SwordServerException
    {
        System.out.println("getCollection entered");
        Connection connection = null;
        ArrayList<InvCollection> invList = new ArrayList<>();
        try {
            System.out.println("MerrittServiceDocument.getCOllection:"
                    + " - user:" + user
                    + " - pwd:" + pwd
            );
            List<String> profiles = ldap.getNames(user, pwd, user);
            if (profiles == null) {
                System.out.println("Profiles null");
                return null;
            }
            connection = db.getConnection(true);
            for (String profile : profiles) {
                InvCollection invCollection = InvDBUtil.getCollectionFromMnemonic(profile, connection, logger);
                invList.add(invCollection);
            }
            return invList;
            
        } catch (TException.USER_NOT_AUTHENTICATED una) {
            throw new SwordAuthException("user:" + user + " not authenticated");
            
        } catch (TException tex) {
            throw new SwordServerException("TException:" + tex);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof AuthenticationException){
                throw new SwordAuthException("user:" + user + " not authenticated");
            }
            throw new SwordServerException("Exception:" + ex);
        }
    }
}
