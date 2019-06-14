package com.github.dakusui.scriptiveunit;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;

import java.util.List;

enum ScriptiveUnitUtils {
  ;

  /**
   * Extract all the factors whose values are not changed among in the test cases.
   *
   * @param parameters A list of all the parameters used  in the test suite
   * @return A tuple that holds unmodified factors and their values.
   */
  static Tuple createCommonFixture(List<Parameter> parameters) {
    Tuple.Builder b = new Tuple.Builder();
    parameters.stream()
        .filter((Parameter in) -> in instanceof Parameter.Simple)
        .filter((Parameter in) -> in.getKnownValues().size() == 1)
        .forEach((Parameter in) -> b.put(in.getName(), in.getKnownValues().get(0)));
    return b.build();
  }
}
