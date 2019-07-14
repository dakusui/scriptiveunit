package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.exceptions.ConfigurationException;
import com.github.dakusui.scriptiveunit.utils.DriverUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public interface FormRegistry {
  Optional<Form> lookUp(String name);

  static FormRegistry load(Object driverObject) {
    Map<String, List<Form>> allFoundForms = new HashMap<>();
    Map<String, Form> formMap = new HashMap<>();
    DriverUtils.getFormsFromImportedFieldsInObject(driverObject)
        .stream()
        .peek((Form each) -> {
          allFoundForms.computeIfAbsent(each.getName(), name -> new LinkedList<>());
          allFoundForms.get(each.getName()).add(each);
        })
        .forEach((Form each) -> {
          formMap.put(each.getName(), each);
        });
    Map<String, List<Form>> duplicatedForms = allFoundForms
        .entrySet()
        .stream()
        .filter((Map.Entry<String, List<Form>> each) -> each.getValue().size() > 1)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (!duplicatedForms.isEmpty())
      throw ConfigurationException.duplicatedFormsAreFound(duplicatedForms);
    return name -> Optional.ofNullable(formMap.get(name));
  }
}
