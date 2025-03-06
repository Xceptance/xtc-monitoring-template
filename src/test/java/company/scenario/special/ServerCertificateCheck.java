package company.scenario.special;


import static org.junit.Assert.fail;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.SessionImpl;

/**
 * Test class for validating SSL/TLS server certificates. This class performs several certificate validation checks
 * including:
 * <ul>
 * <li>Certificate retrieval from a specified host and port</li>
 * <li>Current validity verification</li>
 * <li>Certificate fingerprint validation</li>
 * <li>Future expiration date checking</li>
 * </ul>
 * The test configuration is driven by XLT properties:
 * <ul>
 * <li>xlt.certificate.host - Target host for certificate validation</li>
 * <li>xlt.certificate.port - Port number (defaults to 443)</li>
 * <li>xlt.certificate.maxRetrieveRetries - Maximum retry attempts for certificate retrieval (defaults to 5)</li>
 * <li>xlt.certificate.fingerprint - Expected SHA-256 certificate fingerprint</li>
 * <li>xlt.certificate.numberOfDaysBeforeExpiration - Days to check for certificate validity (defaults to 30)</li>
 * </ul>
 */
public class ServerCertificateCheck extends com.xceptance.xlt.api.tests.AbstractTestCase
{
    @Test
    public void test() throws Exception
    {
        final X509Certificate[] certificates = retrieveCertificates();
        
        checkValidity(certificates);

        validateSignature(certificates);

        certificateExpiration(certificates);
    }

    
    
    /**
     * Retrieves X.509 certificates from a specified host and port with retry capability.
     * This method attempts to establish an SSL connection and fetch the server's certificate chain.
     * If the retrieval fails, it will retry up to the specified maximum number of attempts.
     *
     * @return an array of X509Certificate objects representing the server's certificate chain,
     *         or null if all retry attempts fail
     *         
     * @throws Exception if there are issues with property validation or certificate retrieval
     *                   after all retry attempts are exhausted
     */
    private X509Certificate[] retrieveCertificates() throws Exception
    {
        startNextAction("retrieve certificate");

        final String host = XltProperties.getInstance().getProperty("xlt.certificate.host");
        final int port = XltProperties.getInstance().getProperty("xlt.certificate.port", 443);
        final int maxRetries = XltProperties.getInstance()
                                            .getProperty("xlt.certificate.maxRetrieveRetries", 5);

        Assert.assertNotNull("property for host not found", host);
        Assert.assertNotNull("property for port not found", port);
        Assert.assertNotNull("property for port not found", maxRetries);
        final X509Certificate[] certificates = retrieveCertificates(host, port, maxRetries);
        Assert.assertNotNull("no certificates to validate", certificates);
        Assert.assertTrue("no certificates to validate", certificates.length > 0);

        // log retrieved certificates
        for (int i = 0; i < certificates.length; i++)
        {
            Session.getCurrent().getValueLog().put(String.format("retrieved certificate %s: ", i), certificates[i]);
        }

        return certificates;
    }

    /**
     * Validates that all certificates in the provided chain are currently valid.
     * This method checks each certificate's validity period against the current date
     * and time. The validation is performed using the certificate's built-in
     * checkValidity() method.
     *
     * @param certificates an array of X509Certificate objects to validate
     * @throws AssertionError if any certificate in the chain is not currently valid,
     *                       including the error message from the underlying exception
     * 
     */
    private void checkValidity(final X509Certificate[] certificates)
    {
        startNextAction("validate that the certificate is currently valid");
        for (final X509Certificate certificate : certificates)
        {
            try
            {
                certificate.checkValidity();
            } catch (final Exception e)
            {
                fail("certificate is currently not valid" + e.getMessage());
            }
        }
    }

