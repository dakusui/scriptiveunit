package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.actionunit.Action;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ActionFactory {
  Action setUpBeforeAll(Memo memo);

  Action setUpFixture(Memo memo, Tuple fixture);

  Action before(Memo memo, Tuple testCase);

  int numOracles();

  Action given(Memo memo, int oracleId, Tuple testCase);

  Action when(Memo memo, int oracleId, Tuple testCase);

  Action then(Memo memo, int oracleId, Tuple testCase);

  Action handleFailure(Memo memo, Tuple testCase);

  Action after(Memo memo, Tuple testCase);

  Action tearDownFixture(Memo memo, Tuple fixture);

  Action tearDownBeforeAll(Memo memo);

  static ActionFactory create(TestSuiteDescriptor descriptor) {
    return new ActionFactory() {
      @Override
      public Action setUpBeforeAll(Memo memo) {
        return descriptor.getSetUpBeforeAllActionFactory().apply(createStage());
      }

      @Override
      public Action setUpFixture(Memo memo, Tuple fixture) {
        return null;
      }

      @Override
      public Action before(Memo memo, Tuple testCase) {
        return null;
      }

      @Override
      public int numOracles() {
        return 0;
      }

      @Override
      public Action given(Memo memo, int oracleId, Tuple testCase) {
        return null;
      }

      @Override
      public Action when(Memo memo, int oracleId, Tuple testCase) {
        return null;
      }

      @Override
      public Action then(Memo memo, int oracleId, Tuple testCase) {
        return null;
      }

      @Override
      public Action handleFailure(Memo memo, Tuple testCase) {
        return null;
      }

      @Override
      public Action after(Memo memo, Tuple testCase) {
        return null;
      }

      @Override
      public Action tearDownFixture(Memo memo, Tuple fixture) {
        return null;
      }

      @Override
      public Action tearDownBeforeAll(Memo memo) {
        return null;
      }

      private Stage createStage() {
        return null;
      }
    };
  }
}
