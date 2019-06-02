package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;

import java.util.List;

public interface ActionFactory {
  Action setUpBeforeAll(Stage stage);

  Action setUpFixture(Stage stage, Tuple fixture);

  Action before(Stage stage, Tuple testCase);

  int numOracles();

  Action given(Stage stage, Tuple testCase);

  Action when(Stage stage, Tuple testCase);

  Action then(Stage stage, Tuple testCase);

  Action handleFailure(Stage stage, Tuple testCase);

  Action after(Stage stage, Tuple testCase);

  Action tearDownFixture(Stage stage, Tuple testCase);

  Action tearDownBeforeAll(Stage stage, Tuple testCase);

  static ActionFactory create(TestSuiteDescriptor descriptor) {
    return new ActionFactory() {
      @Override
      public Action setUpBeforeAll(Stage stage) {
        return descriptor.getSetUpBeforeAllActionFactory().apply(stage);
      }

      @Override
      public Action setUpFixture(Stage stage, Tuple fixture) {
        return null;
      }

      @Override
      public Action before(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public int numOracles() {
        return descriptor.getTestOracles().size();
      }

      @Override
      public Action given(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action when(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action then(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action handleFailure(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action after(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action tearDownFixture(Stage stage, Tuple testCase) {
        return null;
      }

      @Override
      public Action tearDownBeforeAll(Stage stage, Tuple testCase) {
        return descriptor.getTearDownAfterAllActionFactory().apply(stage);
      }

      private final Tuple commonFixture = Utils.createCommonFixture(
          descriptor.getFactorSpaceDescriptor().getParameters()
      );
    };
  }

  enum Utils {
    ;

    public static Tuple createCommonFixture(List<Parameter> parameters) {
      Tuple.Builder b = new Tuple.Builder();
      parameters.stream()
          .filter((Parameter in) -> in instanceof Parameter.Simple)
          .filter((Parameter in) -> in.getKnownValues().size() == 1)
          .forEach((Parameter in) -> b.put(in.getName(), in.getKnownValues().get(0)));
      return b.build();
    }
  }
}