    /**
    * Validates the certificate signature by comparing the SHA-256 fingerprint of the first
    * certificate in the chain against an expected value from the XLT properties.
    * 
    * The method calculates the SHA-256 hash of the DER-encoded certificate and compares
    * it to the configured fingerprint value. This ensures the certificate has not been
    * tampered with or replaced.
    *
    * @param certificates an array of X509Certificate objects, where the first certificate
    *                    is validated against the expected fingerprint
    * @throws CertificateEncodingException if there is an error encoding the certificate
    * @throws AssertionError if the calculated fingerprint does not match the expected value
    *                       from the XLT properties
    * 
    */
    private void validateSignature(final X509Certificate[] certificates) throws CertificateEncodingException
    {
        startNextAction("validate certificate signature");
        final String fingerprint = XltProperties.getInstance()
                                                .getProperty("xlt.certificate.fingerprint");

        final String sha256Hex = DigestUtils.sha256Hex(certificates[0].getEncoded());
        Assert.assertEquals("certificate fingerprint does not match the expectations", fingerprint, sha256Hex);
    }

    /**
    * Validates that all certificates in the chain will remain valid for a specified
    * number of days into the future. The default validation period is 30 days if not
    * configured otherwise in XLT properties.
    * 
    * The method checks each certificate's validity against a future date calculated
    * by adding the configured number of days to the current date. It logs both the
    * current date and the calculated expiration date to the session's value log.
    *
    * @param certificates an array of X509Certificate objects to check for future validity
    * @throws AssertionError if any certificate will expire within the specified number
    *                       of days, including which certificate failed and when
    * 
    */
    private void certificateExpiration(final X509Certificate[] certificates)
    {
        startNextAction("validate certificate expiration");

        final int numberOfDaysBeforeExpiration = XltProperties.getInstance()
                                                              .getProperty("xlt.numberOfDaysBeforeExpiration", 30);

        final Date now = new Date();
        final Date expirationDate = DateUtils.addDays(now, numberOfDaysBeforeExpiration);
        Session.getCurrent().getValueLog().put("now", now);
        Session.getCurrent().getValueLog().put("expirationDate", expirationDate);
        for (final X509Certificate certificate : certificates)
        {
            try
            {
                certificate.checkValidity(expirationDate);
            } catch (final Exception e)
            {
                fail(String.format("certificate is not valid in %s days. %s", numberOfDaysBeforeExpiration,
                        e.getMessage()));
            }
        }
    }

    /**
    * Attempts to establish an SSL connection and retrieve the server's certificate chain.
    * 
    * @return an array of X509Certificate objects representing the server's certificate
    *         chain, or null if all retry attempts fail
    *         
    * @throws Exception if there are issues creating the SSL socket or retrieving
    *                   certificates (after exhausting all retry attempts)
    *                   
    */
    private X509Certificate[] retrieveCertificates(final String host, final int port, final int maxRetries)
            throws Exception
    {
        int retries = 0;
        while (retries < maxRetries)
        {
            try
            {
                final SSLSocketFactory factory = createInsecureSslSocketFactory();
                try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port))
                {
                    socket.setEnabledProtocols(new String[] { "TLSv1.2", "TLSv1.3" });
                    final SSLParameters sslParams = new SSLParameters();
                    sslParams.setEndpointIdentificationAlgorithm("HTTPS");
                    socket.setSSLParameters(sslParams);
                    return (X509Certificate[]) socket.getSession().getPeerCertificates();
                }

            } catch (final Exception e)
            {
                Session.getCurrent().getValueLog().put(
                        String.format("retrieve certificates catched exception with retry %s: ", retries),
                        e.getMessage());
                retries++;
            }
        }
        return null;
    }

    private SSLSocketFactory createInsecureSslSocketFactory()
    {
        try
        {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustManagers = new TrustManager[] { new X509TrustManager()
                {
                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                    {
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                    {
                    }
                } };

            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e)
        {
            throw new XltException("Failed to create insecure SSL socket factory", e);
        }
    }

    public static void startNextAction(final String name)
    {
        SessionImpl.getCurrent().getRequestHistory().add(name);
    }
}