package com.bzm.api.explorer;

import com.bzm.BlazeMeterManager;
import com.bzm.api.utils.BlazeMeterUtils;
import com.bzm.api.utils.MockedBlazeMeterUtils;

public class BlazemeterTests {
  protected static final String EMAIL = "ricardopoleo@gmail.com";
  protected static final String ID = "314510";
  protected static final String NAME = "RP Default Account";

  protected static String WORKSPACE_ID = "307677";

  protected static String apikeyFilename = "/Users/abstracta/projects/GitLab/bzm-repositories-api/bzm-repositories-api/src/test/resources/api-key.json";
  protected BlazeMeterUtils utils;

  protected static BlazeMeterManager instance;

  protected MockedBlazeMeterUtils mockedUtils;

}
