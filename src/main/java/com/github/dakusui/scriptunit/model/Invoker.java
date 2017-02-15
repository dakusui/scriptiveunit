package com.github.dakusui.scriptunit.model;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.scriptunit.core.ObjectMethod;
import com.github.dakusui.scriptunit.core.Utils;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.scriptunit.core.Utils.convertIfNecessary;
import static com.github.dakusui.scriptunit.core.Utils.toBigDecimal;
import static com.github.dakusui.scriptunit.exceptions.ScriptUnitException.wrap;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.asList;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class Invoker {
  private final Writer writer;
  private       int    indent;
  private final Map<List<Object>, Object> memo = new HashMap<List<Object>, Object>() {
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

  public Invoker(int initialIndent) {
    checkArgument(initialIndent >= 0);
    this.indent = initialIndent;
    this.writer = new Writer();
  }

  public Object invoke(ObjectMethod method, Object[] args) {
    return createProxyIfNecessary(method.getName(), method.invoke(args));
  }

  public <T> Object createConst(T value) {
    return createProxy((proxy, method, args) -> {
      enter();
      try {
        writeLine(format("'%s' (const)", value));
        return value;
      } finally {
        leave();
      }
    }, Func.Const.class);
  }

  public String asString() {
    return this.writer.asString();
  }

  public void reset() {
    this.memo.clear();
    this.writer.reset();
  }

  private Object createProxyIfNecessary(String name, Object target) {
    if (target instanceof Func) {
      return createProxy((Object proxy, Method method, Object[] args) -> {
        if (!"apply".equals(method.getName())) {
          return method.invoke(target, args);
        }
        Object ret = "N/A";
        enter();
        List<Object> key = asList(method, target, args);
        boolean wasAbsent = !memo.containsKey(key);
        boolean targetIsMemoized = target instanceof Func.Memoized;
        try {
          writeLine("%s(", name);
          if (targetIsMemoized) {
            ret = memo.computeIfAbsent(
                key,
                (List<Object> input) -> invokeMethod(input.get(1), (Method) input.get(0), input.get(2))
            );
          } else {
            ret = invokeMethod(target, method, args);
          }
          return Utils.toBigDecimalIfPossible(ret);
        } finally {
          if (!targetIsMemoized || wasAbsent)
            writeLine(") -> %s", ret);
          else
            writeLine(") -> (memoized)");
          leave();
        }
      }, Func.class);
    }
    return toBigDecimal((Number) target);
  }

  private Object invokeMethod(Object target, Method method, Object... args) {
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

  private <I, O> Func<I, O> createProxy(InvocationHandler handler, Class<? extends Func> interfaceClass) {
    //noinspection unchecked
    return (Func<I, O>) Proxy.newProxyInstance(
        Func.class.getClassLoader(),
        new Class[] { interfaceClass },
        handler
    );
  }

  private void enter() {
    this.indent++;
  }

  private void leave() {
    this.indent--;
  }

  private void writeLine(String format, Object... args) {
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

  static class Writer implements ActionPrinter.Writer, Iterable<String> {
    List<String> output = Lists.newArrayList();

    @Override
    public void writeLine(String s) {
      this.output.add(s);
    }

    String asString() {
      return StringUtils.join("\n", this.output.toArray());
    }

    void reset() {
      this.output.clear();
    }

    @Override
    public Iterator<String> iterator() {
      return this.output.iterator();
    }
  }
}
