package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.utils.CoreUtils.toBigDecimalIfPossible;
import static java.lang.String.format;
import static java.util.Arrays.stream;

public interface FormInvoker {
  <T> T invokeConst(Object value);

  Object invokeForm(Form target, Stage stage, String alias);

  String asString();

  interface Memo extends Map<List<Object>, Object> {
    class Impl extends HashMap<List<Object>, Object> implements Memo {
      @Override
      public Object computeIfAbsent(List<Object> key,
                                    Function<? super List<Object>, ?> mappingFunction) {
        Object ret = mappingFunction.apply(key);
        put(key, ret);
        return ret;
      }
    }
  }

  static Memo createMemo() {
    return new Memo.Impl();
  }

  static FormInvoker create() {
    return new Impl(0);
  }

  class Impl implements FormInvoker {
    private final Writer writer;
    private int indent;

    private Impl(int initialIndent) {
      this.indent = initialIndent;
      this.writer = new Writer();
    }

    void enter() {
      this.indent++;
    }

    @Override
    public <T> T invokeConst(Object value) {
      this.enter();
      try {
        this.writeLine("%s(const)", value);
        //noinspection unchecked
        return (T) value;
      } finally {
        this.leave();
      }
    }

    @Override
    public Object invokeForm(Form target, Stage stage, String alias) {
      Object ret = "(N/A)";
      this.enter();
      try {
        this.writeLine("%s(", alias);
        return toBigDecimalIfPossible(target.apply(stage));
      } finally {
        this.writeLine(") -> %s", ret);
        this.leave();
      }
    }

    public String asString() {
      return this.writer.asString();
    }

    void leave() {
      --this.indent;
    }

    void writeLine(String format, Object... args) {
      String s = format(format, prettify(args));
      if (s.contains("\n")) {
        stream(s.split("\\n")).forEach((String in) -> writer.writeLine(indent(this.indent) + in));
      } else {
        writer.writeLine(indent(this.indent) + s);
      }
    }

    private String indent(int indent) {
      StringBuilder ret = new StringBuilder();
      for (int i = 0; i < indent; i++) {
        ret.append(indent());
      }
      return ret.toString();
    }

    private String indent() {
      return "  ";
    }

    private static Object[] prettify(Object... args) {
      return Arrays.stream(args).map((Object in) -> in instanceof Iterable ? com.github.dakusui.scriptiveunit.utils.StringUtils.iterableToString(((Iterable) in)) : in).toArray();
    }
  }

  class Writer implements ActionPrinter.Writer, Iterable<String> {
    List<String> output = Lists.newArrayList();

    Writer() {
    }

    @Override
    public void writeLine(String s) {
      this.output.add(s);
    }

    String asString() {
      return StringUtils.join("\n", this.output.toArray());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<String> iterator() {
      return this.output.iterator();
    }
  }

  enum Utils {
    ;

    public static Object invokeForm(Form target, Stage stage, String alias) {
      return toBigDecimalIfPossible(target.apply(stage));
    }

    public static <T> T invokeConst(Object value) {
      return (T) value;
    }
  }
}
