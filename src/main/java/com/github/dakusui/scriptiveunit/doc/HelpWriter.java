package com.github.dakusui.scriptiveunit.doc;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.scriptiveunit.core.Config;

import static com.github.dakusui.scriptiveunit.core.Utils.indent;
import static java.lang.String.format;

public class HelpWriter {
  public static void help(Class<?> testClass, ActionPrinter.Writer stdout, ActionPrinter.Writer stderr, String... args) {
    boolean succeeded = false;
    try {
      if (args.length == 0)
        help(testClass, stdout);
      else if (args.length == 1)
        help(testClass, stdout, Documentation.EntryType.valueOf(args[0]));
      else if (args.length == 2)
        help(testClass, stdout, Documentation.EntryType.valueOf(args[0]), args[1]);
      else
        throw new IllegalArgumentException("Too many arguments");
      succeeded = true;
    } finally {
      if (!succeeded)
        help(testClass, stderr);
    }
  }

  public static void help(Class<?> testClass, String... args) {
    help(testClass, ActionPrinter.Writer.Std.OUT, ActionPrinter.Writer.Std.ERR, args);
  }

  public static void help(Class<?> testClass, ActionPrinter.Writer writer) {
    String className = testClass.getCanonicalName();
    String scriptSystemPropertyKey = new Config.Builder(testClass, System.getProperties()).build().getScriptResourceNameKey();
    writer.writeLine(format(
        "This is a test class " + className + ".%n"
            + "You can run this as a JUnit test class from your IDE, build tool, etc.%n%n"
            + " Use -D" + scriptSystemPropertyKey + "={path to your script on your class path}"
            + " to specify script to be run.%n%n"
            + "By running this as an application, you can see this message and other helps."
            + " To list all the available scripts on your classpath, run%n%n"
            + "    " + className + " help " + Documentation.EntryType.SCRIPT + "%n%n"
            + " To list all the available functions that can be used in your scripts, run%n%n"
            + "    " + className + " help " + Documentation.EntryType.FUNCTION + "%n%n"
            + " To list all the available runners by which you can run your scripts, run%n%n"
            + "    " + className + " help " + Documentation.EntryType.RUNNER + "%n%n"
            + " To describe, a script, a function, or a runner, run %n%n"
            + "    " + className + " help " + Documentation.EntryType.SCRIPT + " {script name}%n"
            + "    " + className + " help " + Documentation.EntryType.FUNCTION + " {function name}%n"
            + "    " + className + " help " + Documentation.EntryType.RUNNER + " {runner name}%n"
            + "%n"));
  }

  public static void help(Class<?> testClass, ActionPrinter.Writer writer, Documentation.EntryType type) {
    Documentation.create(testClass, type)
        .list()
        .forEach(writer::writeLine);
  }

  public static void help(Class<?> testClass, ActionPrinter.Writer writer, Documentation.EntryType type, String name) {
    Description description = Documentation.create(testClass, type).describe(name);
    writeDescription(writer, 0, description);
  }

  public static void writeDescription(ActionPrinter.Writer writer, int level, Description description) {
    StringBuilder b = new StringBuilder();
    b.append(format("%s: ", description.name()));
    description.content().forEach(b::append);
    writer.writeLine(indent(level) + b.toString());
    description.children().forEach(input -> writeDescription(writer, level + 1, input));
  }
}
