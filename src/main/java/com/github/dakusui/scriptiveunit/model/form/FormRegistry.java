package com.github.dakusui.scriptiveunit.model.form;

import com.github.dakusui.scriptiveunit.exceptions.ConfigurationException;

import java.util.*;

import static com.github.dakusui.scriptiveunit.utils.DriverUtils.getFormsFromImportedFieldsInObject;
import static java.util.stream.Collectors.toMap;

public interface FormRegistry {
  Optional<Form> lookUp(String name);

  static FormRegistry load(Object driverObject) {
    return getFormRegistry(driverObject);
  }

  static FormRegistry getFormRegistry(Object driverObject) {
    Map<String, Form> formMap = loadFormMapFromDriverObject(driverObject);
    Map<String, List<Form>> allFoundForms = new HashMap<>();
    formMap.values().forEach((Form each) -> {
      allFoundForms.computeIfAbsent(each.getName(), name -> new LinkedList<>());
      allFoundForms.get(each.getName()).add(each);
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

  static Map<String, Form> loadFormMapFromDriverObject(Object driverObject) {
    Map<String, Form> formMap = new HashMap<>();
    getFormsFromImportedFieldsInObject(driverObject)
        .forEach((Form each) -> {
          formMap.put(each.getName(), each);
        });
    return formMap;
  }
}
