package com.bzm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertNotNull;
import com.bzm.api.explorer.Account;
import com.bzm.api.explorer.CorrelationAsset;
import com.bzm.api.explorer.User;
import com.bzm.api.explorer.Workspace;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlazeMeterManagerTest {

  private static BlazeMeterManager instance;
  private static String WORKSPACE_ID = "307677";
  private static String apikeyFilename = "";
  private WireMockServer wireMockServer;
  private final String LOCALHOST = "localhost";

  @Before
  public void setUp() throws IOException {
    WireMockConfiguration configuration = wireMockConfig()
        .dynamicPort()
        .dynamicHttpsPort()
        .notifier(new ConsoleNotifier(true));

    wireMockServer = new WireMockServer(configuration);
//    wireMockServer.start();

    // Configure WireMock to use Logback for logging
//    WireMock.configureFor("localhost", wireMockServer.port());

    instance = BlazeMeterManager.getInstance();
    instance.initialize(apikeyFilename);
    instance.getMockedBlazeMeterUtils().loadBlazeMeterMocks();
  }

  @After
  public void tearDown() {
    instance.getMockedBlazeMeterUtils().printRequestsCount();
    instance.getMockedBlazeMeterUtils().clearMockedResponses();
    System.out.println("tearDown");
    if (wireMockServer.isRunning()) {
      System.out.println(" wireMockServer is running");
      List<ServeEvent> allServeEvents = wireMockServer.getAllServeEvents();
      for (ServeEvent serveEvent : allServeEvents) {
        System.out.println(serveEvent.getRequest().getUrl());
      }
      wireMockServer.stop();
    }
  }

  private void changeUtilsAddress() {
    String baseUrl = wireMockServer.baseUrl();
    instance.getBlazeMeterUtils().setAddress(baseUrl);
    instance.getBlazeMeterUtils().setDataAddress(baseUrl + "/data");
  }

  @Test
  public void shouldReturnTrueIfConfigured() {
    BlazeMeterManager instance = BlazeMeterManager.getInstance();
    instance.setApiId("");
    instance.setApiSecret("");
    TestCase.assertFalse(instance.isConfigured());
  }

  @Test
  public void shouldReturnFalseIfNotConfigured() {
    BlazeMeterManager instance = BlazeMeterManager.getInstance();
    TestCase.assertTrue(instance.isConfigured());
  }

  @Test
  public void shouldReturnTrueIfValid() {
    BlazeMeterManager instance = BlazeMeterManager.getInstance();
    TestCase.assertTrue(instance.isValid());
  }

  @Test
  public void shouldGetUser() throws IOException {
//    mockGetUser();
//    changeUtilsAddress();
    User user = instance.getUser();
    String userString = user.toString();
    System.out.println(JSONObject.fromObject(userString).toString(2));
    assertNotNull(user);
  }

  @Test
  public void shouldRemain20SecsWaitingForRequests () throws IOException, InterruptedException {
    mockGetUser();
    System.out.println("WireMockServer port: " + wireMockServer.baseUrl());
    // Now we should wait 20 seconds for the requests to be finished
    Thread.sleep(20000);
  }

  /**
   * This method mocks the response of the API call to get the user's information.
   * The response is a JSON file located in the resources folder, called user.json
   * @throws IOException
   */
  private void mockGetUser() throws IOException {
    prepareURL("/backupResponses/v4/user");
  }

  private void prepareURL(String URL) throws IOException {
    System.out.println("Mocking URL: " + URL);
    String fileContent = TestUtils.getFileContent(URL + ".json", getClass());
    wireMockServer.stubFor(get(urlEqualTo(URL))
        .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Cache-Control", "no-cache")
        .withHeader("Content-Type", "application/json")
        .withBody(fileContent)
    ));
  }

  @Test
  public void shouldGetAccounts() throws IOException {
    List<Account> accounts = instance.getAccounts();
    // Loop and print the account toString
    for (Account account : accounts) {
      System.out.println("Account: " + account);
    }
  }

  @Test
  public void shouldGetSystemAssetsForProtocolsAsString() throws IOException {
    List<Pair<String, String>> protocols = new java.util.ArrayList<>();
    protocols.add(Pair.of("wordpress", "0.1"));
    List<CorrelationAsset> workspaceCatalog = instance.getBlazeMeterVersions(WORKSPACE_ID, protocols);
    System.out.println("Found " + workspaceCatalog.size() + " assets");
    CorrelationAsset asset = workspaceCatalog.get(0);
    String fileAsString = asset.getCorrelationRulesFileAsString();
    assertNotNull(fileAsString);
    JSONObject jsonObject = JSONObject.fromObject(fileAsString);
    System.out.println("JSON: " + jsonObject.toString(2));
  }

  @Test
  public void shouldGetWorkspaceAssets() throws IOException {
    List<CorrelationAsset> workspaceCatalog = instance.getWorkspaceCatalogWithData(WORKSPACE_ID);
    System.out.println("Found " + workspaceCatalog.size() + " assets");
  }

  @Test
  public void shouldGetWorkspaceAssetsWithTheFile() throws IOException {
    List<CorrelationAsset> workspaceCatalog = instance.getBlazemeterCatalogWithData(WORKSPACE_ID);
    System.out.println("Found " + workspaceCatalog.size() + " assets");
    System.out.println(workspaceCatalog);
  }

  @Test
  public void shouldGetAssetFile() throws IOException {
    String assetId = "a6698428-7f48-4e84-87b4-e18844e689de";

    CorrelationAsset asset = new CorrelationAsset(instance.getBlazeMeterUtils());
    asset.setId(assetId);
    asset.setWorkspaceId(WORKSPACE_ID);

    File file = asset.getFile(assetId);
    //Get the String of that file
    String fileContent = new String(Files.readAllBytes(file.toPath()));
    System.out.println("File content: '" + fileContent + "'");
  }

  /**
   * Receive the path to a file, get the String content, replace "AAA" with a random number, and returns the final File.
   */
  private File getModifiedFile(File file, String random, String filename) throws IOException {
    String fileContent = new String(Files.readAllBytes(file.toPath()));
    fileContent = fileContent.replace("AAA", random);
    File tempFile = File.createTempFile(filename, ".json");
    Files.write(tempFile.toPath(), fileContent.getBytes());
    return tempFile;
  }


  @Test
  public void should() throws IOException {
    User user = instance.getUser();
    Account defaultAccount = user.getDefaultAccount();
    List<Account> accounts = user.getAccounts();
    System.out.println("Default account: " + defaultAccount);
    System.out.println("Total Accounts: " + accounts.size());

    List<Account> sharedAccounts = new ArrayList<>();
    for (Account account : accounts) {
      if (account.getId().equals(defaultAccount.getId())) {
        continue;
      }
      sharedAccounts.add(account);
    }

    System.out.println("Shared accounts: " + sharedAccounts.size());
    for (Account account : sharedAccounts) {
      System.out.println("Account '" + account.getName() + "' with id " + account.getId());
      List<Workspace> workspaces = account.getWorkspaces();
      System.out.println(" Workspaces: " + workspaces.size());
      for (Workspace workspace : workspaces) {
        List<CorrelationAsset> workspaceCatalog = instance.getWorkspaceCatalogWithData(workspace.getId());
        System.out.println(" Workspace '" + workspace.getName() + "' with " + workspaceCatalog.size() + " workspace assets");
        for (CorrelationAsset asset : workspaceCatalog) {
          System.out.println("  Asset '" + asset.getName() + "' with id " + asset.getVersion() + ". wsId=" + asset.getWorkspaceId() + ". Id= " + asset.getId());
        }
      }
    }
  }

  @Test
  public void shouldGetSharedWorkspaces() throws IOException {
    List<Workspace> sharedWorkspaces = instance.getSharedWorkspaces();
    System.out.println("Shared workspaces: " + sharedWorkspaces.size());
    for (Workspace workspace : sharedWorkspaces) {
      System.out.println("Workspace '" + workspace.getName() + "' with id " + workspace.getId());
    }
  }

  @Test
  public void shouldGetSharedWorkspacesIds() throws IOException {
    List<Workspace> sharedWorkspaces = instance.getSharedWorkspaces();
    System.out.println("Shared workspaces: " + sharedWorkspaces.size());
    for (Workspace workspace : sharedWorkspaces) {
      System.out.println("Workspace '" + workspace.getName() + "' with id " + workspace.getId());
    }
  }
}
