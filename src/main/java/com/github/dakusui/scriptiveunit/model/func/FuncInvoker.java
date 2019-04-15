package com.github.dakusui.scriptiveunit.model.func;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.scriptiveunit.core.Utils;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.scriptiveunit.core.Utils.toBigDecimalIfPossible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public interface FuncInvoker {
  <T> T invokeConst(Object value);

  Object invokeFunc(Func target, Stage stage, String alias);

  String asString();

  static Map<List<Object>, Object> createMemo() {
    return new HashMap<List<Object>, Object>() {
      @Override
      public Object computeIfAbsent(List<Object> key,
          Function<? super List<Object>, ?> mappingFunction) {
        Object ret = mappingFunction.apply(key);
        put(key, ret);
        return ret;
      }
    };
  }

  static FuncInvoker create(Map<List<Object>, Object> memo) {
    return new Impl(0, memo);
  }

  static FuncInvoker create() {
    return create(FuncInvoker.createMemo());
  }

  class Impl implements FuncInvoker {
    private final Writer                    writer;
    private       int                       indent;
    final         Map<List<Object>, Object> memo;

    private Impl(int initialIndent, Map<List<Object>, Object> memo) {
      this.indent = initialIndent;
      this.writer = new Writer();
      this.memo = memo;
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
    public Object invokeFunc(Func target, Stage stage, String alias) {
      List<Object> key = asList(target, stage);
      boolean wasAbsent = !this.memo.containsKey(key);
      boolean targetIsMemoized = target instanceof Func.Memoized;
      Object ret = "(N/A)";
      this.enter();
      try {
        this.writeLine("%s(", alias);
        if (targetIsMemoized) {
          ret = computeIfAbsent(target, stage, key);
        } else {
          ret = target.apply(stage);
        }
        return toBigDecimalIfPossible(ret);
      } finally {
        if (!targetIsMemoized || wasAbsent)
          this.writeLine(") -> %s", ret);
        else
          this.writeLine(") -> (memoized)");
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

    private Object computeIfAbsent(Func target, Stage stage, List<Object> key) {
      Object ret;
      if (this.memo.containsKey(key)) {
        enter();
        try {
          writeLine("...");
        } finally {
          leave();
        }
        return this.memo.get(key);
      }
      ret = this.memo.computeIfAbsent(
          key,
          (List<Object> input) -> target.apply(stage)
      );
      return ret;
    }

    private static Object[] prettify(Object... args) {
      return Arrays.stream(args).map((Object in) -> in instanceof Iterable ? Utils.iterableToString(((Iterable) in)) : in).toArray();
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

    @Override
    public Iterator<String> iterator() {
      return this.output.iterator();
    }
  }

}
