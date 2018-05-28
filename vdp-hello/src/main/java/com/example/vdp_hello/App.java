package com.example.vdp_hello;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		System.out.println("Hello World!");
		if (args.length != 5) {
			System.out.println(
					"Please call with parameters: <USER> <PASSWORD> <KEY_STORE_PATH> <KEY_STORE_PASSWORD> <PRIVATE_KEY_PASSWORD>");
			System.exit(0);
		}

		String USER = args[0];
		String PASSWORD = args[1];
		String KEY_STORE_PATH = args[2];
		String KEY_STORE_PASSWORD = args[3];
		String PRIVATE_KEY_PASSWORD = args[4];

		// Load client certificate into key store
		SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(new File(KEY_STORE_PATH),
				KEY_STORE_PASSWORD.toCharArray(), PRIVATE_KEY_PASSWORD.toCharArray()).build();

		// Allow TLSv1.2 protocol only
		SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslcontext,
				new String[] { "TLSv1.2" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		// CloseableHttpClient httpClient =
		// HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();

		HttpHost target = new HttpHost("sandbox.api.visa.com", 443, "https");
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
				new UsernamePasswordCredentials(USER, PASSWORD));
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider)
				.setSSLSocketFactory(sslSocketFactory).build();
		try {

			// Preemptive Authentication
			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local
			// auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(target, basicAuth);

			// Add AuthCache to the execution context
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			// Proxy setup
			HttpHost proxy = new HttpHost("127.0.0.1", 3128, "http");
			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

			HttpGet httpget = new HttpGet("https://sandbox.api.visa.com/vdp/helloworld");
			httpget.setConfig(config);

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

				@Override
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					System.out.println("----------------------------------------");
					System.out.println(response.getStatusLine());
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};

			System.out.println("Executing request " + httpget.getRequestLine());
			System.out.println(httpclient.execute(target, httpget, responseHandler, localContext));

		} finally {
			httpclient.close();
		}
	}
}
