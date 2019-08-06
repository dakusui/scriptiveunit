package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.*;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;
import com.github.dakusui.scriptiveunit.model.stage.Stage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.*;
import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.SearchEngineUtils.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SearchEngineSupport<REQ extends Request, RESP extends Response<DOC, REQ>, DOC> {
  private final Predicates predicates = new Predicates();
  private final SearchEngine<REQ, RESP, DOC> searchEngine;
  private final SearchResultEvaluator<DOC> defaultEvaluator;

  public SearchEngineSupport(SearchEngine<REQ, RESP, DOC> searchEngine, SearchResultEvaluator<DOC> defaultEvaluator) {
    this.searchEngine = requireNonNull(searchEngine);
    this.defaultEvaluator = requireNonNull(defaultEvaluator);
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
  public Value<SearchResultEvaluator<DOC>>
  defaultEvaluator() {
    return stage -> this.defaultEvaluator;
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

      @Override
      public String name() {
        return "nonEmpty";
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

      @Override
      public String name() {
        return "numDocsSatisfying[" + cond + "] is " + range;
      }
    };
  }

  @Scriptable
  public Value<SearchResultEvaluator<DOC>>
  evaluatorByKnownRelevantDocIds(ValueList<String> valueDocIds) {
    return stage -> new SearchResultEvaluator<DOC>() {
      Set<String> docIds = valueDocIds.stream().map(each -> each.apply(stage)).collect(toSet());

      @Override
      public double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
        // TODO document this behaviour.
        return 1.0;
      }

      @Override
      public double relevancyOf(DOC doc, String userQuery, List<Request.Option<?>> options) {
        return isRelevant(doc, userQuery, options) ? 1.0 : 0.0;
      }

      @Override
      public boolean isRelevant(DOC doc, String userQuery, List<Request.Option<?>> options) {
        return docIds.contains(searchEngine.idOf(doc));
      }

      @Override
      public String toString() {
        return "evaluatorByKnownRelevantDocIds:" + docIds;
      }
    };
  }

  @Scriptable
  public Value<SearchResultEvaluator<DOC>>
  evaluatorByKnownIrrelevantDocIds(ValueList<String> valueDocIds) {
    return stage -> new SearchResultEvaluator<DOC>() {
      Set<String> docIds = valueDocIds.stream().map(each -> each.apply(stage)).collect(toSet());

      @Override
      public double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
        // TODO document this behaviour.
        return 1.0;
      }

      @Override
      public double relevancyOf(DOC doc, String userQuery, List<Request.Option<?>> options) {
        return isRelevant(doc, userQuery, options) ? 1.0 : 0.0;
      }

      @Override
      public boolean isRelevant(DOC doc, String userQuery, List<Request.Option<?>> options) {
        return !docIds.contains(searchEngine.idOf(doc));
      }

      @Override
      public String toString() {
        return "evaluatorByKnownIrrelevantDocIds:" + docIds;
      }
    };
  }

  @Scriptable
  public Value<SearchResultEvaluator<DOC>>
  evaluatorByLambda(Value<Value<Boolean>> lambda) {
    return stage -> new SearchResultEvaluator<DOC>() {
      @Override
      public double relevancyOfDocumentInIdealSearchResultAt(int position, String userQuery, List<Request.Option<?>> options) {
        // TODO document this behaviour.
        return 1.0;
      }

      @Override
      public double relevancyOf(DOC doc, String userQuery, List<Request.Option<?>> options) {
        return isRelevant(doc, userQuery, options) ? 1.0 : 0.0;
      }

      @Override
      public boolean isRelevant(DOC doc, String userQuery, List<Request.Option<?>> options) {
        Stage wrappedStage = wrapValuesAsArgumentsInStage(
            stage,
            toValue(searchEngine.idOf(doc), doc),
            toValue("userQuery", userQuery),
            toValue("options", options)
        );
        Value<Boolean> booleanValue = lambda.apply(stage);
        return SearchEngineUtils.evaluateValueWithoutListening(wrappedStage, booleanValue);
      }

      @Override
      public String toString() {
        return "evaluatorByLambda:" + lambda.name();
      }
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionBy(Value<SearchResultEvaluator<DOC>> evaluatorValue, Value<Value<Boolean>> criterion) {
    return (Stage stage) -> SearchEngineUtils.evaluateValueWithoutListening(
        stage,
        (Stage s) -> createResponseCheckerByPrecision((
                SearchEngineUtils.printablePredicate(
                    criterion.name(),
                    (Double aDouble) -> {
                      Stage wrappedStage = wrapValueAsArgumentInStage(s, toValue(criterion.name(), aDouble));
                      Value<Boolean> booleanValue = criterion.apply(s);
                      return SearchEngineUtils.evaluateValueWithoutListening(wrappedStage, booleanValue);
                    })),
            evaluatorValue.apply(s)));
  }


  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  precisionAtKby(
      Value<Integer> k,
      Value<SearchResultEvaluator<DOC>> evaluatorValue,
      Value<Predicate<? super Double>> criterion) {
    return stage -> createResponseCheckerByPrecisionAtK(criterion.apply(stage), k.apply(stage), evaluatorValue.apply(stage));
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  dcgBy(Value<Integer> p,
        Value<SearchResultEvaluator<DOC>> evaluatorValue,
        Value<Predicate<? super Double>> criterion) {
    return stage -> createResponseCheckerByDcg(criterion.apply(stage), p.apply(stage), evaluatorValue.apply(stage));
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Double>>
  ndcgBy(Value<Integer> p,
         Value<SearchResultEvaluator<DOC>> evaluatorValue,
         Value<Predicate<? super Double>> criterion) {
    return stage -> createResponseCheckerByNDcg(criterion.apply(stage), p.apply(stage), evaluatorValue.apply(stage));
  }

  @SuppressWarnings("unchecked")
  @Scriptable
  public <T> Value<T> docAttr(Value<DOC> doc, Value<String> attrName) {
    return stage -> (T) searchEngine.valueOf(doc.apply(stage), attrName.apply(stage)).orElseThrow(NoSuchElementException::new);
  }

  private static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>, T>
  Value<Boolean>
  verifyResponse(RESP resp, Value<ResponseChecker<RESP, DOC, T>> responseCheckerValue) {
    return stage -> SearchEngineUtils.evaluateValueWithoutListening(
        stage,
        s -> {
          ResponseChecker<RESP, DOC, T> responseChecker = responseCheckerValue.apply(s);
          T value = responseChecker.transform(resp);
          if (s.getReport().isPresent())
            s.getReport().get().put(responseChecker.name(), value);
          return responseChecker.verify(value);
        }
    );
  }
}
