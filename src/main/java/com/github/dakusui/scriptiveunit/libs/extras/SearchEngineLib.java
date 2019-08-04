package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.*;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.*;
import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngineUtils.toValue;
import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngineUtils.wrapValueAsArgumentInStage;
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
  verifyResponseWith(ValueList<ResponseChecker<RESP, DOC, ? super Object>> checkerValues) {
    return stage -> SearchEngineUtils.evaluateValueWithoutListening(
        stage,
        s -> predicates.allOf(
            ValueList.create(
                checkerValues.stream()
                    .map((Value<ResponseChecker<RESP, DOC, Object>> each) ->
                        SearchEngineUtils.evaluateValueWithoutListening(
                            s,
                            ss -> verifyResponse(ss.<RESP>response().orElseThrow(IllegalStateException::new), each)))
                    .collect(toList()))).apply(s));
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Boolean>> nonEmpty() {
    return stage -> new ResponseChecker<RESP, DOC, Boolean>() {
      @Override
      public Boolean transform(RESP response) {
        return response.docs().stream().findAny().isPresent();
      }

      @Override
      public boolean verify(Boolean value) {
        return value;
      }
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, List<DOC>>>
  numDocsSatisfying(Value<Predicate<? super Integer>> range, Value<Predicate<? super DOC>> condValue) {
    return stage -> new ResponseChecker<RESP, DOC, List<DOC>>() {
      Predicate<? super DOC> cond = condValue.apply(stage);

      @Override
      public List<DOC> transform(RESP response) {
        return response.docs().stream().filter(cond).collect(toList());
      }

      @Override
      public boolean verify(List<DOC> value) {
        return range.apply(stage).test(value.size());
      }
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionByKnownRelevantDocIds(Value<Predicate<? super Double>> criterion, ValueList<String> valueDocIds) {
    return stage -> createResponseCheckerByPrecision(
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
    return stage -> createResponseCheckerByPrecision(
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
  precisionByEvaluator(Value<Value<Boolean>> criterion) {
    return stage -> SearchEngineUtils.evaluateValueWithoutListening(
        stage,
        s -> createResponseCheckerByPrecision((
                SearchEngineUtils.printablePredicate(
                    criterion.name(),
                    aDouble -> {
                      Stage wrappedStage = wrapValueAsArgumentInStage(s, toValue(criterion.name(), aDouble));
                      Value<Boolean> booleanValue = criterion.apply(s);
                      return SearchEngineUtils.evaluateValueWithoutListening(wrappedStage, booleanValue);
                    })),
            evaluator));
  }


  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionKbyEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> k) {
    return stage -> createResponseCheckerByPrecisionK(criterion.apply(stage), k.apply(stage), evaluator);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  dcgByEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> p) {
    return stage -> createResponseCheckerByDcg(criterion.apply(stage), p.apply(stage), evaluator);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  ndcgByEvaluator(Value<Predicate<? super Double>> criterion, Value<Integer> p) {
    return stage -> createResponseCheckerByNDcg(criterion.apply(stage), p.apply(stage), evaluator);
  }

  private static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>, T>
  Value<Boolean>
  verifyResponse(RESP resp, Value<ResponseChecker<RESP, DOC, T>> responseCheckerValue) {
    return stage -> SearchEngineUtils.evaluateValueWithoutListening(
        stage,
        s -> {
          ResponseChecker<RESP, DOC, T> responseChecker = responseCheckerValue.apply(s);
          return responseChecker.verify(responseChecker.transform(resp));
        }
    );
  }
}
