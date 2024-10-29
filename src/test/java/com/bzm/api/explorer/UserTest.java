
package com.bzm.api.explorer;

import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class UserTest extends BlazemeterTests {
  @Test
  public void testGetUser() throws Exception {
    User user = User.getUser(utils);
    System.out.println(user);
    Assert.assertEquals("540658", user.getId());
    assertEquals(EMAIL, user.getName());
  }

  @Test
  public void testGetAccounts() throws Exception {
    User user = new User(utils);
    List<Account> accounts = user.getAccounts();
    assertEquals(1, accounts.size());
    for (Account account : accounts) {
      assertEquals(ID, account.getId());
      assertEquals(NAME, account.getName());
    }
  }

  @Test
  public void testFromJSON() throws Exception {
    JSONObject object = new JSONObject();
    object.put("id", ID);
    object.put("email", EMAIL);

    User user = User.fromJSON(utils, object);
    assertEquals(ID, user.getId());
    assertEquals(EMAIL, user.getName());
  }
}