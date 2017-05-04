package com.github.dakusui.scriptiveunit.model;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.List;

public interface FactorSpaceDescriptor {
  List<Factor> getFactors();

  List<Constraint> getConstraints();
}
