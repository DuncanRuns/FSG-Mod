package me.duncanruns.fsgmod.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.duncanruns.fsgmod.FSGMod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.function.Consumer;

public final class GrabUtil {
    private static final Gson GSON = new Gson();
    private static final HttpClient httpClient;

    static {
        try {
            httpClient = getHttpClient();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private GrabUtil() {
    }

    private static HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //https://stackoverflow.com/a/28847175

        try {
            // Test a Let's Encrypt valid page
            IOUtils.toString(new URL("https://valid-isrgrootx1.letsencrypt.org/").openStream(), Charset.defaultCharset());
            // Normal functionality!
            return HttpClientBuilder.create().build();
        } catch (Exception ignored) {
            FSGMod.LOGGER.warn("Outdated Java, GrabUtil is using an insecure HttpClient!");
        }

        HttpClientBuilder b = HttpClientBuilder.create();

        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) {
                return true;
            }
        }).build();
        b.setSslcontext(sslContext);

        X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        b.setConnectionManager(connMgr);
        return b.build();
    }

    public static String grab(String origin) throws IOException {
        HttpGet request = new HttpGet(origin);
        CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String out = EntityUtils.toString(entity);
        response.close();
        return out;
    }

    public static JsonObject grabJson(String origin) throws IOException, JsonSyntaxException {
        return GSON.fromJson(grab(origin), JsonObject.class);
    }

    public static void download(String origin, Path destination, Consumer<Integer> bytesReadConsumer) throws IOException {
        HttpGet request = new HttpGet(origin);
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request);
             BufferedInputStream sourceStream = new BufferedInputStream(response.getEntity().getContent());
             OutputStream destinationStream = Files.newOutputStream(destination)) {
            int bufferSize = 1024;
            int totalBytesRead = 0;
            {
                byte[] dataBuffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = sourceStream.read(dataBuffer, 0, bufferSize)) != -1) {
                    destinationStream.write(dataBuffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    bytesReadConsumer.accept(totalBytesRead);
                }
            }
        }
    }

    public static void download(String origin, Path destination) throws IOException {
        download(origin, destination, integer -> {
        });
    }
}