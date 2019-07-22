package com.github.dakusui.scriptiveunit.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.scriptiveunit.utils.StringUtils.indent;

public interface Description {
  static Description describe(String name, List<Object> body) {
    return new Description() {
      @Override
      public String name() {
        return name;
      }

      @Override
      public List<String> content() {
        List<String> ret = new LinkedList<>();
        format(0, ret, body);
        return ret;
      }

      @SuppressWarnings("unchecked")
      void format(int indentLevel, List<String> out, List<Object> body) {
        if (body.isEmpty()) {
          out.add(indent(indentLevel) + "()");
          return;
        }
        if (body.size() == 1) {
          out.add(indent(indentLevel) + String.format("(%s)", body.get(0)));
          return;
        }
        out.add(indent(indentLevel) + "(" + body.get(0));
        for (Object each : body.subList(1, body.size())) {
          if (each instanceof List) {
            format(indentLevel + 1, out, (List<Object>) each);
          } else {
            out.add(indent(indentLevel + 1) + each);
          }
        }
        out.add(indent(indentLevel) + ")");
      }

      @Override
      public String toString() {
        return name;
      }
    };
  }

  String name();

  List<String> content();

  default List<Description> children() {
    return Collections.emptyList();
  }
}
