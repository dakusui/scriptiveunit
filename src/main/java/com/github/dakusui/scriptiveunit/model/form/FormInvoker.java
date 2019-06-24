package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public interface FormInvoker {
  static FormInvoker create() {
    return new FormInvokerImpl();
  }

  String asString();

  class FormInvokerImpl implements FormInvoker {
    private final Writer writer;

    FormInvokerImpl() {
      this.writer = new Writer();
    }

    public String asString() {
      return this.writer.asString();
    }

    public static class Writer implements ActionPrinter.Writer, Iterable<String> {
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
  }
}
