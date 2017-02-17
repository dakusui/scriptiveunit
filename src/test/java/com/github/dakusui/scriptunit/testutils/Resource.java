package com.github.dakusui.scriptunit.testutils;

import com.github.dakusui.scriptunit.exceptions.ScriptiveUnitException;

import java.io.IOException;
import java.io.InputStream;

public interface Resource<T> {
  String getName();

  T get();

  abstract class Base<T> implements Resource<T> {

    private final String name;

    public Base(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public T get() {
      try (InputStream is = open(name)) {
        return readObjectFromStream(is);
      } catch (IOException e) {
        throw ScriptiveUnitException.wrap(e);
      }
    }

    abstract protected T readObjectFromStream(InputStream is);

    protected InputStream open(String name) {
      return ClassLoader.getSystemResourceAsStream(name);
    }

    @Override
    public String toString() {
      return this.name + ":" + readObjectFromStream(open(this.name));
    }
  }
}
