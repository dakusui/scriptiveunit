package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.*;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SearchEngineLib<REQ extends Request, RESP extends Response<DOC, REQ>, DOC> {
  private final Predicates predicates = new Predicates();
  private final SearchEngine<REQ, RESP, DOC> searchEngine;
  private final SearchResultEvaluator<DOC> evaluator;

  public SearchEngineLib(SearchEngine<REQ, RESP, DOC> searchEngine, SearchResultEvaluator<DOC> evaluator) {
    this.searchEngine = requireNonNull(searchEngine);
    this.evaluator = requireNonNull(evaluator);
  }

  @Scriptable
  public Value<RESP> issueRequest(Value<String> userQuery, Value<Integer> offset, Value<Integer> hits, ValueList<Request.Option<?>> options) {
    return s -> searchEngine.service(searchEngine.requestBuilder()
        .userQuery(userQuery.apply(s))
        .offset(offset.apply(s))
        .hits(hits.apply(s))
        .addOptions(options.stream()
            .map((Value<Request.Option<?>> each) -> each.apply(s))
            .collect(toList()))
        .build());
  }

  @Scriptable
  public <T> Value<Request.Option<T>> option(Value<String> name, Value<T> value) {
    return s -> Request.Option.create(name.apply(s), value.apply(s));
  }

  @Scriptable
  public <T> Value<Request.Option<T>> emptyOption(Value<String> name) {
    return s -> Request.Option.empty(name.apply(s));
  }

  @Scriptable
  public Value<Boolean>
  verifyResponseWith(Value<RESP> respValue, ValueList<ResponseChecker<RESP, DOC, ? super Object>> checkerValues) {
    return stage -> {
      RESP resp = respValue.apply(stage);
      return predicates.allOf(
          ValueList.create(
              checkerValues.stream()
                  .map((Value<ResponseChecker<RESP, DOC, Object>> each) -> evaluate(resp, each.apply(stage)))
                  .collect(Collectors.toList())))
          .apply(stage);
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, List<DOC>>> nonEmpty() {
    return stage -> new ByDocs<RESP, DOC>() {
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
  public Value<ByDocsMetric<DOC, RESP>>
  precisionByKnownRelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownRelevantDocIds(
        valueDocIds
            .stream()
            .map(each -> each.apply(stage))
            .collect(toSet()),
        this::idOf,
        criterion.apply(stage));
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionByEvaluator(Value<Predicate<? super Double>> criterion) {
    return stage -> createResponseCheckerByPrecision(
        criterion.apply(stage),
        (each, request) -> evaluator.isRelevant(
            each,
            request.userQuery(),
            request.options()));
  }

  private ResponseChecker<RESP, DOC, Double> createResponseCheckerByPrecision(final Predicate<? super Double> range, final BiPredicate<DOC, REQ> docChecker) {
    return createResponseCheckerByMetric(range, response -> {
      Predicate<DOC> docPredicate = each -> docChecker.test(each, response.request());
      return (double) response.docs()
          .stream()
          .filter(docPredicate)
          .count()
          / (double) response.docs().size();
    });
  }

  private ResponseChecker<RESP, DOC, Double> createResponseCheckerByMetric(Predicate<? super Double> range, final ToDoubleFunction<RESP> metric) {
    return new ResponseChecker<RESP, DOC, Double>() {

      @Override
      public Double transform(RESP response) {
        return metric.applyAsDouble(response);
      }

      @Override
      public boolean verify(Double value) {
        return range.test(value);
      }
    };
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, RESP>>
  precisionByKnownIrrelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownIrrelevantDocIds(
        valueDocIds.stream()
            .map(each -> each.apply(stage))
            .collect(toSet()), this::idOf, criterion.apply(stage));
  }

  private String idOf(DOC doc) {
    return searchEngine.idOf(doc);
  }

  private static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>, T>
  Value<Boolean>
  evaluate(RESP resp, ResponseChecker<RESP, DOC, T> checker) {
    return stage -> checker.verify(checker.transform(resp));
  }
}
