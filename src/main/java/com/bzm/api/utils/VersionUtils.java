package com.bzm.api.utils;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionUtils {

  public static int getNextMinor(List<String> versions) {
    String latestVersion = getLatestVersion(versions);
    DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(latestVersion);
    return artifactVersion.getIncrementalVersion();
  }

  public static String getLatestVersion(List<String> versions) {
    if (versions.isEmpty()) {
      return "0.0";
    }

    List<DefaultArtifactVersion> artifactVersions = new ArrayList<>();

    for (String version : versions) {
      artifactVersions.add(new DefaultArtifactVersion(version));
    }

    Collections.sort(artifactVersions);

    DefaultArtifactVersion latestArtifactVersion = artifactVersions.get(artifactVersions.size() - 1);
    return latestArtifactVersion.toString();
  }

  public static String getNextProposedMinor(List<String> versions) {
    String latestVersion = getLatestVersion(versions);
    DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(latestVersion);
    int major = artifactVersion.getMajorVersion();
    int minor = artifactVersion.getMinorVersion() + 1;
    return major + "." + minor;
  }
}

