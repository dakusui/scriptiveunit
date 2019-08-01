package com.github.dakusui.scriptiveunit.libs.extras;

import java.util.List;

public interface SearchEngine<REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<DOC>, DOC> {

  RESP service(REQ request);

  String idOf(DOC doc);

  interface Request {
  }

  interface Response<DOC> {
    boolean wasSuccessful();
    List<DOC> docs();
  }
}
