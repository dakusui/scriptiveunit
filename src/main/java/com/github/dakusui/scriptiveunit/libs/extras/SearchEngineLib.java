package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.*;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.ByDocs;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SearchEngineLib<REQ extends Request, RESP extends Response<DOC, REQ>, DOC> {
  private final Predicates                   predicates = new Predicates();
  private final SearchEngine<REQ, RESP, DOC> searchEngine;
  private final SearchResultEvaluator<DOC>   evaluator;

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
    return stage -> predicates.allOf(
        ValueList.create(
            checkerValues.stream()
                .map((Value<ResponseChecker<RESP, DOC, Object>> each) -> verifyResponse(respValue, each))
                .collect(Collectors.toList())))
        .apply(stage);
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
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionByKnownRelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> ResponseChecker.createResponseCheckerByPrecision(
        criterion.apply(stage),
        new BiPredicate<DOC, REQ>() {
          Set<String> docIds = valueDocIds.stream().map(each -> each.apply(stage)).collect(toSet());

          @Override
          public boolean test(DOC doc, REQ req) {
            return docIds.contains(searchEngine.idOf(doc));
          }
        }
    );
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionByKnownIrrelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> ResponseChecker.createResponseCheckerByPrecision(
        criterion.apply(stage),
        new BiPredicate<DOC, REQ>() {
          Set<String> docIds = valueDocIds.stream().map(each -> each.apply(stage)).collect(toSet());

          @Override
          public boolean test(DOC doc, REQ req) {
            return !docIds.contains(searchEngine.idOf(doc));
          }
        }
    );
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionByEvaluator(Value<Predicate<? super Double>> criterion) {
    return stage -> ResponseChecker.createResponseCheckerByPrecision(criterion.apply(stage), evaluator);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionKbyEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> k) {
    return stage -> ResponseChecker.createResponseCheckerByPrecisionK(criterion.apply(stage), k.apply(stage), evaluator);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  dcgByEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> p) {
    return stage -> ResponseChecker.createResponseCheckerByDcg(criterion.apply(stage), p.apply(stage), evaluator);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  ndcgByEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> p) {
    return stage -> ResponseChecker.createResponseCheckerByNDcg(criterion.apply(stage), p.apply(stage), evaluator);
  }


  private static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>, T>
  Value<Boolean>
  verifyResponse(Value<RESP> resp, Value<ResponseChecker<RESP, DOC, T>> responseCheckerValue) {
    return stage -> {
      ResponseChecker<RESP, DOC, T> reponseChecker = responseCheckerValue.apply(stage);
      return reponseChecker.verify(reponseChecker.transform(resp.apply(stage)));
    };
  }
}
