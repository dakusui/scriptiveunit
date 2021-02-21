package com.github.dakusui.scriptiveunit.utils;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;

import java.util.List;

public enum TupleUtils {
  ;

  public static Tuple filterSimpleSingleLevelParametersOut(Tuple tuple, List<Parameter> factors) {
    Tuple.Builder b = new Tuple.Builder();
    factors.stream().filter(each -> !(each instanceof Parameter.Simple))
        .filter(each -> each.getKnownValues().size() > 1)
        .filter(each -> tuple.containsKey(each.getName()))
        .forEach(each -> b.put(each.getName(), tuple.get(each.getName())));
    return b.build();
  }

  public static Tuple append(Tuple tuple, String key, Object value) {
    Tuple.Builder b = new Tuple.Builder();
    b.putAll(tuple);
    b.put(key, value);
    return b.build();
  }

}
