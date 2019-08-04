package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static com.github.dakusui.scriptiveunit.libs.extras.searchengine.ResponseChecker.Utils.dcg;
import static java.lang.Math.*;
import static java.lang.String.format;

public interface ResponseChecker<RESP extends Response<DOC, ?>, DOC, T> {
  T transform(RESP response);

  boolean verify(T value);

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double> createResponseCheckerByPrecision(
      Predicate<? super Double> range, SearchResultEvaluator<DOC> evaluator) {
    return createResponseCheckerByPrecision(
        range,
        (each, request) -> evaluator.isRelevant(each, request.userQuery(), request.options()));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double> createResponseCheckerByPrecisionK(
      Predicate<? super Double> range,
      int k,
      SearchResultEvaluator<DOC> evaluator) {
    return createResponseCheckerByPrecisionK(
        range,
        k,
        (each, request) -> evaluator.isRelevant(
            each,
            request.userQuery(),
            request.options()));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double> createResponseCheckerByDcg(
      Predicate<? super Double> range, int p, SearchResultEvaluator<DOC> evaluator) {
    return createResponseCheckerByMetric(
        range,
        SearchEngineUtils.printableToDoubleFunction("dcg",
            resp -> dcg(
                p,
                position -> evaluator.relevancyOf(
                    resp.docs().get(position),
                    resp.request().userQuery(),
                    resp.request().options()))));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double> createResponseCheckerByNDcg(
      Predicate<? super Double> range, int p, SearchResultEvaluator<DOC> evaluator) {
    return createResponseCheckerByMetric(
        range,
        SearchEngineUtils.printableToDoubleFunction("nDcg",
            resp -> {
              String userQuery = resp.request().userQuery();
              List<Request.Option<?>> options = resp.request().options();
              return dcg(p, position -> evaluator.relevancyOf(resp.docs().get(position), userQuery, options))
                  / dcg(p, position -> evaluator.relevancyOfDocumentInIdealSearchResultAt(position, userQuery, options));
            }
        ));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double>
  createResponseCheckerByPrecision(final Predicate<? super Double> range, final BiPredicate<DOC, REQ> docChecker) {
    return createResponseCheckerByMetric(
        range,
        SearchEngineUtils.printableToDoubleFunction("precision",
            (RESP response) -> {
              Predicate<DOC> docPredicate = each -> docChecker.test(each, response.request());
              return (double) response.docs()
                  .stream()
                  .filter(docPredicate)
                  .count()
                  / (double) response.docs().size();
            }));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double>
  createResponseCheckerByPrecisionK(final Predicate<? super Double> range, int k, final BiPredicate<DOC, REQ> docChecker) {
    return createResponseCheckerByMetric(
        range,
        SearchEngineUtils.printableToDoubleFunction(format("precision@k{k=%s}", k),
            response -> {
              Predicate<DOC> docPredicate = each -> docChecker.test(each, response.request());
              return (double) response.docs()
                  .subList(0, min(k, response.docs().size()))
                  .stream()
                  .filter(docPredicate)
                  .count()
                  / (double) k;
            }));
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, Double>
  createResponseCheckerByMetric(
      Predicate<? super Double> range,
      final ToDoubleFunction<RESP> metric) {
    return new ResponseChecker<RESP, DOC, Double>() {

      @Override
      public Double transform(RESP response) {
        return metric.applyAsDouble(response);
      }

      @Override
      public boolean verify(Double value) {
        return range.test(value);
      }

      @Override
      public String toString() {
        return format("ResponseCheckerByMetric{metric:<%s>,range:<%s>}", metric, range);
      }
    };
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, DOC>
  createResponseCheckerForAll(Predicate<? super DOC> cond_) {
    return new ResponseChecker<RESP, DOC, DOC>() {
      Predicate<? super DOC> cond = cond_.negate();

      @Override
      public DOC transform(RESP response) {
        return response.docs().stream().filter(cond).findAny().orElse(null);
      }

      @Override
      public boolean verify(DOC counterExample) {
        return counterExample == null;
      }
    };
  }

  static <REQ extends Request, RESP extends Response<DOC, REQ>, DOC>
  ResponseChecker<RESP, DOC, DOC> createResponseCheckerForAny(Predicate<? super DOC> cond_) {
    return new ResponseChecker<RESP, DOC, DOC>() {
      Predicate<? super DOC> cond = cond_;

      @Override
      public DOC transform(RESP response) {
        return response.docs().stream().filter(cond).findAny().orElse(null);
      }

      @Override
      public boolean verify(DOC example) {
        return example != null;
      }
    };
  }

  enum Utils {
    ;

    static double log2(double num) {
      return log(num) / log(2);
    }

    public static double dcg(int p, IntToDoubleFunction documentPositionToRelevancy) {
      double ret = 0;
      for (int i = 1; i < p; i++) {
        double relevancy = documentPositionToRelevancy.applyAsDouble(i);
        ret += (pow(2, relevancy) - 1) / log2(i + 1);
      }
      return ret;
    }
  }
}
