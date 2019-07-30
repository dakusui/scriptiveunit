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
      return ((double) docs.stream().filter(this::isRelevant).count()) / ((double) docs.size());
    }
  }

  interface Dcg<E> extends Metric<E> {

  }
}
