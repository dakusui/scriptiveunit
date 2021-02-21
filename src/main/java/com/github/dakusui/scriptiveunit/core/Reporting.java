package com.github.dakusui.scriptiveunit.core;

import java.io.File;

public class Reporting {
  public final String reportFileName;
  public final File   reportBaseDirectory;

  Reporting(String reportFileName, File reportBaseDirectory) {
    this.reportFileName = reportFileName;
    this.reportBaseDirectory = reportBaseDirectory;
  }

  public static Reporting create() {
    return new Reporting("report.json", new File("."));
  }
}
