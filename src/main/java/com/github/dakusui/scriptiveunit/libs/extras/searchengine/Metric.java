package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

interface Metric<DOC, REQ extends Request> {
  double calc(List<DOC> docs);

  interface Size<E> extends Metric<E, Request> {
    @Override
    default double calc(List<E> docs) {
      return docs.size();
    }
  }

  interface Precision<DOC> extends Metric<DOC, Request> {
    static
    <DOC, REQ extends Request, RESP extends Response<DOC>> ResponseChecker.ByDocsMetric<DOC, REQ, RESP>
    create(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
      return ResponseChecker.createChecker(criterion, (Precision<DOC>) documentOracle::test);
    }



    boolean isRelevant(DOC doc);

    @Override
    default double calc(List<DOC> docs) {
      if (docs.isEmpty())
        return Double.NaN;
      return ((double) docs.stream().filter(this::isRelevant).count()) / ((double) docs.size());
    }
  }

  interface Dcg<E> extends Metric<E, Request> {
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
    default double calc(List<E> docs) {
      double ret = 0;
      for (int i = 1; i < Math.min(p(), docs.size()); i++) {
        ret += (Math.pow(2, relevancy(docs.get(i))) - 1) / Math.log(i + 1);
      }
      return ret;
    }
  }

  interface IDcg<E> extends Metric<E, Request> {
    Dcg<E> dcg();

  }
}
