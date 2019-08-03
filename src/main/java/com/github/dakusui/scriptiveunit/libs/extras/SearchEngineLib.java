package com.github.dakusui.scriptiveunit.libs.extras;

import com.github.dakusui.scriptiveunit.annotations.Scriptable;
import com.github.dakusui.scriptiveunit.libs.Predicates;
import com.github.dakusui.scriptiveunit.libs.extras.searchengine.*;
import com.github.dakusui.scriptiveunit.model.form.value.Value;
import com.github.dakusui.scriptiveunit.model.form.value.ValueList;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.*;
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
                .collect(toList())))
        .apply(stage);
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, Boolean>> nonEmpty() {
    return stage -> {
      return new ResponseChecker<RESP, DOC, Boolean>() {
        @Override
        public Boolean transform(RESP response) {
          return response.docs().stream().findAny().isPresent();
        }

        @Override
        public boolean verify(Boolean value) {
          return value;
        }
      };
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, DOC>> any(Value<Predicate<? super DOC>> condValue) {
    return stage -> {
      Predicate<? super DOC> cond_ = condValue.apply(stage);
      return createResponseCheckerForAny(cond_);
    };
  }

  @Scriptable
  public Value<ResponseChecker<RESP, DOC, DOC>> all(Value<Predicate<? super DOC>> condValue) {
    return stage -> {
      Predicate<? super DOC> cond_ = condValue.apply(stage);
      return createResponseCheckerForAll(cond_);
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
  precisionByEvaluator(Value<Predicate<? super Double>> criterion) {
    return stage -> createResponseCheckerByPrecision(criterion.apply(stage), evaluator);
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

  @Scriptable
  public Value<Predicate<DOC>> hasAttr(Value<String> attr) {
    return stage -> (Predicate<DOC>) doc -> searchEngine.valueOf(doc, attr.apply(stage)).isPresent();
  }

  @SuppressWarnings("unchecked")
  @Scriptable
  public <T> Value<Function<DOC, T>> attrValue(Value<String> attr) {
    return stage -> (DOC doc) -> searchEngine.valueOf(doc, attr.apply(stage))
        .map(element -> (T) element)
        .orElseThrow(RuntimeException::new);
  }

  @SuppressWarnings("unchecked")
  @Scriptable
  public <T> Value<Predicate<DOC>> attrValueSatisfies(Value<String> attr, Value<Predicate<? super T>> condValue) {
    return stage -> (Predicate<DOC>) doc -> searchEngine.valueOf(doc, attr.apply(stage))
        .map(element -> (T) element)
        .map(element -> condValue.apply(stage).test(element))
        .orElseThrow(RuntimeException::new);
  }

  @SuppressWarnings("unchecked")
  @Scriptable
  public <T> Value<Predicate<DOC>> attrValueEqualsTo(Value<String> attr, Value<T> anotherValue) {
    return stage -> (Predicate<DOC>) doc -> searchEngine.valueOf(doc, attr.apply(stage))
        .map(element -> (T) element)
        .map(element -> Objects.equals(element, anotherValue.apply(stage)))
        .orElseThrow(RuntimeException::new);
  }

  @Scriptable
  public Value<Predicate<DOC>> attrValueMatchesRegex(Value<String> attr, Value<String> regexValue) {
    return stage -> (Predicate<DOC>) doc -> searchEngine.valueOf(doc, attr.apply(stage))
        .map(element -> (String) element)
        .map(element -> Pattern.compile(regexValue.apply(stage)).matcher(element).find())
        .orElseThrow(RuntimeException::new);
  }

  @Scriptable
  public <V extends Comparable<V>> Value<Predicate<DOC>> attrValueGreaterThan(Value<String> attr, Value<V> value) {
    return stage -> compareAttributeValueWith(attr.apply(stage), value.apply(stage), v -> v > 0);
  }

  @Scriptable
  public <V extends Comparable<V>> Value<Predicate<DOC>> attrValueGreaterThanOrEqualTo(Value<String> attr, Value<V> value) {
    return stage -> compareAttributeValueWith(attr.apply(stage), value.apply(stage), v -> v >= 0);
  }

  @Scriptable
  public <V extends Comparable<V>> Value<Predicate<DOC>> attrValueLessThanOrEqualTo(Value<String> attr, Value<V> value) {
    return stage -> compareAttributeValueWith(attr.apply(stage), value.apply(stage), v -> v <= 0);
  }

  @Scriptable
  public <V extends Comparable<V>> Value<Predicate<DOC>> attrValueLessThan(Value<String> attr, Value<V> value) {
    return stage -> compareAttributeValueWith(attr.apply(stage), value.apply(stage), v -> v < 0);
  }

  @SuppressWarnings("unchecked")
  private <V extends Comparable<V>> Predicate<DOC> compareAttributeValueWith(String attrName, V value, IntPredicate predicate) {
    return doc -> searchEngine.valueOf(doc, attrName)
        .map(element -> (V) requireNonNull(element))
        .map(element -> predicate.test(element.compareTo(requireNonNull(value))))
        .orElseThrow(RuntimeException::new);
  }


  private static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>, T>
  Value<Boolean>
  verifyResponse(Value<RESP> resp, Value<ResponseChecker<RESP, DOC, T>> responseCheckerValue) {
    return stage -> {
      ResponseChecker<RESP, DOC, T> responseChecker = responseCheckerValue.apply(stage);
      return responseChecker.verify(responseChecker.transform(resp.apply(stage)));
    };
  }
}
