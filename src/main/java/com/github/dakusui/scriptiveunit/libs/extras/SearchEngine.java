package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

abstract public class SearchEngine<REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<E>, E> {
  Value<RESP> service(Value<REQ> request) {
    return s -> performService(request.apply(s));
  }

  abstract RESP performService(REQ request);

  abstract <T> List<Checker<E, RESP, T>> checkers();

  private final Predicates predicates = new Predicates();

  Value<Boolean> verify(Value<RESP> request) {
    return predicates.and(ValueList.create(checks(request)));
  }

  private Checker.DocsMetric<E, RESP> precision(Predicate<E> documentOracle, Predicate<Double> criterion) {
    return new Checker.DocsMetric<E, RESP>() {
      Metric.Precision<E> metric = documentOracle::test;

      @Override
      public Double transform(RESP response) {
        return metric.calc(response.docs());
      }

      @Override
      public boolean verify(Double value) {
        return criterion.test(value);
      }
    };
  }

  Predicate<E> knownToBeRelevant(Function<E, String> id, String... ids) {
    Set<String> idsOfKnownToBeRelevant = new HashSet<>(asList(ids));
    return entry -> idsOfKnownToBeRelevant.contains(id.apply(entry));
  }

  Predicate<E> knownToBeIrrelevant(Function<E, String> id, String... ids) {
    Set<String> idsOfKnownToBeRelevant = new HashSet<>(asList(ids));
    return entry -> !idsOfKnownToBeRelevant.contains(id.apply(entry));
  }

  protected List<Value<Boolean>> checks(Value<RESP> request) {
    return Collections.emptyList();
  }


  interface Request {
  }

  interface Response<E> {
    List<E> docs();
  }

  interface Checker<E, RESP extends SearchEngine.Response<E>, T> {
    T transform(RESP response);

    boolean verify(T value);

    interface Docs<E, RESP extends SearchEngine.Response<E>> extends Checker<E, RESP, List<E>> {

    }

    interface DocsMetric<E, RESP extends SearchEngine.Response<E>> extends Checker<E, RESP, Double> {

    }
  }
}
