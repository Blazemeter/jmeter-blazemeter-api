package com.bzm.api.utils;

import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {

  public static void saveResponseToFile(String destFolder, String fileName, String content) throws IOException {
    save(destFolder, fileName, content);
  }

  public static void save(String destFolder, String fileName, String content) throws IOException {
    String json = content;

    File folder = new File(destFolder);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    File file = new File(folder, fileName);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(json);
    }
  }

  public static JSONObject getJsonFromFilepath(String filepath) throws IOException {
    File file = new File(filepath);
    String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
    return JSONObject.fromObject(content);
  }
}

