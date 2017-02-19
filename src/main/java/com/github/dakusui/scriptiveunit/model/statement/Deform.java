package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.scriptiveunit.model.Stage;
import com.github.dakusui.scriptiveunit.model.func.Func;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * User defined form.
 */
public class Deform implements Form {
  private final List<Object> bodyClause;

  public Deform(List<Object> bodyClause) {
    this.bodyClause = bodyClause;
  }

  @Override
  public Func<? extends Stage, ?> apply(Arguments arguments) {
    List<?> argValues = stream(arguments.spliterator(), false).map(Statement::execute).collect(toList());
    return new Func<Stage, Object>() {
      @Override
      public Object apply(Stage input) {
        return new Stage() {
          @Override
          public Statement.Factory getStatementFactory() {
            return input.getStatementFactory();
          }

          @Override
          public Tuple getTestCaseTuple() {
            return input.getTestCaseTuple();
          }

          @Override
          public <RESPONSE> RESPONSE response() {
            return input.response();
          }

          @Override
          public Type getType() {
            return input.getType();
          }

          @Override
          public <T> T getArgument(int index) {
            return (T) argValues.get(index);
          }

          @Override
          public int sizeOfArguments() {
            return argValues.size();
          }
        }.getStatementFactory().create(bodyClause).execute();
      }
    };
  }

  @Override
  public boolean isAccessor() {
    return false;
  }
}
