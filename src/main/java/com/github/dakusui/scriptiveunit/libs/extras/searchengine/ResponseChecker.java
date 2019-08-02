package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ResponseChecker<REQ extends Request, RESP extends Response<DOC>, DOC, T> {
  T transform(REQ request, RESP response);

  boolean verify(T value);

  interface ByDocs<REQ extends Request, RESP extends Response<DOC>, DOC> extends ResponseChecker<REQ, RESP, DOC, List<DOC>> {

  }

  interface ByDocsMetric<DOC, REQ extends Request,RESP extends Response<DOC>> extends ResponseChecker<REQ, RESP, DOC, Double> {

  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>> ByDocsMetric<DOC, REQ, RESP> precisionCheckerByKnownRelevantDocIds(Collection<String> relevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> relevantDocIds.contains(id.apply(doc)), criterion);
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>> ByDocsMetric<DOC, REQ, RESP> precisionCheckerByKnownIrrelevantDocIds(Collection<String> irrelevantDocIds, Function<DOC, String> id, Predicate<? super Double> criterion) {
    return precisionChecker((DOC doc) -> !irrelevantDocIds.contains(id.apply(doc)), criterion);
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>>
  ByDocsMetric<DOC, REQ, RESP> precisionChecker(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
    return Metric.Precision.create(documentOracle, criterion);
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>>
  ByDocsMetric<DOC, REQ, RESP>
  dcgChecker(Function<DOC, Double> relevancy, int p, Predicate<? super Double> criterion) {
    return createChecker(criterion, Metric.Dcg.create(relevancy, p));
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>>
  ByDocsMetric<DOC, REQ, RESP>
  ndcgChecker(Function<DOC, Double> relevancy, Integer p, double idcg, Predicate<? super Double> criterion) {
    return createChecker(
        criterion,
        (docs) -> idcg != 0 ? Metric.Dcg.create(relevancy, p).calc(docs) / idcg : Double.NaN);
  }

  static <DOC, REQ extends Request, RESP extends Response<DOC>>
  ByDocsMetric<DOC, REQ, RESP> createChecker(Predicate<? super Double> criterion, final Metric<DOC, ? super REQ> metric) {
    return new ByDocsMetric<DOC, REQ, RESP>() {
      Metric<DOC, ? super REQ> dcg = metric;

      @Override
      public Double transform(REQ request, RESP response) {
        return dcg.calc(response.docs());
      }

      @Override
      public boolean verify(Double value) {
        return criterion.test(value);
      }
    };
  }
}
