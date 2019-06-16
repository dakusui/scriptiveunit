package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.model.TestOracle;
import com.github.dakusui.scriptiveunit.model.TestSuiteDescriptor;
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

  public TestOracleBean(String description, List<Object> beforeClause, List<Object> givenClause, List<Object> whenClause, List<Object> thenClause, List<Object> onFailureClause, List<Object> afterClause) {
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
    return new TestOracle.Impl(index, description, beforeClause, givenClause, whenClause, thenClause, onFailureClause, afterClause, testSuiteDescriptor, statementFactory);
  }
}
