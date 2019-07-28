package com.github.dakusui.scriptiveunit.utils;

import java.io.*;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;

public class IoUtils {
  public static InputStream openFile(File file) {
    try {
      return new BufferedInputStream(new FileInputStream(file));
    } catch (IOException e) {
      throw wrapIfNecessary(e);
    }
  }

  public static File currentWorkingDirectory() {
    return new File(System.getProperty("user.dir"));
  }

  static File materializeResource(@SuppressWarnings("SameParameterValue") String resourceName) {
    try {
      try (InputStream i = new BufferedInputStream(ReflectionUtils.openResourceAsStream(resourceName))) {
        return writeToTempFile(i);
      }
    } catch (IOException e) {
      throw wrapIfNecessary(e);
    }
  }

  private static void writeTo(File out, InputStream i) {
    try {
      byte[] buf = new byte[4096];
      try (OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(out))) {
        int len;
        while ((len = i.read(buf, 0, buf.length)) > 0) {
          fileOutputStream.write(buf, 0, len);
        }
      }
    } catch (IOException e) {
      throw wrapIfNecessary(e);
    }
  }

  private static File writeToTempFile(InputStream i) {
    try {
      File ret = File.createTempFile("scriptiveunit", "tmp");
      ret.deleteOnExit();
      writeTo(ret, i);
      return ret;
    } catch (IOException e) {
      throw wrapIfNecessary(e);
    }
  }
}
