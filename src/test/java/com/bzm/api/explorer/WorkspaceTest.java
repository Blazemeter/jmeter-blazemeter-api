package com.bzm.api.explorer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.bzm.BlazeMeterManager;
import com.bzm.api.utils.MockedBlazeMeterUtils;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WorkspaceTest extends BlazemeterTests {

  private static BlazeMeterManager instance;
  private static String apikeyFilename = "/Users/abstracta/projects/GitLab/bzm-repositories-api/bzm-repositories-api/src/test/resources/api-key.json";

  private MockedBlazeMeterUtils mockedUtils;
  @Before
  public void setUp() throws IOException {
    instance = BlazeMeterManager.getInstance();
    instance.initialize(apikeyFilename);
    utils = instance.getBlazeMeterUtils();
    mockedUtils = instance.getMockedBlazeMeterUtils();

//    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/user",
//        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/user/response.json");
//    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/accounts?sort%5B%5D=name&limit=1000",
//        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/accounts/_sort-5B-5D_name-limit_1000/response.json");
//    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=415717&enabled=true&limit=1000",
//        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_415717-enabled_true-limit_1000/response.json");
//    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=312843&enabled=true&limit=1000",
//        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_312843-enabled_true-limit_1000/response.json");
//    mockedUtils.addMockedResponse("https://a.blazemeter.com/api/v4/workspaces?accountId=314510&enabled=true&limit=1000",
//        "/Users/abstracta/projects/GitHub/BlazeMeter/blazemeter-api-client/src/test/resources/responses/api/v4/workspaces/_accountId_314510-enabled_true-limit_1000/response.json");
  }

  @After
  public void tearDown() {
    mockedUtils.printRequestsCount();
    mockedUtils.clearMockedResponses();
  }

  @Test
  public void shouldGetWorkspaces() throws IOException {
    List<Account> accounts = instance.getAccounts();
    for (Account account : accounts) {
      List<Workspace> workspaces = account.getWorkspaces();
      for (Workspace workspace : workspaces) {
        System.out.println("Workspace: " + workspace);
      }
    }

    mockedUtils.printRequestsCount();
  }

  @Test
  public void shouldGetWorkspacesForDefaultAccount() throws IOException {
    List<Workspace> workspaces = instance.getWorkspacesForDefaultUserAccount();
    for (Workspace workspace : workspaces) {
      System.out.println("Workspace: " + workspace);
    }
  }

  @Test
  public void shouldGetWorkspacesIds() {
    List<Long> workspacesIds = instance.getWorkspacesIds();
    System.out.println("Workspaces ids: " + workspacesIds);
    assertNotNull(workspacesIds);
  }

  //@Test
  public void testCreateProject() throws Exception {
    Workspace workspace = new Workspace(utils, "888", "workspace_name");
//    Workspace workspace = new Workspace(utils, "888", "workspace_name");
    Project project = workspace.createProject("NEW_PROJECT");
    assertEquals("999", project.getId());
    assertEquals("NEW_PROJECT", project.getName());
  }
}
