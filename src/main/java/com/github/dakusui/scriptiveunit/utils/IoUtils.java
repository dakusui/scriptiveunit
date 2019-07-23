package com.github.dakusui.scriptiveunit.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException.wrapIfNecessary;
import static java.util.Objects.requireNonNull;

class IoUtils {
  static File materializeResource(@SuppressWarnings("SameParameterValue") String resourceName) {
    try {
      try (InputStream i = new BufferedInputStream(openResource(resourceName))) {
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

  private static InputStream openResource(String resourceName) {
    try {
      return requireNonNull(StringUtils.class.getClassLoader().getResource(resourceName)).openStream();
    } catch (IOException e) {
      throw wrapIfNecessary(e);
    }
  }
}
