package com.bzm.api.explorer;

import org.junit.Test;

import java.util.List;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

public class AccountTest extends BlazemeterTests {
  @Test
  public void shouldGetDefaultAccount() throws Exception {
    User user = User.getUser(utils);
    List<Account> accounts = user.getAccounts();
    assertEquals(1, accounts.size());
    Account account = accounts.get(0);
    System.out.println(account.getId() + " '" + account.getName() + "'");
  }

  @Test
  public void shouldGetWorkspaces() throws Exception {
    User user = User.getUser(utils);
    List<Account> accounts = user.getAccounts();
    assertEquals(1, accounts.size());
    Account account = accounts.get(0);
    List<Workspace> workspaces = account.getWorkspaces();
    assertEquals(1, workspaces.size());
    Workspace workspace = workspaces.get(0);
    System.out.println(workspace.getId() + " '" + workspace.getName() + "'");
  }
}
