package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;

import java.util.List;

public interface ParameterSpaceDescriptor {
  List<Parameter> getParameters();

  List<Constraint> getConstraints();
}
