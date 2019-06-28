package com.github.dakusui.scriptiveunit.model.desc;

import com.github.dakusui.scriptiveunit.model.session.Stage;

import java.util.List;
import java.util.function.Predicate;

public interface ConstraintDefinition extends Predicate<Stage> {
  boolean test(Stage stage);

  List<String> involvedParameterNames();

}
