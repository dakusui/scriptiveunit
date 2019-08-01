package com.github.dakusui.scriptiveunit.libs.extras;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

interface Metric<DOC, REQ extends SearchEngine.Request> {
  double calc(List<DOC> docs, REQ request);

  interface Size<E> extends Metric<E, SearchEngine.Request> {
    @Override
    default double calc(List<E> docs, SearchEngine.Request request) {
      return docs.size();
    }
  }

  interface Precision<E> extends Metric<E, SearchEngine.Request> {
    static
    <DOC, REQ extends SearchEngine.Request, RESP extends SearchEngine.Response<DOC>> ResponseChecker.ByDocsMetric<DOC, REQ, RESP>
    create(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
      return ResponseChecker.createChecker(criterion, (Precision<DOC>) documentOracle::test);
    }

    boolean isRelevant(E doc);

    @Override
    default double calc(List<E> docs, SearchEngine.Request request) {
      if (docs.isEmpty())
        return Double.NaN;
      return ((double) docs.stream().filter(this::isRelevant).count()) / ((double) docs.size());
    }
  }

  interface Dcg<E> extends Metric<E, SearchEngine.Request> {
    static <DOC> Dcg<DOC> create(Function<DOC, Double> relevancy, int p) {
      return new Dcg<DOC>() {
        @Override
        public int p() {
          return p;
        }

        @Override
        public double relevancy(DOC doc) {
          return relevancy.apply(doc);
        }
      };
    }

    int p();

    double relevancy(E doc);

    @Override
    default double calc(List<E> docs, SearchEngine.Request request) {
      double ret = 0;
      for (int i = 1; i < Math.min(p(), docs.size()); i++) {
        ret += (Math.pow(2, relevancy(docs.get(i))) - 1) / Math.log(i + 1);
      }
      return ret;
    }
  }

  interface IDcg<E> extends Metric<E, SearchEngine.Request> {
    Dcg<E> dcg();

  }
}
