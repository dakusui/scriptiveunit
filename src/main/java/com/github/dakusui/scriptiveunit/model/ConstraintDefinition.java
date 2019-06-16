package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.scriptiveunit.loaders.beans.BeanUtils;
import com.github.dakusui.scriptiveunit.model.func.FuncInvoker;
import com.github.dakusui.scriptiveunit.model.stage.Stage;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.scriptiveunit.model.func.FuncInvoker.createMemo;
import static java.util.Objects.requireNonNull;

public interface ConstraintDefinition extends Predicate<Stage> {
  boolean test(Stage stage);

  List<String> involvedParameterNames();

}
