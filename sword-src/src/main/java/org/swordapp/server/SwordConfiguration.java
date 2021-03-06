package org.swordapp.server;

public interface SwordConfiguration
{
    boolean returnDepositReceipt();

    boolean returnStackTraceInError();

    boolean returnErrorBody();

    String generator();

    String generatorVersion();

    String administratorEmail();

	String getAuthType();

	boolean storeAndCheckBinary();

	String getTempDirectory();

	long getMaxUploadSize();

    String getAlternateUrl();

    String getAlternateUrlContentType();

    boolean allowUnauthenticatedMediaAccess();
}
