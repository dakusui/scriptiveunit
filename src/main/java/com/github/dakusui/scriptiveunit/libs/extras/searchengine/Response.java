package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;

public interface Response<DOC, REQ extends Request> {
  /**
   * A request to which this response was made.
   *
   * @return An original request.
   */
  REQ request();

  List<DOC> docs();
}
