package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.actionunit.visitors.ActionPrinter;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public class FormInvokerImpl implements FormInvoker {
  private final Writer writer;

  private FormInvokerImpl() {
    this.writer = new Writer();
  }

  public static FormInvoker create() {
    return new FormInvokerImpl();
  }

  public String asString() {
    return this.writer.asString();
  }

  public static class Writer implements ActionPrinter.Writer, Iterable<String> {
    List<String> output = Lists.newArrayList();

    public Writer() {
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
