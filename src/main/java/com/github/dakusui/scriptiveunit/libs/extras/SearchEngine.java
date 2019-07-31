package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

abstract public class SearchEngine<REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<DOC>, DOC> {
  private final Predicates predicates = new Predicates();

  @Scriptable
  Value<RESP> issueRequest(Value<REQ> request) {
    return s -> service(request.apply(s));
  }

  @Scriptable
  public Value<Boolean> verifyResponseWith(Value<RESP> respValue, ValueList<Checker<DOC, RESP, ? super Object>> checkerValues) {
    return stage -> {
      RESP resp = respValue.apply(stage);
      return predicates.allOf(
          ValueList.create(
              checkerValues.stream()
                  .map((Value<Checker<DOC, RESP, Object>> each) -> evaluate(resp, each))
                  .collect(Collectors.toList())))
          .apply(stage);
    };
  }

  @Scriptable
  public Value<Checker> nonEmpty() {
    return stage -> new Checker.Docs<DOC, RESP>() {
      @Override
      public List<DOC> transform(RESP response) {
        return response.docs();
      }

      @Override
      public boolean verify(List<DOC> value) {
        return !value.isEmpty();
      }
    };
  }

  @Scriptable
  public Value<Checker.DocsMetric<DOC, RESP>> dcgBy(Value<Predicate<? super Double>> criterion, Value<Function<DOC, Double>> relevance, Value<Integer> p) {
    return stage -> dcgChecker(relevance.apply(stage), criterion.apply(stage), p.apply(stage));
  }

  @Scriptable
  public Value<Checker.DocsMetric<DOC, RESP>> precisionBy(Value<Predicate<? super Double>> criterion, Value<Predicate<DOC>> cond) {
    return stage -> precisionChecker(cond.apply(stage), criterion.apply(stage));
  }

  @Scriptable
  public Value<Checker.DocsMetric<DOC, RESP>> precisionByKnownRelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownRelevantDocIds(
        valueDocIds
            .stream()
            .map(each -> each.apply(stage))
            .collect(toSet()), this::idOf, criterion.apply(stage));
  }

  @Scriptable
  public Value<Checker.DocsMetric<DOC, RESP>> precisionByKnownIrrelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownIrrelevantDocIds(valueDocIds.stream().map(each -> each.apply(stage)).collect(toSet()), this::idOf, criterion.apply(stage));
  }

  @Scriptable
  public Value<Checker.DocsMetric<DOC, RESP>> precisionIs(Value<Predicate<DOC>> cond, Value<Predicate<? super Double>> criterion) {
    return stage -> precisionChecker(cond.apply(stage), criterion.apply(stage));
  }


  protected abstract RESP service(REQ request);

  protected abstract String idOf(DOC doc);

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionCheckerByKnownRelevantDocIds(Collection<String> relevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> relevantDocIds.contains(id.apply(doc)), criterion);
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionCheckerByKnownIrrelevantDocIds(Collection<String> irrelevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> !irrelevantDocIds.contains(id.apply(doc)), criterion);
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> precisionChecker(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
    return createChecker(criterion, (Metric.Precision<DOC>) documentOracle::test);
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> dcgChecker(Function<DOC, Double> relevancy, Predicate<? super Double> criterion, Integer p) {
    return createChecker(criterion, new Metric.Dcg<DOC>() {
      @Override
      public int p() {
        return p;
      }

      @Override
      public double relevancy(DOC doc) {
        return relevancy.apply(doc);
      }
    });
  }

  private static <DOC, RESP extends Response<DOC>> Checker.DocsMetric<DOC, RESP> createChecker(Predicate<? super Double> criterion, final Metric<DOC> metric) {
    return new Checker.DocsMetric<DOC, RESP>() {
      Metric<DOC> dcg = metric;

      @Override
      public Double transform(RESP response) {
        return dcg.calc(response.docs());
      }

      @Override
      public boolean verify(Double value) {
        return criterion.test(value);
      }
    };
  }

  private static <DOC, RESP extends SearchEngine.Response<DOC>, T> Value<Boolean> evaluate(RESP resp, Value<Checker<DOC, RESP, T>> checkerValue) {
    return stage -> {
      Checker<DOC, RESP, T> checker = checkerValue.apply(stage);
      return checker.verify(checker.transform(resp));
    };
  }

  interface Request {
  }

  interface Response<DOC> {
    List<DOC> docs();
  }

  interface Checker<DOC, RESP extends SearchEngine.Response<DOC>, T> {
    T transform(RESP response);

    boolean verify(T value);

    interface Docs<DOC, RESP extends SearchEngine.Response<DOC>> extends Checker<DOC, RESP, List<DOC>> {

    }

    interface DocsMetric<DOC, RESP extends SearchEngine.Response<DOC>> extends Checker<DOC, RESP, Double> {

    }
  }
}
