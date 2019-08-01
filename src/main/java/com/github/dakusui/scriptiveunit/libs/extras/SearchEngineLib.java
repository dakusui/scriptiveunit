package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Request;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.Response;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngine;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SearchEngineLib<REQ extends Request, RESP extends Response<DOC>, DOC> {
  private final Predicates                   predicates = new Predicates();
  private final SearchEngine<REQ, RESP, DOC> searchEngine;

  public SearchEngineLib(SearchEngine<REQ, RESP, DOC> searchEngine) {
    this.searchEngine = requireNonNull(searchEngine);
  }

  @Scriptable
  public Value<RESP> issueRequest(Value<String> userQuery, Value<Integer> offset, Value<Integer> hits, ValueList<Request.Option<?>> options) {
    return s -> searchEngine.service(searchEngine.requestBuilder()
        .offset(offset.apply(s))
        .hits(hits.apply(s))
        .addOptions(options.stream()
            .map((Value<Request.Option<?>> each) -> each.apply(s))
            .collect(toList()))
        .build());
  }

  @Scriptable
  public <T> Value<Request.Option<T>> option(Value<String> name, Value<T> value) {
    return s -> new Request.Option<T>() {
      @Override
      public String name() {
        return name.apply(s);
      }

      @Override
      public Optional<T> value() {
        return Optional.of(value.apply(s));
      }
    };
  }

  @Scriptable
  public <T> Value<Request.Option<T>> emptyOption(Value<String> name) {
    return s -> new Request.Option<T>() {
      @Override
      public String name() {
        return name.apply(s);
      }

      @Override
      public Optional<T> value() {
        return Optional.empty();
      }
    };
  }

  @Scriptable
  public Value<Boolean>
  verifyResponseWith(Value<REQ> reqValue, Value<RESP> respValue, ValueList<ResponseChecker<REQ, RESP, DOC, ? super Object>> checkerValues) {
    return stage -> {
      RESP resp = respValue.apply(stage);
      REQ req = reqValue.apply(stage);
      return predicates.allOf(
          ValueList.create(
              checkerValues.stream()
                  .map((Value<ResponseChecker<REQ, RESP, DOC, Object>> each) -> evaluate(req, resp, each.apply(stage)))
                  .collect(Collectors.toList())))
          .apply(stage);
    };
  }

  @Scriptable
  public Value<ResponseChecker<REQ, RESP, DOC, List<DOC>>> nonEmpty() {
    return stage -> new ByDocs<REQ, RESP, DOC>() {
      @Override
      public List<DOC> transform(REQ request, RESP response) {
        return response.docs();
      }

      @Override
      public boolean verify(List<DOC> value) {
        return !value.isEmpty();
      }
    };
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  dcgBy(Value<Predicate<? super Double>> criterion, Value<Function<DOC, Double>> relevance, Value<Integer> p) {
    return stage -> dcgChecker(relevance.apply(stage), p.apply(stage), criterion.apply(stage));
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  ndcgBy(Value<Predicate<? super Double>> criterion, Value<Function<DOC, Double>> relevance, Value<Function<REQ, Double>> iDcgValue, Value<Integer> p) {
    return stage -> ndcgChecker(relevance.apply(stage), p.apply(stage), iDcgValue.apply(stage).apply(null), criterion.apply(stage));
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  precisionBy(Value<Predicate<? super Double>> criterion, Value<Predicate<DOC>> cond) {
    return stage -> precisionChecker(cond.apply(stage), criterion.apply(stage));
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  precisionByKnownRelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownRelevantDocIds(
        valueDocIds
            .stream()
            .map(each -> each.apply(stage))
            .collect(toSet()), this::idOf, criterion.apply(stage));
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  precisionByKnownIrrelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> precisionCheckerByKnownIrrelevantDocIds(
        valueDocIds.stream()
            .map(each -> each.apply(stage))
            .collect(toSet()), this::idOf, criterion.apply(stage));
  }

  @Scriptable
  public Value<ByDocsMetric<DOC, REQ, RESP>>
  precisionIs(Value<Predicate<DOC>> cond, Value<Predicate<? super Double>> criterion) {
    return stage -> precisionChecker(cond.apply(stage), criterion.apply(stage));
  }

  private String idOf(DOC doc) {
    return searchEngine.idOf(doc);
  }

  private static <DOC, REQ extends Request, RESP extends Response<DOC>, T> Value<Boolean>
  evaluate(REQ req, RESP resp, ResponseChecker<REQ, RESP, DOC, T> checker) {
    return stage -> checker.verify(checker.transform(req, resp));
  }
}
