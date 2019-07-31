package com.github.dakusui.scriptiveunit.libs.extras;

import java.util.List;

interface Metric<E> {
  double calc(List<E> docs);

  interface Size<E> extends Metric<E> {
    @Override
    default double calc(List<E> docs) {
      return docs.size();
    }
  }

  interface Precision<E> extends Metric<E> {
    boolean isRelevant(E doc);

    @Override
    default double calc(List<E> docs) {
      if (docs.isEmpty())
        return Double.NaN;
      return ((double) docs.stream().filter(this::isRelevant).count()) / ((double) docs.size());
    }
  }

  interface Dcg<E> extends Metric<E> {
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
}
