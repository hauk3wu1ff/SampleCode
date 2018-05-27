package com.example.vdp_hello;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		System.out.println("Hello World!");

		/*
		 * String KEY_STORE_PATH = args[0]; String KEY_STORE_PASSWORD = args[1]; String
		 * PRIVATE_KEY_PASSWORD = args[2];
		 * 
		 * // Load client certificate into key store SSLContext sslcontext =
		 * SSLContexts.custom().loadKeyMaterial(new File(KEY_STORE_PATH),
		 * KEY_STORE_PASSWORD.toCharArray(),
		 * PRIVATE_KEY_PASSWORD.toCharArray()).build(); // Allow TLSv1.2 protocol only
		 * SSLConnectionSocketFactory sslSocketFactory = new
		 * SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1.2" }, null,
		 * SSLConnectionSocketFactory.getDefaultHostnameVerifier()); CloseableHttpClient
		 * httpClient =
		 * HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
		 */

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope("httpbin.org", 80),
				new UsernamePasswordCredentials("user", "passwd"));
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		try {
			HttpGet httpget = new HttpGet("http://httpbin.org/basic-auth/user/passwd");

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
			System.out.println(httpclient.execute(httpget, responseHandler));

		} finally {
			httpclient.close();
		}
	}
}
