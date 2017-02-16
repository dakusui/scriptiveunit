package com.github.dakusui.scriptunit.model.func;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.scriptunit.core.Utils;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.scriptunit.core.Utils.convertIfNecessary;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public interface FuncInvoker {
  void enter();

  <T> T invokeConst(Object value);

  Object invokeMethod(Object target, Method method, Object[] args, String alias);

  void leave();

  String asString();

  class Impl implements FuncInvoker {
    private final Writer writer;
    private       int    indent;
    final Map<List<Object>, Object> memo = new HashMap<List<Object>, Object>() {
      @Override
      public Object computeIfAbsent(List<Object> key,
          Function<? super List<Object>, ?> mappingFunction) {
        if (containsKey(key)) {
          enter();
          try {
            writeLine("...");
          } finally {
            leave();
          }
          return get(key);
        }
        Object ret = mappingFunction.apply(key);
        put(key, ret);
        return ret;
      }
    };

    public Impl(int initialIndent) {
      this.indent = initialIndent;
      this.writer = new Writer();
    }

    @Override
    public void enter() {
      this.indent++;
    }

    @Override
    public <T> T invokeConst(Object value) {
      //noinspection unchecked
      return (T) value;
    }

    @Override
    public Object invokeMethod(Object target, Method method, Object[] args, String alias) {
      List<Object> key = asList(target, method, asList(args));
      boolean wasAbsent = !this.memo.containsKey(key);
      boolean targetIsMemoized = target instanceof Func.Memoized;
      Object ret = "(N/A)";
      try {
        this.writeLine("%s(", alias);
        if (targetIsMemoized) {
          ret = this.memo.computeIfAbsent(
              key,
              (List<Object> input) -> invokeMethod(target, method, args)
          );
        } else {
          ret = invokeMethod(target, method, args);
        }
        return Utils.toBigDecimalIfPossible(ret);
      } finally {
        if (!targetIsMemoized || wasAbsent)
          this.writeLine(") -> %s", ret);
        else
          this.writeLine(") -> (memoized)");
      }
    }

    private static Object invokeMethod(Object target, Method method, Object... args) {
      try {
        return method.invoke(target, stream(args).map(new Func<Object, Object>() {
          int i = 0;

          @Override
          public Object apply(Object input) {
            try {
              return convertIfNecessary(input, method.getParameterTypes()[i]);
            } finally {
              i++;
            }
          }
        }).collect(toList()).toArray());
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw wrap(e);
      }
    }

    @Override
    public void leave() {
      this.indent--;
    }

    public String asString() {
      return this.writer.asString();
    }

    void writeLine(String format, Object... args) {
      String s = format(format, prettify(args));
      if (s.contains("\n")) {
        enter();
        try {
          stream(s.split("\\n")).forEach((String in) -> writer.writeLine(indent(this.indent) + in));
        } finally {
          leave();
        }
      } else {
        writer.writeLine(indent(this.indent) + s);
      }
    }

    private String indent(int indent) {
      String ret = "";
      for (int i = 0; i < indent; i++) {
        ret += indent();
      }
      return ret;
    }

    private String indent() {
      return "  ";
    }

    private static Object[] prettify(Object... args) {
      return Arrays.stream(args).map((Object in) -> in instanceof Iterable ? Utils.iterableToString(((Iterable) in)) : in).collect(toList()).toArray();
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
