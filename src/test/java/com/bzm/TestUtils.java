package com.bzm;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestUtils {
  ///api/v4/user
  public static String getFileContent(String filePath, Class<?> testClass) throws IOException {
    return Resources.toString(testClass.getResource(filePath), Charset.defaultCharset());
  }
}
