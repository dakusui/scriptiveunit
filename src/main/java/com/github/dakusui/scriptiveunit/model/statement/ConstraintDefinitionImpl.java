package com.github.dakusui.scriptiveunit.model.statement;

import com.github.dakusui.scriptiveunit.loaders.beans.BeanUtils;
import com.github.dakusui.scriptiveunit.model.desc.ConstraintDefinition;
import com.github.dakusui.scriptiveunit.model.form.FormInvoker;
import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.List;

import static com.github.dakusui.scriptiveunit.model.form.FormInvoker.createMemo;
import static java.util.Objects.requireNonNull;

public class ConstraintDefinitionImpl implements ConstraintDefinition {
  private final Statement statement;

  public ConstraintDefinitionImpl(Statement statement) {
    this.statement = statement;
  }

  @Override
  public boolean test(Stage stage) {
    return requireNonNull(
        BeanUtils.<Boolean>toForm(
            statement,
            FormInvoker.create(createMemo()))
            .apply(stage));
  }

  @Override
  public List<String> involvedParameterNames() {
    return Statement.Utils.involvedParameters(statement);
  }
}
