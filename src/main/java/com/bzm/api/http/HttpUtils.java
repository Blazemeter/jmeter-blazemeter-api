package com.bzm.api.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

  public static final String PROXY_HOST = "http.proxyHost";
  public static final String PROXY_PORT = "http.proxyPort";
  public static final String PROXY_USER = "http.proxyUser";
  public static final String PROXY_PASS = "http.proxyPass";

  protected static final String ACCEPT = "Accept";
  protected static final String APP_JSON = "application/json";
  protected static final String CONTENT_TYPE = "Content-type";
  protected static final String APP_JSON_UTF_8 = "application/json; charset=UTF-8";

  private static final Logger LOG = LoggerFactory.getLogger(HttpUtils.class);
  private static final String AUTHORIZATION = "Authorization";

  protected final ExecutorService service = Executors.newFixedThreadPool(4);

  private HttpURLConnection client;
  private String apiKeyId;
  private String apiKeySecret;

  public HttpUtils() {
    client = createHTTPClient("https://a.blazemeter.com");
  }

  public HttpUtils(String apiKeyId, String apiKeySecret) {
    this.apiKeyId = apiKeyId;
    this.apiKeySecret = apiKeySecret;
    this.client = createHTTPSClient("https://a.blazemeter.com");
  }

  protected HttpsURLConnection createHTTPSClient(String initialUrl) {
    try {
      Proxy proxy = Proxy.NO_PROXY;
      Authenticator auth = null;
      String proxyHost = System.getProperty(PROXY_HOST);
      if (!StringUtils.isBlank(proxyHost)) {
        LOG.info("Using http.proxyHost = " + proxyHost);
        int proxyPort = getProxyPort();
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        auth = createAuthenticator();
      }

      URL url = new URL(modifyRequestUrl(initialUrl));
      HttpsURLConnection connection;

      if (proxy == Proxy.NO_PROXY) {
        connection = (HttpsURLConnection) url.openConnection();
      } else {
        connection = (HttpsURLConnection) url.openConnection(proxy);
      }

      connection.setConnectTimeout(180000);
      connection.setReadTimeout(60000);

      // Set proxy authenticator
      if (auth != null) {
        Authenticator.setDefault(auth);
      }

      return connection;
    } catch (Exception ex) {
      LOG.warn("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
      throw new RuntimeException("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
    }
  }

  /**
   * Create Get Request
   */
  public HttpsURLConnection createGet(String url) throws IOException {
    client.setRequestMethod("GET");
    HttpsURLConnection connection = openConnection(url);
    connection.setRequestMethod("GET");
    connection.setRequestProperty(ACCEPT, APP_JSON);
    connection.setRequestProperty(CONTENT_TYPE, APP_JSON_UTF_8);
    connection = addRequiredHeader(connection); // Add authorization header
    return connection;
  }



  /**
   * Create Post Request with json body
   */
  public HttpsURLConnection createPost(String url, String data) throws IOException {
    URL apiUrl = new URL(url);
    HttpsURLConnection connection = (HttpsURLConnection) apiUrl.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    String credentials = apiKeyId + ":" + apiKeySecret;
    String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
    connection.setDoOutput(true);
    connection.setDoInput(true);
    return connection;
  }


  public HttpsURLConnection createPost(String url, Object data) throws IOException {
    this.client.setRequestMethod("POST");
    HttpsURLConnection connection = openConnection(url);
    connection.setRequestMethod("POST");
    connection.setRequestProperty(ACCEPT, APP_JSON);
    connection.setRequestProperty(CONTENT_TYPE, APP_JSON_UTF_8);
    connection.setDoOutput(true);
    connection = addRequiredHeader(connection); // Add authorization header

    if (data instanceof String) {
      try (OutputStream outputStream = connection.getOutputStream()) {
        outputStream.write(((String) data).getBytes(StandardCharsets.UTF_8));
      }
    } else if (data instanceof File) {
      try (OutputStream outputStream = connection.getOutputStream();
           FileInputStream inputStream = new FileInputStream((File) data)) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    } else {
      throw new IllegalArgumentException("Unsupported request body type: " + data.getClass().getSimpleName());
    }

    return connection;
  }


  /**
   * Create Patch Request
   */
  public HttpsURLConnection createPatch(String url, String data) throws IOException {
    HttpsURLConnection connection = openConnection(url);
    connection.setRequestMethod("PATCH");
    connection.setRequestProperty(ACCEPT, APP_JSON);
    connection.setRequestProperty(CONTENT_TYPE, APP_JSON_UTF_8);
    connection.setDoOutput(true);
    try (OutputStream outputStream = connection.getOutputStream()) {
      outputStream.write(data.getBytes(StandardCharsets.UTF_8));
    }
    return connection;
  }

  /**
   * Execute Http request
   *
   * @param connection - HTTP Connection
   * @return - response in JSONObject
   */
  public JSONObject execute(HttpsURLConnection connection) throws IOException {
    String response = executeRequest(connection);
    if (response == null || response.isEmpty()) {
      LOG.warn("Response is empty");
      return null;
    }
    return processResponse(response);
  }

  public void saveResponse(JSONObject response, URL url) throws IOException {
    String saveResponsesString = System.getProperty("blazemeter.api.save_responses", "false");
    boolean saveResponses = Boolean.parseBoolean(saveResponsesString);
    if (!saveResponses) {
      return;
    }

    String storeFolder = System.getProperty("user.dir");
    String requestPath = cleanUrl(url);
    Path pathToFolder  = Paths.get(storeFolder, requestPath);
    Path pathToResponse = pathToFolder.resolve("response.json");

    try (FileWriter file = new FileWriter(pathToResponse.toString())) {
      file.write(response.toString(2));
    }
  }

  private String cleanUrl(URL url) {
    String base = url.getPath() + "/";
    if (url.getQuery() != null) {
      base += "?" + url.getQuery();
    }

    return base.replaceAll("%", "-")
        .replaceAll("\\?", "_")
        .replaceAll("&", "-")
        .replaceAll("=", "_");
  }

  protected JSONObject processResponse(String response) {
    return JSONObject.fromObject(response);
  }

  public String executeRequest(HttpsURLConnection connection) throws IOException {
    StringBuilder responseBuilder = new StringBuilder();
    try {
      connection.connect(); // Reset the connection
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          responseBuilder.append(line);
        }
      }
    } catch (IOException e) {

    } finally {
      connection.disconnect(); // Disconnect the connection after the request
    }
    return responseBuilder.toString();
  }


  protected String extractErrorMessage(String response) {
    return response;
  }

  private HttpsURLConnection openConnection(String url) throws IOException {
    URL connectionUrl = new URL(url);
    HttpsURLConnection connection = (HttpsURLConnection) connectionUrl.openConnection(getProxy());
    connection.setConnectTimeout(180000);
    connection.setReadTimeout(60000);
    connection.setInstanceFollowRedirects(true);
    return connection;
  }

  private Proxy getProxy() {
    Proxy proxy = Proxy.NO_PROXY;
    String proxyHost = System.getProperty(PROXY_HOST);
    if (!StringUtils.isBlank(proxyHost)) {
      LOG.info("Using http.proxyHost = " + proxyHost);
      int proxyPort = getProxyPort();
      proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }
    return proxy;
  }

  private int getProxyPort() {
    try {
      int proxyPort = Integer.parseInt(System.getProperty(PROXY_PORT));
      LOG.info("Using http.proxyPort = " + proxyPort);
      return proxyPort;
    } catch (NumberFormatException nfe) {
      LOG.warn("Failed to read http.proxyPort: ", nfe);
      return 8080;
    }
  }

  private TrustManager[] createTrustManager() {
    return new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
          }

          public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
          }
        }
    };
  }

  private void disableSSLValidation() {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, createTrustManager(), new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    } catch (Exception ex) {
      LOG.warn("Error disabling SSL validation: " + ex.getMessage(), ex);
    }
  }

  private Authenticator createAuthenticator() {
    final String proxyUser = System.getProperty(PROXY_USER);
    LOG.info("Using http.proxyUser = " + proxyUser);
    final String proxyPass = System.getProperty(PROXY_PASS);
    LOG.info("Using http.proxyPass = " + StringUtils.left(proxyPass, 4));
    if (!StringUtils.isBlank(proxyUser) && !StringUtils.isBlank(proxyPass)) {
      return new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
        }
      };
    }
    return null; // No authentication
  }


  public org.slf4j.Logger getLogger() {
    return LOG;
  }

  protected HttpURLConnection createHTTPClient(String initialUrl) {
    try {
      Proxy proxy = Proxy.NO_PROXY;
      Authenticator auth = null;
      String proxyHost = System.getProperty(PROXY_HOST);
      if (!StringUtils.isBlank(proxyHost)) {
        LOG.info("Using http.proxyHost = " + proxyHost);
        int proxyPort = getProxyPort();
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        auth = createAuthenticator();
      }

      URL url = new URL(modifyRequestUrl(initialUrl));
      HttpURLConnection connection;

      if (proxy == Proxy.NO_PROXY) {
        connection = (HttpURLConnection) url.openConnection();
      } else {
        connection = (HttpURLConnection) url.openConnection(proxy);
      }

      connection.setConnectTimeout(180000);
      connection.setReadTimeout(60000);

      // Set proxy authenticator
      if (auth != null) {
        Authenticator.setDefault(auth);
      }

      return connection;
    } catch (Exception ex) {
      LOG.warn("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
      throw new RuntimeException("ERROR Instantiating HTTPClient. Exception received: " + ex.getMessage(), ex);
    }
  }



  protected String modifyRequestUrl(String url) {
    return url;
  }

  private boolean isValidCredentials(String apiKeyId, String apiKeySecret) {
    return !StringUtils.isBlank(apiKeyId) && !StringUtils.isBlank(apiKeySecret);
  }

  protected HttpsURLConnection addRequiredHeader(HttpsURLConnection connection) {
    if (isValidCredentials(apiKeyId, apiKeySecret)) {
      String credentials = apiKeyId + ":" + apiKeySecret;
      String base64Credentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
      connection.setRequestProperty(AUTHORIZATION, "Basic " + base64Credentials);
    }
    return connection;
  }


}

