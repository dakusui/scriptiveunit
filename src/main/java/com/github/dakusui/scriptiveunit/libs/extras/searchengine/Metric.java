package com.github.dakusui.scriptiveunit.libs.extras.searchengine;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import static java.lang.Math.log;
import static java.lang.Math.pow;

interface Metric<DOC> {
  double calc(List<? extends DOC> docs);

  interface Size<DOC> extends Metric<DOC> {
    @Override
    default double calc(List<? extends DOC> docs) {
      return docs.size();
    }
  }

  interface Precision<DOC> extends Metric<DOC> {
    static <DOC, REQ extends Request, RESP extends Response<DOC, REQ>> ResponseChecker.ByDocsMetric<DOC, RESP>
    create(Predicate<DOC> documentOracle, Predicate<? super Double> criterion) {
      return ResponseChecker.createChecker(criterion, (Precision<DOC>) documentOracle::test);
    }


    boolean isRelevant(DOC doc);

    @Override
    default double calc(List<? extends DOC> docs) {
      if (docs.isEmpty())
        return Double.NaN;
      return ((double) docs.stream().filter(this::isRelevant).count()) / ((double) docs.size());
    }
  }

  interface Dcg<DOC> extends Metric<DOC> {
    static <DOC> Dcg<DOC> create(int p, Function<DOC, Double> relevancy) {
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
    @Override
    default double calc(List<? extends DOC> docs) {
      return Utils.dcg(this.p(), i -> i < docs.size() ?
          relevancy(docs.get(i)) :
          0);
    }

    int p();

    double relevancy(DOC doc);
  }

  interface NDcg<DOC> extends Metric<DOC> {
    static <DOC> NDcg<DOC> create(int p, Function<DOC, Double> relevancy, IntFunction<Double> relevancyForIdealSearchResult) {
      return new NDcg<DOC>() {
        @Override
        public Dcg<DOC> dcg() {
          return Dcg.create(p, relevancy);
        }

        @Override
        public double idcg() {
          return Utils.dcg(p, relevancyForIdealSearchResult);
        }
      };
    }

    Dcg<DOC> dcg();

    double idcg();

    @Override
    default double calc(List<? extends DOC> docs) {
      return dcg().calc(docs) / idcg();
    }
  }

  enum Utils {
    ;

    static double log2(double num) {
      return log(num) / log(2);
    }

    public static <E> double dcg(int p, IntFunction<Double> relevancyFunction) {
      double ret = 0;
      for (int i = 1; i < p; i++) {
        double relevancy = relevancyFunction.apply(i);
        ret += (pow(2, relevancy) - 1) / log2(i + 1);
      }
      return ret;
    }
  }
}
