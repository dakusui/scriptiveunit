package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ResponseChecker<RESP extends Response<DOC, ?>, DOC, T> {
  T transform(RESP response);

  boolean verify(T value);

  interface ByDocs<RESP extends Response<DOC, ?>, DOC> extends ResponseChecker<RESP, DOC, List<DOC>> {

  }

  interface ByDocsMetric<DOC, RESP extends Response<DOC, ?>> extends ResponseChecker<RESP, DOC, Double> {

  }

  static
  <DOC, REQ extends Request,RESP extends Response<DOC, REQ>> ByDocsMetric<DOC, RESP>
  precisionCheckerByKnownRelevantDocIds(Collection<String> relevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> relevantDocIds.contains(id.apply(doc)), criterion);
  }

  static
  <DOC, REQ extends Request,RESP extends Response<DOC, REQ>> ByDocsMetric<DOC, RESP>
  precisionCheckerByKnownIrrelevantDocIds(Collection<String> irrelevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> !irrelevantDocIds.contains(id.apply(doc)), criterion);
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>>
  ByDocsMetric<DOC, RESP>
  precisionChecker(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
    return Metric.Precision.create(documentOracle, criterion);
  }

  static <DOC, RESP extends Response<DOC, ?>>
  ByDocsMetric<DOC, RESP> createChecker(Predicate<? super Double> criterion, final Metric<DOC> metric) {
    return new ByDocsMetric<DOC, RESP>() {
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
}
