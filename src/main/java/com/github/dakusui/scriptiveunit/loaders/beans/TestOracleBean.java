package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.model.desc.TestSuiteDescriptor;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestItem;
import com.github.dakusui.scriptiveunit.model.desc.testitem.TestOracle;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;

public abstract class TestOracleBean {
  private final String       description;
  private final List<Object> givenClause;
  private final List<Object> whenClause;
  private final List<Object> thenClause;
  private final List<Object> onFailureClause;
  private final List<Object> afterClause;
  private final List<Object> beforeClause;

  protected TestOracleBean(String description, List<Object> beforeClause, List<Object> givenClause, List<Object> whenClause, List<Object> thenClause, List<Object> onFailureClause, List<Object> afterClause) {
    this.description = description;
    this.beforeClause = beforeClause;
    this.givenClause = givenClause;
    this.whenClause = whenClause;
    this.thenClause = thenClause;
    this.onFailureClause = onFailureClause;
    this.afterClause = afterClause;
  }

  /**
   * Test oracles created by this method are not thread safe since invokers ({@code FuncHandler}
   * objects) have their internal states and not created every time the oracles
   * are performed.
   */
  public TestOracle create(int index, TestSuiteDescriptor testSuiteDescriptor) {
    Statement.Factory statementFactory = testSuiteDescriptor.statementFactory();
    return new TestOracleImpl(index, description, beforeClause, givenClause, whenClause, thenClause, onFailureClause, afterClause, statementFactory);
  }

  public static class TestOracleImpl implements TestOracle {
    private final int                 index;
    private final Statement.Factory   statementFactory;
    private       List<Object>        afterClause;
    private       List<Object>        beforeClause;
    private       String              description;
    private       List<Object>        givenClause;
    private       List<Object>        onFailureClause;
    private       List<Object>        thenClause;
    private       List<Object>        whenClause;

    TestOracleImpl(int index, final String description, final List<Object> beforeClause, final List<Object> givenClause, final List<Object> whenClause, final List<Object> thenClause, final List<Object> onFailureClause, final List<Object> afterClause, Statement.Factory statementFactory) {
      this.index = index;
      this.statementFactory = statementFactory;
      this.afterClause = afterClause;
      this.beforeClause = beforeClause;
      this.description = description;
      this.givenClause = givenClause;
      this.onFailureClause = onFailureClause;
      this.thenClause = thenClause;
      this.whenClause = whenClause;
    }

    @Override
    public int getIndex() {
      return index;
    }

    @Override
    public String getDescription() {
      return description;
    }

    public Definition definitionFor(TestItem testItem) {
      return TestOracle.Definition.create(
          statementFactory,
          this.beforeClause,
          this.givenClause,
          this.whenClause,
          this.thenClause,
          this.onFailureClause,
          this.afterClause);
    }
  }
}
