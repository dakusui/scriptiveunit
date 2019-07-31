package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

abstract public class SearchEngine<REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<DOC>, DOC> {
  private final Predicates predicates = new Predicates();

  @Scriptable
  Value<RESP> issueRequest(Value<REQ> request) {
    return s -> service(request.apply(s));
  }

  @Scriptable
  public Value<Boolean> verifyResponseWith(Value<RESP> request, ValueList<Checker<DOC, RESP, ?>> checkers) {
    return predicates.and(ValueList.create(checks(request)));
  }

  protected abstract RESP service(REQ request);

  protected abstract String idOf(DOC doc);

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionCheckerByKnownRelevantDocIds(List<String> relevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    Set<String> relevantDocIdSet = new HashSet<>(requireNonNull(relevantDocIds));
    return precisionChecker((DOC doc) -> relevantDocIdSet.contains(id.apply(doc)), criterion);
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionCheckerByKnownIrrelevantDocIds(List<String> irrelevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    Set<String> irrelevantDocIdSet = new HashSet<>(requireNonNull(irrelevantDocIds));
    return precisionChecker((DOC doc) -> !irrelevantDocIdSet.contains(id.apply(doc)), criterion);
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionChecker(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
    return new Checker.DocsMetric<DOC, RESP>() {
      Metric.Precision<DOC> metric = documentOracle::test;

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
