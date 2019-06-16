package com.github.dakusui.scriptiveunit.loaders.beans;

import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.scriptiveunit.exceptions.ScriptiveUnitException;
import com.github.dakusui.scriptiveunit.model.statement.ConstraintDefinitionImpl;
import com.github.dakusui.scriptiveunit.model.desc.ParameterSpaceDescriptor;
import com.github.dakusui.scriptiveunit.model.session.Session;
import com.github.dakusui.scriptiveunit.model.statement.Statement;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.Class.forName;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A base class for factor space descriptors.
 */
public abstract class FactorSpaceDescriptorBean {
  protected static class ParameterDefinition {
    String       type;
    List<Object> args;

    public ParameterDefinition(String type, List<Object> args) {
      this.type = type;
      this.args = args;
    }
  }

  private final Map<String, ParameterDefinition> parameterDefinitionMap;
  private final List<List<Object>>               constraintList;

  public FactorSpaceDescriptorBean(Map<String, ParameterDefinition> parameterDefinitionMap, List<List<Object>> constraintList) {
    this.parameterDefinitionMap = parameterDefinitionMap;
    this.constraintList = constraintList;
  }

  public ParameterSpaceDescriptor create(Session session, Statement.Factory statementFactory) {
    return new ParameterSpaceDescriptor() {
      @Override
      public List<Parameter> getParameters() {
        return composeParameters(FactorSpaceDescriptorBean.this.parameterDefinitionMap);
      }

      @Override
      public List<Constraint> getConstraints() {
        return constraintList.stream()
            .map(statementFactory::create)
            .map(ConstraintDefinitionImpl::new)
            .map(session::createConstraint)
            .collect(toList());
      }
    };
  }

  @SuppressWarnings("unchecked")
  private List<Parameter> composeParameters(Map<String, ParameterDefinition> factorMap) {
    return requireNonNull(factorMap).keySet().stream()
        .map(
            (String parameterName) -> {
              ParameterDefinition def = requireNonNull(factorMap.get(parameterName));
              switch (def.type) {
              case "simple":
                return Parameter.Simple.Factory.of(validateParameterDefinitionArgsForSimple(def.args)).create(parameterName);
              case "regex":
                return Parameter.Regex.Factory.of(Objects.toString(validateParameterDefinitionArgsForRegex(def.args).get(0))).create(parameterName);
              case "fsm":
                try {
                  return Parameter.Fsm.Factory.of(
                      (Class<? extends FsmSpec<Object>>) forName(Objects.toString(def.args.get(0))),
                      Integer.valueOf(Objects.toString(def.args.get(1)))
                  ).create(parameterName);
                } catch (ClassCastException | ClassNotFoundException e) {
                  throw new RuntimeException(e);
                }
              default:
                throw new RuntimeException(
                    String.format(
                        "unknown type '%s' was given to parameter '%s''s definition.",
                        def.type,
                        parameterName
                    ));
              }
            })
        .collect(Collectors.toList());
  }

  private List<Object> validateParameterDefinitionArgsForSimple(List<Object> def) {
    return def;
  }

  private List<Object> validateParameterDefinitionArgsForRegex(List<Object> def) {
    if (def.size() != 1)
      throw ScriptiveUnitException.fail("").get();
    return def;
  }
}
