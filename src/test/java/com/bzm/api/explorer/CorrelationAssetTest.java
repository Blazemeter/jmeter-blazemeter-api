package com.bzm.api.explorer;

import com.bzm.BlazeMeterManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CorrelationAssetTest extends BlazemeterTests {
  @Before
  public void setUp() throws IOException {
    instance = BlazeMeterManager.getInstance();
    instance.initialize(apikeyFilename);
    utils = instance.getBlazeMeterUtils();
    mockedUtils = instance.getMockedBlazeMeterUtils();

    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/user",
        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/user/response.json");
    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/accounts?sort%5B%5D=name&limit=1000",
        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/accounts/_sort-5B-5D_name-limit_1000/response.json");
    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=415717&enabled=true&limit=1000",
        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_415717-enabled_true-limit_1000/response.json");
    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=312843&enabled=true&limit=1000",
        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_312843-enabled_true-limit_1000/response.json");
    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=314510&enabled=true&limit=1000",
        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_314510-enabled_true-limit_1000/response.json");
  }

  @Test
  public void shouldGetBlazeMeterCatalogWithoutData() throws IOException {
    List<CorrelationAsset> workspaceCatalog = instance.getSystemCatalogWithoutData(WORKSPACE_ID);
    System.out.println("Found " + workspaceCatalog.size() + " assets");
    for (CorrelationAsset asset : workspaceCatalog) {
      System.out.println(" Asset: " + asset);
    }
  }

  @Test
  public void shouldGetBlazeMeterCatalogWithData() throws IOException {
    List<CorrelationAsset> workspaceCatalog = instance.getBlazemeterCatalogWithData(WORKSPACE_ID);
    System.out.println("Found " + workspaceCatalog.size() + " assets");
  }
}