package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.Formattable;
import java.util.Formatter;
import java.util.List;

public interface Response<DOC, REQ extends Request> extends Formattable {
  /**
   * A request to which this response was made.
   *
   * @return An original request.
   */
  REQ request();

  List<DOC> docs();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    formatter.format("%s:{req:<%s>,docs:{size=<%s>,elements=<%s>}}",
        this.getClass().getSimpleName(),
        this.request(),
        docs().size(),
        docs());
  }
}
