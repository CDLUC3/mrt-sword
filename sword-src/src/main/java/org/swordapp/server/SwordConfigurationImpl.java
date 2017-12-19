package org.swordapp.server;

import org.swordapp.server.action.MerrittServiceDocument;

public class SwordConfigurationImpl 
    extends SwordConfigurationDefault
    implements SwordConfiguration
{

    private MerrittServiceDocument msd = null;
    public SwordConfigurationImpl()
    {
        try {
            msd = new MerrittServiceDocument();
        } catch (Exception ex) {
            System.out.println("SwordConfigurationImpl exception:" + ex);
        }
    }

	public long getMaxUploadSize()
	{
            if (msd == null) {
                return -1;
            }
            else {
                System.out.println("---MerrittServiceDocument found");
                return msd.getMaxUploadSize();
            }
	}

}
