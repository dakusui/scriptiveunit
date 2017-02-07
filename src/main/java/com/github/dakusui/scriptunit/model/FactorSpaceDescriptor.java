package com.github.dakusui.scriptunit.model;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.framework.TestSuite;

import java.util.List;

public interface FactorSpaceDescriptor {
  List<Factor> getFactors();

  List<TestSuite.Predicate> getConstraints();
}
