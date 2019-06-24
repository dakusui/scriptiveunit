package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.scriptiveunit.model.form.handle.FormUtils;
import com.github.dakusui.scriptiveunit.model.desc.ConstraintDefinition;
import com.github.dakusui.scriptiveunit.model.session.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class ConstraintDefinitionImpl implements ConstraintDefinition {
  private final Statement statement;

  ConstraintDefinitionImpl(Statement statement) {
    this.statement = statement;
  }

  @Override
  public boolean test(Stage stage) {
    return requireNonNull(FormUtils.INSTANCE.<Boolean>toForm(statement).apply(stage));
  }

  @Override
  public List<String> involvedParameterNames() {
    return FormUtils.involvedParameters(statement);
  }
}
