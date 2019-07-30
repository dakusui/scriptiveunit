package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.crest.Crest;
import com.github.dakusui.scriptiveunit.model.form.value.Value;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asObject;

abstract public class DocsApi<REQ extends DocsApi.Request, RESP extends DocsApi.Response<E>, E> {
  Value<RESP> service(Value<REQ> request) {
    return s -> performService(request.apply(s));
  }

  abstract RESP performService(REQ request);

  abstract <T> List<Checker<E, RESP, T>> checkers();

  <T> void verify(REQ request) {
    RESP resp = performService(request);
    List<Checker<E, RESP, T>> checkers = checkers();
    Crest.assertThat(
        resp,
        allOf(
            asObject(checkers.get(0)::transform).check(checkers.get(0)::verify).$()
        )
    );
  }

  public Value<Double> metric(Value<RESP> response, Value<Predicate<E>> cond) {
    return s -> (double) response.apply(s).docs().size();
  }

  interface Request {
  }

  interface Response<E> {
    List<E> docs();
  }

  interface Checker<E, RESP extends DocsApi.Response<E>, T> {
    T transform(RESP response);

    boolean verify(T value);
  }
}
