package com.bzm.api.explorer;

import com.bzm.api.utils.MockedBlazeMeterUtils;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class MockedBlazeMeterUtilsTest {

  private MockedBlazeMeterUtils utils;

  @Test
  public void shouldUpdateMockResponse() throws IOException {
    utils = new MockedBlazeMeterUtils("", "");
    utils.loadBlazeMeterMocks();

    String url = "https://ar.blazemeter.com/api/v1/workspaces/573391/assets?withSystem=true&withData=true&limit=100&skip=0&q=type=correlation-rule&q=metadata.application=wordpress&q=metadata.version=0.1";
    String keyToModify = "dataAccessible";
    boolean valueToModify = true;
    Object originalValue = utils.getValueFromMockedResponse(url, keyToModify);
    utils.modifyMockedResponse(url, keyToModify, valueToModify);
    Object finalValue = utils.getValueFromMockedResponse(url, keyToModify);
  }

}
