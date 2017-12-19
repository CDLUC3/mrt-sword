package org.swordapp.server;

public class SwordConfigurationDefault implements SwordConfiguration
{
    public boolean returnDepositReceipt()
    {
        return true;
    }

    public boolean returnStackTraceInError()
    {
        return true;
    }

    public boolean returnErrorBody()
    {
        return true;
    }

    public String generator()
    {
        return "http://www.swordapp.org/";
    }

    public String generatorVersion()
    {
        return "2.0";
    }

    public String administratorEmail()
    {
        return null;
    }

	public String getAuthType()
	{
		//return "None";
                return "Basic";
	}

	public boolean storeAndCheckBinary()
	{
		return false;
	}

	public String getTempDirectory()
	{
		return null;
	}

	public long getMaxUploadSize()
	{
		return -1;
	}

    public String getAlternateUrl()
    {
        return null;
    }

    public String getAlternateUrlContentType()
    {
        return null;
    }

    public boolean allowUnauthenticatedMediaAccess()
    {
        return false;
    }
}
