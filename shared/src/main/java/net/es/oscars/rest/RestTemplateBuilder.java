package net.es.oscars.rest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;


public class RestTemplateBuilder {

    public RestTemplate build() throws Exception {

        String sharedusername = "oscars";
        String sharedpassword = "oscars-shared";
        String truststorepath = "./config/oscars.jks";
        File truststorefile = new File(truststorepath);

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(truststorefile).build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(sharedusername, sharedpassword));

        HttpClient httpClient = HttpClientBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();

        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;

    }
}
